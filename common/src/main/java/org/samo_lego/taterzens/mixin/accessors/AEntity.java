package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface AEntity {
    @Accessor("DATA_SHARED_FLAGS_ID")
    static EntityDataAccessor<Byte> FLAGS() {
        throw new AssertionError();
    }

    @Accessor("FLAG_GLOWING")
    static int FLAG_GLOWING() {
        throw new AssertionError();
    }
}
