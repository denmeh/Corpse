package com.github.denmeh.corpse.corpse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.github.denmeh.corpse.CorpsePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class CorpseNPC {
    private final int id;
    private final UserProfile profile;
    private final WrapperPlayServerTeams.NameTagVisibility nameTagVisibility;

    private GameMode gamemode;
    private Component tabName;
    private NamedTextColor nameColor;
    private Component prefixName;
    private Component suffixName;
    private String teamName;
    private int displayPing = 0;
    private Location location = new Location(0.0, 0.0, 0.0, 0.0f, 0.0f);
    private ItemStack mainHand = null;
    private ItemStack offHand = null;
    private ItemStack helmet = null;
    private ItemStack chestPlate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;
    private final Set<Object> channels = new HashSet<>();
    private final Set<Object> tabChannels = new HashSet<>();

    private static final long TAB_REMOVE_DELAY_TICKS = 40L;

    private CorpseNPC(UserProfile profile, int entityId, @Nullable  WrapperPlayServerTeams.NameTagVisibility nameTagVisibility, GameMode gamemode, @Nullable Component tabName, @Nullable NamedTextColor nameColor,
                     @Nullable Component prefixName, @Nullable Component suffixName) {
        this.profile = profile;
        this.id = entityId;
        this.nameTagVisibility = nameTagVisibility;

        this.gamemode = gamemode;

        this.tabName = tabName;
        this.nameColor = nameColor;
        this.prefixName = prefixName;
        this.suffixName = suffixName;
        this.teamName = "npc-" + id;
    }

    private CorpseNPC(UserProfile profile, int entityId, @Nullable  WrapperPlayServerTeams.NameTagVisibility nameTagVisibility, @Nullable Component tabName) {
        this(profile, entityId, nameTagVisibility, GameMode.SURVIVAL, tabName, null, null, null);
    }

    CorpseNPC(UserProfile profile, int entityId, @Nullable WrapperPlayServerTeams.NameTagVisibility nameTagVisibility) {
        this(profile, entityId, nameTagVisibility, null);
    }

    public boolean hasSpawned(Object channel) {
        return channels.contains(channel);
    }

    public void spawn(Object channel) {
        if (hasSpawned(channel)) return;
        sendAddToTab(channel);

        //TODO Later if we want entity metadata, its not supported on newer server versions though(confirm if its mandatory on older versions)

        PacketWrapper<?> spawnPacket;
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            spawnPacket = new WrapperPlayServerSpawnEntity(getId(), getProfile().getUUID(), EntityTypes.PLAYER, getLocation(), getLocation().getYaw(), 0, null);
        }
        else {
            spawnPacket = new WrapperPlayServerSpawnPlayer(getId(),
                    getProfile().getUUID(),
                    getLocation());
        }
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnPacket);

        //Create team
        if (nameTagVisibility != null || getNameColor() != null || getPrefixName() != null
                || getSuffixName() != null) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, generateTeamsData());
        }
        channels.add(channel);

        // Skin needs the tab entry briefly; remove it so corpses do not clone the player in tab
        // and do not block reconnects.
        final Object spawnedChannel = channel;
        Bukkit.getScheduler().runTaskLaterAsynchronously(CorpsePlugin.getInstance(), () -> {
            if (hasSpawned(spawnedChannel)) {
                sendRemoveFromTab(spawnedChannel);
            }
        }, TAB_REMOVE_DELAY_TICKS);
    }

    public void despawn(Object channel) {
        if (!hasSpawned(channel)) return;
        sendRemoveFromTab(channel);
        //TODO Confirm if we need to destroy the team too
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(getId());
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroyEntities);
        channels.remove(channel);
    }

    public void despawnAll() {
        for (Object channel : new HashSet<>(channels)) {
            despawn(channel);
        }
    }

    private void sendAddToTab(Object channel) {
        PacketWrapper<?> playerInfo;
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> actions = EnumSet.of(
                    WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY
            );
            playerInfo = new WrapperPlayServerPlayerInfoUpdate(actions, getModernPlayerInfoData());
        } else {
            playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, getLegacyPlayerInfoData());
        }
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfo);
        tabChannels.add(channel);
    }

    private void sendRemoveFromTab(Object channel) {
        if (!tabChannels.remove(channel)) {
            return;
        }
        PacketWrapper<?> playerInfoRemove;
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            playerInfoRemove = new WrapperPlayServerPlayerInfoRemove(getProfile().getUUID());
        } else {
            playerInfoRemove = new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, getLegacyPlayerInfoData());
        }
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfoRemove);
    }

    public void teleport(Location to) {
        setLocation(to);
        for (Object channel : channels) {
            WrapperPlayServerEntityTeleport entityTeleport = new WrapperPlayServerEntityTeleport(getId(), to, true);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityTeleport);
        }
    }

    public void updateLocation(Location to) {
        Location from = getLocation();
        setLocation(to);
        double distXAbs = Math.abs(to.getPosition().getX() - from.getPosition().getX());
        double distYAbs = Math.abs(to.getPosition().getY() - from.getPosition().getY());
        double distZAbs = Math.abs(to.getPosition().getZ() - from.getPosition().getZ());
        boolean shouldUseEntityTeleport = distXAbs > 8 ||
                distYAbs > 8 ||
                distZAbs > 8;
        for (Object channel : channels) {
            if (shouldUseEntityTeleport) {
                WrapperPlayServerEntityTeleport entityTeleport =
                        new WrapperPlayServerEntityTeleport(getId(), to, true);
                PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityTeleport);
            } else {
                boolean rotationChanged =
                        to.getYaw() != from.getYaw() || to.getPitch() != from.getPitch();
                boolean positionChanged =
                        to.getPosition().getX() != from.getPosition().getX() ||
                                to.getPosition().getY() != from.getPosition().getY() ||
                                to.getPosition().getZ() != from.getPosition().getZ();
                double deltaX = positionChanged ? (to.getPosition().getX() - from.getPosition().getX()) : 0;
                double deltaY = positionChanged ? (to.getPosition().getY() - from.getPosition().getY()) : 0;
                double deltaZ = positionChanged ? (to.getPosition().getZ() - from.getPosition().getZ()) : 0;
                if (positionChanged && rotationChanged) {
                    WrapperPlayServerEntityRelativeMoveAndRotation entityRelativeMoveAndRotation =
                            new WrapperPlayServerEntityRelativeMoveAndRotation(getId(), deltaX, deltaY, deltaZ,
                                    to.getYaw(), to.getPitch(), true);
                    PacketEvents.getAPI().getProtocolManager()
                            .sendPacket(channel, entityRelativeMoveAndRotation);

                    WrapperPlayServerEntityHeadLook headYaw =
                            new WrapperPlayServerEntityHeadLook(getId(), to.getYaw());
                    PacketEvents.getAPI().getProtocolManager().sendPacket(channel, headYaw);
                } else if (positionChanged) {
                    WrapperPlayServerEntityRelativeMove entityRelativeMove =
                            new WrapperPlayServerEntityRelativeMove(getId(), deltaX, deltaY, deltaZ, true);
                    PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityRelativeMove);
                } else if (rotationChanged) {
                    WrapperPlayServerEntityRotation entityRotation =
                            new WrapperPlayServerEntityRotation(getId(), to.getYaw(), to.getPitch(), true);
                    PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityRotation);

                    WrapperPlayServerEntityHeadLook headYaw =
                            new WrapperPlayServerEntityHeadLook(getId(), to.getYaw());
                    PacketEvents.getAPI().getProtocolManager().sendPacket(channel, headYaw);
                }
            }
        }
    }

    public void updateRotation(float yaw, float pitch) {
        getLocation().setYaw(yaw);
        getLocation().setPitch(pitch);
        for (Object channel : channels) {
            WrapperPlayServerEntityRotation entityRotation =
                    new WrapperPlayServerEntityRotation(getId(), yaw, pitch, true);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityRotation);

            WrapperPlayServerEntityHeadLook headYaw =
                    new WrapperPlayServerEntityHeadLook(getId(), yaw);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, headYaw);
        }
    }

    public void updateTabPing(int ping) {
        setDisplayPing(ping);
        for (Object channel : channels) {
            PacketWrapper<?> playerInfo;
            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
                playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY, getModernPlayerInfoData());
            }
            else {
                playerInfo =
                        new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY, getLegacyPlayerInfoData());
            }
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfo);
        }
    }

    public void updateGameMode(GameMode gamemode) {
        setGameMode(gamemode);
        for (Object channel : channels) {
            PacketWrapper<?> playerInfo;
            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
                playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
                        getModernPlayerInfoData());
            }
            else {
                playerInfo =
                        new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_GAME_MODE, getLegacyPlayerInfoData());
            }
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfo);
        }
    }

    public void changeSkin(UUID skinUUID, List<TextureProperty> skinTextureProperties) {
        for (Object channel : channels) {
            sendRemoveFromTab(channel);

            WrapperPlayServerDestroyEntities destroyEntities =
                    new WrapperPlayServerDestroyEntities(getId());
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroyEntities);

            getProfile().setTextureProperties(skinTextureProperties);
            getProfile().setUUID(skinUUID);
            sendAddToTab(channel);

            PacketWrapper<?> spawnPacket;
            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
                spawnPacket = new WrapperPlayServerSpawnEntity(getId(), getProfile().getUUID(), EntityTypes.PLAYER, getLocation(), getLocation().getYaw(), 0, null);
            }
            else {
                spawnPacket = new WrapperPlayServerSpawnPlayer(getId(),
                        getProfile().getUUID(),
                        getLocation());
            }
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnPacket);
        }
    }

    public void updateNameTag(Object channel) {
        //Destroy team
        WrapperPlayServerTeams removeTeam =
                new WrapperPlayServerTeams(teamName,
                        WrapperPlayServerTeams.TeamMode.REMOVE,
                        Optional.empty());
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, removeTeam);

        if (nameTagVisibility != null || this.getNameColor() != null || this.getPrefixName() != null
                || this.getSuffixName() != null) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, generateTeamsData());
        }
    }

    public void updateEquipment(Object channel) {
        List<Equipment> equipmentList = new ArrayList<>();
        ItemStack handItem = getMainHand();
        if (handItem == null) {
            handItem = ItemStack.EMPTY;
        }
        equipmentList.add(new Equipment(EquipmentSlot.MAIN_HAND,
                handItem));
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
            ItemStack offHandItem = getOffHand();
            if (offHandItem == null) {
                offHandItem = ItemStack.EMPTY;
            }
            equipmentList.add(new Equipment(EquipmentSlot.OFF_HAND,
                    offHandItem));
        }
        ItemStack helmetItem = getHelmet();
        if (helmetItem == null) {
            helmetItem = ItemStack.EMPTY;
        }
        equipmentList.add(new Equipment(EquipmentSlot.HELMET,
                helmetItem));

        ItemStack chestPlateItem = getChestplate();
        if (chestPlateItem == null) {
            chestPlateItem = ItemStack.EMPTY;
        }
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE,
                chestPlateItem));
        ItemStack leggingsItem = getLeggings();
        if (leggingsItem == null) {
            leggingsItem = ItemStack.EMPTY;
        }
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS,
                leggingsItem));
        ItemStack bootsItem = getBoots();
        if (bootsItem == null) {
            bootsItem = ItemStack.EMPTY;
        }
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS,
                bootsItem));

        WrapperPlayServerEntityEquipment equipmentPacket
                = new WrapperPlayServerEntityEquipment(getId(),
                equipmentList);
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, equipmentPacket);
    }

    public ItemStack getMainHand() {
        return mainHand;
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return chestPlate;
    }

    public void setChestplate(ItemStack chestPlate) {
        this.chestPlate = chestPlate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    @Nullable
    public NamedTextColor getNameColor() {
        return nameColor;
    }

    public void setNameColor(@Nullable NamedTextColor nameColor) {
        this.nameColor = nameColor;
    }

    @Nullable
    public Component getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(@Nullable Component namePrefix) {
        this.prefixName = namePrefix;
    }

    @Nullable
    public Component getSuffixName() {
        return suffixName;
    }

    public void setSuffixName(@Nullable Component nameSuffix) {
        this.suffixName = nameSuffix;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_18)) {
            this.teamName = teamName.substring(0, 16);
        } else this.teamName = teamName;
    }

    @Nullable
    public Component getTabName() {
        return tabName;
    }

    public void setTabName(@Nullable Component tabName) {
        this.tabName = tabName;
    }

    public int getId() {
        return id;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public GameMode getGameMode() {
        return gamemode;
    }

    public void setGameMode(GameMode gamemode) {
        this.gamemode = gamemode;
    }

    private WrapperPlayServerTeams generateTeamsData() {
        return new WrapperPlayServerTeams(teamName,
                WrapperPlayServerTeams.TeamMode.CREATE,
                Optional.of(
                        new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                Component.text(teamName),
                                prefixName,
                                suffixName,
                                nameTagVisibility,
                                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                nameColor,
                                WrapperPlayServerTeams.OptionData.NONE
                        )),
                getProfile().getName());
    }

    public WrapperPlayServerPlayerInfo.PlayerData getLegacyPlayerInfoData() {
        return new WrapperPlayServerPlayerInfo.PlayerData(getTabName(),
                getProfile(), getGameMode(),
                getDisplayPing());
    }

    public WrapperPlayServerPlayerInfoUpdate.PlayerInfo getModernPlayerInfoData() {
        return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(getProfile(), false,
                getDisplayPing(), getGameMode(), getTabName(), null);
    }

    public int getDisplayPing() {
        return displayPing;
    }

    public void setDisplayPing(int ping) {
        this.displayPing = ping;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<Object> getChannels() {
        return channels;
    }
}