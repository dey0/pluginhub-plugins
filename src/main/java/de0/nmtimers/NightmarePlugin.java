package de0.nmtimers;

import static net.runelite.api.ChatMessageType.GAMEMESSAGE;

import java.util.Arrays;

import javax.inject.Inject;

import de0.util.MiscUtil;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(name = "Nightmare Timers", description = "Nightmare time tracking")
public class NightmarePlugin extends Plugin {

  @Inject
  private Client client;

  @Inject
  private InfoBoxManager infoBoxManager;

  private static final int NM_ROOM_MASK = 0b11_1111111000_11111111000_000;
  private static final int NM_ROOM = 3 << 24 | 120 << 16 | 310 << 5;

  private static final int NIGHTMARE_P1 = 9425;
  private static final int NIGHTMARE_P2 = 9426;
  private static final int NIGHTMARE_P3 = 9427;

  private static final int NIGHTMARE_P1_PILLARS = 9428;
  private static final int NIGHTMARE_P2_PILLARS = 9429;
  private static final int NIGHTMARE_P3_PILLARS = 9430;
  private static final int NIGHTMARE_SLEEPWALKERS = 9431;
  private static final int NIGHTMARE_DOWN = 9432;
  private static final int NIGHTMARE_DEATH = 9433;

  private static final int PHOSANI_VARIANT_1 = 9416;
  private static final int PHOSANI_VARIANT_2 = 9417;
  private static final int PHOSANI_VARIANT_3 = 9418;
  private static final int PHOSANI_VARIANT_4 = 11153;
  private static final int PHOSANI_P4 = 11154;

  private static final int PHOSANI_PILLARS_VARIANT_1 = 9419;
  private static final int PHOSANI_PILLARS_VARIANT_2 = 9420;
  private static final int PHOSANI_PILLARS_VARIANT_3 = 9421;
  private static final int PHOSANI_PILLARS_VARIANT_4 = 11155;
  private static final int PHOSANI_SLEEPWALKERS = 9422;
  private static final int PHOSANI_DOWN = 9423;
  private static final int PHOSANI_DEATH = 9424;

  private NPC nm;
  private int phase;
  private NightmareInfoBox nib;
  boolean dirty;
  int fight_timer = -1, phase_timer = -1, subph_timer = -1;
  int phase_splits[] = new int[6];

  @Override
  protected void shutDown() throws Exception {
    fight_timer = phase_timer = subph_timer = -1;
    nm = null;
    Arrays.fill(phase_splits, -1);
    if (nib != null) {
      infoBoxManager.removeInfoBox(nib);
      nib = null;
    }
  }

  @Subscribe
  public void onNpcSpawned(NpcSpawned e) {
    if (!is_in_noa()) return;
    NPC npc = e.getNpc();
    if (get_noa_npc_type(npc) == -1) return;
    nm = npc;
    fight_timer = phase_timer = subph_timer = client.getTickCount();
    Arrays.fill(phase_splits, -1);
    dirty = true;
    phase = -1;
  }

  @Subscribe
  public void onNpcChanged(NpcChanged e) {
    if (e.getNpc() != nm) return;

    onNightmareChanged(e.getOld().getId());
  }

  @Subscribe
  public void onNpcDespawned(NpcDespawned e) throws Exception {
    if (e.getNpc() != nm) return;

    shutDown();
  }

  @Subscribe
  public void onChatMessage(ChatMessage e) {
    if (e.getMessage().contains("All four totems are fully charged.")) {
      // start sleepwalker phase
      int tick_count = client.getTickCount() + 4;
      mes(tick_count, "pillars");
      subph_timer = tick_count;
      phase_splits[phase] = tick_count - phase_timer;
      if (get_noa_npc_type(nm) == 0 && phase == 3) phase_splits[0] = tick_count - fight_timer;
    }
  }

  private void onNightmareChanged(int oldid) {
    int tick_count = client.getTickCount();
    // full boss: 9432->9425->9428->9431->9426->9429->9431->9427->9430->9433
    switch (nm.getId()) {
    case NIGHTMARE_P1:
    case NIGHTMARE_P2:
    case NIGHTMARE_P3:
    case PHOSANI_VARIANT_1:
    case PHOSANI_VARIANT_2:
    case PHOSANI_VARIANT_3:
    case PHOSANI_VARIANT_4:
    case PHOSANI_P4:
      if (oldid == NIGHTMARE_DOWN || oldid == PHOSANI_DOWN) {
        if (nib == null) {
          nib = new NightmareInfoBox(client, this);
          infoBoxManager.addInfoBox(nib);
        }
        Arrays.fill(phase_splits, -1);
        phase = 1;
        fight_timer = tick_count;
        dirty = false;
      } else {
        mes(tick_count, "sleepwalkers");
        phase++;
      }
      // reset phase and subphase timers
      phase_timer = tick_count;
      subph_timer = tick_count;
      break;
    case PHOSANI_DEATH:
      phase_splits[5] = tick_count - phase_timer;
      phase_splits[0] = tick_count - fight_timer;
    case NIGHTMARE_P1_PILLARS:
    case NIGHTMARE_P2_PILLARS:
    case NIGHTMARE_P3_PILLARS:
    case PHOSANI_PILLARS_VARIANT_1:
    case PHOSANI_PILLARS_VARIANT_2:
    case PHOSANI_PILLARS_VARIANT_3:
    case PHOSANI_PILLARS_VARIANT_4:
      mes(tick_count, "boss");
      subph_timer = tick_count;
      break;
    }
  }

  private void mes(int tick_count, String type) {
    StringBuilder sb = new StringBuilder();
    if (get_noa_npc_type(nm) == 1) sb.append("Phosani's ");
    sb.append("Nightmare P");
    if (phase == -1) sb.append('?');
    else sb.append(phase);
    sb.append(' ').append(type).append(' ');
    sb.append("complete! Duration: <col=ff0000>");
    sb.append(to_mmss(tick_count - subph_timer));
    sb.append("</col>");
    if (!dirty) {
      sb.append(" Total: <col=ff0000>");
      sb.append(to_mmss(tick_count - fight_timer));
      sb.append("</col>");
    }
    client.addChatMessage(GAMEMESSAGE, "", sb.toString(), null);
  }

  private int get_noa_npc_type(NPC npc) {
    if (npc == null) return -1;

    switch (npc.getId()) {
    case NIGHTMARE_P1:
    case NIGHTMARE_P2:
    case NIGHTMARE_P3:
    case NIGHTMARE_P1_PILLARS:
    case NIGHTMARE_P2_PILLARS:
    case NIGHTMARE_P3_PILLARS:
    case NIGHTMARE_SLEEPWALKERS:
    case NIGHTMARE_DOWN:
    case NIGHTMARE_DEATH:
      return 0;
    case PHOSANI_VARIANT_1:
    case PHOSANI_VARIANT_2:
    case PHOSANI_VARIANT_3:
    case PHOSANI_VARIANT_4:
    case PHOSANI_P4:
    case PHOSANI_PILLARS_VARIANT_1:
    case PHOSANI_PILLARS_VARIANT_2:
    case PHOSANI_PILLARS_VARIANT_3:
    case PHOSANI_PILLARS_VARIANT_4:
    case PHOSANI_DOWN:
    case PHOSANI_SLEEPWALKERS:
    case PHOSANI_DEATH:
      return 1;
    }

    return -1;
  }

  private String to_mmss(int ticks) {
    return client.getVarbitValue(11866) == 1 ? MiscUtil.to_mmss_precise(ticks)
        : MiscUtil.to_mmss(ticks);
  }

  private boolean is_in_noa() {
    WorldPoint wp = client.getLocalPlayer().getWorldLocation();
    int x = wp.getX() - client.getBaseX();
    int y = wp.getY() - client.getBaseY();
    int template = client.getInstanceTemplateChunks()[client.getPlane()][x / 8][y / 8];
    return (template & NM_ROOM_MASK) == NM_ROOM;
  }

}
