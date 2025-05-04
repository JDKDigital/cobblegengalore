package cy.jdkdigital.cobblegengalore.common.block.entity;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.Config;
import cy.jdkdigital.cobblegengalore.common.block.BlockGenBlock;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import cy.jdkdigital.cobblegengalore.util.RecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlockGenBlockEntity extends BlockEntity
{
    private RecipeHolder<BlockGenRecipe> recipe;
    private ResourceLocation recipeId;
    private ItemStack internalBuffer = ItemStack.EMPTY;

    public BlockGenBlockEntity(BlockPos pos, BlockState blockState) {
        super(CobbleGenGalore.BLOCKGEN_BLOCKENTITY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BlockGenBlockEntity blockEntity) {
        if (blockEntity.recipeId != null) {
            Optional<RecipeHolder<?>> loadedRecipe = level.getRecipeManager().byKey(blockEntity.recipeId);
            loadedRecipe.ifPresent(recipeHolder -> blockEntity.setRecipe((RecipeHolder<BlockGenRecipe>) recipeHolder));
            blockEntity.recipeId = null;
        }

        if (level.getGameTime() % Config.tickRate == 0) {
            if (blockEntity.recipe != null) {
                var recipe = blockEntity.recipe.value();
                var modifier = (blockEntity.getBlockState().getBlock() instanceof BlockGenBlock block ? block.modifier : 1) * recipe.speed;
                var production = recipe.result.copy();
                var maxProduction = (int) (production.getCount() * modifier);

                // Consume fluids
                var consumedAmountLeft = recipe.consumeLeft ? 0 : maxProduction;
                var consumedAmountRight = recipe.consumeRight ? 0 : maxProduction;

                if (recipe.consumeLeft || recipe.consumeRight) {
                    for (Direction dir : Direction.values()) {
                        if (dir.getAxis().isHorizontal()) {
                            var state = RecipeHelper.getStateAtPos(level, blockPos, dir);
                            if (recipe.consumeLeft && state.is(recipe.left.getBlock())) {
                                consumedAmountLeft = RecipeHelper.consumeFluid(level, blockPos, dir, maxProduction, true);
                                consumedAmountRight = recipe.consumeRight ? RecipeHelper.consumeFluid(level, blockPos, dir.getOpposite(), maxProduction, true) : consumedAmountLeft;
                                maxProduction = Math.min(consumedAmountLeft, consumedAmountRight);
                                RecipeHelper.consumeFluid(level, blockPos, dir, maxProduction, false);
                                if (recipe.consumeRight) {
                                    RecipeHelper.consumeFluid(level, blockPos, dir.getOpposite(), maxProduction, false);
                                }
                                break;
                            }
                            if (recipe.consumeRight && state.is(recipe.right.getBlock())) {
                                consumedAmountRight = RecipeHelper.consumeFluid(level, blockPos, dir, maxProduction, true);
                                consumedAmountLeft = recipe.consumeLeft ? RecipeHelper.consumeFluid(level, blockPos, dir.getOpposite(), maxProduction, true) : consumedAmountRight;
                                maxProduction = Math.min(consumedAmountLeft, consumedAmountRight);
                                RecipeHelper.consumeFluid(level, blockPos, dir, maxProduction, false);
                                if (recipe.consumeLeft) {
                                    RecipeHelper.consumeFluid(level, blockPos, dir.getOpposite(), maxProduction, false);
                                }
                                break;
                            }
                        }
                    }
                    maxProduction = Math.min(consumedAmountLeft, consumedAmountRight);
                }

                if (maxProduction > 0) {
                    production.setCount(maxProduction);
                    if (ItemStack.isSameItemSameComponents(blockEntity.internalBuffer, production)) {
                        if (blockEntity.internalBuffer.getCount() < blockEntity.internalBuffer.getMaxStackSize()) {
                            blockEntity.internalBuffer.grow(production.getCount());
                        }
                    } else {
                        blockEntity.internalBuffer = production;
                    }
                }
            }
            var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.above(), null);
            blockEntity.internalBuffer = ItemHandlerHelper.insertItem(cap, blockEntity.internalBuffer, false);
        }

        if (level.getGameTime() % 113 == 0 && blockEntity.recipe == null && level instanceof ServerLevel serverLevel) {
            blockEntity.setRecipe(RecipeHelper.getRecipe(serverLevel, blockPos));
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, BlockGenBlockEntity blockEntity) {
        if (blockEntity.recipeId != null) {
            Optional<RecipeHolder<?>> loadedRecipe = level.getRecipeManager().byKey(blockEntity.recipeId);
            loadedRecipe.ifPresent(recipeHolder -> blockEntity.setRecipe((RecipeHolder<BlockGenRecipe>) recipeHolder));
            blockEntity.recipeId = null;
        }
    }

    public BlockState getResultBlock() {
        return recipe != null && recipe.value().result.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
    }

    public boolean hasResult() {
        return recipe != null;
    }

    public ItemStack getResultItem() {
        return recipe != null ? recipe.value().result : null;
    }

    public void setRecipe(RecipeHolder<BlockGenRecipe> recipe) {
        this.recipe = recipe;
        this.setChanged();
        if (this.level instanceof ServerLevel) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("recipe")) {
            this.recipe = null;
            this.recipeId = ResourceLocation.tryParse(tag.getString("recipe"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.recipe != null) {
            tag.putString("recipe", this.recipe.id().toString());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithId(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
    }

    public ItemStack getBuffer() {
        return internalBuffer;
    }

    public void clearBuffer() {
        internalBuffer = ItemStack.EMPTY;
    }
}
