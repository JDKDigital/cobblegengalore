package cy.jdkdigital.cobblegengalore.event;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.gametest.CobbleGenGaloreGameTests;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = CobbleGenGalore.MODID)
public class ModEventHandler
{
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.get());
    }

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, "empty"));
        Identifier structure = Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, "empty_7x7");
        CobbleGenGaloreGameTests.MAX_TICKS.forEach((name, maxTicks) -> {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, name));
            boolean required = CobbleGenGaloreGameTests.REQUIRED.getOrDefault(name, true);
            event.registerTest(
                    Identifier.fromNamespaceAndPath(CobbleGenGalore.MODID, name),
                    new FunctionGameTestInstance(function, new TestData<>(environment, structure, maxTicks, 0, required, Rotation.NONE, false, 1, 1, false, 0))
            );
        });
    }
}
