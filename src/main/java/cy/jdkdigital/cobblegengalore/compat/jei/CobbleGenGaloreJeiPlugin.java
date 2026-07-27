package cy.jdkdigital.cobblegengalore.compat.jei;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import cy.jdkdigital.cobblegengalore.event.RecipeMapCache;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@JeiPlugin
public class CobbleGenGaloreJeiPlugin implements IModPlugin
{
    private static final Identifier pluginId = Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, CobbleGenGalore.MODID);

    public static final RecipeType<RecipeHolder<BlockGenRecipe>> BLOCK_GEN_TYPE = RecipeType.createRecipeHolderType(CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.getId());

    public CobbleGenGaloreJeiPlugin() {}

    @Nonnull
    @Override
    public Identifier getPluginUid() {
        return pluginId;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_STONE_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_COPPER_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_IRON_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_GOLD_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_EMERALD_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_DIAMOND_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
        registration.addRecipeCatalyst(CobbleGenGalore.BLOCKGEN_NETHERITE_BLOCK.get().asItem().getDefaultInstance(), BLOCK_GEN_TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new BlockGenRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(BLOCK_GEN_TYPE, new ArrayList<>(RecipeMapCache.getRecipeMap().byType(CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.get())));
    }
}
