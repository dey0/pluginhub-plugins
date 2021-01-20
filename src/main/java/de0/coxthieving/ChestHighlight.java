package de0.coxthieving;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class ChestHighlight extends Overlay {

  private Client client;
  private CoxThievingPlugin plugin;
  private CoxThievingConfig config;

  @Inject
  public ChestHighlight(Client client, CoxThievingPlugin plugin,
      CoxThievingConfig config) {
    super(plugin);
    this.client = client;
    this.plugin = plugin;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
  }

  @Override
  public Dimension render(Graphics2D g) {
    if (!config.highlightBatChests())
      return null;
    byte[][] solns = ChestData.CHEST_SOLNS[plugin.wind][plugin.rot];
    byte[][] locs = ChestData.CHEST_LOCS[plugin.wind];
    int[] rgb = new int[10];
    if (config.gumdropFactor() != 0) {
      float factor = config.gumdropFactor() / 20.0f;
      float cols[] = new float[3];
      Color.RGBtoHSB(255, 255, 0, cols);
      cols[2] = 0.9f;
      rgb[0] = Color.HSBtoRGB(cols[0] + factor * -1.0f, cols[1], cols[2]);
      rgb[1] = Color.HSBtoRGB(cols[0] + factor * -0.8f, cols[1], cols[2]);
      rgb[2] = Color.HSBtoRGB(cols[0] + factor * -0.6f, cols[1], cols[2]);
      rgb[3] = Color.HSBtoRGB(cols[0] + factor * -0.4f, cols[1], cols[2]);
      rgb[4] = Color.HSBtoRGB(cols[0] + factor * -0.2f, cols[1], cols[2]);
      rgb[5] = 0xffffff;
      rgb[6] = Color.HSBtoRGB(cols[0] + factor * 0.2f, cols[1], cols[2]);
      rgb[7] = Color.HSBtoRGB(cols[0] + factor * 0.4f, cols[1], cols[2]);
      rgb[8] = Color.HSBtoRGB(cols[0] + factor * 0.6f, cols[1], cols[2]);
      rgb[9] = Color.HSBtoRGB(cols[0] + factor * 0.8f, cols[1], cols[2]);
    } else {
      Arrays.fill(rgb, 0xffff00);
    }
    if (plugin.soln == -1) {
      for (byte n = 0; n < solns.length; n++) {
        if (plugin.not_solns.contains(n))
          continue;
        Color col = new Color(rgb[n] & 0xffffff | 0x50000000, true);
        for (int i = 0; i < 4; i++) {
          Tile t = findChest(locs[solns[n][i] - 1], plugin.rot);
          if (t != null)
            highlightChest(t, col, g);
        }
      }
    } else {
      byte[] b = solns[plugin.soln];
      for (int i = 0; i < b.length; i++) {
        Tile t = findChest(locs[b[i] - 1], plugin.rot);
        if (t != null) // tile outside of view
          highlightChest(t, new Color(0, 255, 0, 50), g);
      }
    }
    return null;
  }

  private void highlightChest(Tile t, Color c, Graphics2D g) {
    GameObject chest = t.getGameObjects()[0];
    if (chest == null)
      return;
    if (chest.getId() == CoxThievingPlugin.CCHEST || chest.getId() == CoxThievingPlugin.PCHEST) {
      g.setColor(c);
      if (chest.getCanvasLocation() != null)
        g.fill(chest.getConvexHull());
    }
  }

  private Tile findChest(byte[] coords, int rot) {
    // convert room coords to scene coords
    int rx = coords[0];
    int ry = coords[1];
    int chestX = plugin.room_base_x;
    int chestY = plugin.room_base_y;
    if (rot == 0) {
      chestX += rx;
      chestY += ry;
    } else if (rot == 1) {
      chestX += ry;
      chestY -= rx;
    } else if (rot == 2) {
      chestX -= rx;
      chestY -= ry;
    } else {
      chestX -= ry;
      chestY += rx;
    }
    if (chestX < 0 || chestY < 0 || chestX >= 104 || chestY >= 104)
      return null;
    return client.getScene().getTiles()[client.getPlane()][chestX][chestY];
  }

}
