package de0;

import de0.miasmatiles.MiasmaTilesPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MiasmaTilesPluginTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(MiasmaTilesPlugin.class);
    RuneLite.main(args);
  }

}
