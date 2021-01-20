package de0;

import de0.coxthieving.CoxThievingPlugin;
import de0.coxtimers.CoxTimersPlugin;
import de0.coxvanguards.CoxVanguardsPlugin;
import de0.loadinglines.LoadingLinesPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginHubPluginsTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(CoxTimersPlugin.class, CoxThievingPlugin.class, LoadingLinesPlugin.class, CoxVanguardsPlugin.class);
    RuneLite.main(args);
  }

}
