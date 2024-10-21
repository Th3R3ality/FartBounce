package net.ccbluex.liquidbounce.features.module.modules.reality

import net.ccbluex.liquidbounce.config.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.PlayerStrideEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.reality.*
import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.BlockShapeEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.PlayerTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.Timer
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.regular
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.ccbluex.liquidbounce.utils.client.sendPacketSilently
import com.mojang.blaze3d.systems.RenderSystem

object ModuleRealitySilentTP : Module("silent tp", Category.REALITY, ) {


    override fun enable(){
        chat("silent tp")

        for (i in 0..10)
        {
            network.sendPacket(
                PlayerMoveC2SPacket.Full(
                    player.x + i * 0.08f,
                    player.y + 100f,
                    player.z,
                    player.yaw,
                    player.pitch,
                    true
                )
            )
        }


        RenderSystem.recordRenderCall {
            this.enabled = false
        }
    }


}
