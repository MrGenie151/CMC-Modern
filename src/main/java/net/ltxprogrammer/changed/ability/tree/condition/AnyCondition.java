package net.ltxprogrammer.changed.ability.tree.condition;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;

import java.util.List;

public class AnyCondition extends AbstractCondition {
    public final List<AbstractCondition> components;

    public AnyCondition(List<AbstractCondition> components) {
        this.components = components;
    }

    @Override
    public boolean test(IAbstractChangedEntity entity) {
        return components.stream().anyMatch(condition -> condition.test(entity));
    }
}
