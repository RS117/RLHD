/*
 * Copyright (c) 2019 Abex
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
package rs117.hd.scene.lighting;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.primitives.Ints;
import com.jogamp.opengl.math.FloatUtil;
import static com.jogamp.opengl.math.FloatUtil.cos;
import static com.jogamp.opengl.math.FloatUtil.pow;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Projectile;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.entityhider.EntityHiderConfig;
import net.runelite.client.plugins.entityhider.EntityHiderPlugin;
import rs117.hd.utils.HDUtils;
import rs117.hd.HdPlugin;
import rs117.hd.HdPluginConfig;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;

@Singleton
@Slf4j
public class LightManager
{
	public static String LIGHTS_CONFIG_ENV = "RLHD_LIGHTS_PATH";

	@Inject
	private ConfigManager configManager;

	@Inject
	private HdPluginConfig config;

	@Inject
	private Client client;

	@Inject
	private HdPlugin hdPlugin;

	@Inject
	private EntityHiderPlugin entityHiderPlugin;

	@Inject
	private PluginManager pluginManager;

	private static final ArrayList<SceneLight> WORLD_LIGHTS = new ArrayList<>();
	private static final ListMultimap<Integer, Light> NPC_LIGHTS = ArrayListMultimap.create();
	private static final ListMultimap<Integer, Light> OBJECT_LIGHTS = ArrayListMultimap.create();
	private static final ListMultimap<Integer, Light> PROJECTILE_LIGHTS = ArrayListMultimap.create();

	private FileWatcher fileWatcher;
	@Getter
	ArrayList<SceneLight> sceneLights = new ArrayList<>();
	@Getter
	ArrayList<Projectile> sceneProjectiles = new ArrayList<>();

	long lastFrameTime = -1;
	boolean hotswapScheduled = false;

	int sceneMinX = 0;
	int sceneMinY = 0;
	int sceneMaxX = 0;
	int sceneMaxY = 0;

	public int visibleLightsCount = 0;

	private EntityHiderConfig entityHiderConfig;

	public void startUp()
	{

		entityHiderConfig = configManager.getConfig(EntityHiderConfig.class);

		Path lightsConfigPath = Env.getPath(LIGHTS_CONFIG_ENV);
		if (lightsConfigPath == null)
		{
			reloadLightConfiguration();
		}
		else
		{
			reloadLightConfiguration(lightsConfigPath.toFile());

			try
			{
				fileWatcher = new FileWatcher()
					.watchFile(lightsConfigPath)
					.addChangeHandler(path -> hotswapScheduled = true);
			}
			catch (IOException ex)
			{
				log.info("Failed to initialize file watcher", ex);
			}
		}
	}

	public void shutDown()
	{
		reset();
		if (fileWatcher != null)
		{
			fileWatcher.close();
			fileWatcher = null;
		}
	}

	public void clearLightConfiguration()
	{
		WORLD_LIGHTS.clear();
		NPC_LIGHTS.clear();
		OBJECT_LIGHTS.clear();
		PROJECTILE_LIGHTS.clear();
	}

	public void reloadLightConfiguration()
	{
		clearLightConfiguration();
		LightConfig.load(WORLD_LIGHTS, NPC_LIGHTS, OBJECT_LIGHTS, PROJECTILE_LIGHTS);
	}

	public void reloadLightConfiguration(File jsonFile)
	{
		clearLightConfiguration();
		LightConfig.load(jsonFile, WORLD_LIGHTS, NPC_LIGHTS, OBJECT_LIGHTS, PROJECTILE_LIGHTS);
	}

	public void update()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (hotswapScheduled)
		{
			hotswapScheduled = false;
			Path lightsConfigPath = Env.getPath(LIGHTS_CONFIG_ENV);
			if (lightsConfigPath != null && lightsConfigPath.toFile().exists())
			{
				reloadLightConfiguration(lightsConfigPath.toFile());
			}
			else
			{
				reloadLightConfiguration();
			}
			reset();
			loadSceneLights();
		}

		int camX = hdPlugin.camTarget[0];
		int camY = hdPlugin.camTarget[1];
		int camZ = hdPlugin.camTarget[2];

		Iterator<SceneLight> lightIterator = sceneLights.iterator();

		while (lightIterator.hasNext())
		{
			SceneLight light = lightIterator.next();

			long frameTime = System.currentTimeMillis() - lastFrameTime;

			light.distance = Integer.MAX_VALUE;

			if (light.projectile != null)
			{
				if (light.projectile.getRemainingCycles() <= 0)
				{
					lightIterator.remove();
					sceneProjectiles.remove(light.projectile);
					continue;
				}

				light.x = (int) light.projectile.getX();
				light.y = (int) light.projectile.getY();
				light.z = (int) light.projectile.getZ();

				light.visible = projectileLightVisible();
			}

			if (light.npc != null)
			{
				if (light.npc != client.getCachedNPCs()[light.npc.getIndex()])
				{
					lightIterator.remove();
					continue;
				}

				light.x = light.npc.getLocalLocation().getX();
				light.y = light.npc.getLocalLocation().getY();

				// Offset the light's position based on its Alignment
				if (light.alignment == Alignment.NORTH || light.alignment == Alignment.NORTHEAST || light.alignment == Alignment.NORTHWEST)
				{
					light.y += Perspective.LOCAL_HALF_TILE_SIZE;
				}
				if (light.alignment == Alignment.SOUTH || light.alignment == Alignment.SOUTHEAST || light.alignment == Alignment.SOUTHWEST)
				{
					light.y -= Perspective.LOCAL_HALF_TILE_SIZE;
				}
				if (light.alignment == Alignment.EAST || light.alignment == Alignment.SOUTHEAST || light.alignment == Alignment.NORTHEAST)
				{
					light.x += Perspective.LOCAL_HALF_TILE_SIZE;
				}
				if (light.alignment == Alignment.WEST || light.alignment == Alignment.SOUTHWEST || light.alignment == Alignment.NORTHWEST)
				{
					light.x -= Perspective.LOCAL_HALF_TILE_SIZE;
				}

				int plane = light.npc.getWorldLocation().getPlane();
				light.plane = plane;
				int npcTileX = light.npc.getLocalLocation().getSceneX();
				int npcTileY = light.npc.getLocalLocation().getSceneY();

				// Some NPCs, such as Crystalline Hunllef in The Gauntlet, sometimes return scene X/Y values far outside of the possible range.
				if (npcTileX < Perspective.SCENE_SIZE && npcTileY < Perspective.SCENE_SIZE && npcTileX >= 0 && npcTileY >= 0)
				{
					// Tile null check is to prevent oddities caused by - once again - Crystalline Hunllef.
					// May also apply to other NPCs in instances.
					if (client.getScene().getTiles()[plane][npcTileX][npcTileY] != null && client.getScene().getTiles()[plane][npcTileX][npcTileY].getBridge() != null)
					{
						plane++;
					}

					// Interpolate between tile heights based on specific scene coordinates.
					float lerpX = (light.x % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
					float lerpY = (light.y % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
					int baseTileX = (int) Math.floor(light.x / (float) Perspective.LOCAL_TILE_SIZE);
					int baseTileY = (int) Math.floor(light.y / (float) Perspective.LOCAL_TILE_SIZE);
					float heightNorth = HDUtils.lerp(client.getTileHeights()[plane][baseTileX][baseTileY + 1], client.getTileHeights()[plane][baseTileX + 1][baseTileY + 1], lerpX);
					float heightSouth = HDUtils.lerp(client.getTileHeights()[plane][baseTileX][baseTileY], client.getTileHeights()[plane][baseTileX + 1][baseTileY], lerpX);
					float tileHeight = HDUtils.lerp(heightSouth, heightNorth, lerpY);
					light.z = (int) tileHeight - 1 - light.height;

					light.visible = npcLightVisible(light.npc);
				}
				else
				{
					light.visible = false;
				}
			}

			if (light.type == LightType.FLICKER)
			{
				long repeatMs = 60000;
				int offset = light.randomOffset;
				float t = ((System.currentTimeMillis() + offset) % repeatMs) / (float) repeatMs * FloatUtil.TWO_PI;

				float flicker = (
					pow(cos(11 * t), 2) +
						pow(cos(17 * t), 4) +
						pow(cos(23 * t), 6) +
						pow(cos(31 * t), 2) +
						pow(cos(71 * t), 2) / 3 +
						pow(cos(151 * t), 2) / 7
				) / 4.335f;

				float maxFlicker = 1f + (light.range / 100f);
				float minFlicker = 1f - (light.range / 100f);

				flicker = minFlicker + (maxFlicker - minFlicker) * flicker;

				light.currentStrength = light.strength * flicker;
				light.currentSize = (int) (light.radius * flicker * 1.5f);
			}
			else if (light.type == LightType.PULSE)
			{
				float duration = light.duration / 1000f;
				float range = light.range / 100f;
				float fullRange = range * 2f;
				float change = (frameTime / 1000f) / duration;
//				change = change % 1.0f;

				light.currentAnimation += change % 1.0f;
				// lock animation to 0-1
				light.currentAnimation = light.currentAnimation % 1.0f;

				float output;

				if (light.currentAnimation > 0.5f)
				{
					// light is shrinking
					output = 1f - (light.currentAnimation - 0.5f) * 2;
				}
				else
				{
					// light is expanding
					output = light.currentAnimation * 2f;
				}

				float multiplier = (1.0f - range) + output * fullRange;

				light.currentSize = (int) (light.radius * multiplier);
				light.currentStrength = light.strength * multiplier;
			}
			else
			{
				light.currentStrength = light.strength;
				light.currentSize = light.radius;
				light.currentColor = light.color;
			}
			// Apply fade-in
			if (light.fadeInDuration > 0)
			{
				light.currentStrength *= Math.min((float) light.currentFadeIn / (float) light.fadeInDuration, 1.0f);

				light.currentFadeIn += frameTime;
			}

			// Calculate the distance between the player and the light to determine which
			// lights to display based on the 'max dynamic lights' config option
			light.distance = (int) Math.sqrt(Math.pow(camX - light.x, 2) + Math.pow(camY - light.y, 2) + Math.pow(camZ - light.z, 2));

			int tileX = (int) Math.floor(light.x / 128f);
			int tileY = (int) Math.floor(light.y / 128f);
			int tileZ = light.plane;

			light.belowFloor = false;
			light.aboveFloor = false;

			if (tileX < Perspective.SCENE_SIZE && tileY < Perspective.SCENE_SIZE && tileX >= 0 && tileY >= 0)
			{
				Tile aboveTile = tileZ < 3 ? client.getScene().getTiles()[tileZ + 1][tileX][tileY] : null;

				if (aboveTile != null && (aboveTile.getSceneTilePaint() != null || aboveTile.getSceneTileModel() != null))
				{
					light.belowFloor = true;
				}

				Tile lightTile = client.getScene().getTiles()[tileZ][tileX][tileY];

				if (lightTile != null && (lightTile.getSceneTilePaint() != null || lightTile.getSceneTileModel() != null))
				{
					light.aboveFloor = true;
				}
			}
		}

		sceneLights.sort(Comparator.comparingInt(light -> light.distance));

		lastFrameTime = System.currentTimeMillis();
	}

	public boolean npcLightVisible(NPC npc)
	{
		if (npc.getModel() == null)
		{
			return false;
		}

		if (pluginManager.isPluginEnabled(entityHiderPlugin))
		{
			boolean isPet = npc.getComposition().isFollower();
			if (entityHiderConfig.hideNPCs() && !isPet)
			{
				return false;
			}
			if (entityHiderConfig.hidePets() && isPet)
			{
				return false;
			}
		}

		return hdPlugin.configNpcLights;
	}

	public boolean projectileLightVisible()
	{

		if (pluginManager.isPluginEnabled(entityHiderPlugin))
		{
			if (entityHiderConfig.hideProjectiles())
			{
				return false;
			}

		}

		return hdPlugin.configProjectileLights;
	}

	public void reset()
	{
		sceneLights = new ArrayList<>();
		sceneProjectiles = new ArrayList<>();
	}

	public void loadSceneLights()
	{
		sceneMinX = client.getBaseX();
		sceneMinY = client.getBaseY();
		if (client.isInInstancedRegion())
		{
			// adjust coordinates when inside an instanced area
			LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
			WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
			sceneMinX = worldPoint.getX() - localPoint.getSceneX();
			sceneMinY = worldPoint.getY() - localPoint.getSceneY();
		}
		sceneMaxX = sceneMinX + Constants.SCENE_SIZE - 2;
		sceneMaxY = sceneMinY + Constants.SCENE_SIZE - 2;

		for (SceneLight light : WORLD_LIGHTS)
		{
			if (light.worldX >= sceneMinX && light.worldX <= sceneMaxX && light.worldY >= sceneMinY && light.worldY <= sceneMaxY)
			{
				sceneLights.add(light);
				calculateScenePosition(light);
			}
		}
		Tile[][][] tiles = client.getScene().getTiles();
		for (int i = 0; i < tiles.length; i++)
		{
			for (int j = 0; j < tiles[i].length; j++)
			{
				for (int k = 0; k < tiles[i][j].length; k++)
				{
					Tile tile = tiles[i][j][k];

					if (tile == null)
					{
						continue;
					}

					DecorativeObject decorativeObject = tile.getDecorativeObject();
					if (decorativeObject != null && decorativeObject.getRenderable() != null)
					{
						addObjectLight(decorativeObject, tile.getRenderLevel());
					}

					WallObject wallObject = tile.getWallObject();
					if (wallObject != null && wallObject.getRenderable1() != null)
					{
						int orientation = 0;
						// east = 1, south = 2, west = 4, north = 8,
						// southeast = 16, southwest = 32, northwest = 64, northeast = 128
						switch (wallObject.getOrientationA())
						{
							case 1:
								orientation = 512;
								break;
							case 2:
								orientation = 1024;
								break;
							case 4:
								orientation = 1536;
								break;
							case 8:
								orientation = 0;
								break;
							case 16:
								orientation = 768;
								break;
							case 32:
								orientation = 1280;
								break;
							case 64:
								orientation = 1792;
								break;
							case 128:
								orientation = 256;
								break;
						}
						addObjectLight(wallObject, tile.getRenderLevel(), 1, 1, orientation);
					}

					GroundObject groundObject = tile.getGroundObject();
					if (groundObject != null && groundObject.getRenderable() != null)
					{
						addObjectLight(groundObject, tile.getRenderLevel());
					}

					for (GameObject gameObject : tile.getGameObjects())
					{
						if (gameObject != null)
						{
							addObjectLight(gameObject, tile.getRenderLevel(), gameObject.sizeX(), gameObject.sizeY(), gameObject.getOrientation().getAngle());
						}
					}
				}
			}
		}

		updateSceneNpcs();
	}

	public void updateSceneNpcs()
	{
		// check the NPCs in the scene to make sure they have lights assigned, if applicable,
		// for scenarios in which HD mode or dynamic lights were disabled during NPC spawn
		client.getNpcs().forEach(this::addNpcLights);
	}

	public void updateNpcChanged(NpcChanged npcChanged)
	{
		removeNpcLight(npcChanged);
		addNpcLights(npcChanged.getNpc());
	}

	public ArrayList<SceneLight> getVisibleLights(int maxDistance, int maxLights)
	{
		ArrayList<SceneLight> visibleLights = new ArrayList<>();
		int lightsCount = 0;

		for (SceneLight light : sceneLights)
		{
			if (lightsCount >= maxLights || light.distance > maxDistance * Perspective.LOCAL_TILE_SIZE)
			{
				break;
			}
			if (!light.visible)
			{
				continue;
			}
			// Hide certain lights on planes lower than the player to prevent light 'leaking' through the floor
			if (light.plane < client.getPlane() && light.belowFloor)
			{
				continue;
			}
			// Hide any light that is above the current plane and is above a solid floor
			if (light.plane > client.getPlane() && light.aboveFloor)
			{
				continue;
			}

			visibleLights.add(light);
			lightsCount++;
		}
		visibleLightsCount = lightsCount;

		return visibleLights;
	}


	public void addProjectileLight(Projectile projectile)
	{
		for (Light l : PROJECTILE_LIGHTS.get(projectile.getId()))
		{
			// prevent duplicate lights being spawned for the same projectile
			if (sceneProjectiles.contains(projectile))
			{
				continue;
			}

			SceneLight light = new SceneLight(
				0, 0, projectile.getFloor(), 0, Alignment.CENTER, l.radius,
				l.strength, l.color, l.type, l.duration, l.range, 300);
			light.projectile = projectile;
			light.x = (int) projectile.getX();
			light.y = (int) projectile.getY();
			light.z = (int) projectile.getZ();

			sceneProjectiles.add(projectile);
			sceneLights.add(light);
		}
	}

	public void addNpcLights(NPC npc)
	{
		for (Light l : NPC_LIGHTS.get(npc.getId()))
		{
			// prevent duplicate lights being spawned for the same NPC
			if (sceneLights.stream().anyMatch(x -> x.npc == npc))
			{
				continue;
			}

			SceneLight light = new SceneLight(
				0, 0, -1, l.height, l.alignment, l.radius,
				l.strength, l.color, l.type, l.duration, l.range, 0);
			light.npc = npc;
			light.visible = false;

			sceneLights.add(light);
		}
	}

	public void removeNpcLight(NpcDespawned npcDespawned)
	{
		sceneLights.removeIf(light -> light.npc == npcDespawned.getNpc());
	}

	public void removeNpcLight(NpcChanged npcChanged)
	{
		sceneLights.removeIf(light -> light.npc == npcChanged.getNpc());
	}

	public void addObjectLight(TileObject tileObject, int plane)
	{
		addObjectLight(tileObject, plane, 1, 1, -1);
	}

	public void addObjectLight(TileObject tileObject, int plane, int sizeX, int sizeY, int orientation)
	{
		for (Light l : OBJECT_LIGHTS.get(tileObject.getId()))
		{
			// prevent duplicate lights being spawned for the same object
			if (sceneLights.stream().anyMatch(light -> light.object != null && tileObjectHash(light.object) == tileObjectHash(tileObject)))
			{
				continue;
			}

			WorldPoint worldLocation = tileObject.getWorldLocation();
			SceneLight light = new SceneLight(
				worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane(), l.height, l.alignment, l.radius,
				l.strength, l.color, l.type, l.duration, l.range, 0);
			LocalPoint localLocation = tileObject.getLocalLocation();
			light.x = localLocation.getX();
			light.y = localLocation.getY();

			int lightX = tileObject.getX();
			int lightY = tileObject.getY();
			int localSizeX = sizeX * Perspective.LOCAL_TILE_SIZE;
			int localSizeY = sizeY * Perspective.LOCAL_TILE_SIZE;

			if (orientation != -1 && light.alignment != Alignment.CENTER)
			{
				float radius = localSizeX / 2f;
				if (!light.alignment.radial)
				{
					radius = (float) Math.sqrt(localSizeX * localSizeX + localSizeX * localSizeX) / 2;
				}

				if (!light.alignment.relative)
				{
					orientation = 0;
				}
				orientation += light.alignment.orientation;
				orientation %= 2048;

				float sine = Perspective.SINE[orientation] / 65536f;
				float cosine = Perspective.COSINE[orientation] / 65536f;
				cosine /= (float) localSizeX / (float) localSizeY;

				int offsetX = (int) (radius * sine);
				int offsetY = (int) (radius * cosine);

				lightX += offsetX;
				lightY += offsetY;
			}

			float tileX = (float) lightX / Perspective.LOCAL_TILE_SIZE;
			float tileY = (float) lightY / Perspective.LOCAL_TILE_SIZE;
			float lerpX = (lightX % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
			float lerpY = (lightY % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
			int tileMinX = (int) Math.floor(tileX);
			int tileMinY = (int) Math.floor(tileY);
			int tileMaxX = tileMinX + 1;
			int tileMaxY = tileMinY + 1;
			tileMinX = Ints.constrainToRange(tileMinX, 0, Constants.SCENE_SIZE - 1);
			tileMinY = Ints.constrainToRange(tileMinY, 0, Constants.SCENE_SIZE - 1);
			tileMaxX = Ints.constrainToRange(tileMaxX, 0, Constants.SCENE_SIZE - 1);
			tileMaxY = Ints.constrainToRange(tileMaxY, 0, Constants.SCENE_SIZE - 1);

			float heightNorth = HDUtils.lerp(
				client.getTileHeights()[plane][tileMinX][tileMaxY],
				client.getTileHeights()[plane][tileMaxX][tileMaxY],
				lerpX);
			float heightSouth = HDUtils.lerp(
				client.getTileHeights()[plane][tileMinX][tileMinY],
				client.getTileHeights()[plane][tileMaxX][tileMinY],
				lerpX);
			float tileHeight = HDUtils.lerp(heightSouth, heightNorth, lerpY);

			light.x = lightX;
			light.y = lightY;
			light.z = (int) tileHeight - light.height - 1;
			light.object = tileObject;

			sceneLights.add(light);
		}
	}

	public void removeObjectLight(TileObject tileObject)
	{
		for (Light l : OBJECT_LIGHTS.get(tileObject.getId()))
		{
			LocalPoint localLocation = tileObject.getLocalLocation();
			int plane = tileObject.getWorldLocation().getPlane();

			sceneLights.removeIf(light ->
				light.x == localLocation.getX() &&
					light.y == localLocation.getY() &&
					light.plane == plane);
		}
	}

	int tileObjectHash(TileObject tileObject)
	{
		return tileObject.getWorldLocation().getX() * tileObject.getWorldLocation().getY() * (tileObject.getPlane() + 1) + tileObject.getId();
	}

	void calculateScenePosition(SceneLight light)
	{
		light.x = ((light.worldX - sceneMinX) * Perspective.LOCAL_TILE_SIZE) + Perspective.LOCAL_HALF_TILE_SIZE;
		light.y = ((light.worldY - sceneMinY) * Perspective.LOCAL_TILE_SIZE) + Perspective.LOCAL_HALF_TILE_SIZE;
		light.z = client.getTileHeights()[light.plane][light.worldX - sceneMinX][light.worldY - sceneMinY] - light.height - 1;
		if (light.alignment == Alignment.NORTH || light.alignment == Alignment.NORTHEAST || light.alignment == Alignment.NORTHWEST)
		{
			light.y += Perspective.LOCAL_HALF_TILE_SIZE;
		}
		if (light.alignment == Alignment.EAST || light.alignment == Alignment.NORTHEAST || light.alignment == Alignment.SOUTHEAST)
		{
			light.x += Perspective.LOCAL_HALF_TILE_SIZE;
		}
		if (light.alignment == Alignment.SOUTH || light.alignment == Alignment.SOUTHEAST || light.alignment == Alignment.SOUTHWEST)
		{
			light.y -= Perspective.LOCAL_HALF_TILE_SIZE;
		}
		if (light.alignment == Alignment.WEST || light.alignment == Alignment.NORTHWEST || light.alignment == Alignment.SOUTHWEST)
		{
			light.x -= Perspective.LOCAL_HALF_TILE_SIZE;
		}
	}
}
