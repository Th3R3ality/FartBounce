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
import net.ccbluex.liquidbounce.features.module.modules.movement.fly.ModuleFly
import net.ccbluex.liquidbounce.features.module.modules.movement.fly.modes.*

object ModuleRealityDimensionWalk : Module("Dimension Walk", Category.REALITY, ) {

    // 2.5 is the maximum timer tested.
    private val timer by float("Timer", 1f, 1f..2.5f)

    var flags = 0
    var flagPos: Vec3d? = null
    private var successful: Boolean = false

    override fun enable(){
        flags = 0
        flagPos = null
        successful = false
        chat("Shifting space and time")
    }

    override fun disable(){
        if (successful)
        {
            chat("Get #themethod @ WiZARDHAX.COM")
        }
        else
        {
            chat("Disposition Cancelled")
        }

    }

    val tickHandler = handler<PlayerTickEvent> {
        if (flags > 1) {
            Timer.requestTimerSpeed(timer, Priority.NORMAL, ModuleRealityDimensionWalk, 1)
        }
    }

     val packetHandler = handler<PacketEvent> {
        val packet = it.packet

        if (packet is PlayerPositionLookS2CPacket) {
            flags++

            val pos = Vec3d(packet.x, packet.y, packet.z)
            if (flags == 2) {
                flagPos = pos
            } else if (flags > 2) {

                if (flagPos != pos){
                    flags = 0
                    flagPos = null

                    successful = true
                    this.enabled = false

                    return@handler
                }
            }

            it.cancelEvent()
        }


        if (ModuleFly.enabled && flags > 2 && packet is PlayerMoveC2SPacket) {
            {
                it.cancelEvent()
            }
        }
    }

    val shapeHandler = handler<BlockShapeEvent> { event ->
        if (event.pos == player.blockPos.down() && !player.isSneaking) {
            event.shape = VoxelShapes.fullCube()
        } else {
            event.shape = VoxelShapes.empty()
        }
    }
}
