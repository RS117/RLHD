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
package rs117.hd.data.materials;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import rs117.hd.data.environments.Area;
import rs117.hd.data.WaterType;

@Getter
public enum Overlay
{
	// Tutorial Island
	TUTORIAL_ISLAND_KITCHEN_TILE_1(9, Area.TUTORIAL_ISLAND_KITCHEN, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	TUTORIAL_ISLAND_KITCHEN_TILE_2(11, Area.TUTORIAL_ISLAND_KITCHEN, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),
	TUTORIAL_ISLAND_QUEST_BUILDING_TILE_1(13, Area.TUTORIAL_ISLAND_QUEST_BUILDING, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	TUTORIAL_ISLAND_QUEST_BUILDING_TILE_2(26, Area.TUTORIAL_ISLAND_QUEST_BUILDING, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),
	TUTORIAL_ISLAND_BANK_TILE_1(2, Area.TUTORIAL_ISLAND_BANK, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	TUTORIAL_ISLAND_BANK_TILE_2(3, Area.TUTORIAL_ISLAND_BANK, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),

	// Lumbridge
	LUM_BRIDGE(10, Area.LUM_BRIDGE, GroundMaterial.GRAVEL),
	LUMBRIDGE_CASTLE_TILE(3, Area.LUMBRIDGE_CASTLE_BASEMENT, GroundMaterial.MARBLE_1_SEMIGLOSS),
	LUMBRIDGE_CASTLE_FLOORS(10, Area.LUMBRIDGE_CASTLE, GroundMaterial.VARROCK_PATHS_LIGHT, new Properties().setShiftLightness(10)),
	LUMBRIDGE_PATHS(10, Area.LUMBRIDGE, GroundMaterial.GRAVEL, new Properties().setShiftLightness(12).setHue(7).setSaturation(1)),
	LUMBRIDGE_CASTLE_ENTRYWAY_1(2, Area.LUMBRIDGE_CASTLE_ENTRYWAY, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	LUMBRIDGE_CASTLE_ENTRYWAY_2(3, Area.LUMBRIDGE_CASTLE_ENTRYWAY, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),

	// Varrock
	VARROCK_MUSEUM_FLOOR(56, Area.VARROCK_MUSEUM, GroundMaterial.TILES_2x2_2_GLOSS, new Properties().setBlended(false)),
	VARROCK_MUSEUM_BASEMENT_FLOOR(56, Area.VARROCK_MUSEUM_BASEMENT, GroundMaterial.TILES_2x2_2_GLOSS, new Properties().setBlended(false)),
	VARROCK_JULIETS_FLOWER_BED(81, Area.VARROCK_JULIETS_HOUSE_FLOWER_BED, GroundMaterial.DIRT, new Properties().setBlended(true)),
	VARROCK_JULIETS_HOUSE_HARD_FLOORS(-85, Area.VARROCK_JULIETS_HOUSE, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	VARROCK_JULIETS_HOUSE_CARPET_RED(-93, Area.VARROCK_JULIETS_HOUSE, GroundMaterial.CARPET, new Properties().setBlended(false)),
	VARROCK_JULIETS_HOUSE_CARPET_PINK(-96, Area.VARROCK_JULIETS_HOUSE, GroundMaterial.CARPET, new Properties().setBlended(false)),
	VARROCK_JOLLY_BOAR_INN_KITCHEN(-84, Area.VARROCK_JOLLY_BOAR_INN, GroundMaterial.NONE, new Properties().setBlended(false)),
	VARROCK_CHURCH(-83, Area.VARROCK_CHURCH, GroundMaterial.NONE, new Properties().setBlended(false)),
	VARROCK_ANVILS(81, Area.VARROCK_ANVILS, GroundMaterial.DIRT),
	VARROCK_BUILDING_RUINS(81, Area.VARROCK_BUILDING_RUINS, GroundMaterial.DIRT),
	VARROCK_BUILDING_FLOOR_1(81, Area.VARROCK, GroundMaterial.TILE_SMALL, new Properties().setBlended(false)),
	VARROCK_BUILDING_FLOOR_2(4, Area.VARROCK, GroundMaterial.NONE, new Properties().setBlended(false)),
	VARROCK_PLANT_PATCHES(89, Area.VARROCK, GroundMaterial.DIRT, new Properties().setBlended(false)),
	VARROCK_EAST_BANK(-83, Area.VARROCK_EAST_BANK, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	VARROCK_ROOF_GRAVEL(2, Area.VARROCK_CASTLE, GroundMaterial.GRAVEL, new Properties().setBlended(false)),
	VARROCK_ROOF_ARCHERY_FLOOR_1(-83, Area.VARROCK_CASTLE, GroundMaterial.DIRT, new Properties().setBlended(false)),
	VARROCK_ROOF_ARCHERY_FLOOR_2(-84, Area.VARROCK_CASTLE, GroundMaterial.DIRT, new Properties().setBlended(false)),

	// Digsite
	DIGSITE_DOCK(93, Area.DIGSITE_DOCK, GroundMaterial.TILES_2x2_1_GLOSS, new Properties().setBlended(false)),

	// Al Kharid
	MAGE_TRAINING_ARENA_FLOOR(-122, Area.MAGE_TRAINING_ARENA, GroundMaterial.TILES_2x2_2_GLOSS, new Properties().setBlended(false)),
	AL_KHARID_FLOOR_1(26, Area.AL_KHARID_BUILDINGS, GroundMaterial.TILES_2x2_2_SEMIGLOSS, new Properties().setBlended(false).setShiftSaturation(-1).setShiftLightness(7)),
	AL_KHARID_FLOOR_2(1, Area.AL_KHARID_BUILDINGS, GroundMaterial.TILES_2x2_2_SEMIGLOSS, new Properties().setBlended(false)),
	AL_KHARID_FLOOR_MARBLE_1(3, Area.AL_KHARID_BUILDINGS, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),
	AL_KHARID_FLOOR_MARBLE_2(4, Area.AL_KHARID_BUILDINGS, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	KHARID_PATHS_1(26, Area.KHARID_DESERT_REGION, GroundMaterial.DIRT, new Properties().setSaturation(2).setHue(6).setShiftLightness(5)),
	KHARID_PATHS_2(76, Area.KHARID_DESERT_REGION, GroundMaterial.DIRT, new Properties().setSaturation(3).setHue(6).setShiftLightness(-10)),
	KHARID_PATHS_3(25, Area.KHARID_DESERT_REGION, GroundMaterial.DIRT, new Properties().setSaturation(3).setHue(6)),

	// Falador
	FALADOR_PATHS(-119, Area.FALADOR, GroundMaterial.FALADOR_PATHS, new Properties().setHue(7).setSaturation(1).setShiftLightness(7)),
	FALADOR_HAIRDRESSER_TILE_1(77, Area.FALADOR_HAIRDRESSER, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	FALADOR_HAIRDRESSER_TILE_2(123, Area.FALADOR_HAIRDRESSER, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	FALADOR_PARTY_ROOM_TILE_1(33, Area.FALADOR_PARTY_ROOM, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	FALADOR_PARTY_ROOM_TILE_2(123, Area.FALADOR_PARTY_ROOM, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	FALADOR_BUILDING_FLOOR_1(123, Area.FALADOR, GroundMaterial.TILES_2x2_1_GLOSS, new Properties().setBlended(false)),
	FALADOR_BUILDING_FLOOR_2(33, Area.FALADOR, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	FALADOR_BUILDING_FLOOR_3(77, Area.FALADOR, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	FALADOR_BUILDING_FLOOR_4(13, Area.FALADOR, GroundMaterial.NONE, new Properties().setBlended(false)),

	// Rimmington
	CRAFTING_GUILD_TILE_1(2, Area.CRAFTING_GUILD, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	CRAFTING_GUILD_TILE_2(3, Area.CRAFTING_GUILD, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	CRAFTING_GUILD_TILE_3(4, Area.CRAFTING_GUILD, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),

	// Edgeville
	EDGEVILLE_BANK_TILE_1(3, Area.EDGEVILLE_BANK, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	EDGEVILLE_BANK_TILE_2(4, Area.EDGEVILLE_BANK, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	EDGEVILLE_BANK_SURROUNDING_STONE(10, Area.EDGEVILLE_BANK_SURROUNDING, GroundMaterial.VARROCK_PATHS),
	EDGEVILLE_DORIS_HOUSE_FLOOR(119, Area.EDGEVILLE_DORIS_HOUSE, GroundMaterial.TILE_SMALL),
	EDGEVILLE_MONASTERY_FLOOR(10, Area.EDGEVILLE_MONASTERY, GroundMaterial.GRAVEL, new Properties().setBlended(false)),

	// Burthorpe
	HEROES_GUILD_TILE_1(3, Area.HEROES_GUILD, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	HEROES_GUILD_TILE_2(4, Area.HEROES_GUILD, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	WARRIORS_GUILD_TILE_1(10, Area.WARRIORS_GUILD_FLOOR_2, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	WARRIORS_GUILD_TILE_2(11, Area.WARRIORS_GUILD_FLOOR_2, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	WARRIORS_GUILD_TILE_BLUE(87, Area.WARRIORS_GUILD, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	WARRIORS_GUILD_FLOOR_1(11, Area.WARRIORS_GUILD, GroundMaterial.VARROCK_PATHS, new Properties().setBlended(false)),
	WARRIORS_GUILD_CARPET(86, Area.WARRIORS_GUILD, GroundMaterial.CARPET, new Properties().setBlended(false)),

	// Seers
	SEERS_BANK_TILE_1(3, Area.SEERS_BANK, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	SEERS_BANK_TILE_2(4, Area.SEERS_BANK, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	SEERS_BANK_TILE_3(8, Area.SEERS_BANK, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),

	// Catherby
	CATHERBY_BEACH_OBELISK_WATER_FIX(6, Area.CATHERBY_BEACH_OBELISK_WATER_FIX, WaterType.WATER_FLAT),
	CATHERBY_BEACH_LADDER_FIX(11, Area.CATHERBY_BEACH_LADDER_FIX, GroundMaterial.NONE, new Properties().setBlended(false)),
	CATHERBY_BANK_TILE_1(3, Area.CATHERBY_BANK, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	CATHERBY_BANK_TILE_2(4, Area.CATHERBY_BANK, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),

	// Ardougne
	EAST_ARDOUGNE_PATHS_1(10, Area.EAST_ARDOUGNE, GroundMaterial.VARROCK_PATHS_LIGHT, new Properties().setShiftLightness(6)),
	WIZARD_HOUSE_TILE_LIGHT(38, Area.EAST_ARDOUGNE, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	WIZARD_HOUSE_TILE_DARK(40, Area.EAST_ARDOUGNE, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),

	// Yanille
	YANILLE_BANK_TILE_1(3, Area.YANILLE_BANK, GroundMaterial.MARBLE_1_GLOSS, new Properties().setBlended(false)),
	YANILLE_BANK_TILE_2(4, Area.YANILLE_BANK, GroundMaterial.MARBLE_2_GLOSS, new Properties().setBlended(false)),
	YANILLE_HUNTER_SHOP_FLOOR(16, Area.YANILLE, GroundMaterial.CARPET, new Properties().setBlended(false)),
	GUTANOTH_CAVE(29, Area.GUTANOTH_CAVE, WaterType.SWAMP_WATER_FLAT),

	// Draynor
	WISE_OLD_MANS_HOUSE_CARPET(86, Area.DRAYNOR, GroundMaterial.CARPET, new Properties().setBlended(false)),
	// Draynor manor
	DRAYNOR_MANOR_TILE_DARK(2, Area.DRAYNOR_MANOR_INTERIOR, GroundMaterial.MARBLE_1, new Properties().setBlended(false)),
	DRAYNOR_MANOR_TILE_LIGHT(10, Area.DRAYNOR_MANOR_INTERIOR, GroundMaterial.MARBLE_2, new Properties().setBlended(false)),
	DRAYNOR_MANOR_TILE_SMALL(11, Area.DRAYNOR_MANOR_INTERIOR, GroundMaterial.TILE_SMALL, new Properties().setBlended(false)),
	DRAYNOR_MANOR_WOOD(119, Area.DRAYNOR_MANOR_INTERIOR, GroundMaterial.WOOD_PLANKS_1, new Properties().setBlended(false)),
	DRAYNOR_MANOR_CARPET(127, Area.DRAYNOR_MANOR_INTERIOR, GroundMaterial.CARPET, new Properties().setBlended(false)),
	DRAYNOR_MANOR_ENTRANCE_DIRT_1(2, Area.DRAYNOR_MANOR, GroundMaterial.DIRT),
	DRAYNOR_MANOR_ENTRANCE_DIRT_2(127, Area.DRAYNOR_MANOR, GroundMaterial.DIRT),

	// Misthalin Mystery
	MISTHALIN_MYSTERY_MANOR_TILE_DARK_1(11, Area.MISTHALIN_MYSTERY_MANOR, GroundMaterial.MARBLE_2, new Properties().setBlended(false)),
	MISTHALIN_MYSTERY_MANOR_TILE_DARK_2(10, Area.MISTHALIN_MYSTERY_MANOR, GroundMaterial.MARBLE_2, new Properties().setBlended(false)),
	MISTHALIN_MYSTERY_MANOR_TILE_LIGHT_1(127, Area.MISTHALIN_MYSTERY_MANOR, GroundMaterial.MARBLE_1, new Properties().setBlended(false)),
	MISTHALIN_MYSTERY_MANOR_TILE_LIGHT_2(2, Area.MISTHALIN_MYSTERY_MANOR, GroundMaterial.MARBLE_1, new Properties().setBlended(false)),
	MISTHALIN_MYSTERY_MANOR_WOOD(119, Area.MISTHALIN_MYSTERY_MANOR, GroundMaterial.WOOD_PLANKS_1, new Properties().setBlended(false)),

	// Castle Wars
	CASTLE_WARS_LOBBY_FLOOR(14, Area.CASTLE_WARS_LOBBY, GroundMaterial.TILES_2x2_2_GLOSS, new Properties().setSaturation(0).setShiftLightness(4).setBlended(false)),
	CASTLE_WARS_SARADOMIN_FLOOR_CENTER(15, Area.CASTLE_WARS_ARENA_SARADOMIN_SIDE, GroundMaterial.FALADOR_PATHS, new Properties().setSaturation(1).setShiftLightness(18).setHue(9).setBlended(false)),
	CASTLE_WARS_SARADOMIN_FLOOR(26, Area.CASTLE_WARS, GroundMaterial.FALADOR_PATHS, new Properties().setSaturation(1).setShiftLightness(5).setBlended(false)),
	CASTLE_WARS_ZAMORAK_FLOOR(15, Area.CASTLE_WARS, GroundMaterial.TILES_2x2_2_GLOSS, new Properties().setSaturation(1).setShiftLightness(5).setBlended(false)),

	// Zanaris
	COSMIC_ENTITYS_PLANE_ABYSS(37, Area.COSMIC_ENTITYS_PLANE, GroundMaterial.NONE, new Properties().setLightness(0).setBlended(false)),

	// Morytania
	MORYTANIA_SLAYER_TOWER(102, Area.MORYTANIA_SLAYER_TOWER, GroundMaterial.VARROCK_PATHS_LIGHT),
	ABANDONED_MINE_ROCK(11, Area.MORYTANIA, GroundMaterial.DIRT),
	TRUE_BLOOD_ALTAR_BLOOD(72, Area.TRUE_BLOOD_ALTAR, WaterType.BLOOD),

	// Tirannwn
	POISON_WASTE(85, Area.POISON_WASTE, WaterType.POISON_WASTE),

	// Fossil Island
	ANCIENT_MUSHROOM_POOL(95, Area.FOSSIL_ISLAND, WaterType.SWAMP_WATER_FLAT),

	// Zeah
	XERICS_LOOKOUT_TILE_1(50, Area.XERICS_LOOKOUT, GroundMaterial.TILES_2x2_2, new Properties().setBlended(false)),
	XERICS_LOOKOUT_TILE_2(2, Area.XERICS_LOOKOUT, GroundMaterial.TILES_2x2_2, new Properties().setBlended(false)),
	HOSIDIUS_STONE_FLOOR(123, Area.HOSIDIUS, GroundMaterial.FALADOR_PATHS),
	BLOOD_ALTAR_BLOOD(72, Area.BLOOD_ALTAR, WaterType.BLOOD),
	SHAYZIEN_PAVED_AREA_1(2, Area.SHAYZIEN, GroundMaterial.GRAVEL, new Properties().setBlended(false)),
	SHAYZIEN_PAVED_AREA_2(-117, Area.SHAYZIEN, GroundMaterial.GRAVEL, new Properties().setBlended(false)),
	SHAYZIEN_COMBAT_RING_FLOOR_1(30, Area.SHAYZIEN_COMBAT_RING, GroundMaterial.CARPET, new Properties().setBlended(false)),
	SHAYZIEN_COMBAT_RING_FLOOR_2(37, Area.SHAYZIEN_COMBAT_RING, GroundMaterial.CARPET, new Properties().setBlended(false)),
	SHAYZIEN_COMBAT_RING_FLOOR_3(72, Area.SHAYZIEN_COMBAT_RING, GroundMaterial.CARPET, new Properties().setBlended(false)),
	SHAYZIEN_COMBAT_RING_FLOOR_4(73, Area.SHAYZIEN_COMBAT_RING, GroundMaterial.CARPET, new Properties().setBlended(false)),
	MESS_HALL_KITCHEN_TILE_1(30, Area.MESS_HALL_KITCHEN, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	MESS_HALL_KITCHEN_TILE_2(99, Area.MESS_HALL_KITCHEN, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),
	// Chambers of Xeric
	MOUNT_QUIDAMORTEM_SYMBOL(-93, Area.MOUNT_QUIDAMORTEM, GroundMaterial.DIRT, new Properties().setBlended(false)),
	// Kebos Lowlands
	LIZARDMAN_TEMPLE_WATER(-100, Area.LIZARDMAN_TEMPLE, WaterType.SWAMP_WATER_FLAT),

	// Temple of the Eye
	TEMPLE_OF_THE_EYE_INCORRECT_WATER(-100, Area.TEMPLE_OF_THE_EYE, GroundMaterial.DIRT),

	// God Wars Dungeon (GWD)
	GWD_WATER(104, Area.GOD_WARS_DUNGEON, WaterType.ICE_FLAT),

	// Purple symbol near Wintertodt
	PURPLE_SYMBOL(68, Area.ZEAH_SNOWY_NORTHERN_REGION, GroundMaterial.DIRT, new Properties().setBlended(false)),

	// Burthorpe games room
	GAMES_ROOM_FLOOR(22, Area.GAMES_ROOM, GroundMaterial.WOOD_PLANKS_1, new Properties().setBlended(false)),

	CRANDOR_GROUND_1(11, Area.CRANDOR, GroundMaterial.GRAVEL),

	FISHING_TRAWLER_BOAT_PORT_KHAZARD_FIX(42, Area.FISHING_TRAWLER_BOAT_PORT_KHAZARD, WaterType.WATER),
	FISHING_TRAWLER_BOAT_FLOODED(6, Area.FISHING_TRAWLER_BOAT_FLOODED, WaterType.WATER_FLAT),

	// Mind Altar
	MIND_ALTAR_TILE_1(3, Area.MIND_ALTAR, GroundMaterial.MARBLE_1_SEMIGLOSS, new Properties().setBlended(false)),
	MIND_ALTAR_TILE_4(4, Area.MIND_ALTAR, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),
	MIND_ALTAR_TILE_3(10, Area.MIND_ALTAR, GroundMaterial.MARBLE_2_SEMIGLOSS, new Properties().setBlended(false)),

	// Dragon Slayer II
	DS2_SHIPS_WATER(6, Area.DS2_SHIPS, WaterType.WATER_FLAT),
	DS2_FLEET_ATTACKED(6, Area.DS2_FLEET_ATTACKED, WaterType.WATER_FLAT),

	// Camdozaal (Below Ice Mountain)
	CAMDOZAAL_WATER(-75, Area.CAMDOZAAL, WaterType.WATER),

	// Pest Control
	PEST_CONTROL_LANDER_WATER_FIX_1(-95, Area.PEST_CONTROL_LANDER_WATER_FIX, WaterType.WATER),
	PEST_CONTROL_LANDER_WATER_FIX_2(42, Area.PEST_CONTROL_LANDER_WATER_FIX, WaterType.WATER),

	// Barbarian Assault
	BA_WAITING_ROOM_NUMBERS(89, Area.BARBARIAN_ASSAULT_WAITING_ROOMS, GroundMaterial.DIRT, new Properties().setBlended(false)),

	// POHs
	POH_DESERT_INDOORS(26, Area.PLAYER_OWNED_HOUSE, GroundMaterial.TILES_2x2_2, new Properties().setBlended(false)),
	POH_TWISTED_INDOORS(99, Area.PLAYER_OWNED_HOUSE, GroundMaterial.TILES_2x2_2, new Properties().setBlended(false)),

	// Random events
	PRISON_PETE_TILE_1(2, Area.RANDOM_EVENT_PRISON_PETE, GroundMaterial.MARBLE_1, new Properties().setBlended(false)),
	PRISON_PETE_TILE_2(-125, Area.RANDOM_EVENT_PRISON_PETE, GroundMaterial.MARBLE_2, new Properties().setBlended(false)),
	
	// GOTR Entrance fix
	TEMPLE_OF_THE_EYE_ENTRANCE(0, Area.TEMPLE_OF_THE_EYE_ENTRANCE_FIX, GroundMaterial.DIRT, new Properties().setShiftLightness(-10).setBlended(false)),
	TEMPLE_OF_THE_EYE_ENTRANCE_2(-53, Area.TEMPLE_OF_THE_EYE_ENTRANCE_FIX, GroundMaterial.DIRT, new Properties().setShiftLightness(-10).setBlended(false)),



	// default overlays
	OVERLAY_N128(-128, WaterType.WATER),
	OVERLAY_N124(-124, GroundMaterial.DIRT),
	OVERLAY_N122(-122, GroundMaterial.TILES_2x2_2_GLOSS),
	OVERLAY_N119(-119, GroundMaterial.FALADOR_PATHS),
	OVERLAY_N105(-105, WaterType.WATER),
	OVERLAY_N100(-100, WaterType.SWAMP_WATER),
	OVERLAY_N98(-98, WaterType.WATER),
	OVERLAY_N93(-93, GroundMaterial.CARPET),
	OVERLAY_N85(-85, GroundMaterial.VARROCK_PATHS),
	OVERLAY_N77(-77, GroundMaterial.VARROCK_PATHS),
	OVERLAY_N76(-76, GroundMaterial.GRAVEL),
	OVERLAY_N84(-84, GroundMaterial.DIRT),
	OVERLAY_N83(-83, GroundMaterial.DIRT),
	OVERLAY_N82(-82, GroundMaterial.TILE_DARK),
	OVERLAY_N49(-49, GroundMaterial.SAND_BRICK),
	OVERLAY_2(2, GroundMaterial.GRAVEL),
	OVERLAY_3(3, GroundMaterial.GRAVEL),
	OVERLAY_4(4, GroundMaterial.GRAVEL),
	OVERLAY_5(5, GroundMaterial.WOOD_PLANKS_1),
	OVERLAY_6(6, WaterType.WATER),
	OVERLAY_7(7, WaterType.SWAMP_WATER),
	OVERLAY_8(8, GroundMaterial.GRAVEL),
	OVERLAY_10(10, GroundMaterial.GRAVEL),
	OVERLAY_11(11, GroundMaterial.VARROCK_PATHS),
	OVERLAY_12(12, GroundMaterial.STONE_PATTERN),
	OVERLAY_13(13, GroundMaterial.CARPET, new Properties().setBlended(false)),
	OVERLAY_14(14, GroundMaterial.DIRT),
	OVERLAY_15(15, GroundMaterial.DIRT),
	LAVA(19, GroundMaterial.HD_LAVA, new Properties().setHue(0).setSaturation(0).setShiftLightness(127).setBlended(false)),
	OVERLAY_20(20, GroundMaterial.MARBLE_DARK),
	OVERLAY_21(21, GroundMaterial.DIRT),
	OVERLAY_22(22, GroundMaterial.DIRT),
	OVERLAY_23(23, GroundMaterial.DIRT),
	OVERLAY_25(25, GroundMaterial.SAND),
	OVERLAY_26(26, GroundMaterial.SAND),
	OVERLAY_27(27, GroundMaterial.BRICK_BROWN, new Properties().setBlended(false)),
	OVERLAY_28(28, GroundMaterial.BRICK, new Properties().setBlended(false)),
	OVERLAY_29(29, GroundMaterial.GRASS_1),
	OVERLAY_30(30, GroundMaterial.SNOW_2),
	OVERLAY_32(32, GroundMaterial.CONCRETE),
	OVERLAY_33(33, GroundMaterial.SNOW_2),
	OVERLAY_35(35, GroundMaterial.WOOD_PLANKS_1),
	OVERLAY_41(41, WaterType.WATER),
	OVERLAY_46(46, GroundMaterial.BRICK_BROWN, new Properties().setBlended(false)),
	OVERLAY_49(49, GroundMaterial.VARIED_DIRT),
	OVERLAY_52(52, GroundMaterial.WOOD_PLANKS_1),
	OVERLAY_60(60, GroundMaterial.DIRT),
	OVERLAY_77(77, GroundMaterial.DIRT),
	OVERLAY_81(81, GroundMaterial.DIRT),
	OVERLAY_82(82, GroundMaterial.DIRT),
	OVERLAY_83(83, GroundMaterial.VARIED_DIRT),
	OVERLAY_84(84, GroundMaterial.SAND_BRICK), // DT pyramid
	OVERLAY_88(88, GroundMaterial.DIRT),
	OVERLAY_89(89, GroundMaterial.DIRT),
	OVERLAY_101(101, GroundMaterial.DIRT),
	OVERLAY_102(102, GroundMaterial.DIRT),
	OVERLAY_104(104, WaterType.WATER),
	OVERLAY_107(107, GroundMaterial.DIRT),
	OVERLAY_108(108, GroundMaterial.DIRT),
	OVERLAY_110(110, GroundMaterial.DIRT),
	OVERLAY_115(115, GroundMaterial.DIRT),
	OVERLAY_119(119, GroundMaterial.GRAVEL),
	OVERLAY_123(123, GroundMaterial.DIRT),

	// Seasonal
	WINTER_GRASS(-999, GroundMaterial.SNOW_1, new Properties().setHue(0).setSaturation(0).setShiftLightness(40).setBlended(true)),

	DEFAULT(-1, GroundMaterial.DIRT),
	;

	private final int id;
	private final Area area;
	private final GroundMaterial groundMaterial;
	private final WaterType waterType;
	private final boolean blended;
	private final boolean blendedAsUnderlay;
	private final int hue;
	private final int shiftHue;
	private final int saturation;
	private final int shiftSaturation;
	private final int lightness;
	private final int shiftLightness;

	private static class Properties
	{
		private boolean blended = true;
		private boolean blendedAsUnderlay = false;
		private int hue = -1;
		private int shiftHue = 0;
		private int saturation = -1;
		private int shiftSaturation = 0;
		private int lightness = -1;
		private int shiftLightness = 0;

		public Properties setBlended(boolean blended)
		{
			this.blended = blended;
			return this;
		}

		public Properties setBlendedAsUnderlay(boolean blendedAsUnderlay)
		{
			this.blendedAsUnderlay = blendedAsUnderlay;
			return this;
		}

		public Properties setHue(int hue)
		{
			this.hue = hue;
			return this;
		}

		public Properties setShiftHue(int shiftHue)
		{
			this.shiftHue = shiftHue;
			return this;
		}

		public Properties setSaturation(int saturation)
		{
			this.saturation = saturation;
			return this;
		}

		public Properties setShiftSaturation(int shiftSaturation)
		{
			this.shiftSaturation = shiftSaturation;
			return this;
		}

		public Properties setLightness(int lightness)
		{
			this.lightness = lightness;
			return this;
		}

		public Properties setShiftLightness(int shiftLightness)
		{
			this.shiftLightness = shiftLightness;
			return this;
		}
	}

	Overlay(int id, WaterType waterType)
	{
		this.id = id;
		this.area = Area.ALL;
		this.groundMaterial = waterType.getGroundMaterial();
		this.waterType = waterType;
		this.blended = false;
		this.blendedAsUnderlay = false;
		this.hue = -1;
		this.shiftHue = 0;
		this.saturation = -1;
		this.shiftSaturation = 0;
		this.lightness = -1;
		this.shiftLightness = 0;
	}

	Overlay(int id, Area area, WaterType waterType)
	{
		this.id = id;
		this.area = area;
		this.groundMaterial = waterType.getGroundMaterial();
		this.waterType = waterType;
		this.blended = false;
		this.blendedAsUnderlay = false;
		this.hue = -1;
		this.shiftHue = 0;
		this.saturation = -1;
		this.shiftSaturation = 0;
		this.lightness = -1;
		this.shiftLightness = 0;
	}

	Overlay(int id, Area area, GroundMaterial groundMaterial, Properties properties)
	{
		this.id = id;
		this.area = area;
		this.groundMaterial = groundMaterial;
		this.waterType = WaterType.NONE;
		this.blended = properties.blended;
		this.blendedAsUnderlay = properties.blendedAsUnderlay;
		this.hue = properties.hue;
		this.shiftHue = properties.shiftHue;
		this.saturation = properties.saturation;
		this.shiftSaturation = properties.shiftSaturation;
		this.lightness = properties.lightness;
		this.shiftLightness = properties.shiftLightness;
	}

	Overlay(int id, Area area, GroundMaterial groundMaterial)
	{
		this.id = id;
		this.area = area;
		this.groundMaterial = groundMaterial;
		this.waterType = WaterType.NONE;
		this.blended = true;
		this.blendedAsUnderlay = false;
		this.hue = -1;
		this.shiftHue = 0;
		this.saturation = -1;
		this.shiftSaturation = 0;
		this.lightness = -1;
		this.shiftLightness = 0;
	}

	Overlay(int id, GroundMaterial groundMaterial, Properties properties)
	{
		this.id = id;
		this.area = Area.ALL;
		this.groundMaterial = groundMaterial;
		this.waterType = WaterType.NONE;
		this.blended = properties.blended;
		this.blendedAsUnderlay = properties.blendedAsUnderlay;
		this.hue = properties.hue;
		this.shiftHue = properties.shiftHue;
		this.saturation = properties.saturation;
		this.shiftSaturation = properties.shiftSaturation;
		this.lightness = properties.lightness;
		this.shiftLightness = properties.shiftLightness;
	}

	Overlay(int id, GroundMaterial groundMaterial)
	{
		this.id = id;
		this.groundMaterial = groundMaterial;
		this.area = Area.ALL;
		this.waterType = WaterType.NONE;
		this.blended = true;
		this.blendedAsUnderlay = false;
		this.hue = -1;
		this.shiftHue = 0;
		this.saturation = -1;
		this.shiftSaturation = 0;
		this.lightness = -1;
		this.shiftLightness = 0;
	}

	private static final ListMultimap<Integer, Overlay> GROUND_MATERIAL_MAP;

	static
	{
		GROUND_MATERIAL_MAP = ArrayListMultimap.create();
		for (Overlay overlay : values())
		{
			GROUND_MATERIAL_MAP.put(overlay.id, overlay);
		}
	}

	public static Overlay getOverlay(int overlayId, Tile tile, Client client)
	{
		WorldPoint worldPoint = tile.getWorldLocation();

		if (client.isInInstancedRegion())
		{
			LocalPoint localPoint = tile.getLocalLocation();
			worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
		}

		int worldX = worldPoint.getX();
		int worldY = worldPoint.getY();
		int worldZ = worldPoint.getPlane();

		List<Overlay> overlays = GROUND_MATERIAL_MAP.get(overlayId);
		for (Overlay overlay : overlays)
		{
			if (overlay.area.containsPoint(worldX, worldY, worldZ))
			{
				return overlay;
			}
		}

		return Overlay.DEFAULT;
	}
}
