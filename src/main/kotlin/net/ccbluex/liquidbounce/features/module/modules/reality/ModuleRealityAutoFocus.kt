package net.ccbluex.liquidbounce.features.module.modules.reality

import net.ccbluex.liquidbounce.event.events.TagEntityEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.antibot.ModuleAntiBot
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting

object ModuleRealityAutoFocus : Module("AutoFocus", Category.REALITY) {

    /**
     * This option will only focus the enemy on combat modules
     */
    private val combatOnly by boolean("CombatOnly", false)

    @Suppress("unused")
    private val tagEntityEvent = handler<TagEntityEvent> {
        if ((it.entity !is AbstractClientPlayerEntity || ModuleAntiBot.isBot(it.entity)) || isInFocus(it.entity)) {
            return@handler
        }

        if (this.combatOnly) {
            it.dontTarget()
        } else {
            it.ignore()
        }
    }

    /**
     * Check if [entity] is in your focus
     */
    private fun isInFocus(entity: AbstractClientPlayerEntity): Boolean {
        if (!enabled) {
            return false
        }

        val nameColor = entity.displayName?.style?.color

//        chat("Color " + nameColor!!.name)

        if (nameColor == null) return false

        when (nameColor) {
            TextColor.fromFormatting(Formatting.BLACK) -> return false
            TextColor.fromFormatting(Formatting.DARK_GRAY) -> return false
            TextColor.fromFormatting(Formatting.GRAY) -> return false
            TextColor.fromFormatting(Formatting.WHITE) -> return false
        }

//        chat("targetting [" + entity.displayName!!.string + "]")
        return true
    }
}
