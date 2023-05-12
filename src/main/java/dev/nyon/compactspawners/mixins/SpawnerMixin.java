package dev.nyon.compactspawners.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SpawnerBlock.class)
public class SpawnerMixin {
    @ModifyVariable(
        method = "spawnAfterBreak",
        at = @At(
            value = "HEAD"
        ),
        index = 5,
        argsOnly = true
    )
    public boolean cancelExpOnBreak(
        boolean value,
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        ItemStack stack,
        boolean dropExperience
    ) {
        return !EnchantmentHelper.hasSilkTouch(stack);
    }
}