package de0.coxthieving;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

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

  private final int A = 0x50;
  private final Color[] palette = new Color[10];
  private final Color pcolor = new Color(0, 255, 0, A);

  private int last_gdfactor = -1;

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
    int gdfactor = config.gumdropFactor();
    if (gdfactor != last_gdfactor) {
      final float h0 = 1f / 6f;
      final float hf = gdfactor / 20.0f;
      final float s = 0.95f;
      final float b = 0.80f;
      int[] rgb = new int[10];
      rgb[0] = Color.HSBtoRGB(h0 + hf * -1.0f, s, b);
      rgb[1] = Color.HSBtoRGB(h0 + hf * -0.8f, s, b);
      rgb[2] = Color.HSBtoRGB(h0 + hf * -0.6f, s, b);
      rgb[3] = Color.HSBtoRGB(h0 + hf * -0.4f, s, b);
      rgb[4] = Color.HSBtoRGB(h0 + hf * -0.2f, s, b);
      rgb[5] = Color.HSBtoRGB(h0, s, b);
      rgb[6] = Color.HSBtoRGB(h0 + hf * 0.2f, s, b);
      rgb[7] = Color.HSBtoRGB(h0 + hf * 0.4f, s, b);
      rgb[8] = Color.HSBtoRGB(h0 + hf * 0.6f, s, b);
      rgb[9] = Color.HSBtoRGB(h0 + hf * 0.8f, s, b);
      for (int i = 0; i < 10; i++) {
        palette[i] = new Color(A << 24 | rgb[i] & 0xffffff, true);
      }
      last_gdfactor = gdfactor;
    }
    if (plugin.soln == -1) {
      for (byte n = 0; n < solns.length; n++) {
        if (plugin.not_solns.contains(n))
          continue;
        for (int i = 0; i < 4; i++) {
          Tile t = findChest(locs[solns[n][i] - 1], plugin.rot);
          if (t != null)
            highlightChest(t, palette[n], g);
        }
      }
    } else {
      byte[] b = solns[plugin.soln];
      for (int i = 0; i < b.length; i++) {
        Tile t = findChest(locs[b[i] - 1], plugin.rot);
        if (t != null) // tile outside of view
          highlightChest(t, pcolor, g);
      }
    }
    return null;
  }

  private void highlightChest(Tile t, Color c, Graphics2D g) {
    GameObject chest = t.getGameObjects()[0];
    if (chest == null)
      return;
    if (chest.getId() == CoxThievingPlugin.CCHEST
        || chest.getId() == CoxThievingPlugin.PCHEST) {
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
