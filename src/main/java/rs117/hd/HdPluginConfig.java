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
import rs117.hd.config.MaxDynamicLights;
import rs117.hd.config.Saturation;
import rs117.hd.config.DefaultSkyColor;
import rs117.hd.config.FogDepthMode;
import rs117.hd.config.ShadowDistance;
import rs117.hd.config.ShadowResolution;
import rs117.hd.config.UIScalingMode;

@ConfigGroup("hd")
public interface HdPluginConfig extends Config
{
	/*====== Limited-time settings ======*/

	@ConfigSection(
		name = "Limited-time",
		description = "Fun and/or experimental settings that are available for a limited time",
		position = -10,
		closedByDefault = false
	)
	String limitedTimeSettings = "limitedTimeSettings";

	@ConfigItem(
		keyName = "winterTheme0",
		name = "Winter theme",
		description = "Covers the Gielinor overworld with a layer of snow!",
		position = -9,
		section = limitedTimeSettings
	)
	default boolean winterTheme()
	{
		return false;
	}


	/*====== General settings ======*/

	@ConfigSection(
		name = "General",
		description = "General settings",
		position = 0,
		closedByDefault = false
	)
	String generalSettings = "generalSettings";

	@Range(
		max = MAX_DISTANCE
	)
	@ConfigItem(
		keyName = "drawDistance",
		name = "Draw Distance",
		description = "Draw distance",
		position = 1,
		section = generalSettings
	)
	default int drawDistance()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "antiAliasingMode",
		name = "Anti Aliasing",
		description = "Improves jagged/shimmering edges at a cost of GPU performance. 8x/16x MSAA are highly expensive.",
		position = 2,
		section = generalSettings
	)
	default AntiAliasingMode antiAliasingMode()
	{
		return AntiAliasingMode.DISABLED;
	}

	@ConfigItem(
		keyName = "uiScalingMode",
		name = "UI scaling mode",
		description = "Sampling function to use for the UI in stretched mode",
		position = 3,
		section = generalSettings
	)
	default UIScalingMode uiScalingMode()
	{
		return UIScalingMode.LINEAR;
	}

	@Range(
		min = 0,
		max = 16
	)
	@ConfigItem(
		keyName = "anisotropicFilteringLevel",
		name = "Anisotropic Filtering",
		description = "Configures the anisotropic filtering level from 0 to 16x.",
		position = 4,
		section = generalSettings
	)
	default int anisotropicFilteringLevel()
	{
		return 16;
	}

	@ConfigItem(
		keyName = "unlockFps",
		name = "Unlock FPS",
		description = "Removes the 50 FPS cap for some game content such as camera movement and dynamic lighting.",
		position = 5,
		section = generalSettings
	)
	default boolean unlockFps()
	{
		return false;
	}

	enum SyncMode
	{
		OFF,
		ON,
		ADAPTIVE
	}

	@ConfigItem(
			keyName = "vsyncMode",
			name = "VSync Mode",
			description = "Method to synchronize frame rate with refresh rate",
			position = 6,
			section = generalSettings
	)
	default SyncMode syncMode()
	{
		return SyncMode.ADAPTIVE;
	}

	@ConfigItem(
			keyName = "fpsTarget",
			name = "FPS Target",
			description = "Target FPS when unlock FPS is enabled and Vsync mode is OFF",
			position = 7,
			section = generalSettings
	)
	@Range(
			min = 0,
			max = 999
	)
	default int fpsTarget()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "colorBlindMode",
		name = "Colorblindness Correction",
		description = "Adjusts colors to account for colorblindness",
		position = 8,
		section = generalSettings
	)
	default ColorBlindMode colorBlindMode()
	{
		return ColorBlindMode.NONE;
	}

	@ConfigItem(
		keyName = "flashingEffects",
		name = "Flashing Effects",
		description = "Displays fast flashing effects, such as lightning, in certain areas.",
		position = 9,
		section = generalSettings
	)
	default boolean flashingEffects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "saturation",
		name = "Saturation",
		description = "Controls the saturation of the final rendered image.",
		position = 10,
		section = generalSettings
	)
	default Saturation saturation()
	{
		return Saturation.DEFAULT;
	}

	@ConfigItem(
		keyName = "contrast",
		name = "Contrast",
		description = "Controls the contrast of the final rendered image.",
		position = 11,
		section = generalSettings
	)
	default Contrast contrast()
	{
		return Contrast.DEFAULT;
	}

	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		keyName = "brightness2",
		name = "Brightness",
		description = "Controls the brightness of scene lighting.",
		position = 12,
		section = generalSettings
	)
	default int brightness() { return 20; }


	/*====== Lighting settings ======*/

	@ConfigSection(
		name = "Lighting",
		description = "Lighting settings",
		position = 100,
		closedByDefault = false
	)
	String lightingSettings = "lightingSettings";

	@ConfigItem(
		keyName = "maxDynamicLights",
		name = "Dynamic Lights",
		description = "The maximum number of dynamic lights visible at one time. Reducing this will improve performance.",
		position = 101,
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
		position = 102,
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
		position = 103,
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
		position = 104,
		section = lightingSettings
	)
	default boolean atmosphericLighting()
	{
		return false;
	}

	@ConfigItem(
		keyName = "shadowsEnabled",
		name = "Shadows",
		description = "Enables fully-dynamic shadows.",
		position = 105,
		section = lightingSettings
	)
	default boolean shadowsEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shadowResolution",
		name = "Shadow Quality",
		description = "The resolution of the shadow maps. Higher resolutions result in sharper, higher quality shadows at the cost of GPU performance.",
		position = 106,
		section = lightingSettings
	)
	default ShadowResolution shadowResolution()
	{
		return ShadowResolution.RES_1024;
	}

	@ConfigItem(
		keyName = "shadowDistance",
		name = "Shadow Distance",
		description = "The maximum draw distance of shadow maps. Shorter distances result in sharper, higher quality shadows.",
		position = 107,
		section = lightingSettings
	)
	default ShadowDistance shadowDistance()
	{
		return ShadowDistance.DISTANCE_30;
	}

	@ConfigItem(
		keyName = "expandShadowDraw",
		name = "Expand Shadow Draw",
		description = "Reduces 'flickering' of shadows disappearing at screen edge by increasing geometry drawn at a cost of performance.",
		position = 108,
		section = lightingSettings
	)
	default boolean expandShadowDraw()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideBakedEffects",
		name = "Hide Fake Lights and Shadows",
		description = "Hides the fake light and shadow effects that Jagex often includes with models",
		position = 109,
		section = lightingSettings
	)
	default boolean hideBakedEffects() {
		return true;
	}

	/*====== Environment settings ======*/

	@ConfigSection(
		name = "Environment",
		description = "Environment settings",
		position = 200,
		closedByDefault = false
	)
	String environmentSettings = "environmentSettings";

	@ConfigItem(
		keyName = "fogDepthMode",
		name = "Fog Depth Mode",
		description = "Determines the method of controlling the depth of the fog. 'Dynamic' changes fog depth based on the area.",
		position = 201,
		section = environmentSettings
	)
	default FogDepthMode fogDepthMode()
	{
		return FogDepthMode.DYNAMIC;
	}

	@Range(
		max = MAX_FOG_DEPTH
	)
	@ConfigItem(
		keyName = "fogDepth",
		name = "Static Fog Depth",
		description = "Distance from the scene edge the fog starts. Applies when 'Fog Depth Mode' is set to 'static'.",
		position = 202,
		section = environmentSettings
	)
	default int fogDepth()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "groundFog",
		name = "Ground Fog",
		description = "Enables a height-based fog effect that covers the ground in certain areas.",
		position = 203,
		section = environmentSettings
	)
	default boolean groundFog() {
		return true;
	}

	@ConfigItem(
		keyName = "defaultSkyColor",
		name = "Default Sky Color",
		description = "Determines the color of the sky when in a location without a custom sky color assigned.",
		position = 204,
		section = environmentSettings
	)
	default DefaultSkyColor defaultSkyColor()
	{
		return DefaultSkyColor.DEFAULT;
	}

	@ConfigItem(
		keyName = "overrideSky",
		name = "Override Sky Color",
		description = "Forces the selected sky color in all environments",
		position = 205,
		section = environmentSettings
	)
	default boolean overrideSky() {
		return false;
	}

	@ConfigItem(
		keyName = "objectTextures",
		name = "Object Textures",
		description = "Adds detail textures to certain world objects.",
		position = 206,
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
		position = 207,
		section = environmentSettings
	)
	default boolean groundTextures()
	{
		return true;
	}

	@ConfigItem(
			keyName = "groundBlending",
			name = "Ground Blending",
			description = "Affects the quality of blending between different ground/terrain textures.",
			position = 208,
			section = environmentSettings
	)
	default boolean groundBlending()
	{
		return true;
	}

	@ConfigItem(
		keyName = "underwaterCaustics",
		name = "Underwater Caustics",
		description = "Apply underwater lighting effects to imitate sunlight moving through waves on the surface.",
		position = 209,
		section = environmentSettings
	)
	default boolean underwaterCaustics()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tzhaarHD",
		name = "HD TzHaar Reskin",
		description = "Recolors the TzHaar city of Mor Ul Rek to give it an appearance similar to that of its 2008 HD variant.",
		position = 210,
		section = environmentSettings
	)
	default boolean tzhaarHD()
	{
		return true;
	}



	/*====== Miscellaneous settings ======*/

	@ConfigSection(
		name = "Miscellaneous",
		description = "Miscellaneous settings",
		position = 300,
		closedByDefault = true
	)
	String miscellaneousSettings = "miscellaneousSettings";

	@ConfigItem(
		keyName = "macosIntelWorkaround",
		name = "Fix shading on MacOS with Intel",
		description = "Workaround for visual artifacts on some Intel GPU drivers on MacOS.",
		warning = "This setting can cause RuneLite to crash, and can be difficult to revert. Only enable it if you\nare seeing black patches. Are you sure you want to enable the setting?",
		position = 301,
		section = miscellaneousSettings
	)
	default boolean macosIntelWorkaround()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hdInfernalTexture",
		name = "HD Infernal Texture",
		description = "Replaces the OSRS infernal cape texture with a high detail one.",
		position = 302,
		section = miscellaneousSettings
	)
	default boolean hdInfernalTexture()
	{
		return true;
	}

	@ConfigItem(
		keyName = "modelCaching",
		name = "Disable model caching",
		description = "Model caching improves performance with increased memory usage.",
		position = 303,
		section = miscellaneousSettings
	)
	default boolean disableModelCaching() { return false; }
}
