package net.ltxprogrammer.changed.entity.beast;

import net.ltxprogrammer.changed.entity.Gender;
import net.ltxprogrammer.changed.entity.TransfurCause;
import net.ltxprogrammer.changed.entity.TransfurMode;
import net.ltxprogrammer.changed.util.Color3;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class LatexRabbitFemale extends AbstractLatexRabbit {
    public LatexRabbitFemale(EntityType<? extends LatexRabbitFemale> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    public Color3 getTransfurColor(TransfurCause cause) {
        return Color3.getColor("#fef0e5");
    }

    @Override
    public TransfurMode getTransfurMode() {
        return TransfurMode.ABSORPTION;
    }

    @Override
    public Gender getGender() {
        return Gender.FEMALE;
    }
}