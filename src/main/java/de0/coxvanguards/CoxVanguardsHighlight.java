package de0.coxvanguards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.inject.Inject;

import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class CoxVanguardsHighlight extends Overlay {

  private CoxVanguardsPlugin plugin;
  private CoxVanguardsConfig config;

  @Inject
  public CoxVanguardsHighlight(CoxVanguardsPlugin plugin, CoxVanguardsConfig config) {
    super(plugin);
    this.plugin = plugin;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
  }

  @Override
  public Dimension render(Graphics2D g) {
    if (plugin.melee != null)
      renderVanguard(plugin.melee, plugin.melhp, plugin.melhp_fine, g,
          config.getMeleeColor());
    if (plugin.range != null)
      renderVanguard(plugin.range, plugin.rnghp, plugin.rnghp_fine, g,
          config.getRangeColor());
    if (plugin.mage != null)
      renderVanguard(plugin.mage, plugin.maghp, plugin.maghp_fine, g,
          config.getMageColor());
    return null;
  }

  private void renderVanguard(NPC van, int last_hp, int hp_fine, Graphics2D g,
      Color c) {
    if (van.getId() < 7525 || van.getId() > 7529)
      return;

    if (config.highlight()) {
      Shape s = van.getConvexHull();
      if (s != null) {
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
        g.fill(s);
      }
    }
    if (config.showDmgToReset() && plugin.isSolo()) {
      int rt = plugin.solo_base_hp * 4 / 10 - 1;
      int mel = plugin.melhp_fine;
      int rng = plugin.rnghp_fine;
      int mag = plugin.maghp_fine;
      int hi = max(max(mel, rng), mag);
      int lo = min(min(mel, rng), mag);
      int mid = mel ^ rng ^ mag ^ hi ^ lo;
      int thres = max(0, (hp_fine == hi ? mid : hi) - rt);
      int dist = hp_fine - thres;

      boolean goodToKill = hp_fine < rt;
      boolean goodToClear = mel < rt && rng < rt && mag < rt;

      if (!goodToClear) {
        String str = goodToKill ? "*" + dist : Integer.toString(dist);
        npctext(g, van, str, c);
      }
    } else if (config.showHps()) {
      int hp = van.getHealthRatio();
      if (hp < 0)
        hp = last_hp;
      int hpPercent = hp * 100 / 30;
      String str = Integer.toString(hpPercent);
      npctext(g, van, str, c);
    }
  }

  private void npctext(Graphics2D g, NPC npc, String str, Color c) {
    Point point = npc.getCanvasTextLocation(g, str, npc.getLogicalHeight());
    if (point == null)
      return;
    point = new Point(point.getX(), point.getY() + 20);
    g.setFont(FontManager.getRunescapeBoldFont());
    OverlayUtil.renderTextLocation(g, point, str, c);
  }

}
