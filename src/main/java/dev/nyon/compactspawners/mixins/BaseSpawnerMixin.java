package dev.nyon.compactspawners.mixins;

import dev.nyon.compactspawners.spawner.CompactSpawnerBlock;
import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        if (getConfig().getRequiredPlayerRange() != -1 && !this.isNearPlayer(serverLevel, pos)) {
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

        BlockEntity rawEntity = serverLevel.getBlockEntity(pos);
        CompactSpawnerTickInterface compactSpawnerData = (CompactSpawnerTickInterface) rawEntity;
        int mobsToSpawn = getConfig().getMobsPerSpawner() * (compactSpawnerData.getSpawners().size() + 1);
        System.out.println(mobsToSpawn);
        for (int i = 0; i <= mobsToSpawn; i++) {
            if (!(entityType.get().create(serverLevel) instanceof Mob mob)) {
                ci.cancel();
                return;
            }

            Player player = FakePlayer.get(serverLevel);
            mob.setLastHurtByPlayer(player);
            LootTable lootTable = serverLevel.getServer().getLootTables().get(mob.getLootTable());
            LootContext.Builder lootContextBuilder = ((MobAccessor) mob).invokeCreateLootContext(true, serverLevel.damageSources().playerAttack(player));
            lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.ENTITY), itemStack -> compactSpawnerData.setMobDrops(CompactSpawnerBlock.INSTANCE.handleNewDrop(itemStack, compactSpawnerData.getMobDrops(), serverLevel, pos)));

            compactSpawnerData.setExp(CompactSpawnerBlock.INSTANCE.handleNewExp(mob.getExperienceReward(), compactSpawnerData.getExp(), pos, serverLevel));
        }

        rawEntity.setChanged();
        ci.cancel();
    }

    private boolean isNearPlayer(ServerLevel level, BlockPos pos) {
        return level.hasNearbyAlivePlayer(
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            getConfig().getRequiredPlayerRange()
        );
    }
}
