package org.samo_lego.taterzens.common.mixin.network;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Handles bungee packets.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_BungeeListener extends ServerCommonPacketListenerImpl {
    @Unique
    private static final String GET_SERVERS = "GetServers";
    @Shadow
    public ServerPlayer player;

    @Unique
    private static final String taterzens$permission = "taterzens.npc.edit.commands.addBungee";

    public ServerGamePacketListenerImplMixin_BungeeListener(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    // todo
    /*@Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void taterzens_onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        ResourceLocation packetId = packet.payload().id();
        CommandSourceStack commandSourceStack = player.createCommandSourceStack();
        boolean hasPermission = Taterzens.getInstance().getPlatform().checkPermission(commandSourceStack, taterzens$permission, config.perms.npcCommandPermissionLevel);

        if (AVAILABLE_SERVERS.isEmpty() && config.bungee.enableCommands && hasPermission) {
            if (packetId.equals(BUNGEE_CHANNEL)) {
                // Reading data
                byte[] bytes = new byte[packet.getData().readableBytes()];
                packet.getData().readBytes(bytes);

                // Parsing the response
                if (bytes.length != 0) {
                    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
                    String subchannel = in.readUTF();

                    if (subchannel.equals(GET_SERVERS)) {
                        // Adding available servers to suggestions
                        String[] servers = in.readUTF().split(", ");
                        Collections.addAll(AVAILABLE_SERVERS, servers);
                    }
                }
            } else if (packetId.equals(BRAND)) {
                // Fetch available servers from proxy
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(GET_SERVERS);
                BungeeCommand.sendProxyPacket((ServerGamePacketListenerImpl) (Object) this, out.toByteArray());
            }
        }
    }*/
}
