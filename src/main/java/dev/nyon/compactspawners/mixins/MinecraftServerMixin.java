package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.CompactSpawners;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(
        method = "stopServer",
        at = @At("HEAD")
    )
    public void initServerShutdown(CallbackInfo ci) {
        CompactSpawners.INSTANCE.shutdown();
    }
}