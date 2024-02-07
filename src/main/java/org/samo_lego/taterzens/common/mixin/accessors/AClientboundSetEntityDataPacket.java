package org.samo_lego.taterzens.common.mixin.accessors;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundSetEntityDataPacket.class)
public interface AClientboundSetEntityDataPacket {
    @Mutable
    @Accessor("id")
    int getEntityId();

    @Mutable
    @Accessor("packedItems")
    void setPackedItems(List<SynchedEntityData.DataValue<?>> packedItems);
}
