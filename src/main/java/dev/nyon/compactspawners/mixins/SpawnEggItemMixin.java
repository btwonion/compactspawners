package dev.nyon.compactspawners.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Shadow
    public abstract EntityType<?> getType(@Nullable CompoundTag nbt);

    @Inject(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    public void compactSpawnerHandling(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var blockState = level.getBlockState(pos);
        if (!blockState.is(Blocks.SPAWNER)) return;
        var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CompactSpawnerEntity compactSpawnerEntity)) return;
        var itemStack = context.getItemInHand();
        EntityType<?> entityType = getType(itemStack.getTag());
        compactSpawnerEntity.setEntityId(entityType, level.getRandom());
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, blockState, blockState, 3);
        level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, pos);
        itemStack.shrink(1);
        cir.setReturnValue(InteractionResult.CONSUME);
    }

    @ModifyExpressionValue(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"
        )
    )
    public BlockEntity replaceEntityWithNull(BlockEntity original) {
        return null;
    }
}