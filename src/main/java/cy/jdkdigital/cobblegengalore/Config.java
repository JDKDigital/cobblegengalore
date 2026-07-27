package cy.jdkdigital.cobblegengalore;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CobbleGenGalore.MODID)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue TICK_RATE = BUILDER
            .comment("Number of ticks between production cycles")
            .defineInRange("tickRate", 20, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int tickRate;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        tickRate = TICK_RATE.get();
    }
}
