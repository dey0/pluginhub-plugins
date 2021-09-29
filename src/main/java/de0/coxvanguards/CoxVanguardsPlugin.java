package de0.coxvanguards;

import com.google.inject.Provides;

import de0.util.CoxUtil;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat.HitsplatType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.NPC;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name = "CoX Vanguards", description = "Adds a highlight and indicates HPs for Vanguards")
public class CoxVanguardsPlugin extends Plugin {

  @Inject
  private OverlayManager overlayManager;

  @Inject
  private Client client;

  @Inject
  private CoxVanguardsHighlight highlight;

  @Inject
  private CoxVanguardsDatabox databox;

  private boolean in_raid;
  private int roomtype = -1;

  int solo_base_hp;

  NPC melee, range, magic;
  int melhp, rnghp, maghp;
  int melhp_fine, rnghp_fine, maghp_fine;

  protected void startUp() throws Exception {
    overlayManager.add(highlight);
    overlayManager.add(databox);
  }

  protected void shutDown() throws Exception {
    overlayManager.remove(highlight);
    overlayManager.remove(databox);
  }

  @Provides
  CoxVanguardsConfig provideConfig(ConfigManager configManager) {
    return (CoxVanguardsConfig) configManager.getConfig(CoxVanguardsConfig.class);
  }

  @Subscribe
  public void onGameTick(GameTick e) {
    boolean in_raid = client.getVarbitValue(5432) != 0;
    if (this.in_raid != in_raid) {
      if (in_raid) {
        melhp_fine = rnghp_fine = maghp_fine = solo_base_hp = getSoloBaseHp();
      } else {
        melee = range = magic = null;
      }
      this.in_raid = in_raid;
    }

    int plane = client.getPlane();
    WorldPoint wp = client.getLocalPlayer().getWorldLocation();
    int x = wp.getX() - client.getBaseX();
    int y = wp.getY() - client.getBaseY();
    int type = CoxUtil.getroom_type(client.getInstanceTemplateChunks()[plane][x / 8][y / 8]);
    if (type != this.roomtype) {
      if (type == CoxUtil.VANGUARDS) {
        // player has entered Vanguards room
        overlayManager.add(databox);
      } else if (this.roomtype == CoxUtil.VANGUARDS) {
        // player has left Vanguards room
        overlayManager.remove(databox);
      }
      this.roomtype = type;
    }
  }

  @Subscribe
  public void onClientTick(ClientTick e) {
    if (melee != null && melee.getHealthRatio() > 0) melhp = melee.getHealthRatio();
    if (range != null && range.getHealthRatio() > 0) rnghp = range.getHealthRatio();
    if (magic != null && magic.getHealthRatio() > 0) maghp = magic.getHealthRatio();
  }

  @Subscribe
  public void onNpcSpawned(NpcSpawned e) {
    NPC npc = e.getNpc();
    if (melee == null && npc.getId() == 7527) melee = npc;
    else if (range == null && npc.getId() == 7528) range = npc;
    else if (magic == null && npc.getId() == 7529) magic = npc;
  }

  @Subscribe
  public void onNpcChanged(NpcChanged e) {
    NPC npc = e.getNpc();
    if (npc.getId() == 7527 && melee == null) melee = npc;
    else if (npc.getId() == 7528 && range == null) range = npc;
    else if (npc.getId() == 7529 && magic == null) magic = npc;
    else if (npc.getId() == 7526) {
      if (npc == melee) melhp_fine = Math.min(solo_base_hp, melhp_fine + 1);
      else if (npc == range) rnghp_fine = Math.min(solo_base_hp, rnghp_fine + 1);
      else if (npc == magic) maghp_fine = Math.min(solo_base_hp, maghp_fine + 1);
    }
  }

  @Subscribe
  public void onNpcDespawned(NpcDespawned e) {
    NPC npc = e.getNpc();
    if (npc == melee) melee = null;
    else if (npc == range) range = null;
    else if (npc == magic) magic = null;
  }

  @Subscribe
  public void onHitsplatApplied(HitsplatApplied e) {
    if (!isSolo()) return;

    if (!(e.getActor() instanceof NPC)) return;

    NPC npc = (NPC) e.getActor();
    if (npc == null || npc.getId() < 7526 || npc.getId() > 7529) return;

    HitsplatType hs = e.getHitsplat().getHitsplatType();
    if (hs == HitsplatType.HEAL) {
      this.melhp_fine = solo_base_hp;
      this.rnghp_fine = solo_base_hp;
      this.maghp_fine = solo_base_hp;
    } else if (hs == HitsplatType.DAMAGE_ME || hs == HitsplatType.DAMAGE_OTHER) {
      int amt = e.getHitsplat().getAmount();
      if (npc == melee) this.melhp_fine -= amt;
      else if (npc == range) this.rnghp_fine -= amt;
      else if (npc == magic) this.maghp_fine -= amt;
    }
  }

  boolean isSolo() {
    return client.getVarbitValue(5424) == 1;
  }

  int getSoloBaseHp() {
    int base_hp = 180;
    base_hp = base_hp * client.getLocalPlayer().getCombatLevel() / 126;
    boolean cm = client.getVarbitValue(6385) != 0;
    if (cm) base_hp = base_hp * 3 / 2;

    return base_hp;
  }

}
