package net.ltxprogrammer.changed.entity.decoration;

import net.ltxprogrammer.changed.entity.variant.EntityShape;
import net.ltxprogrammer.changed.init.ChangedEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LeglessArmorStand extends AbstractArmorStand {
    public LeglessArmorStand(EntityType<? extends LeglessArmorStand> entityType, Level level) {
        super(entityType, level);
    }

    public LeglessArmorStand(Level level, double x, double y, double z) {
        this(ChangedEntities.LEGLESS_ARMOR_STAND.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public ItemStack getPickResult() {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull EntityShape getEntityShape() {
        return EntityShape.MER;
    }
}
