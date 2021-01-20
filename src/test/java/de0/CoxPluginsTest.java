package de0;

import de0.coxthieving.CoxThievingPlugin;
import de0.coxtimers.CoxTimersPlugin;
import de0.coxvanguards.CoxVanguardsPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CoxPluginsTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(CoxTimersPlugin.class,
        CoxVanguardsPlugin.class, CoxThievingPlugin.class);
    RuneLite.main(args);
  }

}
