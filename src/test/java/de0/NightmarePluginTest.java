package de0;

import de0.nmtimers.NightmarePlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NightmarePluginTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(NightmarePlugin.class);
    RuneLite.main(args);
  }

}
