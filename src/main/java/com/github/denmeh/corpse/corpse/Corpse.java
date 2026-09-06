package com.github.denmeh.corpse.corpse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUseBed;
import com.github.denmeh.corpse.model.CorpseArmor;
import com.github.denmeh.corpse.pool.CorpsePool;
import com.github.denmeh.corpse.util.BedUtil;
import com.github.denmeh.corpse.util.ProfileUtils;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Corpse class that represents a dead body.
 * To create a Corpse, use fromPlayer, fromLocation methods.
 */
public class Corpse {


    /**
     * Create a CorpseBuilder from a player.
     * @param player The player to create the CorpseBuilder for.
     * @return A CorpseBuilder object.
     */
    public static CorpseBuilder fromPlayer(@NotNull Player player) {
        return new CorpseBuilder(player);
    }

    /**
     * Create a CorpseBuilder from a location.
     * @param location The location to create the CorpseBuilder for.
     * @return A CorpseBuilder object.
     */
    public static CorpseBuilder fromLocation(@NotNull Location location) {
        return new CorpseBuilder(location);
    }

    protected final int id;
    protected final Location location;
    protected final UserProfile profile;
    private final Collection<Player> seeingPlayers = new CopyOnWriteArraySet<>();
    private final CorpsePool pool;

    private final CorpseNPC internalNPC;
    private final boolean hasArmor;
    private final long expiresAtMillis;

    Corpse(
            @NotNull Location location,
            @NotNull List<TextureProperty> textures,
            @Nullable CorpseArmor armor,
            @Nullable String name
    ) {
        this(location, textures, armor, name, CorpsePool.getInstance().getTimeRemove());
    }

    Corpse(
            @NotNull Location location,
            @NotNull List<TextureProperty> textures,
            @Nullable CorpseArmor armor,
            @Nullable String name,
            int time
    ) {
        pool = CorpsePool.getInstance();

        this.id = pool.getFreeEntityId();
        this.location = location;
        this.profile = new UserProfile(UUID.randomUUID(), name != null ? name : ProfileUtils.randomName(), textures);

        // create npc
        internalNPC = new CorpseNPC(profile, id,
                pool.isShowTags() ? null : WrapperPlayServerTeams.NameTagVisibility.NEVER);

        // set npc location
        internalNPC.setLocation(SpigotConversionUtil.fromBukkitLocation(location));

        // set npc armor
        if (pool.isRenderArmor() && armor != null) {
            this.setArmor(armor);
            hasArmor = true;
        } else {
            hasArmor = false;
        }

        //pool take care
        pool.takeCareOf(this);

        this.expiresAtMillis = time > -1
                ? System.currentTimeMillis() + (time * 1000L)
                : Long.MAX_VALUE;
    }

    @ApiStatus.Internal
    public void show(@NotNull Player player) {
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(player.getUniqueId());
        if (channel == null) {
            return;
        }

        this.seeingPlayers.add(player);

        // Spawn npc
        internalNPC.spawn(channel);

        // set npc sleeping metadata
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
            player.sendBlockChange(BedUtil.getBedLocation(location), Material.valueOf("BED_BLOCK"),
                    (byte) BedUtil.yawToFacing(location.getYaw()));
            WrapperPlayServerUseBed packet = new WrapperPlayServerUseBed(id, new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, packet);
//                sendPackets(player,
//                        this.packetLoader.getWrapperBed().get(),
//                        this.packetLoader.getWrapperEntityTeleport()
//                                .get());  // Set the correct height of the player lying down
        } else {
            List<EntityData<?>> entityData = new ArrayList<>();
            entityData.add(new EntityData<>(6, EntityDataTypes.ENTITY_POSE, pool.getEntityPose()));
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(id, entityData);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, packet);
        }

        // show armor
        if (hasArmor) {
            internalNPC.updateEquipment(channel);
        }

    }

    @ApiStatus.Internal
    public void hide(@NotNull Player player) {
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(player.getUniqueId());
        if (channel == null) {
            this.seeingPlayers.remove(player);
            return;
        }

        internalNPC.despawn(channel);
        this.seeingPlayers.remove(player);
    }

    public boolean isShownFor(@NotNull Player player) {
        return this.seeingPlayers.contains(player);
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis >= this.expiresAtMillis;
    }

    /**
     * Removes this corpse from the world.
     */
    public void destroy() {
        CorpsePool.getInstance().remove(this.id);
    }

    /**
     * Gets the entity id of the corpse.
     * @return The entity id of the corpse.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the corpse.
     * @return The name of the corpse.
     */
    @NotNull
    public String getName() {
        return profile.getName();
    }

    /**
     * Gets the location of the corpse.
     * @return The location of the corpse.
     */
    @NotNull
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the players that are seeing the corpse.
     * @return The players that are seeing the corpse.
     */
    @NotNull
    public Collection<Player> getSeeingPlayers() {
        return Collections.unmodifiableCollection(this.seeingPlayers);
    }

    /**
     * Sets the armor of the corpse.
     * @param armor The armor to set.
     */
    private void setArmor(@NotNull CorpseArmor armor) {
        if (armor.getBoots() != null)
            internalNPC.setBoots(SpigotConversionUtil.fromBukkitItemStack(armor.getBoots()));
        if (armor.getLeggings() != null)
            internalNPC.setLeggings(SpigotConversionUtil.fromBukkitItemStack(armor.getLeggings()));
        if (armor.getChestplate() != null)
            internalNPC.setChestplate(SpigotConversionUtil.fromBukkitItemStack(armor.getChestplate()));
        if (armor.getHelmet() != null)
            internalNPC.setHelmet(SpigotConversionUtil.fromBukkitItemStack(armor.getHelmet()));
    }
}
