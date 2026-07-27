package cy.jdkdigital.cobblegengalore.gametest;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.common.block.entity.BlockGenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CobbleGenGaloreGameTests
{
    public static final Map<String, Consumer<GameTestHelper>> FUNCTIONS = new LinkedHashMap<>();
    public static final Map<String, Integer> MAX_TICKS = new LinkedHashMap<>();
    public static final Map<String, Boolean> REQUIRED = new LinkedHashMap<>();

    private static boolean registered = false;

    static {
        register("cobblestone", helper -> blockGenTest(helper, Items.COBBLESTONE, Blocks.WATER, Blocks.LAVA, null));
        register("stone", helper -> blockGenTest(helper, Items.STONE, Blocks.WATER, Blocks.LAVA, Blocks.STONE));
        register("andesite", helper -> blockGenTest(helper, Items.ANDESITE, Blocks.WATER, Blocks.LAVA, Blocks.ANDESITE));
        register("basalt", helper -> blockGenTest(helper, Items.BASALT, Blocks.BLUE_ICE, Blocks.LAVA, null));
        register("cobbled_deepslate", helper -> blockGenTest(helper, Items.COBBLED_DEEPSLATE, Blocks.PACKED_ICE, Blocks.LAVA, null));
        register("diorite", helper -> blockGenTest(helper, Items.DIORITE, Blocks.WATER, Blocks.LAVA, Blocks.DIORITE));
        register("granite", helper -> blockGenTest(helper, Items.GRANITE, Blocks.WATER, Blocks.LAVA, Blocks.GRANITE));
        register("netherrack", helper -> blockGenTest(helper, Items.NETHERRACK, Blocks.LAVA, Blocks.LAVA, Blocks.NETHERRACK));
        register("obsidian", helper -> blockGenTest(helper, Items.OBSIDIAN, Blocks.WATER, Blocks.LAVA, Blocks.OBSIDIAN));
        register("tuff", helper -> blockGenTest(helper, Items.TUFF, Blocks.WATER, Blocks.LAVA, Blocks.TUFF));
        register("calcite", helper -> blockGenTest(helper, Items.CALCITE, Blocks.WATER, Blocks.LAVA, Blocks.CALCITE));
        register("blackstone", helper -> blockGenTest(helper, Items.BLACKSTONE, Blocks.LAVA, Blocks.LAVA, Blocks.BLACKSTONE));
        register("end_stone", helper -> blockGenTest(helper, Items.END_STONE, Blocks.WATER, Blocks.LAVA, Blocks.END_STONE));
        register("mud", helper -> blockGenTest(helper, Items.MUD, Blocks.WATER, Blocks.DIRT, null));

        // Cross-mod compat: only resolves when allthemodium is on the runtime classpath, so it is optional.
        register("atm_ancient_stone", CobbleGenGaloreGameTests::atmAncientStone, 160, false);
    }

    private static void register(String name, Consumer<GameTestHelper> body) {
        register(name, body, 160, true);
    }

    private static void register(String name, Consumer<GameTestHelper> body, int maxTicks, boolean required) {
        FUNCTIONS.put(name, body);
        MAX_TICKS.put(name, maxTicks);
        REQUIRED.put(name, required);
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        if (registered || System.getProperty("neoforge.enabledGameTestNamespaces") == null) {
            return;
        }
        registered = true;
        MappedRegistry<Consumer<GameTestHelper>> registry = (MappedRegistry<Consumer<GameTestHelper>>) BuiltInRegistries.TEST_FUNCTION;
        registry.unfreeze(false);
        FUNCTIONS.forEach((name, body) ->
                Registry.register(registry, ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, name)), body));
        registry.freeze();
    }

    private static void atmAncientStone(GameTestHelper helper) {
        Block soulLava = BuiltInRegistries.BLOCK.get(Identifier.fromNamespaceAndPath("allthemodium", "soul_lava")).map(net.minecraft.core.Holder::value).orElse(null);
        Item result = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("allthemodium", "ancient_stone")).map(net.minecraft.core.Holder::value).orElse(null);
        if (soulLava == null || result == null) {
            throw helper.assertionException("allthemodium is not loaded; ancient_stone compat recipe cannot be verified");
        }
        blockGenTest(helper, result, soulLava, Blocks.BLUE_ICE, Blocks.OXIDIZED_COPPER);
    }

    private static void blockGenTest(GameTestHelper helper, Item result, Block left, Block right, @Nullable Block modifier) {
        BlockPos genPos = new BlockPos(3, 2, 3);
        helper.setBlock(genPos, CobbleGenGalore.BLOCKGEN_STONE_BLOCK.get());
        if (modifier != null) {
            helper.setBlock(genPos.below(), modifier);
        }
        helper.setBlock(genPos.north(), left);
        helper.setBlock(genPos.south(), right);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    BlockGenBlockEntity be = helper.getBlockEntity(genPos, BlockGenBlockEntity.class);
                    ItemStack buffer = be.getBuffer();
                    if (buffer.isEmpty() || !buffer.is(result)) {
                        throw helper.assertionException(genPos, "generator did not produce " + result);
                    }
                })
                .thenSucceed();
    }
}
