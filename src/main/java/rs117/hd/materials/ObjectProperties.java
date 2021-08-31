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
package rs117.hd.materials;

import java.util.HashMap;
import lombok.Getter;
import static net.runelite.api.ObjectID.*;

@Getter
public enum ObjectProperties
{
	NONE(Material.NONE, -1),

	// Trees
	TREES(Material.BARK, 1276, 1278, 1293, 1294, 1295, 37329, 2092, 10819, 10820, 10823, 10832, 10833, 10834),

	// Farming patches
	FARMING_PATCH_1(Material.DIRT_1, new Properties().setUvType(UvType.GROUND_PLANE), 7517),
	FARMING_PATCH_2(Material.GRUNGE_1, new Properties().setUvType(UvType.GROUND_PLANE), 7522),

	GRASS(Material.NONE, new Properties().setFlatNormals(true).setInheritTileColor(true), 1257, 1258, 3547, 3548, 3549, 4333, 4334, 4335, 4336, 4530, 4735, 4736, 4737, 4738, 4739, 4740, 4741, 4742, 4809, 4810, 4811, 4812, 4813, 4814, 5335, 5336, 5337, 5338, 5339, 5340, 5341, 5342, 5533, 5534, 5535, 5536, 6817, 6818, 6819, 6835, 6836, 6837, 6838, 7049, 7050, 9485, 9486, 9487, 9488, 9489, 9490, 9491, 9502, 9503, 9504, 9505, 9506, 9507, 13861, 13862, 13863, 14775, 14776, 14777, 14778, 16382, 16383, 16384, 16385, 19823, 19824, 19825, 19826, 19827, 19828, 19829, 19830, 19831, 19832, 19833, 19834, 19835, 19836, 19837, 19838, 20742, 20743, 20744, 20745, 23914, 23915, 31777, 31778, 31779, 31780, 34490, 34491, 34492),
	GRASS_MAINTAIN_ORIGINAL_COLOR(Material.NONE, new Properties().setFlatNormals(true), 16823, 16824, 16825, 16826),
	FERN(Material.NONE, new Properties().setInheritTileColor(true), 7518, 19827, 19833, 19839),

	// Lumbridge
	LUMBRIDGE_CASTLE_WALLS(Material.NONE, new Properties().setFlatNormals(true), 1651, 1911, 1912, 1913),

	// Varrock fountain floor
	VARROCK_FOUNTAIN_FLOOR(Material.GRUNGE_1, new Properties().setFlatNormals(true).setUvType(UvType.GROUND_PLANE), 7149, 7150, 7151, 7152, 7153),
	VARROCK_FOUNTAIN_STATUE(Material.GRUNGE_1, 7144),
	VARROCK_KNIGHT_STATUE(Material.MARBLE_1_SEMIGLOSS, 3642),
	// Grand Exchange
	GRAND_EXCHANGE_FLOOR(Material.GRUNGE_2, new Properties().setFlatNormals(true).setUvType(UvType.GROUND_PLANE), 9371, 10689, 10690, 10691, 10692, 10693, 10694, 10695, 10696, 10699, 10700, 10701, 10702, 10703, 10704, 10705, 10706, 10711, 10712, 10713, 11703, 11704, 11705, 11706, 11707, 11708, 11709, 11710, 11711, 11712, 11713, 11714, 11715, 11716, 11717, 15810, 16010, 16155),
	GRAND_EXCHANGE_WALL(Material.GRUNGE_1, new Properties().setFlatNormals(true), 23755, 23775, 23776, 23777, 23778, 23779, 23795, 23810, 23811),
	GRAND_EXCHANGE_CLAN_PORTAL(Material.GRUNGE_1, new Properties().setFlatNormals(true), 41724),
	VARROCK_WALLS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 436, 441, 443, 450, 455, 462, 477, 490, 493, 494, 495, 496, 497, 500, 501, 504, 505, 506, 511, 512, 517, 519, 520, 9269, 9265, 9264, 23734, 23735, 23736, 23737, 23738, 23739, 23740, 23741, 23742, 23743, 23744, 23745, 23746, 23747, 23757, 23758, 23759, 23775, 23780, 23783, 23784, 23794, 23797, 23801, 23802, 23803, 23804, 23805, 23806, 23807, 23809, 23810, 23811, 23812, 23813, 23814, 23815, 23816, 23817, 23818, 23819, 23820, 23821, 23822, 23823, 23824, 23825, 23826, 23827, 23828, 23829, 23835, 23836, 23837, 23838, 23839, 23840, 23841, 23842, 23843, 23844, 23859, 23860, 23861, 23862, 23863, 23864, 23865, 23874, 23875, 23876, 23877, 23878, 23879, 23880, 23881, 23882, 23883, 23884, 23885, 23907, 23908, 23909, 23910, 24428, 40285),

	// Falador
	FALADOR_WALLS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 23982, 23983, 23984, 23985, 23986, 23987, 23988, 23989, 23990, 23991, 23992, 23993, 23997, 23999, 24026, 24029, 24033, 24034, 24035, 24119, 24125, 24127, 24128, 24131, 24132, 24134, 24135, 24137, 24175, 24176, 24183, 24184, 24185, 24186, 24188, 24191, 24198, 24199, 24200, 24203, 24206, 24207, 24210, 24211, 24212, 24213, 24215, 24220, 24221, 24222, 24227, 24235, 24242, 24245, 24246, 24247, 24260, 24261, 24262, 24264, 24273, 24274, 23744, 23806, 23826, 23834, 23835, 23836, 23837, 23838, 23839, 23841, 23844, 23994, 23995, 23996, 23998, 24027, 24028, 24030, 24031, 24032, 24115, 24116, 24120, 24178, 24180, 24182, 24189, 24193, 24194, 24195, 24196, 24236, 24237, 24238, 24239, 24243, 24244, 24256, 24266, 24267, 24268, 24269, 24270, 24271, 24272),
	STATUE_OF_SARADOMIN_1(Material.MARBLE_1_SEMIGLOSS, 24043, 24044),
	STONE_SIGNPOST(Material.GRUNGE_1, 23970, 23971),
	FALADOR_STEPS_1(Material.GRUNGE_1, new Properties().setFlatNormals(true), 7386, 7387, 10729, 10730, 10731, 23885),
	FALADOR_UNKNOWN_1(Material.GRUNGE_1, 24022, 24023, 24024),

	// Al Kharid
	AL_KHARID_WALLS(Material.NONE, new Properties().setFlatNormals(true), 1415, 1416, 21799, 33348),
	// Citharede Abbey
	CITHAREDE_ABBEY_WALLS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 39725, 39726, 39727, 39728, 39729, 39730, 39731, 39732, 39733, 39734, 39735, 39736, 39737, 39738, 39739, 39740),

	// Seers Village
	SEERS_COURTHOUSE_WALLS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 25966, 25967, 25969, 25970, 25971, 25972, 25973, 25974, 25975, 25978, 25979, 25980, 25981, 26010),
	SEERS_BUILDING_WALLS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 25753, 25755, 25756, 25757, 25890, 25896, 25897, 25898, 25899, 25901, 25902, 25904, 25957, 25958, 25959, 25960, 25961, 25962, 25963, 25964, 25965, 25905),

	// Stone walls (north of Falador)
	STONE_WALL(Material.GRUNGE_1, new Properties().setFlatNormals(true), 979 , 5566, 5567, 5568, 5569, 5570),

	// Temple of Ikov
	STATUE_OF_A_WARRIOR_1(Material.MARBLE_2_GLOSS, 562, 566),

	// Karamja
	KARAMJA_DUNGEON_WALLS(Material.GRUNGE_1, 1428, 21719, 21725, 21726, 21729, 21730, 21736, 21737),

	// TzHaar
	TZHAAR_ROCK_GRADIENT(Material.ROCK_1, new Properties().setTzHaarRecolorType(TzHaarRecolorType.GRADIENT), 11818, 11819, 11820, 11821, 11822, 11823, 11824, 11825, 11826, 11827, 11828, 11829, 11833, 11834, 11836, 11837, 11838, 11839, 11840, 11841, 11842, 11971, 11972, 11973, 11974, 11981, 11983, 11984, 11985, 30269, 30270, 30271, 30272, 30273, 41013),
	TZHAAR_GRADIENT(Material.NONE, new Properties().setTzHaarRecolorType(TzHaarRecolorType.GRADIENT), 11847, 11848, 11849, 11850),
	TZHAAR_ROCK_HUESHIFT(Material.ROCK_1, new Properties().setTzHaarRecolorType(TzHaarRecolorType.HUE_SHIFT), 26723, 26724, 26725, 30263, 30264, 30265),

	// Ape Atoll
	APE_ATOLL_DUNGEON_WALLS(Material.GRUNGE_1, 4898),

	// Zeah
	SHAYZIEN_SHAY_SHRINE(Material.MARBLE_3_GLOSS, CAT_SHRINE),
	SHAYZIEN_HERO_STATUE(Material.GRUNGE_1, DRAGON_STATUE),
	SHAYZIEN_DRAGON_STATUE(Material.MARBLE_2_GLOSS, SHAYZIEN_STATUE_42178),
	SHAYZIEN_GRAVE(Material.GRUNGE_1, GRAVE_28451),
	SHAYZIEN_CEREMONIAL_PILLAR(Material.BLANK_GLOSS, new Properties().setFlatNormals(true), CEREMONIAL_PILLAR, CEREMONIAL_PILLAR_42168, CEREMONIAL_PILLAR_42169, CEREMONIAL_PILLAR_42170, CEREMONIAL_PILLAR_42171, CEREMONIAL_PILLAR_42172, CEREMONIAL_PILLAR_42173, CEREMONIAL_PILLAR_42174, CEREMONIAL_PILLAR_42175, CEREMONIAL_PILLAR_42176, CEREMONIAL_PILLAR_42177, CEREMONIAL_PILLAR_42179, CEREMONIAL_PILLAR_42180),
	SHAYZIEN_WALLS_ROCK(Material.ROCK_1, new Properties().setFlatNormals(true), 14543, 14544, 14545, 14546, 14547, 14548, 14549, 14550, 14551, 14558, 42110, 42111, 42116),
	SHAYZIEN_WALLS_WOOD(Material.GRUNGE_1, new Properties().setFlatNormals(true), 42107, 42108, 42109, 42112, 42113),
	SHAYZIEN_STAIRS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 42193, 42195, 42196),
	STATUE_BUST(Material.MARBLE_1_GLOSS, 26129),

	// Castle Wars
	CASTLE_WARS_WALLS(Material.NONE, new Properties().setFlatNormals(true), 1620, 1622, 1631, 4409, 4410, 4445, 4446, 4447, 4908),
	CASTLE_WARS_UNDERGROUND_WALLS(Material.GRUNGE_1, 1417, 1434, 1435, 4448),
	CASTLE_WARS_UNDERGROUND_ROCKS(Material.GRUNGE_1, 314, 315, 316, 317, 318, 319, 320, 321),
	CASTLE_WARS_DECORATION(Material.NONE, new Properties().setFlatNormals(true), 4435, 4436),

	// Morytania
	MORYTANIA_SLAYER_TOWER_WALLS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 2114, 2160, 2161, 2162, 2163, 2164, 2165, 2166, 2167, 2168, 2169, 2170, 2171, 2172, 2173, 2173, 4332),
	// Theatre of Blood
	TOB_VERZIK_TILE_FLOOR(Material.BLANK_SEMIGLOSS, 32719, 32720, 32721, 32722, 32723, 32724, 32725, 32726, 32727, 32728),

	// Chambers of Xeric
	COX_OUTSIDE_RUINS(Material.GRUNGE_1, new Properties().setFlatNormals(true), 29921, 29922, 29923, 29924, 29925, 29926, 29927, 29928, 29929, 29930, 29931, 29932, 29933, 29934, 29935, 29936, 29937, 29938, 29939, 29940, 29941, 29942, 29943),
	COX_PILLAR(Material.GRUNGE_1, new Properties().setFlatNormals(true), 29806, 29807, 29808, 29809, 29810),

	// Unknown
	UNKNOWN_1(Material.GRUNGE_1, new Properties().setFlatNormals(true), 677),
	UNKNOWN_2(Material.NONE, new Properties().setFlatNormals(true), 1602, 2569, 3089, 3090, 3091, 3096, 3097, 3102, 3103, 3111, 3112, 3113, 3114, 3192, 10748, 10767, 10770),
	UNKNOWN_3(Material.GRUNGE_1, 2141, 3669, 3714, 3737, 3738, 3759, 3760, 3805, 3806, 3807, 3808, 3812, 6820, 6822, 6826, 6827),
	UNKNOWN_4(Material.NONE, new Properties().setFlatNormals(true), 39617),
	UNKNOWN_5(Material.GRUNGE_1, 29032),
	UNKNOWN_6(Material.NONE, new Properties().setFlatNormals(true), 436, 441, 443, 455, 458, 461, 462, 477),
	UNKNOWN_7(Material.GRUNGE_1, 679),
	UNKNOWN_8(Material.NONE, new Properties().setFlatNormals(true), 10743, 10744, 10749, 10750, 10769, 17349),
	UNKNOWN_9(Material.NONE, new Properties().setFlatNormals(true), 3110, 3174),
	UNKNOWN_10(Material.GRUNGE_1, 3709, 3724, 3803, 3804, 3948, 3950),
	UNKNOWN_11(Material.GRUNGE_2, 7057, 7103, 7104, 7105, 7106, 7107),

	;

	private final int[] id;
	private final Material material;
	private final boolean flatNormals;
	private final UvType uvType;
	private final TzHaarRecolorType tzHaarRecolorType;
	private final boolean inheritTileColor;

	private static class Properties
	{
		private boolean flatNormals = false;
		private UvType uvType = UvType.GEOMETRY;
		private TzHaarRecolorType tzHaarRecolorType = TzHaarRecolorType.NONE;
		private boolean inheritTileColor = false;

		public Properties setFlatNormals(boolean flatNormals)
		{
			this.flatNormals = flatNormals;
			return this;
		}

		public Properties setUvType(UvType uvType)
		{
			this.uvType = uvType;
			return this;
		}

		public Properties setTzHaarRecolorType(TzHaarRecolorType tzHaarRecolorType)
		{
			this.tzHaarRecolorType = tzHaarRecolorType;
			return this;
		}

		public Properties setInheritTileColor(boolean inheritTileColor)
		{
			this.inheritTileColor = inheritTileColor;
			return this;
		}
	}

	ObjectProperties(Material material, int... ids)
	{
		this.id = ids;
		this.material = material;
		this.flatNormals = false;
		this.uvType = UvType.GEOMETRY;
		this.tzHaarRecolorType = TzHaarRecolorType.NONE;
		this.inheritTileColor = false;
	}

	ObjectProperties(Material material, Properties properties, int... ids)
	{
		this.id = ids;
		this.material = material;
		this.flatNormals = properties.flatNormals;
		this.uvType = properties.uvType;
		this.tzHaarRecolorType = properties.tzHaarRecolorType;
		this.inheritTileColor = properties.inheritTileColor;
	}

	private static final HashMap<Integer, ObjectProperties> OBJECT_ID_MAP;

	static
	{
		OBJECT_ID_MAP = new HashMap<>();
		for (ObjectProperties objectProperties : values())
		{
			for (int id : objectProperties.id)
			{
				OBJECT_ID_MAP.put(id, objectProperties);
			}
		}
	}

	public static ObjectProperties getObjectProperties(int objectId)
	{
		return OBJECT_ID_MAP.getOrDefault(objectId, ObjectProperties.NONE);
	}
}
