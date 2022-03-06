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
package rs117.hd.lighting;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.NpcID;
import static net.runelite.api.NpcID.*;
import rs117.hd.HDUtils;

@AllArgsConstructor
@Getter
@Deprecated
enum NpcLight
{
	HELLCAT(20, Alignment.CENTER, 220, 7.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.HELLCAT, HELLCAT_6668, LAZY_HELLCAT, OVERGROWN_HELLCAT, WILY_HELLCAT, LAZY_HELLCAT_6689, OVERGROWN_HELLCAT_6682, WILY_HELLCAT_6696, HELLKITTEN),
	HELLRAT(20, Alignment.CENTER, 220, 7.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.HELLRAT, HELLRAT_BEHEMOTH),
	HELLPUPPY(20, Alignment.CENTER, 220, 15f, rgb(255, 208, 54), LightType.PULSE, 5000, 20, NpcID.HELLPUPPY, HELLPUPPY_3099),

	IKKLE_HYDRA_RED(20, Alignment.CENTER, 120, 7.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, IKKLE_HYDRA_8494, IKKLE_HYDRA_8519),
	IKKLE_HYDRA_GREEN(20, Alignment.CENTER, 120, 7.5f , rgb(195, 255, 0), LightType.STATIC, 0, 0, IKKLE_HYDRA, IKKLE_HYDRA_8517),
	IKKLE_HYDRA_BLUE(20, Alignment.CENTER, 120, 7.5f , rgb(0, 140, 255), LightType.STATIC, 0, 0, IKKLE_HYDRA_8493, IKKLE_HYDRA_8518),
	IKKLE_HYDRA_BLACK(20, Alignment.CENTER, 120, 7.5f , rgb(53, 222, 216), LightType.STATIC, 0, 0, IKKLE_HYDRA_8495, IKKLE_HYDRA_8520),

	PHOENIX(20, Alignment.CENTER, 320, 25.0f , rgb(255, 124, 0), LightType.FLICKER, 0, 40,
	PHOENIX_7368, PHOENIX_7370),
	PHOENIX_WHITE(20, Alignment.CENTER, 320, 15.0f , rgb(255, 255, 255), LightType.FLICKER, 0, 40, PHOENIX_3079, PHOENIX_3083),
	PHOENIX_BLUE(20, Alignment.CENTER, 320, 22.5f , rgb(18, 77, 255), LightType.FLICKER, 0, 40, PHOENIX_3078, PHOENIX_3082),
	PHOENIX_PURPLE(20, Alignment.CENTER, 320, 22.5f , rgb(162, 0, 255), LightType.FLICKER, 0, 40, PHOENIX_3080, PHOENIX_3084),
	PHOENIX_GREEN(20, Alignment.CENTER, 320, 20.0f , rgb(116, 245, 81), LightType.FLICKER, 0, 40, 3077, PHOENIX_3081),

	TZREK_ZUK(10, Alignment.CENTER, 100, 12.5f , rgb(252, 128, 45), LightType.STATIC, 0, 0, TZREKZUK, TZREKZUK_8011),

	MIDNIGHT(10, Alignment.CENTER, 100, 12.5f , rgb(252, 128, 45), LightType.STATIC, 0, 0, NpcID.MIDNIGHT, MIDNIGHT_7893),

	RIFT_GUARDIAN_FIRE(50, Alignment.CENTER, 100, 17.5f , rgb(255, 0, 0), LightType.PULSE, 2100, 10, RIFT_GUARDIAN, RIFT_GUARDIAN_7354),
	RIFT_GUARDIAN_AIR(50, Alignment.CENTER, 100, 17.5f , rgb(255, 255, 255), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7338, RIFT_GUARDIAN_7355),
	RIFT_GUARDIAN_MIND(50, Alignment.CENTER, 100, 17.5f , rgb(255, 163, 59), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7339, RIFT_GUARDIAN_7356),
	RIFT_GUARDIAN_WATER(50, Alignment.CENTER, 100, 17.5f , rgb(18, 172, 255), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7340, RIFT_GUARDIAN_7357),
	RIFT_GUARDIAN_EARTH(50, Alignment.CENTER, 100, 17.5f , rgb(184, 124, 81), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7341, RIFT_GUARDIAN_7358),
	RIFT_GUARDIAN_BODY(50, Alignment.CENTER, 100, 17.5f , rgb(37, 71, 194), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7342, RIFT_GUARDIAN_7359),
	RIFT_GUARDIAN_COSMIC(50, Alignment.CENTER, 100, 17.5f , rgb(255, 255, 0), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7343, RIFT_GUARDIAN_7360),
	RIFT_GUARDIAN_CHAOS(50, Alignment.CENTER, 100, 17.5f , rgb(255, 196, 0), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7344, RIFT_GUARDIAN_7361),
	RIFT_GUARDIAN_NATURE(50, Alignment.CENTER, 100, 17.5f , rgb(0, 255, 0), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7345, RIFT_GUARDIAN_7362),
	RIFT_GUARDIAN_LAW(50, Alignment.CENTER, 100, 17.5f , rgb(21, 63, 232), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7346, RIFT_GUARDIAN_7363),
	RIFT_GUARDIAN_DEATH(50, Alignment.CENTER, 100, 17.5f , rgb(255, 239, 235), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7347, RIFT_GUARDIAN_7364),
	RIFT_GUARDIAN_SOUL(50, Alignment.CENTER, 100, 17.5f , rgb(115, 77, 255), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7348, RIFT_GUARDIAN_7365),
	RIFT_GUARDIAN_ASTRAL(50, Alignment.CENTER, 100, 17.5f , rgb(245, 153, 255), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7349, RIFT_GUARDIAN_7366),
	RIFT_GUARDIAN_BLOOD(50, Alignment.CENTER, 100, 17.5f , rgb(255, 0, 0), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_7350, RIFT_GUARDIAN_7367),
	RIFT_GUARDIAN_WRATH(50, Alignment.CENTER, 100, 17.5f , rgb(212, 58, 47), LightType.PULSE, 2100, 10, RIFT_GUARDIAN_8024, RIFT_GUARDIAN_8028),

	CHAOS_ELEMENTAL_JR(250, Alignment.CENTER, 150, 20.0f , rgb(255, 255, 255), LightType.FLICKER, 0, 80, NpcID.CHAOS_ELEMENTAL_JR, CHAOS_ELEMENTAL_JR_5907),

	SKOTOS(70, Alignment.CENTER, 150, 17.5f , rgb(157, 0, 255), LightType.PULSE, 3000, 20, NpcID.SKOTOS, SKOTOS_7671),

	VANGUARD_PET(20, Alignment.CENTER, 100, 17.5f , rgb(255, 153, 0), LightType.PULSE, 2100, 10, VANGUARD_8198, VANGUARD_8203),
	VASA_MINIRIO(100, Alignment.CENTER, 150, 20.0f , rgb(76, 0, 255), LightType.PULSE, 2400, 30, NpcID.VASA_MINIRIO, VASA_MINIRIO_8204),
	TEKTINY(10, Alignment.CENTER, 100, 20.0f , rgb(255, 162, 41), LightType.STATIC, 2400, 0, NpcID.TEKTINY, TEKTINY_8202),
	ENRAGED_TEKTINY(10, Alignment.CENTER, 100, 20.0f , rgb(255, 95, 41), LightType.STATIC, 2400, 0, NpcID.ENRAGED_TEKTINY, ENRAGED_TEKTINY_9513),

	SMOKE_DEVIL_PET(20, Alignment.CENTER, 100, 17.5f , rgb(255, 242, 0), LightType.PULSE, 3000, 15, SMOKE_DEVIL_6639, SMOKE_DEVIL_6655),

	YOUNGLLEF(40, Alignment.CENTER, 100, 17.5f , rgb(0, 255, 255), LightType.PULSE, 3000, 15, NpcID.YOUNGLLEF, YOUNGLLEF_8737),
	CORRUPTED_YOUNGLLEF(40, Alignment.CENTER, 100, 17.5f , rgb(255, 0, 0), LightType.PULSE, 3000, 15, NpcID.CORRUPTED_YOUNGLLEF, CORRUPTED_YOUNGLLEF_8738),

	TINY_TEMPOR(30, Alignment.CENTER, 200, 15.0f , rgb(0, 255, 255), LightType.PULSE, 3200, 20, NpcID.TINY_TEMPOR, TINY_TEMPOR_10637),

	PYREFIEND(60, Alignment.CENTER, 280, 7.5f , rgb(252, 122, 3), LightType.FLICKER, 0, 20, NpcID.PYREFIEND, PYREFIEND_434, PYREFIEND_435, PYREFIEND_436, PYREFIEND_3139),

	GHOST(80, Alignment.CENTER, 200, 7.5f , rgb(200, 200, 255), LightType.FLICKER, 1, 10, NpcID.GHOST, GHOST_, GHOST_86, GHOST_87, GHOST_88, GHOST_89, GHOST_90, GHOST_91, GHOST_92, GHOST_93, GHOST_94, GHOST_95, GHOST_96, GHOST_97, GHOST_98, GHOST_99, GHOST_472, GHOST_473, GHOST_474, GHOST_505, GHOST_506, GHOST_507, GHOST_920, GHOST_1786, GHOST_2527, GHOST_2528, GHOST_2529, GHOST_2530, GHOST_2531, GHOST_2532, GHOST_2533, GHOST_2534, GHOST__3009, GHOST_3516, GHOST_3625, GHOST_3975, GHOST_3976, GHOST_3977, GHOST_3978, GHOST_3979, GHOST_5370, GHOST_7263, GHOST_7264, GHOST_9194, GHOST_10538, ANCIENT_GHOST_10558, MYSTERIOUS_GHOST_3451, MYSTERIOUS_GHOST_3452, MYSTERIOUS_GHOST_3453, MYSTERIOUS_GHOST_3454, MYSTERIOUS_GHOST_3455),

	SOUL_WARS_FORGOTTEN_SOUL(80, Alignment.CENTER, 200, 10.0f , rgb(100, 180, 240), LightType.FLICKER, 1, 10, FORGOTTEN_SOUL_10524, FORGOTTEN_SOUL_10525, FORGOTTEN_SOUL_10526, FORGOTTEN_SOUL_10534, FORGOTTEN_SOUL_10535, FORGOTTEN_SOUL_10536, FORGOTTEN_SOUL_10537, FORGOTTEN_SOUL_10544, FORGOTTEN_SOUL_10545),

	PHASMATYS_GHOSTS(80, Alignment.CENTER, 300, 12.5f , rgb(100, 255, 100), LightType.FLICKER, 0, 10, GHOST_DISCIPLE, NECROVARUS, GHOST_GUARD, TORTURED_SOUL, GHOST_VILLAGER, GRAVINGAS, GHOST_INNKEEPER, GHOST_SHOPKEEPER, DROALAK, DROALAK_3494, SARAH_8134, GHOST_BANKER, GHOST_CAPTAIN, GHOST_CAPTAIN_3006, GHOST_SAILOR, VELORINA, GHOST_FARMER, GHOST_GUARD_6698),

	ABERRANT_SPECTRES(150, Alignment.CENTER, 600, 12.5f , rgb(150, 255, 50), LightType.FLICKER, 0, 10, ABERRANT_SPECTRE, ABERRANT_SPECTRE_3, ABERRANT_SPECTRE_4, ABERRANT_SPECTRE_5, ABERRANT_SPECTRE_6, ABERRANT_SPECTRE_7),
	ABHORRENT_SPECTRES(225, Alignment.CENTER, 900, 16f , rgb(150, 255, 50), LightType.FLICKER, 0, 10, ABHORRENT_SPECTRE),
	
	BARROWS_BROTHERS(50, Alignment.CENTER, 220, 12.5f , rgb(255, 150, 100), LightType.PULSE, 1, 0.7f, AHRIM_THE_BLIGHTED, KARIL_THE_TAINTED, DHAROK_THE_WRETCHED, TORAG_THE_CORRUPTED, VERAC_THE_DEFILED, GUTHAN_THE_INFESTED),

	SEREN(280, Alignment.CENTER, 500, 12.5f , rgb(0, 200, 225), LightType.PULSE, 3200, 10, NpcID.SEREN, MEMORY_OF_SEREN, MEMORY_OF_SEREN_8777, MEMORY_OF_SEREN_8778, MEMORY_OF_SEREN_8779, MEMORY_OF_SEREN_8780, MEMORY_OF_SEREN_8781, MEMORY_OF_SEREN_8782, MEMORY_OF_SEREN_8783, MEMORY_OF_SEREN_8784),
	FRAGMENT_OF_SEREN(200, Alignment.CENTER, 500, 17.5f , rgb(187, 153, 255), LightType.PULSE, 3200, 10, NpcID.FRAGMENT_OF_SEREN, FRAGMENT_OF_SEREN_8919, FRAGMENT_OF_SEREN_8920),

	// The Gauntlet
	CRYSTALLINE_HUNLLEF(200, Alignment.CENTER, 800, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, 9021, CRYSTALLINE_HUNLLEF_9022, CRYSTALLINE_HUNLLEF_9023, CRYSTALLINE_HUNLLEF_9024),
	CRYSTALLINE_BAT(200, Alignment.CENTER, 400, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_BAT),
	CRYSTALLINE_RAT(40, Alignment.CENTER, 400, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_RAT),
	CRYSTALLINE_SPIDER(20, Alignment.CENTER, 300, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_SPIDER),
	CRYSTALLINE_DRAGON(80, Alignment.CENTER, 600, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_DRAGON),
	CRYSTALLINE_BEAR(80, Alignment.CENTER, 500, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_BEAR),
	CRYSTALLINE_DARK_BEAST(80, Alignment.CENTER, 600, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_DARK_BEAST),
	CRYSTALLINE_SCORPION(30, Alignment.CENTER, 400, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_SCORPION),
	CRYSTALLINE_UNICORN(70, Alignment.CENTER, 500, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_UNICORN),
	CRYSTALLINE_WOLF(40, Alignment.CENTER, 500, 12.5f , rgb(0, 255, 255), LightType.STATIC, 0, 0, NpcID.CRYSTALLINE_WOLF),
	// Corrupted Gauntlet
	CORRUPTED_HUNLLEF(200, Alignment.CENTER, 800, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_HUNLLEF, CORRUPTED_HUNLLEF_9036, CORRUPTED_HUNLLEF_9037, CORRUPTED_HUNLLEF_9038),
	CORRUPTED_BAT(200, Alignment.CENTER, 400, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_BAT),
	CORRUPTED_RAT(40, Alignment.CENTER, 400, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_RAT),
	CORRUPTED_SPIDER(20, Alignment.CENTER, 300, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_SPIDER),
	CORRUPTED_DRAGON(80, Alignment.CENTER, 600, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_DRAGON),
	CORRUPTED_BEAR(80, Alignment.CENTER, 500, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_BEAR),
	CORRUPTED_DARK_BEAST(80, Alignment.CENTER, 600, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_DARK_BEAST),
	CORRUPTED_SCORPION(30, Alignment.CENTER, 400, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_SCORPION),
	CORRUPTED_UNICORN(70, Alignment.CENTER, 500, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_UNICORN),
	CORRUPTED_WOLF(40, Alignment.CENTER, 500, 12.5f , rgb(255, 0, 0), LightType.STATIC, 0, 0, NpcID.CORRUPTED_WOLF),


	// Cerberus
	CERBERUS(100, Alignment.CENTER, 800, 30f, rgb(255, 208, 54), LightType.PULSE, 5000, 20, NpcID.CERBERUS, CERBERUS_5863, CERBERUS_5866),

	// Tempoross
	TEMPOROSS(250, Alignment.CENTER, 800, 17.5f , rgb(0, 255, 255), LightType.PULSE, 3200, 20, NpcID.TEMPOROSS),
	TEMPOROSS_WHIRLPOOL(-30, Alignment.CENTER, 500, 17.5f , rgb(0, 255, 255), LightType.PULSE, 3200, 20, TEMPOROSS_10574, 10573),
	TEMPOROSS_SPIRIT_POOL(5, Alignment.CENTER, 500, 17.5f , rgb(0, 255, 255), LightType.PULSE, 3200, 20, SPIRIT_POOL),

	// Morytania
	// Hallowed Sepulchre
	CROSSBOWMAN_STATUE_BOLT(220, Alignment.CENTER, 400, 10.0f , rgb(0, 190, 252), LightType.STATIC, 0, 0, 9672),
	CROSSBOWMAN_STATUE_BOLT2(220, Alignment.CENTER, 400, 4 , rgb(130, 224, 255), LightType.STATIC, 0, 0, 9673),
	CROSSBOWMAN_STATUE_BOLT3(220, Alignment.CENTER, 400, 4 , rgb(252, 214, 0), LightType.STATIC, 0, 0, 9674),
	// Theatre of Blood
	NYCOLAS_HAGIOS_SMALL(5, Alignment.CENTER, 300, 10.0f , rgb(0, 200, 255), LightType.FLICKER, 0, 40, 8344),
	NYCOLAS_HAGIOS_MEDIUM(30, Alignment.CENTER, 500, 10.0f , rgb(0, 200, 255), LightType.FLICKER, 0, 40, 8347, 8383),
	NYCOLAS_VASILIAS(30, Alignment.CENTER, 1000, 12.5f , rgb(0, 200, 255), LightType.FLICKER, 0, 40, 8356),

	// Zanaris
	ZANARIS_FAIRY(200, Alignment.CENTER, 200, 15.0f, rgb(255, 255, 255), LightType.PULSE, 4500, 10, CHAELDAR, FAIRY_QUEEN, FAIRY_NUFF, FAIRY_GODFATHER, FAIRY_QUEEN_1842, FAIRY_VERY_WISE, FAIRY, FAIRY_1849, FAIRY_1850, FAIRY_1851, FAIRY_2829, FAIRY_SHOP_KEEPER, FAIRY_SHOP_ASSISTANT, BANKER_3092, FAIRY_3204, FAIRY_AERYKA, COORDINATOR, FAIRY_NUFF_5836, FAIRY_GODFATHER_5837, SLIM_LOUIE, FAT_ROCCO, FAIRY_CHEF, FAIRY_FIXIT, FAIRY_7748),

	// Arceuus spellbook
	GHOSTLY_THRALL(80, Alignment.CENTER, 400, 15.0f , rgb(0, 100, 255), LightType.FLICKER, 0, 10, LESSER_GHOSTLY_THRALL, GREATER_GHOSTLY_THRALL, SUPERIOR_GHOSTLY_THRALL),
	SKELETAL_THRALL(80, Alignment.CENTER, 400, 15.0f , rgb(144, 245, 66), LightType.FLICKER, 0, 10, LESSER_SKELETAL_THRALL, GREATER_SKELETAL_THRALL, SUPERIOR_SKELETAL_THRALL),
	ZOMBIE_THRALL(80, Alignment.CENTER, 400, 15.0f , rgb(255, 0, 0), LightType.FLICKER, 0, 10, LESSER_ZOMBIFIED_THRALL, GREATER_ZOMBIFIED_THRALL, SUPERIOR_ZOMBIFIED_THRALL),

	// Chambers of Xeric
	COX_GLOWING_CRYSTAL(180, Alignment.CENTER, 900, 15.0f , rgb(76, 0, 255), LightType.PULSE, 2400, 20, 7568),
	VASA_NISTIRIO(300, Alignment.CENTER, 1200, 15.0f , rgb(76, 0, 255), LightType.PULSE, 2400, 30, NpcID.VASA_NISTIRIO, VASA_NISTIRIO_7567),
	TEKTON(100, Alignment.CENTER, 600, 15.0f , rgb(255, 162, 41), LightType.STATIC, 2400, 0, NpcID.TEKTON, TEKTON_7541, TEKTON_7542, TEKTON_7545),
	TEKTON_ENRAGED(100, Alignment.CENTER, 600, 20.0f , rgb(255, 95, 41), LightType.STATIC, 2400, 0, NpcID.TEKTON_ENRAGED, TEKTON_ENRAGED_7544),
	VANGUARD(20, Alignment.CENTER, 600, 15.0f , rgb(255, 162, 41), LightType.STATIC, 2400, 0, NpcID.VANGUARD, VANGUARD_7526, VANGUARD_7527, VANGUARD_7528, VANGUARD_7529),
	JEWELLED_CRAB_GREEN(60, Alignment.CENTER, 300, 15.0f , rgb(128, 255, 0), LightType.PULSE, 2400, 20, NpcID.JEWELLED_CRAB_GREEN),
	JEWELLED_CRAB_RED(60, Alignment.CENTER, 300, 15.0f , rgb(255, 0, 0), LightType.PULSE, 2400, 20, NpcID.JEWELLED_CRAB_RED),
	JEWELLED_CRAB_BLUE(60, Alignment.CENTER, 300, 15.0f , rgb(0, 255, 255), LightType.PULSE, 2400, 20, NpcID.JEWELLED_CRAB_BLUE),

	// Nightmare of Ashihama
	THE_NIGHTMARE_INACTIVE(100, Alignment.CENTER, 1200, 15.0f , rgb(255, 0, 166), LightType.PULSE, 1950, 20, THE_NIGHTMARE_9461, THE_NIGHTMARE_9462, THE_NIGHTMARE_9463, THE_NIGHTMARE_9464),

	FIRE_GIANT(370, Alignment.CENTER, 400, 12.5f , rgb(255, 124, 0), LightType.FLICKER, 0, 20, NpcID.FIRE_GIANT, FIRE_GIANT_2076, FIRE_GIANT_2077, FIRE_GIANT_2078, FIRE_GIANT_2079, FIRE_GIANT_2080, FIRE_GIANT_2081, FIRE_GIANT_2082, FIRE_GIANT_2083, FIRE_GIANT_2084, FIRE_GIANT_7251, FIRE_GIANT_7252),

	// Pest Control
	PEST_CONTROL_PURPLE_PORTAL(120, Alignment.CENTER, 400, 15.0f , rgb(150, 75, 255), LightType.PULSE, 2000, 25, PORTAL, PORTAL_1743, PORTAL_1747, PORTAL_1751),
	PEST_CONTROL_BLUE_PORTAL(120, Alignment.CENTER, 400, 12.5f , rgb(0, 175, 255), LightType.PULSE, 2000, 25, PORTAL_1740, PORTAL_1744, PORTAL_1748, PORTAL_1752),
	PEST_CONTROL_YELLOW_PORTAL(120, Alignment.CENTER, 400, 12.5f , rgb(230, 255, 90), LightType.PULSE, 2000, 25, PORTAL_1741, PORTAL_1745, PORTAL_1749, PORTAL_1753),
	PEST_CONTROL_RED_PORTAL(120, Alignment.CENTER, 400, 12.5f , rgb(255, 0, 90), LightType.PULSE, 2000, 25, PORTAL_1742, PORTAL_1746, PORTAL_1750, PORTAL_1754),
	PEST_CONTROL_KNIGHT(15, Alignment.CENTER, 200, 7.5f , rgb(255, 0, 30), LightType.PULSE, 1500, 10, VOID_KNIGHT_2950, VOID_KNIGHT_2951, VOID_KNIGHT_2952, VOID_KNIGHT_2953),

	// Rev caves
	REVENANT_CYCLOPS(150, Alignment.CENTER, 400, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_CYCLOPS),
	REVENANT_IMP(30, Alignment.CENTER, 200, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_IMP),
	REVENANT_DEMON(200, Alignment.CENTER, 500, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_DEMON),
	REVENANT_DRAGON(100, Alignment.CENTER, 700, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_DRAGON),
	REVENANT_GOBLIN(50, Alignment.CENTER, 300, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_GOBLIN),
	REVENANT_DARK_BEAST(100, Alignment.CENTER, 600, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_DARK_BEAST),
	REVENANT_HELLHOUND(80, Alignment.CENTER, 200, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_HELLHOUND),
	REVENANT_HOBGOBLIN(50, Alignment.CENTER, 400, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_HOBGOBLIN),
	REVENANT_KNIGHT(100, Alignment.CENTER, 400, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_KNIGHT),
	REVENANT_ORK(100, Alignment.CENTER, 400, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_ORK),
	REVENANT_PYREFIEND(30, Alignment.CENTER, 200, 12.5f, rgb(183, 234, 225), LightType.PULSE, 3400, 10, NpcID.REVENANT_PYREFIEND),

	;


	private final int[] id;
	private final int height;
	private final Alignment alignment;
	private final int size;
	private final float strength;
	private final float[] color;
	private final LightType lightType;
	private final float duration;
	private final float range;

	NpcLight(int height, Alignment alignment, int size, float strength, float[] color, LightType lightType, float duration, float range, int... ids)
	{
		this.height = height;
		this.alignment = alignment;
		this.size = size;
		this.strength = strength;
		this.color = color;
		this.lightType = lightType;
		this.duration = duration;
		this.range = range;
		this.id = ids;
	}

	private static final Map<Integer, NpcLight> LIGHTS;

	static
	{
		ImmutableMap.Builder<Integer, NpcLight> builder = new ImmutableMap.Builder<>();
		for (NpcLight npcLight : values())
		{
			for (int id : npcLight.id)
			{
				builder.put(id, npcLight);
			}
		}
		LIGHTS = builder.build();
	}

	static NpcLight find(int id)
	{
		return LIGHTS.get(id);
	}

	private static float[] rgb(int r, int g, int b)
	{
		return new float[]{
			HDUtils.gammaToLinear(r / 255f),
			HDUtils.gammaToLinear(g / 255f),
			HDUtils.gammaToLinear(b / 255f)
		};
	}
}
