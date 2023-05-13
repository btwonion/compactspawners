package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.storage.SpawnerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow
    public static List<ItemStack> getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool) {
        return null;
    }

    @Inject(
        method = "setPlacedBy",
        at = @At("HEAD")
    )
    public void callSpawnerPlacement(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (!(((Block) (Object) this) instanceof SpawnerBlock)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        new SpawnerSavedData(serverLevel).createSpawner(level.dimension().location(), pos);
    }

    @Redirect(
        method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
        )
    )
    private static List<ItemStack> addSpawnerDrops(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        @Nullable Entity entity,
        ItemStack tool
    ) {
        var list = getDrops(state, level, pos, blockEntity, entity, tool);
        if (state.getBlock() instanceof SpawnerBlock) {
            var savedData = new SpawnerSavedData(level);
            var data = savedData.retrieveSpawner(level.dimension().location(), pos);
            list.addAll(Stream.of(data.component3(), data.component4()).flatMap(Collection::stream).toList());
        }

        return list;
    }
}
