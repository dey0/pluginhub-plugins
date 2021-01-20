package de0.coxtimers;

import static de0.util.CoxUtil.*;

import javax.inject.Inject;

import de0.util.CoxUtil;
import de0.util.MiscUtil;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "CoX Timers", description = "Time tracking for CoX rooms")
public class CoxTimersPlugin extends Plugin {

  @Inject
  private Client client;

  // Room state
  private boolean in_raid;
  private int prvtime, prvfltime;
  private int cryp[] = new int[16], cryx[] = new int[16], cryy[] = new int[16];

  // Olm state
  private int olm_phase;

  // Misc state
  private boolean iceout, treecut;

  @Subscribe
  public void onClientTick(ClientTick e) {
    if (client.getGameState() != GameState.LOGGED_IN)
      return;

    if (clock() == 0 || !client.isInInstancedRegion()) {
      in_raid = false;
      return;
    }
    if (!in_raid) {
      in_raid = true;
      prvtime = 0;
      prvfltime = 0;
      olm_phase = ~0;
      iceout = false;
      treecut = false;
    }
    for (int i = 0; i < 16; i++) {
      if (this.cryp[i] == -1)
        continue;
      int p = cryp[i];
      int x = cryx[i] - client.getBaseX();
      int y = cryy[i] - client.getBaseY();
      if (p != client.getPlane() || x < 0 || x >= 104 || y < 0 || y >= 104) {
        this.cryp[i] = -1;
        continue;
      }
      int flags = client.getCollisionMaps()[p].getFlags()[x][y];
      if ((flags & 0x100) == 0) {
        StringBuilder mes = new StringBuilder();
        mes.append(getroom_sort(i) == 'C' ? "Combat room `" : "Puzzle `");
        mes.append(getroom_name(i));
        mes.append("` complete! Duration: <col=ff0000>");
        mes.append(MiscUtil.to_mmss(clock() - prvtime));
        mes.append("</col> Total: <col=ff0000>");
        mes.append(MiscUtil.to_mmss(clock()));
        mes.append("</col>");

        fc_mes(mes.toString());
        prvtime = clock();
        this.cryp[i] = -1;
      }
    }
  }

  @Subscribe
  public void onChatMessage(ChatMessage e) {
    String mes = e.getMessage();
    if (e.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION
        && mes.startsWith("<col=ef20ff>")) {
      int duration = mes.indexOf("level complete! Duration: <col=ff0000>");
      boolean is_fl_time = duration != -1;
      boolean is_olm_time = mes.contains("<br>");
      boolean is_top_floor = mes.contains("Upper");

      if (!is_fl_time && !is_olm_time)
        return;

      if (is_olm_time) {
        e.getMessageNode().setValue(mes + " Olm duration: <col=ff0000>"
            + MiscUtil.to_mmss(clock() - prvfltime) + "</col>");
      } else if (!is_top_floor) {
        String before = mes.substring(0, duration + 38);
        String after = mes.substring(duration + 38);
        e.getMessageNode()
            .setValue(before + MiscUtil.to_mmss(clock() - prvfltime)
                + "</col> Total: <col=ff0000>" + after);
      }

      prvtime = prvfltime = clock();
    } else if (e.getType() == ChatMessageType.GAMEMESSAGE && mes
        .equals("The Great Olm is giving its all. This is its final stand.")) {
      splitphase();
      olm_phase = 99;
    }
  }

  private void splitphase() {
    StringBuilder mes = new StringBuilder();
    if (olm_phase == 99) {
      mes.append("Olm head");
    } else {
      mes.append("Olm phase ");
      mes.append(++olm_phase);
    }
    mes.append(" duration: <col=ff0000>");
    mes.append(MiscUtil.to_mmss(clock() - prvtime));
    mes.append("</col>");
    if (olm_phase != 99) {
      mes.append(" Total: <col=ff0000>");
      mes.append(MiscUtil.to_mmss(clock()));
      mes.append("</col>");
    }

    fc_mes(mes.toString());
    prvtime = clock();
  }

  @Subscribe
  public void onGameObjectSpawned(GameObjectSpawned e) {
    GameObject go = e.getGameObject();
    switch (go.getId()) {
    case 29881: // Olm spawned
      if (olm_phase < 0) {
        prvtime = clock();
        olm_phase = ~olm_phase;
      }
      break;
    case 30013:
      if (!treecut) {
        StringBuilder mes = new StringBuilder(
            "Muttadile tree cut duration: <col=ff0000>");
        mes.append(MiscUtil.to_mmss(clock() - prvtime));
        mes.append("</col>");
        mes.append(" Total: <col=ff0000>");
        mes.append(MiscUtil.to_mmss(clock()));
        mes.append("</col>");
        fc_mes(mes.toString());
        treecut = true;
      }
      break;
    case 26209: // shamans/thieving/guardians
    case 29741: // mystics
    case 29749: // tightrope
    case 29753: // crabs
    case 29754:
    case 29755:
    case 29756:
    case 29757:
    case 29876: // ice
    case 30016: // vasa
    case 30017: // tekton/vanguards
    case 30018: // mutt
    case 30070: // vespula
      Point pt = go.getSceneMinLocation();
      int p = go.getPlane();
      int x = pt.getX();
      int y = pt.getY();
      int template = client.getInstanceTemplateChunks()[p][x / 8][y / 8];
      int roomtype = getroom_type(template);
      if (roomtype < 16) {
        // add obstacle to list
        cryp[roomtype] = p;
        cryx[roomtype] = x + client.getBaseX();
        cryy[roomtype] = y + client.getBaseY();
      }
      break;
    }
  }

  @Subscribe
  public void onGameObjectDespawned(GameObjectDespawned e) {
    if (e.getGameObject().getId() == ObjectID.LARGE_HOLE_29881) {
      splitphase();
      olm_phase = ~olm_phase;
    }
  }

  private static final int SMOKE_PUFF = 188;

  @Subscribe
  public void onGraphicsObjectCreated(GraphicsObjectCreated e) {
    if (e.getGraphicsObject().getId() == SMOKE_PUFF && !iceout) {
      WorldPoint wp = WorldPoint.fromLocal(client,
          e.getGraphicsObject().getLocation());
      int p = client.getPlane();
      int x = wp.getX() - client.getBaseX();
      int y = wp.getY() - client.getBaseY();
      int template = client.getInstanceTemplateChunks()[p][x / 8][y / 8];
      if (CoxUtil.getroom_type(template) == ICE_DEMON) {
        StringBuilder mes = new StringBuilder(
            "Ice Demon pop duration: <col=ff0000>");
        mes.append(MiscUtil.to_mmss(clock() - prvtime));
        mes.append("</col>");
        mes.append(" Total: <col=ff0000>");
        mes.append(MiscUtil.to_mmss(clock()));
        mes.append("</col>");
        fc_mes(mes.toString());
        iceout = true;
      }
    }
  }

  private void fc_mes(String mes) {
    client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", mes,
        null);
  }

  private int clock() {
    return client.getVarbitValue(6386);
  }

}
