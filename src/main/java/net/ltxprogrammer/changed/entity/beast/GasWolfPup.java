package net.ltxprogrammer.changed.entity.beast;

import net.ltxprogrammer.changed.entity.TransfurMode;
import net.ltxprogrammer.changed.entity.variant.EntityShape;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedTransfurVariants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

public class GasWolfPup extends GasWolf {
    protected static final int MAX_AGE = 72000;
    protected int age = 0;
    public GasWolfPup(EntityType<? extends GasWolfPup> type, Level level) {
        super(type, level);
    }

    @Override
    protected void setAttributes(AttributeMap attributes) {
        super.setAttributes(attributes);
        attributes.getInstance(Attributes.MOVEMENT_SPEED).setBaseValue(1.25);
        attributes.getInstance(ForgeMod.SWIM_SPEED.get()).setBaseValue(0.975);
        attributes.getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        attributes.getInstance(Attributes.MAX_HEALTH).setBaseValue(12.0);
    }

    @Override
    public double getMyRidingOffset() {
        return 0.2;
    }

    @Override
    public float getEyeHeightMul() {
        if (this.isCrouching())
            return 0.65F;
        else
            return 0.8F;
    }

    public boolean canBeLeashed(Player player) {
        return !this.isLeashed();
    }

    @Override
    public TransfurMode getTransfurMode() {
        return TransfurMode.NONE;
    }

    @Override
    public TransfurVariant<?> getTransfurVariant() {
        return ChangedTransfurVariants.GAS_WOLF.get();
    }

    @Override
    public @NotNull EntityShape getEntityShape() {
        return EntityShape.FERAL;
    }

    @Override
    public boolean isItemAllowedInSlot(ItemStack stack, EquipmentSlot slot) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR)
            return false;
        return super.isItemAllowedInSlot(stack, slot);
    }
} //TODO: Add aging process when female gas wolf is implemented.
