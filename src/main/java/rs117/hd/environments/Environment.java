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
import lombok.Getter;

@Getter
public enum Environment
{
	FROZEN_WASTE_PLATEAU(Area.FROZEN_WASTE_PLATEAU, new Properties()
		.setFogColor("#252C37")
		.setFogDepth(80)
		.setAmbientStrength(0.4f)
		.setAmbientColor("#3B87E4")
		.setDirectionalStrength(0.9f)
		.setDirectionalColor("#8A9EB6")
	),
	WILDERNESS_HIGH(Area.WILDERNESS_HIGH, new Properties()
		.setFogColor("#464449")
		.setFogDepth(30)
		.setAmbientStrength(0.5f)
		.setAmbientColor(215, 210, 210)
		.setDirectionalStrength(0.8f)
		.setDirectionalColor("#C5B8B6")
		.enableLightning()
		.setGroundFog(-0, -250, 0.3f)
	),
	WILDERNESS_LOW(Area.WILDERNESS_LOW, new Properties()
		.setFogColor("#A9A2B0")
		.setFogDepth(20)
		.setAmbientStrength(0.6f)
		.setAmbientColor(215, 210, 210)
		.setDirectionalStrength(1.0f)
		.setDirectionalColor(138, 158, 182)
	),
	WILDERNESS(Area.WILDERNESS, new Properties()
		.setFogColor("#695B6B")
		.setFogDepth(30)
		.setAmbientStrength(0.6f)
		.setAmbientColor(215, 210, 210)
		.setDirectionalStrength(1.0f)
		.setDirectionalColor("#C5B8B6")
		.setGroundFog(-0, -250, 0.3f)
	),

	// Varrock
	VARROCK_MUSEUM_BASEMENT(Area.VARROCK_MUSEUM_BASEMENT, new Properties()
		.setFogColor("#131B26")
		.setFogDepth(20)
		.setAmbientColor("#CBC2B2")
		.setAmbientStrength(1.0f)
		.setDirectionalColor("#4C78B6")
		.setDirectionalStrength(0.3f)),
	// A Soul's Bane
	TOLNA_DUNGEON_ANGER(Area.TOLNA_DUNGEON_ANGER, new Properties()
		.setFogColor("#290000")
		.setFogDepth(40)
		.setAmbientColor("#AE7D46")
		.setAmbientStrength(1.3f)
		.setDirectionalColor("#CB4848")
		.setDirectionalStrength(0.7f)
	),
	TOLNA_DUNGEON_FEAR(Area.TOLNA_DUNGEON_FEAR, new Properties()
		.setFogColor("#000B0F")
		.setFogDepth(40)
		.setAmbientColor("#77A0FF")
		.setAmbientStrength(1.3f)
		.setDirectionalColor("#4C78B6")
		.setDirectionalStrength(0.5f)
	),
	TOLNA_DUNGEON_CONFUSION(Area.TOLNA_DUNGEON_CONFUSION, new Properties()
		.setFogColor("#2E0C23")
		.setFogDepth(40)
		.setAmbientColor("#77A0FF")
		.setAmbientStrength(1.3f)
		.setDirectionalColor("#4E9DD0")
		.setDirectionalStrength(0.5f)
	),

	THE_INFERNO(Area.THE_INFERNO, new Properties()
		.setUnderglowColor(255, 0, 0)
		.setUnderglowStrength(2f)
		.setFogColor(23, 11, 7)
		.setFogDepth(20)
		.setAmbientColor(240, 184, 184)
		.setAmbientStrength(1.7f)
		.setDirectionalColor(255, 246, 202)
		.setDirectionalStrength(0f)
	),
	TZHAAR(Area.TZHAAR, new Properties()
		.setFogColor("#1A0808")
		.setFogDepth(15)
		.setAmbientColor("#FFEACC")
		.setAmbientStrength(0.8f)
		.setDirectionalColor("#FFA400")
		.setDirectionalStrength(0.7f)
	),

	BARROWS_CRYPTS(Area.BARROWS_CRYPTS, new Properties()
		.setFogColor(0, 0, 0)
		.setFogDepth(20)
		.setAmbientColor(181, 143, 124)
		.setAmbientStrength(1.2f)
		.setDirectionalColor(255, 200, 117)
		.setDirectionalStrength(0.1f)
	),
	BARROWS(Area.BARROWS, new Properties()
		.setFogColor("#242D3A")
		.setFogDepth(50)
		.setAmbientColor("#5B83B3")
		.setAmbientStrength(1.4f)
		.setDirectionalColor("#526E8B")
		.setDirectionalStrength(0.3f)
		.enableLightning()
		.setGroundFog(-300, -500, 0.5f)
	),
	MORYTANIA(Area.MORYTANIA, new Properties()
		.setFogColor("#1E314B")
		.setFogDepth(40)
		.setAmbientColor("#5B83B3")
		.setAmbientStrength(0.7f)
		.setDirectionalColor("#526E8B")
		.setDirectionalStrength(2.0f)
		.setGroundFog(-150, -350, 0.5f)
	),

	LUMBRIDGE(Area.LUMBRIDGE, new Properties()),

	DRAYNOR_MANOR(Area.DRAYNOR_MANOR, new Properties()
		.setFogColor(15, 14, 13)
		.setFogDepth(30)
		.setAmbientColor("#615C57")
		.setAmbientStrength(1.5f)
		.setDirectionalColor("#FFBCB7")
		.setAmbientStrength(0.5f)
		.enableLightning()
	),
	DRAYNOR_MANOR_BASEMENT(Area.DRAYNOR_MANOR_BASEMENT, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(40)
		.setAmbientColor("#7891B5")
		.setAmbientStrength(1.0f)
		.setDirectionalColor(76, 120, 182)
		.setDirectionalStrength(0.0f)
	),
	DRAYNOR(Area.DRAYNOR, new Properties()),

	FALADOR(Area.FALADOR, new Properties()),

	GAMES_ROOM(Area.GAMES_ROOM, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(20)
		.setAmbientColor(162, 151, 148)
		.setAmbientStrength(0.3f)
		.setDirectionalColor(138, 158, 182)
		.setDirectionalStrength(0.1f)
	),

	SOUL_WARS_RED_TEAM(Area.SOUL_WARS_RED_BASE, new Properties()
		.setFogColor(28, 21, 13)
	),
	TUTORIAL_SOUL_WARS_RED_TEAM(Area.SOUL_WARS_RED_BASE_TUTORIAL, new Properties()
		.setFogColor(28, 21, 13)
	),
	TUTORIAL_ISLE_OF_SOULS(Area.ISLE_OF_SOULS_TUTORIAL, new Properties()),

	SMOKE_DUNGEON(Area.SMOKE_DUNGEON, new Properties()
		.setFogColor(0, 0, 0)
		.setFogDepth(80)
		.setAmbientColor(171, 171, 171)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(86, 86, 86)
		.setDirectionalStrength(0.2f)
	),
	DUEL_ARENA(Area.DUEL_ARENA, new Properties()
		.setAmbientStrength(0.8f)
		.setAmbientColor("#FFEEC4")
		.setDirectionalStrength(1.2f)
	),
	SHANTAY_PASS(Area.SHANTAY_PASS, new Properties()
		.setAmbientStrength(0.8f)
		.setAmbientColor("#FFEEC4")
		.setDirectionalStrength(1.2f)
	),
	AL_KHARID(Area.AL_KHARID, new Properties()
		.setAmbientStrength(0.8f)
		.setAmbientColor("#FFEEC4")
		.setDirectionalStrength(1.2f)
	),
	EAST_AL_KHARID(Area.EAST_AL_KHARID, new Properties()
		.setAmbientStrength(0.8f)
		.setAmbientColor("#FFEEC4")
		.setDirectionalStrength(1.2f)
	),
	AL_KHARID_MINE(Area.AL_KHARID_MINE, new Properties()
		.setAmbientStrength(0.8f)
		.setAmbientColor("#FFEEC4")
		.setDirectionalStrength(1.2f)
	),
	KHARIDIAN_DESERT_DEEP(Area.KHARIDIAN_DESERT_DEEP, new Properties()
		.setFogColor(213, 179, 117)
		.setFogDepth(80)
		.setAmbientColor(205, 185, 150)
		.setAmbientStrength(0.8f)
		.setDirectionalColor(200, 169, 115)
		.setDirectionalStrength(0.4f)
	),
	KHARIDIAN_DESERT(Area.KHARIDIAN_DESERT, new Properties()
		.setFogColor(200, 186, 152)
		.setFogDepth(50)
		.setAmbientColor(208, 185, 156)
		.setAmbientStrength(1.5f)
		.setDirectionalColor(181, 155, 121)
		.setDirectionalStrength(0.5f)
	),
	DESERT_TREASURE_PYRAMID(Area.DESERT_TREASURE_PYRAMID, new Properties()
		.setFogColor(39, 23, 4)
		.setFogDepth(40)
		.setAmbientColor(192, 159, 110)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(138, 158, 182)
		.setDirectionalStrength(0.1f)
	),
	PYRAMID_PLUNDER(Area.PYRAMID_PLUNDER, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(40)
		.setAmbientColor(181, 155, 121)
		.setAmbientStrength(0.6f)
		.setDirectionalColor(138, 158, 182)
		.setDirectionalStrength(0.1f)
	),

	GIELINOR_SNOWY_NORTHERN_REGION(Area.GIELINOR_SNOWY_NORTHERN_REGION, new Properties()
		.setFogColor(174, 189, 224)
		.setFogDepth(80)
		.setAmbientColor(59, 135, 228)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(255, 201, 104)
		.setDirectionalStrength(0.9f)
	),

	WHITE_WOLF_MOUNTAIN(Area.WHITE_WOLF_MOUNTAIN, new Properties()),

	KEEP_LE_FAYE(Area.KEEP_LE_FAYE, new Properties()),

	MOUNTAIN_CAMP_ENTRY_PATH(Area.MOUNTAIN_CAMP_ENTRY_PATH, new Properties()
		.setFogColor(178, 187, 197)
		.setFogDepth(50)
		.setAmbientStrength(0.9f)
		.setDirectionalStrength(0.3f)
		.setGroundFog(-600, -900, 0.4f)
	),
	MOUNTAIN_CAMP(Area.MOUNTAIN_CAMP, new Properties()
		.setFogColor(178, 187, 197)
		.setFogDepth(50)
		.setAmbientStrength(0.9f)
		.setDirectionalStrength(0.3f)
		.setGroundFog(-1200, -1600, 0.5f)
		.setGroundFog(-1200, -1600, 0.5f)
	),
	FREMENNIK_PROVINCE(Area.FREMENNIK_PROVINCE, new Properties()
		.setFogColor("#969CA2")
		.setFogDepth(40)
		.setAmbientStrength(0.9f)
		.setAmbientColor("#96A3CB")
		.setDirectionalStrength(0.8f)
		.setDirectionalColor("#ABC2D3")
		.setGroundFog(-200, -400, 0.3f)
	),

	HAM_HIDEOUT(Area.HAM_HIDEOUT, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(40)
		.setAmbientColor("#7891B5")
		.setAmbientStrength(1.3f)
		.setDirectionalColor(76, 120, 182)
		.setDirectionalStrength(0.0f)
	),

	// Karamja
	KARAMJA_VOLCANO_DUNGEON(Area.KARAMJA_VOLCANO_DUNGEON, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(40)
		.setAmbientColor("#7891B5")
		.setAmbientStrength(0.9f)
		.setDirectionalColor(76, 120, 182)
		.setDirectionalStrength(0.0f)
	),
	KARAMJA(Area.KARAMJA, new Properties()),


	UNGAEL(Area.UNGAEL, new Properties()
		.setFogColor(226, 230, 237)
		.setFogDepth(40)
		.setAmbientColor(234, 226, 205)
		.setAmbientStrength(0.6f)
		.setDirectionalColor(130, 172, 224)
		.setDirectionalStrength(0.6f)
	),

	FROZEN_DOOR(Area.FROZEN_DOOR, new Properties()
		.setFogColor(3, 16, 25)
		.setFogDepth(30)
		.setAmbientColor(200, 228, 247)
		.setAmbientStrength(0.3f)
		.setDirectionalColor(146, 209, 250)
		.setDirectionalStrength(0.3f)
		.enableLightning()
	),
	GOD_WARS_DUNGEON(Area.GOD_WARS_DUNGEON, new Properties()
		.setFogColor(14, 59, 89)
		.setFogDepth(30)
		.setAmbientColor("#68ACFF")
		.setAmbientStrength(0.5f)
		.setDirectionalColor(146, 209, 250)
		.setDirectionalStrength(0.7f)
	),

	TAR_SWAMP(Area.TAR_SWAMP, new Properties()
		.setFogColor(42, 49, 36)
		.setFogDepth(50)
		.setAmbientColor(248, 224, 172)
		.setAmbientStrength(0.8f)
		.setDirectionalColor(168, 171, 144)
		.setDirectionalStrength(0.5f)
	),

	SOTE_LLETYA_SMALL_FIRES(Area.SOTE_LLETYA_SMALL_FIRES, new Properties()
		.setFogColor(91, 139, 120)
		.setFogDepth(30)
		.setAmbientStrength(1.0f)
		.setDirectionalStrength(0.0f)
	),
	SOTE_LLETYA_ON_FIRE(Area.SOTE_LLETYA_ON_FIRE, new Properties()
		.setFogColor(91, 139, 120)
		.setFogDepth(50)
		.setAmbientStrength(0.9f)
		.setDirectionalStrength(0.0f)
	),
	POSION_WASTE(Area.POISON_WASTE, new Properties()
		.setFogColor(50, 55, 47)
		.setFogDepth(50)
		.setAmbientColor(192, 219, 173)
		.setAmbientStrength(0.8f)
		.setDirectionalColor(173, 176, 139)
		.setDirectionalStrength(0.4f)
	),
	TIRANNWN(Area.TIRANNWN, new Properties()
		.setFogColor("#99D8C8")
		.setFogDepth(15)
	),
	PRIFDDINAS(Area.PRIFDDINAS, new Properties()
		.setFogColor("#99D8C8")
		.setFogDepth(15)
	),
	SOTE_GRAND_LIBRARY(Area.SOTE_GRAND_LIBRARY, new Properties()
		.setFogColor(18, 64, 83)
		.setAmbientStrength(0.3f)
		.setDirectionalStrength(0.3f)
	),
	SOTE_FRAGMENT_OF_SEREN_ARENA(Area.SOTE_FRAGMENT_OF_SEREN_ARENA, new Properties()
		.setFogColor(0, 0, 0)
	),

	// Yanille
	YANILLE(Area.YANILLE, new Properties()),
	// Nightmare Zone
	NIGHTMARE_ZONE(Area.NIGHTMARE_ZONE, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(40)
		.setAmbientColor("#F2B979")
		.setAmbientStrength(0.9f)
		.setDirectionalColor("#97DDFF")
		.setDirectionalStrength(0.4f)
	),

	// Tree Gnome Stronghold
	TREE_GNOME_STRONGHOLD(Area.TREE_GNOME_STRONGHOLD, new Properties()),

	// Castle Wars
	CASTLE_WARS_UNDERGROUND(Area.CASTLE_WARS_UNDERGROUND, new Properties()
		.setAmbientStrength(1.3f)
		.setAmbientColor("#FFD79C")
		.setDirectionalStrength(0.0f)
	),

	// Last Man Standing
	LMS_ARENA_WILD_VARROCK(Area.LMS_ARENA_WILD_VARROCK, new Properties()
		.setFogColor("#695B6B")
		.setFogDepth(30)
		.setAmbientStrength(0.6f)
		.setAmbientColor(215, 210, 210)
		.setDirectionalStrength(1.0f)
		.setDirectionalColor("#C5B8B6")
		.setGroundFog(-0, -250, 0.3f)
	),
	LMS_ARENA_DESERTED_ISLAND(Area.LMS_ARENA_DESERTED_ISLAND, new Properties()),

	// Zeah
	KOUREND_CATACOMBS(Area.KOUREND_CATACOMBS, new Properties()
		.setFogColor("#0E0022")
		.setFogDepth(40)
		.setAmbientColor("#8B7DDB")
		.setAmbientStrength(1.5f)
		.setDirectionalColor(76, 120, 182)
		.setDirectionalStrength(0.0f)
	),
	MOUNT_QUIDAMORTEM(Area.MOUNT_QUIDAMORTEM, new Properties()),
	KEBOS_LOWLANDS(Area.KEBOS_LOWLANDS, new Properties()
		.setFogColor(41, 44, 16)
		.setFogDepth(50)
		.setAmbientColor(255, 215, 133)
		.setAmbientStrength(0.8f)
		.setDirectionalColor(207, 229, 181)
		.setDirectionalStrength(0.3f)
	),
	BLOOD_ALTAR(Area.BLOOD_ALTAR, new Properties()
		.setFogColor(79, 19, 37)
		.setFogDepth(30)
		.setAmbientColor(190, 72, 174)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(78, 238, 255)
		.setDirectionalStrength(1.0f)
	),
	ZEAH_SNOWY_NORTHERN_REGION(Area.ZEAH_SNOWY_NORTHERN_REGION, new Properties()
		.setFogColor(174, 189, 224)
		.setFogDepth(80)
		.setAmbientColor(59, 135, 228)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(255, 201, 104)
		.setDirectionalStrength(0.9f)
	),
	ARCEUUS(Area.ARCEUUS, new Properties()
		.setFogColor(19, 24, 79)
		.setFogDepth(30)
		.setAmbientColor(99, 105, 255)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(78, 238, 255)
		.setDirectionalStrength(1.0f)
	),
	LOVAKENGJ(Area.LOVAKENGJ, new Properties()
		.setFogColor(21, 10, 5)
		.setFogDepth(40)
		.setAmbientColor(255, 215, 133)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(125, 141, 179)
		.setDirectionalStrength(1.0f)
	),

	// Zanaris
	COSMIC_ENTITYS_PLANE(Area.COSMIC_ENTITYS_PLANE, new Properties()
		.setFogColor("#000000")
		.setAmbientStrength(1.5f)
		.setAmbientColor("#DB6FFF")
		.setDirectionalStrength(1.2f)
		.setDirectionalColor("#57FF00")
	),
	ZANARIS(Area.ZANARIS, new Properties()
		.setFogColor(22, 63, 71)
		.setFogDepth(30)
		.setAmbientColor(115, 181, 195)
		.setAmbientStrength(0.3f)
		.setDirectionalColor(245, 214, 122)
		.setDirectionalStrength(0.3f)
	),

	// Dragon Slayer II
	DS2_FLASHBACK_PLATFORM(Area.DS2_FLASHBACK_PLATFORM, new Properties()
		.setFogColor(0, 0, 0)
		.setFogDepth(20)
		.setAmbientStrength(1.2f)
		.setAmbientColor(255, 255, 255)
		.setDirectionalStrength(0f)
	),
	DS2_FLEET_ATTACKED(Area.DS2_FLEET_ATTACKED, new Properties()
		.setFogColor("#FFD3C7")
		.setFogDepth(20)
		.setAmbientColor("#68ACFF")
		.setAmbientStrength(0.8f)
		.setDirectionalColor("#FF8700")
		.setDirectionalStrength(1.5f)
	),
	DS2_SHIPS(Area.DS2_SHIPS, new Properties()
		.setFogColor("#FFD3C7")
		.setFogDepth(20)
		.setAmbientColor("#68ACFF")
		.setAmbientStrength(0.8f)
		.setDirectionalColor("#FF8700")
		.setDirectionalStrength(1.5f)
	),

	// The Gauntlet
	THE_GAUNTLET(Area.THE_GAUNTLET, new Properties()
		.setFogColor("#090606")
		.setFogDepth(20)
		.setAmbientColor("#D2C0B7")
		.setAmbientStrength(1.2f)
		.setDirectionalColor("#78FFE3")
		.setDirectionalStrength(0.8f)
	),
	THE_GAUNTLET_CORRUPTED(Area.THE_GAUNTLET_CORRUPTED, new Properties()
		.setFogColor("#090606")
		.setFogDepth(20)
		.setAmbientColor("#95B6F7")
		.setAmbientStrength(1.2f)
		.setDirectionalColor("#FF7878")
		.setDirectionalStrength(0.8f)
	),
	THE_GAUNTLET_LOBBY(Area.THE_GAUNTLET_LOBBY, new Properties()
		.setFogColor("#090606")
		.setFogDepth(20)
		.setAmbientColor("#D2C0B7")
		.setAmbientStrength(1.2f)
		.setDirectionalColor("#78FFE3")
		.setDirectionalStrength(0.8f)
	),

	// Islands
	BRAINDEATH_ISLAND(Area.BRAINDEATH_ISLAND, new Properties()),

	// Ape Atoll
	// Monkey Madness 2
	MM2_AIRSHIP_PLATFORM(Area.MM2_AIRSHIP_PLATFORM, new Properties()),

	// POHs
	PLAYER_OWNED_HOUSE_SNOWY(Area.PLAYER_OWNED_HOUSE_SNOWY, new Properties()
		.setFogColor(174, 189, 224)
		.setFogDepth(50)
		.setAmbientColor(59, 135, 228)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(255, 201, 104)
		.setDirectionalStrength(0.9f)
	),
	PLAYER_OWNED_HOUSE(Area.PLAYER_OWNED_HOUSE, new Properties()),

	// Blackhole
	BLACKHOLE(Area.BLACKHOLE, new Properties()
		.setFogColor(0, 0, 0)
		.setFogDepth(20)
		.setAmbientStrength(1.2f)
		.setAmbientColor(255, 255, 255)
		.setDirectionalStrength(0f)
	),

	// Fishing Trawler
	FISHING_TRAWLER(Area.FISHING_TRAWLER, new Properties()),

	// Camdozaal (Below Ice Mountain)
	CAMDOZAAL(Area.CAMDOZAAL, new Properties()
		.setFogColor("#080012")
		.setFogDepth(40)
		.setAmbientStrength(1.5f)
		.setAmbientColor("#C9B9F7")
		.setDirectionalStrength(0f)
		.setDirectionalColor("#6DC5FF")
	),

	// Tempoross
	TEMPOROSS_COVE(Area.TEMPOROSS_COVE, new Properties()
		.setFogColor("#45474B")
		.setFogDepth(60)
		.setAmbientStrength(0.9f)
		.setAmbientColor("#A5ACBD")
		.setDirectionalStrength(0.1f)
		.setDirectionalColor("#707070")
		.enableLightning()
	),

	// Morytania
	// Hallowed Sepulchre
	HALLOWED_SEPULCHRE_LOBBY(Area.HALLOWED_SEPULCHRE_LOBBY, new Properties()
		.setFogColor("#0D1012")
		.setFogDepth(50)
		.setAmbientStrength(0.7f)
		.setAmbientColor("#C4D5EA")
		.setDirectionalStrength(0.4f)
		.setDirectionalColor("#A0BBE2")
	),
	HALLOWED_SEPULCHRE_FLOOR_1(Area.HALLOWED_SEPULCHRE_FLOOR_1, new Properties()
		.setFogColor(17, 28, 26)
		.setFogDepth(50)
		.setAmbientStrength(0.9f)
		.setAmbientColor(155, 187, 177)
		.setDirectionalStrength(0.7f)
		.setDirectionalColor(117, 231, 255)
	),
	HALLOWED_SEPULCHRE_FLOOR_2(Area.HALLOWED_SEPULCHRE_FLOOR_2, new Properties()
		.setFogColor(17, 28, 27)
		.setFogDepth(50)
		.setAmbientStrength(0.875f)
		.setAmbientColor(160, 191, 191)
		.setDirectionalStrength(0.675f)
		.setDirectionalColor(116, 214, 247)
	),
	HALLOWED_SEPULCHRE_FLOOR_3(Area.HALLOWED_SEPULCHRE_FLOOR_3, new Properties()
		.setFogColor(18, 28, 29)
		.setFogDepth(50)
		.setAmbientStrength(0.85f)
		.setAmbientColor(165, 195, 205)
		.setDirectionalStrength(0.65f)
		.setDirectionalColor(115, 196, 240)
	),
	HALLOWED_SEPULCHRE_FLOOR_4(Area.HALLOWED_SEPULCHRE_FLOOR_4, new Properties()
		.setFogColor(18, 27, 31)
		.setFogDepth(50)
		.setAmbientStrength(0.825f)
		.setAmbientColor(170, 199, 220)
		.setDirectionalStrength(0.625f)
		.setDirectionalColor(114, 178, 233)
	),
	HALLOWED_SEPULCHRE_FLOOR_5(Area.HALLOWED_SEPULCHRE_FLOOR_5, new Properties()
		.setFogColor(19, 27, 33)
		.setFogDepth(50)
		.setAmbientStrength(0.8f)
		.setAmbientColor(175, 202, 234)
		.setDirectionalStrength(0.6f)
		.setDirectionalColor(113, 160, 226)
	),
	// Theatre of Blood
	TOB_ROOM_VAULT(Area.TOB_ROOM_VAULT, new Properties()
		.setFogColor("#0E081A")
		.setFogDepth(40)
		.setAmbientStrength(2.0f)
		.setAmbientColor("#7963C3")
		.setDirectionalStrength(0.0f)
		.setDirectionalColor("#FF6767")
	),
	THEATRE_OF_BLOOD(Area.THEATRE_OF_BLOOD, new Properties()
		.setFogColor("#0E0C2C")
		.setFogDepth(40)
		.setAmbientStrength(2.0f)
		.setAmbientColor("#6D73FF")
		.setDirectionalStrength(1.2f)
		.setDirectionalColor("#FF6767")
	),

	// Chambers of Xeric
	CHAMBERS_OF_XERIC(Area.CHAMBERS_OF_XERIC, new Properties()
		.setFogColor("#122717")
		.setFogDepth(35)
		.setAmbientStrength(1.2f)
		.setAmbientColor("#7897C3")
		.setDirectionalStrength(0.3f)
		.setDirectionalColor("#A4F065")
	),

	// Nightmare of Ashihama
	NIGHTMARE_OF_ASHIHAMA_ARENA(Area.NIGHTMARE_OF_ASHIHAMA_ARENA, new Properties()
		.setFogColor("#000000")
		.setFogDepth(30)
		.setAmbientStrength(1.4f)
		.setAmbientColor("#9A5DFD")
		.setDirectionalStrength(0.8f)
		.setDirectionalColor("#00FF60")
	),

	// Underwater areas
	MOGRE_CAMP_CUTSCENE(Area.MOGRE_CAMP_CUTSCENE, new Properties()),
	MOGRE_CAMP(Area.MOGRE_CAMP, new Properties()
		.setFogColor("#133156")
		.setFogDepth(80)
		.setAmbientStrength(0.5f)
		.setAmbientColor("#255590")
		.setDirectionalStrength(1.5f)
		.setDirectionalColor("#71A3D0")
		.setGroundFog(0, -500, 0.5f)
	),
	HARMONY_ISLAND_UNDERWATER_TUNNEL(Area.HARMONY_ISLAND_UNDERWATER_TUNNEL, new Properties()
		.setFogColor("#133156")
		.setFogDepth(80)
		.setAmbientStrength(2.0f)
		.setAmbientColor("#255590")
		.setDirectionalStrength(0.4f)
		.setDirectionalColor("#71A3D0")
		.setGroundFog(-800, -1100, 0.5f)
	),
	FOSSIL_ISLAND_UNDERWATER_AREA(Area.FOSSIL_ISLAND_UNDERWATER_AREA, new Properties()
		.setFogColor("#133156")
		.setFogDepth(80)
		.setAmbientStrength(0.5f)
		.setAmbientColor("#255590")
		.setDirectionalStrength(1.5f)
		.setDirectionalColor("#71A3D0")
		.setGroundFog(-400, -750, 0.5f)
	),

	// Lunar Isle
	LUNAR_DIPLOMACY_DREAM_WORLD(Area.LUNAR_DIPLOMACY_DREAM_WORLD, new Properties()
		.setFogColor("#000000")
		.setFogDepth(40)
		.setAmbientColor("#77A0FF")
		.setAmbientStrength(1.3f)
		.setDirectionalColor("#CAB6CD")
		.setDirectionalStrength(0.7f)
	),

	// Runecrafting altars
	NATURE_ALTAR(Area.NATURE_ALTAR, new Properties()),
	WATER_ALTAR(Area.WATER_ALTAR, new Properties()),
	AIR_ALTAR(Area.AIR_ALTAR, new Properties()),

	// Random events
	CLASSROOM(Area.RANDOM_EVENT_CLASSROOM, new Properties()),
	FREAKY_FORESTER(Area.RANDOM_EVENT_FREAKY_FORESTER, new Properties()),
	GRAVEDIGGER(Area.RANDOM_EVENT_GRAVEDIGGER, new Properties()),
	DRILL_DEMON(Area.RANDOM_EVENT_DRILL_DEMON, new Properties()
		.setFogColor("#696559")
	),

	// Clan halls
	CLAN_HALL(Area.CLAN_HALL, new Properties()),

	// Standalone and miscellaneous areas
	LIGHTHOUSE(Area.LIGHTHOUSE, new Properties()),
	SORCERESSS_GARDEN(Area.SORCERESSS_GARDEN, new Properties()),
	PURO_PURO(Area.PURO_PURO, new Properties()),
	RATCATCHERS_HOUSE(Area.RATCATCHERS_HOUSE, new Properties()),
	CANOE_CUTSCENE(Area.CANOE_CUTSCENE, new Properties()),
	FISHER_KINGS_REALM(Area.FISHER_KINGS_REALM, new Properties()),
	ENCHANTED_VALLEY(Area.ENCHANTED_VALLEY, new Properties()),



	UNKNOWN_OVERWORLD_SNOWY(Area.UNKNOWN_OVERWORLD_SNOWY, new Properties()
		.setFogColor(174, 189, 224)
		.setFogDepth(80)
		.setAmbientColor(59, 135, 228)
		.setAmbientStrength(1.0f)
		.setDirectionalColor(255, 201, 104)
		.setDirectionalStrength(0.9f)),
	UNKNOWN_OVERWORLD(Area.UNKNOWN_OVERWORLD, new Properties()),

	// overrides 'ALL' to provide default daylight conditions for the overworld area
	OVERWORLD(Area.OVERWORLD, new Properties()),
	// used for underground, instances, etc.
	ALL(Area.ALL, new Properties()
		.setFogColor("#31271A")
		.setFogDepth(40)
		.setAmbientColor("#77A0FF")
		.setAmbientStrength(1.3f)
		.setDirectionalColor("#4C78B6")
		.setDirectionalStrength(0.5f)
	),
	;

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

	private static class Properties
	{
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
			this.fogDepth = depth * 10;
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

		public Properties setFogColor(String hex)
		{
			Color color = Color.decode(hex);
			this.fogColor = new float[3];
			this.fogColor[0] = color.getRed() / 255f;
			this.fogColor[1] = color.getGreen() / 255f;
			this.fogColor[2] = color.getBlue() / 255f;
			this.customFogColor = true;
			return this;
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

		public Properties setAmbientColor(String hex)
		{
			Color color = Color.decode(hex);
			this.ambientColor = new float[3];
			this.ambientColor[0] = color.getRed() / 255f;
			this.ambientColor[1] = color.getGreen() / 255f;
			this.ambientColor[2] = color.getBlue() / 255f;
			this.customAmbientColor = true;
			return this;
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

		public Properties setDirectionalColor(String hex)
		{
			Color color = Color.decode(hex);
			this.directionalColor = new float[3];
			this.directionalColor[0] = color.getRed() / 255f;
			this.directionalColor[1] = color.getGreen() / 255f;
			this.directionalColor[2] = color.getBlue() / 255f;
			this.customDirectionalColor = true;
			return this;
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

		public Properties setUnderglowColor(String hex)
		{
			Color color = Color.decode(hex);
			this.underglowColor = new float[3];
			this.underglowColor[0] = color.getRed() / 255f;
			this.underglowColor[1] = color.getGreen() / 255f;
			this.underglowColor[2] = color.getBlue() / 255f;
			return this;
		}

		public Properties setGroundFog(int start, int end, float maxOpacity)
		{
			this.groundFogStart = start;
			this.groundFogEnd = end;
			this.groundFogOpacity = maxOpacity;
			return this;
		}

		public Properties enableLightning()
		{
			this.lightningEnabled = true;
			return this;
		}
	}

	Environment(Area area, Properties properties)
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
}
