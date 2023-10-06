package dev.nyon.compactspawners.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nyon.compactspawners.spawner.CompactSpawnerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherFortressPieces.MonsterThrone.class)
public class NetherFortressPiecesMixin {

    @ModifyExpressionValue(
        method = "postProcess",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/WorldGenLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"
        )
    )
    private BlockEntity redirectVanillaSpawnerInitiation(BlockEntity original, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        if (original instanceof CompactSpawnerEntity compactSpawner)
            compactSpawner.setEntityId(EntityType.BLAZE, random);
        return null;
    }
}
