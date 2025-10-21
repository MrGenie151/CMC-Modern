package net.ltxprogrammer.changed.ability.tree.condition;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;

import java.util.List;

public class NoneCondition extends AbstractCondition {
    public final List<AbstractCondition> components;

    public NoneCondition(List<AbstractCondition> components) {
        this.components = components;
    }

    @Override
    public boolean test(IAbstractChangedEntity entity) {
        return components.stream().noneMatch(condition -> condition.test(entity));
    }
}
