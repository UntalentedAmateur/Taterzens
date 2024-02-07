package org.samo_lego.taterzens.common.mixin.network;

import com.google.gson.JsonParseException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.samo_lego.taterzens.common.interfaces.ITaterzenEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

import static org.samo_lego.taterzens.common.Taterzens.config;
import static org.samo_lego.taterzens.common.util.TextUtil.successText;
import static org.samo_lego.taterzens.common.util.TextUtil.translate;

@Mixin(PlayerList.class)
public class ServerGamePacketListenerImplMixin_MsgEditor {

    /**
     * Catches messages; if player is in
     * message edit mode, messages sent to chat
     * will be saved to taterzen instead.
     */
    @Inject(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void taterzens_chatBroadcast(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer player, ChatType.Bound bound, CallbackInfo ci) {
        if (player == null) return;

        ITaterzenEditor editor = (ITaterzenEditor) player;
        var taterzen = editor.getSelectedNpc();
        String msg = playerChatMessage.decoratedContent().getString();

        if (taterzen.isPresent() && ((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES && !msg.startsWith("/")) {
            if (msg.startsWith("delay")) {
                String[] split = msg.split(" ");
                if (split.length > 1) {
                    try {
                        int delay = Integer.parseInt(split[1]);
                        taterzen.get().setMessageDelay(editor.getEditingMessageIndex(), delay);
                        player.displayClientMessage(successText("taterzens.command.message.delay", String.valueOf(delay)), false);
                    } catch (NumberFormatException ignored) {

                    }
                }
            } else {
                Component text;
                if ((msg.startsWith("{") && msg.endsWith("}") || (msg.startsWith("[") && msg.endsWith("]")))) {
                    // NBT tellraw message structure, try parse it
                    try {
                        text = Component.Serializer.fromJson(msg);
                    } catch (JsonParseException ignored) {
                        player.displayClientMessage(translate("taterzens.error.invalid.text").withStyle(ChatFormatting.RED), false);
                        ci.cancel();
                        return;
                    }
                } else {
                    text = Component.literal(msg);
                }


                if ((editor).getEditingMessageIndex() != -1) {
                    // Editing selected message
                    taterzen.get().editMessage(editor.getEditingMessageIndex(), text); // Editing message
                    player.displayClientMessage(successText("taterzens.command.message.changed", text.getString()), false);

                    // Exiting the editor
                    if (config.messages.exitEditorAfterMsgEdit) {
                        ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                        (editor).setEditingMessageIndex(-1);
                        player.displayClientMessage(translate("taterzens.command.equipment.exit").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    }
                } else {
                    taterzen.get().addMessage(text); // Adding message
                    player.displayClientMessage(successText("taterzens.command.message.editor.add", text.getString()), false);
                }

            }
            ci.cancel();
        }
    }
}