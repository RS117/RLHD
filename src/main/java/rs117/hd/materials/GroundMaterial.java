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

import java.util.Random;
import lombok.Getter;

@Getter
public enum GroundMaterial
{
	NONE(Material.NONE),

	GRASS_1(Material.GRASS_1, Material.GRASS_2, Material.GRASS_3),
	DIRT(Material.DIRT_1, Material.DIRT_2),
	SNOW_1(Material.SNOW_1, Material.SNOW_1, Material.SNOW_2, Material.SNOW_3, Material.SNOW_3, Material.SNOW_4),
	SNOW_2(Material.SNOW_2, Material.SNOW_4),
	GRAVEL(Material.GRAVEL),
	FALADOR_PATHS(Material.FALADOR_PATH_BRICK),
	VARROCK_PATHS(Material.JAGGED_STONE_TILE),
	VARROCK_PATHS_LIGHT(Material.JAGGED_STONE_TILE),
	VARIED_DIRT(Material.GRAVEL, Material.DIRT_1, Material.DIRT_2),
	VARIED_DIRT_SHINY(Material.GRAVEL_SHINY, Material.DIRT_SHINY_1, Material.DIRT_SHINY_2),
	TILE_SMALL(Material.TILE_SMALL_1),
	CARPET(Material.CARPET),
	BRICK(Material.BRICK),

	TILES_2x2_1(Material.TILES_1_2x2),
	TILES_2x2_2(Material.TILES_2_2x2),
	TILES_2x2_1_GLOSS(Material.TILES_2x2_1_GLOSS),
	TILES_2x2_2_GLOSS(Material.TILES_2x2_2_GLOSS),
	TILES_2x2_1_SEMIGLOSS(Material.TILES_2x2_1_SEMIGLOSS),
	TILES_2x2_2_SEMIGLOSS(Material.TILES_2x2_2_SEMIGLOSS),

	MARBLE_1(Material.MARBLE_1, Material.MARBLE_2, Material.MARBLE_3),
	MARBLE_2(Material.MARBLE_3, Material.MARBLE_1, Material.MARBLE_2),
	MARBLE_1_GLOSS(Material.MARBLE_1_GLOSS, Material.MARBLE_2_GLOSS, Material.MARBLE_3_GLOSS),
	MARBLE_2_GLOSS(Material.MARBLE_3_GLOSS, Material.MARBLE_1_GLOSS, Material.MARBLE_2_GLOSS),
	MARBLE_1_SEMIGLOSS(Material.MARBLE_1_SEMIGLOSS, Material.MARBLE_2_SEMIGLOSS, Material.MARBLE_3_SEMIGLOSS),
	MARBLE_2_SEMIGLOSS(Material.MARBLE_3_SEMIGLOSS, Material.MARBLE_1_SEMIGLOSS, Material.MARBLE_2_SEMIGLOSS),
	MARBLE_DARK(Material.MARBLE_DARK),

	SAND(Material.SAND_1, Material.SAND_2, Material.SAND_3),

	UNDERWATER_GENERIC(Material.DIRT_1, Material.DIRT_2),

	WOOD_PLANKS_1(Material.WOOD_PLANKS_1),

	HD_LAVA(Material.HD_LAVA_1, Material.HD_LAVA_2, Material.HD_LAVA_1, Material.HD_LAVA_1, Material.HD_LAVA_2, Material.HD_MAGMA_1, Material.HD_MAGMA_2),

	STONE_PATTERN(Material.STONE_PATTERN),
	CONCRETE(Material.CONCRETE),
	SAND_BRICK(Material.SAND_BRICK),
	TILE_DARK(Material.TILE_DARK),

	// water/fluid variants
	WATER(Material.WATER),
	WATER_FLAT(Material.WATER_FLAT),
	SWAMP_WATER(Material.SWAMP_WATER),
	SWAMP_WATER_FLAT(Material.SWAMP_WATER_FLAT),
	POISON_WASTE(Material.POISON_WASTE),
	POISON_WASTE_FLAT(Material.POISON_WASTE_FLAT),
	BLOOD(Material.BLOOD),
	BLOOD_FLAT(Material.BLOOD_FLAT),
	ICE(Material.ICE),
	ICE_FLAT(Material.ICE_FLAT),
	;

	private final Material[] materials;

	GroundMaterial(Material... materials)
	{
		this.materials = materials;
	}

	public Material getRandomMaterial(int plane, int worldX, int worldY)
	{
		Random randomTex = new Random();
		// Generate a seed from the tile coordinates for
		// consistent 'random' results between scene loads.
		// This seed creates a patchy, varied terrain
		long seed = (plane + 1) * 10 * (worldX % 100) * 20 * (worldY % 100) * 30;
		randomTex.setSeed(seed);
		int randomInt = randomTex.nextInt(this.materials.length);
		return this.materials[randomInt];
	}
}
