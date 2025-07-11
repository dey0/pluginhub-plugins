package de0.coxvanguards;

import java.awt.*;
import java.util.Objects;

import javax.inject.Inject;

import lombok.NonNull;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class CoxVanguardsHighlight extends Overlay {

  @Inject
  private Client client;

  @Inject
  private CoxVanguardsPlugin plugin;

  @Inject
  private CoxVanguardsConfig config;

  @Inject
  protected void init() {
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
    if (plugin.meleeSpawn != null && config.wanderRange()) {
      renderBox(g, plugin.meleeSpawn);
    }
    return null;
  }

  private Point getTileCorner(Client client, WorldView vw, WorldPoint wp, int plane, int cornerIndex)
  {
    LocalPoint lp = LocalPoint.fromWorld(vw, wp);
    if (lp == null)
    {
      return null;
    }

    Polygon poly = Perspective.getCanvasTilePoly(client, lp, plane);
    if (poly == null || poly.npoints < 4)
    {
      return null;
    }

    // Indices: 0 = TL, 1 = TR, 2 = BR, 3 = BL
    return new Point(poly.xpoints[cornerIndex], poly.ypoints[cornerIndex]);
  }

  private void renderBox(Graphics2D g, @NonNull WorldPoint spawn) {
    WorldView vw = client.getWorldView(-1);
    int plane = vw.getPlane();
    int radius = 9;

    // Define the 4 corner tiles of the square
    WorldPoint topLeft = spawn.dx(-radius).dy(-radius);
    WorldPoint topRight = spawn.dx(radius).dy(-radius);
    WorldPoint bottomRight = spawn.dx(radius).dy(radius);
    WorldPoint bottomLeft = spawn.dx(-radius).dy(radius);

    // Get the relevant corners of each tile (see note below)
    Point tl = getTileCorner(client, vw, topLeft, plane, 0); // Top-left
    Point tr = getTileCorner(client, vw, topRight, plane, 1); // Top-right
    Point br = getTileCorner(client, vw, bottomRight, plane, 2); // Bottom-right
    Point bl = getTileCorner(client, vw, bottomLeft, plane, 3); // Bottom-left

    if (tl != null && tr != null && br != null && bl != null)
    {
      Polygon outline = new Polygon();
      outline.addPoint(tl.getX(), tl.getY());
      outline.addPoint(tr.getX(), tr.getY());
      outline.addPoint(br.getX(), br.getY());
      outline.addPoint(bl.getX(), bl.getY());

      g.setColor(config.getMeleeWanderColor());
      g.drawPolygon(outline);
      g.drawPolygon(Perspective.getCanvasTilePoly(client, LocalPoint.fromWorld(vw, spawn)));
    }
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
