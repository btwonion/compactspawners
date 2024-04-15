package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Shadow
    public abstract Block getBlock();

    @Inject(
        method = "useOn",
        at = @At(
            value = "HEAD"
        ),
        cancellable = true
    )
    private void invokeSpawnerOnCompactSpawnerAction(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(getBlock() instanceof SpawnerBlock)) return;
        var pos = context.getClickedPos();
        var entity = context.getLevel().getBlockEntity(pos);
        if (!(entity instanceof CompactSpawnerEntity compactSpawner)) return;

        var compactSpawnerResult = compactSpawner.use(context.getLevel(), pos, context.getPlayer(), context.getHand());
        cir.setReturnValue(compactSpawnerResult);
    }
}
