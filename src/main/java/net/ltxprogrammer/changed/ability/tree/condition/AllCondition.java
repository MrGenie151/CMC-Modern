package net.ltxprogrammer.changed.ability.tree.condition;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;

import java.util.List;

public class AllCondition extends AbstractCondition {
    public final List<AbstractCondition> components;

    public AllCondition(List<AbstractCondition> components) {
        this.components = components;
    }

    @Override
    public boolean test(IAbstractChangedEntity entity) {
        return components.stream().allMatch(condition -> condition.test(entity));
    }
}
