package de0.coxthieving;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class ScavHighlight extends Overlay {

  private Client client;

  @Inject
  public ScavHighlight(CoxThievingPlugin plugin, Client client) {
    super(plugin);
    this.client = client;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
  }

  @Override
  public Dimension render(Graphics2D g) {
    NPC scav = null;
    for (NPC npc : client.getNpcs()) {
      if (npc == null)
        continue;
      if (npc.getId() == 7602 || npc.getId() == 7603)
        scav = npc;
    }
    if (scav == null)
      return null;

    g.setFont(FontManager.getRunescapeBoldFont());
    String str;
    if (client.getVarbitValue(5424) == 1) {
      str = Integer.toString((scav.getHealthRatio() + 2) * 3 / 10);
    } else {
      str = scav.getHealthRatio() + "%";
    }
    Point point = scav.getCanvasTextLocation(g, str, scav.getLogicalHeight());
    if (point == null)
      return null;
    point = new Point(point.getX(), point.getY() + 20);
    OverlayUtil.renderTextLocation(g, point, str, Color.GREEN);
    return null;
  }

}
