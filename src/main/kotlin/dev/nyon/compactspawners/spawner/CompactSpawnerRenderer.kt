package dev.nyon.compactspawners.spawner

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import kotlin.math.max

@Environment(EnvType.CLIENT)
class CompactSpawnerRenderer(context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<CompactSpawnerEntity> {
    private val entityRenderer: EntityRenderDispatcher = context.entityRenderer

    override fun render(
        blockEntity: CompactSpawnerEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val level = blockEntity.level
        if (level != null) {
            val baseSpawner = blockEntity.spawner
            val entity = baseSpawner.getOrCreateDisplayEntity(level, blockEntity.blockPos)
            if (entity != null) {
                renderEntityInSpawner(
                    partialTick,
                    poseStack,
                    buffer,
                    packedLight,
                    entity,
                    this.entityRenderer,
                    baseSpawner.getoSpin(),
                    baseSpawner.spin
                )
            }
        }
    }

    private fun renderEntityInSpawner(
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        entity: Entity,
        entityRenderer: EntityRenderDispatcher,
        oSpin: Double,
        spin: Double
    ) {
        poseStack.pushPose()
        poseStack.translate(0.5f, 0.0f, 0.5f)
        var f = 0.53125f
        val g = max(entity.bbWidth.toDouble(), entity.bbHeight.toDouble()).toFloat()
        if (g.toDouble() > 1.0) {
            f /= g
        }

        poseStack.translate(0.0f, 0.4f, 0.0f)
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick.toDouble(), oSpin, spin).toFloat() * 10.0f))
        poseStack.translate(0.0f, -0.2f, 0.0f)
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0f))
        poseStack.scale(f, f, f)
        entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0f, partialTick, poseStack, buffer, packedLight)
        poseStack.popPose()
    }
}
