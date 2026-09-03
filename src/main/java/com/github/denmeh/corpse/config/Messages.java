package com.github.denmeh.corpse.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class Messages {

  private final FileConfiguration config;

  // Parsed once at load
  private final String noPermission;
  private final String playersOnly;
  private final String corpseCreated;
  private final String corpseUsage;
  private final String spawnUsage;
  private final String corpsesDeleted;
  private final String radiusNotNumber;
  private final String removeUsage;
  private final String corpseAlreadyLooted;
  private final String lootInventoryTitle;
  private final String configReloaded;

  public Messages(@NotNull DataManager messagesData) {
    this.config = messagesData.getConfig();
    this.noPermission = color(config.getString("no-permission", "&cYou don't have permission to use this command."));
    this.playersOnly = color(config.getString("players-only", "&cOnly players can run this command."));
    this.corpseCreated = color(config.getString("corpse-created", "&aCorpse created"));
    this.corpseUsage = color(first(config, "&c/corpse <spawn|remove|reload>", "corpse-usage"));
    this.spawnUsage = color(first(config, "&c/corpse spawn [player] - Spawns a corpse of a player if the name is given else it just spawns a corpse of yourself.", "spawn-usage", "spawncorpse-usage"));
    this.corpsesDeleted = color(config.getString("corpses-deleted", "&a({amount}) Corpses deleted"));
    this.radiusNotNumber = color(config.getString("radius-not-number", "&cRadius must be a number"));
    this.removeUsage = color(first(config, "&c/corpse remove <radius> - Removes any corpse(s) in a radius of you.", "remove-usage", "removecorpse-usage"));
    this.corpseAlreadyLooted = color(config.getString("corpse-already-looted", "&cCorpse already looted"));
    this.lootInventoryTitle = color(config.getString("loot-inventory-title", "&6Loot"));
    this.configReloaded = color(config.getString("config-reloaded", "&aCorpse config reloaded."));
  }

  private static String first(@NotNull FileConfiguration config, @NotNull String fallback, @NotNull String... keys) {
    for (String key : keys) {
      String value = config.getString(key);
      if (value != null) {
        return value;
      }
    }
    return fallback;
  }

  private static String color(String s) {
    return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
  }

  @NotNull
  public String getNoPermission() {
    return noPermission;
  }

  @NotNull
  public String getPlayersOnly() {
    return playersOnly;
  }

  @NotNull
  public String getCorpseCreated() {
    return corpseCreated;
  }

  @NotNull
  public String getCorpseUsage() {
    return corpseUsage;
  }

  @NotNull
  public String getSpawnUsage() {
    return spawnUsage;
  }

  @NotNull
  public String getCorpsesDeleted(int amount) {
    return corpsesDeleted.replace("{amount}", String.valueOf(amount));
  }

  @NotNull
  public String getRadiusNotNumber() {
    return radiusNotNumber;
  }

  @NotNull
  public String getRemoveUsage() {
    return removeUsage;
  }

  @NotNull
  public String getCorpseAlreadyLooted() {
    return corpseAlreadyLooted;
  }

  @NotNull
  public String getLootInventoryTitle() {
    return lootInventoryTitle;
  }

  @NotNull
  public String getConfigReloaded() {
    return configReloaded;
  }
}
