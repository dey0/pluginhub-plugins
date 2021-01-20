package de0.util;

public class MiscUtil {

  public static String to_mmss(int ticks) {
    int m = ticks / 100;
    int s = (ticks - m * 100) * 6 / 10;
    return m + (s < 10 ? ":0" : ":") + s;
  }

}
