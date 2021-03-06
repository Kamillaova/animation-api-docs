import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object Recharge {

    private lateinit var line: RectangleElement
    private lateinit var content: TextElement

    private var cooldown: RectangleElement? = null

    private var added = false

    init {
        var time = 0.0
        var currentTime = System.currentTimeMillis()

        registerHandler<GameLoop> {
            if (System.currentTimeMillis() - currentTime > 1000) {
                time--
                currentTime = System.currentTimeMillis()
            }
        }

        App::class.mod.registerChannel("func:recharge") {
            if (!added || cooldown == null) {
                cooldown = rectangle {
                    offset.y -= 65
                    origin = BOTTOM
                    align = BOTTOM
                    size = V3(180.0, 5.0, 0.0)
                    color = Color(0, 0, 0, 0.62)
                    line = +rectangle {
                        origin = LEFT
                        align = LEFT
                        size = V3(180.0, 5.0, 0.0)
                        color = Color(42, 102, 189, 0.62)
                    }
                    content = +text {
                        origin = TOP
                        align = TOP
                        color = WHITE
                        shadow = true
                        content = "Загрузка..."
                        offset.y -= 15
                    }
                    enabled = false
                }
            }
            time = this.readDouble()
            val text = NetUtil.readUtf8(this)
            line.color = Color(readInt(), readInt(), readInt(), 1.0)
            
            if (time == 0.0) {
                line.size.x = 0.0
                cooldown?.enabled = false
                return@registerChannel
            }

            cooldown?.enabled = true
            content.content = text
            line.animate(time - 0.1) {
                size.x = 0.0
            }
            UIEngine.schedule(time) {
                cooldown?.enabled = false
                line.size.x = 180.0
            }
        }
        if (!added) {
            UIEngine.overlayContext + cooldown!!
            added = true
        }
    }
}
