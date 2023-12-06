package dev.nyon.compactspawners.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomFeatureMixin {

    @Shadow protected abstract EntityType<?> randomEntityId(RandomSource random);

    @ModifyExpressionValue(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/WorldGenLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"
        )
    )
    @SuppressWarnings("unused")
    private BlockEntity redirectVanillaSpawnerInitiation(BlockEntity original, FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (original instanceof CompactSpawnerEntity compactSpawner)
            compactSpawner.setEntityId(randomEntityId(context.random()), context.random());
        return null;
    }

    @Redirect(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V"
        )
    )
    private void disableErrorMessage(Logger instance, String s, Object[] objects) {}
}
