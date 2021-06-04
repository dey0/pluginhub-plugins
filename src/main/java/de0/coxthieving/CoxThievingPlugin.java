package de0.coxthieving;

import javax.inject.Inject;

import com.google.inject.Provides;

import de0.util.CoxUtil;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import static de0.util.CoxUtil.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

@PluginDescriptor(name = "CoX Thieving", description = "Finds bat chests and counts grubs at thieving room in CoX")
public class CoxThievingPlugin extends Plugin {

  @Inject
  private Client client;

  @Inject
  private OverlayManager overlayManager;

  @Inject
  private ChestHighlight overlay;

  @Inject
  private CoxThievingDatabox grubOverlay;

  @Inject
  private ScavHighlight scavOverlay;

  @Inject
  private CoxThievingConfig config;

  static final int CCHEST = 29742; // closed chest
  static final int PCHEST = 29743; // poison chest
  static final int ECHEST = 29744; // empty chest
  static final int GCHEST = 29745; // grubs chest

  private int roomtype = -1;

  private int plane, base_x, base_y;

  int room_base_x, room_base_y;
  int rot, wind;
  byte soln = -1;

  Set<Byte> not_solns = new HashSet<>();

  class GrubCollection {
    String displayname;
    int num_opened;
    int num_with_grubs;
  }

  private int last_grubs;
  int num_grubs;
  GrubCollection gc_local;
  GrubCollection gc_others[] = new GrubCollection[99];
  int gc_others_count = 0;

  @Provides
  CoxThievingConfig getConfig(ConfigManager configManager) {
    return configManager.getConfig(CoxThievingConfig.class);
  }

  @Subscribe
  public void onGameTick(GameTick e) {
    if (client.getVar(Varbits.IN_RAID) == 0) {
      // player has left the raid
      if (roomtype != -1) try {
        shutDown();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      return;
    }
    int plane = client.getPlane();
    int base_x = client.getBaseX();
    int base_y = client.getBaseY();
    if (this.base_x != base_x || this.base_y != base_y || this.plane != plane) {
      // scene was reloaded
      this.base_x = base_x;
      this.base_y = base_y;
      this.plane = plane;
      searchForThieving();
    }
    WorldPoint wp = client.getLocalPlayer().getWorldLocation();
    int x = wp.getX() - client.getBaseX();
    int y = wp.getY() - client.getBaseY();
    int type = CoxUtil.getroom_type(client.getInstanceTemplateChunks()[plane][x / 8][y / 8]);
    if (type != this.roomtype) {
      if (type == THIEVING) {
        // player has entered thieving room
        overlayManager.add(overlay);
        overlayManager.add(grubOverlay);
        overlayManager.add(scavOverlay);
      } else if (this.roomtype == THIEVING) {
        // player has left thieving room
        overlayManager.remove(overlay);
        overlayManager.remove(grubOverlay);
        overlayManager.remove(scavOverlay);
      }
      this.roomtype = type;
    }
  }

  @Subscribe
  public void onGameObjectSpawned(GameObjectSpawned e) {
    GameObject obj = e.getGameObject();
    if (obj.getId() != PCHEST && obj.getId() != ECHEST && obj.getId() != GCHEST) return;

    Point p = e.getTile().getSceneLocation();
    int x = p.getX();
    int y = p.getY();
    int chestX, chestY;
    if (rot == 0) {
      chestX = x - room_base_x;
      chestY = y - room_base_y;
    } else if (rot == 1) {
      chestX = room_base_y - y;
      chestY = x - room_base_x;
    } else if (rot == 2) {
      chestX = room_base_x - x;
      chestY = room_base_y - y;
    } else {
      chestX = y - room_base_y;
      chestY = room_base_x - x;
    }
    byte chestno = coordToChestNo(chestX, chestY);
    boolean opened = false;
    boolean grub = false;
    if (obj.getId() == ECHEST || obj.getId() == GCHEST) {
      byte notsoln = solve(chestno);
      if (notsoln != -1) not_solns.add(notsoln);
      opened = true;
    }
    if (obj.getId() == GCHEST) { // found grubs
      grub = true;
    } else if (obj.getId() == PCHEST && soln == -1) { // found bats or poison
      soln = solve(chestno);
    }
    if (opened) {
      int angle = obj.getOrientation().getAngle() >> 9;
      int px = x + (angle == 1 ? -1 : angle == 3 ? 1 : 0);
      int py = y + (angle == 0 ? -1 : angle == 2 ? 1 : 0);
      for (Player pl : client.getPlayers()) {
        WorldPoint wp = pl.getWorldLocation();
        int plx = wp.getX() - client.getBaseX();// p.getPathX()[0];
        int ply = wp.getY() - client.getBaseY();// p.getPathY()[0];
        if (plx == px && ply == py) {
          if (grub && pl == client.getLocalPlayer()) {
            add_grubs_local();
          } else if (grub) {
            add_grubs_other(pl);
          } else {
            add_empty(pl);
          }
          break;
        }
      }
    }
  }

  Comparator<GrubCollection> comparator = Comparator
      .comparingInt(new ToIntFunction<GrubCollection>() {
        @Override
        public int applyAsInt(GrubCollection v) {
          if (v == gc_local) return -num_grubs;
          return -v.num_with_grubs * config.grubRate() / 100;
        }
      });

  private void add_grubs_local() {
    GrubCollection gc = gc_local;
    if (gc == null) {
      gc = gc_local = new GrubCollection();
      gc.displayname = client.getLocalPlayer().getName();
    }
    int grubs = client.getItemContainer(InventoryID.INVENTORY).count(ItemID.CAVERN_GRUBS);
    num_grubs += grubs - last_grubs;
    last_grubs = grubs;
    gc.num_opened++;
    gc.num_with_grubs++;
  }

  private void add_grubs_other(Player pl) {
    GrubCollection gc = null;
    int hash = pl.getName().hashCode();
    for (int i = 0; i < gc_others_count; i++) {
      if (hash == gc_others[i].displayname.hashCode()) {
        gc = gc_others[i];
        break;
      }
    }
    if (gc == null) {
      gc = gc_others[gc_others_count++] = new GrubCollection();
      gc.displayname = pl.getName();
    }
    gc.num_opened++;
    gc.num_with_grubs++;
    Arrays.sort(gc_others, 0, gc_others_count, comparator);
  }

  private void add_empty(Player pl) {
    GrubCollection gc = gc_local;
    if (gc == null) {
      gc = gc_local = new GrubCollection();
      gc.displayname = client.getLocalPlayer().getName();
    }
    int hash = pl.getName().hashCode();
    if (hash != gc.displayname.hashCode()) {
      gc = null;
      for (int i = 0; i < gc_others_count; i++) {
        if (hash == gc_others[i].displayname.hashCode()) {
          gc = gc_others[i];
          break;
        }
      }
      if (gc == null) {
        gc = gc_others[gc_others_count++] = new GrubCollection();
        gc.displayname = pl.getName();
      }
    }
    gc.num_opened++;
  }

  @Subscribe
  public void onItemContainerChanged(ItemContainerChanged e) {
    if (e.getContainerId() == 93) {
      last_grubs = e.getItemContainer().count(ItemID.CAVERN_GRUBS);
    }
  }

  private void searchForThieving() {
    int[][] templates = client.getInstanceTemplateChunks()[this.plane];
    for (int cx = 0; cx < 13; cx += 4) {
      for (int cy = 0; cy < 13; cy += 4) {
        int template = templates[cx][cy];
        // PP_XXXXXXXXXX_YYYYYYYYYYY_RR0
        int tx = template >> 14 & 0x3FF;
        int ty = template >> 3 & 0x7FF;
        if (CoxUtil.getroom_type(template) == THIEVING) {
          rot = CoxUtil.room_rot(template);
          if (rot == 0) {
            room_base_x = (cx - (tx & 0x3)) << 3;
            room_base_y = (cy - (ty & 0x3)) << 3;
          } else if (rot == 1) {
            room_base_x = (cx - (ty & 0x3)) << 3;
            room_base_y = (cy + (tx & 0x3)) << 3 | 7;
          } else if (rot == 2) {
            room_base_x = (cx + (tx & 0x3)) << 3 | 7;
            room_base_y = (cy + (ty & 0x3)) << 3 | 7;
          } else if (rot == 3) {
            room_base_x = (cx + (ty & 0x3)) << 3 | 7;
            room_base_y = (cy - (tx & 0x3)) << 3;
          }

          wind = CoxUtil.room_winding(template);
        }
      }
    }
  }

  @Override
  protected void shutDown() throws Exception {
    overlayManager.remove(overlay);
    overlayManager.remove(grubOverlay);
    overlayManager.remove(scavOverlay);
    not_solns.clear();
    gc_local = null;
    for (int i = 0; i < gc_others_count; i++)
      gc_others[i] = null;
    gc_others_count = 0;
    num_grubs = 0;
    last_grubs = 0;
    soln = -1;
    roomtype = -1;
  }

  private byte solve(byte poisonchestno) {
    byte[][] solns = ChestData.CHEST_SOLNS[wind][rot];
    for (byte i = 0; i < solns.length; i++)
      for (byte j = 0; j < solns[i].length; j++)
        if (solns[i][j] == poisonchestno) return i;
    return -1;
  }

  private byte coordToChestNo(int x, int y) {
    byte[][] locs = ChestData.CHEST_LOCS[wind];
    for (byte i = 0; i < locs.length; i++)
      if (locs[i][0] == x && locs[i][1] == y) return (byte) (i + 1);
    return -1;
  }

}
