package org.samo_lego.taterzens.common.mixin.accessors;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface AEntityTrackerEntry {
    @Accessor("serverEntity")
    ServerEntity getPlayer();

    @Accessor("seenBy")
    Set<ServerPlayerConnection> getSeenBy();
}
