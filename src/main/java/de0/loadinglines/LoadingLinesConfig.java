package de0.loadinglines;

import java.awt.Color;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("loadinglines")
public interface LoadingLinesConfig extends Config {

  @Alpha
  @ConfigItem(name = "Line color", description = "", keyName = "lineColor")
  default Color lineColor() {
    return new Color(0, 255, 87, 111);
  }

  @ConfigItem(name = "Draw distance", description = "", keyName = "drawDistance")
  default int drawDistance() {
    return 24;
  }

}
