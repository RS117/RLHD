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

@Getter
public enum Material
{
	// default
	NONE(-1),

	// reserve first 128 materials for vanilla OSRS texture ids
	TRAPDOOR(0),
	WATER_FLAT(1),
	BRICK(2),
	WOOD_PLANKS_1(3, new Properties().setSpecular(0.35f, 30f)),
	DOOR(4),
	DARK_WOOD(5),
	ROOF_SHINGLES_1(6, new Properties().setSpecular(0.5f, 30f)),
	TEXTURE_7(7),
	LEAVES_1(8, new Properties().setTextureScale(1.3f, 1.0f)),
	TEXTURE_9(9),
	TEXTURE_10(10),
	CONCRETE(11),
	IRON_FENCE(12),
	PAINTING_LANDSCAPE(13),
	PAINTING_KING(14),
	MARBLE_DARK(15, new Properties().setSpecular(1.1f, 380f)),
	TEXTURE_16(16),
	TEXTURE_17(17),
	STRAW(18),
	NET(19),
	BOOKCASE(20),
	TEXTURE_21(21),
	WOOD_PLANKS_2(22, new Properties().setSpecular(0.35f, 30f)),
	BRICK_BROWN(23),
	WATER_FLAT_2(24),
	SWAMP_WATER_FLAT(25),
	SPIDER_WEB(26),
	TEXTURE_27(27),
	MOSS(28),
	PALM_LEAF(29),
	WILLOW_LEAVES(30, new Properties().setTextureScale(1.025f, 1.0f)),
	LAVA(31, new Properties().setEmissive(1).setDisplacement(235, 0.05f, 36f, 22f).setScroll(0f, 3f)),
	TEXTURE_32(32),
	MAPLE_LEAVES(33, new Properties().setTextureScale(1.3f, 1.0f)),
	MAGIC_STARS(34, new Properties().setEmissive(1.0f)),
	SAND_BRICK(35),
	TEXTURE_36(36),
	CHAIN(37),
	TEXTURE_38(38),
	PAINTING_ELF(39),
	FIRE_CAPE(40, new Properties().setEmissive(1).setDisplacement(235, 0.05f, 12f, 4f).setScroll(0f, -3f)),
	LEAVES_2(41, new Properties().setTextureScale(1.1f, 1.1f)),
	MARBLE(42, new Properties().setSpecular(1.0f, 400f)),
	TILE_DARK(43),
	TEXTURE_44(44),
	TEXTURE_45(45),
	STONE_PATTERN(46),
	TEXTURE_47(47),
	HIEROGLYPHICS(48),
	TEXTURE_49(49),
	TEXTURE_50(50),
	TEXTURE_51(51),
	SNOW_FLAKES(52),
	TEXTURE_53(53),
	TEXTURE_54(54),
	TEXTURE_55(55),
	TEXTURE_56(56),
	TEXTURE_57(57),
	TEXTURE_58(58),
	INFERNAL_CAPE(59, new Properties().setEmissive(1).setDisplacement(235, 0.02f, 12f, 4f).setScroll(0f, 0f)),
	TEXTURE_60(60),
	TEXTURE_61(61),
	TEXTURE_62(62),
	TEXTURE_63(63),
	TEXTURE_64(64),
	TEXTURE_65(65),
	TEXTURE_66(66),
	TEXTURE_67(67),
	TEXTURE_68(68),
	TEXTURE_69(69),
	TEXTURE_70(70),
	TEXTURE_71(71),
	TEXTURE_72(72),
	TEXTURE_73(73),
	TEXTURE_74(74),
	TEXTURE_75(75),
	TEXTURE_76(76),
	TEXTURE_77(77),
	TEXTURE_78(78),
	TEXTURE_79(79),
	TEXTURE_80(80),
	TEXTURE_81(81),
	TEXTURE_82(82),
	TEXTURE_83(83),
	TEXTURE_84(84),
	TEXTURE_85(85),
	TEXTURE_86(86),
	TEXTURE_87(87),
	TEXTURE_88(88),
	SHAYZIEN_LEAVES_1(89),
	SHAYZIEN_LEAVES_2(90, new Properties().setTextureScale(1.1f, 1.1f)),
	TEXTURE_91(91),
	TEXTURE_92(92),
	TEXTURE_93(93),
	TEXTURE_94(94),
	TEXTURE_95(95),
	TEXTURE_96(96),
	TEXTURE_97(97),
	TEXTURE_98(98),
	TEXTURE_99(99),
	TEXTURE_100(100),
	TEXTURE_101(101),
	TEXTURE_102(102),
	TEXTURE_103(103),
	TEXTURE_104(104),
	TEXTURE_105(105),
	TEXTURE_106(106),
	TEXTURE_107(107),
	TEXTURE_108(108),
	TEXTURE_109(109),
	TEXTURE_110(110),
	TEXTURE_111(111),
	TEXTURE_112(112),
	TEXTURE_113(113),
	TEXTURE_114(114),
	TEXTURE_115(115),
	TEXTURE_116(116),
	TEXTURE_117(117),
	TEXTURE_118(118),
	TEXTURE_119(119),
	TEXTURE_120(120),
	TEXTURE_121(121),
	TEXTURE_122(122),
	TEXTURE_123(123),
	TEXTURE_124(124),
	TEXTURE_125(125),
	TEXTURE_126(126),
	TEXTURE_127(127),

	WHITE(200),
	GRAY_25(201),
	GRAY_50(202),
	GRAY_75(203),
	BLACK(204),

	BLANK_GLOSS(200, new Properties().setSpecular(0.9f, 280f)),
	BLANK_SEMIGLOSS(200, new Properties().setSpecular(0.35f, 80f)),

	SNOW_1(205),
	SNOW_2(206),
	SNOW_3(207),
	SNOW_4(208),

	GRASS_1(209),
	GRASS_2(210),
	GRASS_3(211),
	GRASS_SCROLLING(209, new Properties().setScroll(0f, 0.7f)),

	DIRT_1(213),
	DIRT_2(214),
	GRAVEL(215),

	DIRT_SHINY_1(213, new Properties().setSpecular(1.1f, 380f)),
	DIRT_SHINY_2(214, new Properties().setSpecular(1.1f, 380f)),
	GRAVEL_SHINY(215, new Properties().setSpecular(1.1f, 380f)),

	SAND_1(218),
	SAND_2(219),
	SAND_3(220),

	GRUNGE_1(221),
	GRUNGE_2(222),

	ROCK_1(223),

	CARPET(225),

	FALADOR_PATH_BRICK(226, new Properties().setSpecular(0.3f, 30f)),
	JAGGED_STONE_TILE(227),

	TILE_SMALL_1(228, new Properties().setSpecular(0.8f, 70f)),
	TILES_1_2x2(229, new Properties()),
	TILES_2_2x2(230, new Properties()),
	TILES_2x2_1_GLOSS(229, new Properties().setSpecular(1.0f, 70f)),
	TILES_2x2_2_GLOSS(230, new Properties().setSpecular(1.0f, 70f)),
	TILES_2x2_1_SEMIGLOSS(229, new Properties().setSpecular(0.5f, 300f)),
	TILES_2x2_2_SEMIGLOSS(230, new Properties().setSpecular(0.5f, 300f)),

	MARBLE_1(231),
	MARBLE_2(232),
	MARBLE_3(234),
	MARBLE_1_GLOSS(231, new Properties().setSpecular(0.9f, 280f)),
	MARBLE_2_GLOSS(232, new Properties().setSpecular(0.8f, 300f)),
	MARBLE_3_GLOSS(234, new Properties().setSpecular(0.7f, 320f)),
	MARBLE_1_SEMIGLOSS(231, new Properties().setSpecular(0.35f, 80f)),
	MARBLE_2_SEMIGLOSS(232, new Properties().setSpecular(0.3f, 100f)),
	MARBLE_3_SEMIGLOSS(234, new Properties().setSpecular(0.4f, 120f)),

	HD_LAVA_1(241, new Properties().setEmissive(1.0f).setDisplacement(235, 0.04f, 36f, 12f)),
	HD_LAVA_2(242, new Properties().setEmissive(1.0f).setDisplacement(235, 0.04f, 36f, 12f)),
	HD_MAGMA_1(243, new Properties().setEmissive(1.0f).setDisplacement(235, 0.04f, 36f, 12f)),
	HD_MAGMA_2(244, new Properties().setEmissive(1.0f).setDisplacement(235, 0.04f, 36f, 12f)),

	BARK(245),
	WOOD_GRAIN(247),

	// water/fluid variants
	WATER(7001),
	SWAMP_WATER(7025),
	POISON_WASTE(7998),
	POISON_WASTE_FLAT(998),
	BLOOD(7999),
	BLOOD_FLAT(999),
	ICE(7997),
	ICE_FLAT(997),
	;

	private final int diffuseMapId;
	private final float specularStrength;
	private final float specularGloss;
	private final float emissiveStrength;
	private final int displacementMapId;
	private final float displacementStrength;
	private final float displacementDurationX;
	private final float displacementDurationY;
	private final float scrollDurationX;
	private final float scrollDurationY;
	private final float textureScaleX;
	private final float textureScaleY;

	private static class Properties
	{
		private float specularStrength = 0f;
		private float specularGloss = 0f;
		private float emissiveStrength = 0f;
		private int displacementMapId = 304;
		private float displacementStrength = 0f;
		private float displacementDurationX = 0;
		private float displacementDurationY = 0;
		private float scrollDurationX = 0;
		private float scrollDurationY = 0;
		private float textureScaleX = 1.0f;
		private float textureScaleY = 1.0f;

		public Properties setSpecular(float specularStrength, float specularGloss)
		{
			this.specularStrength = specularStrength;
			this.specularGloss = specularGloss;
			return this;
		}

		public Properties setEmissive(float emissiveStrength)
		{
			this.emissiveStrength = emissiveStrength;
			return this;
		}

		public Properties setDisplacement(int displacementMapId, float displacementStrength, float displacementDurationX, float displacementDurationY)
		{
			this.displacementMapId = displacementMapId;
			this.displacementStrength = displacementStrength;
			this.displacementDurationX = displacementDurationX;
			this.displacementDurationY = displacementDurationY;
			return this;
		}

		public Properties setScroll(float scrollDurationX, float scrollDurationY)
		{
			this.scrollDurationX = scrollDurationX;
			this.scrollDurationY = scrollDurationY;
			return this;
		}

		public Properties setTextureScale(float textureScaleX, float textureScaleY)
		{
			this.textureScaleX = textureScaleX;
			this.textureScaleY = textureScaleY;
			return this;
		}
	}

	Material(int diffuseMapId)
	{
		this.diffuseMapId = diffuseMapId;
		this.emissiveStrength = 0f;
		this.specularStrength = 0f;
		this.specularGloss = 0f;
		this.displacementMapId = 304;
		this.displacementStrength = 0f;
		this.displacementDurationX = 0;
		this.displacementDurationY = 0;
		this.scrollDurationX = 0;
		this.scrollDurationY = 0;
		this.textureScaleX = 1.0f;
		this.textureScaleY = 1.0f;
	}

	Material(int diffuseMapId, Properties properties)
	{
		this.diffuseMapId = diffuseMapId;
		this.emissiveStrength = properties.emissiveStrength;
		this.specularStrength = properties.specularStrength;
		this.specularGloss = properties.specularGloss;
		this.displacementMapId = properties.displacementMapId;
		this.displacementStrength = properties.displacementStrength;
		this.displacementDurationX = properties.displacementDurationX;
		this.displacementDurationY = properties.displacementDurationY;
		this.scrollDurationX = properties.scrollDurationX;
		this.scrollDurationY = properties.scrollDurationY;
		this.textureScaleX = properties.textureScaleX;
		this.textureScaleY = properties.textureScaleY;
	}

	private static final HashMap<Integer, Material> DIFFUSE_ID_MATERIAL_MAP;

	static
	{
		DIFFUSE_ID_MATERIAL_MAP = new HashMap<>();
		for (Material material : values())
		{
			if (!DIFFUSE_ID_MATERIAL_MAP.containsKey(material.diffuseMapId))
			{
				DIFFUSE_ID_MATERIAL_MAP.put(material.diffuseMapId, material);
			}
		}
	}

	public static Material getTexture(int diffuseMapId)
	{
		return DIFFUSE_ID_MATERIAL_MAP.getOrDefault(diffuseMapId, Material.NONE);
	}

	private static final HashMap<Material, Integer> MATERIAL_INDEX_MAP;

	static
	{
		MATERIAL_INDEX_MAP = new HashMap<>();
		int index = 0;
		for (Material material : values())
		{
			MATERIAL_INDEX_MAP.put(material, index);
			index++;
		}
	}

	public static int getIndex(Material material)
	{
		return MATERIAL_INDEX_MAP.getOrDefault(material, 0);
	}

	public static Material[] getAllTextures()
	{
		return values();
	}
}
