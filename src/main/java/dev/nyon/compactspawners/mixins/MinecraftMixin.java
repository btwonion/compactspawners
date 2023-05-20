package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.CompactSpawners;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
        method = "stop",
        at = @At("HEAD")
    )
    public void initClientShutdown(CallbackInfo ci) {
        CompactSpawners.INSTANCE.shutdown();
    }
}