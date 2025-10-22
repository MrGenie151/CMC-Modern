package net.ltxprogrammer.changed.client;

import net.ltxprogrammer.changed.ability.GrabEntityAbility;
import net.ltxprogrammer.changed.block.WhiteLatexTransportInterface;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedAttributes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class LocalTransfurVariantInstance<T extends ChangedEntity> extends ClientTransfurVariantInstance<T> {
    private final LocalPlayer host;

    public LocalTransfurVariantInstance(TransfurVariant<T> parent, LocalPlayer host) {
        super(parent, host);
        this.host = host;
    }

    @Override
    protected void tickTransfurProgress() {
        super.tickTransfurProgress();

        if (transfurProgression < 1f || this.ageAsVariant < 30 || !this.getItemUseMode().holdMainHand || GrabEntityAbility.getControllingEntity(this.host) != this.host) {
            ((LocalPlayerAccessor)host).setHandsBusy(true);
        } else if (host.getVehicle() == null && host.isHandsBusy()) {
            ((LocalPlayerAccessor)host).setHandsBusy(false);
        }
    }

    public final UUID sprintSpeedModifier = Mth.createInsecureUUID(RandomSource.createNewThreadLocalInstance());
    private static final UUID ENTITY_SPEED_MODIFIER_SPRINTING_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");

    public void handleSprintModifier(AttributeInstance movementSpeed) {
        if (movementSpeed.getModifier(ENTITY_SPEED_MODIFIER_SPRINTING_UUID) != null) {
            // Vanilla sprint speed = MOVEMENT_SPEED + (0.3 * MOVEMENT_SPEED)
            var sprintMultiplier = host.getAttributeValue(ChangedAttributes.SPRINT_SPEED.get());
            var delta = (sprintMultiplier * 0.3) - 0.3;

            var sprintModifier = movementSpeed.getModifier(sprintSpeedModifier);
            if (sprintModifier != null && sprintModifier.getAmount() == delta)
                return;
            if (sprintModifier == null && delta == 0.0)
                return;

            movementSpeed.removeModifier(sprintSpeedModifier);
            if (delta != 0.0)
                movementSpeed.addTransientModifier(new AttributeModifier(sprintSpeedModifier, "Sprinting speed boost modifier", delta, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (WhiteLatexTransportInterface.isEntityInWhiteLatex(host)) {
            ((LocalPlayerAccessor)host).setHandsBusy(true);
        }

        var movementSpeed = host.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null)
            return;

        this.handleSprintModifier(movementSpeed);
    }

    @Override
    public void unhookAll(Player player) {
        super.unhookAll(player);

        if (host.getVehicle() == null && host.isHandsBusy()) {
            ((LocalPlayerAccessor)host).setHandsBusy(false);
        }
    }
}
