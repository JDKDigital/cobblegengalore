package cy.jdkdigital.cobblegengalore.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BlockGenRecipe implements Recipe<RecipeInput>
{
    public static final MapCodec<BlockGenRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                            BlockState.CODEC.fieldOf("left").orElse(Blocks.AIR.defaultBlockState()).forGetter(recipe -> recipe.left),
                            BlockState.CODEC.fieldOf("right").orElse(Blocks.AIR.defaultBlockState()).forGetter(recipe -> recipe.right),
                            BlockState.CODEC.fieldOf("modifier").orElse(Blocks.AIR.defaultBlockState()).forGetter(recipe -> recipe.modifier),
                            Codec.FLOAT.fieldOf("speed").orElse(1f).forGetter(recipe -> recipe.speed),
                            Codec.BOOL.fieldOf("consumeLeft").orElse(false).forGetter(recipe -> recipe.consumeLeft),
                            Codec.BOOL.fieldOf("consumeRight").orElse(false).forGetter(recipe -> recipe.consumeRight)
                    )
                    .apply(builder, BlockGenRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockGenRecipe> STREAM_CODEC = StreamCodec.of(
            BlockGenRecipe::toNetwork, BlockGenRecipe::fromNetwork
    );

    public static final RecipeSerializer<BlockGenRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public ItemStackTemplate result;
    public final BlockState left;
    public final BlockState right;
    public final BlockState modifier;
    public final float speed;
    public final boolean consumeLeft;
    public final boolean consumeRight;

    public BlockGenRecipe(ItemStackTemplate result, BlockState left, BlockState right, BlockState modifier, float speed, boolean consumeLeft, boolean consumeRight) {
        this.result = result;
        this.left = left;
        this.right = right;
        this.modifier = modifier;
        this.speed = speed;
        this.consumeLeft = consumeLeft;
        this.consumeRight = consumeRight;
    }

    @Override
    public boolean matches(RecipeInput inv, Level worldIn) {
        return false;
    }

    public boolean matches(BlockState first, BlockState second, BlockState below) {
        boolean validModifierBlock = this.modifier.isAir() || this.modifier.is(below.getBlock());
        return validModifierBlock && (
                ((this.left.isAir() || this.left.is(first.getBlock())) && (this.right.isAir() || this.right.is(second.getBlock()))) ||
                ((this.right.isAir() || this.right.is(first.getBlock())) && (this.left.isAir() || this.left.is(second.getBlock())))
        );
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack assemble(RecipeInput inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Nonnull
    @Override
    public RecipeSerializer<BlockGenRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Nonnull
    @Override
    public RecipeType<BlockGenRecipe> getType() {
        return CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.get();
    }

    public static BlockGenRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
        try {
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            BlockState left = NbtUtils.readBlockState(buffer.registryAccess().lookupOrThrow(Registries.BLOCK), buffer.readNbt());
            BlockState right = NbtUtils.readBlockState(buffer.registryAccess().lookupOrThrow(Registries.BLOCK), buffer.readNbt());
            BlockState modifier = NbtUtils.readBlockState(buffer.registryAccess().lookupOrThrow(Registries.BLOCK), buffer.readNbt());

            return new BlockGenRecipe(result, left, right, modifier, buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean());
        } catch (Exception e) {
            CobbleGenGalore.LOGGER.error("Error reading blockgen recipe from packet. ", e);
            throw e;
        }
    }

    public static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, BlockGenRecipe recipe) {
        try {
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeNbt(NbtUtils.writeBlockState(recipe.left));
            buffer.writeNbt(NbtUtils.writeBlockState(recipe.right));
            buffer.writeNbt(NbtUtils.writeBlockState(recipe.modifier));
            buffer.writeFloat(recipe.speed);
            buffer.writeBoolean(recipe.consumeLeft);
            buffer.writeBoolean(recipe.consumeRight);
        } catch (Exception e) {
            CobbleGenGalore.LOGGER.error("Error writing blockgen recipe to packet. ", e);
            throw e;
        }
    }
}
