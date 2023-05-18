package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerMenuKt;
import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return BaseEntityBlock.createTickerHelper(
            blockEntityType,
            BlockEntityType.MOB_SPAWNER,
            (tickLevel, blockPos, tickState, blockEntity) -> {
                if (tickLevel.isClientSide)
                    SpawnerBlockEntity.clientTick(tickLevel, blockPos, tickState, blockEntity);
                else
                    ((CompactSpawnerTickInterface) blockEntity).serverTick(blockEntity.getSpawner(), (ServerLevel) tickLevel, blockPos);
            }
        );
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience && !EnchantmentHelper.hasSilkTouch(stack)) {
            var i = 15 + level.random.nextInt(15) + level.random.nextInt(15);
            popExperience(level, pos, i);
        }
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        CompactSpawnerMenuKt.openCompactSpawnerGUI(player, (CompactSpawnerTickInterface) level.getBlockEntity(pos));
        return InteractionResult.PASS;
    }
}
