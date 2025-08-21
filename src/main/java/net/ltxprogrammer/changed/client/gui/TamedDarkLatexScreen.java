package net.ltxprogrammer.changed.client.gui;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.util.Color3;
import net.ltxprogrammer.changed.util.SingleRunnable;
import net.ltxprogrammer.changed.world.inventory.TamedDarkLatexMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TamedDarkLatexScreen extends AbstractRadialScreen<TamedDarkLatexMenu> {
    private static final String PATH_GOO = "textures/gui/radial/goo/";
    private static final String PATH_GOO_SELECTED = "textures/gui/radial/goo_selected/";

    public TamedDarkLatexScreen(TamedDarkLatexMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text, Color3.DARK, Color3.WHITE, menu.tamedDarkLatex);
    }

    protected ResourceLocation getTextureForSection(int section, boolean thisHovered, boolean anyHovered) {
        return Changed.modResource((thisHovered ? PATH_GOO_SELECTED : PATH_GOO) + section + ".png");
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public @Nullable List<Component> tooltipsFor(int section) {
        return switch (section) {
            case 0 -> List.of(Component.literal("View Inventory"));
            case 1 -> List.of(Component.literal("Combat: Target Type"), Component.literal(menu.tamedDarkLatex.getTargetType().getSerializedName()));
            case 2 -> List.of(Component.literal("Combat: Attack Type"), Component.literal(menu.tamedDarkLatex.getAttackType().getSerializedName()));
            case 3 -> List.of(Component.literal("Combat: Attack Condition"), Component.literal(menu.tamedDarkLatex.getAttackCondition().getSerializedName()));
            case 4 -> List.of(Component.literal("Favor: Fishing"));
            case 5 -> List.of(Component.literal("Favor: Help Caving"));
            case 6 -> List.of(Component.literal("Favor: Suit Owner"));
            default -> List.of();
        };
    }

    @Override
    public void renderSectionBackground(GuiGraphics graphics, int section, double x, double y, float partialTicks, int mouseX, int mouseY, float red, float green, float blue) {
        var hovered = getSectionAt(mouseX, mouseY);
        boolean anyHovered = hovered.isPresent();
        boolean thisHovered = anyHovered && hovered.get() == section;
        graphics.setColor(red, green, blue, 1);
        graphics.blit(getTextureForSection(section, thisHovered, anyHovered),
                (int)x - 32 + this.leftPos, (int)y - 32 + this.topPos, 0, 0, 64, 64, 64, 64);
    }

    @Override
    public void renderSectionForeground(GuiGraphics graphics, int section, double x, double y, float partialTicks, int mouseX, int mouseY, float red, float green, float blue, float alpha) {
        // TODO icon or button
    }

    @Override
    public boolean handleClicked(int section, SingleRunnable close) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        switch (section) {
            case 0 -> {
                var tag = new CompoundTag();
                tag.putString("command", "view_inventory");
                menu.setDirty(tag);
            }
            case 1 -> {
                var tag = new CompoundTag();
                tag.putString("command", "cycle_target_type");
                menu.setDirty(tag);
            }
            case 2 -> {
                var tag = new CompoundTag();
                tag.putString("command", "cycle_attack_type");
                menu.setDirty(tag);
            }
            case 3 -> {
                var tag = new CompoundTag();
                tag.putString("command", "cycle_attack_condition");
                menu.setDirty(tag);
            }
            case 4 -> {
                var tag = new CompoundTag();
                tag.putString("command", "favor_fishing");
                menu.setDirty(tag);
            }
            case 5 -> {
                var tag = new CompoundTag();
                tag.putString("command", "favor_help_caving");
                menu.setDirty(tag);
            }
            case 6 -> {
                var tag = new CompoundTag();
                tag.putString("command", "favor_suit_owner");
                menu.setDirty(tag);
            }
        };
        return false;
    }
}
