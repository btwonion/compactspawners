package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Shadow
    @Final
    @Deprecated
    private Block block;

    @Inject(
        method = "useOn",
        at = @At(
            value = "HEAD"
        ),
        cancellable = true
    )
    private void invokeSpawnerOnCompactSpawnerAction(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(block instanceof SpawnerBlock spawnerBlock)) return;
        var pos = context.getClickedPos();
        if (context.getLevel().getBlockEntity(pos) == null) return;
        var compactSpawnerResult = spawnerBlock.use(context.getLevel().getBlockState(pos), context.getLevel(), pos, context.getPlayer(), context.getHand(), ((UseOnContextAccessor) context).getBHitResult());
        cir.setReturnValue(compactSpawnerResult);
    }

    @Redirect(
        method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BlockEntity;onlyOpCanSetNbt()Z"
        )
    )
    private static boolean writeTagToBlockEntity(BlockEntity instance) {
        return instance.onlyOpCanSetNbt() && !(instance instanceof CompactSpawnerEntity);
    }
}
