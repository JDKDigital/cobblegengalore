package cy.jdkdigital.cobblegengalore.datagen.recipe;

import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockGenRecipeBuilder implements RecipeBuilder
{
    private final ItemStackTemplate result;
    private BlockState left = Blocks.AIR.defaultBlockState();
    private BlockState right = Blocks.AIR.defaultBlockState();
    private BlockState modifier = Blocks.AIR.defaultBlockState();
    private float speed = 1f;
    private boolean consumeLeft = false;
    private boolean consumeRight = false;

    private BlockGenRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
    }

    public static BlockGenRecipeBuilder of(ItemStackTemplate result) {
        return new BlockGenRecipeBuilder(result);
    }

    public BlockGenRecipeBuilder left(BlockState left) {
        this.left = left;
        return this;
    }

    public BlockGenRecipeBuilder right(BlockState right) {
        this.right = right;
        return this;
    }

    public BlockGenRecipeBuilder modifier(BlockState modifier) {
        this.modifier = modifier;
        return this;
    }

    public BlockGenRecipeBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    public BlockGenRecipeBuilder consumeLeft() {
        this.consumeLeft = true;
        return this;
    }

    public BlockGenRecipeBuilder consumeRight() {
        this.consumeRight = true;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("cobblegengalore", "blockgen/result"));
    }

    public void save(RecipeOutput recipeOutput, Identifier id) {
        recipeOutput.accept(ResourceKey.create(Registries.RECIPE, id), new BlockGenRecipe(result, left, right, modifier, speed, consumeLeft, consumeRight), null);
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        recipeOutput.accept(id, new BlockGenRecipe(result, left, right, modifier, speed, consumeLeft, consumeRight), null);
    }
}
