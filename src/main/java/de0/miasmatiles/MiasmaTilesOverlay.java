package de0.miasmatiles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MiasmaTilesOverlay extends Overlay {

  private Client client;
  private MiasmaTilesConfig config;

  @Inject
  MiasmaTilesOverlay(Client client, MiasmaTilesConfig config) {
    this.client = client;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
  }

  @Override
  public Dimension render(Graphics2D g) {
    WorldPoint wp = client.getLocalPlayer().getWorldLocation();
    int region = WorldPoint.fromLocalInstance(client, LocalPoint.fromWorld(client, wp)).getRegionID();
    
    if (region != MiasmaTilesData.REGION_ID)
      return null;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        config.thinOutline() ? RenderingHints.VALUE_ANTIALIAS_OFF
            : RenderingHints.VALUE_ANTIALIAS_ON);
    g.setStroke(new BasicStroke(config.thinOutline() ? 1 : 2));

    for (int[] tile_data : MiasmaTilesData.TILES) {
      int x = tile_data[0] + 16;
      int y = tile_data[1] + 32;
      int color = tile_data[2];
      
      if (color == MiasmaTilesData.BURN && config.showBurnTiles()) {
        draw_tile(g, x, y, config.burnTileColor());
      } else if (color == MiasmaTilesData.OLM && config.showOlmTiles()) {
        draw_tile(g, x, y, config.olmTileColor());
      }
    }

    return null;
  }

  private void draw_tile(Graphics2D g, int x, int y, Color c) {
    g.setColor(c);
    // West
    line(g, x, y, x, y + 1);
    // East
    line(g, x + 1, y, x + 1, y + 1);
    // South
    line(g, x, y, x + 1, y);
    // North
    line(g, x, y + 1, x + 1, y + 1);
  }

  private void line(Graphics g, int x0, int y0, int x1, int y1) {
    if (x0 < 0 || y0 < 0 || x1 < 0 || y1 < 0)
      return;

    if (x0 >= 104 || y0 >= 104 || x1 >= 104 || y1 >= 104)
      return;

    int p = client.getPlane();
    int[][][] h = client.getTileHeights();

    Point p0 = Perspective.localToCanvas(client, x0 << 7, y0 << 7,
        h[p][x0][y0]);
    Point p1 = Perspective.localToCanvas(client, x1 << 7, y1 << 7,
        h[p][x1][y1]);

    if (p0 != null && p1 != null)
      g.drawLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
  }

}
