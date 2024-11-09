package net.ccbluex.liquidbounce.features.module.modules.reality

import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.chat

object ModuleRealityStaffDetector : Module("Sniffer", Category.REALITY) {

    val repeatable = repeatable {
        waitTicks(20)
        chat("repeatable")
    }

}
