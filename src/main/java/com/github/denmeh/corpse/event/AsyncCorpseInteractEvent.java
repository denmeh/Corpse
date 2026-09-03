package com.github.denmeh.corpse.event;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.denmeh.corpse.corpse.Corpse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncCorpseInteractEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Corpse corpse;
    private final WrapperPlayClientInteractEntity.InteractAction action;

    public AsyncCorpseInteractEvent(@NotNull Player player, @NotNull Corpse corpse, WrapperPlayClientInteractEntity.InteractAction action) {
        super(true); // async
        this.player = player;
        this.corpse = corpse;
        this.action = action;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Corpse getCorpse() {
        return corpse;
    }

    public WrapperPlayClientInteractEntity.InteractAction getAction() {
        return action;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}