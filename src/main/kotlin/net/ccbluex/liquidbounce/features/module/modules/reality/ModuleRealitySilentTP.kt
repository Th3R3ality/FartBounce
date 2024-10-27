package net.ccbluex.liquidbounce.features.module.modules.reality

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat

object ModuleRealitySilentTP : Module("SilentTranspose", Category.REALITY, ) {

    var moveOffset: Float = 0f

    override fun enable(){

        if (ModuleRealityDimensionWalk.enabled)
        {
            if ( ModuleRealityDimensionWalk.flags > 2)
            {
                chat("Attempting Transposition")
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
    }

    override fun disable(){

    }
}
