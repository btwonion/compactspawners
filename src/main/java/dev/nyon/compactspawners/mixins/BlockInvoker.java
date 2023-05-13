package dev.nyon.compactspawners.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
interface BlockInvoker {
    @Invoker("popExperience")
    void invokePopExperience(ServerLevel level, BlockPos pos, int amount);
}