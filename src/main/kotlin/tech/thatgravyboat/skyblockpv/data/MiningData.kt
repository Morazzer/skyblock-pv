package tech.thatgravyboat.skyblockpv.data

data class MiningCore(
    val nodes: Map<String, Int>,
    val crystals: Map<String, Crystal>,
    val experience: Long,
    val powderMithril: Int,
    val powderSpentMithril: Int,
    val powderGemstone: Int,
    val powderSpentGemstone: Int,
    val powderGlacite: Int,
    val powderSpentGlacite: Int,
)

data class Crystal(
    val state: String,
    val totalPlaced: Int,
    val totalFound: Int,
)

data class Forge(
    val slots: Map<Int, ForgeSlot>,
)

data class ForgeSlot(
    val type: String,
    val id: String,
    val startTime: Long,
    val notified: Boolean,
    val oldItem: Any?, // No clue what this is, in my forge it's always null. Maybe ItemStack or just id? Probably id
)
