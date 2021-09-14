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

import lombok.AllArgsConstructor;
import lombok.Getter;
import rs117.hd.lighting.data.Alignment;
import rs117.hd.lighting.data.Light;
import rs117.hd.lighting.data.LightType;

import java.awt.*;
import java.util.ArrayList;

@AllArgsConstructor
@Getter
enum LightData
{
	LAVA(LightType.PULSE, new Color(252, 122, 3), 1200, 800,10,7000,20, new LightLocation[]{
			new LightLocation(2467, 5174), new LightLocation(2471, 5172),
			new LightLocation(2448, 5155), new LightLocation(2444, 5155),
			new LightLocation(2439, 5150), new LightLocation(2444, 5103),
			new LightLocation(2467, 5088), new LightLocation(2470, 5100),
			new LightLocation(2472, 5102), new LightLocation(2475, 5104),
			new LightLocation(2474, 5076), new LightLocation(2477, 5077),
			new LightLocation(2479, 5075), new LightLocation(2499, 5060),
			new LightLocation(2539, 5076), new LightLocation(2533, 5077),
			new LightLocation(2533, 5081), new LightLocation(2536, 5083),
			new LightLocation(2537, 5085), new LightLocation(2539, 5084),
			new LightLocation(2540, 5086), new LightLocation(2526, 5106),
			new LightLocation(2524, 5104), new LightLocation(2523, 5109),
			new LightLocation(2522, 5104), new LightLocation(2521, 5109),
			new LightLocation(2519, 5107), new LightLocation(2531, 5160),
			new LightLocation(2530, 5158), new LightLocation(2528, 5160),
			new LightLocation(2507, 5157), new LightLocation(2508, 5154),
			new LightLocation(2511, 5153), new LightLocation(2484, 5149),
			new LightLocation(2488, 5151), new LightLocation(2470, 5152),
			new LightLocation(2465, 5137), new LightLocation(2462, 5132),
			new LightLocation(2451, 5131), new LightLocation(2450, 5127),
			new LightLocation(2403, 5175), new LightLocation(2406, 5170),
			new LightLocation(2402, 5169), new LightLocation(2394, 5174),
			new LightLocation(2392, 5171), new LightLocation(2395, 5168),
			new LightLocation(2413, 5164), new LightLocation(2420, 5157),
			new LightLocation(2419, 5151), new LightLocation(2379, 5162),
			new LightLocation(2389, 5149), new LightLocation(2380, 5145),
			new LightLocation(2398, 5131), new LightLocation(2418, 5097),
			new LightLocation(2424, 5102), new LightLocation(2424, 5106),
			new LightLocation(2424, 5110), new LightLocation(2401, 5107),
			new LightLocation(2401, 5115), new LightLocation(2396, 5107),
			new LightLocation(2392, 5112), new LightLocation(2376, 5118),
			new LightLocation(2371, 5111), new LightLocation(2382, 5088),
			new LightLocation(2375, 5082), new LightLocation(2369, 5066),
			new LightLocation(2375, 5060), new LightLocation(2379, 5060),
			new LightLocation(2388, 5060), new LightLocation(2394, 5062),
			new LightLocation(2409, 5063), new LightLocation(2410, 5060),
			new LightLocation(2420, 5060), new LightLocation(2422, 5063),
			new LightLocation(2415, 5065), new LightLocation(2427, 5079),
			new LightLocation(2427, 5083), new LightLocation(2427, 5087),
			new LightLocation(2423, 5091),
	}),
	LAVA_SOUL_WARS(LightType.PULSE, new Color(252, 122, 3), 1400, 600,10,7000,-20, new LightLocation[]{
			new LightLocation(2218, 2839), new LightLocation(2219, 2845),
			new LightLocation(2220, 2847), new LightLocation(2223, 2848),
			new LightLocation(2226, 2848), new LightLocation(2225, 2850),
			new LightLocation(2230, 2844), new LightLocation(2230, 2838),
			new LightLocation(2229, 2836), new LightLocation(2227, 2836),
			new LightLocation(2224, 2836), new LightLocation(2221, 2836),
			new LightLocation(2218, 2837),
			//Battle Field
			new LightLocation(2277, 2910), new LightLocation(2280, 2911),
			new LightLocation(2283, 2912), new LightLocation(2283, 2908),
			new LightLocation(2291, 2910), new LightLocation(2294, 2912),
			new LightLocation(2292, 2914), new LightLocation(2283, 2925),
			new LightLocation(2280, 2927), new LightLocation(2279, 2931),
			new LightLocation(2279, 2935), new LightLocation(2282, 2939),
			new LightLocation(2286, 2938), new LightLocation(2290, 2938),
			new LightLocation(2294, 2938), new LightLocation(2294, 2936),
			new LightLocation(2295, 2933), new LightLocation(2294, 2929),
			new LightLocation(2293, 2926), new LightLocation(2291, 2925),
	}),
	LAVA_INFERNO(LightType.PULSE, new Color(235, 97, 23), 300, 2200,10,7000,200, new LightLocation[]{
			new LightLocation(2495, 5041), new LightLocation(2505, 5041),
			new LightLocation(2512, 5039), new LightLocation(2513, 5031),
			new LightLocation(2513, 5022), new LightLocation(2514, 5011),
			new LightLocation(2512, 5004), new LightLocation(2500, 5003),
			new LightLocation(2492, 5002), new LightLocation(2485, 5005),
			new LightLocation(2482, 5002), new LightLocation(2477, 5004),
			new LightLocation(2477, 5014), new LightLocation(2474, 5021),
			new LightLocation(2478, 5028), new LightLocation(2474, 5035),
			new LightLocation(2478, 5039), new LightLocation(2484, 5041),
			// Jad Challenge Instance
			new LightLocation(2271, 5361), new LightLocation(2281, 5361),
			new LightLocation(2288, 5359), new LightLocation(2289, 5351),
			new LightLocation(2289, 5342), new LightLocation(2290, 5331),
			new LightLocation(2288, 5324), new LightLocation(2276, 5323),
			new LightLocation(2268, 5322), new LightLocation(2261, 5325),
			new LightLocation(2258, 5322), new LightLocation(2253, 5324),
			new LightLocation(2253, 5334), new LightLocation(2250, 5341),
			new LightLocation(2254, 5348), new LightLocation(2250, 5355),
			new LightLocation(2254, 5359), new LightLocation(2260, 5361),
	}),
	LAVA_KARAMJA_VOLCANO(LightType.PULSE, new Color(252, 122, 3), 400, 2000,10,3000,50, new LightLocation[]{
			new LightLocation(2851, 9565), new LightLocation(2856, 9564),
			new LightLocation(2844, 9564), new LightLocation(2841, 9572),
			new LightLocation(2842, 9577), new LightLocation(2849, 9574),
			new LightLocation(2860, 9564), new LightLocation(2832, 9571),
			new LightLocation(2832, 9576), new LightLocation(2845, 9622),
			new LightLocation(2845, 9616), new LightLocation(2840, 9619),
			new LightLocation(2855, 9626), new LightLocation(2864, 9633),
			new LightLocation(2864, 9637), new LightLocation(2864, 9645),
			new LightLocation(2849, 9648),
	}),
	LAVA_KARAMJA_DUNGEON(LightType.PULSE, new Color(252, 122, 3), 400, 1500,10,3000,50, new LightLocation[]{
			new LightLocation(2837, 9548), new LightLocation(2843, 9551),
			new LightLocation(2834, 9552), new LightLocation(2829, 9560),
			new LightLocation(2835, 9627), new LightLocation(2833, 9622),
			new LightLocation(2860, 9630),
	}),
	ARCEUUS_CANDLES(LightType.FLICKER, new Color(230, 120, 5), 300, 300,20,1000,150, new LightLocation[]{
			new LightLocation(1702, 2767, Alignment.SOUTHWEST),
			new LightLocation(1704, 2767, Alignment.SOUTH),
			new LightLocation(1720, 3758, Alignment.NORTHWEST),
			new LightLocation(1718, 3771),
			new LightLocation(1715, 3771),
			new LightLocation(1706, 3767,1,Alignment.WEST),
			new LightLocation(2860, 9630,1, Alignment.WEST),
	}),
	STEALING_CREATION(LightType.FLICKER, new Color(180, 20, 220), 800, 1000,0.2f,1000,0, new LightLocation[]{
			new LightLocation(3171, 10003, Alignment.NORTHWEST),
	}),
	VARROCK_WEST_BANK_CHANDELIERS(LightType.FLICKER, new Color(252, 122, 3), 400, 700,0.2f,1000,150, new LightLocation[]{
			new LightLocation(3184, 3443), new LightLocation(3184, 3438),
	}),
	GOD_WARS_ENTRANCE(LightType.STATIC, new Color(255, 255, 255), 400, 600,0.2f,1000,300, new LightLocation[]{
			new LightLocation(2882, 5314, Alignment.SOUTH),
	}),
	GOD_WARS(LightType.STATIC, new Color(255, 255, 255), 300, 2000,0.2f,1000,50, new LightLocation[]{
			new LightLocation(2882, 5314, Alignment.SOUTH),
	}),
	ZAMMY_ALTAR(LightType.STATIC, new Color(252, 122, 3), 400, 300,0.2f,1000,200, new LightLocation[]{
			new LightLocation(2937, 5324, 2, Alignment.NORTHEAST),
	}),
	VER_SINHAZA(LightType.PULSE, new Color(220, 60, 0), 500, 300,5,3200,580, new LightLocation[]{
			new LightLocation(3656, 3227), new LightLocation(3661, 3225),
			new LightLocation(3664, 3225),
			new LightLocation(3660, 3234, Alignment.SOUTH),
			new LightLocation(3665, 3234, Alignment.SOUTH),
			new LightLocation(3664, 3213, Alignment.SOUTH),
			new LightLocation(3661, 3213, Alignment.SOUTH),
			new LightLocation(3669, 3211, Alignment.WEST),
			new LightLocation(3665, 3204, Alignment.NORTH),
			new LightLocation(3660, 3204, Alignment.NORTH),
			new LightLocation(3685, 3207, Alignment.WEST),
			new LightLocation(3678, 3215, Alignment.WEST),
			new LightLocation(3678, 3223, Alignment.WEST),
			new LightLocation(3684, 3231, Alignment.EAST),
			new LightLocation(3668, 3227, Alignment.EAST),
			new LightLocation(3656, 3211, Alignment.EAST),
	}),
	VER_SINHAZA_DOOR(LightType.PULSE, new Color(220, 60, 0), 500, 300,5,3200,920, new LightLocation[]{
			new LightLocation(3679, 3219, Alignment.EAST),
			new LightLocation(3687, 3222, Alignment.SOUTH),
			new LightLocation(3687, 3216, Alignment.NORTH),
	}),
	VER_SINHAZA_1(LightType.PULSE, new Color(220, 60, 0), 500, 500,5,3200,500, new LightLocation[]{
			new LightLocation(3662, 3226, Alignment.EAST),
	}),
	VER_SINHAZA_2(LightType.PULSE, new Color(220, 60, 0), 500, 300,5,3200,540, new LightLocation[]{
			new LightLocation(3644, 3213), new LightLocation(3640, 3203),
			new LightLocation(3644, 3225), new LightLocation(3640, 3235),
	}),
	LITHKREN_BASEMENT_SUNLIGHT(LightType.STATIC, new Color(255, 243, 204), 400, 1500,0.2f,1000,800, new LightLocation[]{
			new LightLocation(3549, 10477, Alignment.EAST),
	}),
	CASTLE_WARS(LightType.STATIC, new Color(130, 180, 255), 400, 700,0.2f,1000,260, new LightLocation[]{
			new LightLocation(2399, 9499), new LightLocation(2400, 9508),
			new LightLocation(2369, 9525), new LightLocation(2430, 9482),
	});

	private final Color color;
	private final int radius;
	private final float strength;
	private final float range;
	private final int duration;
	private final int height;
	private final LightType lightType;
	private final LightLocation[] locations;

	@Getter
	private static final  ArrayList<Light> LIGHTS = new ArrayList<>();

	static
	{
		for (LightData light : values())
		{
			for (LightLocation point: light.locations)
			{
				LIGHTS.add(new Light(
						point,
						light.height,
						light.radius,
						light.strength,
						new int[]{light.color.getRed(), light.color.getGreen(), light.color.getBlue()},
						light.lightType,
						light.duration,
						light.range,
						0)
				);
			}
		}
	}

	LightData(LightType defaultType, Color color, float defaultStrength, int defaultRadius, float defaultRange, int defaultDuration, int defaultHeight, LightLocation[] locations)
	{
		this.lightType = defaultType;
		this.color = color;
		this.radius = defaultRadius;
		this.strength = defaultStrength / 100f;
		this.duration = defaultDuration;
		this.height = defaultHeight;
		this.range = defaultRange;
		this.locations = locations;
	}

}
