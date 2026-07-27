package cy.jdkdigital.cobblegengalore.common.block.entity;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.Config;
import cy.jdkdigital.cobblegengalore.common.block.BlockGenBlock;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import cy.jdkdigital.cobblegengalore.util.RecipeHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class BlockGenBlockEntity extends BlockEntity
{
    private RecipeHolder<BlockGenRecipe> recipe;
    private Identifier recipeId;
    private ItemStack internalBuffer = ItemStack.EMPTY;

    public BlockGenBlockEntity(BlockPos pos, BlockState blockState) {
        super(CobbleGenGalore.BLOCKGEN_BLOCKENTITY.get(), pos, blockState);
    }

    @SuppressWarnings("unchecked")
    private void resolveRecipeId(Level level) {
        if (this.recipeId != null) {
            RecipeMap recipeMap = RecipeHelper.getRecipeMap(level);
            if (recipeMap != null) {
                RecipeHolder<?> loadedRecipe = recipeMap.byKey(ResourceKey.create(Registries.RECIPE, this.recipeId));
                if (loadedRecipe != null) {
                    this.setRecipe((RecipeHolder<BlockGenRecipe>) loadedRecipe);
                }
            }
            this.recipeId = null;
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BlockGenBlockEntity blockEntity) {
        blockEntity.resolveRecipeId(level);

        if (level.getGameTime() % Config.tickRate == 0) {
            if (blockEntity.recipe != null && (blockEntity.internalBuffer.isEmpty() || blockEntity.internalBuffer.getCount() < blockEntity.internalBuffer.getMaxStackSize())) {
                var recipe = blockEntity.recipe.value();
                var modifier = (blockEntity.getBlockState().getBlock() instanceof BlockGenBlock block ? block.modifier : 1) * recipe.speed;
                var production = recipe.result.create();
                var maxProduction = Math.min(production.getMaxStackSize(), (int) (production.getCount() * modifier));

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
                    // Don't go over stack limit
                    blockEntity.internalBuffer.setCount(Math.min(blockEntity.internalBuffer.getMaxStackSize(), blockEntity.internalBuffer.getCount()));
                }
            }
            ResourceHandler<ItemResource> cap = level.getCapability(Capabilities.Item.BLOCK, blockPos.above(), null);
            if (cap != null && !blockEntity.internalBuffer.isEmpty()) {
                int inserted = ResourceHandlerUtil.insertStacking(cap, ItemResource.of(blockEntity.internalBuffer), blockEntity.internalBuffer.getCount(), null);
                if (inserted > 0) {
                    blockEntity.internalBuffer.shrink(inserted);
                    if (blockEntity.internalBuffer.isEmpty()) {
                        blockEntity.internalBuffer = ItemStack.EMPTY;
                    }
                }
            }
        }

        if (level.getGameTime() % 113 == 0 && blockEntity.recipe == null && level instanceof ServerLevel serverLevel) {
            blockEntity.setRecipe(RecipeHelper.getRecipe(serverLevel, blockPos));
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, BlockGenBlockEntity blockEntity) {
        blockEntity.resolveRecipeId(level);
    }

    public BlockState getResultBlock() {
        return recipe != null && recipe.value().result.item().value() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
    }

    public boolean hasResult() {
        return recipe != null;
    }

    public ItemStack getResultItem() {
        return recipe != null ? recipe.value().result.create() : null;
    }

    public void setRecipe(RecipeHolder<BlockGenRecipe> recipe) {
        this.recipe = recipe;
        this.setChanged();
        if (this.level instanceof ServerLevel) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.loadPacketNBT(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.savePacketNBT(output);
    }

    private void loadPacketNBT(ValueInput input) {
        String recipeKey = input.getStringOr("recipe", "");
        if (!recipeKey.isEmpty()) {
            this.recipe = null;
            this.recipeId = Identifier.tryParse(recipeKey);
        }
        this.internalBuffer = input.read("buffer", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    private void savePacketNBT(ValueOutput output) {
        if (this.recipe != null) {
            output.putString("recipe", this.recipe.id().identifier().toString());
        }
        if (!this.internalBuffer.isEmpty()) {
            output.store("buffer", ItemStack.CODEC, this.internalBuffer);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        super.onDataPacket(net, input);
        this.loadPacketNBT(input);
        if (this.level instanceof ClientLevel) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public ItemStack getBuffer() {
        return internalBuffer;
    }

    public void clearBuffer() {
        internalBuffer = ItemStack.EMPTY;
    }
}
