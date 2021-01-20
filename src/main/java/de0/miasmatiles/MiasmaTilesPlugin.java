package de0.miasmatiles;

import javax.inject.Inject;

import com.google.inject.Provides;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name = "Miasma Tiles", description = "Displays burn tiles for use in Olm masses")
public class MiasmaTilesPlugin extends Plugin {

  @Inject
  private OverlayManager overlayManager;

  @Inject
  private MiasmaTilesOverlay overlay;

  @Provides
  MiasmaTilesConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(MiasmaTilesConfig.class);
  }

  @Override
  protected void startUp() throws Exception {
    overlayManager.add(overlay);
  }

  @Override
  protected void shutDown() throws Exception {
    overlayManager.remove(overlay);
  }

}
