package de0.miasmatiles;

import java.awt.Color;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("miasmatiles")
public interface MiasmaTilesConfig extends Config {
  
  @ConfigItem(name = "Show burn tiles", description = "", keyName = "burnTiles")
  default boolean showBurnTiles() {
    return true;
  }
  
  @Alpha
  @ConfigItem(name = "Burn tile color", description = "", keyName = "burnTileColor")
  default Color burnTileColor() {
    return new Color(0x96CB2929, true);
  }

  @ConfigItem(name = "Show Olm tiles", description = "", keyName = "olmTiles")
  default boolean showOlmTiles() {
    return true;
  }
  
  @Alpha
  @ConfigItem(name = "Olm tile color", description = "", keyName = "olmTileColor")
  default Color olmTileColor() {
    return new Color(0xffB8C7CB, true);
  }

  @ConfigItem(name = "Thin outline", description = "Draw 1px non-AA outline instead of RuneLite default", keyName = "thinOutline")
  default boolean thinOutline() {
    return false;
  }

}
