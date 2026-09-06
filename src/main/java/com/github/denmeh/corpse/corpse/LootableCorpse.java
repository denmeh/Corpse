package com.github.denmeh.corpse.corpse;

import com.github.denmeh.corpse.CorpsePlugin;
import com.github.denmeh.corpse.model.CorpseArmor;
import com.github.denmeh.corpse.pool.CorpsePool;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public class LootableCorpse extends Corpse implements InventoryHolder {

    private final Inventory inventory;

    public LootableCorpse(Location location, Player player, List<ItemStack> items) {
        super(location, SpigotReflectionUtil.getUserProfile(player), new CorpseArmor(player), player.getName(),
                CorpsePool.getInstance().getTimeRemove(player));
        this.inventory = Bukkit.createInventory(this, 54, CorpsePlugin.getInstance().getMessages().getLootInventoryTitle());
        int slot = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR || slot >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(slot++, item);
        }
    }


    public void open(Player player) {
        if (isEmpty()) {
            player.sendMessage(CorpsePlugin.getInstance().getMessages().getCorpseAlreadyLooted());
            return;
        }
        player.openInventory(inventory);
    }

    public boolean isEmpty() {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Drops leftover loot at the corpse. Safe to call from async threads.
     */
    public void dropRemaining() {
        Runnable task = () -> {
            for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
                viewer.closeInventory();
            }
            World world = location.getWorld();
            if (world != null) {
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        world.dropItemNaturally(location, item);
                    }
                }
            }
            inventory.clear();
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(CorpsePlugin.getInstance(), task);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }


}
