package com.objecthider;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;

@PluginDescriptor(name = "Ground Object Hider", description = "Hides Ground Objects. A selector is used to choose objects to hide.", tags = {"external", "objects", "memory", "usage", "ground", "decorations", "performance"})
public class ObjectHiderPlugin extends Plugin {
  @Inject
  private Client client;

  @Inject
  private ClientUI clientUI;

  @Inject
  private ClientThread clientThread;

  @Inject
  private ObjectHiderConfig config;

  @Inject
  private OverlayManager overlayManager;

  @Inject
  private ObjectHiderOverlay overlay;

  @Inject
  private KeyManager keyManager;

  @Inject
  private MouseManager mouseManager;

  @Inject
  private ChatMessageManager chatMessageManager;

  @Inject
  private RenderCallbackManager renderCallbackManager;

  @Inject
  private ObjectHiderRenderCallback callback;

  boolean selectGroundObjectMode = false;

  private Set<Integer> m_objectsToHide;

  /**
   * groundObjectsKeyListener is an instance of `HotkeyListener` designed to let
   * the user pick a tile in-game to have its Ground Object hidden.
   */
  private final HotkeyListener groundObjectsKeyListener = new HotkeyListener(() -> config.hideGroundObjectKey()) {
    @Override
    public void keyPressed(KeyEvent e) {
      if (config.hideGroundObjectKey().matches(e)) {
        if (!selectGroundObjectMode) {
          ChatMessageBuilder message = new ChatMessageBuilder().append("Ground Object Hider hotkey triggered.");
          chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(message.build()).build());
        }
        selectGroundObjectMode = true;
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
      if (config.hideGroundObjectKey().matches(e)) {
        if (selectGroundObjectMode) {
          ChatMessageBuilder message = new ChatMessageBuilder().append("Ground Object Hider hotkey released.");
          chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(message.build()).build());
        }
        selectGroundObjectMode = false;
      }
    }
  };

  /**
   * mouseListener is an instance of `MouseListener` designed solely to let the
   * user pick a tile in-game to have its Ground Object hidden.
   */
  private final MouseListener mouseListener = new MouseListener() {
    @Override
    public MouseEvent mouseClicked(MouseEvent mouseEvent) {
      if (SwingUtilities.isRightMouseButton(mouseEvent)) {
        if (!selectGroundObjectMode) {
          return mouseEvent;
        }
        final Tile tile = client.getTopLevelWorldView().getSelectedSceneTile();
        if (tile == null) {
          return mouseEvent;
        }
        // have a selected tile, in a suitable mode, so consume event:
        mouseEvent.consume();

        // get current list:
        final Set<Integer> curGroundHide = new HashSet<>(m_objectsToHide);
        final GroundObject obj = tile.getGroundObject();
        if (obj != null) {
          clientThread.invokeLater(() -> removeGroundObjectFromScene(obj.getId()));
          curGroundHide.add(obj.getId());
          config.setGroundObjectsToHide(Text.toCSV(curGroundHide.stream().map(String::valueOf).collect(Collectors.toList())));
        }
      }
      return mouseEvent;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent mouseEvent) {
      if (selectGroundObjectMode && SwingUtilities.isRightMouseButton(mouseEvent)) {
        mouseEvent.consume();
      }
      return mouseEvent;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent mouseEvent) {
      return mouseEvent;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent mouseEvent) {
      return mouseEvent;
    }

    @Override
    public MouseEvent mouseExited(MouseEvent mouseEvent) {
      return mouseEvent;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent mouseEvent) {
      return mouseEvent;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent mouseEvent) {
      return mouseEvent;
    }
  };

  @Provides
  ObjectHiderConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ObjectHiderConfig.class);
  }

  @Override
  protected void startUp() {
    m_objectsToHide = this.getGroundObjects();
    keyManager.registerKeyListener(groundObjectsKeyListener);
    mouseManager.registerMouseListener(mouseListener);
    overlayManager.add(overlay);
    renderCallbackManager.register(callback);

    callback.setHideAll(config.getHideAll());
    callback.setHideMapIcons(config.getHideMinimapIcons());
    callback.setTilesToHide(this.getGroundObjects());
    clientThread.invokeLater(this::tryReloadScene);
  }

  @Override
  protected void shutDown() {
    m_objectsToHide.clear();
    keyManager.unregisterKeyListener(groundObjectsKeyListener);
    mouseManager.unregisterMouseListener(mouseListener);
    overlayManager.remove(overlay);
    renderCallbackManager.unregister(callback);

    callback.reset();
    clientThread.invokeLater(this::tryReloadScene);
  }

  /**
   * Given a tile and a ground object, decides whether to hide it.
   * If hideAll is enabled, prevent minimap icons, interactable objects,
   * and objects with imposters, from being hidden.
   * Certain disallowed objects are also prevented.
   * If a ground object is not to be hidden, early return;
   *
   * @precondition tile and groundObject should not be null, called on client thread
   */
  private void tryHideGroundObjectOnTile(Tile tile, GroundObject groundObject) {
    assert tile != null && groundObject != null;
    assert client.isClientThread();

    int groundObjectId = groundObject.getId();

    // Disallow hiding in Sotetseg
    WorldView wv = client.getTopLevelWorldView();
    if (Arrays.stream(wv.getMapRegions()).anyMatch(n -> (n == 13123 || n == 13379)))
      return;

    ObjectComposition oc = client.getObjectDefinition(groundObjectId);
    if (oc == null) return;

    // Handle map icons first
    if (oc.getMapIconId() != -1) {
      boolean hideMapIcons = config.getHideMinimapIcons() || m_objectsToHide.contains(groundObjectId);
      if (hideMapIcons) {
        tile.setGroundObject(null);
      }
      return;
    }

    if (config.getHideAll()) {
      // For hide all, don't hide if there are imposters or actions
      int[] imposters = oc.getImpostorIds();
      String[] actions = oc.getActions();
      if ((imposters == null || imposters.length == 0) && (actions == null || Arrays.stream(actions).allMatch(Objects::isNull))) {
        tile.setGroundObject(null);
      }
    } else if (m_objectsToHide.contains(groundObject.getId())) {
      tile.setGroundObject(null);
    }
  }

  private void removeGroundObjectFromScene(int groundObjectId) {
    WorldView wv = client.getTopLevelWorldView();
    Scene scene = wv.getScene();
    final Tile[][][] tiles = scene.getTiles();
    if (tiles == null) return;

    for (int plane = 0; plane < tiles.length; plane++) {
      for (int x = 0; x < tiles[plane].length; x++) {
        for (int y = 0; y < tiles[plane][x].length; y++) {
          final Tile currentTile = tiles[plane][x][y];
          if (currentTile == null) continue;

          GroundObject groundObj = currentTile.getGroundObject();
          if (groundObj != null && groundObj.getId() == groundObjectId)
            tryHideGroundObjectOnTile(currentTile, groundObj);
        }
      }
    }
    if (client.isGpu()) tryReloadScene();
  }

  /**
   * getGroundObjects retrieves the list of Ground Objects to hide from the
   * config, and transforms into a `Set<Integer>` for consumption.
   * <p>
   * If something goes wrong, an empty set will be returned.
   *
   * @return configured list of Ground Objects to hide
   */
  Set<Integer> getGroundObjects() {
    try {
      return new HashSet<>(intsFromCSVString(config.getGroundObjectsToHide()));
    } catch (NumberFormatException ex) {
      return Collections.emptySet();
    }
  }

  /**
   * intsFromCSVStrong takes a String containing a list of Integers and returns
   * those Integers in a `List<Integer>` format.
   *
   * @param val - the string containing integers to parse
   * @return a list of integers
   * @throws NumberFormatException - if the string contains non-integers or is
   *                               badly formatted.
   */
  private static List<Integer> intsFromCSVString(String val) throws NumberFormatException {
    // parse a string of CSV integers:
    if (val.isEmpty()) {
      return Collections.emptyList();
    }
    return Text.fromCSV(val).stream().map(Integer::parseInt).collect(Collectors.toList());
  }

  /**
   * onGroundObjectSpawned listens for newly-spawned Ground Objects in case they
   * should be hidden.
   *
   * @param event - the spawn event
   */
  @Subscribe
  public void onGroundObjectSpawned(GroundObjectSpawned event) {
    final GroundObject groundObject = event.getGroundObject();
    final Tile currentTile = event.getTile();

    if (groundObject == null || currentTile == null) {
      return;
    }

    tryHideGroundObjectOnTile(currentTile, groundObject);
  }

  /**
   * @precondition on client thread
   */
  private void tryReloadScene() {
    assert client.isClientThread();
    if (client.getGameState() == GameState.LOGGED_IN)
      client.setGameState(GameState.LOADING);
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged) {
    if (!configChanged.getGroup().equals("objecthider")) {
      return;
    }

    // If objects to hide is the key, check it is an addition.
    if (configChanged.getKey().equals("toHide")) {
      Set<Integer> cachedObjects = new HashSet<>(m_objectsToHide);
      m_objectsToHide = this.getGroundObjects();
      callback.setHideAll(config.getHideAll());
      callback.setHideMapIcons(config.getHideMinimapIcons());
      callback.setTilesToHide(this.getGroundObjects());

      // If objects to hide is the key, check if we gained an object. If so, the keyListener has handled it already.
      if (selectGroundObjectMode && (!cachedObjects.equals(m_objectsToHide)) && m_objectsToHide.containsAll(cachedObjects)) {
        return;
      }
    }

    clientThread.invokeLater(this::tryReloadScene);
  }
}
