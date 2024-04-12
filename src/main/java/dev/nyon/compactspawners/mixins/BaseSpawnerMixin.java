package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

            ResourceKey<LootTable> resourceLocation = mob.getLootTable();
            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceLocation);
            LootParams.Builder builder = new LootParams.Builder((ServerLevel)mob.level())
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                .withLuck(getConfig().getLuck());

            LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
            lootTable.getRandomItems(lootParams, ((MobAccessor) mob).invokeGetLootTableSeed(), item -> spawnerEntity.handleNewDrop(item, serverLevel, pos));

            spawnerEntity.handleNewExp(mob.getExperienceReward(), serverLevel, pos);
        }

        serverLevel.playSound(null, pos, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.BLOCKS, 1, 0.75f);

        spawnerEntity.setChanged();
        ci.cancel();
    }

    @Unique
    private boolean isSpawnerAvailable(ServerLevel level, BlockPos pos) {
        return level.hasNearbyAlivePlayer(pos.getX(), pos.getY(), pos.getZ(), getConfig().getRequiredPlayerDistance());
    }
}
