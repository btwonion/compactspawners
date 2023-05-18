package dev.nyon.compactspawners.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {

    @Invoker("createLootContext")
    LootContext.Builder createLootContext(boolean hitByPlayer, DamageSource damageSource);
}