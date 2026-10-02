package cy.jdkdigital.cobblegengalore.common.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.block.entity.BlockGenBlockEntity;
import cy.jdkdigital.cobblegengalore.util.RecipeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockGenBlock extends BaseEntityBlock
{
    public static final MapCodec<BlockGenBlock> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(propertiesCodec(), Codec.INT.fieldOf("modifier").forGetter(block -> block.modifier)).apply(builder, BlockGenBlock::new));
    public final int modifier;

    public BlockGenBlock(Properties properties, int modifier) {
        super(properties);
        this.modifier = modifier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockGenBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ?
                createTickerHelper(blockEntityType, CobbleGenGalore.BLOCKGEN_BLOCKENTITY.get(), BlockGenBlockEntity::clientTick) :
                createTickerHelper(blockEntityType, CobbleGenGalore.BLOCKGEN_BLOCKENTITY.get(), BlockGenBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof BlockGenBlockEntity blockEntity) {
            if (!blockEntity.getBuffer().isEmpty()) {
                var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (ItemStack.isSameItemSameComponents(itemInHand, blockEntity.getBuffer())) {
                    var addedCount = Math.min(itemInHand.getMaxStackSize() - itemInHand.getCount(), blockEntity.getBuffer().getCount());
                    itemInHand.grow(addedCount);
                    blockEntity.getBuffer().shrink(addedCount);
                } else if (itemInHand.isEmpty()) {
                    var addedStack = blockEntity.getBuffer().copy();
                    addedStack.setCount(Math.min(addedStack.getCount(), addedStack.getMaxStackSize()));
                    player.setItemInHand(InteractionHand.MAIN_HAND, addedStack);
                    if (addedStack.getCount() == blockEntity.getBuffer().getCount()) {
                        blockEntity.clearBuffer();
                    } else {
                        blockEntity.getBuffer().shrink(addedStack.getCount());
                    }
                }
                blockEntity.setChanged();
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        refreshRecipe(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        refreshRecipe(level, pos);
    }

    private void refreshRecipe(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            if (level.getBlockEntity(pos) instanceof BlockGenBlockEntity blockEntity) {
                blockEntity.setRecipe(RecipeHelper.getRecipe(serverLevel, pos));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("block_gen.tooltip", this.modifier).withStyle(ChatFormatting.GOLD));
    }
}
