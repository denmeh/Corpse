package com.github.denmeh.corpse.util;

import org.jetbrains.annotations.*;

import java.util.*;

public class ProfileUtils {

  /**
   * Creates a random name which is exactly 10 chars long and only contains hexadecimal chars.
   *
   * @return a randomly created minecraft-compatible name.
   */
  @NotNull
  public static String randomName() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
  }

}
