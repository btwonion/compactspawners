package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.CompactSpawners;
import dev.nyon.compactspawners.config.ConfigKt;
import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import dev.nyon.compactspawners.utils.ServerLevelExtensionsKt;
import kotlin.ranges.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpawnerBlock.class)
public class SpawnerBlockMixin extends BaseEntityBlock {
    protected SpawnerBlockMixin(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompactSpawnerEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, CompactSpawners.INSTANCE.getBlockEntityType(), (level1, blockPos, blockState, blockEntity) -> {
            if (level1.isClientSide) CompactSpawnerEntity.Companion.clientTick(level1, blockPos, blockEntity);
            else CompactSpawnerEntity.Companion.serverTick((ServerLevel) level1, blockPos, blockEntity);
        });
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if (!(level instanceof ServerLevel serverLevel)) return;

        var spawnerEntity = (CompactSpawnerEntity) level.getBlockEntity(pos);
        var spawnerItem = generateSpawnerItem(spawnerEntity.getSpawner());

        spawnerEntity.getStoredDrops().forEach(item -> Block.popResource(level, pos, item));
        if (EnchantmentHelper.hasSilkTouch(player.getMainHandItem())) {
            var stacks = spawnerEntity.getSpawnerCount() / 64;
            var singleItems = spawnerEntity.getSpawnerCount() - (stacks * 64);

            if (stacks > 0)
                new IntRange(1, stacks).forEach(i -> Block.popResource(level, pos, spawnerItem.copyWithCount(64)));
            if (singleItems > 0)
                Block.popResource(level, pos, spawnerItem.copyWithCount(singleItems));
        }

        ServerLevelExtensionsKt.dropExperience(
            serverLevel,
            pos,
            spawnerEntity.getStoredExp()
        );
        spawnerEntity.setStoredExp(0);
    }

    @Override
    public void spawnAfterBreak(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        ItemStack stack,
        boolean dropExperience
    ) {
        if (!dropExperience && !EnchantmentHelper.hasSilkTouch(stack)) return;
        var i = 15 + level.random.nextInt(15) + level.random.nextInt(15);
        popExperience(level, pos, i);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var spawnerEntity = (CompactSpawnerEntity) level.getBlockEntity(pos);

        SpawnData nextSpawnData = ((BaseSpawnerAccessor) spawnerEntity.getSpawner()).getNextSpawnData();
        if (nextSpawnData == null || EntityType.by(nextSpawnData.getEntityToSpawn()).isEmpty()) {
            spawnerEntity.setActivated(false);
            return InteractionResult.FAIL;
        }
        spawnerEntity.setActivated(true);
        var itemInHand = player.getItemInHand(hand);
        if (
            itemInHand.is(Items.SPAWNER)
                && itemInHand.hasTag()
                && itemInHand.getTag().getCompound("BlockEntityTag").getCompound("SpawnData").getCompound("entity").getString("id").equals(nextSpawnData.entityToSpawn().getString("id"))
        ) {
            var freeSlots = ConfigKt.getConfig().getMaxMergedSpawners() - spawnerEntity.getSpawnerCount();
            if (ConfigKt.getConfig().getMaxMergedSpawners() == -1 || freeSlots >= itemInHand.getCount()) {
                spawnerEntity.setSpawnerCount(spawnerEntity.getSpawnerCount() + itemInHand.getCount());
                player.setItemInHand(hand, ItemStack.EMPTY);
            } else {
                spawnerEntity.setSpawnerCount(spawnerEntity.getSpawnerCount() + freeSlots);
                player.setItemInHand(hand, itemInHand.copyWithCount(itemInHand.getCount() - freeSlots));
            }
            return InteractionResult.SUCCESS;
        }

        player.openMenu(spawnerEntity);
        return InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        var spawnerEntity = (CompactSpawnerEntity) level.getBlockEntity(pos);
        player.giveExperiencePoints(spawnerEntity.getStoredExp());
        spawnerEntity.setStoredExp(0);
    }


    @Unique
    private ItemStack generateSpawnerItem(BaseSpawner baseSpawner) {
        var spawnerItem = new ItemStack(Items.SPAWNER);
        var spawnData = ((BaseSpawnerAccessor) baseSpawner).getNextSpawnData();
        if (spawnData == null) return spawnerItem;
        var itemTag = spawnerItem.getOrCreateTag();
        var blockEntityTag = new CompoundTag();
        var spawnDataTag = new CompoundTag();
        var entityTag = new CompoundTag();
        entityTag.putString("id", spawnData.getEntityToSpawn().getString("id"));
        spawnDataTag.put("entity", entityTag);
        blockEntityTag.put("SpawnData", spawnDataTag);
        itemTag.put("BlockEntityTag", blockEntityTag);
        return spawnerItem;
    }
}
