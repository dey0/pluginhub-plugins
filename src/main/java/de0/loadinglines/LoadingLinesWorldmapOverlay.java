package de0.loadinglines;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

public class LoadingLinesWorldmapOverlay extends Overlay {
  private static final int CHUNK_SIZE = Constants.CHUNK_SIZE;
  private static final int SCENE_SIZE = Constants.SCENE_SIZE;

  private Client client;
  private LoadingLinesConfig config;

  @Inject
  private WorldMapOverlay worldMapOverlay;

  @Inject
  private LoadingLinesWorldmapOverlay(Client client, LoadingLinesConfig config) {
    this.client = client;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setPriority(OverlayPriority.LOW);
    setLayer(OverlayLayer.MANUAL);
    drawAfterLayer(WidgetInfo.WORLD_MAP_VIEW);
  }

  @Override
  public Dimension render(Graphics2D g) {
    if (!config.worldmap()) return null;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setStroke(new BasicStroke(1));
    g.setColor(config.lineColor());

    final Area mapClipArea = getWorldMapClipArea();
    if (mapClipArea == null) return null;

    g.setClip(mapClipArea);

    final int z = client.getPlane();
    final int baseX = client.getBaseX();
    final int baseY = client.getBaseY();

    WorldPoint southWest = new WorldPoint(baseX + CHUNK_SIZE * 2, baseY + CHUNK_SIZE * 2, z);
    WorldPoint northWest = new WorldPoint(baseX + CHUNK_SIZE * 2,
        baseY + SCENE_SIZE - CHUNK_SIZE * 2, z);
    WorldPoint northEast = new WorldPoint(baseX + SCENE_SIZE - CHUNK_SIZE * 2,
        baseY + SCENE_SIZE - CHUNK_SIZE * 2, z);
    WorldPoint southEast = new WorldPoint(baseX + SCENE_SIZE - CHUNK_SIZE * 2,
        baseY + CHUNK_SIZE * 2, z);

    Point sw = worldMapOverlay.mapWorldPointToGraphicsPoint(southWest);
    Point nw = worldMapOverlay.mapWorldPointToGraphicsPoint(northWest);
    Point ne = worldMapOverlay.mapWorldPointToGraphicsPoint(northEast);
    Point se = worldMapOverlay.mapWorldPointToGraphicsPoint(southEast);

    if (sw != null && nw != null) g.drawLine(sw.getX(), sw.getY(), nw.getX(), nw.getY());

    if (nw != null && ne != null) g.drawLine(nw.getX(), nw.getY(), ne.getX(), ne.getY());

    if (ne != null && se != null) g.drawLine(ne.getX(), ne.getY(), se.getX(), se.getY());

    if (se != null && sw != null) g.drawLine(se.getX(), se.getY(), sw.getX(), sw.getY());

    return null;
  }

  private Area getWorldMapClipArea() {
    final Widget overview = client.getWidget(WidgetInfo.WORLD_MAP_OVERVIEW_MAP);
    final Widget surfaceSelector = client.getWidget(WidgetInfo.WORLD_MAP_SURFACE_SELECTOR);

    final Widget worldMapView = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
    if (worldMapView == null) return null;

    final Rectangle bounds = worldMapView.getBounds();
    if (bounds == null) return null;

    Area clipArea = new Area(bounds);

    if (overview != null && !overview.isHidden()) clipArea.subtract(new Area(overview.getBounds()));
    if (surfaceSelector != null && !surfaceSelector.isHidden())
      clipArea.subtract(new Area(surfaceSelector.getBounds()));

    return clipArea;
  }
}
