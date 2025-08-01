package net.ltxprogrammer.changed.client.renderer.model;

import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.minecraft.client.model.geom.ModelPart;

public interface TripleHeadedModel<T extends ChangedEntity> extends DoubleHeadedModel<T> {
    ModelPart getCenterHead();
}
