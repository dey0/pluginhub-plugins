package de0.coxvanguards;

import com.google.inject.Provides;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
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

  int solo_base_hp;

  NPC melee, range, mage;
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
        melee = range = mage = null;
        overlayManager.remove(databox);
      }
      this.in_raid = in_raid;
    }
  }

  @Subscribe
  public void onClientTick(ClientTick e) {
    if (melee != null && melee.getHealthRatio() > 0)
      melhp = melee.getHealthRatio();

    if (range != null && range.getHealthRatio() > 0)
      rnghp = range.getHealthRatio();

    if (mage != null && mage.getHealthRatio() > 0)
      maghp = mage.getHealthRatio();
  }

  @Subscribe
  public void onNpcSpawned(NpcSpawned e) {
    NPC npc = e.getNpc();
    if (melee == null && npc.getId() == 7527)
      melee = npc;
    else if (range == null && npc.getId() == 7528)
      range = npc;
    else if (mage == null && npc.getId() == 7529)
      mage = npc;
  }

  @Subscribe
  public void onNpcChanged(NpcChanged e) {
    NPC npc = e.getNpc();
    if (npc.getId() == 7527 && melee == null)
      melee = npc;
    else if (npc.getId() == 7528 && range == null)
      range = npc;
    else if (npc.getId() == 7529 && mage == null)
      mage = npc;
    else if (npc.getId() == 7526) {
      if (npc == melee) {
        melhp_fine = Math.min(solo_base_hp, melhp_fine + 1);
      } else if (npc == range) {
        rnghp_fine = Math.min(solo_base_hp, rnghp_fine + 1);
      } else if (npc == mage) {
        maghp_fine = Math.min(solo_base_hp, maghp_fine + 1);
      }
    }
  }

  @Subscribe
  public void onNpcDespawned(NpcDespawned e) {
    NPC npc = e.getNpc();
    if (npc == melee)
      melee = null;
    else if (npc == range)
      range = null;
    else if (npc == mage)
      mage = null;
  }

  @Subscribe
  public void onHitsplatApplied(HitsplatApplied e) {
    if (!isSolo())
      return;

    if (!(e.getActor() instanceof NPC))
      return;

    NPC npc = (NPC) e.getActor();
    if (npc == null || npc.getId() < 7526 || npc.getId() > 7529)
      return;

    int hs = e.getHitsplat().getHitsplatType();
    if (hs == HitsplatID.HEAL) {
      this.melhp_fine = solo_base_hp;
      this.rnghp_fine = solo_base_hp;
      this.maghp_fine = solo_base_hp;
    } else if (hs == HitsplatID.DAMAGE_ME || hs == HitsplatID.DAMAGE_OTHER
        || hs == HitsplatID.DAMAGE_MAX_ME) {
      int amt = e.getHitsplat().getAmount();
      if (npc == melee) {
        this.melhp_fine -= amt;
      } else if (npc == range) {
        this.rnghp_fine -= amt;
      } else if (npc == mage) {
        this.maghp_fine -= amt;
      }
    }
  }

  boolean isSolo() {
    return client.getVarbitValue(5424) == 1;
  }

  int getSoloBaseHp() {
    int base_hp = 180;
    base_hp = base_hp * client.getLocalPlayer().getCombatLevel() / 126;
    boolean cm = client.getVarbitValue(6385) != 0;
    if (cm)
      base_hp = base_hp * 3 / 2;

    return base_hp;
  }

}
