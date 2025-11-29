package com.objecthider;

import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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
    GroundObject groundObject = tile.getGroundObject();
    if (groundObject == null) {
      return RenderCallback.super.drawTile(scene, tile);
    }
    int groundObjectId = groundObject.getId();

    // Disallow hiding in Sotetseg
    WorldView wv = client.getTopLevelWorldView();
    if (Arrays.stream(wv.getMapRegions()).anyMatch(n -> (n == 13123 || n == 13379)))
      return RenderCallback.super.drawTile(scene, tile);

//    ObjectComposition oc = client.getObjectDefinition(groundObjectId);
//    if (oc == null) return RenderCallback.super.drawTile(scene, tile);
//
//    // Handle map icons first
//    if (oc.getMapIconId() != -1 && !hideMapIcons) {
//      tile.setGroundObject(null);
//      return RenderCallback.super.drawTile(scene, tile);
//    }

    if (hideAll) {
      // For hide all, don't hide if there are imposters or actions
//      int[] imposters = oc.getImpostorIds();
//      String[] actions = oc.getActions();
//      if ((imposters == null || imposters.length == 0) && (actions == null || Arrays.stream(actions).allMatch(Objects::isNull))) {
      tile.setGroundObject(null);
//      }
    } else if (idsToHide.contains(groundObject.getId())) {
      tile.setGroundObject(null);
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
    this.idsToHide = ids;
  }

  void reset() {
    this.hideAll = false;
    this.hideMapIcons = false;
    this.idsToHide.clear();

  }
}
