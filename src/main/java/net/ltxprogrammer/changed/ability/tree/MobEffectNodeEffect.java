package net.ltxprogrammer.changed.ability.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class MobEffectNodeEffect extends AbilityTree.NodeEffect {
    public static final Codec<MobEffectInstance> MOB_EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(MobEffectInstance::getEffect),
            Codec.INT.fieldOf("duration").orElse(600).forGetter(MobEffectInstance::getDuration),
            Codec.INT.fieldOf("amplifier").orElse(0).forGetter(MobEffectInstance::getAmplifier),
            Codec.BOOL.fieldOf("hideParticles").orElse(false).forGetter(MobEffectInstance::isAmbient),
            Codec.BOOL.fieldOf("visible").orElse(true).forGetter(MobEffectInstance::isVisible),
            Codec.BOOL.fieldOf("showIcon").orElse(true).forGetter(MobEffectInstance::showIcon)
    ).apply(instance, MobEffectInstance::new));

    public static final Codec<MobEffectNodeEffect> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MOB_EFFECT_CODEC.fieldOf("mobEffect").forGetter(node -> node.mobEffect)
    ).apply(builder, MobEffectNodeEffect::new));

    public final MobEffectInstance mobEffect;

    public MobEffectNodeEffect(MobEffectInstance mobEffect) {
        this.mobEffect = mobEffect;
    }

    @Override
    public void applyEffect(AbilityCounter counter) {
        counter.variantInstance.getHost().addEffect(new MobEffectInstance(mobEffect));
    }
}
