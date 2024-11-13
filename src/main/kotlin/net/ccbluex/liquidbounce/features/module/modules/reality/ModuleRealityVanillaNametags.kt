package net.ccbluex.liquidbounce.features.module.modules.reality

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.render.nametags.ModuleNametags
import net.ccbluex.liquidbounce.utils.client.asText
import net.ccbluex.liquidbounce.utils.client.regular
import net.ccbluex.liquidbounce.utils.client.withColor
import net.ccbluex.liquidbounce.utils.combat.shouldBeShown
import net.ccbluex.liquidbounce.utils.entity.getActualHealth
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAttachmentType
import net.minecraft.entity.LivingEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ModuleRealityVanillaNametags : Module("VanillaNametags", Category.REALITY) {

    @Suppress("LongParameterList", "CognitiveComplexMethod", "FunctionNaming", "MaxLineLength")
    fun RenderLabel(entity: Entity, text: Text, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, tickDelta: Float, textRenderer: TextRenderer, dispatcher: EntityRenderDispatcher) {
        var text: MutableText = text as MutableText;


        if (entity is LivingEntity) {
            val actualHealth = entity.getActualHealth().toInt()

            val healthColor = when {
                // Perhaps you should modify the values here
                actualHealth >= 14 -> Formatting.GREEN
                actualHealth >= 8 -> Formatting.YELLOW
                else -> Formatting.RED
            }
            text.append( withColor(" $actualHealth❤", healthColor))



            val absorptionAmount = entity.absorptionAmount.toInt()

            if (absorptionAmount > 0) {
                text.append(withColor(" $absorptionAmount\uD83D\uDEE1", Formatting.GOLD))
            }
        }


        val d: Double = dispatcher.getSquaredDistanceToCamera(entity)
        if (!(d > 4096.0)) {
            val vec3d = entity.attachments.getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getYaw(tickDelta))
            if (vec3d != null) {
                //val bl = !entity.isSneaky
                val bl = false;
                val i = if ("deadmau5" == text.string) -10 else 0
                matrices.push()
                matrices.translate(vec3d.x, vec3d.y + 0.5, vec3d.z)
                matrices.multiply(dispatcher.getRotation())
                matrices.scale(0.025f, -0.025f, 0.025f)

                var matrix4f = matrices.peek().positionMatrix
                val f = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f)
                val j = (f * 255.0f).toInt() shl 24
                val g = (-textRenderer.getWidth(text) / 2).toFloat()
                textRenderer.draw(
                    text,
                    g,
                    i.toFloat(),
                    553648127,
                    false,
                    matrix4f,
                    vertexConsumers,
                    TextLayerType.SEE_THROUGH,
                    j,
                    light
                )


                if (bl) {
                    textRenderer.draw(
                        text,
                        g,
                        i.toFloat(),
                        -1,
                        false,
                        matrix4f,
                        vertexConsumers,
                        TextLayerType.NORMAL,
                        0,
                        light
                    )
                }

                matrices.pop()
            }
        }
    }


    /**
     * Should [ModuleNametags] render nametags above this [entity]?
     */
    @JvmStatic
    fun shouldRenderNametag(entity: Entity) = entity.shouldBeShown()
}
