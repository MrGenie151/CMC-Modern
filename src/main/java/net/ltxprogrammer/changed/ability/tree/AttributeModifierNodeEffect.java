package net.ltxprogrammer.changed.ability.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeModifierNodeEffect extends AbilityTree.NodeEffect {
    public static final Codec<AttributeModifierNodeEffect> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(node -> node.attribute),
            Codec.DOUBLE.fieldOf("factor").forGetter(node -> node.factor)
    ).apply(builder, AttributeModifierNodeEffect::new));

    public final Attribute attribute;
    public final double factor;

    public AttributeModifierNodeEffect(Attribute attribute, double factor) {
        this.attribute = attribute;
        this.factor = factor;
    }

    @Override
    public void applyEffect(AbilityCounter counter) {
        counter.addAttributeMultiplier(attribute, factor);
    }
}
