package net.ltxprogrammer.changed.world.inventory;

import net.ltxprogrammer.changed.entity.beast.AbstractDarkLatexEntity;
import net.ltxprogrammer.changed.init.ChangedMenus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

public class TamedDarkLatexMenu extends AbstractContainerMenu implements UpdateableMenu {
    public AbstractDarkLatexEntity tamedDarkLatex;
    public final Player player;

    public TamedDarkLatexMenu(int id, Inventory inventory, AbstractDarkLatexEntity tamedDarkLatex) {
        super(ChangedMenus.TAMED_DARK_LATEX.get(), id);
        this.tamedDarkLatex = tamedDarkLatex;
        this.player = inventory.player;
    }

    public TamedDarkLatexMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ChangedMenus.TAMED_DARK_LATEX.get(), id);
        this.player = inv.player;

        if (extraData == null)
            return;

        this.tamedDarkLatex = (AbstractDarkLatexEntity) inv.player.level().getEntity(extraData.readInt());
    }

    @Override
    public ItemStack quickMoveStack(Player viewer, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player viewer) {
        if (this.tamedDarkLatex.isRemoved()) {
            return false;
        } else if (this.tamedDarkLatex.getOwner() != viewer) {
            return false;
        } else {
            return !(viewer.distanceToSqr(this.tamedDarkLatex) > 64.0D);
        }
    }

    @Override
    public int getId() {
        return containerId;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void update(CompoundTag payload, LogicalSide receiver, @Nullable ServerPlayer origin) {
        if (receiver == LogicalSide.SERVER && origin == this.tamedDarkLatex.getOwner()) {
            switch (payload.getString("command")) {
                case "cycle_target_type" -> this.tamedDarkLatex.setTargetType(this.tamedDarkLatex.getTargetType().cycle());
                case "cycle_attack_type" -> this.tamedDarkLatex.setAttackType(this.tamedDarkLatex.getAttackType().cycle());
                case "cycle_attack_condition" -> this.tamedDarkLatex.setAttackCondition(this.tamedDarkLatex.getAttackCondition().cycle());
            }
        }
    }
}
