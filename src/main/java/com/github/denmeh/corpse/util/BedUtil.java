package com.github.denmeh.corpse.util;

import org.bukkit.Location;

public class BedUtil {

  /**
   * Get bed location.
   *
   * @param location corpse's location
   * @return fake bed location
   */
  public static Location getBedLocation(Location location) {
    Location loc = location.clone();
    loc.setY(1);
    return loc;
  }

  /**
   * Get corpse facing.
   *
   * @param yaw location yaw
   * @return bed block facing
   */
  public static int yawToFacing(float yaw) {
    int facing = 2;
    if (yaw >= -45 && yaw <= 45) {
      facing = 0;
    } else if (yaw >= 45 && yaw <= 135) {
      facing = 1;
    } else if (yaw <= -45 && yaw >= -135) {
      facing = 3;
    } else if (yaw <= -135 && yaw >= -225) {
      facing = 2;
    } else if (yaw <= -225 && yaw >= -315) {
      facing = 1;
    } else if (yaw >= 135 && yaw <= 225) {
      facing = 2;
    } else if (yaw >= 225 && yaw <= 315) {
      facing = 3;
    } else if (yaw >= 315) {
      facing = 0;
    } else if (yaw <= -315) {
      facing = 0;
    }
    return facing;
  }

}
