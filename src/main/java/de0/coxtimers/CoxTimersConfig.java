package de0.coxtimers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("coxtimers")
public interface CoxTimersConfig extends Config {

  @ConfigItem(position = 0, keyName = "preciseTimers", name = "Precise Timers", description = "Respect in-game precise timer setting")
  default PreciseTimersSetting preciseTimers() {
    return PreciseTimersSetting.RESPECT_INGAME_SETTING;
  }
  
  @ConfigItem(position = 1, keyName = "showIcePopTime", name = "Time Ice demon pop-out", description = "Partial room timer for Ice Demon")
  default boolean showIcePopTime() {
    return true;
  }
  
  @ConfigItem(position = 2, keyName = "showMuttadileTreeCutTime", name = "Time Muttadile tree cut", description = "Partial room timer for Muttadiles")
  default boolean showMuttadileTreeCutTime() {
    return true;
  }

}
