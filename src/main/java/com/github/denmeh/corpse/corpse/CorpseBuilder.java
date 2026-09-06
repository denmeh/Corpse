package com.github.denmeh.corpse.corpse;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;

import io.github.retrooper.packetevents.util.SpigotReflectionUtil;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.github.denmeh.corpse.model.CorpseArmor;
import com.github.denmeh.corpse.pool.CorpsePool;

/**
 * Builder for creating a Corpse object.
 */
public class CorpseBuilder {

    private Location location;
    private List<TextureProperty> textures = new ArrayList<>();
    private CorpseArmor armor = null;
    private String name = null;
    private Player player = null;
    private Integer time = null;

    /**
     * Constructor for creating a CorpseBuilder with a player.
     * @param player The player to create the CorpseBuilder for.
     */
    CorpseBuilder(@NotNull Player player) {
        this.location = player.getLocation();
        this.textures = SpigotReflectionUtil.getUserProfile(player);
        this.armor = new CorpseArmor(player);
        this.name = player.getName();
        this.player = player;
    }

    /**
     * Constructor for creating a CorpseBuilder with a location.
     * @param location The location to create the CorpseBuilder for.
     */
    CorpseBuilder(@NotNull Location location) {
        this.location = location;
    }

    /**
     * Set the location of the corpse.
     * @param location The location to set.
     * @return This builder.
     */
    public CorpseBuilder location(@NotNull Location location) {
        this.location = location;
        return this;
    }

    /**
     * Set the textures of the corpse (skin).
     * @param textures The textures to set; a copy is stored.
     * @return This builder.
     */
    public CorpseBuilder textures(@NotNull List<TextureProperty> textures) {
        this.textures = new ArrayList<>(textures);
        return this;
    }

    /**
     * Set the armor of the corpse.
     * @param armor The armor to set.
     * @return This builder.
     */
    public CorpseBuilder armor(@NotNull CorpseArmor armor) {
        this.armor = armor;
        return this;
    }

    /**
     * Set the display name of the corpse.
     * @param name The name to set.
     * @return This builder.
     */
    public CorpseBuilder name(@NotNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Override how long the corpse lasts, in seconds. {@code -1} means never despawn.
     * If unset, the owner's {@code corpse-time-groups} permission is used when known,
     * otherwise {@code corpse-time}.
     * @param time Seconds before despawn, or {@code -1} for forever.
     * @return This builder.
     */
    public CorpseBuilder time(int time) {
        this.time = time;
        return this;
    }

    /**
     * Spawn the corpse in the world.
     * @return The spawned Corpse instance.
     */
    public Corpse spawn() {
        return new Corpse(location, textures, armor, name, resolveTime());
    }

    private int resolveTime() {
        if (this.time != null) {
            return this.time;
        }
        return CorpsePool.getInstance().getTimeRemove(this.player);
    }
}
