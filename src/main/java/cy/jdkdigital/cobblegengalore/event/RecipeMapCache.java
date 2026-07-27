package cy.jdkdigital.cobblegengalore.event;

import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CobbleGenGalore.MODID)
public final class RecipeMapCache
{
    private static RecipeMap recipeMap = RecipeMap.EMPTY;

    private RecipeMapCache() {}

    public static RecipeMap getRecipeMap() {
        return recipeMap;
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }
}
