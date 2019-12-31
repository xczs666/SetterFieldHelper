package com.jenkin.intellij.plugin.utils;

import org.jetbrains.annotations.NotNull;

public class NameUtil {

  @NotNull
  public static String firstUpperName(String fullName) {
    final int u = fullName.indexOf("<");
    return extractUpperName(u == -1 ? fullName : fullName.substring(0, u));
  }

  private static String extractUpperName(String fullName) {
    fullName = fullName.substring(fullName.lastIndexOf(".") + 1);
    return fullName.substring(0, 1).toUpperCase() + fullName.substring(1);
  }

  @NotNull
  public static String variableName(String fullName) {
    int u = fullName.indexOf("<");
    return extractLowerName(u == -1 ? fullName : fullName.substring(0, u));
  }

  private static String extractLowerName(String fullName) {
    fullName = fullName.substring(fullName.lastIndexOf(".") + 1);
    return fullName.substring(0, 1).toLowerCase() + fullName.substring(1);
  }
}
