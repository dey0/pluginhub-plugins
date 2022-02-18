package de0.loadinglines;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import static net.runelite.api.CollisionDataFlag.*;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class LoadingLinesOverlay extends Overlay {

  private Client client;
  private LoadingLinesConfig config;

  @Inject
  LoadingLinesOverlay(Client client, LoadingLinesConfig config) {
    this.client = client;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
  }

  private static final int MASK_W = BLOCK_MOVEMENT_FULL | BLOCK_MOVEMENT_WEST;
  private static final int MASK_E = BLOCK_MOVEMENT_FULL | BLOCK_MOVEMENT_EAST;
  private static final int MASK_S = BLOCK_MOVEMENT_FULL | BLOCK_MOVEMENT_SOUTH;
  private static final int MASK_N = BLOCK_MOVEMENT_FULL | BLOCK_MOVEMENT_NORTH;

  @Override
  public Dimension render(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setStroke(new BasicStroke(1));
    g.setColor(config.lineColor());

    CollisionData cmap = client.getCollisionMaps()[client.getPlane()];

    WorldPoint wp = client.getLocalPlayer().getWorldLocation();
    int curr_x = wp.getX() - client.getBaseX();
    int curr_y = wp.getY() - client.getBaseY();
    int[][] t = null;
    if (client.isInInstancedRegion()) t = client.getInstanceTemplateChunks()[client.getPlane()];

    final int draw_dist = config.drawDistance();

    int min_x = Math.max(curr_x - draw_dist, 16);
    int max_x = Math.min(curr_x + draw_dist, 88);
    int min_y = Math.max(curr_y - draw_dist, 16);
    int max_y = Math.min(curr_y + draw_dist, 88);

    for (int y = min_y; y < max_y; y++) {
      int x = 16;
      if (x >= min_x) {
        if (t != null && (t[x / 8][y / 8] == -1 || t[(x - 1) / 8][y / 8] == -1)) continue;
        int fl1 = cmap.getFlags()[x][y];
        int fl2 = cmap.getFlags()[x - 1][y];
        if ((fl1 & MASK_W) == 0 && (fl2 & MASK_E) == 0) line(g, 16, y, 16, y + 1);
      }

      x = 88;
      if (x <= max_x) {
        if (t != null && (t[(x - 1) / 8][y / 8] == -1 || t[x / 8][y / 8] == -1)) continue;
        int fl1 = cmap.getFlags()[x - 1][y];
        int fl2 = cmap.getFlags()[x][y];
        if ((fl1 & MASK_E) == 0 && (fl2 & MASK_W) == 0) line(g, 88, y, 88, y + 1);
      }
    }
    for (int x = min_x; x < max_x; x++) {
      int y = 16;
      if (y >= min_y) {
        if (t != null && (t[x / 8][y / 8] == -1 || t[x / 8][(y - 1) / 8] == -1)) continue;
        int fl1 = cmap.getFlags()[x][y];
        int fl2 = cmap.getFlags()[x][y - 1];
        if ((fl1 & MASK_S) == 0 && (fl2 & MASK_N) == 0) line(g, x, y, x + 1, y);
      }

      y = 88;
      if (y <= max_y) {
        if (t != null && (t[x / 8][(y - 1) / 8] == -1 || t[x / 8][y / 8] == -1)) continue;
        int fl1 = cmap.getFlags()[x][y - 1];
        int fl2 = cmap.getFlags()[x][y];
        if ((fl1 & MASK_N) == 0 && (fl2 & MASK_S) == 0) line(g, x, 88, x + 1, 88);
      }
    }

    return null;
  }

  private void line(Graphics g, int x0, int y0, int x1, int y1) {
    int p = client.getPlane();
    int[][][] h = client.getTileHeights();

    Point p0 = Perspective.localToCanvas(client, x0 << 7, y0 << 7, h[p][x0][y0]);
    Point p1 = Perspective.localToCanvas(client, x1 << 7, y1 << 7, h[p][x1][y1]);

    if (p0 != null && p1 != null) g.drawLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
  }

}
