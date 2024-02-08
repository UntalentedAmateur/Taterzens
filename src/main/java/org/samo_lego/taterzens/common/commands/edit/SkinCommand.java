package org.samo_lego.taterzens.common.commands.edit;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.samo_lego.taterzens.common.Taterzens;
import org.samo_lego.taterzens.common.commands.NpcCommand;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.taterzens.common.Taterzens.GSON;
import static org.samo_lego.taterzens.common.Taterzens.config;
import static org.samo_lego.taterzens.common.compatibility.ModDiscovery.FABRICTAILOR_LOADED;
import static org.samo_lego.taterzens.common.mixin.accessors.APlayer.getPLAYER_MODE_CUSTOMISATION;
import static org.samo_lego.taterzens.common.util.TextUtil.*;
import static org.samo_lego.taterzens.common.util.WebUtil.urlRequest;

public class SkinCommand {

    private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/uuid/";
    private static final String MOJANG_NAME2UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_UUID2SKIN = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> skinNode = literal("skin")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.skin", config.perms.npcCommandPermissionLevel))
                .then(argument("mineskin|player", message())
                        .executes(SkinCommand::setCustomSkin)
                )
                .executes(SkinCommand::copySkinLayers)
                .build();

        editNode.addChild(skinNode);
    }

    private static int setCustomSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String id = MessageArgument.getMessage(context, "mineskin|player").getString();
        Entity entity = source.getEntityOrException();

        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            // Shameless self-promotion
            if(config.fabricTailorAdvert) {
                if(FABRICTAILOR_LOADED) {
                    source.sendSuccess(() -> translate("advert.fabrictailor.skin_command")
                                    .withStyle(ChatFormatting.GOLD)
                                    .withStyle(style ->
                                            style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skin set"))
                                    ),
                            false
                    );
                } else {
                    source.sendSuccess(() -> translate("advert.fabrictailor")
                                    .withStyle(ChatFormatting.ITALIC)
                                    .withStyle(ChatFormatting.GOLD)
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/FabricTailor"))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("advert.tooltip.install", "FabricTailor")))
                                    ),
                            false
                    );
                }

            }
            THREADPOOL.submit(() -> {
                URL url = null;
                if(id.contains(":") ) {
                    // Mineskin
                    String param = id.substring(id.lastIndexOf('/') + 1);  // + 1 so as to not include "/"
                    String mineskinUrl = MINESKIN_API_URL + param;
                    try {
                        url = new URL(mineskinUrl);
                    } catch(MalformedURLException e) {
                        source.sendFailure(errorText("taterzens.error.invalid.url", mineskinUrl));
                    }
                } else {
                    // Get skin by player's name
                    try {
                        String uuidReply = urlRequest(new URL(MOJANG_NAME2UUID + id));
                        if(uuidReply != null) {
                            JsonObject replyJson = GSON.fromJson(uuidReply, JsonObject.class);
                            String uuid = replyJson.get("id").getAsString();

                            url = new URL(String.format(MOJANG_UUID2SKIN, uuid));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(url != null) {
                    try {
                        String reply = urlRequest(url);
                        if(reply != null && !reply.contains("error") && !reply.isEmpty()) {

                            String value = reply.split("\"value\":\"")[1].split("\"")[0];
                            String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

                            // Setting the skin
                            if(!value.isEmpty() && !signature.isEmpty()) {
                                CompoundTag skinTag = new CompoundTag();
                                skinTag.putString("value", value);
                                skinTag.putString("signature", signature);

                                taterzen.setSkinFromTag(skinTag);
                                taterzen.broadcastProfileUpdates();


                                context.getSource().sendSuccess(() ->
                                                successText("taterzens.command.skin.fetched", id),
                                        false
                                );
                            }
                        } else {
                            context.getSource().sendFailure(errorText("taterzens.command.skin.error", id));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }


    private static int copySkinLayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            Byte skinLayers = player.getEntityData().get(getPLAYER_MODE_CUSTOMISATION());
            taterzen.setSkinLayers(skinLayers);

            taterzen.broadcastProfileUpdates();
            source.sendSuccess(() ->
                            successText("taterzens.command.skin.mirrored", taterzen.getName().getString()),
                    false
            );
        });
    }


}
