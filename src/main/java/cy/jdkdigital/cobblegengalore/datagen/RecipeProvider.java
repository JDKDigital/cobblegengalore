package cy.jdkdigital.cobblegengalore.datagen;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.datagen.recipe.BlockGenRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider
{
    public RecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        RecipeOutput recipeOutput = this.output;

        crafting(recipeOutput, CobbleGenGalore.BLOCKGEN_STONE_BLOCK.get(), Items.SMOOTH_STONE);
        craftingTag(recipeOutput, CobbleGenGalore.BLOCKGEN_COPPER_BLOCK.get(), "c:ingots/copper");
        craftingTag(recipeOutput, CobbleGenGalore.BLOCKGEN_IRON_BLOCK.get(), "c:ingots/iron");
        craftingTag(recipeOutput, CobbleGenGalore.BLOCKGEN_GOLD_BLOCK.get(), "c:ingots/gold");
        craftingTag(recipeOutput, CobbleGenGalore.BLOCKGEN_EMERALD_BLOCK.get(), "c:gems/emerald");
        craftingTag(recipeOutput, CobbleGenGalore.BLOCKGEN_DIAMOND_BLOCK.get(), "c:gems/diamond");
        craftingTag(recipeOutput, CobbleGenGalore.BLOCKGEN_NETHERITE_BLOCK.get(), "c:ingots/netherite");

        blockgen(recipeOutput, "cobblestone", Items.COBBLESTONE, b -> b.left(water()).right(lava()));
        blockgen(recipeOutput, "stone", Items.STONE, b -> b.left(water()).right(lava()).modifier(state(Blocks.STONE)).consumeLeft());
        blockgen(recipeOutput, "andesite", Items.ANDESITE, b -> b.left(water()).right(lava()).modifier(state(Blocks.ANDESITE)));
        blockgen(recipeOutput, "basalt", Items.BASALT, b -> b.left(state(Blocks.BLUE_ICE)).right(lava()));
        blockgen(recipeOutput, "cobbled_deepslate", Items.COBBLED_DEEPSLATE, b -> b.left(state(Blocks.PACKED_ICE)).right(lava()));
        blockgen(recipeOutput, "diorite", Items.DIORITE, b -> b.left(water()).right(lava()).modifier(state(Blocks.DIORITE)));
        blockgen(recipeOutput, "granite", Items.GRANITE, b -> b.left(water()).right(lava()).modifier(state(Blocks.GRANITE)));
        blockgen(recipeOutput, "netherrack", Items.NETHERRACK, b -> b.left(lava()).right(lava()).modifier(state(Blocks.NETHERRACK)));
        blockgen(recipeOutput, "obsidian", Items.OBSIDIAN, b -> b.left(water()).right(lava()).modifier(state(Blocks.OBSIDIAN)).consumeRight());
        blockgen(recipeOutput, "tuff", Items.TUFF, b -> b.left(water()).right(lava()).modifier(state(Blocks.TUFF)));
        blockgen(recipeOutput, "calcite", Items.CALCITE, b -> b.left(water()).right(lava()).modifier(state(Blocks.CALCITE)));
        blockgen(recipeOutput, "blackstone", Items.BLACKSTONE, b -> b.left(lava()).right(lava()).modifier(state(Blocks.BLACKSTONE)));
        blockgen(recipeOutput, "end_stone", Items.END_STONE, b -> b.left(water()).right(lava()).modifier(state(Blocks.END_STONE)));
        blockgen(recipeOutput, "mud", Items.MUD, b -> b.left(water()).right(state(Blocks.DIRT)));
    }

    private void crafting(RecipeOutput recipeOutput, Block result, Item ring) {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, result)
                .pattern("RRR").pattern("G G").pattern("RRR")
                .define('G', Items.GLASS)
                .define('R', ring)
                .unlockedBy(getHasName(ring), has(ring))
                .save(recipeOutput, CobbleGenGalore.MODID + ":crafting/" + name(result));
    }

    private void craftingTag(RecipeOutput recipeOutput, Block result, String tag) {
        TagKey<Item> ring = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(tag));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, result)
                .pattern("RRR").pattern("G G").pattern("RRR")
                .define('G', Items.GLASS)
                .define('R', ring)
                .unlockedBy("has_ring", has(ring))
                .save(recipeOutput, CobbleGenGalore.MODID + ":crafting/" + name(result));
    }

    private static void blockgen(RecipeOutput recipeOutput, String name, ItemLike result, Function<BlockGenRecipeBuilder, BlockGenRecipeBuilder> config) {
        BlockGenRecipeBuilder builder = BlockGenRecipeBuilder.of(new ItemStackTemplate(result.asItem()));
        config.apply(builder).save(recipeOutput, Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, "blockgen/" + name));
    }

    private static net.minecraft.world.level.block.state.BlockState state(Block block) {
        return block.defaultBlockState();
    }

    private static net.minecraft.world.level.block.state.BlockState water() {
        return Blocks.WATER.defaultBlockState();
    }

    private static net.minecraft.world.level.block.state.BlockState lava() {
        return Blocks.LAVA.defaultBlockState();
    }

    private static String name(Block block) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    public static class Runner extends net.minecraft.data.recipes.RecipeProvider.Runner
    {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Cobblegen Galore Recipes";
        }
    }
}
