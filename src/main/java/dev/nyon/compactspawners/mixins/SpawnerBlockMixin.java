package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface;
import dev.nyon.compactspawners.utils.BlockPosExtensionKt;
import dev.nyon.compactspawners.utils.ServerLevelExtensionsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.stream.Stream;

@Mixin(SpawnerBlock.class)
public class SpawnerBlockMixin extends BaseEntityBlock {
    protected SpawnerBlockMixin(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpawnerBlockEntity(pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);

        if (!(level instanceof ServerLevel serverLevel)) return;

        CompactSpawnerTickInterface compactSpawnerInfo = (CompactSpawnerTickInterface) level.getBlockEntity(pos);
        for (ItemStack i : Stream.of(compactSpawnerInfo.getMobDrops(), compactSpawnerInfo.getSpawners()).flatMap(Collection::stream).toList()) {
            Block.popResource(level, BlockPosExtensionKt.random(pos, 1, true, true), i);
        }

        ServerLevelExtensionsKt.dropExperience(
            serverLevel,
            BlockPosExtensionKt.random(pos, 1, true, true),
            compactSpawnerInfo.getExp()
        );
    }

    @Override
    public void spawnAfterBreak(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        ItemStack stack,
        boolean dropExperience
    ) {
        if (dropExperience && !EnchantmentHelper.hasSilkTouch(stack)) {
            var i = 15 + level.random.nextInt(15) + level.random.nextInt(15);
            popExperience(level, pos, i);
        }
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ((CompactSpawnerTickInterface) level.getBlockEntity(pos)).use(player);
        return InteractionResult.PASS;
    }
}
