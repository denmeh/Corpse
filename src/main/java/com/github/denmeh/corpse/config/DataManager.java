package com.github.denmeh.corpse.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.logging.Level;

public class DataManager {

  private Plugin plugin;
  private FileConfiguration dataConfig;
  private File configFile;
  private String nameFile;

  public DataManager(@NotNull Plugin plugin, @NotNull String nameFile) {
    this.dataConfig = null;
    this.configFile = null;
    this.plugin = plugin;
    this.nameFile = nameFile;
    this.saveDefaultConfig();
  }

  public void reloadConfig() {
    if (this.configFile == null) {
      this.configFile = new File(this.plugin.getDataFolder(), this.nameFile);
    }
    this.dataConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(this.configFile);
    final InputStream defaultStream = this.plugin.getResource(this.nameFile);
    if (defaultStream != null) {
      final YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
          (Reader) new InputStreamReader(defaultStream));
      this.dataConfig.setDefaults((Configuration) defaultConfig);
    }
  }

  public FileConfiguration getConfig() {
    if (this.dataConfig == null) {
      this.reloadConfig();
    }
    return this.dataConfig;
  }

  public void saveConfig() {
    if (this.dataConfig == null || this.configFile == null) {
      return;
    }
    try {
      this.getConfig().save(this.configFile);
    } catch (IOException e) {
      this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
    }
  }

  public void saveDefaultConfig() {
    if (this.configFile == null) {
      this.configFile = new File(this.plugin.getDataFolder(), this.nameFile);
    }
    if (!this.configFile.exists()) {
      this.plugin.saveResource(this.nameFile, false);
    }
  }
}