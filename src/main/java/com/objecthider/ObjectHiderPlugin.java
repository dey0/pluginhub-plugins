package com.objecthider;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
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

  public boolean selectGroundObjectMode = false;

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
        final Tile tile = client.getSelectedSceneTile();
        if (tile == null) {
          return mouseEvent;
        }
        // have a selected tile, in a suitable mode, so consume event:
        mouseEvent.consume();

        // get current list:
        final List<Integer> curGroundHide = new ArrayList<>(getGroundObjects());
        final GroundObject obj = tile.getGroundObject();
        if (obj != null) {
          if (!curGroundHide.contains(obj.getId())) {
            curGroundHide.add(obj.getId());
          }
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
    keyManager.registerKeyListener(groundObjectsKeyListener);
    mouseManager.registerMouseListener(mouseListener);
    overlayManager.add(overlay);
    renderCallbackManager.register(callback);

    callback.setHideAll(config.getHideAll());
    callback.setHideMapIcons(config.getHideMinimapIcons());
    callback.setTilesToHide(this.getGroundObjects());
    this.tryReloadScene();
  }

  @Override
  protected void shutDown() {
    // on shutDown make sure to remove the draw callbacks and clear lists:
    keyManager.unregisterKeyListener(groundObjectsKeyListener);
    mouseManager.unregisterMouseListener(mouseListener);
    overlayManager.remove(overlay);
    renderCallbackManager.unregister(callback);

    callback.setHideAll(false);
    callback.setHideMapIcons(false);
    callback.clearHiddenTiles();
    this.tryReloadScene();
  }

  /**
   * getGroundObjects retrieves the list of Ground Objects to hide from the
   * config, and transforms into a `List<Integer>` for consumption.
   * <p>
   * If something goes wrong, an empty list will be returned.
   *
   * @return configured list of Ground Objects to hide
   */
  Set<Integer> getGroundObjects() {
    try {
      return new HashSet<Integer>(intsFromCSVString(config.getGroundObjectsToHide()));
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
   * onGameTick listens for game ticks to schedule a regular garbage collection of
   * Game Objects that are no longer in the scene.
   *
   * @param event - the tick event
   */
  @Subscribe
  public void onGameTick(GameTick event) {
    if (selectGroundObjectMode && !clientUI.isFocused()) {
      ChatMessageBuilder message = new ChatMessageBuilder().append("Ground Object Hider hotkey released.");
      chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(message.build()).build());
      selectGroundObjectMode = false;
    }
  }

  /**
   * onConfigChanged listens for changes to the plugin configuration to ensure the
   * client is synchronised when the config changes.
   *
   * @param configChanged - the change event (not used)
   */
  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged) {
    if (!configChanged.getGroup().equals("objecthider")) {
      return;
    }
    callback.setHideAll(config.getHideAll());
    callback.setHideMapIcons(config.getHideMinimapIcons());
    callback.setTilesToHide(this.getGroundObjects());
    this.tryReloadScene();
  }

  private void tryReloadScene() {
    clientThread.invokeLater(() -> {
      if (client.getGameState() == GameState.LOGGED_IN) client.setGameState(GameState.LOADING);
    });
  }
}
