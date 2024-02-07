package org.samo_lego.taterzens.common.mixin.accessors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface AChunkMap {
    @Accessor("entityMap")
    Int2ObjectMap<AEntityTrackerEntry> getEntityMap();
}
