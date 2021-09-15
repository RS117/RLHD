/*
 * Copyright (c) 2021, 117 <https://twitter.com/117scape>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package rs117.hd.environments;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Environment
{
    // Wilderness
    public final static Environment FROZEN_WASTE_PLATEAU = new Environment(Area.FROZEN_WASTE_PLATEAU, new Properties()
            .setFogColor("#252C37")
            .setFogDepth(800)
            .setAmbientStrength(0.4f)
            .setAmbientColor("#3B87E4")
            .setDirectionalStrength(0.9f)
            .setDirectionalColor("#8A9EB6"));
    public final static Environment WILDERNESS_HIGH = new Environment(Area.WILDERNESS_HIGH, new Properties()
            .setFogColor("#464449")
            .setFogDepth(300)
            .setAmbientStrength(0.5f)
            .setAmbientColor(215, 210, 210)
            .setDirectionalStrength(0.8f)
            .setDirectionalColor("#C5B8B6")
            .enableLightning()
            .setGroundFog(-0, -250, 0.3f));
    public final static Environment WILDERNESS_LOW = new Environment(Area.WILDERNESS_LOW, new Properties()
            .setFogColor("#A9A2B0")
            .setFogDepth(200)
            .setAmbientStrength(0.6f)
            .setAmbientColor(215, 210, 210)
            .setDirectionalStrength(1.0f)
            .setDirectionalColor(138, 158, 182));
    public final static Environment WILDERNESS = new Environment(Area.WILDERNESS, new Properties()
            .setFogColor("#695B6B")
            .setFogDepth(300)
            .setAmbientStrength(0.6f)
            .setAmbientColor(215, 210, 210)
            .setDirectionalStrength(1.0f)
            .setDirectionalColor("#C5B8B6")
            .setGroundFog(-0, -250, 0.3f));

    // Varrock
    public final static Environment VARROCK_MUSEUM_BASEMENT = new Environment(Area.VARROCK_MUSEUM_BASEMENT, new Properties()
            .setFogColor("#131B26")
            .setFogDepth(200)
            .setAmbientColor("#CBC2B2")
            .setAmbientStrength(1.0f)
            .setDirectionalColor("#4C78B6")
            .setDirectionalStrength(0.3f));

    // A Soul's Bane
    public final static Environment TOLNA_DUNGEON_ANGER = new Environment(Area.TOLNA_DUNGEON_ANGER, new Properties()
            .setFogColor("#290000")
            .setFogDepth(400)
            .setAmbientColor("#AE7D46")
            .setAmbientStrength(1.3f)
            .setDirectionalColor("#CB4848")
            .setDirectionalStrength(0.7f));
    public final static Environment TOLNA_DUNGEON_FEAR = new Environment(Area.TOLNA_DUNGEON_FEAR, new Properties()
            .setFogColor("#000B0F")
            .setFogDepth(400)
            .setAmbientColor("#77A0FF")
            .setAmbientStrength(1.3f)
            .setDirectionalColor("#4C78B6")
            .setDirectionalStrength(0.5f));
    public final static Environment TOLNA_DUNGEON_CONFUSION = new Environment(Area.TOLNA_DUNGEON_CONFUSION, new Properties()
            .setFogColor("#2E0C23")
            .setFogDepth(400)
            .setAmbientColor("#77A0FF")
            .setAmbientStrength(1.3f)
            .setDirectionalColor("#4E9DD0")
            .setDirectionalStrength(0.5f));

    // Inferno
    public final static Environment THE_INFERNO = new Environment(Area.THE_INFERNO, new Properties()
            .setUnderglowColor(255, 0, 0)
            .setUnderglowStrength(2f)
            .setFogColor(23, 11, 7)
            .setFogDepth(200)
            .setAmbientColor(240, 184, 184)
            .setAmbientStrength(1.7f)
            .setDirectionalColor(255, 246, 202)
            .setDirectionalStrength(0f));
    public final static Environment TZHAAR = new Environment(Area.TZHAAR, new Properties()
            .setFogColor("#1A0808")
            .setFogDepth(150)
            .setAmbientColor("#FFEACC")
            .setAmbientStrength(0.8f)
            .setDirectionalColor("#FFA400")
            .setDirectionalStrength(0.7f));

    public final static Environment BARROWS_CRYPTS = new Environment(Area.BARROWS_CRYPTS, new Properties()
            .setFogColor(0, 0, 0)
            .setFogDepth(200)
            .setAmbientColor(181, 143, 124)
            .setAmbientStrength(1.2f)
            .setDirectionalColor(255, 200, 117)
            .setDirectionalStrength(0.1f));
    public final static Environment BARROWS = new Environment(Area.BARROWS, new Properties()
            .setFogColor("#242D3A")
            .setFogDepth(500)
            .setAmbientColor("#5B83B3")
            .setAmbientStrength(1.4f)
            .setDirectionalColor("#526E8B")
            .setDirectionalStrength(0.3f)
            .enableLightning()
            .setGroundFog(-300, -500, 0.5f));
    public final static Environment MORYTANIA = new Environment(Area.MORYTANIA, new Properties()
            .setFogColor("#1E314B")
            .setFogDepth(400)
            .setAmbientColor("#5B83B3")
            .setAmbientStrength(0.7f)
            .setDirectionalColor("#526E8B")
            .setDirectionalStrength(2.0f)
            .setGroundFog(-150, -350, 0.5f));

    public final static Environment LUMBRIDGE = new Environment(Area.LUMBRIDGE, new Properties());

    public final static Environment DRAYNOR_MANOR = new Environment(Area.DRAYNOR_MANOR, new Properties()
            .setFogColor(15, 14, 13)
            .setFogDepth(300)
            .setAmbientColor("#615C57")
            .setAmbientStrength(1.5f)
            .setDirectionalColor("#FFBCB7")
            .setAmbientStrength(0.5f)
            .enableLightning());
    public final static Environment DRAYNOR_MANOR_BASEMENT = new Environment(Area.DRAYNOR_MANOR_BASEMENT, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor("#7891B5")
            .setAmbientStrength(1.0f)
            .setDirectionalColor(76, 120, 182)
            .setDirectionalStrength(0.0f));
    public final static Environment DRAYNOR = new Environment(Area.DRAYNOR, new Properties());

    public final static Environment FALADOR = new Environment(Area.FALADOR, new Properties());

    public final static Environment GAMES_ROOM = new Environment(Area.GAMES_ROOM, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(200)
            .setAmbientColor(162, 151, 148)
            .setAmbientStrength(0.3f)
            .setDirectionalColor(138, 158, 182)
            .setDirectionalStrength(0.1f));

    public final static Environment SOUL_WARS_RED_TEAM = new Environment(Area.SOUL_WARS_RED_BASE, new Properties()
            .setFogColor(28, 21, 13));
    public final static Environment TUTORIAL_SOUL_WARS_RED_TEAM = new Environment(Area.SOUL_WARS_RED_BASE_TUTORIAL, new Properties()
            .setFogColor(28, 21, 13));
    public final static Environment TUTORIAL_ISLE_OF_SOULS = new Environment(Area.ISLE_OF_SOULS_TUTORIAL, new Properties());

    public final static Environment SMOKE_DUNGEON = new Environment(Area.SMOKE_DUNGEON, new Properties()
            .setFogColor(0, 0, 0)
            .setFogDepth(800)
            .setAmbientColor(171, 171, 171)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(86, 86, 86)
            .setDirectionalStrength(0.2f));
    public final static Environment DUEL_ARENA = new Environment(Area.DUEL_ARENA, new Properties()
            .setAmbientStrength(0.8f)
            .setAmbientColor("#FFEEC4")
            .setDirectionalStrength(1.2f));
    public final static Environment SHANTAY_PASS = new Environment(Area.SHANTAY_PASS, new Properties()
            .setAmbientStrength(0.8f)
            .setAmbientColor("#FFEEC4")
            .setDirectionalStrength(1.2f));
    public final static Environment AL_KHARID = new Environment(Area.AL_KHARID, new Properties()
            .setAmbientStrength(0.8f)
            .setAmbientColor("#FFEEC4")
            .setDirectionalStrength(1.2f));
    public final static Environment EAST_AL_KHARID = new Environment(Area.EAST_AL_KHARID, new Properties()
            .setAmbientStrength(0.8f)
            .setAmbientColor("#FFEEC4")
            .setDirectionalStrength(1.2f));
    public final static Environment AL_KHARID_MINE = new Environment(Area.AL_KHARID_MINE, new Properties()
            .setAmbientStrength(0.8f)
            .setAmbientColor("#FFEEC4")
            .setDirectionalStrength(1.2f));
    public final static Environment KHARIDIAN_DESERT_DEEP = new Environment(Area.KHARIDIAN_DESERT_DEEP, new Properties()
            .setFogColor(213, 179, 117)
            .setFogDepth(800)
            .setAmbientColor(205, 185, 150)
            .setAmbientStrength(0.8f)
            .setDirectionalColor(200, 169, 115)
            .setDirectionalStrength(0.4f));
    public final static Environment KHARIDIAN_DESERT = new Environment(Area.KHARIDIAN_DESERT, new Properties()
            .setFogColor(200, 186, 152)
            .setFogDepth(500)
            .setAmbientColor(208, 185, 156)
            .setAmbientStrength(1.5f)
            .setDirectionalColor(181, 155, 121)
            .setDirectionalStrength(0.5f));
    public final static Environment DESERT_TREASURE_PYRAMID = new Environment(Area.DESERT_TREASURE_PYRAMID, new Properties()
            .setFogColor(39, 23, 4)
            .setFogDepth(400)
            .setAmbientColor(192, 159, 110)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(138, 158, 182)
            .setDirectionalStrength(0.1f));
    public final static Environment PYRAMID_PLUNDER = new Environment(Area.PYRAMID_PLUNDER, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor(181, 155, 121)
            .setAmbientStrength(0.6f)
            .setDirectionalColor(138, 158, 182)
            .setDirectionalStrength(0.1f));

    public final static Environment GIELINOR_SNOWY_NORTHERN_REGION = new Environment(Area.GIELINOR_SNOWY_NORTHERN_REGION, new Properties()
            .setFogColor(174, 189, 224)
            .setFogDepth(800)
            .setAmbientColor(59, 135, 228)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(255, 201, 104)
            .setDirectionalStrength(0.9f));

    public final static Environment WHITE_WOLF_MOUNTAIN = new Environment(Area.WHITE_WOLF_MOUNTAIN, new Properties());

    public final static Environment KEEP_LE_FAYE = new Environment(Area.KEEP_LE_FAYE, new Properties());

    public final static Environment MOUNTAIN_CAMP_ENTRY_PATH = new Environment(Area.MOUNTAIN_CAMP_ENTRY_PATH, new Properties()
            .setFogColor(178, 187, 197)
            .setFogDepth(500)
            .setAmbientStrength(0.9f)
            .setDirectionalStrength(0.3f)
            .setGroundFog(-600, -900, 0.4f));
    public final static Environment MOUNTAIN_CAMP = new Environment(Area.MOUNTAIN_CAMP, new Properties()
            .setFogColor(178, 187, 197)
            .setFogDepth(500)
            .setAmbientStrength(0.9f)
            .setDirectionalStrength(0.3f)
            .setGroundFog(-1200, -1600, 0.5f)
            .setGroundFog(-1200, -1600, 0.5f));
    public final static Environment FREMENNIK_PROVINCE = new Environment(Area.FREMENNIK_PROVINCE, new Properties()
            .setFogColor("#969CA2")
            .setFogDepth(400)
            .setAmbientStrength(0.9f)
            .setAmbientColor("#96A3CB")
            .setDirectionalStrength(0.8f)
            .setDirectionalColor("#ABC2D3")
            .setGroundFog(-200, -400, 0.3f));

    public final static Environment HAM_HIDEOUT = new Environment(Area.HAM_HIDEOUT, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor("#7891B5")
            .setAmbientStrength(1.3f)
            .setDirectionalColor(76, 120, 182)
            .setDirectionalStrength(0.0f));

    // Karamja
    public final static Environment KARAMJA_VOLCANO_DUNGEON = new Environment(Area.KARAMJA_VOLCANO_DUNGEON, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor("#7891B5")
            .setAmbientStrength(0.9f)
            .setDirectionalColor(76, 120, 182)
            .setDirectionalStrength(0.0f));
    public final static Environment KARAMJA = new Environment(Area.KARAMJA, new Properties());


    public final static Environment UNGAEL = new Environment(Area.UNGAEL, new Properties()
            .setFogColor(226, 230, 237)
            .setFogDepth(400)
            .setAmbientColor(234, 226, 205)
            .setAmbientStrength(0.6f)
            .setDirectionalColor(130, 172, 224)
            .setDirectionalStrength(0.6f));

    public final static Environment ROZEN_DOOR = new Environment(Area.FROZEN_DOOR, new Properties()
            .setFogColor(3, 16, 25)
            .setFogDepth(300)
            .setAmbientColor(200, 228, 247)
            .setAmbientStrength(0.3f)
            .setDirectionalColor(146, 209, 250)
            .setDirectionalStrength(0.3f)
            .enableLightning());
    public final static Environment GOD_WARS_DUNGEON = new Environment(Area.GOD_WARS_DUNGEON, new Properties()
            .setFogColor(14, 59, 89)
            .setFogDepth(300)
            .setAmbientColor("#68ACFF")
            .setAmbientStrength(0.5f)
            .setDirectionalColor(146, 209, 250)
            .setDirectionalStrength(0.7f));

    public final static Environment TAR_SWAMP = new Environment(Area.TAR_SWAMP, new Properties()
            .setFogColor(42, 49, 36)
            .setFogDepth(500)
            .setAmbientColor(248, 224, 172)
            .setAmbientStrength(0.8f)
            .setDirectionalColor(168, 171, 144)
            .setDirectionalStrength(0.5f));

    public final static Environment SOTE_LLETYA_SMALL_FIRES = new Environment(Area.SOTE_LLETYA_SMALL_FIRES, new Properties()
            .setFogColor(91, 139, 120)
            .setFogDepth(300)
            .setAmbientStrength(1.0f)
            .setDirectionalStrength(0.0f));
    public final static Environment SOTE_LLETYA_ON_FIRE = new Environment(Area.SOTE_LLETYA_ON_FIRE, new Properties()
            .setFogColor(91, 139, 120)
            .setFogDepth(500)
            .setAmbientStrength(0.9f)
            .setDirectionalStrength(0.0f));
    public final static Environment POSION_WASTE = new Environment(Area.POISON_WASTE, new Properties()
            .setFogColor(50, 55, 47)
            .setFogDepth(500)
            .setAmbientColor(192, 219, 173)
            .setAmbientStrength(0.8f)
            .setDirectionalColor(173, 176, 139)
            .setDirectionalStrength(0.4f));
    public final static Environment TIRANNWN = new Environment(Area.TIRANNWN, new Properties()
            .setFogColor("#99D8C8")
            .setFogDepth(150));
    public final static Environment PRIFDDINAS = new Environment(Area.PRIFDDINAS, new Properties()
            .setFogColor("#99D8C8")
            .setFogDepth(150));
    public final static Environment SOTE_GRAND_LIBRARY = new Environment(Area.SOTE_GRAND_LIBRARY, new Properties()
            .setFogColor(18, 64, 83)
            .setAmbientStrength(0.3f)
            .setDirectionalStrength(0.3f));
    public final static Environment SOTE_FRAGMENT_OF_SEREN_ARENA = new Environment(Area.SOTE_FRAGMENT_OF_SEREN_ARENA, new Properties()
            .setFogColor(0, 0, 0));

    // Yanille
    public final static Environment YANILLE = new Environment(Area.YANILLE, new Properties());
    // Nightmare Zone
    public final static Environment NIGHTMARE_ZONE = new Environment(Area.NIGHTMARE_ZONE, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor("#F2B979")
            .setAmbientStrength(0.9f)
            .setDirectionalColor("#97DDFF")
            .setDirectionalStrength(0.4f));

    // Tree Gnome Stronghold
    public final static Environment TREE_GNOME_STRONGHOLD = new Environment(Area.TREE_GNOME_STRONGHOLD, new Properties());

    // Castle Wars
    public final static Environment CASTLE_WARS_UNDERGROUND = new Environment(Area.CASTLE_WARS_UNDERGROUND, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor("#FFD79C")
            .setAmbientStrength(1.3f)
            .setDirectionalColor("#4C78B6")
            .setDirectionalStrength(0.0f));

    // Last Man Standing
    public final static Environment LMS_ARENA_WILD_VARROCK = new Environment(Area.LMS_ARENA_WILD_VARROCK, new Properties()
            .setFogColor("#695B6B")
            .setFogDepth(300)
            .setAmbientStrength(0.6f)
            .setAmbientColor(215, 210, 210)
            .setDirectionalStrength(1.0f)
            .setDirectionalColor("#C5B8B6")
            .setGroundFog(-0, -250, 0.3f));
    public final static Environment LMS_ARENA_DESERTED_ISLAND = new Environment(Area.LMS_ARENA_DESERTED_ISLAND, new Properties());

    // Zeah
    public final static Environment KOUREND_CATACOMBS = new Environment(Area.KOUREND_CATACOMBS, new Properties()
            .setFogColor("#0E0022")
            .setFogDepth(400)
            .setAmbientColor("#8B7DDB")
            .setAmbientStrength(1.5f)
            .setDirectionalColor(76, 120, 182)
            .setDirectionalStrength(0.0f));
    public final static Environment MOUNT_QUIDAMORTEM = new Environment(Area.MOUNT_QUIDAMORTEM, new Properties());
    public final static Environment KEBOS_LOWLANDS = new Environment(Area.KEBOS_LOWLANDS, new Properties()
            .setFogColor(41, 44, 16)
            .setFogDepth(500)
            .setAmbientColor(255, 215, 133)
            .setAmbientStrength(0.8f)
            .setDirectionalColor(207, 229, 181)
            .setDirectionalStrength(0.3f));
    public final static Environment BLOOD_ALTAR = new Environment(Area.BLOOD_ALTAR, new Properties()
            .setFogColor(79, 19, 37)
            .setFogDepth(300)
            .setAmbientColor(190, 72, 174)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(78, 238, 255)
            .setDirectionalStrength(1.0f));
    public final static Environment ZEAH_SNOWY_NORTHERN_REGION = new Environment(Area.ZEAH_SNOWY_NORTHERN_REGION, new Properties()
            .setFogColor(174, 189, 224)
            .setFogDepth(800)
            .setAmbientColor(59, 135, 228)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(255, 201, 104)
            .setDirectionalStrength(0.9f));
    public final static Environment ARCEUUS = new Environment(Area.ARCEUUS, new Properties()
            .setFogColor(19, 24, 79)
            .setFogDepth(300)
            .setAmbientColor(99, 105, 255)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(78, 238, 255)
            .setDirectionalStrength(1.0f));
    public final static Environment LOVAKENGJ = new Environment(Area.LOVAKENGJ, new Properties()
            .setFogColor(21, 10, 5)
            .setFogDepth(400)
            .setAmbientColor(255, 215, 133)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(125, 141, 179)
            .setDirectionalStrength(1.0f));

    // Zanaris
    public final static Environment COSMIC_ENTITYS_PLANE = new Environment(Area.COSMIC_ENTITYS_PLANE, new Properties()
            .setFogColor("#000000")
            .setAmbientStrength(1.5f)
            .setAmbientColor("#DB6FFF")
            .setDirectionalStrength(1.2f)
            .setDirectionalColor("#57FF00"));
    public final static Environment ZANARIS = new Environment(Area.ZANARIS, new Properties()
            .setFogColor(22, 63, 71)
            .setFogDepth(300)
            .setAmbientColor(115, 181, 195)
            .setAmbientStrength(0.3f)
            .setDirectionalColor(245, 214, 122)
            .setDirectionalStrength(0.3f));

    // Dragon Slayer II
    public final static Environment DS2_FLASHBACK_PLATFORM = new Environment(Area.DS2_FLASHBACK_PLATFORM, new Properties()
            .setFogColor(0, 0, 0)
            .setFogDepth(200)
            .setAmbientStrength(1.2f)
            .setAmbientColor(255, 255, 255)
            .setDirectionalStrength(0f));
    public final static Environment DS2_FLEET_ATTACKED = new Environment(Area.DS2_FLEET_ATTACKED, new Properties()
            .setFogColor("#FFD3C7")
            .setFogDepth(200)
            .setAmbientColor("#68ACFF")
            .setAmbientStrength(0.8f)
            .setDirectionalColor("#FF8700")
            .setDirectionalStrength(1.5f));
    public final static Environment DS2_SHIPS = new Environment(Area.DS2_SHIPS, new Properties()
            .setFogColor("#FFD3C7")
            .setFogDepth(200)
            .setAmbientColor("#68ACFF")
            .setAmbientStrength(0.8f)
            .setDirectionalColor("#FF8700")
            .setDirectionalStrength(1.5f));

    // The Gauntlet
    public final static Environment THE_GAUNTLET = new Environment(Area.THE_GAUNTLET, new Properties()
            .setFogColor("#090606")
            .setFogDepth(200)
            .setAmbientColor("#D2C0B7")
            .setAmbientStrength(1.2f)
            .setDirectionalColor("#78FFE3")
            .setDirectionalStrength(0.8f));
    public final static Environment THE_GAUNTLET_CORRUPTED = new Environment(Area.THE_GAUNTLET_CORRUPTED, new Properties()
            .setFogColor("#090606")
            .setFogDepth(200)
            .setAmbientColor("#95B6F7")
            .setAmbientStrength(1.2f)
            .setDirectionalColor("#FF7878")
            .setDirectionalStrength(0.8f));
    public final static Environment THE_GAUNTLET_LOBBY = new Environment(Area.THE_GAUNTLET_LOBBY, new Properties()
            .setFogColor("#090606")
            .setFogDepth(200)
            .setAmbientColor("#D2C0B7")
            .setAmbientStrength(1.2f)
            .setDirectionalColor("#78FFE3")
            .setDirectionalStrength(0.8f));

    // Islands
    public final static Environment BRAINDEATH_ISLAND = new Environment(Area.BRAINDEATH_ISLAND, new Properties());

    // Ape Atoll
    // Monkey Madness 2
    public final static Environment MM2_AIRSHIP_PLATFORM = new Environment(Area.MM2_AIRSHIP_PLATFORM, new Properties());

    // POHs
    public final static Environment PLAYER_OWNED_HOUSE_SNOWY = new Environment(Area.PLAYER_OWNED_HOUSE_SNOWY, new Properties()
            .setFogColor(174, 189, 224)
            .setFogDepth(500)
            .setAmbientColor(59, 135, 228)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(255, 201, 104)
            .setDirectionalStrength(0.9f));
    public final static Environment PLAYER_OWNED_HOUSE = new Environment(Area.PLAYER_OWNED_HOUSE, new Properties());

    // Blackhole
    public final static Environment BLACKHOLE = new Environment(Area.BLACKHOLE, new Properties()
            .setFogColor(0, 0, 0)
            .setFogDepth(200)
            .setAmbientStrength(1.2f)
            .setAmbientColor(255, 255, 255)
            .setDirectionalStrength(0f));

    // Fishing Trawler
    public final static Environment FISHING_TRAWLER = new Environment(Area.FISHING_TRAWLER, new Properties());

    // Camdozaal (Below Ice Mountain)
    public final static Environment CAMDOZAAL = new Environment(Area.CAMDOZAAL, new Properties()
            .setFogColor("#080012")
            .setFogDepth(400)
            .setAmbientStrength(1.5f)
            .setAmbientColor("#C9B9F7")
            .setDirectionalStrength(0f)
            .setDirectionalColor("#6DC5FF"));

    // Tempoross
    public final static Environment TEMPOROSS_COVE = new Environment(Area.TEMPOROSS_COVE, new Properties()
            .setFogColor("#45474B")
            .setFogDepth(600)
            .setAmbientStrength(0.9f)
            .setAmbientColor("#A5ACBD")
            .setDirectionalStrength(0.1f)
            .setDirectionalColor("#707070")
            .enableLightning());

    // Morytania
    // Hallowed Sepulchre
    public final static Environment HALLOWED_SEPULCHRE_LOBBY = new Environment(Area.HALLOWED_SEPULCHRE_LOBBY, new Properties()
            .setFogColor("#0D1012")
            .setFogDepth(500)
            .setAmbientStrength(0.7f)
            .setAmbientColor("#C4D5EA")
            .setDirectionalStrength(0.4f)
            .setDirectionalColor("#A0BBE2"));
    public final static Environment HALLOWED_SEPULCHRE_FLOOR_1 = new Environment(Area.HALLOWED_SEPULCHRE_FLOOR_1, new Properties()
            .setFogColor(17, 28, 26)
            .setFogDepth(500)
            .setAmbientStrength(0.9f)
            .setAmbientColor(155, 187, 177)
            .setDirectionalStrength(0.7f)
            .setDirectionalColor(117, 231, 255));
    public final static Environment HALLOWED_SEPULCHRE_FLOOR_2 = new Environment(Area.HALLOWED_SEPULCHRE_FLOOR_2, new Properties()
            .setFogColor(17, 28, 27)
            .setFogDepth(500)
            .setAmbientStrength(0.875f)
            .setAmbientColor(160, 191, 191)
            .setDirectionalStrength(0.675f)
            .setDirectionalColor(116, 214, 247));
    public final static Environment HALLOWED_SEPULCHRE_FLOOR_3 = new Environment(Area.HALLOWED_SEPULCHRE_FLOOR_3, new Properties()
            .setFogColor(18, 28, 29)
            .setFogDepth(500)
            .setAmbientStrength(0.85f)
            .setAmbientColor(165, 195, 205)
            .setDirectionalStrength(0.65f)
            .setDirectionalColor(115, 196, 240));
    public final static Environment HALLOWED_SEPULCHRE_FLOOR_4 = new Environment(Area.HALLOWED_SEPULCHRE_FLOOR_4, new Properties()
            .setFogColor(18, 27, 31)
            .setFogDepth(500)
            .setAmbientStrength(0.825f)
            .setAmbientColor(170, 199, 220)
            .setDirectionalStrength(0.625f)
            .setDirectionalColor(114, 178, 233));
    public final static Environment HALLOWED_SEPULCHRE_FLOOR_5 = new Environment(Area.HALLOWED_SEPULCHRE_FLOOR_5, new Properties()
            .setFogColor(19, 27, 33)
            .setFogDepth(500)
            .setAmbientStrength(0.8f)
            .setAmbientColor(175, 202, 234)
            .setDirectionalStrength(0.6f)
            .setDirectionalColor(113, 160, 226));

    // Theatre of Blood
    public final static Environment TOB_ROOM_VAULT = new Environment(Area.TOB_ROOM_VAULT, new Properties()
            .setFogColor("#0E081A")
            .setFogDepth(400)
            .setAmbientStrength(2.0f)
            .setAmbientColor("#7963C3")
            .setDirectionalStrength(0.0f)
            .setDirectionalColor("#FF6767"));
    public final static Environment THEATRE_OF_BLOOD = new Environment(Area.THEATRE_OF_BLOOD, new Properties()
            .setFogColor("#0E0C2C")
            .setFogDepth(400)
            .setAmbientStrength(2.0f)
            .setAmbientColor("#6D73FF")
            .setDirectionalStrength(1.2f)
            .setDirectionalColor("#FF6767"));

    // Chambers of Xeric
    public final static Environment CHAMBERS_OF_XERIC = new Environment(Area.CHAMBERS_OF_XERIC, new Properties()
            .setFogColor("#122717")
            .setFogDepth(350)
            .setAmbientStrength(1.2f)
            .setAmbientColor("#7897C3")
            .setDirectionalStrength(0.3f)
            .setDirectionalColor("#A4F065"));

    // Nightmare of Ashihama
    public final static Environment NIGHTMARE_OF_ASHIHAMA_ARENA = new Environment(Area.NIGHTMARE_OF_ASHIHAMA_ARENA, new Properties()
            .setFogColor("#000000")
            .setFogDepth(300)
            .setAmbientStrength(1.4f)
            .setAmbientColor("#9A5DFD")
            .setDirectionalStrength(0.8f)
            .setDirectionalColor("#00FF60"));

    // Underwater areas
    public final static Environment MOGRE_CAMP_CUTSCENE = new Environment(Area.MOGRE_CAMP_CUTSCENE, new Properties());
    public final static Environment MOGRE_CAMP = new Environment(Area.MOGRE_CAMP, new Properties()
            .setFogColor("#133156")
            .setFogDepth(800)
            .setAmbientStrength(0.5f)
            .setAmbientColor("#255590")
            .setDirectionalStrength(1.5f)
            .setDirectionalColor("#71A3D0")
            .setGroundFog(0, -500, 0.5f));
    public final static Environment HARMONY_ISLAND_UNDERWATER_TUNNEL = new Environment(Area.HARMONY_ISLAND_UNDERWATER_TUNNEL, new Properties()
            .setFogColor("#133156")
            .setFogDepth(800)
            .setAmbientStrength(2.0f)
            .setAmbientColor("#255590")
            .setDirectionalStrength(0.4f)
            .setDirectionalColor("#71A3D0")
            .setGroundFog(-800, -1100, 0.5f));
    public final static Environment FOSSIL_ISLAND_UNDERWATER_AREA = new Environment(Area.FOSSIL_ISLAND_UNDERWATER_AREA, new Properties()
            .setFogColor("#133156")
            .setFogDepth(800)
            .setAmbientStrength(0.5f)
            .setAmbientColor("#255590")
            .setDirectionalStrength(1.5f)
            .setDirectionalColor("#71A3D0")
            .setGroundFog(-400, -750, 0.5f));

    // Lunar Isle
    public final static Environment LUNAR_DIPLOMACY_DREAM_WORLD = new Environment(Area.LUNAR_DIPLOMACY_DREAM_WORLD, new Properties()
            .setFogColor("#000000")
            .setFogDepth(400)
            .setAmbientColor("#77A0FF")
            .setAmbientStrength(1.3f)
            .setDirectionalColor("#CAB6CD")
            .setDirectionalStrength(0.7f));

    // Runecrafting altars
    public final static Environment NATURE_ALTAR = new Environment(Area.NATURE_ALTAR, new Properties());
    public final static Environment WATER_ALTAR = new Environment(Area.WATER_ALTAR, new Properties());
    public final static Environment AIR_ALTAR = new Environment(Area.AIR_ALTAR, new Properties());

    // Random events
    public final static Environment CLASSROOM = new Environment(Area.RANDOM_EVENT_CLASSROOM, new Properties());
    public final static Environment FREAKY_FORESTER = new Environment(Area.RANDOM_EVENT_FREAKY_FORESTER, new Properties());
    public final static Environment GRAVEDIGGER = new Environment(Area.RANDOM_EVENT_GRAVEDIGGER, new Properties());
    public final static Environment DRILL_DEMON = new Environment(Area.RANDOM_EVENT_DRILL_DEMON, new Properties()
            .setFogColor("#696559"));

    // Clan halls
    public final static Environment CLAN_HALL = new Environment(Area.CLAN_HALL, new Properties());

    // Standalone and miscellaneous areas
    public final static Environment LIGHTHOUSE = new Environment(Area.LIGHTHOUSE, new Properties());
    public final static Environment SORCERESSS_GARDEN = new Environment(Area.SORCERESSS_GARDEN, new Properties());
    public final static Environment PURO_PURO = new Environment(Area.PURO_PURO, new Properties());
    public final static Environment RATCATCHERS_HOUSE = new Environment(Area.RATCATCHERS_HOUSE, new Properties());
    public final static Environment CANOE_CUTSCENE = new Environment(Area.CANOE_CUTSCENE, new Properties());
    public final static Environment FISHER_KINGS_REALM = new Environment(Area.FISHER_KINGS_REALM, new Properties());
    public final static Environment ENCHANTED_VALLEY = new Environment(Area.ENCHANTED_VALLEY, new Properties());



    public final static Environment UNKNOWN_OVERWORLD_SNOWY = new Environment(Area.UNKNOWN_OVERWORLD_SNOWY, new Properties()
            .setFogColor(174, 189, 224)
            .setFogDepth(800)
            .setAmbientColor(59, 135, 228)
            .setAmbientStrength(1.0f)
            .setDirectionalColor(255, 201, 104)
            .setDirectionalStrength(0.9f));
    public final static Environment UNKNOWN_OVERWORLD = new Environment(Area.UNKNOWN_OVERWORLD, new Properties());

    // overrides 'ALL' to provide default daylight conditions for the overworld area
    public final static Environment OVERWORLD = new Environment(Area.OVERWORLD, new Properties());
    // used for underground, instances, etc.
    public final static Environment ALL = new Environment(Area.ALL, new Properties()
            .setFogColor("#31271A")
            .setFogDepth(400)
            .setAmbientColor("#77A0FF")
            .setAmbientStrength(1.3f)
            .setDirectionalColor("#4C78B6")
            .setDirectionalStrength(0.5f));

    private final Area area;
    private final int fogDepth;
    private final boolean customFogDepth;
    private final float[] fogColor;
    private final boolean customFogColor;
    private final float ambientStrength;
    private final boolean customAmbientStrength;
    private final float[] ambientColor;
    private final boolean customAmbientColor;
    private final float directionalStrength;
    private final boolean customDirectionalStrength;
    private final float[] directionalColor;
    private final boolean customDirectionalColor;
    private final float underglowStrength;
    private final float[] underglowColor;
    private final boolean lightningEnabled;
    private final int groundFogStart;
    private final int groundFogEnd;
    private final float groundFogOpacity;

    // Linked Hash Map so that the values are in-order of declaration, like they would be in an Enum
    private static final Map<String, Environment> valMap = new LinkedHashMap<>();
    // Map of overrides
    public static final Map<String, Environment> overridesMap = new HashMap<>();

    public Environment(Area area, Properties properties)
    {
        this.area = area;
        this.fogDepth = properties.fogDepth;
        this.customFogDepth = properties.customFogDepth;
        this.fogColor = properties.fogColor;
        this.customFogColor = properties.customFogColor;
        this.ambientStrength = properties.ambientStrength;
        this.customAmbientStrength = properties.customAmbientStrength;
        this.ambientColor = properties.ambientColor;
        this.customAmbientColor = properties.customAmbientColor;
        this.directionalStrength = properties.directionalStrength;
        this.customDirectionalStrength = properties.customDirectionalStrength;
        this.directionalColor = properties.directionalColor;
        this.customDirectionalColor = properties.customDirectionalColor;
        this.underglowColor = properties.underglowColor;
        this.underglowStrength = properties.underglowStrength;
        this.lightningEnabled = properties.lightningEnabled;
        this.groundFogStart = properties.groundFogStart;
        this.groundFogEnd = properties.groundFogEnd;
        this.groundFogOpacity = properties.groundFogOpacity;
    }

    static {
        loadClassData();
        log.debug("Finished Setting up Environments definitions");
    }

    private static void loadClassData() {
        Arrays.stream(Environment.class.getDeclaredFields())
                .filter(declaredField -> declaredField.getType() == Environment.class)
                .forEach(Environment::putInMap);
    }

    private static void putInMap(Field declaredField) {
        try {
            valMap.putIfAbsent(declaredField.getName(), (Environment) declaredField.get(null));
        } catch (IllegalAccessException ex) {
            log.error("Could not initialize Environment Map value : {} : {}", declaredField.getName(), ex);
        }
    }

    public static Environment[] values() {
        return valMap.values().toArray(new Environment[0]).clone();
    }

    public static Environment valueOf(String value, boolean ignoreOverrides) {
        Environment env = null;

        if (!ignoreOverrides) {
            env = overridesMap.getOrDefault(value, null);
        }

        if (env == null) {
            env = valMap.getOrDefault(value, null);
        }

        return env;
    }

    public static void addOverride(Environment environment) {
        overridesMap.put(environment.getArea().name(), environment);
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String name() {
        return this.area.name();
    }

    public static class Properties
    {
        public Properties() {}
        public Properties(Environment environment) {
            this.fogDepth = environment.fogDepth;
            this.customFogDepth = environment.customFogDepth;
            this.fogColor = environment.fogColor;
            this.customFogColor = environment.customFogColor;
            this.ambientStrength = environment.ambientStrength;
            this.customAmbientStrength = environment.customAmbientStrength;
            this.ambientColor = environment.ambientColor;
            this.customAmbientColor = environment.customAmbientColor;
            this.directionalStrength = environment.directionalStrength;
            this.customDirectionalStrength = environment.customDirectionalStrength;
            this.directionalColor = environment.directionalColor;
            this.customDirectionalColor = environment.customDirectionalColor;
            this.underglowStrength = environment.underglowStrength;
            this.underglowColor = environment.underglowColor;
            this.lightningEnabled = environment.lightningEnabled;
            this.groundFogStart = environment.groundFogStart;
            this.groundFogEnd = environment.groundFogEnd;
            this.groundFogOpacity = environment.groundFogOpacity;
        }

        private int fogDepth = 65;
        private boolean customFogDepth = false;
        private float[] fogColor = new float[]{185 / 255f, 214 / 255f, 255 / 255f};
        private boolean customFogColor = false;
        private float ambientStrength = 1.0f;
        private boolean customAmbientStrength = false;
        private float[] ambientColor = new float[]{136 / 255f, 163 / 255f, 208 / 255f};
        private boolean customAmbientColor = false;
        private float directionalStrength = 1.3f;
        private boolean customDirectionalStrength = false;
        private float[] directionalColor = new float[]{255 / 255f, 246 / 255f, 215 / 255f};
        private boolean customDirectionalColor = false;
        private float underglowStrength = 0.0f;
        private float[] underglowColor = new float[]{0, 0, 0};
        private boolean lightningEnabled = false;
        private int groundFogStart = -200;
        private int groundFogEnd = -500;
        private float groundFogOpacity = 0;

        public Properties setFogDepth(int depth)
        {
            this.fogDepth = depth;
            this.customFogDepth = true;
            return this;
        }

        public Properties setFogColor(int r, int g, int b)
        {
            this.fogColor = new float[3];
            this.fogColor[0] = r / 255f;
            this.fogColor[1] = g / 255f;
            this.fogColor[2] = b / 255f;
            this.customFogColor = true;
            return this;
        }

        public Properties setFogColor(Color color) {
            this.fogColor = new float[3];
            this.fogColor[0] = color.getRed() / 255f;
            this.fogColor[1] = color.getGreen() / 255f;
            this.fogColor[2] = color.getBlue() / 255f;
            this.customFogColor = true;
            return this;
        }

        public Properties setFogColor(String hex)
        {
            Color color = Color.decode(hex);
            return this.setFogColor(color);
        }

        public Properties setAmbientStrength(float str)
        {
            this.ambientStrength = str;
            this.customAmbientStrength = true;
            return this;
        }

        public Properties setAmbientColor(int r, int g, int b)
        {
            this.ambientColor = new float[3];
            this.ambientColor[0] = r / 255f;
            this.ambientColor[1] = g / 255f;
            this.ambientColor[2] = b / 255f;
            this.customAmbientColor = true;
            return this;
        }

        public Properties setAmbientColor(Color color) {
            this.ambientColor = new float[3];
            this.ambientColor[0] = color.getRed() / 255f;
            this.ambientColor[1] = color.getGreen() / 255f;
            this.ambientColor[2] = color.getBlue() / 255f;
            this.customAmbientColor = true;
            return this;
        }

        public Properties setAmbientColor(String hex)
        {
            Color color = Color.decode(hex);
            return this.setAmbientColor(color);
        }

        public Properties setDirectionalStrength(float str)
        {
            this.directionalStrength = str;
            this.customDirectionalStrength = true;
            return this;
        }

        public Properties setDirectionalColor(int r, int g, int b)
        {
            this.directionalColor = new float[3];
            this.directionalColor[0] = r / 255f;
            this.directionalColor[1] = g / 255f;
            this.directionalColor[2] = b / 255f;
            this.customDirectionalColor = true;
            return this;
        }

        public Properties setDirectionalColor(Color color)
        {
            this.directionalColor = new float[3];
            this.directionalColor[0] = color.getRed() / 255f;
            this.directionalColor[1] = color.getGreen() / 255f;
            this.directionalColor[2] = color.getBlue() / 255f;
            this.customDirectionalColor = true;
            return this;
        }

        public Properties setDirectionalColor(String hex)
        {
            Color color = Color.decode(hex);
            return this.setDirectionalColor(color);
        }

        public Properties setUnderglowStrength(float str)
        {
            this.underglowStrength = str;
            return this;
        }

        public Properties setUnderglowColor(int r, int g, int b)
        {
            this.underglowColor = new float[3];
            this.underglowColor[0] = r / 255f;
            this.underglowColor[1] = g / 255f;
            this.underglowColor[2] = b / 255f;
            return this;
        }

        public Properties setUnderglowColor(Color color) {
            this.underglowColor = new float[3];
            this.underglowColor[0] = color.getRed() / 255f;
            this.underglowColor[1] = color.getGreen() / 255f;
            this.underglowColor[2] = color.getBlue() / 255f;
            return this;
        }

        public Properties setUnderglowColor(String hex)
        {
            Color color = Color.decode(hex);
            return this.setUnderglowColor(color);
        }

        public Properties setGroundFogStart(int start) {
            this.groundFogStart = start;
            return this;
        }

        public Properties setGroundFogEnd(int end) {
            this.groundFogEnd = end;
            return this;
        }

        public Properties setGroundFogOpacity(float maxOpacity) {
            this.groundFogOpacity = maxOpacity;
            return this;
        }

        public Properties setGroundFog(int start, int end, float maxOpacity)
        {
            this.groundFogStart = start;
            this.groundFogEnd = end;
            this.groundFogOpacity = maxOpacity;
            return this;
        }

        public Properties enableLightning(Boolean enabled) {
            this.lightningEnabled = enabled;
            return this;
        }

        public Properties enableLightning()
        {
            this.lightningEnabled = true;
            return this;
        }
    }
}
