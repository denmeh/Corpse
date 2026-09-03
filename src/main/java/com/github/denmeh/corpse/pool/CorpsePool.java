package com.github.denmeh.corpse.pool;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.denmeh.corpse.*;
import com.github.denmeh.corpse.corpse.*;
import com.github.denmeh.corpse.event.AsyncCorpseInteractEvent;
import com.google.common.collect.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.*;

public class CorpsePool implements Listener {

  private static final Random RANDOM = new Random();
  private static CorpsePool instance;
  private final CorpsePlugin plugin;

  //config options
  private double spawnDistance;
  private int timeRemove;
  private boolean onDeath;
  private boolean showTags;
  private boolean renderArmor;
  private boolean lootableCorpses;
  private boolean despawnWhenEmpty;
  private double lootClickRange;
  private Set<String> worlds;
  private EntityPose entityPose;
  
  private final Map<Integer, Corpse> corpseMap = new ConcurrentHashMap<>();

  private BukkitTask tickTask;

  @ApiStatus.Internal
  private CorpsePool() {
    this.plugin = CorpsePlugin.getInstance();
    this.loadConfig();
    Bukkit.getPluginManager().registerEvents(this, plugin);
    this.corpseTick();
  }

  public void reload() {
    this.loadConfig();
  }

  private void loadConfig() {
    FileConfiguration config = plugin.getConfigYml();
    this.spawnDistance = Math.pow(config.getInt("corpse-distance"), 2);
    this.timeRemove = config.getInt("corpse-time");
    this.onDeath = config.getBoolean("on-death");
    this.showTags = config.getBoolean("show-tags");
    this.renderArmor = config.getBoolean("render-armor");
    this.lootableCorpses = config.getBoolean("lootable-corpses");
    this.despawnWhenEmpty = config.getBoolean("despawn-when-empty", true);
    this.lootClickRange = config.getDouble("loot-click-range", 1.2);
    this.worlds = new HashSet<>(config.getStringList("worlds"));

    String entityPoseString = config.getString("entity-pose");
    if (entityPoseString == null) {
      this.entityPose = EntityPose.SLEEPING;
    } else {
      try {
        this.entityPose = EntityPose.valueOf(entityPoseString.toUpperCase());
      } catch (IllegalArgumentException e) {
        this.entityPose = EntityPose.SLEEPING;
      }
    }
  }


  @NotNull
  public static synchronized CorpsePool getInstance() {
    if (instance == null) {
      instance = new CorpsePool();
    }
    return instance;
  }

  public boolean existCorpseWithName(@NotNull String name) {
    for(Corpse c: corpseMap.values()) {
      if(c.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  private void corpseTick() {
    tickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
      for (Player player : ImmutableList.copyOf(Bukkit.getOnlinePlayers())) {
        for (Corpse corpse : this.corpseMap.values()) {
          Location holoLoc = corpse.getLocation();
          Location playerLoc = player.getLocation();
          boolean isShown = corpse.isShownFor(player);

          if (!holoLoc.getWorld().equals(playerLoc.getWorld())) {
            if (isShown) {
              corpse.hide(player);
            }
            continue;
          } else if (!holoLoc.getWorld()
              .isChunkLoaded(holoLoc.getBlockX() >> 4, holoLoc.getBlockZ() >> 4) && isShown) {
            corpse.hide(player);
            continue;
          }
          boolean inRange = holoLoc.distanceSquared(playerLoc) <= this.spawnDistance;

          if (!inRange && isShown) {
            corpse.hide(player);
          } else if (inRange && !isShown) {
            corpse.show(player);
          }
        }
      }
    }, 20, 2);
  }

  @NotNull
  public Optional<Corpse> getCorpse(int entityId) {
    return Optional.ofNullable(this.corpseMap.get(entityId));
  }

  @NotNull
  public Optional<Corpse> getCorpse(String name) {
    return this.getCorpses()
        .stream()
        .filter(corpse -> corpse.getName().equals(name))
        .findFirst();
  }

  public void remove(int entityId) {
    this.getCorpse(entityId).ifPresent(corpse -> {
      this.corpseMap.remove(entityId);
      if (corpse instanceof LootableCorpse) {
        ((LootableCorpse) corpse).dropRemaining();
      }
      corpse.getSeeingPlayers()
          .forEach(corpse::hide);
    });
  }

  public int getFreeEntityId() {
    int id;

    do {
      id = RANDOM.nextInt(Integer.MAX_VALUE);
    } while (this.corpseMap.containsKey(id));

    return id;
  }

  @NotNull
  public Collection<Corpse> getCorpses() {
    return Collections.unmodifiableCollection(this.corpseMap.values());
  }

  public void takeCareOf(@NotNull Corpse corpse) {
    // Prevent two corpses with same name and showTags is enabled
    if (this.showTags) {
      this.getCorpse(corpse.getName()).ifPresent(c -> this.remove(c.getId()));
    }
    this.corpseMap.put(corpse.getId(), corpse);
  }

  public int getTimeRemove() {
    return timeRemove;
  }

  public boolean isRenderArmor() {
    return renderArmor;
  }

  public boolean isShowTags() {
    return showTags;
  }

  public boolean isLootableCorpses() {
    return lootableCorpses;
  }

  public boolean isDespawnWhenEmpty() {
    return despawnWhenEmpty;
  }

  public EntityPose getEntityPose() {
    return entityPose;
  }

  public boolean isWorldEnabled(@Nullable World world) {
    if (world == null || this.worlds.isEmpty()) {
      return true;
    }
    return this.worlds.contains(world.getName());
  }

  @Nullable
  public BukkitTask getTickTask() {
    return tickTask;
  }

  @EventHandler
  public void handleQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    this.corpseMap.values().stream()
        .filter(corpse -> corpse.isShownFor(player))
        .forEach(corpse -> corpse.hide(player));
  }

  @EventHandler
  public void handleRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();

    this.corpseMap.values().stream()
        .filter(corpse -> corpse.isShownFor(player))
        .forEach(corpse -> corpse.hide(player));
  }

  @EventHandler
  public void handleDeath(PlayerDeathEvent event) {
    //Fix player death message disappear
//        event.setDeathMessage(null);

    if(onDeath) {
      Player player = event.getEntity();
      if (!isWorldEnabled(player.getWorld())) {
        return;
      }

      if(lootableCorpses) {
        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        if (despawnWhenEmpty && !hasItems(drops)) {
          event.getDrops().clear();
          return;
        }

        event.getDrops().clear();
        new LootableCorpse(player.getLocation(), player, drops);

      } else {
        Corpse.fromPlayer(player).spawn();
      }
    }
  }

  @EventHandler
  public void onCorpseInteract(AsyncCorpseInteractEvent event) {
    if(event.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT ||
       event.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
      Player player = event.getPlayer();
      Corpse corpse = event.getCorpse();


      if(corpse instanceof LootableCorpse) {
        LootableCorpse lootableCorpse = (LootableCorpse) corpse;

        Bukkit.getScheduler().runTask(CorpsePlugin.getInstance(), () -> lootableCorpse.open(player));
      }
    }
  }

  /**
   * Sleeping NPCs only have a tiny hitbox at the feet. Clicking the ground under
   * the body is a normal block interact, so treat nearby blocks as the corpse too.
   */
  @EventHandler
  public void onLootBlockClick(PlayerInteractEvent event) {
    if (!lootableCorpses || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
    // getHand() does not exist on 1.8
    if (version.isNewerThanOrEquals(ServerVersion.V_1_9)
        && event.getHand() != null && event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    // isInteractable() does not exist before 1.13
    if (version.isNewerThanOrEquals(ServerVersion.V_1_13) && block.getType().isInteractable()) {
      return;
    }

    Location at = block.getLocation().add(0.5, 1.0, 0.5);
    LootableCorpse nearest = null;
    double nearestDist = this.lootClickRange * this.lootClickRange;
    for (Corpse corpse : this.corpseMap.values()) {
      if (!(corpse instanceof LootableCorpse)) {
        continue;
      }
      if (!block.getWorld().equals(corpse.getLocation().getWorld())) {
        continue;
      }
      double dist = corpse.getLocation().distanceSquared(at);
      if (dist < nearestDist) {
        nearestDist = dist;
        nearest = (LootableCorpse) corpse;
      }
    }
    if (nearest != null) {
      event.setCancelled(true);
      nearest.open(event.getPlayer());
    }
  }

  @EventHandler
  public void onLootClick(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof LootableCorpse)) {
      return;
    }
    Bukkit.getScheduler().runTask(this.plugin, () -> removeIfLootEmpty(event.getInventory()));
  }

  @EventHandler
  public void onLootDrag(InventoryDragEvent event) {
    if (!(event.getInventory().getHolder() instanceof LootableCorpse)) {
      return;
    }
    Bukkit.getScheduler().runTask(this.plugin, () -> removeIfLootEmpty(event.getInventory()));
  }

  @EventHandler
  public void onLootClose(InventoryCloseEvent event) {
    removeIfLootEmpty(event.getInventory());
  }

  private void removeIfLootEmpty(@NotNull Inventory inventory) {
    if (!this.despawnWhenEmpty) {
      return;
    }
    InventoryHolder holder = inventory.getHolder();
    if (!(holder instanceof LootableCorpse)) {
      return;
    }
    LootableCorpse corpse = (LootableCorpse) holder;
    if (corpse.isEmpty()) {
      this.remove(corpse.getId());
    }
  }

  private static boolean hasItems(@NotNull List<ItemStack> items) {
    for (ItemStack item : items) {
      if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
        return true;
      }
    }
    return false;
  }

}
