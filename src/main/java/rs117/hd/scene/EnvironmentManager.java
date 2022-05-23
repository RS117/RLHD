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
package rs117.hd.scene;

import com.google.common.primitives.Floats;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import static net.runelite.api.Constants.CHUNK_SIZE;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import rs117.hd.HdPlugin;
import rs117.hd.HdPluginConfig;
import rs117.hd.data.environments.Environment;
import rs117.hd.utils.HDUtils;
import rs117.hd.config.DefaultSkyColor;
import rs117.hd.utils.Rect;

@Singleton
@Slf4j
public class EnvironmentManager
{
	@Inject
	private Client client;

	@Inject
	private HdPluginConfig config;

	@Inject
	private HdPlugin hdPlugin;

	private ArrayList<Environment> sceneEnvironments;
	private Environment currentEnvironment;
	private final Environment defaultEnvironment = Environment.OVERWORLD;

	// transition time
	private final int transitionDuration = 3000;
	// distance in tiles to skip transition (e.g. entering cave, teleporting)
	// walking across a loading line causes a movement of 40-41 tiles
	private final int skipTransitionTiles = 41;

	// last environment change time
	private long startTime = 0;
	// transition complete time
	private long transitionCompleteTime = 0;
	// time of last frame; used for lightning
	long lastFrameTime = -1;
	// used for tracking changes to settings
	DefaultSkyColor lastSkyColor = DefaultSkyColor.DEFAULT;
	boolean lastEnvironmentLighting = true;
	boolean lastSkyOverride = false;
	boolean lastUnderwater = false;

	// previous camera target world X
	private int prevCamTargetX = 0;
	// previous camera target world Y
	private int prevCamTargetY = 0;

	public static final float[] BLACK_COLOR = {0,0,0};

	private float[] startFogColor = new float[]{0,0,0};
	public float[] currentFogColor = new float[]{0,0,0};
	private float[] targetFogColor = new float[]{0,0,0};

	private float[] startWaterColor = new float[]{0,0,0};
	public float[] currentWaterColor = new float[]{0,0,0};
	private float[] targetWaterColor = new float[]{0,0,0};

	private int startFogDepth = 0;
	public int currentFogDepth = 0;
	private int targetFogDepth = 0;

	private float startAmbientStrength = 0f;
	public float currentAmbientStrength = 0f;
	private float targetAmbientStrength = 0f;

	private float[] startAmbientColor = new float[]{0,0,0};
	public float[] currentAmbientColor = new float[]{0,0,0};
	private float[] targetAmbientColor = new float[]{0,0,0};

	private float startDirectionalStrength = 0f;
	public float currentDirectionalStrength = 0f;
	private float targetDirectionalStrength = 0f;

	private float[] startUnderwaterCausticsColor = new float[]{0,0,0};
	public float[] currentUnderwaterCausticsColor = new float[]{0,0,0};
	private float[] targetUnderwaterCausticsColor = new float[]{0,0,0};

	private float startUnderwaterCausticsStrength = 1f;
	public float currentUnderwaterCausticsStrength = 1f;
	private float targetUnderwaterCausticsStrength = 1f;

	private float[] startDirectionalColor = new float[]{0,0,0};
	public float[] currentDirectionalColor = new float[]{0,0,0};
	private float[] targetDirectionalColor = new float[]{0,0,0};

	private float startUnderglowStrength = 0f;
	public float currentUnderglowStrength = 0f;
	private float targetUnderglowStrength = 0f;

	private float[] startUnderglowColor = new float[]{0,0,0};
	public float[] currentUnderglowColor = new float[]{0,0,0};
	private float[] targetUnderglowColor = new float[]{0,0,0};

	private float startGroundFogStart = 0f;
	public float currentGroundFogStart = 0f;
	private float targetGroundFogStart = 0f;

	private float startGroundFogEnd = 0f;
	public float currentGroundFogEnd = 0f;
	private float targetGroundFogEnd = 0f;

	private float startGroundFogOpacity = 0f;
	public float currentGroundFogOpacity = 0f;
	private float targetGroundFogOpacity = 0f;

	private float startLightPitch = 0f;
	public float currentLightPitch = 0f;
	private float targetLightPitch = 0f;

	private float startLightYaw = 0f;
	public float currentLightYaw = 0f;
	private float targetLightYaw = 0f;

	public boolean lightningEnabled = false;

	public void update()
	{
		WorldPoint camPosition = localPointToWorldTile(hdPlugin.camTarget[0], hdPlugin.camTarget[1]);
		int camTargetX = camPosition.getX();
		int camTargetY = camPosition.getY();
		int camTargetZ = camPosition.getPlane();

		for (Environment environment : sceneEnvironments)
		{
			if (environment.getArea().containsPoint(camTargetX, camTargetY, camTargetZ))
			{
				if (environment != currentEnvironment)
				{
					if (environment == Environment.PLAYER_OWNED_HOUSE || environment == Environment.PLAYER_OWNED_HOUSE_SNOWY) {
						hdPlugin.setInHouse(true);
						hdPlugin.setNextSceneReload(System.currentTimeMillis() + 2500);
					} else {
						hdPlugin.setInHouse(false);
					}

					hdPlugin.setInGauntlet(environment == Environment.THE_GAUNTLET || environment == Environment.THE_GAUNTLET_CORRUPTED);

					changeEnvironment(environment, camTargetX, camTargetY, false);
				}
				break;
			}
		}

		if (lastSkyColor != config.defaultSkyColor() ||
			lastEnvironmentLighting != config.atmosphericLighting() ||
			lastSkyOverride != config.overrideSky() ||
			lastUnderwater != isUnderwater())
		{
			changeEnvironment(currentEnvironment, camTargetX, camTargetY, true);
		}

		// modify all environment values during transition
		long currentTime = System.currentTimeMillis();
		if (currentTime >= transitionCompleteTime)
		{
			currentFogColor = targetFogColor;
			currentWaterColor = targetWaterColor;
			currentFogDepth = targetFogDepth;
			currentAmbientStrength = targetAmbientStrength;
			currentAmbientColor = targetAmbientColor;
			currentDirectionalStrength = targetDirectionalStrength;
			currentDirectionalColor = targetDirectionalColor;
			currentUnderglowStrength = targetUnderglowStrength;
			currentUnderglowColor = targetUnderglowColor;
			currentGroundFogStart = targetGroundFogStart;
			currentGroundFogEnd = targetGroundFogEnd;
			currentGroundFogOpacity = targetGroundFogOpacity;
			currentLightPitch = targetLightPitch;
			currentLightYaw = targetLightYaw;
			currentUnderwaterCausticsColor = targetUnderwaterCausticsColor;
			currentUnderwaterCausticsStrength = targetUnderwaterCausticsStrength;
		}
		else
		{
			// interpolate between start and target values
			float t = (float)(currentTime - startTime) / (float)transitionDuration;

			currentFogColor = HDUtils.lerpVectors(startFogColor, targetFogColor, t);
			currentWaterColor = HDUtils.lerpVectors(startWaterColor, targetWaterColor, t);
			currentFogDepth = (int) HDUtils.lerp(startFogDepth, targetFogDepth, t);
			currentAmbientStrength = HDUtils.lerp(startAmbientStrength, targetAmbientStrength, t);
			currentAmbientColor = HDUtils.lerpVectors(startAmbientColor, targetAmbientColor, t);
			currentDirectionalStrength = HDUtils.lerp(startDirectionalStrength, targetDirectionalStrength, t);
			currentDirectionalColor = HDUtils.lerpVectors(startDirectionalColor, targetDirectionalColor, t);
			currentUnderglowStrength = HDUtils.lerp(startUnderglowStrength, targetUnderglowStrength, t);
			currentUnderglowColor = HDUtils.lerpVectors(startUnderglowColor, targetUnderglowColor, t);
			currentGroundFogStart  = HDUtils.lerp(startGroundFogStart, targetGroundFogStart, t);
			currentGroundFogEnd  = HDUtils.lerp(startGroundFogEnd, targetGroundFogEnd, t);
			currentGroundFogOpacity  = HDUtils.lerp(startGroundFogOpacity, targetGroundFogOpacity, t);
			currentLightPitch = HDUtils.lerp(startLightPitch, targetLightPitch, t);
			currentLightYaw = HDUtils.lerp(startLightYaw, targetLightYaw, t);
			currentUnderwaterCausticsColor = HDUtils.lerpVectors(startUnderwaterCausticsColor, targetUnderwaterCausticsColor, t);
			currentUnderwaterCausticsStrength = HDUtils.lerp(startUnderwaterCausticsStrength, targetUnderwaterCausticsStrength, t);
		}

		updateLightning();

		// update some things for use next frame
		prevCamTargetX = camTargetX;
		prevCamTargetY = camTargetY;
		lastFrameTime = System.currentTimeMillis();
		lastSkyColor = config.defaultSkyColor();
		lastSkyOverride = config.overrideSky();
		lastEnvironmentLighting = config.atmosphericLighting();
		lastUnderwater = isUnderwater();
	}

	/**
	 * Updates variables used in transition effects
	 *
	 * @param newEnvironment
	 * @param camTargetX
	 * @param camTargetY
	 */
	private void changeEnvironment(Environment newEnvironment, int camTargetX, int camTargetY, boolean instantChange)
	{
		currentEnvironment = newEnvironment;
		log.debug("currentEnvironment changed to " + newEnvironment);

		startTime = System.currentTimeMillis();
		transitionCompleteTime = instantChange ? 0 : startTime + transitionDuration;

		// set previous variables to current ones
		startFogColor = currentFogColor;
		startWaterColor = currentWaterColor;
		startFogDepth = currentFogDepth;
		startAmbientStrength = currentAmbientStrength;
		startAmbientColor = currentAmbientColor;
		startDirectionalStrength = currentDirectionalStrength;
		startDirectionalColor = currentDirectionalColor;
		startUnderglowStrength = currentUnderglowStrength;
		startUnderglowColor = currentUnderglowColor;
		startGroundFogStart = currentGroundFogStart;
		startGroundFogEnd = currentGroundFogEnd;
		startGroundFogOpacity = currentGroundFogOpacity;
		startLightPitch = currentLightPitch;
		startLightYaw = currentLightYaw;
		startUnderwaterCausticsColor = currentUnderwaterCausticsColor;
		startUnderwaterCausticsStrength = currentUnderwaterCausticsStrength;

		updateSkyColor();

		targetFogDepth = newEnvironment.getFogDepth();
		if (hdPlugin.configWinterTheme)
		{
			if (!newEnvironment.isCustomFogDepth())
			{
				targetFogDepth = Environment.WINTER.getFogDepth();
			}
		}

		if (config.atmosphericLighting())
		{
			targetAmbientStrength = newEnvironment.getAmbientStrength();
			targetAmbientColor = newEnvironment.getAmbientColor();
			targetDirectionalStrength = newEnvironment.getDirectionalStrength();
			targetDirectionalColor = newEnvironment.getDirectionalColor();
			targetUnderglowStrength = newEnvironment.getUnderglowStrength();
			targetUnderglowColor = newEnvironment.getUnderglowColor();
			targetLightPitch = newEnvironment.getLightPitch();
			targetLightYaw = newEnvironment.getLightYaw();

			if (hdPlugin.configWinterTheme)
			{
				if (!newEnvironment.isCustomAmbientStrength())
				{
					targetAmbientStrength = Environment.WINTER.getAmbientStrength();
				}
				if (!newEnvironment.isCustomAmbientColor())
				{
					targetAmbientColor = Environment.WINTER.getAmbientColor();
				}
				if (!newEnvironment.isCustomDirectionalStrength())
				{
					targetDirectionalStrength = Environment.WINTER.getDirectionalStrength();
				}
				if (!newEnvironment.isCustomDirectionalColor())
				{
					targetDirectionalColor = Environment.WINTER.getDirectionalColor();
				}
			}
		}
		else
		{
			targetAmbientStrength = defaultEnvironment.getAmbientStrength();
			targetAmbientColor = defaultEnvironment.getAmbientColor();
			targetDirectionalStrength = defaultEnvironment.getDirectionalStrength();
			targetDirectionalColor = defaultEnvironment.getDirectionalColor();
			targetUnderglowStrength = defaultEnvironment.getUnderglowStrength();
			targetUnderglowColor = defaultEnvironment.getUnderglowColor();
			targetLightPitch = defaultEnvironment.getLightPitch();
			targetLightYaw = defaultEnvironment.getLightYaw();

			if (hdPlugin.configWinterTheme)
			{
				if (!defaultEnvironment.isCustomAmbientStrength())
				{
					targetAmbientStrength = Environment.WINTER.getAmbientStrength();
				}
				if (!defaultEnvironment.isCustomAmbientColor())
				{
					targetAmbientColor = Environment.WINTER.getAmbientColor();
				}
				if (!defaultEnvironment.isCustomDirectionalStrength())
				{
					targetDirectionalStrength = Environment.WINTER.getDirectionalStrength();
				}
				if (!defaultEnvironment.isCustomDirectionalColor())
				{
					targetDirectionalColor = Environment.WINTER.getDirectionalColor();
				}
			}
		}
		targetGroundFogStart = newEnvironment.getGroundFogStart();
		targetGroundFogEnd = newEnvironment.getGroundFogEnd();
		targetGroundFogOpacity = newEnvironment.getGroundFogOpacity();
		targetUnderwaterCausticsColor = newEnvironment.getUnderwaterCausticsColor();
		targetUnderwaterCausticsStrength = newEnvironment.getUnderwaterCausticsStrength();

		lightningEnabled = newEnvironment.isLightningEnabled();

		int tileChangeX = Math.abs(prevCamTargetX - camTargetX);
		int tileChangeY = Math.abs(prevCamTargetY - camTargetY);
		int tileChange = Math.max(tileChangeX, tileChangeY);

		// skip the transitional fade if the player has moved too far
		// since the previous frame. results in an instant transition when
		// teleporting, entering dungeons, etc.
		if (tileChange >= skipTransitionTiles)
		{
			transitionCompleteTime = startTime;
		}
	}

	public void updateSkyColor()
	{
		Environment env = hdPlugin.configWinterTheme ? Environment.WINTER : currentEnvironment;
		if (!env.isCustomAmbientColor() || env.isAllowSkyOverride() && config.overrideSky())
		{
			DefaultSkyColor sky = config.defaultSkyColor();
			targetFogColor = sky.getRgb(client);
			if (sky == DefaultSkyColor.OSRS)
			{
				sky = DefaultSkyColor.DEFAULT;
			}
			targetWaterColor = sky.getRgb(client);
		}
		else
		{
			targetFogColor = targetWaterColor = env.getFogColor();
		}
	}

	/**
	 * Figures out which Areas exist in the current scene and
	 * adds them to lists for easy access.
	 */
	public void loadSceneEnvironments()
	{
		// loop through all Areas, check Rects of each Area. if any
		// coordinates overlap scene coordinates, add them to a list.
		// then loop through all Environments, checking to see if any
		// of their Areas match any of the ones in the current scene.
		// if so, add them to a list.

		sceneEnvironments = new ArrayList<>();

		int sceneMinX = client.getBaseX();
		int sceneMinY = client.getBaseY();
		if (client.isInInstancedRegion())
		{
			LocalPoint localPoint = client.getLocalPlayer() != null ? client.getLocalPlayer().getLocalLocation() : new LocalPoint(0, 0);
			WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
			sceneMinX = worldPoint.getX() - localPoint.getSceneX();
			sceneMinY = worldPoint.getY() - localPoint.getSceneY();
		}
		int sceneMaxX = sceneMinX + Constants.SCENE_SIZE - 2;
		int sceneMaxY = sceneMinY + Constants.SCENE_SIZE - 2;

		log.debug("adding environments for scene {},{} - {},{}..", sceneMinX, sceneMinY, sceneMaxX, sceneMaxY);

		for (Environment environment: Environment.values())
		{
			for (Rect rect : environment.getArea().getRects())
			{
				if (rect.getMinX() > sceneMaxX || sceneMinX > rect.getMaxX() || rect.getMinY() > sceneMaxY || sceneMinY > rect.getMaxY())
				{
					continue;
				}
				log.debug("added environment {} to sceneArea list", environment.name());
				sceneEnvironments.add(environment);
				break;
			}
		}

		for (Environment environment : sceneEnvironments)
		{
			log.debug("sceneArea: " + environment.name());
		}

		if (currentEnvironment != null)
		{
			WorldPoint camPosition = localPointToWorldTile(hdPlugin.camTarget[0], hdPlugin.camTarget[1]);
			int camTargetX = camPosition.getX();
			int camTargetY = camPosition.getY();
			changeEnvironment(currentEnvironment, camTargetX, camTargetY, false);
		}
	}



	/* lightning */
	public float lightningBrightness = 0f;
	public float[] lightningColor = new float[]{1.0f, 1.0f, 1.0f};
	double nextLightningTime = -1;
	float newLightningBrightness = 7f;
	float lightningFadeSpeed = 80f; // brightness units per second
	int minLightningInterval = 5500;
	int maxLightningInterval = 17000;
	float quickLightningChance = 2f;
	int minQuickLightningInterval = 40;
	int maxQuickLightningInterval = 150;

	/**
	 * Updates lightning variables and sets water reflection and sky
	 * colors during lightning flashes.
	 */
	void updateLightning()
	{
		if (lightningBrightness > 0)
		{
			int frameTime = (int)(System.currentTimeMillis() - lastFrameTime);
			float brightnessChange = (frameTime / 1000f) * lightningFadeSpeed;
			lightningBrightness = Math.max(lightningBrightness - brightnessChange, 0);
		}

		if (nextLightningTime == -1)
		{
			generateNextLightningTime();
			return;
		}
		if (System.currentTimeMillis() > nextLightningTime)
		{
			lightningBrightness = newLightningBrightness;

			generateNextLightningTime();
		}

		if (lightningEnabled && config.flashingEffects())
		{
			float t = Floats.constrainToRange(lightningBrightness, 0.0f, 1.0f);
			currentFogColor = HDUtils.lerpVectors(currentFogColor, lightningColor, t);
			currentWaterColor = HDUtils.lerpVectors(currentWaterColor, lightningColor, t);
		}
		else
		{
			lightningBrightness = 0f;
		}
	}

	/**
	 * Determines when the next lighting strike will occur.
	 * Produces a short interval for a quick successive strike
	 * or a longer interval at the end of a cluster.
	 */
	void generateNextLightningTime()
	{
		int lightningInterval = (int)(minLightningInterval + ((maxLightningInterval - minLightningInterval) * Math.random()));
		int quickLightningInterval = (int)(minQuickLightningInterval + ((maxQuickLightningInterval - minQuickLightningInterval) * Math.random()));
		if (Math.random() <= 1f / quickLightningChance)
		{
			// chain together lighting strikes in quick succession
			nextLightningTime = System.currentTimeMillis() + quickLightningInterval;
		}
		else
		{
			// cool-down period before a new lightning cluster
			nextLightningTime = System.currentTimeMillis() + lightningInterval;
		}
	}

	/**
	 * Returns the current fog color if logged in.
	 * Else, returns solid black.
	 *
	 * @return
	 */
	public float[] getFogColor()
	{
		return Arrays.copyOf(client.getGameState().getState() >= GameState.LOADING.getState() ?
			currentFogColor : BLACK_COLOR, 3);
	}

	/**
	 * Returns the world tile coordinates of a given local point, adjusted to template coordinates if within an instance.
	 *
	 * @param pointX
	 * @param pointY
	 * @return adjusted world coordinates
	 */
	WorldPoint localPointToWorldTile(int pointX, int pointY)
	{
		int[][][] instanceTemplateChunks = client.getInstanceTemplateChunks();
		LocalPoint localPoint = new LocalPoint(pointX, pointY);
		int chunkX = localPoint.getSceneX() / CHUNK_SIZE;
		int chunkY = localPoint.getSceneY() / CHUNK_SIZE;

		if (client.isInInstancedRegion() && chunkX >= 0 && chunkX < instanceTemplateChunks[client.getPlane()].length && chunkY >= 0 && chunkY < instanceTemplateChunks[client.getPlane()][chunkX].length)
		{
			// In some scenarios, taking the detached camera outside of instances
			// will result in a crash if we don't check the chunk array indices first
			return WorldPoint.fromLocalInstance(client, localPoint);
		}
		else
		{
			return WorldPoint.fromLocal(client, localPoint);
		}
	}

	public boolean isUnderwater()
	{
		return currentEnvironment != null && currentEnvironment.isUnderwater();
	}
}
