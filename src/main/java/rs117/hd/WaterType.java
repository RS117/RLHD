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
package rs117.hd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rs117.hd.materials.GroundMaterial;

@Getter
@RequiredArgsConstructor
public enum WaterType
{
	NONE(0, GroundMaterial.NONE),
	WATER(1, GroundMaterial.WATER),
	WATER_FLAT(2, GroundMaterial.WATER_FLAT),
	SWAMP_WATER(3, GroundMaterial.SWAMP_WATER),
	SWAMP_WATER_FLAT(4, GroundMaterial.SWAMP_WATER_FLAT),
	POISON_WASTE(5, GroundMaterial.POISON_WASTE),
	POISON_WASTE_FLAT(6, GroundMaterial.POISON_WASTE_FLAT),
	BLOOD(7, GroundMaterial.BLOOD_FLAT),
	ICE(8, GroundMaterial.ICE),
	ICE_FLAT(9, GroundMaterial.ICE_FLAT),
	;

	private final int value;
	private final GroundMaterial groundMaterial;
}
