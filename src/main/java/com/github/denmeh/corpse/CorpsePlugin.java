package com.github.denmeh.corpse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.denmeh.corpse.command.*;
import com.github.denmeh.corpse.config.*;
import com.github.denmeh.corpse.corpse.*;
import com.github.denmeh.corpse.event.AsyncCorpseInteractEvent;
import com.github.denmeh.corpse.pool.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.*;
import org.bukkit.scheduler.*;
import org.jetbrains.annotations.*;

import java.util.ArrayList;


public class CorpsePlugin extends JavaPlugin {

  private static final int BSTATS_PLUGIN_ID = 33830;

  private static CorpsePlugin instance;

  private DataManager configYml;
  private DataManager messagesData;
  private Messages messages;
  private CorpsePool pool;

  @NotNull
  public static CorpsePlugin getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    CorpsePlugin.instance = this;

    CorpseCommand corpseCommand = new CorpseCommand();
    this.getCommand("corpse").setExecutor(corpseCommand);
    this.getCommand("corpse").setTabCompleter(corpseCommand);

    configYml = new DataManager(this, "config.yml");
    messagesData = new DataManager(this, "messages.yml");
    messages = new Messages(messagesData);

    pool = CorpsePool.getInstance();
    startMetrics();

    PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
      @Override
      public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
          return;
        }

        Player player = event.getPlayer();
        if (player == null || pool == null) {
          return;
        }

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        Corpse corpse = pool.getCorpse(packet.getEntityId()).orElse(null);
        if (corpse == null) {
          return;
        }

        Bukkit.getPluginManager().callEvent(new AsyncCorpseInteractEvent(player, corpse, packet.getAction()));
      }
    }, PacketListenerPriority.NORMAL);
  }

  @Override
  public void onDisable() {
    if (pool != null) {
      BukkitTask task = pool.getTickTask();
      if (task != null) {
        task.cancel();
      }
      for (Corpse c : new ArrayList<>(pool.getCorpses())) {
        pool.remove(c.getId());
      }
    }
  }

  private void startMetrics() {
    if (BSTATS_PLUGIN_ID <= 0) {
      return;
    }
    Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
    metrics.addCustomChart(new SimplePie("lootable_corpses",
        () -> Boolean.toString(pool.isLootableCorpses())));
    metrics.addCustomChart(new SimplePie("entity_pose",
        () -> pool.getEntityPose().name()));
  }

  public void reloadPlugin() {
    configYml.reloadConfig();
    messagesData.reloadConfig();
    messages = new Messages(messagesData);
    if (pool != null) {
      pool.reload();
    }
  }

  @NotNull
  public FileConfiguration getConfigYml() {
    return configYml.getConfig();
  }

  @NotNull
  public Messages getMessages() {
    return messages;
  }
}
