package net.ltxprogrammer.changed.entity.ai;

import net.minecraft.util.StringRepresentable;

public enum DarkLatexFavor implements StringRepresentable {
    NONE("none"),
    FISHING("fishing"),
    CAVING("caving"),
    SUIT_OWNER("suit_owner");

    private final String serializedName;

    DarkLatexFavor(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
