package cy.jdkdigital.cobblegengalore.datagen;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CobbleGenGalore.MODID)
public class CobbleGenGaloreDataProvider
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        gen.addProvider(true, new BlockModelProvider(output));
        gen.addProvider(true, new RecipeProvider.Runner(output, provider));
    }
}
