package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.CompactSpawners;
import dev.nyon.compactspawners.spawner.CompactSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockEntityRenderers.class)
public abstract class BlockEntityRenderersMixin {
    @Shadow
    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> renderProvider) {}

    @Shadow
    @Final
    private static Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> PROVIDERS;

    @Inject(
        method = "createEntityRenderers",
        at = @At("HEAD")
    )
    private static void addSpawnerRendererForCompactSpawner(BlockEntityRendererProvider.Context context, CallbackInfoReturnable<Map<BlockEntityType<?>, BlockEntityRenderer<?>>> cir) {
        register(CompactSpawners.INSTANCE.getBlockEntityType(), CompactSpawnerRenderer::new);
        PROVIDERS.remove(BlockEntityType.MOB_SPAWNER);
    }
}
