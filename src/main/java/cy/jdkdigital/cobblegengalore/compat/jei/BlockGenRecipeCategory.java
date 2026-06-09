package cy.jdkdigital.cobblegengalore.compat.jei;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class BlockGenRecipeCategory extends AbstractRecipeCategory<RecipeHolder<BlockGenRecipe>>
{
    private final IDrawable background;

    public BlockGenRecipeCategory(IGuiHelper guiHelper) {
        super(
                CobbleGenGaloreJeiPlugin.BLOCK_GEN_TYPE,
                Component.translatable("jei." + CobbleGenGalore.MODID + ".blockgen"),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CobbleGenGalore.BLOCKGEN_IRON_BLOCK.get())),
                126, 70
        );
        this.background = guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath(CobbleGenGalore.MODID, "textures/gui/jei/block_generator.png"), 0, 0, 126, 70);
    }

    @Override
    public void draw(RecipeHolder<BlockGenRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BlockGenRecipe> recipe, IFocusGroup focuses) {
        addBlockStateInput(builder, recipe.value().left, 10, 27).addRichTooltipCallback((iRecipeSlotView, list) -> {
            if (recipe.value().consumeLeft) {
                list.add(Component.translatable("jei.cobblegengalore.consumed").withStyle(ChatFormatting.RED));
            }
        });
        addBlockStateInput(builder, recipe.value().right, 101, 27).addRichTooltipCallback((iRecipeSlotView, list) -> {
            if (recipe.value().consumeRight) {
                list.add(Component.translatable("jei.cobblegengalore.consumed").withStyle(ChatFormatting.RED));
            }
        });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 27).addIngredient(VanillaTypes.ITEM_STACK, recipe.value().result);
        builder.addSlot(RecipeIngredientRole.CATALYST, 55, 27).addItemStacks(BuiltInRegistries.ITEM.getOrCreateTag(CobbleGenGalore.BLOCK_GENERATORS).stream().map(itemHolder -> new ItemStack(itemHolder.value())).toList());

        if (!recipe.value().modifier.isAir()) {
            addBlockStateInput(builder, recipe.value().modifier, 55, 46);
        }
    }

    private static IRecipeSlotBuilder addBlockStateInput(IRecipeLayoutBuilder builder, BlockState state, int x, int y) {
        if (!state.getFluidState().isEmpty()) {
            return builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(state.getFluidState().getType(), 1000));
        } else {
            return builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(state.getBlock().asItem()));
        }
    }
}
