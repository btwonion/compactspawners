package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.config.ConfigKt;
import dev.nyon.compactspawners.spawner.CompactSpawnerBlock;
import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface;
import dev.nyon.compactspawners.spawner.CustomTicker;
import dev.nyon.compactspawners.utils.BlockPosExtensionKt;
import dev.nyon.compactspawners.utils.CompoundExtensionsKt;
import dev.nyon.compactspawners.utils.ServerLevelExtensionsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(SpawnerBlockEntity.class)
public class SpawnerBlockEntityMixin extends BlockEntity implements CompactSpawnerTickInterface {
    boolean activated = false;
    NonNullList<ItemStack> mobDrops = NonNullList.withSize(ConfigKt.getConfig().getMaxStoredDrops(), ItemStack.EMPTY);
    NonNullList<ItemStack> spawners = NonNullList.withSize(ConfigKt.getConfig().getMaxMergedSpawners(), ItemStack.EMPTY);
    Integer exp = 0;

    public SpawnerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (getLevel() == null) return;
        for (ItemStack i : Stream.of(mobDrops, spawners).flatMap(Collection::stream).toList()) {
            Block.popResource(getLevel(), BlockPosExtensionKt.random(getBlockPos(), 1, true), i);
        }

        ServerLevelExtensionsKt.dropExperience(
            (ServerLevel) getLevel(),
            BlockPosExtensionKt.random(getBlockPos(), 1, true),
            exp
        );
    }

    @Inject(
        method = "load",
        at = @At("HEAD")
    )
    public void manipulateLoad(CompoundTag tag, CallbackInfo ci) {
        activated = tag.getBoolean("activated");
        CompoundExtensionsKt.loadAllItems(tag, "drops", mobDrops);
        CompoundExtensionsKt.loadAllItems(tag, "spawners", spawners);
        exp = tag.getInt("exp");
    }

    @Inject(
        method = "saveAdditional",
        at = @At("TAIL")
    )
    public void manipulateSave(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("activated", activated);
        CompoundExtensionsKt.saveAllItems(tag, "drops", mobDrops, false);
        CompoundExtensionsKt.saveAllItems(tag, "spawners", spawners, false);
        tag.putInt("exp", exp);
    }


    @Override
    public void serverTick(@NotNull BaseSpawner instance, @NotNull ServerLevel level, @NotNull BlockPos pos) {
        Optional<EntityType<?>> entityType = EntityType.by(((BaseSpawnerAccessor) instance).getNextSpawnData().getEntityToSpawn());
        if (entityType.isEmpty()) return;
        CustomTicker.INSTANCE.serverTick(level, pos, entityType.get(), /* TODO */ 1, (ItemStack itemStack) -> {
            CompactSpawnerBlock.INSTANCE.handleNewDrop(itemStack, mobDrops, level, pos);
            return null;
        }, (Integer newExp) -> {
            exp = CompactSpawnerBlock.INSTANCE.handleNewExp(newExp, exp, pos, level);
            return null;
        });
    }

    @Override
    public boolean getActivated() {
        return activated;
    }

    @Override
    public void setActivated(boolean b) {
        activated = b;
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getMobDrops() {
        return mobDrops;
    }

    @Override
    public void setMobDrops(@NotNull NonNullList<ItemStack> itemStacks) {
        mobDrops = itemStacks;
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getSpawners() {
        return spawners;
    }

    @Override
    public void setSpawners(@NotNull NonNullList<ItemStack> itemStacks) {
        spawners = itemStacks;
    }

    @Override
    public int getExp() {
        return exp;
    }

    @Override
    public void setExp(int i) {
        exp = i;
    }
}
