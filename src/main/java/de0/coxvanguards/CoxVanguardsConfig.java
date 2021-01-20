package de0.coxvanguards;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("vanguards")
public interface CoxVanguardsConfig extends Config {

  @ConfigItem(position = 0, keyName = "showHps", name = "Show HPs", description = "Show Vanguard HPs")
  default boolean showHps() {
    return true;
  }

  @ConfigItem(position = 1, keyName = "showDmgToReset", name = "Show dmg to reset (solo)", description = "Show Vanguard dmg amount till reset")
  default boolean showDmgToReset() {
    return true;
  }

  @ConfigItem(position = 2, keyName = "highlight", name = "Highlight", description = "Highlights Vanguards of their respective color.")
  default boolean highlight() {
    return true;
  }

  @ConfigItem(position = 3, keyName = "showDatabox", name = "Show HPs in a databox", description = "Show Vanguard HPs databox")
  default boolean showDatabox() {
    return false;
  }

  @ConfigItem(position = 4, keyName = "meleeColor", name = "Melee Vanguard color", description = "Highlight color for melee Vanguard")
  default Color getMeleeColor() {
    return Color.RED;
  }

  @ConfigItem(position = 5, keyName = "rangeColor", name = "Range Vanguard color", description = "Highlight color for range Vanguard")
  default Color getRangeColor() {
    return Color.GREEN;
  }

  @ConfigItem(position = 6, keyName = "mageColor", name = "Mage Vanguard color", description = "Highlight color for mage Vanguard")
  default Color getMageColor() {
    return Color.CYAN;
  }

}
