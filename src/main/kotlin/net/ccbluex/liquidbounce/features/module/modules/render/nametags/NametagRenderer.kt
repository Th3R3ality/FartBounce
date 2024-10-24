/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.render.nametags

import com.mojang.blaze3d.systems.RenderSystem
import net.ccbluex.liquidbounce.render.*
import net.ccbluex.liquidbounce.render.engine.Color4b
import net.ccbluex.liquidbounce.render.engine.Vec3
import net.ccbluex.liquidbounce.utils.client.asText
import net.ccbluex.liquidbounce.render.engine.font.FontRenderer
import net.ccbluex.liquidbounce.render.engine.font.FontRendererBuffers
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.item.toRegistryEntry
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.VertexFormat
import net.minecraft.item.ItemStack
import net.minecraft.item.AirBlockItem
import org.lwjgl.opengl.GL11
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.*
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.registry.*
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.enchantment.*
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier

private const val NAMETAG_PADDING: Int = 5
private const val ITEM_SIZE: Int = 20
private const val ITEM_SCALE: Float = 1.0F

class NametagRenderer {
    private val quadBuffers =
        RenderBufferBuilder(
            VertexFormat.DrawMode.QUADS,
            VertexInputType.Pos,
            RenderBufferBuilder.TESSELATOR_A,
        )
    private val lineBuffers =
        RenderBufferBuilder(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexInputType.Pos,
            RenderBufferBuilder.TESSELATOR_B,
        )

    private val fontBuffers = FontRendererBuffers()

    fun drawNametag(
        env: RenderEnvironment,
        info: NametagInfo,
        pos: Vec3,
    ) = with(env) {
        val c = Fonts.DEFAULT_FONT_SIZE.toFloat()

        val scale = 1.0F / (c * 0.15F) * ModuleNametags.scale

        matrixStack.push()
        matrixStack.translate(pos.x, pos.y, pos.z)
        matrixStack.scale(scale, scale, 1.0F)

        val x =
            ModuleNametags.fontRenderer.draw(
                ModuleNametags.fontRenderer.process(info.text),
                0.0F,
                0.0F,
                shadow = true,
                z = 0.001F,
            )

        // Make the model view matrix center the text when rendering
        matrixStack.translate(-x * 0.5F, -ModuleNametags.fontRenderer.height * 0.5F, 0.00F)

        ModuleNametags.fontRenderer.commit(env, fontBuffers)

        val q1 = Vec3(-0.1F * c, ModuleNametags.fontRenderer.height * -0.1F, 0.0F)
        val q2 = Vec3(x + 0.2F * c, ModuleNametags.fontRenderer.height * 1.1F, 0.0F)

        quadBuffers.drawQuad(env, q1, q2)

        if (ModuleNametags.border) {
            lineBuffers.drawQuadOutlines(env, q1, q2)
        }

        matrixStack.pop()

        if (ModuleNametags.items.enabled) {
            drawItemList(env, pos, info.hands, info.equipped)
        }

    }

    private fun drawItemList(
        env: RenderEnvironment,
        pos: Vec3,
        handsItems: List<ItemStack?>,
        equippedItems: List<ItemStack?>,
    ) = with(env) {
        val totalItemsCount = handsItems.size + equippedItems.size

        val width = totalItemsCount * ITEM_SIZE
        val height = ITEM_SIZE

        val dc = DrawContext(
            mc,
            mc.bufferBuilders.entityVertexConsumers
        )

        val itemScale = ITEM_SCALE * ModuleNametags.scale
        dc.matrices.translate(pos.x, pos.y - NAMETAG_PADDING, pos.z)
        dc.matrices.scale(itemScale, itemScale, 1.0F)
        dc.matrices.translate(-width / 2.0F, -ITEM_SIZE.toFloat(), pos.z)

        if (ModuleNametags.items.itemsBackground)
        {
            // draw background
            dc.fill(
                -itemScale.toInt() - NAMETAG_PADDING/2,
                -itemScale.toInt() - NAMETAG_PADDING/2,
                width + itemScale.toInt() - NAMETAG_PADDING/2,
                height + itemScale.toInt() - NAMETAG_PADDING/2,
                Color4b(0, 0, 0, 128).toRGBA()
            )
        }

        dc.matrices.translate(0.0F, 0.0F, 100.0F)

        val c = ModuleNametags.fontRenderer.size
        val fontScale = 1.0F / (c * 0.15F) * ModuleNametags.scale

        // sync x pos between item and count
        fun scale(f: Int) = f * itemScale / fontScale

        matrixStack.push()
        matrixStack.translate(pos.x, pos.y, pos.z)
        matrixStack.scale(fontScale, fontScale, 1.0F)
        matrixStack.translate(-scale(width) / 2 - ITEM_SIZE / 2, -scale(height) - ITEM_SIZE / 2, pos.z)

        for (idx in 0..totalItemsCount)
        {
            var itemStack =
                if (idx < handsItems.size) {
                    handsItems.getOrNull(idx)
                }
                else {
                    equippedItems.getOrNull(idx - handsItems.size)
            }

            if (itemStack == null || itemStack.isEmpty()) {
                continue
            }
            val leftX = idx * ITEM_SIZE

            dc.drawItem(
                itemStack,
                leftX,
                0,
            )

            if (!ModuleNametags.items.itemCount)
                continue

            if (itemStack!!.count > 1)
            {
                val text = ModuleNametags.fontRenderer.process(itemStack.count.toString().asText())

                ModuleNametags.fontRenderer.draw(
                    text,
                    scale(leftX + ITEM_SIZE - ITEM_SIZE/5),
                    scale(ITEM_SIZE - ITEM_SIZE/5) - ModuleNametags.fontRenderer.height,
                    shadow = true,
                )
            }

            if (!ModuleNametags.items.showEnchants.enabled)
                continue

            val enchants = itemStack!!.enchantments


            val protID = if (ModuleNametags.items.showEnchants.capitalEnchants) {"P"} else {"p"}
            val protLevel = enchants.getLevel(Enchantments.PROTECTION.toRegistryEntry())
            if (protLevel > 1)
            {
                val text = ModuleNametags.fontRenderer.process(protID + protLevel.toString())

                ModuleNametags.fontRenderer.draw(
                    text,
                    scale(leftX + ITEM_SIZE - ITEM_SIZE/5),
                    scale(ITEM_SIZE/5) - ModuleNametags.fontRenderer.height,
                    shadow = true,
                )
                continue
            }

            val sharpID = if (ModuleNametags.items.showEnchants.capitalEnchants) {"S"} else {"s"}
            val sharpLevel = enchants.getLevel(Enchantments.SHARPNESS.toRegistryEntry())
            if (sharpLevel > 1)
            {
                val text = ModuleNametags.fontRenderer.process(sharpID + sharpLevel.toString())

                ModuleNametags.fontRenderer.draw(
                    text,
                    scale(leftX + ITEM_SIZE - ITEM_SIZE/5),
                    scale(ITEM_SIZE/5) - ModuleNametags.fontRenderer.height,
                    shadow = true,
                )
                continue
            }
        }

        ModuleNametags.fontRenderer.commit(fontBuffers)
        matrixStack.pop()
    }

    fun commit(env: RenderEnvironment) {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glEnable(GL11.GL_DEPTH_TEST)

        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )

        env.withColor(Color4b(0, 0, 0, 120)) {
            quadBuffers.draw()
        }
        env.withColor(Color4b(0, 0, 0, 255)) {
            lineBuffers.draw()
        }
        env.withColor(Color4b.WHITE) {
            fontBuffers.draw(ModuleNametags.fontRenderer)
        }
    }
}
