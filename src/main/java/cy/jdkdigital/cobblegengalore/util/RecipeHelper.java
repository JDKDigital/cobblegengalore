package cy.jdkdigital.cobblegengalore.util;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import cy.jdkdigital.cobblegengalore.event.RecipeMapCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class RecipeHelper
{
    @Nullable
    public static RecipeMap getRecipeMap(Level level) {
        return level instanceof ServerLevel serverLevel ? serverLevel.recipeAccess().recipeMap() : RecipeMapCache.getRecipeMap();
    }

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
        RecipeMap recipeMap = getRecipeMap(level);
        if (recipeMap == null) {
            return null;
        }
        RecipeHolder<BlockGenRecipe> matchingRecipe = null;
        for (RecipeHolder<BlockGenRecipe> recipeHolder : recipeMap.byType(CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.get())) {
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
        ResourceHandler<FluidResource> fluidCap = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(dir), dir.getOpposite());
        if (fluidCap != null && fluidCap.size() > 0) {
            state = fluidCap.getResource(0).getFluid().defaultFluidState().createLegacyBlock();
        } else {
            state = level.getBlockState(pos.relative(dir));
        }
        return state;
    }

    public static int consumeFluid(Level level, BlockPos pos, Direction dir, int count, boolean simulate) {
        ResourceHandler<FluidResource> fluidCap = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(dir), dir.getOpposite());
        if (fluidCap != null && fluidCap.size() > 0) {
            var removedAmount = (int) Math.min(count, Math.floor(fluidCap.getAmountAsInt(0) / 1000f));
            if (removedAmount > 0) {
                if (!simulate) {
                    try (Transaction tx = Transaction.openRoot()) {
                        fluidCap.extract(fluidCap.getResource(0), 1000 * removedAmount, tx);
                        tx.commit();
                    }
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
