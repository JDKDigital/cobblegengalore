package cy.jdkdigital.cobblegengalore.util;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class RecipeHelper
{
    public static RecipeHolder<BlockGenRecipe> getRecipe(ServerLevel level, BlockPos genPos) {
        BlockState belowState = getStateAtPos(level, genPos, Direction.DOWN);
        BlockState northState = getStateAtPos(level, genPos, Direction.NORTH);
        BlockState southState = getStateAtPos(level, genPos, Direction.SOUTH);

        var recipe = getRecipe(level, northState, southState, belowState);
        if (recipe == null) {
            BlockState eastState = getStateAtPos(level, genPos, Direction.EAST);
            BlockState westState = getStateAtPos(level, genPos, Direction.WEST);
            recipe = getRecipe(level, eastState, westState, belowState);
        }

        return recipe;
    }

    @Nullable
    private static RecipeHolder<BlockGenRecipe> getRecipe(Level level, BlockState left, BlockState right, BlockState below) {
        var allRecipes = level.getRecipeManager().getAllRecipesFor(CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.get());
        RecipeHolder<BlockGenRecipe> matchingRecipe = null;
        for (RecipeHolder<BlockGenRecipe> recipeHolder : allRecipes) {
            if (recipeHolder.value().matches(left, right, below)) {
                if (matchingRecipe != null) {
                    matchingRecipe = matchingRecipe.value().modifier.isAir() ? recipeHolder : matchingRecipe;
                } else {
                    matchingRecipe = recipeHolder;
                }
            }
        }
        return matchingRecipe;
    }

    public static BlockState getStateAtPos(Level level, BlockPos pos, Direction dir) {
        BlockState state;
        var northCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
        if (northCap != null && northCap.getTanks() > 0) {
            state = northCap.getFluidInTank(0).getFluid().defaultFluidState().createLegacyBlock();
        } else {
            state = level.getBlockState(pos.relative(dir));
        }
        return state;
    }

    public static int consumeFluid(Level level, BlockPos pos, Direction dir, int count, boolean simulate) {
        var fluidCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
        if (fluidCap != null && fluidCap.getTanks() > 0) {
            var removedAmount = (int)Math.min(count, Math.floor(fluidCap.getFluidInTank(0).getAmount() / 1000f));
            if (removedAmount > 0) {
                if (!simulate) {
                    fluidCap.drain(1000 * removedAmount, IFluidHandler.FluidAction.EXECUTE);
                }
                return removedAmount;
            }
            return 0;
        }
        if (!simulate) {
            level.setBlockAndUpdate(pos.relative(dir), Blocks.AIR.defaultBlockState());
        }
        return 1;
    }
}
