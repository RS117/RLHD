package rs117.hd.areas

import com.google.gson.GsonBuilder
import rs117.hd.HdPlugin
import rs117.hd.data.area.Area
import rs117.hd.data.area.AreaTheme
import rs117.hd.data.environments.Area.*
import java.io.File


object GenerateAreas {

    fun initialize() {
        println("======== Starting ===========")

        val areaList : MutableList<Area> = emptyList<Area>().toMutableList()

        values().filter { it.name != "NONE" }.forEach {

            val area = Area()
            area.description = it.name

            area.rects = it.rects.toMutableList()

            val name = it.name.lowercase()

            if(hideOther.contains(it)) {
                area.isHideOtherRegions = true
            }

            area.theme = when {
                !snowKey.none { key -> name.contains(key, true) } -> AreaTheme.SNOW
                !hotKey.none { key -> name.contains(key, true) } -> AreaTheme.HOT
                else -> AreaTheme.NORMAL
            }

            areaList.add(area)
        }


        val json = GsonBuilder().setPrettyPrinting().create().toJson(areaList)

        File("areas-new.json").writeText(json.formatJson())

    }

    private val snowKey = listOf(
        "snow","snowy",
        "frozen_waste_plateau", "god_wars_dungeon"
    )

    private val hotKey = listOf(
        "desert", "al_kharid",
        "desert_mining_camp", "pyramid_plunder",
        "the_inferno","tzhaar",
        "karamja"
    )

     private fun String.formatJson() : String {
        return this.
            replace("\n" + "        \"minX"," \"minX").
            replace("\n" + "        \"minY"," \"minY").
            replace("\n" + "        \"maxX"," \"maxX").
            replace("\n" + "        \"maxY"," \"maxY").
            replace("\n" + "        \"plane"," \"plane").
            replace(": -1\n" + "      }"," : -1 }").
            replace("\"plane\": 1\n" + "      }","\"plane\": 1 }").
            replace(", \"plane\" : -1","")
     }

    private val hideOther = listOf(
        FOSSIL_ISLAND_UNDERWATER_AREA,
        LITHKREN_DUNGEON,
        PURO_PURO,
        REVENANT_CAVES,
        KARUULM_SLAYER_DUNGEON,
        BARROWS_TUNNELS,
        KEEP_LE_FAYE_INSTANCE,
        LUMBRIDGE_CASTLE_BASEMENT,
        TOB_ROOM_VERZIK,
        TOB_ROOM_VAULT,
        TOB_ROOM_SOTETSEG,
        TOB_ROOM_NYCOLAS,
        TOB_ROOM_BLOAT,
        TOB_ROOM_MAIDEN,
        TOB_ROOM_XARPUS,
        RANDOM_EVENT_PRISON_PETE,
        PYRAMID_PLUNDER,
        ENCHANTED_VALLEY,
        DEATHS_OFFICE,
        TARNS_LAIR,
        FISHER_KINGS_REALM,
        EVIL_BOB_ISLAND,
        NIGHTMARE_ZONE,
        DEATH_ALTAR,
        WRATH_ALTAR,
        NATURE_ALTAR,
        LAW_ALTAR,
        BODY_ALTAR,
        FIRE_ALTAR,
        EARTH_ALTAR,
        WATER_ALTAR,
        MIND_ALTAR,
        AIR_ALTAR,
        RANDOM_EVENT_DRILL_DEMON,
        TRUE_BLOOD_ALTAR,
        COSMIC_ALTAR,
        CHAOS_ALTAR,
        COSMIC_ENTITYS_PLANE,
        VER_SINHAZA,
        MISTHALIN_MYSTERY_MANOR,
        RANDOM_EVENT_CLASSROOM,
        RANDOM_EVENT_GRAVEDIGGER,
        BRAINDEATH_ISLAND,
        GAMES_ROOM,
        RATCATCHERS_HOUSE,
        TZHAAR,
        BARBARIAN_ASSAULT_WAITING_ROOMS,
        DORGESHKAAN,
        TOLNA_DUNGEON_ANGER,
        CASTLE_WARS_UNDERGROUND,
        HAM_HIDEOUT,
        LIZARDMAN_TEMPLE,
        KARAMJA_VOLCANO_DUNGEON,
        TOLNA_DUNGEON_FEAR,
        MOGRE_CAMP,
        GUTANOTH_CAVE,
        TUTORIAL_ISLAND_UNDERGROUND,
        TEMPLE_OF_THE_EYE,
        DESERT_TREASURE_PYRAMID,
        CANOE_CUTSCENE,
        CHAMBERS_OF_XERIC,
        LMS_ARENA_WILD_VARROCK,
        SOTE_LLETYA_ON_FIRE,
        HALLOWED_SEPULCHRE_FLOOR_2,
        HALLOWED_SEPULCHRE_LOBBY,
        HALLOWED_SEPULCHRE_FLOOR_4,
        HALLOWED_SEPULCHRE_FLOOR_3,
        HALLOWED_SEPULCHRE_FLOOR_5,
        HALLOWED_SEPULCHRE_FLOOR_1,
        MOTHERLODE_MINE,
        SORCERESSS_GARDEN,
        DS2_SHIPS,
        CLAN_HALL,
        TOLNA_DUNGEON_FEAR,
        TOLNA_DUNGEON_CONFUSION,
        KHARIDIAN_DESERT_INSTANCE,
        YANILLE_INSTANCE,
        FALADOR_INSTANCE,
        DRAYNOR_INSTANCE,
        FISHING_TRAWLER_INSTANCE,
        TEMPLE_TREAKING,
        WHITE_WOLF_MOUNTAIN_INSTANCE,
        TREE_GNOME_STRONGHOLD_INSTANCE,
        CAMDOZAAL,
        ISLE_OF_SOULS_TUTORIAL,
        TIRANNWN_INSTANCE,
        EAST_ARDOUGNE_INSTANCE,
        TUTORIAL_ISLAND_INSTANCE,
        KARAMJA_INSTANCE,
        CLAN_WARS_INSTANCE,
        SINHAZA_CUTSCENE,
        MOGRE_CAMP_CUTSCENE,
        SOTE_TEMPLE_OF_LIGHT_SEREN_CUTSCENE
    )

}

fun main() {
    GenerateAreas.initialize()
}