package net.ltxprogrammer.changed.world.features.structures;

import com.mojang.serialization.Codec;
import net.ltxprogrammer.changed.init.ChangedFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Intended to locally fix MC-102223
 * Placement processors may opt in to this fix with {@link #INSTANCE HangingBlockFixerProcessor.INSTANCE}.
 * Implementation in {@link net.ltxprogrammer.changed.mixin.StructureTemplateMixin}
 */
public class HangingBlockFixerProcessor extends StructureProcessor {
    public static final Codec<HangingBlockFixerProcessor> CODEC = Codec.unit(() -> {
        return HangingBlockFixerProcessor.INSTANCE;
    });
    public static final HangingBlockFixerProcessor INSTANCE = new HangingBlockFixerProcessor();

    private HangingBlockFixerProcessor() {
    }

    protected StructureProcessorType<?> getType() {
        return ChangedFeatures.HANGING_BLOCK_FIXER_PROCESSOR.get();
    }
}
