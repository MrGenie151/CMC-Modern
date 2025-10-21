package net.ltxprogrammer.changed.ability.tree.condition;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;

import java.util.List;

public class TrueCondition extends AbstractCondition {
    public static final TrueCondition INSTANCE = new TrueCondition();

    private TrueCondition() {}

    @Override
    public boolean test(IAbstractChangedEntity entity) {
        return true;
    }
}
