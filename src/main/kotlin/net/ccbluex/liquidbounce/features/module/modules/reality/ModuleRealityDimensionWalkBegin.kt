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

object ModuleRealityDimensionWalkBegin : Module("4th dimension walk", Category.REALITY, ) {

    // 2.5 is the maximum timer tested.
    private val timer by float("Timer", 1f, 1f..2.5f)

    var flags = 0
    var tpOffset: Float = 0.05f
    var shouldFinalize: Boolean = false
    var flagPos: Vec3d? = null
    var begin: Boolean = false
    private var ourPackets: Boolean = false
    var finalizeAttempts: Int = 0

    override fun enable(){
        flags = 0
        flagPos = null
        begin = false
        finalizeAttempts = 0
        chat("Get Wurst at WiZARDHAX.COM")
        //chat("shifting space and time")
    }

    override fun disable(){
        ModuleRealityDimensionWalkFinalize.enabled = false
    }

    // val tickHandler = handler<PlayerTickEvent> {
    //     if (flags > 1) {
    //         Timer.requestTimerSpeed(timer, Priority.NORMAL, ModuleRealityDimensionWalkBegin, 1)
    //     }
    // }

     val packetHandler = handler<PacketEvent> {
        if (!begin)
        {
            return@handler
        }

        val packet = it.packet

        if (packet is PlayerPositionLookS2CPacket) {
            flags++

            val pos = Vec3d(packet.x, packet.y, packet.z)
            if (flags == 1)
            {
                chat("flag 1")
            }
            if (flags == 2) {
                flagPos = pos
                chat("set pos")
            } else if (flags > 2) {

                if (shouldFinalize){
                    chat("finalizing?")
                    finalizeAttempts++

                    network.sendPacket(
                        PlayerMoveC2SPacket.Full(
                            player.x + tpOffset,
                            player.y,
                            player.z,
                            player.yaw,
                            player.pitch,
                            true
                        )
                    )
                    tpOffset += 0.1f
                    if (tpOffset > 1f)
                    {
                        tpOffset -= 1f
                    }

                    if (finalizeAttempts > 10)
                    {
                        begin = false
                        shouldFinalize = false
                        finalizeAttempts = 0
                        flags = 0
                        flagPos = null
                        ModuleRealityDimensionWalkFinalize.tooManyAttemps = true
                        ModuleRealityDimensionWalkFinalize.enabled = false
                        return@handler
                    }
                }

                if (flagPos != pos){
                    chat("tp'ed")

                    flags = 0
                    flagPos = null
                    begin = false
                    shouldFinalize = false
                    finalizeAttempts = 0
                    if (ModuleRealityDimensionWalkFinalize.enabled){
                        ModuleRealityDimensionWalkFinalize.autodisabled = true
                        ModuleRealityDimensionWalkFinalize.enabled = false
                    }
                    return@handler
                }
            }

            it.cancelEvent()
        }


        if (packet is PlayerMoveC2SPacket && flags > 2) {
            if (shouldFinalize)
            {
                packet.y += 100f
                chat("finalize modify move packet")
            }
            else if (ModuleFly.enabled)
            {
                chat("cancelling move packet")
                it.cancelEvent()
            }
            chat("pmove")
        }
    }

    val shapeHandler = handler<BlockShapeEvent> { event ->
        if (!begin) return@handler

        if (event.pos == player.blockPos.down() && !player.isSneaking) {
            event.shape = VoxelShapes.fullCube()
        } else {
            event.shape = VoxelShapes.empty()
        }
    }
}
