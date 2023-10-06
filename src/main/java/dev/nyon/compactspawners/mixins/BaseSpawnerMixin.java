package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static dev.nyon.compactspawners.config.ConfigKt.getConfig;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {

    @Shadow
    private int spawnDelay;

    @Shadow
    protected abstract SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource random, BlockPos pos);

    @Shadow
    protected abstract void delay(Level level, BlockPos pos);

    @Inject(
        method = "serverTick",
        at = @At("HEAD"),
        cancellable = true
    )
    public void customServerTick(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (getConfig().getRequiredPlayerDistance() != -1 && !this.isSpawnerAvailable(serverLevel, pos)) {
            ci.cancel();
            return;
        }

        if (spawnDelay > 0) {
            spawnDelay -= 1;
            ci.cancel();
            return;
        }

        delay(serverLevel, pos);

        SpawnData spawnData = getOrCreateNextSpawnData(serverLevel, serverLevel.getRandom(), pos);
        Optional<EntityType<?>> entityType = EntityType.by(spawnData.getEntityToSpawn());
        if (entityType.isEmpty()) {
            ci.cancel();
            return;
        }

        var spawnerEntity = (CompactSpawnerEntity) serverLevel.getBlockEntity(pos);
        int mobsToSpawn = getConfig().getMobsPerSpawner() * (spawnerEntity.getSpawnerCount());

        for (int i = 1; i <= mobsToSpawn; i++) {
            if (!(entityType.get().create(serverLevel) instanceof Mob mob)) {
                ci.cancel();
                return;
            }

            FakePlayer player = FakePlayer.get(serverLevel);
            mob.setLastHurtByPlayer(player);
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(mob.getLootTable());
            LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, player.getEyePosition()).withParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().playerAttack(player)).create(LootContextParamSets.ENTITY);
            lootTable.getRandomItems(lootParams, ((MobAccessor) mob).invokeGetLootTableSeed(), item -> spawnerEntity.handleNewDrop(item, serverLevel, pos));

            spawnerEntity.handleNewExp(mob.getExperienceReward(), serverLevel, pos);
        }

        spawnerEntity.setChanged();
        ci.cancel();
    }

    @Unique
    private boolean isSpawnerAvailable(ServerLevel level, BlockPos pos) {
        return level.hasNearbyAlivePlayer(pos.getX(), pos.getY(), pos.getZ(), getConfig().getRequiredPlayerDistance());
    }
}
