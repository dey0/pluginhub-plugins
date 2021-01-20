package de0;

import de0.loadinglines.LoadingLinesPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LoadingLinesPluginTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(LoadingLinesPlugin.class);
    RuneLite.main(args);
  }

}
