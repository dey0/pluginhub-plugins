package de0.coxvanguards;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("vanguards")
public interface CoxVanguardsConfig extends Config {

  @ConfigItem(position = 0, keyName = "showHps", name = "Show HPs", description = "Show Vanguard HPs")
  default boolean showHps() {
    return true;
  }

  @Range(min = 8, max = 32)
  @ConfigItem(position = 1, keyName = "fontSize", name = "HP text font size", description = "Size of the HP/reset text font")
  default int fontSize() {
    return 16;
  }

  @Range(min = -100, max = 100)
  @ConfigItem(position = 2, keyName = "heightOffset", name = "HP text height offset", description = "Shifts the HP/reset text up (positive) or down (negative) relative to its default position")
  default int heightOffset() {
    return 0;
  }

  @ConfigItem(position = 3, keyName = "showDmgToReset", name = "Show dmg to reset (solo)", description = "Show Vanguard dmg amount till reset")
  default boolean showDmgToReset() {
    return true;
  }

  @ConfigItem(position = 4, keyName = "highlight", name = "Highlight", description = "Highlights Vanguards of their respective color.")
  default boolean highlight() {
    return true;
  }

  @ConfigItem(position = 5, keyName = "showDatabox", name = "Show HPs in a databox", description = "Show Vanguard HPs databox")
  default boolean showDatabox() {
    return false;
  }

  @ConfigItem(position = 6, keyName = "meleeColor", name = "Melee Vanguard color", description = "Highlight color for melee Vanguard")
  default Color getMeleeColor() {
    return Color.RED;
  }

  @ConfigItem(position = 7, keyName = "rangeColor", name = "Range Vanguard color", description = "Highlight color for range Vanguard")
  default Color getRangeColor() {
    return Color.GREEN;
  }

  @ConfigItem(position = 8, keyName = "mageColor", name = "Mage Vanguard color", description = "Highlight color for mage Vanguard")
  default Color getMageColor() {
    return Color.CYAN;
  }

  @ConfigItem(position = 9, keyName = "wanderRange", name ="Show melee wander range", description = "Show how far you need to go before losing agro from melee")
  default boolean wanderRange() {
    return false;
  }

  @ConfigItem(position = 10, keyName = "wanderColor", name = "Melee Vanguard wander color", description = "Highlight color for melee Vanguard wander range")
  default Color getMeleeWanderColor() {
    return new Color(0xC35364);
  }

}
