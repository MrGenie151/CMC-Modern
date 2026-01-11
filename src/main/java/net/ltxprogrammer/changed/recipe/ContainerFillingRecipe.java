package net.ltxprogrammer.changed.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.ltxprogrammer.changed.init.ChangedRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ContainerFillingRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    final String group;
    final NonNullList<Ingredient> ingredients;
    final NonNullList<Ingredient> minimumIngredients;
    final Item container;
    final int containerCountLimit;
    final Item result;

    public ContainerFillingRecipe(ResourceLocation id, String group, NonNullList<Ingredient> ingredients, Item container, int containerCountLimit, Item result) {
        this.id = id;
        this.group = group;
        this.ingredients = ingredients;
        this.minimumIngredients = NonNullList.create();
        minimumIngredients.addAll(this.ingredients);
        minimumIngredients.add(Ingredient.of(container));

        this.container = container;
        this.containerCountLimit = containerCountLimit;
        this.result = result;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return minimumIngredients;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        int nonEmptyStacks = 0;
        int containerStacks = 0;

        for(int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack itemstack = container.getItem(j);
            if (!itemstack.isEmpty()) {
                if (!itemstack.is(this.container))
                    ++nonEmptyStacks;
                else {
                    ++containerStacks;
                    continue;
                }

                inputs.add(itemstack);
            }
        }

        return nonEmptyStacks == this.ingredients.size() &&
                containerStacks > 0 && containerStacks <= containerCountLimit &&
                net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs, this.ingredients) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess registryAccess) {
        int containerStacks = 0;

        for(int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack itemstack = container.getItem(j);
            if (!itemstack.isEmpty()) {
                if (itemstack.is(this.container))
                    ++containerStacks;
            }
        }

        return new ItemStack(this.result, Mth.clamp(containerStacks, 1, this.containerCountLimit));
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return new ItemStack(this.result);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size() + 1;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ChangedRecipeSerializers.CONTAINER_FILL_RECIPE.get();
    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    public static class Serializer implements RecipeSerializer<ContainerFillingRecipe> {
        public ContainerFillingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for container filling recipe");
            } else if (nonnulllist.size() > ShapedRecipe.MAX_WIDTH * ShapedRecipe.MAX_HEIGHT - 1) {
                throw new JsonParseException("Too many ingredients for container filling recipe. The maximum is " + (ShapedRecipe.MAX_WIDTH * ShapedRecipe.MAX_HEIGHT - 1));
            } else {
                Item container = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(json.get("container").getAsString()));
                if (nonnulllist.stream().anyMatch(ingredient -> {
                    assert container != null;
                    return ingredient.test(new ItemStack(container));
                }))
                    throw new JsonParseException("Cannot set container to ingredient item");
                int countLimit = GsonHelper.getAsInt(json, "containerCountLimit", Integer.MAX_VALUE);
                Item out = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(json.get("result").getAsString()));
                return new ContainerFillingRecipe(id, group, nonnulllist, container, countLimit, out);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray p_44276_) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < p_44276_.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(p_44276_.get(i));
                if (!ingredient.isEmpty()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        public ContainerFillingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();

            int i = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            for (int j = 0; j < ingredients.size(); ++j) {
                ingredients.set(j, Ingredient.fromNetwork(buffer));
            }

            Item container = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(buffer.readUtf()));
            int countLimit = buffer.readVarInt();
            Item out = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(buffer.readUtf()));
            return new ContainerFillingRecipe(id, group, ingredients, container, countLimit, out);
        }

        public void toNetwork(FriendlyByteBuf buffer, ContainerFillingRecipe recipe) {
            buffer.writeUtf(recipe.group);

            buffer.writeVarInt(recipe.ingredients.size());

            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeUtf(ForgeRegistries.ITEMS.getKey(recipe.container).toString());
            buffer.writeVarInt(recipe.containerCountLimit);
            buffer.writeUtf(ForgeRegistries.ITEMS.getKey(recipe.result).toString());
        }
    }
}
