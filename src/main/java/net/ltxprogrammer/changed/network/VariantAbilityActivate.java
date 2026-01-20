package net.ltxprogrammer.changed.network;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.ability.AbstractAbility;
import net.ltxprogrammer.changed.ability.GrabEntityAbility;
import net.ltxprogrammer.changed.init.ChangedAbilities;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.network.packet.ChangedPacket;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.ltxprogrammer.changed.world.inventory.AbilityRadialMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VariantAbilityActivate implements ChangedPacket {
    final UUID uuid;
    final boolean keyDown;
    final AbstractAbility<?> ability;

    public static VariantAbilityActivate openRadial(Player player) {
        return new VariantAbilityActivate(player, false);
    }

    public VariantAbilityActivate(Player player, boolean keyDown, AbstractAbility<?> ability) {
        this.uuid = player.getUUID();
        this.keyDown = keyDown;
        this.ability = ability;
    }

    public VariantAbilityActivate(Player player, boolean keyDown) {
        this.uuid = player.getUUID();
        this.keyDown = keyDown;
        this.ability = null;
    }

    public VariantAbilityActivate(FriendlyByteBuf buffer) {
        this.uuid = buffer.readUUID();
        this.keyDown = buffer.readBoolean();
        this.ability = ChangedRegistry.ABILITY.readRegistryObject(buffer);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeBoolean(keyDown);
        ChangedRegistry.ABILITY.writeRegistryObject(buffer, ability);
    }

    @Override
    public CompletableFuture<Void> handle(NetworkEvent.Context context, CompletableFuture<Level> levelFuture, Executor sidedExecutor) {
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            context.setPacketHandled(true);
            return levelFuture.thenAccept(level -> {
                ProcessTransfur.ifPlayerTransfurred(level.getPlayerByUUID(this.uuid), (player, variant) -> {
                    context.setPacketHandled(true);
                    if (variant.isTemporaryFromSuit())
                        return;

                    if (ability != null)
                        variant.setSelectedAbility(ability);

                    if ((keyDown || ability != null) && variant.abilityKeyStateFlips < 6) { // Prevent DoS by limiting flip count / tick
                        if (variant.isAbilityKeyEffectivelyDown() != keyDown)
                            variant.abilityKeyStateFlips++; // Only queue flip if keyDown is opposite to what the keyState is
                    }
                });
            });
        }

        else {
            final var sender = context.getSender();
            if (!sender.getUUID().equals(this.uuid))
                return CompletableFuture.failedFuture(new IllegalArgumentException("Incorrect UUID for sending player"));

            ProcessTransfur.ifPlayerTransfurred(sender, (variant) -> {
                context.setPacketHandled(true);

                GrabEntityAbility.getGrabberSafe(sender).ifPresent(entity -> {
                    if (entity.getAbilityInstanceSafe(ChangedAbilities.GRAB_ENTITY_ABILITY.get())
                            .map(ability -> ability.grabbedHasControl).orElse(false)) {
                        entity.getEntity().interact(sender, InteractionHand.MAIN_HAND);
                    }
                });

                if (variant.isTemporaryFromSuit())
                    return;

                if (ability != null)
                    variant.setSelectedAbility(ability);

                if (!keyDown && ability == null) {
                    if (!sender.isUsingItem())
                        sender.openMenu(new SimpleMenuProvider((id, inventory, givenPlayer) ->
                                new AbilityRadialMenu(id, inventory, null), AbilityRadialMenu.CONTAINER_TITLE));
                }
                else if (variant.abilityKeyStateFlips < 6) { // Prevent DoS by limiting flip count / tick
                    if (variant.isAbilityKeyEffectivelyDown() != keyDown)
                        variant.abilityKeyStateFlips++; // Only queue flip if keyDown is opposite to what the keyState is
                }

                Changed.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> sender), this);
            });

            return CompletableFuture.completedFuture(null);
        }
    }
}
