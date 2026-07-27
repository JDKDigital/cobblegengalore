package cy.jdkdigital.cobblegengalore.datagen;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockModelProvider extends net.minecraft.client.data.models.ModelProvider
{
    private static final TextureSlot SLOT_SIDE = TextureSlot.create("0");
    private static final TextureSlot SLOT_TOP = TextureSlot.create("1");
    private static final TextureSlot SLOT_BOTTOM = TextureSlot.create("2");

    private static final ModelTemplate TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, "block/template_block_gen")),
            Optional.empty(),
            SLOT_SIDE, SLOT_TOP, SLOT_BOTTOM, TextureSlot.PARTICLE);

    private static final List<DeferredBlock<Block>> BLOCKS = List.of(
            CobbleGenGalore.BLOCKGEN_STONE_BLOCK,
            CobbleGenGalore.BLOCKGEN_COPPER_BLOCK,
            CobbleGenGalore.BLOCKGEN_IRON_BLOCK,
            CobbleGenGalore.BLOCKGEN_GOLD_BLOCK,
            CobbleGenGalore.BLOCKGEN_EMERALD_BLOCK,
            CobbleGenGalore.BLOCKGEN_DIAMOND_BLOCK,
            CobbleGenGalore.BLOCKGEN_NETHERITE_BLOCK);

    public BlockModelProvider(PackOutput packOutput) {
        super(packOutput, CobbleGenGalore.MODID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return BLOCKS.stream().map(b -> b.get().builtInRegistryHolder());
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BLOCKS.stream().map(b -> b.get().asItem().builtInRegistryHolder());
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (DeferredBlock<Block> holder : BLOCKS) {
            Block block = holder.get();
            String material = name(block).substring("block_gen_".length());

            TextureMapping mapping = new TextureMapping()
                    .put(SLOT_SIDE, tex(material + "_side"))
                    .put(SLOT_TOP, tex(material + "_top"))
                    .put(SLOT_BOTTOM, tex(material + "_bottom"))
                    .put(TextureSlot.PARTICLE, tex(material + "_side"));

            Identifier model = TEMPLATE.create(block, mapping, blockModels.modelOutput);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(model)));
            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
        }
    }

    private static Material tex(String name) {
        return new Material(Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, "block/" + name));
    }

    private static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private static MultiVariant plainVariant(Identifier model) {
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }
}
