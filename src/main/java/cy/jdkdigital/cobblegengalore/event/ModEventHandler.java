//package cy.jdkdigital.cobblegengalore.event;
//
//import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.capabilities.Capabilities;
//import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
//
//@EventBusSubscriber(modid = CobbleGenGalore.MODID, bus = EventBusSubscriber.Bus.MOD)
//public class ModEventHandler
//{
//    @SubscribeEvent
//    public static void registerBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
//        event.registerBlockEntity(
//                Capabilities.ItemHandler.BLOCK,
//                CobbleGenGalore.BLOCKGEN_BLOCKENTITY.get(),
//                (myBlockEntity, side) -> myBlockEntity.getItemHandler()
//        );
//    }
//}
