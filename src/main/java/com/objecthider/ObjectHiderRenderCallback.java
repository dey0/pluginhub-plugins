package com.objecthider;

import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.client.callback.RenderCallback;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class ObjectHiderRenderCallback implements RenderCallback {

  private Set<Integer> idsToHide = new HashSet<>();

  @Override
  public boolean drawTile(Scene scene, Tile tile) {
    if (tile.getGroundObject() == null) {
      return RenderCallback.super.drawTile(scene, tile);
    }

    // Disallow hiding in Sotetseg regions.
    for (int i : scene.getMapRegions()) {
      if (i == 13123 || i == 13379) {
        return RenderCallback.super.drawTile(scene, tile);
      }
    }

    GroundObject groundObject = tile.getGroundObject();
    if (groundObject == null) {
      return RenderCallback.super.drawTile(scene, tile);
    }

    if (idsToHide.contains(groundObject.getId())) {
      tile.setGroundObject(null);
      return RenderCallback.super.drawTile(scene, tile);
    }

    return RenderCallback.super.drawTile(scene, tile);
  }

  void setTilesToHide(Set<Integer> ids) {
    idsToHide = ids;
  }

  void clearHiddenTiles() {
    idsToHide.clear();
  }
}
