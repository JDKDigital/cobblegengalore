package cy.jdkdigital.cobblegengalore.event;


import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import cy.jdkdigital.cobblegengalore.client.render.BlockGenBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CobbleGenGalore.MODID, value = Dist.CLIENT)
public class ClientEventHandler
{
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CobbleGenGalore.BLOCKGEN_BLOCKENTITY.get(), BlockGenBlockEntityRenderer::new);
    }
}
