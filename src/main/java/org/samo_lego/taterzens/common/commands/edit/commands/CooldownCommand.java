package org.samo_lego.taterzens.common.commands.edit.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.world.entity.Entity;
import org.samo_lego.taterzens.common.commands.NpcCommand;
import org.samo_lego.taterzens.common.Taterzens;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.common.Taterzens.config;
import static org.samo_lego.taterzens.common.util.TextUtil.successText;

public class CooldownCommand {

    public static void registerNode(LiteralCommandNode<CommandSourceStack> commandsNode) {
        LiteralCommandNode<CommandSourceStack> cooldown = literal("cooldown")
                .requires(cs -> Taterzens.getInstance().getPlatform().checkPermission(cs, "taterzens.edit.commands.cooldown", config.perms.npcCommandPermissionLevel))
                .then(literal("set")
                        .requires(cs -> Taterzens.getInstance().getPlatform().checkPermission(cs, "taterzens.edit.commands.cooldown.set", config.perms.npcCommandPermissionLevel))
                        .then(argument("cooldown", LongArgumentType.longArg(0))
                                .executes(CooldownCommand::setCooldown)
                        )
                )
                .then(literal("editMessage")
                        .requires(cs -> Taterzens.getInstance().getPlatform().checkPermission(cs, "taterzens.edit.commands.cooldown.edit_message", config.perms.npcCommandPermissionLevel))
                        .then(argument("new cooldown message", MessageArgument.message())
                                .executes(CooldownCommand::setMessage)
                        )
                )
                .build();

        commandsNode.addChild(cooldown);
    }

    private static int setMessage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = context.getSource().getEntityOrException();
        String msg = MessageArgument.getMessage(context, "new cooldown message").getString();
        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            taterzen.setCooldownMessage(msg);
            entity.sendSystemMessage(
                    successText("taterzens.command.commands.cooldown.edit_message", msg, taterzen.getName().getString()));
        });

    }

    private static int setCooldown(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = context.getSource().getEntityOrException();
        long cooldown = LongArgumentType.getLong(context, "cooldown");
        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            taterzen.setMinCommandInteractionTime(cooldown);
            entity.sendSystemMessage(
                    successText("taterzens.command.commands.cooldown.set", String.valueOf(cooldown), taterzen.getName().getString()));
        });
    }
}
