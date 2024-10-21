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

object ModuleRealityDimensionWalkFinalize : Module("4th dimension finalize", Category.REALITY, ) {


    // override fun init(){

    var autodisabled: Boolean = false
    var tooManyAttemps: Boolean = false

    // }
    //disableOnQuit = true

    override fun enable(){
        if (ModuleRealityDimensionWalkBegin.enabled == false)
        {
            chat("enabling dimension walk")
            ModuleRealityDimensionWalkBegin.enabled = true
            // RenderSystem.recordRenderCall {
            //     this.enabled = false
            // }
            // return
        }

        autodisabled = false
        tooManyAttemps = false

        ModuleRealityDimensionWalkBegin.finalizeAttempts = 0
        ModuleRealityDimensionWalkBegin.flags = 0
        ModuleRealityDimensionWalkBegin.flagPos = null
        ModuleRealityDimensionWalkBegin.begin = true
        ModuleRealityDimensionWalkBegin.shouldFinalize = false
    }

     override fun disable(){

        if (autodisabled)
        {
            autodisabled = false
            chat("autowarped")
        }
        else if(tooManyAttemps)
        {
            tooManyAttemps = false
            chat("tooManyAttemps")
        }
        else if (ModuleRealityDimensionWalkBegin.flags > 2){
            ModuleRealityDimensionWalkBegin.shouldFinalize = true
            chat("warping")
        }
        else
        {
            ModuleRealityDimensionWalkBegin.flags = 0
            ModuleRealityDimensionWalkBegin.flagPos = null
            ModuleRealityDimensionWalkBegin.begin = false
            ModuleRealityDimensionWalkBegin.shouldFinalize = false
            chat("nevermind")
        }
     }

}
