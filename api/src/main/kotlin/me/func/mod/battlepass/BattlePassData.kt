package me.func.mod.battlepass

import me.func.protocol.battlepass.BattlePassFacade
import me.func.protocol.battlepass.BattlePassPage
import org.bukkit.entity.Player
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

data class BattlePassData(
    var uuid: UUID = UUID.randomUUID(),
    var facade: BattlePassFacade = BattlePassFacade(),
    var pages: MutableList<BattlePassPageAdvanced> = mutableListOf(),
    var quests: MutableList<String> = mutableListOf(),
    var questStatusUpdater: Function<Player, List<String>>? = null,
    var buyAdvanced: Consumer<Player>? = null,
    var buyPage: BiConsumer<Player, Int>? = null,
) {
    constructor(init: BattlePassData.() -> Unit) : this() { this.init() }

    data class Builder(val battlepass: BattlePassData = BattlePassData()) {

        fun onBuyAdvanced(consumer: Consumer<Player>) = apply { battlepass.buyAdvanced = consumer }
        fun onBuyPage(consumer: BiConsumer<Player, Int>) = apply { battlepass.buyPage = consumer }
        fun price(price: Int) = apply { battlepass.facade.price = price }
        fun salePercent(percent: Double) = apply { battlepass.facade.salePercent = percent }
        fun tags(vararg tags: String) = apply { battlepass.facade.tags.addAll(tags) }
        fun page(page: BattlePassPageAdvanced) = apply { battlepass.pages.add(page) }
        fun pages(vararg pages: BattlePassPageAdvanced) = apply { battlepass.pages.addAll(pages) }

        fun build() = battlepass
    }
}