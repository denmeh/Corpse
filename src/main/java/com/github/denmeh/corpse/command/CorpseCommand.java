package com.github.denmeh.corpse.command;

import com.github.denmeh.corpse.CorpsePlugin;
import com.github.denmeh.corpse.corpse.Corpse;
import com.github.denmeh.corpse.pool.CorpsePool;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CorpseCommand implements CommandExecutor, TabCompleter {

  private static final List<String> RADIUS_SUGGESTIONS = Arrays.asList("5", "10", "25", "50");

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
      sender.sendMessage(CorpsePlugin.getInstance().getMessages().getCorpseUsage());
      return true;
    }

    String sub = args[0].toLowerCase();
    if (sub.equals("reload")) {
      return reload(sender);
    }

    if (!(sender instanceof Player)) {
      sender.sendMessage(CorpsePlugin.getInstance().getMessages().getPlayersOnly());
      return true;
    }

    Player player = (Player) sender;
    if (sub.equals("spawn")) {
      return spawn(player, args);
    }
    if (sub.equals("remove")) {
      return remove(player, args);
    }

    sender.sendMessage(CorpsePlugin.getInstance().getMessages().getCorpseUsage());
    return true;
  }

  private boolean reload(@NotNull CommandSender sender) {
    if (!sender.hasPermission("corpses.reload")) {
      sender.sendMessage(CorpsePlugin.getInstance().getMessages().getNoPermission());
      return true;
    }
    CorpsePlugin.getInstance().reloadPlugin();
    sender.sendMessage(CorpsePlugin.getInstance().getMessages().getConfigReloaded());
    return true;
  }

  private boolean spawn(@NotNull Player player, @NotNull String[] args) {
    if (!player.hasPermission("corpses.spawn")) {
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getNoPermission());
      return true;
    }
    if (args.length == 1) {
      Corpse.fromPlayer(player).spawn();
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getCorpseCreated());
      return true;
    }
    if (args.length == 2) {
      OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
      if (target.isOnline()) {
        Corpse.fromPlayer((Player) target).location(player.getLocation()).spawn();
      } else {
        Corpse.fromLocation(player.getLocation()).name(target.getName()).spawn();
      }
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getCorpseCreated());
      return true;
    }
    player.sendMessage(CorpsePlugin.getInstance().getMessages().getSpawnUsage());
    return true;
  }

  private boolean remove(@NotNull Player player, @NotNull String[] args) {
    if (!player.hasPermission("corpses.remove")) {
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getNoPermission());
      return true;
    }
    if (args.length != 2) {
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getRemoveUsage());
      return true;
    }
    try {
      double radius = Math.pow(Double.parseDouble(args[1]), 2);
      CorpsePool pool = CorpsePool.getInstance();
      List<Integer> toRemove = new ArrayList<>();
      for (Corpse corpse : pool.getCorpses()) {
        if (corpse.getLocation().getWorld().equals(player.getWorld())
            && corpse.getLocation().distanceSquared(player.getLocation()) <= radius) {
          toRemove.add(corpse.getId());
        }
      }
      for (Integer id : toRemove) {
        pool.remove(id);
      }
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getCorpsesDeleted(toRemove.size()));
    } catch (NumberFormatException e) {
      player.sendMessage(CorpsePlugin.getInstance().getMessages().getRadiusNotNumber());
    }
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      List<String> subs = new ArrayList<>();
      if (sender.hasPermission("corpses.spawn")) {
        subs.add("spawn");
      }
      if (sender.hasPermission("corpses.remove")) {
        subs.add("remove");
      }
      if (sender.hasPermission("corpses.reload")) {
        subs.add("reload");
      }
      return StringUtil.copyPartialMatches(args[0], subs, new ArrayList<>());
    }

    if (args.length == 2 && args[0].equalsIgnoreCase("spawn") && sender.hasPermission("corpses.spawn")) {
      List<String> names = new ArrayList<>();
      Player viewer = sender instanceof Player ? (Player) sender : null;
      for (Player online : Bukkit.getOnlinePlayers()) {
        if (viewer != null && !viewer.canSee(online)) {
          continue;
        }
        names.add(online.getName());
      }
      List<String> matches = StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
      Collections.sort(matches);
      return matches;
    }

    if (args.length == 2 && args[0].equalsIgnoreCase("remove") && sender.hasPermission("corpses.remove")) {
      return StringUtil.copyPartialMatches(args[1], RADIUS_SUGGESTIONS, new ArrayList<>());
    }

    return Collections.emptyList();
  }
}
