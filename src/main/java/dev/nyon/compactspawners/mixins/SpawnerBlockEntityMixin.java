package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface;
import dev.nyon.compactspawners.spawner.menu.CSMenuScreen;
import dev.nyon.compactspawners.spawner.menu.CompactSpawnerMenuProvider;
import dev.nyon.compactspawners.utils.CompoundExtensionsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(SpawnerBlockEntity.class)
public class SpawnerBlockEntityMixin extends BlockEntity implements CompactSpawnerTickInterface {
    @Shadow
    @Final
    private BaseSpawner spawner;
    boolean activated = false;
    ArrayList<ItemStack> mobDrops = new ArrayList<>();
    ArrayList<ItemStack> spawners = new ArrayList<>();
    Integer exp = 0;

    public SpawnerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
        method = "load",
        at = @At("TAIL")
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
        CompoundExtensionsKt.saveAllItems(tag, "drops", mobDrops);
        CompoundExtensionsKt.saveAllItems(tag, "spawners", spawners);
        tag.putInt("exp", exp);
    }

    @Override
    public void use(@NotNull Player player) {
        SpawnData nextSpawnData = ((BaseSpawnerAccessor) spawner).getNextSpawnData();
        if (nextSpawnData == null || EntityType.by(nextSpawnData.getEntityToSpawn()).isEmpty()) {
            activated = false;
            return;
        }
        if (!(player instanceof ServerPlayer)) return;

        activated = true;
        var menuProvider = new CompactSpawnerMenuProvider(this, getBlockPos(), CSMenuScreen.Start);
        player.openMenu(menuProvider);
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
    public ArrayList<ItemStack> getMobDrops() {
        return mobDrops;
    }

    @Override
    public void setMobDrops(@NotNull ArrayList<ItemStack> mobDrops) {
        this.mobDrops = mobDrops;
    }

    @NotNull
    @Override
    public ArrayList<ItemStack> getSpawners() {
        return spawners;
    }

    @Override
    public void setSpawners(@NotNull ArrayList<ItemStack> spawners) {
        this.spawners = spawners;
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
