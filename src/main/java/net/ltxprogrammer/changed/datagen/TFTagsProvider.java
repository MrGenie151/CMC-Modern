package net.ltxprogrammer.changed.datagen;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class TFTagsProvider extends TagsProvider<TransfurVariant<?>> {

    public TFTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> pLookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, ChangedRegistry.TRANSFUR_VARIANT.get().getRegistryKey(), pLookupProvider, Changed.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
    }

    private static <T extends ChangedEntity> ResourceKey<TransfurVariant<?>> cast(RegistryObject<TransfurVariant<T>> key){
        return (ResourceKey<TransfurVariant<?>>) (Object) key.getKey();
    }

    protected TagAppender<TransfurVariant<?>> addAllMatching(TagAppender<TransfurVariant<?>> tag, Predicate<TransfurVariant<?>> predicate) {
        for (Map.Entry<ResourceKey<TransfurVariant<?>>, TransfurVariant<?>> entry : ChangedRegistry.TRANSFUR_VARIANT.get().getEntries()) {
            if (predicate.test(entry.getValue())) tag.add(entry.getKey());
        }

        return tag;
    }

    @Override
    public @NotNull String getName() {
        return "Transfur Type Tags";
    }
}
