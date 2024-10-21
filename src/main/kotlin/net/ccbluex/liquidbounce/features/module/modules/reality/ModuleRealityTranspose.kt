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
package net.ccbluex.liquidbounce.features.module.modules.reality

import net.ccbluex.liquidbounce.config.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.PlayerStrideEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.reality.*
import com.mojang.blaze3d.systems.RenderSystem

import net.ccbluex.liquidbounce.utils.client.chat

object ModuleRealityTranspose : Module("Transpose", Category.REALITY, ) {

    override fun enable(){
        if (ModuleRealityDimensionWalk.enabled)
        {
            if ( ModuleRealityDimensionWalk.flags > 2)
            {
                chat("Transposing")
                player.updatePosition(player.x, player.y + 100f, player.z)
            }
            else
            {
                chat("Wormhole hasn't opened yet...")
            }
        }
        else
        {
            chat("Please enable Dimension Walk™ before transposing")
        }
        RenderSystem.recordRenderCall {
            this.enabled = false
        }
    }
}
