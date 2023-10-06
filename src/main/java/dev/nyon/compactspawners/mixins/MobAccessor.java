package dev.nyon.compactspawners.mixins;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {

    @Invoker("getLootTableSeed")
    long invokeGetLootTableSeed();
}