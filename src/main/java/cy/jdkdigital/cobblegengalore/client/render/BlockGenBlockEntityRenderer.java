package cy.jdkdigital.cobblegengalore.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.cobblegengalore.common.block.entity.BlockGenBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockGenBlockEntityRenderer implements BlockEntityRenderer<BlockGenBlockEntity, BlockGenBlockEntityRenderer.BlockGenRenderState>
{
    private final ItemModelResolver itemModelResolver;

    public BlockGenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public BlockGenRenderState createRenderState() {
        return new BlockGenRenderState();
    }

    @Override
    public void extractRenderState(BlockGenBlockEntity be, BlockGenRenderState state, float partialTicks, @Nonnull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        ItemStack result = be.hasResult() ? be.getResultItem() : null;
        state.hasResult = result != null && !result.isEmpty();
        if (state.hasResult) {
            state.isBlock = result.getItem() instanceof BlockItem;
            state.angle = (float) ((System.currentTimeMillis() / 800.0D * 30.0D) % 360);
            this.itemModelResolver.updateForTopItem(state.itemState, result, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(BlockGenRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (!state.hasResult) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (state.isBlock) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(state.angle));
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        state.itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class BlockGenRenderState extends BlockEntityRenderState
    {
        public boolean hasResult;
        public boolean isBlock;
        public float angle;
        public final ItemStackRenderState itemState = new ItemStackRenderState();
    }
}
