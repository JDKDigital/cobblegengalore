package cy.jdkdigital.cobblegengalore;

import cy.jdkdigital.cobblegengalore.common.block.BlockGenBlock;
import cy.jdkdigital.cobblegengalore.common.block.BlockGenBlockItem;
import cy.jdkdigital.cobblegengalore.common.block.entity.BlockGenBlockEntity;
import cy.jdkdigital.cobblegengalore.common.recipe.BlockGenRecipe;
import cy.jdkdigital.cobblegengalore.gametest.CobbleGenGaloreGameTests;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CobbleGenGalore.MODID)
public class CobbleGenGalore
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "cobblegengalore";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

    public static final DeferredBlock<Block> BLOCKGEN_STONE_BLOCK = registerBlock("block_gen_stone", 1, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> BLOCKGEN_COPPER_BLOCK = registerBlock("block_gen_copper", 2, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> BLOCKGEN_IRON_BLOCK = registerBlock("block_gen_iron", 4, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> BLOCKGEN_GOLD_BLOCK = registerBlock("block_gen_gold", 8, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> BLOCKGEN_EMERALD_BLOCK = registerBlock("block_gen_emerald", 16, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> BLOCKGEN_DIAMOND_BLOCK = registerBlock("block_gen_diamond", 32, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> BLOCKGEN_NETHERITE_BLOCK = registerBlock("block_gen_netherite", 64, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).noOcclusion());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockGenBlockEntity>> BLOCKGEN_BLOCKENTITY = BLOCK_ENTITIES.register("blockgen", () -> new BlockEntityType<>(BlockGenBlockEntity::new,
            BLOCKGEN_STONE_BLOCK.get(),
            BLOCKGEN_COPPER_BLOCK.get(),
            BLOCKGEN_IRON_BLOCK.get(),
            BLOCKGEN_GOLD_BLOCK.get(),
            BLOCKGEN_EMERALD_BLOCK.get(),
            BLOCKGEN_DIAMOND_BLOCK.get(),
            BLOCKGEN_NETHERITE_BLOCK.get()
    ));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlockGenRecipe>> BLOCKGEN_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("blockgen", () -> BlockGenRecipe.SERIALIZER);
    public static DeferredHolder<RecipeType<?>, RecipeType<BlockGenRecipe>> BLOCKGEN_RECIPE_TYPE = RECIPE_TYPES.register("blockgen", () -> new RecipeType<>() {});

    public static final TagKey<Item> BLOCK_GENERATORS = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "block_generators"));


    public CobbleGenGalore(IEventBus modEventBus, ModContainer modContainer)
    {
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        CobbleGenGaloreGameTests.init();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ITEMS.getEntries().forEach(itemDeferredHolder -> {
                event.accept(itemDeferredHolder.value());
            });
        }
    }

    private static DeferredBlock<Block> registerBlock(String name, int modifier, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(name, p -> new BlockGenBlock(p, modifier), properties);
        ITEMS.registerItem(name, p -> new BlockGenBlockItem(block.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
        return block;
    }
}
