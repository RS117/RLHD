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
package rs117.hd.lighting;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import net.runelite.api.events.NpcDespawned;
import rs117.hd.HdPlugin;
import rs117.hd.HdPluginConfig;
import rs117.hd.HDUtils;

@Singleton
@Slf4j
public class LightManager
{
	@Inject
	private HdPluginConfig config;

	@Inject
	private Client client;

	@Inject
	private HdPlugin hdPlugin;

	ArrayList<Light> allLights = new ArrayList<>();
	ArrayList<Light> sceneLights = new ArrayList<>();
	ArrayList<Projectile> sceneProjectiles = new ArrayList<>();

	long lastFrameTime = -1;

	int sceneMinX = 0;
	int sceneMinY = 0;
	int sceneMaxX = 0;
	int sceneMaxY = 0;

	public int visibleLightsCount = 0;

	enum LightType
	{
		STATIC, FLICKER, PULSE
	}

	enum Alignment
	{
		CENTER(0, false, false),

		NORTH(0, true, false),
		NORTHEAST(256, true, false),
		NORTHEAST_CORNER(256, false, false),
		EAST(512, true, false),
		SOUTHEAST(768, true, false),
		SOUTHEAST_CORNER(768, false, false),
		SOUTH(1024, true, false),
		SOUTHWEST(1280, true, false),
		SOUTHWEST_CORNER(1280, false, false),
		WEST(1536, true, false),
		NORTHWEST(1792, true, false),
		NORTHWEST_CORNER(1792, false, false),

		BACK(0, true, true),
		BACKLEFT(256, true, true),
		BACKLEFT_CORNER(256, false, true),
		LEFT(512, true, true),
		FRONTLEFT(768, true, true),
		FRONTLEFT_CORNER(768, false, true),
		FRONT(1024, true, true),
		FRONTRIGHT(1280, true, true),
		FRONTRIGHT_CORNER(1280, false, true),
		RIGHT(1536, true, true),
		BACKRIGHT(1792, true, true),
		BACKRIGHT_CORNER(1792, false, true);

		public final int orientation;
		public final boolean radial;
		public final boolean relative;

		Alignment(int orientation, boolean radial, boolean relative)
		{
			this.orientation = orientation;
			this.radial = radial;
			this.relative = relative;
		}
	}

	public static class Light
	{
		public int worldX;
		public int worldY;
		public int plane;
		public int height;
		public Alignment alignment;
		public int size;
		public float strength;
		public int[] color;
		public LightType type;
		public float duration;
		public float range;
		public int fadeInDuration = 0;

		public int currentSize;
		public float currentStrength;
		public int[] currentColor;
		public float currentAnimation = 0.5f;
		public int currentFadeIn = 0;
		public boolean visible = true;

		public int x;
		public int y;
		public int z;
		public int distance = 0;
		public boolean belowFloor = false;
		public boolean aboveFloor = false;

		public Projectile projectile = null;
		public NPC npc = null;
		public TileObject object = null;

		public Light(int worldX, int worldY, int plane, int height, Alignment alignment, int size, float strength, int[] color, LightType type, float duration, float range, int fadeInDuration)
		{
			this.worldX = worldX;
			this.worldY = worldY;
			this.plane = plane;
			this.height = height;
			this.alignment = alignment;
			this.size = size;
			this.strength = strength;
			this.color = color;
			this.type = type;
			this.duration = duration;
			this.range = range;
			this.fadeInDuration = fadeInDuration;

			this.currentSize = size;
			this.currentStrength = strength;
			this.currentColor = color;

			if (type == LightType.PULSE)
			{
				this.currentAnimation = (float)Math.random();
			}
		}
	}

	public void update()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		int camX = hdPlugin.camTarget[0];
		int camY = hdPlugin.camTarget[1];
		int camZ = hdPlugin.camTarget[2];

		Iterator<Light> lightIterator = sceneLights.iterator();

		while (lightIterator.hasNext())
		{
			Light light = lightIterator.next();

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

				light.visible = hdPlugin.configProjectileLights;
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

					light.visible = light.npc.getModel() != null;

					if (!hdPlugin.configNpcLights)
					{
						light.visible = false;
					}
				}
				else
				{
					light.visible = false;
				}
			}

			if (light.type == LightType.FLICKER)
			{
				double change = Math.random() * 2 - 1.0f;
				int flickerRate = 1000; // 1800
				int sizeAdjustment = 15;
				float maxFlicker = 1f + (light.range / 100f);
				float minFlicker = 1f - (light.range / 100f);

				light.currentStrength += (light.strength / ((frameTime / 1000f) * flickerRate)) * change;
				light.currentStrength = Floats.constrainToRange(light.currentStrength, light.strength * minFlicker, light.strength * maxFlicker);

				light.currentSize += (light.size / sizeAdjustment) * change;
				light.currentSize = Ints.constrainToRange(light.currentSize, (int)(light.size * minFlicker), (int)(light.size * maxFlicker));
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

				light.currentSize = (int)(light.size * multiplier);
				light.currentStrength = light.strength * multiplier;
			}
			else
			{
				light.currentStrength = light.strength;
				light.currentSize = light.size;
				light.currentColor = light.color;
			}
			// Apply fade-in
			if (light.fadeInDuration > 0)
			{
				light.currentStrength *= Math.min((float)light.currentFadeIn / (float)light.fadeInDuration, 1.0f);

				light.currentFadeIn += frameTime;
			}

			// Calculate the distance between the player and the light to determine which
			// lights to display based on the 'max dynamic lights' config option
			light.distance = (int) Math.sqrt(Math.pow(camX - light.x, 2) + Math.pow(camY - light.y, 2) + Math.pow(camZ - light.z, 2));

			int tileX = (int)Math.floor(light.x / 128f);
			int tileY = (int)Math.floor(light.y / 128f);
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

		for (Light light : allLights)
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


	void updateSceneNpcs()
	{
		// check the NPCs in the scene to make sure they have lights assigned, if applicable,
		// for scenarios in which HD mode or dynamic lights were disabled during NPC spawn

		List<NPC> npcs = client.getNpcs();

		for (NPC npc : npcs)
		{
			int npcId = npc.getId();
			NpcLight npcLight = NpcLight.find(npcId);
			if (npcLight == null)
			{
				continue;
			}

			addNpcLight(npc);
		}
	}


	public ArrayList<Light> getVisibleLights(int maxDistance, int maxLights)
	{
		ArrayList<Light> visibleLights = new ArrayList<>();
		int lightsCount = 0;

		for (Light light : sceneLights)
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
		int id = projectile.getId();
		ProjectileLight projectileLight = ProjectileLight.find(id);
		if (projectileLight == null)
		{
			return;
		}

		if (sceneProjectiles.contains(projectile))
		{
			// prevent duplicate lights being spawned for the same projectile
			return;
		}

		int rgb = projectileLight.getRgb();
		int r = rgb >>> 16;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		Light light = new Light(0, 0, projectile.getFloor(),
			0, Alignment.CENTER, projectileLight.getSize(), projectileLight.getStrength(), new int[]{r, g, b}, projectileLight.getLightType(), projectileLight.getDuration(), projectileLight.getRange(), 300);
		light.projectile = projectile;
		light.x = (int) projectile.getX();
		light.y = (int) projectile.getY();
		light.z = (int) projectile.getZ();

		sceneProjectiles.add(projectile);
		sceneLights.add(light);
	}

	public void addNpcLight(NPC npc)
	{
		int id = npc.getId();
		NpcLight npcLight = NpcLight.find(id);
		if (npcLight == null)
		{
			return;
		}

		// prevent duplicate lights being spawned for the same NPC
		for (Light light : sceneLights)
		{
			if (light.npc == npc)
			{
				return;
			}
		}

		int rgb = npcLight.getRgb();
		int r = rgb >>> 16;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		Light light = new Light(0, 0, -1,
			npcLight.getHeight(), npcLight.getAlignment(), npcLight.getSize(), npcLight.getStrength(), new int[]{r, g, b}, npcLight.getLightType(), npcLight.getDuration(), npcLight.getRange(), 0);
		light.npc = npc;
		light.visible = false;

		sceneLights.add(light);
	}

	public void removeNpcLight(NpcDespawned npcDespawned)
	{
		sceneLights.removeIf(light -> light.npc == npcDespawned.getNpc());
	}

	public void addObjectLight(TileObject tileObject, int plane)
	{
		addObjectLight(tileObject, plane, 1, 1, -1);
	}

	public void addObjectLight(TileObject tileObject, int plane, int sizeX, int sizeY, int orientation)
	{
		int id = tileObject.getId();
		ObjectLight objectLight = ObjectLight.find(id);
		if (objectLight == null)
		{
			return;
		}

		if (sceneLights.stream().anyMatch(light -> light.object != null && tileObjectHash(light.object) == tileObjectHash(tileObject)))
		{
			// prevent duplicate lights being spawned for the same object
			return;
		}

		int rgb = objectLight.getRgb();
		int r = rgb >>> 16;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		WorldPoint worldLocation = tileObject.getWorldLocation();
		Light light = new Light(worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane(),
			objectLight.getHeight(), objectLight.getAlignment(), objectLight.getSize(), objectLight.getStrength(), new int[]{r, g, b}, objectLight.getLightType(), objectLight.getDuration(), objectLight.getRange(), 0);
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
				radius = (float)Math.sqrt(localSizeX * localSizeX + localSizeX * localSizeX) / 2;
			}

			if (!light.alignment.relative)
			{
				orientation = 0;
			}
			orientation += light.alignment.orientation;
			orientation %= 2048;

			float sine = Perspective.SINE[orientation] / 65536f;
			float cosine = Perspective.COSINE[orientation] / 65536f;
			cosine /= (float)localSizeX / (float)localSizeY;

			int offsetX = (int)(radius * sine);
			int offsetY = (int)(radius * cosine);

			lightX += offsetX;
			lightY += offsetY;
		}

		float tileX = (float)lightX / Perspective.LOCAL_TILE_SIZE;
		float tileY = (float)lightY / Perspective.LOCAL_TILE_SIZE;
		float lerpX = (lightX % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
		float lerpY = (lightY % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
		int tileMinX = (int)Math.floor(tileX);
		int tileMinY = (int)Math.floor(tileY);
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

	public void removeObjectLight(TileObject tileObject)
	{
		int id = tileObject.getId();
		ObjectLight objectLight = ObjectLight.find(id);
		if (objectLight == null)
		{
			return;
		}

		LocalPoint localLocation = tileObject.getLocalLocation();
		int plane = tileObject.getWorldLocation().getPlane();

		sceneLights.removeIf(light -> light.x == localLocation.getX() && light.y == localLocation.getY() && light.plane == plane);
	}

	int tileObjectHash(TileObject tileObject)
	{
		return tileObject.getWorldLocation().getX() * tileObject.getWorldLocation().getY() * (tileObject.getPlane() + 1) + tileObject.getId();
	}

	void calculateScenePosition(Light light)
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

	private static final Pattern PATTERN = Pattern.compile("^[ \\t]*(?<expr>" +
		"//.*$|" + // //comment
		"/\\*.*$|" + // /* start comment block
		"\\*/.*$|" + //    end comment block */
		"Reset|" + // sets all variables to defaults
		"(?<x>[0-9-]+)(,)[ \\t]*(?<y>[0-9-]+)((,)[ \\t]*(?<alignment>[A-Za-z]+))?|" + // 3124, 2843
		"#([ \\t]*(?<color>[0-9a-fA-F]{6}|[0-9a-fA-F]{3}))|" + // #<RRGGBB> or #<RGB> (hex color)
		"Color[ \\t]*(?<r>[0-9-]+)(,)[ \\t]*(?<g>[0-9-]+)(,)[ \\t]*(?<b>[0-9-]+)|" + // C 255, 128, 0 (RGB color)
		"Strength[ \\t]*(?<strength>[0-9-]+)|" + // S 100 (strength)
		"Radius[ \\t]*(?<radius>[0-9-]+)|" + // R 500 (radius)
		"Range[ \\t]*(?<range>[0-9-]+)|" + // R 500 (radius)
		"Duration[ \\t]*(?<duration>[0-9-]+)|" + // R 500 (radius)
		"Plane[ \\t]*(?<plane>[0-9-]+)|" + // P 0 (plane)
		"Height[ \\t]*(?<h>[0-9-]+)|" + // H 128 (height)
		"Type[ \\t]*(?<type>[a-z]+)|" + // T flicker (type)
		")[ \\t]*");

	public void loadLightsFromFile() throws IOException
	{
		// create arraylist of lights from text file
		allLights = new ArrayList<>();

		String filename = "lights.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
		boolean commentBlock = false;

		int[] defaultColor = new int[]{255, 255, 255};
		int defaultRadius = 500;
		float defaultStrength = 1.0f;
		float defaultRange =  0.2f;
		int defaultDuration = 1000;
		int defaultHeight = 0;
		int defaultPlane = 0;
		LightType defaultType = LightType.STATIC;

		int[] color = defaultColor;
		int radius = defaultRadius;
		float strength = defaultStrength;
		float range =  defaultRange;
		int duration = defaultDuration;
		int height = defaultHeight;
		int plane = defaultPlane;
		LightType type = defaultType;

		int lineNo = 1;
		try
		{
			Matcher m = PATTERN.matcher("");
			String line;
			while ((line = br.readLine()) != null)
			{
				m.reset(line);
				int end = 0;
				while (end < line.length())
				{
					m.region(end, line.length());
					if (!m.find())
					{
						throw new IllegalArgumentException("Unexpected: \"" + line.substring(end) + "\" (" + filename + ":" + lineNo + ")");
					}
					end = m.end();

					String expr = m.group("expr");
					if (expr == null || expr.length() <= 0 || expr.startsWith("//"))
					{
						continue;
					}

					if (expr.startsWith("/*")) {
						commentBlock = true;
						continue;
					} else if (expr.startsWith("*/")) {
						commentBlock = false;
						continue;
					}

					if (commentBlock) {
						continue;
					}

					if (expr.toLowerCase().startsWith("reset"))
					{
						color = defaultColor;
						radius = defaultRadius;
						range = defaultRange;
						duration = defaultDuration;
						strength = defaultStrength;
						height = defaultHeight;
						plane = defaultPlane;
						type = defaultType;
						continue;
					}

					char cha = expr.toLowerCase().charAt(0);
					switch (cha)
					{
						case '#':
							String sColor = m.group("color");
							Color RGB = Color.decode("#" + sColor);
							float[] RGBTmp = new float[3];
							RGB.getRGBColorComponents(RGBTmp);
							color = new int[]{(int)(RGBTmp[0] * 255f), (int)(RGBTmp[1] * 255f), (int)(RGBTmp[2] * 255f)};
							break;
						case 'c':
							int r = Integer.parseInt(m.group("r"));
							int g = Integer.parseInt(m.group("g"));
							int b = Integer.parseInt(m.group("b"));
							color = new int[]{r, g, b};
							break;
						case 's':
							strength = Integer.parseInt(m.group("strength")) / 100f;
							break;
						case 'r':
							if (expr.toLowerCase().startsWith("radius")) {
								radius = Integer.parseInt(m.group("radius"));
								break;
							} else if (expr.toLowerCase().startsWith("range")) {
								range = Integer.parseInt(m.group("range"));
								break;
							}
						case 'd':
							duration = Integer.parseInt(m.group("duration"));
							break;
						case 'p':
							plane = Integer.parseInt(m.group("plane"));
							break;
						case 'h':
							height = Integer.parseInt(m.group("h"));
							break;
						case 't':
							String typeStr = m.group("type").toLowerCase().trim();
							switch (typeStr) {
								case "flicker":
									type = LightType.FLICKER;
									break;
								case "pulse":
									type = LightType.PULSE;
									break;
								default:
									type = LightType.STATIC;
									break;
							}
							break;
						default:
							int x = Integer.parseInt(m.group("x"));
							int y = Integer.parseInt(m.group("y"));
							Alignment alignment = Alignment.CENTER;
							if (m.group("alignment") != null) {
								switch (m.group("alignment").toLowerCase().trim()) {
									case "n":
										alignment = Alignment.NORTH;
										break;
									case "ne":
										alignment = Alignment.NORTHEAST;
										break;
									case "e":
										alignment = Alignment.EAST;
										break;
									case "se":
										alignment = Alignment.SOUTHEAST;
										break;
									case "s":
										alignment = Alignment.SOUTH;
										break;
									case "sw":
										alignment = Alignment.SOUTHWEST;
										break;
									case "w":
										alignment = Alignment.WEST;
										break;
									case "nw":
										alignment = Alignment.NORTHWEST;
										break;
									default:
										alignment = Alignment.CENTER;
										break;
								}
							}
							allLights.add(new Light(x, y, plane, height, alignment, radius, strength, color, type, duration, range, 0));
							break;
					}
				}
			}
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException("Expected number (" + filename + ":" + lineNo + ")", ex);
		}

		br.close();
		log.debug("loaded {} lights from file", allLights.size());
	}
}
