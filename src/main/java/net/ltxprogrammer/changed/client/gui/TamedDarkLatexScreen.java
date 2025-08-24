package net.ltxprogrammer.changed.client.gui;

import com.google.common.collect.ImmutableList;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.entity.ai.DarkLatexFavor;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TamedDarkLatexScreen extends AbstractRadialScreen<TamedDarkLatexMenu> {
    private static final String PATH_GOO = "textures/gui/radial/goo/";
    private static final String PATH_GOO_SELECTED = "textures/gui/radial/goo_selected/";

    private static final Component ACTIVE = Component.literal("Active");
    private static final Component INACTIVE = Component.literal("Inactive");

    public record Interaction(String command, Supplier<List<Component>> tooltips, Supplier<Boolean> shouldHighlight) {}

    protected final ImmutableList<Interaction> availableInteractions;

    public TamedDarkLatexScreen(TamedDarkLatexMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text, Color3.DARK, Color3.WHITE, menu.tamedDarkLatex);
        var interactionsBuilder = ImmutableList.<Interaction>builder();
        interactionsBuilder.add(new Interaction("view_inventory",
                () -> List.of(Component.literal("View Inventory")),
                () -> false
        ));
        interactionsBuilder.add(new Interaction("cycle_follow",
                () -> List.of(Component.literal("Follow Mode"),
                        Component.translatable(menu.tamedDarkLatex.isFollowingOwner() ? "changed.tamed_dark_latex.follow" : "changed.tamed_dark_latex.wander")),
                () -> false
        ));
        interactionsBuilder.add(new Interaction("cycle_target_type",
                () -> List.of(Component.literal("Combat: Target Type"),
                        menu.tamedDarkLatex.getTargetType().getDisplayText()),
                () -> false
        ));
        interactionsBuilder.add(new Interaction("cycle_attack_type",
                () -> List.of(Component.literal("Combat: Attack Type"),
                        menu.tamedDarkLatex.getAttackType().getDisplayText()),
                () -> false
        ));
        interactionsBuilder.add(new Interaction("cycle_attack_condition",
                () -> List.of(Component.literal("Combat: Attack Condition"),
                        menu.tamedDarkLatex.getAttackCondition().getDisplayText()),
                () -> false
        ));
        if (menu.tamedDarkLatex.canDoFavor(DarkLatexFavor.FISHING))
            interactionsBuilder.add(new Interaction("favor_fishing",
                    () -> List.of(Component.literal("Favor: Fishing"),
                            menu.tamedDarkLatex.getCurrentFavor() == DarkLatexFavor.FISHING ? ACTIVE : INACTIVE),
                    () -> menu.tamedDarkLatex.getCurrentFavor() == DarkLatexFavor.FISHING
            ));
        if (menu.tamedDarkLatex.canDoFavor(DarkLatexFavor.CAVING))
            interactionsBuilder.add(new Interaction("favor_caving",
                    () -> List.of(Component.literal("Favor: Help Caving"),
                            menu.tamedDarkLatex.getCurrentFavor() == DarkLatexFavor.CAVING ? ACTIVE : INACTIVE),
                    () -> menu.tamedDarkLatex.getCurrentFavor() == DarkLatexFavor.CAVING
            ));
        if (menu.tamedDarkLatex.canDoFavor(DarkLatexFavor.SUIT_OWNER))
            interactionsBuilder.add(new Interaction("favor_suit_owner",
                    () -> List.of(Component.literal("Favor: Suit Owner"),
                            menu.tamedDarkLatex.getCurrentFavor() == DarkLatexFavor.SUIT_OWNER ? ACTIVE : INACTIVE),
                    () -> menu.tamedDarkLatex.getCurrentFavor() == DarkLatexFavor.SUIT_OWNER
            ));
        availableInteractions = interactionsBuilder.build();
    }

    protected ResourceLocation getTextureForSection(int section, boolean thisHovered, boolean anyHovered, boolean active) {
        return Changed.modResource(((thisHovered || (!anyHovered && active)) ? PATH_GOO_SELECTED : PATH_GOO) + section + ".png");
    }

    @Override
    public int getCount() {
        return availableInteractions.size();
    }

    @Override
    public @Nullable List<Component> tooltipsFor(int section) {
        return availableInteractions.get(section).tooltips.get();
    }

    @Override
    public void renderSectionBackground(GuiGraphics graphics, int section, double x, double y, float partialTicks, int mouseX, int mouseY, float red, float green, float blue) {
        var hovered = getSectionAt(mouseX, mouseY);
        boolean anyHovered = hovered.isPresent();
        boolean thisHovered = anyHovered && hovered.get() == section;
        graphics.setColor(red, green, blue, 1);
        graphics.blit(getTextureForSection(section, thisHovered, anyHovered, availableInteractions.get(section).shouldHighlight.get()),
                (int)x - 32 + this.leftPos, (int)y - 32 + this.topPos, 0, 0, 64, 64, 64, 64);
    }

    @Override
    public void renderSectionForeground(GuiGraphics graphics, int section, double x, double y, float partialTicks, int mouseX, int mouseY, float red, float green, float blue, float alpha) {
        // TODO icon or button
    }

    @Override
    public boolean handleClicked(int section, SingleRunnable close) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        var tag = new CompoundTag();
        tag.putString("command", availableInteractions.get(section).command);
        menu.setDirty(tag);

        return false;
    }
}
