package net.ltxprogrammer.changed.entity.ai;

import net.minecraft.util.StringRepresentable;

public enum DarkLatexAttackCondition implements StringRepresentable {
    NEVER("never"),
    ALWAYS("always"),
    OWNER_IS_HOSTILE("owner_is_hostile");

    private final String serializedName;

    DarkLatexAttackCondition(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public DarkLatexAttackCondition cycle() {
        if (this.ordinal() + 1 == values().length)
            return values()[0];
        else
            return values()[this.ordinal() + 1];
    }
}
