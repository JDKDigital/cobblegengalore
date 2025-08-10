package cy.jdkdigital.cobblegengalore.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.cobblegengalore.CobbleGenGalore;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class BlockGenRecipe implements Recipe<RecipeInput>
{
    public ItemStack result;
    public final BlockState left;
    public final BlockState right;
    public final BlockState modifier;
    public final float speed;
    public final boolean consumeLeft;
    public final boolean consumeRight;

    public BlockGenRecipe(ItemStack result, BlockState left, BlockState right, BlockState modifier, float speed, boolean consumeLeft, boolean consumeRight) {
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
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider pRegistries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return result.copy();
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return CobbleGenGalore.BLOCKGEN_RECIPE_SERIALIZER.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return CobbleGenGalore.BLOCKGEN_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BlockGenRecipe>
    {
        private static final MapCodec<BlockGenRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                ItemStack.CODEC.fieldOf("result").orElse(ItemStack.EMPTY).forGetter(recipe -> recipe.result),
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
                BlockGenRecipe.Serializer::toNetwork, BlockGenRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<BlockGenRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlockGenRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static BlockGenRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
            try {
                ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                BlockState left = readBlockState(buffer.readNbt());
                BlockState right = readBlockState(buffer.readNbt());
                BlockState modifier = readBlockState(buffer.readNbt());

                return new BlockGenRecipe(result, left, right, modifier, buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean());
            } catch (Exception e) {
                CobbleGenGalore.LOGGER.error("Error reading blockgen recipe from packet. ", e);
                throw e;
            }
        }

        public static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, BlockGenRecipe recipe) {
            try {
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
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

    private static BlockState readBlockState(@Nullable CompoundTag tag) {
        if (tag == null) return Blocks.AIR.defaultBlockState();

        ResourceLocation resourcelocation = ResourceLocation.parse(tag.getString("Name"));
        Block block = BuiltInRegistries.BLOCK.get(resourcelocation);
        BlockState blockstate = block.defaultBlockState();
        if (tag.contains("Properties", 10)) {
            CompoundTag compoundtag = tag.getCompound("Properties");
            StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();

            for (String propertyName : compoundtag.getAllKeys()) {
                Property<?> property = statedefinition.getProperty(propertyName);
                if (property != null) {
                    blockstate = setValueHelper(blockstate, property, propertyName, compoundtag, tag);
                }
            }
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState setValueHelper(BlockState blockState, Property<T> property, String propertyName, CompoundTag tag, CompoundTag stateTag) {
        Optional<T> optional = property.getValue(tag.getString(propertyName));
        if (optional.isPresent()) {
            return blockState.setValue(property, optional.get());
        } else {
            CobbleGenGalore.LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", propertyName, tag.getString(propertyName), stateTag.toString());
            return blockState;
        }
    }
}
