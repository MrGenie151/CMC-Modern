package net.ltxprogrammer.changed.network.packet;

import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.entity.BasicPlayerInfo;
import net.ltxprogrammer.changed.entity.PlayerDataExtension;
import net.ltxprogrammer.changed.util.UniversalDist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class BasicPlayerInfoPacket implements ChangedPacket {
    public static final BasicPlayerInfoPacket EMPTY = new BasicPlayerInfoPacket(Map.of());

    private final Map<UUID, BasicPlayerInfo> playerInfos;
    public BasicPlayerInfoPacket(Map<UUID, BasicPlayerInfo> playerInfos) {
        this.playerInfos = playerInfos;
    }

    public BasicPlayerInfoPacket(FriendlyByteBuf buffer) {
        this.playerInfos = new HashMap<>();
        buffer.readList(next ->
                new Pair<>(next.readUUID(), new BasicPlayerInfo(next.readNbt()))).forEach(pair ->
                playerInfos.put(pair.getFirst(), pair.getSecond()));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(playerInfos.entrySet(), (next, form) -> {
            next.writeUUID(form.getKey());
            CompoundTag bpiTag = new CompoundTag();
            form.getValue().save(bpiTag);
            next.writeNbt(bpiTag);
        });
    }

    @Override
    public CompletableFuture<Void> handle(NetworkEvent.Context context, CompletableFuture<Level> levelFuture, Executor sidedExecutor) {
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            context.setPacketHandled(true);
            return levelFuture.thenAccept(level -> {
                Player localPlayer = UniversalDist.getLocalPlayer();

                if (!playerInfos.isEmpty()) {
                    Objects.requireNonNull(level);
                    playerInfos.forEach((uuid, listing) -> {
                        var player = level.getPlayerByUUID(uuid);
                        if (player instanceof PlayerDataExtension ext && player != localPlayer) {
                            ext.getBasicPlayerInfo().copyFrom(listing);
                        }
                    });
                }

                else {
                    Changed.PACKET_HANDLER.sendToServer(BasicPlayerInfoPacket.Builder.of(localPlayer));
                }
            });
        }

        else { // Mirror packet
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                if (sender instanceof PlayerDataExtension ext && playerInfos.containsKey(sender.getUUID())) {
                    BasicPlayerInfo received = playerInfos.get(sender.getUUID());
                    ext.getBasicPlayerInfo().copyFrom(received); // Keep player info state

                    Changed.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> sender),
                            new BasicPlayerInfoPacket(Map.of(sender.getUUID(), received)));
                }
            }
            context.setPacketHandled(true);
            return CompletableFuture.completedFuture(null);
        }
    }

    public static class Builder {
        private final Map<UUID, BasicPlayerInfo> playerInfos = new HashMap<>();

        public void addPlayer(Player player) {
            if (player instanceof PlayerDataExtension ext) {
                playerInfos.put(player.getUUID(), ext.getBasicPlayerInfo());
            }
        }

        public boolean worthSending() {
            return !playerInfos.isEmpty();
        }

        public BasicPlayerInfoPacket build() {
            return new BasicPlayerInfoPacket(playerInfos);
        }

        public static BasicPlayerInfoPacket of(Player player) {
            Builder builder = new Builder();
            builder.addPlayer(player);
            return builder.build();
        }
    }
}
