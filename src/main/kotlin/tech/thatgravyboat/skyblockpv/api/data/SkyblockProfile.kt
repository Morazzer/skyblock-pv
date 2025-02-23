package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.Util
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.data.MobData
import tech.thatgravyboat.skyblockpv.data.SlayerTypeData
import tech.thatgravyboat.skyblockpv.utils.*
import java.util.*

data class SkyblockProfile(
    val selected: Boolean,
    val id: ProfileId,
    val profileType: ProfileType = ProfileType.UNKNOWN,

    val skill: Map<String, Long> = emptyMap(),
    val collections: List<CollectionItem>,
    val mobData: List<MobData>,
    val slayer: Map<String, SlayerTypeData>,
) {
    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyblockProfile? {
            val member = json.getAsJsonObject("members").getAsJsonObject(user.toString().replace("-", "")) ?: return null
            val playerStats = member.getAsJsonObject("player_stats") ?: return null
            val playerData = member.getAsJsonObject("player_data") ?: return null
            val slayerData = member.getAsJsonObject("slayer") ?: return null

            return SkyblockProfile(
                selected = json["selected"].asBoolean(false),
                id = ProfileId(
                    id = json["profile_id"].asUUID(Util.NIL_UUID),
                    name = json["cute_name"].asString("Unknown"),
                ),

                profileType = json.get("game_mode")?.asString.let {
                    when (it) {
                        "ironman" -> ProfileType.IRONMAN
                        "island" -> ProfileType.STRANDED
                        "bingo" -> ProfileType.BINGO
                        else -> ProfileType.NORMAL
                    }
                },

                skill = playerData["experience"].asMap { id, amount -> id to amount.asLong(0) },

                collections = run {
                    val playerCollections = member["collection"].asMap { id, amount -> id to amount.asLong(0) }
                    CollectionCategory.entries.flatMap { it.collections.toList() }.map {
                        it to (playerCollections[it] ?: 0)
                    }.mapNotNull { (id, amount) ->
                        CollectionCategory.getCategoryByItemName(id)?.let {
                            CollectionItem(it, id, SkyBlockItems.getItemById(id), amount)
                        }
                    }
                },

                mobData = run {
                    val deaths = playerStats["deaths"].asMap { id, amount -> id to amount.asLong(0) }
                    val kills = playerStats["kills"].asMap { id, amount -> id to amount.asLong(0) }

                    (deaths.keys + kills.keys).map { id ->
                        MobData(
                            mobId = id,
                            kills = kills[id] ?: 0,
                            deaths = deaths[id] ?: 0,
                        )
                    }
                },

                slayer = run {
                    slayerData["slayer_bosses"].asMap { name, data ->
                        val data = data.asJsonObject
                        name to SlayerTypeData(
                            exp = data["xp"].asLong(0),
                            bossAttemptsTier = (0..4).associateWith { tier ->
                                data["boss_attempts_tier_$tier"].asInt(0)
                            },
                            bossKillsTier = (0..4).associateWith { tier ->
                                data["boss_kills_tier_$tier"].asInt(0)
                            },
                        )
                    }
                },
            )
        }
    }
}
