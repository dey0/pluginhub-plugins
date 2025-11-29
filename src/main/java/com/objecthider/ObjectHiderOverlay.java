package com.objecthider;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * ObjectHiderOverlay adds a magenta colored border around the currently
 * highlighted tile when in selection mode.
 */
public class ObjectHiderOverlay extends Overlay {
  private final Client client;
  private final ObjectHiderPlugin plugin;

  @Inject
  private ObjectHiderOverlay(Client client, ObjectHiderPlugin plugin, ObjectHiderConfig config) {
    this.client = client;
    this.plugin = plugin;
    // position, layer + priority copied from `plugins.tileindicator`
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
    setPriority(Overlay.PRIORITY_MED);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    WorldView wv = client.getTopLevelWorldView();
    if (wv == null) return null;

    if (plugin.selectGroundObjectMode && wv.getSelectedSceneTile() != null) {
      // create a polygon
      final Polygon poly = Perspective.getCanvasTilePoly(client,
        wv.getSelectedSceneTile().getLocalLocation());
      if (poly != null) {
        // and render it
        OverlayUtil.renderPolygon(graphics, poly, Color.MAGENTA);
      }
    }
    return null;
  }
}
