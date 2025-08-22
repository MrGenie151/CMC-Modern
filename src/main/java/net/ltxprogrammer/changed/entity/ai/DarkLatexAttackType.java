package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.beast.AbstractDarkLatexEntity;
import net.ltxprogrammer.changed.init.ChangedTags;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public enum DarkLatexAttackType implements BiPredicate<AbstractDarkLatexEntity, LivingEntity>, StringRepresentable {
    ALWAYS_KILL("always_kill", (self, target) -> false),
    TRY_TRANSFUR("try_transfur", (self, target) -> {
        return target.getType().is(ChangedTags.EntityTypes.HUMANOIDS) || target instanceof ChangedEntity;
    });

    private final String serializedName;
    private final BiPredicate<AbstractDarkLatexEntity, LivingEntity> predicate;

    DarkLatexAttackType(String serializedName, BiPredicate<AbstractDarkLatexEntity, LivingEntity> predicate) {
        this.serializedName = serializedName;
        this.predicate = predicate;
    }

    @Override
    public boolean test(AbstractDarkLatexEntity self, LivingEntity possibleTarget) {
        return predicate.test(self, possibleTarget);
    }

    public Predicate<LivingEntity> forEntity(AbstractDarkLatexEntity self) {
        return target -> this.test(self, target);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public DarkLatexAttackType cycle() {
        if (this.ordinal() + 1 == values().length)
            return values()[0];
        else
            return values()[this.ordinal() + 1];
    }

    public Component getDisplayText() {
        return Component.translatable("changed.tamed_dark_latex.attacking." + serializedName);
    }
}
