package net.ltxprogrammer.changed.entity.decoration;

import net.ltxprogrammer.changed.entity.variant.ClothingShape;
import net.ltxprogrammer.changed.entity.variant.EntityShape;
import net.ltxprogrammer.changed.item.ExtendedItemProperties;
import net.ltxprogrammer.changed.world.enchantments.FormFittingEnchantment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractArmorStand extends ArmorStand implements EntityShape.Provider {
    public AbstractArmorStand(EntityType<? extends AbstractArmorStand> entityType, Level level) {
        super(entityType, level);
    }

    @NotNull
    public abstract EntityShape getEntityShape();

    @Override
    protected boolean swapItem(Player player, EquipmentSlot slot, ItemStack itemStack, InteractionHand hand) {
        if (itemStack.isEmpty())
            return super.swapItem(player, slot, itemStack, hand);

        var testingStack = FormFittingEnchantment.getFormFitted(this, itemStack, slot);
        if (testingStack.getItem() instanceof ExtendedItemProperties ext) {
            if (!ext.allowedInSlot(testingStack, this, slot))
                return false;
        } else { // Default expected entity shapes
            boolean shapeFits = switch (slot) {
                case HEAD -> getEntityShape().getHeadShape() == ClothingShape.Head.ANTHRO;
                case CHEST -> getEntityShape().getTorsoShape() == ClothingShape.Torso.ANTHRO;
                case LEGS -> getEntityShape().getLegsShape() == ClothingShape.Legs.BIPEDAL;
                case FEET -> getEntityShape().getFeetShape() == ClothingShape.Feet.BIPEDAL;
                default -> true;
            };

            if (!shapeFits)
                return false;
        }

        return super.swapItem(player, slot, itemStack, hand);
    }

    @Override
    protected void brokenByPlayer(@NotNull DamageSource damageSource) {
        ItemStack itemstack = this.getPickResult();
        if (this.hasCustomName()) {
            itemstack.setHoverName(this.getCustomName());
        }

        Block.popResource(this.level(), this.blockPosition(), itemstack);
        this.brokenByAnything(damageSource);
    }
}
