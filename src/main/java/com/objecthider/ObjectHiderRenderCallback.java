package com.objecthider;

import net.runelite.api.*;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.callback.RenderCallback;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class ObjectHiderRenderCallback implements RenderCallback {
  @Inject
  private Client client;

  private Set<Integer> idsToHide = new HashSet<>();
  private boolean hideAll = false;
  private boolean hideMapIcons = false;

  @Inject
  ObjectHiderRenderCallback() {
  }

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

    // Handle non hide all cases first
    if (!hideAll && idsToHide.contains(groundObject.getId())) {
      tile.setGroundObject(null);
      return RenderCallback.super.drawTile(scene, tile);
    }

    ObjectComposition oc = client.getObjectDefinition(groundObject.getId());
    if (oc == null) {
      return RenderCallback.super.drawTile(scene, tile);
    }

    if (this.hideMapIcons && oc.getMapIconId() != -1) {
      tile.setGroundObject(null);
      return RenderCallback.super.drawTile(scene, tile);
    }

    // If object has an imposter, don't hide
    if (this.hideAll && oc.getMapIconId() == -1) {
      int[] imposters = oc.getImpostorIds();
      if (imposters == null) {
        tile.setGroundObject(null);
      } else if (imposters.length == 0) {
        tile.setGroundObject(null);
      }
    }

    return RenderCallback.super.drawTile(scene, tile);
  }

  void setHideAll(boolean hideAll) {
    this.hideAll = hideAll;
  }

  void setHideMapIcons(boolean hideMapIcons) {
    this.hideMapIcons = hideMapIcons;
  }

  void setTilesToHide(Set<Integer> ids) {
    idsToHide = ids;
  }

  void clearHiddenTiles() {
    idsToHide.clear();
  }
}
