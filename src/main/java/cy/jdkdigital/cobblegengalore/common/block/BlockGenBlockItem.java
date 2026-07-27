package cy.jdkdigital.cobblegengalore.common.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class BlockGenBlockItem extends BlockItem
{
    public BlockGenBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, tooltipFlag);
        if (getBlock() instanceof BlockGenBlock blockGenBlock) {
            consumer.accept(Component.translatable("block_gen.tooltip", blockGenBlock.modifier).withStyle(ChatFormatting.GOLD));
        }
    }
}
