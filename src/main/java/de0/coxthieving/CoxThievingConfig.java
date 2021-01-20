package de0.coxthieving;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("thieving")
public interface CoxThievingConfig extends Config {

  @ConfigItem(keyName = "highlightBats", name = "Highlight Potential Bats", description = "Highlight bat chests")
  default boolean highlightBatChests() {
    return true;
  }

  @ConfigItem(keyName = "gumdropFactor", name = "Gumdrop Highlight Factor", description = "Highlight bat chests in pretty colors")
  default int gumdropFactor() {
    return 0;
  }

  @ConfigItem(keyName = "grubRate", name = "Expected Grubs per Chest", description = "Average grubs per chest that contained some, for use in team count estimation")
  default int grubRate() {
    return 225;
  }

}