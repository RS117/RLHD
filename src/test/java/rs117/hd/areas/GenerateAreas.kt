package rs117.hd.areas

import com.google.gson.GsonBuilder
import org.apache.commons.lang3.StringUtils
import rs117.hd.data.WaterType
import rs117.hd.data.area.Area
import rs117.hd.data.area.AreaTheme
import rs117.hd.data.area.effects.*
import rs117.hd.data.environments.Area.*
import java.io.File


object GenerateAreas {

    fun initialize() {
        println("======== Starting ===========")

        val areaList : MutableList<Area> = emptyList<Area>().toMutableList()

        values().filter { it.name != "NONE" }.forEach {

            val currentArea = it
            val area = Area()
            area.description = it.name

            val rects : MutableList<IntArray> = emptyList<IntArray>().toMutableList()

            it.rects.forEach {
                val plane = if(it.plane == -1) 0 else it.plane
                if(plane == 0) {
                    rects.add(arrayOf(it.minX,it.maxY,it.maxX,it.minY).toIntArray())
                } else {
                    rects.add(arrayOf(it.minX,it.maxY,it.maxX,it.minY,plane).toIntArray())
                }
            }

            area.rects = rects


            val name = it.name.lowercase()

            if(hideOther.contains(it)) {
                area.isHideOtherRegions = true
            }

            area.theme = when {
                !snowKey.none { key -> name.contains(key, true) } -> AreaTheme.SNOW
                !hotKey.none { key -> name.contains(key, true) } -> AreaTheme.HOT
                else -> AreaTheme.NORMAL
            }

            val overlays = emptyList<TileData>().toMutableList()

            rs117.hd.data.materials.Overlay.values().filter { d -> d.area == currentArea }.forEach { data ->
                val overlay = TileData()
                overlay.id = data.id
                if (data.waterType != WaterType.NONE) {
                    overlay.waterType = data.waterType
                }
                overlay.groundMaterial = data.groundMaterial
                overlay.hue = data.hue
                overlay.isBlended = data.isBlended
                overlay.isBlendedAsUnderlay = data.isBlendedAsUnderlay
                overlay.lightness = data.lightness
                overlay.saturation = data.saturation
                overlay.shiftHue = data.shiftHue
                overlay.shiftLightness = data.shiftLightness
                overlay.shiftSaturation = data.shiftSaturation
                overlays.add(overlay)
            }

            val underlays = emptyList<TileData>().toMutableList()

            rs117.hd.data.materials.Underlay.values().filter { d -> d.area == currentArea }.forEach { data ->
                val underlay = TileData()
                underlay.id = data.id
                if (data.waterType != WaterType.NONE) {
                    underlay.waterType = data.waterType
                }
                underlay.groundMaterial = data.groundMaterial
                underlay.hue = data.hue
                underlay.isBlended = data.isBlended
                underlay.isBlendedAsUnderlay = data.isBlendedAsOverlay
                underlay.lightness = data.lightness
                underlay.saturation = data.saturation
                underlay.shiftHue = data.shiftHue
                underlay.shiftLightness = data.shiftLightness
                underlay.shiftSaturation = data.shiftSaturation
                underlays.add(underlay)
            }

            rs117.hd.data.environments.Environment.values().filter { d -> d.area == currentArea }.forEach { data ->
                val environment = Environment()

                val fog = Fog()
                fog.fogColor = data.fogColor1
                fog.fogDepth = data.fogDepthBefore
                fog.groundFogOpacity = data.groundFogOpacity
                fog.groundFogEnd = data.groundFogEnd
                fog.groundFogOpacity = data.groundFogOpacity
                fog.groundFogStart = data.groundFogStart
                fog.isCustomFogDepth = data.isCustomFogDepth

                val caustics = Caustics()

                caustics.isUnderwater = data.isUnderwater
                if(data.underwaterCausticsStrength != data.directionalStrength) {
                    caustics.underwaterCausticsStrength = data.underwaterCausticsStrength
                }


                val light = Lighting()

                light.ambientStrength = data.ambientStrength
                light.isCustomAmbientColor = data.isCustomAmbientStrength
                light.ambientColor = data.ambientColor1
                light.isCustomAmbientColor = data.isCustomAmbientColor
                light.directionalStrength = data.directionalStrength
                light.isCustomDirectionalStrength = data.isCustomDirectionalStrength
                light.directionalColor = data.directionalColor1
                light.isCustomDirectionalColor = data.isCustomDirectionalColor
                light.underglowStrength = data.underglowStrength
                light.underglowColor = data.underglowColor1
                light.lightPitch = data.lightPitch
                light.lightYaw = data.lightYaw

                environment.isAllowSkyOverride = data.isAllowSkyOverride
                environment.isLightningEnabled = data.isLightningEnabled

                environment.fog = fog
                environment.caustics = caustics
                environment.lighting = light


                area.environment = environment
            }


            area.overlays = overlays
            area.underlays = underlays

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
        return this.replace("\n" + "        \"hue\": -1,","").
        replace("\n" + "        \"shiftHue\": 0,","").
        replace("\n" + "        \"saturation\": -1,","").
        replace("\n" + "        \"lightness\": -1,","").
        replace("\n" + "        \"shiftSaturation\": 0,","").
        replace("\n" + "        \"blended\": true,","").
        replace("\n" + "        \"blendedAsOverlay\": false,","").
        replace("\n" + "        \"blendedAsUnderlay\": false,","").
        replace("\n" + "    \"rectangles\": [],","").
        replace("\n" + "    \"underlays\": []","").
        replace(",\n" + "  },\n" + "  {","\n" + "  },\n" + "  {").
        replace(",\n" + "    \"underlays\": []","").
        replace("\n" + "    \"overlays\": []","").
        replace("\n" + "    \"children\": [],","").
        replace("\n" + "        \"shiftLightness\": 0","").
        replace(": true,,",": true,").
        replace(": false,,",": false,").
        replace("\n" + "      \"lightningEnabled\": false,","").
        replace("\n" + "      \"allowSkyOverride\": true,","").
        replace("\n" + "        \"customFogDepth\": true,","").
        replace("\n" + "        \"customFogColor\": false,","").
        replace("\n" + "        \"groundFogStart\": -200,","").
        replace("\n" + "        \"groundFogEnd\": -500,","").
        replace("\n" + "        \"groundFogOpacity\": 0.0,","").
        replace("\n" + "        \"groundFogOpacity\": 0.0","").
        replace("},,","},").
        replace("\"underwater\": false,","").
        replace("\"underwaterCausticsStrength\": 0.0","").
        replace("\"customAmbientColor\": false,","").
        replace("\"directionalStrength\": 4.0,","").
        replace("\"customDirectionalColor\": false,","").
        replace("\"underglowStrength\": 0.0,","").
        replace("\"customDirectionalStrength\": false,","").
        replace("\"lightYaw\": 55.0","").
        replace("\"directionalColor\": \"#FFFFFF\",","").
        replace("\"underglowColor\": \"#000000\",","").
        replace("\"lightPitch\": -128.0,","").
        replace("\"ambientStrength\": 1.0,","").
        replace("\"customAmbientStrength\": false,","").
        replace("\"ambientColor\": \"#97baff\",","").
        replace("\n" + "        \"customAmbientColor\": true,","").
        replace("\n" + "        \"customDirectionalStrength\": true,","").
        replace("\n" + "        \"customDirectionalColor\": true,","").
        replace("\n" + "        \"underwaterCausticsColor\": \"\",","${System.lineSeparator()}").
        replace("\"caustics\": {\n" + "        \n" + "        \n" + "      },","").
        replace("\"caustics\": {\n" + "        \n" + "        \"","\"caustics\": {\n        \"").
        replace("\n" + "      \"caustics\": {\n" + "      },","").
        replace(Regex("(?m)^[ \t]*\r?\n"), "").
        replace(Regex(",[\\n\\s\\t]*(?=[}\\]])"),"${System.lineSeparator()}      ").
        replace("      },\n" + "  {","  },\n" + "  {").
        replace(Regex("\\[\\d+(?:,\\\"\\d+\\\")\\]"),"").
        replace(",\n" + "        \"fogColor\": \"\"","").
        replace(",\n" + "      \"lighting\": {\n" + "      }","").
        replace(",\n" + "        \"customFogDepth\": false","").
        replace("\"fogDepth\": 65","").
        replace("\"caustics\": {\n" + "      },","").
        replace(",\n" + "      \n" + "      \"lighting\": {",",\n" + "      \"lighting\": {").
        replace("        ,","").
        replace(",\n" + "      \"caustics\": {\n" + "      }","").
        replace(Regex("(?m)^[ \t]*\r?\n"), "").
        replace("\"fog\": {\n" + "      }","").
        replace("\"environment\": {\n" + "      \n" + "    },","").
        replace(Regex("(?m)^[ \t]*\r?\n"), "").
        replace("\n" + "    \"environment\": {\n" + "    }","").
        replace("\"hideOtherRegions\": false,\n" + "  },","\"hideOtherRegions\": false\n" + "  },").
        replace("\"hideOtherRegions\": true,\n" + "  },","\"hideOtherRegions\": true\n" + "  },").
        replace("\"environment\": {\n" + "      ,","\"environment\": {" + "      ").
        replace(Regex("(?m)^[ \t]*\r?\n"), "").
        replace(Regex("(?m)^[ \t]*\r?\n"), "").
        replace(Regex(",[\\n\\s\\t]*(?=[}\\]])"),"${System.lineSeparator()}  ").
        replace(Regex("\\[\\d+(?:,\\\"\\d+\\\")\\]"),"")
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