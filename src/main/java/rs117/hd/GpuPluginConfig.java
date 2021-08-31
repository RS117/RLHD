/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import static rs117.hd.HdPlugin.MAX_DISTANCE;
import static rs117.hd.HdPlugin.MAX_FOG_DEPTH;
import rs117.hd.config.AntiAliasingMode;
import rs117.hd.config.ColorBlindMode;
import rs117.hd.config.Contrast;
import rs117.hd.config.Brightness;
import rs117.hd.config.LevelOfDetail;
import rs117.hd.config.MaxDynamicLights;
import rs117.hd.config.Saturation;
import rs117.hd.config.DefaultSkyColor;
import rs117.hd.config.FogDepthMode;
import rs117.hd.config.ShadowDistance;
import rs117.hd.config.ShadowResolution;
import rs117.hd.config.UIScalingMode;
import rs117.hd.config.WaterEffects;

@ConfigGroup("gpu")
public interface GpuPluginConfig extends Config
{
	/*====== General settings ======*/

	@Range(
		max = MAX_DISTANCE
	)
	@ConfigItem(
		keyName = "drawDistance",
		name = "Draw Distance",
		description = "Draw distance",
		position = 1
	)
	default int drawDistance()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "smoothBanding",
		name = "Remove Color Banding",
		description = "Smooths out the color banding that is present in the CPU renderer",
		position = 2
	)
	default boolean smoothBanding()
	{
		return false;
	}

	@ConfigItem(
		keyName = "antiAliasingMode",
		name = "Anti Aliasing",
		description = "Configures the anti-aliasing mode",
		position = 3
	)
	default AntiAliasingMode antiAliasingMode()
	{
		return AntiAliasingMode.DISABLED;
	}

	@ConfigItem(
		keyName = "uiScalingMode",
		name = "UI scaling mode",
		description = "Sampling function to use for the UI in stretched mode",
		position = 4
	)
	default UIScalingMode uiScalingMode()
	{
		return UIScalingMode.LINEAR;
	}

	@Range(
		max = MAX_FOG_DEPTH
	)
	@ConfigItem(
		keyName = "fogDepth",
		name = "Fog depth",
		description = "Distance from the scene edge the fog starts",
		position = 5
	)
	default int fogDepth()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "useComputeShaders",
		name = "Compute Shaders",
		description = "Offloads face sorting to GPU, enabling extended draw distance.",
		warning = "This feature requires OpenGL 4.3 to use. Please check that your GPU supports this.",
		position = 6
	)
	default boolean useComputeShaders()
	{
		return true;
	}

	@Range(
		min = 0,
		max = 16
	)
	@ConfigItem(
		keyName = "anisotropicFilteringLevel",
		name = "Anisotropic Filtering",
		description = "Configures the anisotropic filtering level.",
		position = 7
	)
	default int anisotropicFilteringLevel()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "colorBlindMode",
		name = "Colorblindness Correction",
		description = "Adjusts colors to account for colorblindness",
		position = 8
	)
	default ColorBlindMode colorBlindMode()
	{
		return ColorBlindMode.NONE;
	}

	@ConfigItem(
		keyName = "brightTextures",
		name = "Bright Textures",
		description = "Use old texture lighting method which results in brighter game textures",
		position = 9
	)
	default boolean brightTextures()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hdMode",
		name = "High Detail Mode (Beta)",
		description = "Enables a set of graphical improvements.",
		warning = "This feature is in beta and may cause game crashes. Use with caution!",
		position = 10
	)
	default boolean hdMode()
	{
		return false;
	}



	/*====== General HD settings ======*/

	@ConfigSection(
		name = "[HD] General",
		description = "General settings for High Detail mode.",
		position = 100,
		closedByDefault = false
	)
	String generalHDSettings = "generalHDSettings";

	@ConfigItem(
		keyName = "flashingEffects",
		name = "Flashing Effects",
		description = "Displays fast flashing effects such as lightning in certain areas.",
		position = 101,
		section = generalHDSettings
	)
	default boolean flashingEffects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "saturation",
		name = "Saturation",
		description = "Controls the saturation of the final rendered image.",
		position = 102,
		section = generalHDSettings
	)
	default Saturation saturation()
	{
		return Saturation.DEFAULT;
	}

	@ConfigItem(
		keyName = "contrast",
		name = "Contrast",
		description = "Controls the contrast of the final rendered image.",
		position = 103,
		section = generalHDSettings
	)
	default Contrast contrast()
	{
		return Contrast.DEFAULT;
	}

	@ConfigItem(
		keyName = "brightness",
		name = "Brightness",
		description = "Controls the brightness of scene lighting.",
		position = 104,
		section = generalHDSettings
	)
	default Brightness brightness()
	{
		return Brightness.DEFAULT;
	}

	@ConfigItem(
		keyName = "levelOfDetail",
		name = "Level of Detail",
		description = "Improves performance by preventing certain distant objects from being drawn.",
		position = 105,
		section = generalHDSettings
	)
	default LevelOfDetail levelOfDetail()
	{
		return LevelOfDetail.MEDIUM;
	}



	/*====== Lighting settings ======*/

	@ConfigSection(
		name = "[HD] Lighting",
		description = "Lighting settings for High Detail mode.",
		position = 200,
		closedByDefault = true
	)
	String lightingSettings = "lightingSettings";

	@ConfigItem(
		keyName = "maxDynamicLights",
		name = "Dynamic Lights",
		description = "The maximum number of dynamic lights visible at one time. Reducing this will improve performance.",
		position = 201,
		section = lightingSettings
	)
	default MaxDynamicLights maxDynamicLights()
	{
		return MaxDynamicLights.FEW;
	}

	@ConfigItem(
		keyName = "projectileLights",
		name = "Projectile Lights",
		description = "Adds dynamic lights to some projectiles.",
		position = 202,
		section = lightingSettings
	)
	default boolean projectileLights()
	{
		return true;
	}

	@ConfigItem(
		keyName = "npcLights",
		name = "NPC Lights",
		description = "Adds dynamic lights to some NPCs.",
		position = 203,
		section = lightingSettings
	)
	default boolean npcLights()
	{
		return true;
	}

	@ConfigItem(
		keyName = "atmosphericLighting",
		name = "Atmospheric Lighting",
		description = "Changes the color and brightness of full-scene lighting in certain areas.",
		position = 204,
		section = lightingSettings
	)
	default boolean atmosphericLighting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shadowsEnabled",
		name = "Shadows",
		description = "Enables fully-dynamic shadows. Requires compute shaders.",
		position = 210,
		section = lightingSettings
	)
	default boolean shadowsEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "shadowResolution",
		name = "Shadow Resolution",
		description = "The resolution at which shadow maps are drawn. Higher resolutions result in sharper, higher quality shadows at the cost of performance.",
		position = 211,
		section = lightingSettings
	)
	default ShadowResolution shadowResolution()
	{
		return ShadowResolution.RES_2048;
	}

	@ConfigItem(
		keyName = "shadowDistance",
		name = "Shadow Distance",
		description = "The maximum draw distance of shadow maps. Shorter distances result in sharper, higher quality shadows.",
		position = 212,
		section = lightingSettings
	)
	default ShadowDistance shadowDistance()
	{
		return ShadowDistance.DISTANCE_50;
	}

	@ConfigItem(
		keyName = "expandShadowDraw",
		name = "Expand Shadow Draw",
		description = "Reduces 'flickering' of shadows disappearing at screen edge by increasing geometry drawn at a cost of performance.",
		position = 213,
		section = lightingSettings
	)
	default boolean expandShadowDraw()
	{
		return false;
	}



	/*====== Environment settings ======*/

	@ConfigSection(
		name = "[HD] Environment",
		description = "Environment settings for High Detail mode.",
		position = 300,
		closedByDefault = true
	)
	String environmentSettings = "environmentSettings";

	@ConfigItem(
		keyName = "fogDepthMode",
		name = "Fog Depth Mode",
		description = "Determines the method of controlling the depth of the fog. 'Dynamic' changes fog depth based on the area.",
		position = 301,
		section = environmentSettings
	)
	default FogDepthMode fogDepthMode()
	{
		return FogDepthMode.DYNAMIC;
	}

	@ConfigItem(
		keyName = "groundFog",
		name = "Ground Fog",
		description = "Enables a height-based fog effect that covers the ground in certain areas.",
		position = 302,
		section = environmentSettings
	)
	default boolean groundFog() {
		return true;
	}

	@ConfigItem(
		keyName = "defaultSkyColor",
		name = "Default Sky Color",
		description = "Determines the color of the sky when in a location without a custom sky color assigned.",
		position = 303,
		section = environmentSettings
	)
	default DefaultSkyColor defaultSkyColor()
	{
		return DefaultSkyColor.DEFAULT;
	}

	@ConfigItem(
		keyName = "objectTextures",
		name = "Object Textures",
		description = "Adds detail textures to certain world objects. Requires compute shaders.",
		position = 304,
		section = environmentSettings
	)
	default boolean objectTextures()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groundTextures",
		name = "Ground Textures",
		description = "Adds detail textures to the ground.",
		position = 305,
		section = environmentSettings
	)
	default boolean groundTextures()
	{
		return true;
	}

	@ConfigItem(
		keyName = "waterEffects",
		name = "Water Effects",
		description = "Changes the appearance of the water. ",
		position = 306,
		section = environmentSettings
	)
	default WaterEffects waterEffects()
	{
		return WaterEffects.ALL;
	}

	@ConfigItem(
		keyName = "tzhaarHD",
		name = "HD TzHaar Reskin",
		description = "Recolors the TzHaar city of Mor Ul Rek to give it an appearance similar to that of its 2008 HD variant. Requires compute shaders.",
		position = 307,
		section = environmentSettings
	)
	default boolean tzhaarHD()
	{
		return true;
	}
}
