package de0.loadinglines;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class LoadingLinesMinimapOverlay extends Overlay {

  private static final int CHUNK_SIZE = Constants.CHUNK_SIZE;
  private static final int SCENE_SIZE = Constants.SCENE_SIZE;
  private static final int TILE_SIZE = 4;

  private Client client;
  private LoadingLinesConfig config;

  @Inject
  LoadingLinesMinimapOverlay(Client client, LoadingLinesConfig config) {
    this.client = client;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_WIDGETS);
  }

  @Override
  public Dimension render(Graphics2D g) {
    if (!config.minimap())
      return null;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setStroke(new BasicStroke(1));
    g.setColor(config.lineColor());

    Area minimapClipArea = getMinimapClipArea();
    if (minimapClipArea == null)
      return null;

    g.setClip(minimapClipArea);

    final int z = client.getPlane();
    final int baseX = client.getBaseX();
    final int baseY = client.getBaseY();

    WorldPoint southWest = new WorldPoint(baseX + CHUNK_SIZE * 2, baseY + CHUNK_SIZE * 2, z);
    WorldPoint northWest = new WorldPoint(baseX + CHUNK_SIZE * 2, baseY + SCENE_SIZE - CHUNK_SIZE * 2, z);
    WorldPoint northEast = new WorldPoint(baseX + SCENE_SIZE - CHUNK_SIZE * 2, baseY + SCENE_SIZE - CHUNK_SIZE * 2, z);
    WorldPoint southEast = new WorldPoint(baseX + SCENE_SIZE - CHUNK_SIZE * 2, baseY + CHUNK_SIZE * 2, z);

    Point sw = worldToMinimap(southWest);
    Point nw = worldToMinimap(northWest);
    Point ne = worldToMinimap(northEast);
    Point se = worldToMinimap(southEast);

    if (sw != null && nw != null) {
      g.drawLine(sw.getX(), sw.getY(), nw.getX(), nw.getY());
    }

    if (nw != null && ne != null) {
      g.drawLine(nw.getX(), nw.getY(), ne.getX(), ne.getY());
    }

    if (ne != null && se != null) {
      g.drawLine(ne.getX(), ne.getY(), se.getX(), se.getY());
    }

    if (se != null && sw != null) {
      g.drawLine(se.getX(), se.getY(), sw.getX(), sw.getY());
    }

    return null;
  }

  private Point worldToMinimap(final WorldPoint worldPoint)
  {
    if (client.getLocalPlayer() == null)
      return null;

    final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
    final LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
    final LocalPoint playerLocalPoint = LocalPoint.fromWorld(client, playerLocation);

    if (playerLocalPoint == null)
      return null;

    final int offsetX = playerLocalPoint.getX() - localLocation.getX();
    final int offsetY = playerLocalPoint.getY() - localLocation.getY();

    final int x = (worldPoint.getX() - playerLocation.getX()) * TILE_SIZE + offsetX / 32 - TILE_SIZE / 2;
    final int y = (worldPoint.getY() - playerLocation.getY()) * TILE_SIZE + offsetY / 32 - TILE_SIZE / 2 + 1;

    final int angle = client.getMapAngle() & 0x7FF;

    final int sin = (int) (65536.0D * Math.sin((double) angle * Perspective.UNIT));
    final int cos = (int) (65536.0D * Math.cos((double) angle * Perspective.UNIT));

    final Widget minimapDrawWidget = getMinimapDrawWidget();
    if (minimapDrawWidget == null || minimapDrawWidget.isHidden())
      return null;

    final int xx = y * sin + cos * x >> 16;
    final int yy = sin * x - y * cos >> 16;

    final Point loc = minimapDrawWidget.getCanvasLocation();
    final int minimapX = loc.getX() + xx + minimapDrawWidget.getWidth() / 2;
    final int minimapY = loc.getY() + yy + minimapDrawWidget.getHeight() / 2;

    return new Point(minimapX, minimapY);
  }

  private Widget getMinimapDrawWidget()
  {
    Widget minimapDrawArea;
    if (client.isResized()) {
      if (client.getVar(Varbits.SIDE_PANELS) == 1)
        minimapDrawArea = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
      else
        minimapDrawArea = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
    } else
      minimapDrawArea = client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
    return minimapDrawArea;
  }

  private Area getMinimapClipArea()
  {
    Widget minimapDrawArea = getMinimapDrawWidget();

    if (minimapDrawArea == null || minimapDrawArea.isHidden())
      return null;

    Rectangle bounds = minimapDrawArea.getBounds();
    Ellipse2D ellipse = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

    return new Area(ellipse);
  }
}
