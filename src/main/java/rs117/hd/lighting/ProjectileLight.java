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
import rs117.hd.lighting.LightManager.LightType;

@AllArgsConstructor
@Getter
enum ProjectileLight
{
	// Standard spellbook
	// Combat spells
	WIND_STRIKE(175, 2.5f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 91),
	WATER_STRIKE(175, 3.8f, rgb( 0, 140, 255), LightType.STATIC, 0, 0, 1000, 94),
	EARTH_STRIKE(175, 3.1f, rgb( 0, 255,   0), LightType.STATIC, 0, 0, 1000, 97),
	FIRE_STRIKE(175, 3.8f, rgb(255,   0,   0), LightType.STATIC, 0, 0, 1000, 100),

	WIND_BOLT(210, 3.8f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 118),
	WATER_BOLT(210, 6.3f, rgb( 0, 140, 255), LightType.STATIC, 0, 0, 1000, 121),
	EARTH_BOLT(210, 5.6f, rgb( 0, 255,   0), LightType.STATIC, 0, 0, 1000, 124),
	FIRE_BOLT(210, 6.3f, rgb(255,   0,   0), LightType.STATIC, 0, 0, 1000, 127),

	WIND_BLAST(250, 5.6f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 133),
	WATER_BLAST(250, 8.8f, rgb( 0, 140, 255), LightType.STATIC, 0, 0, 1000, 136),
	EARTH_BLAST(250, 6.9f, rgb( 0, 255,   0), LightType.STATIC, 0, 0, 1000, 139),
	FIRE_BLAST(250, 8.8f, rgb(255,   0,   0), LightType.STATIC, 0, 0, 1000, 130),

	WIND_WAVE(300, 7.5f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 159),
	WATER_WAVE(300, 10.0f, rgb( 0, 140, 255), LightType.STATIC, 0, 0, 1000, 162),
	EARTH_WAVE(300, 8.8f, rgb( 0, 255,   0), LightType.STATIC, 0, 0, 1000, 165),
	FIRE_WAVE(300, 10.0f, rgb(255,   0,   0), LightType.STATIC, 0, 0, 1000, 156),

	WIND_SURGE(400, 10.6f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 1456),
	WATER_SURGE(400, 13.8f, rgb( 0, 140, 255), LightType.STATIC, 0, 0, 1000, 1459),
	EARTH_SURGE(400, 12.5f, rgb( 0, 255,   0), LightType.STATIC, 0, 0, 1000, 1462),
	FIRE_SURGE(400, 13.8f, rgb(255,   0,   0), LightType.STATIC, 0, 0, 1000, 1465),

	// Utility combat spells
	CONFUSE(200, 5.0f, rgb(255, 0, 255), LightType.PULSE, 500, 20, 1000, 103),
	WEAKEN(200, 3.8f, rgb(255, 255, 255), LightType.PULSE, 500, 20, 1000, 106),
	CURSE(200, 5.0f, rgb(255, 0, 255), LightType.PULSE, 500, 20, 1000, 109),
	VULNERABILITY(250, 6.3f, rgb(255, 0, 255), LightType.PULSE, 500, 20, 1000, 168),
	ENFEEBLE(250, 6.3f, rgb(255, 0, 255), LightType.PULSE, 500, 20, 1000, 171),
	STUN(250, 5.0f, rgb(255, 255, 255), LightType.PULSE, 500, 20, 1000, 174),
	BIND_SNARE_ENTANGLE(200, 6.3f, rgb(0, 255, 0), LightType.PULSE, 500, 20, 1000, 178),
	CRUMBLE_UNDEAD(200, 5.0f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 146),
	MAGIC_DART(200, 7.5f, rgb(168, 255, 206), LightType.STATIC, 0, 0, 1000, 328),
	IBAN_BLAST(200, 7.5f, rgb(255, 175, 84), LightType.STATIC, 0, 0, 1000, 88),
	TELE_BLOCK(200, 5.0f, rgb(255, 255, 255), LightType.PULSE, 500, 20, 1000, 344),

	// Charged magic weapons
	TRIDENT_SWAMP_SPELL(400, 12.5f, rgb(0, 167, 204), LightType.STATIC, 0, 0, 1000, 1040),
	TRIDENT_SEA_SPELL(400, 7.5f, rgb(0, 0, 255), LightType.STATIC, 0, 0, 1000, 1252),

	// Ancient magicks spellbook
	SMOKE_RUSH(250, 1.3f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 384),
	SHADOW_RUSH(250, 2.5f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 378),
	ICE_RUSH(250, 5.0f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 360),
	SMOKE_BLITZ(250, 1.3f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 386),
	SHADOW_BLITZ(250, 2.5f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 1000, 380),
	BLOOD_BLITZ(250, 10.0f, rgb(255, 0, 0), LightType.STATIC, 0, 0, 1000, 374),

	// Arrows/bolts
	MAGIC_BOW_SPEC_AND_CRYSTAL_BOW(250, 7.5f, rgb(52, 235, 113), LightType.STATIC, 0, 0, 300, 249),
	CRAWS_BOW(250, 7.5f, rgb(255, 199, 102), LightType.STATIC, 0, 0, 300, 1574),
	FIRE_ARROWS(200, 6.3f, rgb(252,122,3), LightType.FLICKER, 0, 30, 300, 17),
	ARMADYL_CROSSBOW_SPEC(250, 7.5f, rgb(255, 255, 255), LightType.STATIC, 0, 0, 300, 301),

	// TzHaar
	JALTOK_JAD_MAGIC(700, 12.5f, rgb(255, 123, 0), LightType.STATIC, 0, 0, 500, 450),

	// Dragons
	DRAGONFIRE(700, 12.5f, rgb(255, 123, 0), LightType.STATIC, 0, 0, 500, 54),

	// Undead Druids
	UNDEAD_DRUID(300, 10.0f, rgb(150,   0,   0), LightType.STATIC, 0, 0, 1000, 1679),


	;

	private final int[] id;
	private final int size;
	private final float strength;
	private final int rgb;
	private final LightType lightType;
	private final float duration;
	private final float range;
	private final int fadeInDuration;

	ProjectileLight(int size, float strength, int rgb, LightType lightType, float duration, float range, int fadeInDuration, int... ids)
	{
		this.size = size;
		this.strength = strength;
		this.rgb = rgb;
		this.lightType = lightType;
		this.duration = duration;
		this.range = range;
		this.fadeInDuration = fadeInDuration;
		this.id = ids;
	}

	private static final Map<Integer, ProjectileLight> LIGHTS;

	static
	{
		ImmutableMap.Builder<Integer, ProjectileLight> builder = new ImmutableMap.Builder<>();
		for (ProjectileLight ProjectileLight : values())
		{
			for (int id : ProjectileLight.id)
			{
				builder.put(id, ProjectileLight);
			}
		}
		LIGHTS = builder.build();
	}

	static ProjectileLight find(int id)
	{
		return LIGHTS.get(id);
	}

	private static int rgb(int r, int g, int b)
	{
		return (r << 16) | (g << 8) | b;
	}
}
