package de0.nmtimers;

import java.awt.Color;

import de0.util.MiscUtil;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ImageUtil;

public class NightmareInfoBox extends InfoBox {

  private NightmarePlugin plugin;
  private Client client;

  public NightmareInfoBox(Client client, NightmarePlugin plugin) {
    super(ImageUtil.loadImageResource(NightmareInfoBox.class, "nightmare.png"), plugin);
    this.plugin = plugin;
    this.client = client;
  }

  @Override
  public String getText() {
    String str;
    if (plugin.phase_splits[0] != -1) str = MiscUtil.to_mmss(plugin.phase_splits[0]);
    else str = MiscUtil.to_mmss(client.getTickCount() - plugin.fight_timer);
    if (plugin.dirty) str += "*";
    return str;
  }

  @Override
  public Color getTextColor() {
    return plugin.phase_splits[0] == -1 ? Color.WHITE : Color.GREEN;
  }

  @Override
  public String getTooltip() {
    StringBuilder builder = new StringBuilder();
    builder.append("Elapsed nightmare time: ");
    if (plugin.phase_splits[0] != -1)
      builder.append(MiscUtil.to_mmss_precise_short(plugin.phase_splits[0]));
    else builder.append(MiscUtil.to_mmss_precise_short(client.getTickCount() - plugin.fight_timer));

    if (plugin.phase_splits[1] != -1) {
      builder.append("</br>First phase: ");
      builder.append(MiscUtil.to_mmss_precise_short(plugin.phase_splits[1]));
    }

    if (plugin.phase_splits[2] != -1) {
      builder.append("</br>Second phase: ");
      builder.append(MiscUtil.to_mmss_precise_short(plugin.phase_splits[2]));
    }

    if (plugin.phase_splits[3] != -1) {
      builder.append("</br>Third phase: ");
      builder.append(MiscUtil.to_mmss_precise_short(plugin.phase_splits[3]));
    }

    if (plugin.phase_splits[4] != -1) {
      builder.append("</br>Fourth phase: ");
      builder.append(MiscUtil.to_mmss_precise_short(plugin.phase_splits[4]));
    }

    if (plugin.phase_splits[5] != -1) {
      builder.append("</br>Final phase: ");
      builder.append(MiscUtil.to_mmss_precise_short(plugin.phase_splits[5]));
    }

    return builder.toString();
  }

}
