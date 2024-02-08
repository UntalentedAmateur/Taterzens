package org.samo_lego.taterzens.common.mixin.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.samo_lego.taterzens.common.interfaces.ITaterzenEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.samo_lego.taterzens.common.Taterzens;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Optional;

import static org.samo_lego.taterzens.common.Taterzens.config;
import static org.samo_lego.taterzens.common.util.TextUtil.successText;

/**
 * Additional methods for players to track {@link TaterzenNPC}
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixinCast_ITaterzenEditor implements ITaterzenEditor {

    @Unique
    private final ServerPlayer self = (ServerPlayer) (Object) this;

    @Unique
    private TaterzenNPC selectedNpc;
    @Unique
    private int selectedMsgId = -1; // -1 as no selected msg to edit

    @Unique
    private byte lastRenderTick;
    @Unique
    private EditorMode editorMode = EditorMode.NONE;

    /**
     * Used for showing the path particles.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        ITaterzenEditor editor = (ITaterzenEditor) this.self;
        if (editor.getSelectedNpc().isPresent() && lastRenderTick++ > 4) {
            if (this.editorMode == EditorMode.PATH) {
                ArrayList<BlockPos> pathTargets = editor.getSelectedNpc().get().getPathTargets();
                DustParticleOptions effect = new DustParticleOptions(
                        new Vector3f(
                                config.path.color.red / 255.0F,
                                config.path.color.green / 255.0F,
                                config.path.color.blue / 255.0F
                        ),
                        1.0F);

                for (int i = 0; i < pathTargets.size(); ++i) {
                    BlockPos pos = pathTargets.get(i);
                    BlockPos nextPos = pathTargets.get(i + 1 == pathTargets.size() ? 0 : i + 1);

                    int deltaX = pos.getX() - nextPos.getX();
                    int deltaY = pos.getY() - nextPos.getY();
                    int deltaZ = pos.getZ() - nextPos.getZ();

                    double distance = Math.sqrt(pos.distSqr(nextPos));
                    for (double j = 0; j < distance; j += 0.5D) {
                        double x = pos.getX() - j / distance * deltaX;
                        double y = pos.getY() - j / distance * deltaY;
                        double z = pos.getZ() - j / distance * deltaZ;
                        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(effect, true, x + 0.5D, y + 1.5D, z + 0.5D, 0.1F, 0.1F, 0.1F, 1.0F, 1);
                        this.self.connection.send(packet);
                    }
                }
            }
            if (this.editorMode != EditorMode.NONE) {
                self.displayClientMessage(successText("taterzens.tooltip.current_editor", String.valueOf(this.editorMode)), true);
            }

            this.lastRenderTick = 0;
        }
    }

    @Override
    public void setEditorMode(EditorMode mode) {
        ITaterzenEditor editor = (ITaterzenEditor) this.self;

        if (editor.getSelectedNpc().isPresent()) {
            Level world = self.level();
            if (this.editorMode == EditorMode.PATH && mode != EditorMode.PATH) {
                editor.getSelectedNpc().get().getPathTargets().forEach(blockPos -> self.connection.send(
                        new ClientboundBlockUpdatePacket(blockPos, world.getBlockState(blockPos))
                ));
            } else if (this.editorMode != EditorMode.PATH && mode == EditorMode.PATH) {
                editor.getSelectedNpc().get().getPathTargets().forEach(blockPos -> self.connection.send(
                        new ClientboundBlockUpdatePacket(blockPos, Blocks.REDSTONE_BLOCK.defaultBlockState())
                ));
            }

            if (this.editorMode == EditorMode.MESSAGES && mode != EditorMode.MESSAGES) {
                this.setEditingMessageIndex(-1);
            }
        }

        this.editorMode = mode;
    }

    @Override
    public EditorMode getEditorMode() {
        return this.editorMode;
    }


    @Override
    public Optional<TaterzenNPC> getSelectedNpc() {
        return Optional.ofNullable(this.selectedNpc);
    }

    @Override
    public boolean selectNpc(@Nullable TaterzenNPC npc) {
        if (npc != null && !npc.allowEditBy(this.self) &&
                !Taterzens.getInstance().getPlatform().checkPermission(
                        this.self.createCommandSourceStack(), "taterzens.npc.select.bypass", config.perms.selectBypassLevel)) {
            return false;
        }

        if (this.getEditorMode() != EditorMode.NONE) {
            this.setEditorMode(EditorMode.NONE);
        }

        TaterzenNPC selectedNpc = this.selectedNpc;
        this.selectedNpc = npc;

        if (npc != null) {
            npc.broadcastProfileUpdates();
        }

        if (selectedNpc != null) {
            selectedNpc.broadcastProfileUpdates();
        }

        return true;
    }

    @Override
    public void setEditingMessageIndex(int selected) {
        this.selectedMsgId = selected;
    }

    @Override
    public int getEditingMessageIndex() {
        return this.selectedMsgId;
    }
}
