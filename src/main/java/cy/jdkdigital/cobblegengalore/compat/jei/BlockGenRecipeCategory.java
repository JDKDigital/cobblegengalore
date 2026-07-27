package cy.jdkdigital.cobblegengalore.compat.jei;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

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
        this.background = guiHelper.drawableBuilder(Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, "textures/gui/jei/block_generator.png"), 0, 0, 126, 70).setTextureSize(256, 256).build();
    }

    @Override
    public void draw(RecipeHolder<BlockGenRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BlockGenRecipe> recipe, IFocusGroup focuses) {
        addBlockStateInput(builder, recipe.value().left, 10, 27).addRichTooltipCallback((iRecipeSlotView, tooltip) -> {
            if (recipe.value().consumeLeft) {
                tooltip.add(Component.translatable("jei.cobblegengalore.consumed").withStyle(ChatFormatting.RED));
            }
        });
        addBlockStateInput(builder, recipe.value().right, 101, 27).addRichTooltipCallback((iRecipeSlotView, tooltip) -> {
            if (recipe.value().consumeRight) {
                tooltip.add(Component.translatable("jei.cobblegengalore.consumed").withStyle(ChatFormatting.RED));
            }
        });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 27).addIngredient(VanillaTypes.ITEM_STACK, recipe.value().result.create());

        List<ItemStack> generators = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(CobbleGenGalore.BLOCK_GENERATORS).forEach(itemHolder -> generators.add(new ItemStack(itemHolder.value())));
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 55, 27).addIngredients(VanillaTypes.ITEM_STACK, generators);

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
