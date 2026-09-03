package com.github.denmeh.corpse.model;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;

/**
 * Represents the armor of a corpse.
 */
public class CorpseArmor {

    public static final CorpseArmor EMPTY = new CorpseArmor();


    private ItemStack boots;
    private ItemStack leggings;
    private ItemStack chestplate;
    private ItemStack helmet;

    /**
     * Creates a new CorpseArmor.
     * @return A new CorpseArmor.
     */
    public CorpseArmor() {}

    /**
     * Creates a new CorpseArmor from a player.
     * @param player The player to create the CorpseArmor for.
     * @return A new CorpseArmor.
     */
    public CorpseArmor(@NotNull Player player) {
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        this.boots = armorContents[0];
        this.leggings = armorContents[1];
        this.chestplate = armorContents[2];
        this.helmet = armorContents[3];
    }

    /**
     * Sets the boots of the armor.
     * @param boots The boots item stack.
     * @return This armor.
     */
    public CorpseArmor boots(@NotNull ItemStack boots) {
        this.boots = boots;
        return this;
    }

    /**
     * Sets the leggings of the armor.
     * @param leggings The leggings item stack.
     * @return This armor.
     */
    public CorpseArmor leggings(@NotNull ItemStack leggings) {
        this.leggings = leggings;
        return this;
    }

    /**
     * Sets the chestplate of the armor.
     * @param chestplate The chestplate item stack.
     * @return This armor.
     */
    public CorpseArmor chestplate(@NotNull ItemStack chestplate) {
        this.chestplate = chestplate;
        return this;
    }

    /**
     * Sets the helmet of the armor.
     * @param helmet The helmet item stack.
     * @return This armor.
     */
    public CorpseArmor helmet(@NotNull ItemStack helmet) {
        this.helmet = helmet;
        return this;
    }

    /**
     * Gets the boots of the armor.
     * @return The boots item stack.
     */
    public ItemStack getBoots() {
        return boots;
    }

    /**
     * Gets the leggings of the armor.
     * @return The leggings item stack.
     */
    public ItemStack getLeggings() {
        return leggings;
    }

    /**
     * Gets the chestplate of the armor.
     * @return The chestplate item stack.
     */
    public ItemStack getChestplate() {
        return chestplate;
    }

    /**
     * Gets the helmet of the armor.
     * @return The helmet item stack.
     */
    public ItemStack getHelmet() {
        return helmet;
    }
}