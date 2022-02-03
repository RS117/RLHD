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

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;
import com.jogamp.opengl.math.VectorUtil;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import rs117.hd.config.WaterEffects;
import rs117.hd.materials.GroundMaterial;
import rs117.hd.materials.ObjectProperties;
import rs117.hd.materials.Overlay;
import rs117.hd.materials.Material;
import rs117.hd.materials.TzHaarRecolorType;
import rs117.hd.materials.Underlay;
import rs117.hd.materials.UvType;

@Singleton
@Slf4j
class SceneUploader
{
	@Inject
	private Client client;

	@Inject
	private HdPlugin hdPlugin;

	@Inject
	ProceduralGenerator proceduralGenerator;

	@Inject
	private ModelPusher modelPusher;

	int sceneId = (int) (System.currentTimeMillis() / 1000L);
	private int offset;
	private int uvoffset;

	void upload(Scene scene, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer)
	{
		Stopwatch stopwatch = Stopwatch.createStarted();

		++sceneId;
		offset = 0;
		uvoffset = 0;
		vertexBuffer.clear();
		uvBuffer.clear();
		normalBuffer.clear();

		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				for (int y = 0; y < Constants.SCENE_SIZE; ++y)
				{
					Tile tile = scene.getTiles()[z][x][y];
					if (tile != null)
					{
						upload(tile, vertexBuffer, uvBuffer, normalBuffer);
					}
				}
			}
		}

		stopwatch.stop();
		log.debug("Scene upload time: {}", stopwatch);
	}

	private void uploadModel(Model model, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int tileZ, int tileX, int tileY, ObjectProperties objectProperties, ObjectType objectType)
	{
		if (model.getSceneId() == sceneId)
		{
			return; // model has already been uploaded
		}

		byte skipObject = 0b00;

		if (objectType == ObjectType.GROUND_OBJECT || objectType == ObjectType.DECORATIVE_OBJECT)
		{
			// mark it as low priority
			skipObject = 0b01;

			if (client.getBaseX() + tileX == 2558 && client.getBaseY() + tileY >= 3249 && client.getBaseY() + tileY <= 3252)
			{
				// fix for water by khazard spirit tree
				// marks object to never be drawn
				skipObject = 0b11;
			}
		}
		// pack a bit into bufferoffset that we can use later to hide
		// some low-importance objects based on Level of Detail setting
		model.setBufferOffset(offset << 2 | skipObject);
		if (model.getFaceTextures() != null || (objectProperties != null && objectProperties.getMaterial() != Material.NONE))
		{
			model.setUvBufferOffset(uvoffset);
		}
		else
		{
			model.setUvBufferOffset(-1);
		}
		model.setSceneId(sceneId);

		final int[] lengths = modelPusher.pushModel(model, vertexBuffer, uvBuffer, normalBuffer, tileX, tileY, tileZ, objectProperties, objectType);

		offset += lengths[0];
		uvoffset += lengths[1];
	}

	private void upload(Tile tile, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer)
	{
		Tile bridge = tile.getBridge();
		if (bridge != null)
		{
			upload(bridge, vertexBuffer, uvBuffer, normalBuffer);
		}

		final Point tilePoint = tile.getSceneLocation();
		final int tileX = tilePoint.getX();
		final int tileY = tilePoint.getY();
		final int tileZ = tile.getRenderLevel();

		SceneTilePaint sceneTilePaint = tile.getSceneTilePaint();
		if (sceneTilePaint != null)
		{
			int[] uploadedTilePaintData = upload(
				tile, sceneTilePaint,
				tileZ, tileX, tileY,
				vertexBuffer, uvBuffer, normalBuffer,
				0, 0);

			final int bufferLength = uploadedTilePaintData[0];
			final int uvBufferLength = uploadedTilePaintData[1];
			final int underwaterTerrain = uploadedTilePaintData[2];
			// pack a boolean into the buffer length of tiles so we can tell
			// which tiles have procedurally generated underwater terrain.
			// shift the bufferLength to make space for the boolean:
			int packedBufferLength = bufferLength << 1 | underwaterTerrain;
			sceneTilePaint.setBufferOffset(offset);
			sceneTilePaint.setUvBufferOffset(uvBufferLength > 0 ? uvoffset : -1);
			sceneTilePaint.setBufferLen(packedBufferLength);
			offset += bufferLength;
			uvoffset += uvBufferLength;
		}

		SceneTileModel sceneTileModel = tile.getSceneTileModel();
		if (sceneTileModel != null)
		{
			int[] uploadedTileModelData = upload(
				tile, sceneTileModel,
				tileZ, tileX, tileY,
				vertexBuffer, uvBuffer, normalBuffer,
				0, 0);

			final int bufferLength = uploadedTileModelData[0];
			final int uvBufferLength = uploadedTileModelData[1];
			final int underwaterTerrain = uploadedTileModelData[2];
			// pack a boolean into the buffer length of tiles so we can tell
			// which tiles have procedurally-generated underwater terrain
			int packedBufferLength = bufferLength << 1 | underwaterTerrain;
			sceneTileModel.setBufferOffset(offset);
			sceneTileModel.setUvBufferOffset(uvBufferLength > 0 ? uvoffset : -1);
			sceneTileModel.setBufferLen(packedBufferLength);
			offset += bufferLength;
			uvoffset += uvBufferLength;
		}

		ObjectProperties objectProperties;

		WallObject wallObject = tile.getWallObject();
		if (wallObject != null)
		{
			objectProperties = ObjectProperties.getObjectProperties(tile.getWallObject().getId());

			Renderable renderable1 = wallObject.getRenderable1();
			if (renderable1 instanceof Model)
			{
				Model model = (Model) renderable1;
				uploadModel(model, vertexBuffer, uvBuffer, normalBuffer, tileZ, tileX, tileY, objectProperties, ObjectType.WALL_OBJECT);
			}

			Renderable renderable2 = wallObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				Model model = (Model) renderable2;
				uploadModel(model, vertexBuffer, uvBuffer, normalBuffer, tileZ, tileX, tileY, objectProperties, ObjectType.WALL_OBJECT);
			}
		}

		GroundObject groundObject = tile.getGroundObject();
		if (groundObject != null)
		{
			objectProperties = ObjectProperties.getObjectProperties(tile.getGroundObject().getId());

			Renderable renderable = groundObject.getRenderable();
			if (renderable instanceof Model)
			{
				Model model = (Model) renderable;
				uploadModel(model, vertexBuffer, uvBuffer, normalBuffer, tileZ, tileX, tileY, objectProperties, ObjectType.GROUND_OBJECT);
			}
		}

		DecorativeObject decorativeObject = tile.getDecorativeObject();
		if (decorativeObject != null)
		{
			objectProperties = ObjectProperties.getObjectProperties(tile.getDecorativeObject().getId());

			Renderable renderable = decorativeObject.getRenderable();
			if (renderable instanceof Model)
			{
				Model model = (Model) renderable;
				uploadModel(model, vertexBuffer, uvBuffer, normalBuffer, tileZ, tileX, tileY, objectProperties, ObjectType.DECORATIVE_OBJECT);
			}

			Renderable renderable2 = decorativeObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				Model model = (Model) renderable2;
				uploadModel(model, vertexBuffer, uvBuffer, normalBuffer, tileZ, tileX, tileY, objectProperties, ObjectType.DECORATIVE_OBJECT);
			}
		}

		GameObject[] gameObjects = tile.getGameObjects();
		for (GameObject gameObject : gameObjects)
		{
			if (gameObject == null)
			{
				continue;
			}

			objectProperties = ObjectProperties.getObjectProperties(gameObject.getId());

			Renderable renderable = gameObject.getRenderable();
			if (renderable instanceof Model)
			{
				Model model = (Model) gameObject.getRenderable();
				uploadModel(model, vertexBuffer, uvBuffer, normalBuffer, tileZ, tileX, tileY, objectProperties, ObjectType.GAME_OBJECT);
			}
		}
	}

	int[] upload(Tile tile, SceneTilePaint sceneTilePaint, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		int[] bufferLengths;

		bufferLengths = uploadHDTilePaintSurface(tile, sceneTilePaint, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, 0, 0);
		bufferLength += bufferLengths[0];
		uvBufferLength += bufferLengths[1];
		underwaterTerrain += bufferLengths[2];

		bufferLengths = uploadHDTilePaintUnderwater(tile, sceneTilePaint, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, 0, 0);
		bufferLength += bufferLengths[0];
		uvBufferLength += bufferLengths[1];
		underwaterTerrain += bufferLengths[2];

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int[] uploadHDTilePaintSurface(Tile tile, SceneTilePaint sceneTilePaint, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		boolean ignoreTile = false;

		if (sceneTilePaint.getNeColor() == 12345678)
		{
			// ignore certain tiles that aren't supposed to be visible but
			// we can still make a height-adjusted version of it for underwater
			ignoreTile = true;
		}

		final int localX = offsetX;
		final int localY = offsetY;

		int baseX = client.getBaseX();
		int baseY = client.getBaseY();

		final int[][][] tileHeights = client.getTileHeights();
		int swHeight = tileHeights[tileZ][tileX][tileY];
		int seHeight = tileHeights[tileZ][tileX + 1][tileY];
		int neHeight = tileHeights[tileZ][tileX + 1][tileY + 1];
		int nwHeight = tileHeights[tileZ][tileX][tileY + 1];

		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		int localSwVertexX = localX;
		int localSwVertexY = localY;
		int localSeVertexX = localX + Perspective.LOCAL_TILE_SIZE;
		int localSeVertexY = localY;
		int localNwVertexX = localX;
		int localNwVertexY = localY + Perspective.LOCAL_TILE_SIZE;
		int localNeVertexX = localX + Perspective.LOCAL_TILE_SIZE;
		int localNeVertexY = localY + Perspective.LOCAL_TILE_SIZE;

		int[] vertexKeys = proceduralGenerator.tileVertexKeys(tile);
		int swVertexKey = vertexKeys[0];
		int seVertexKey = vertexKeys[1];
		int nwVertexKey = vertexKeys[2];
		int neVertexKey = vertexKeys[3];

		if (!ignoreTile)
		{
			int swColor = sceneTilePaint.getSwColor();
			int seColor = sceneTilePaint.getSeColor();
			int neColor = sceneTilePaint.getNeColor();
			int nwColor = sceneTilePaint.getNwColor();

			int tileTexture = sceneTilePaint.getTexture();

			boolean neVertexIsOverlay = false;
			boolean nwVertexIsOverlay = false;
			boolean seVertexIsOverlay = false;
			boolean swVertexIsOverlay = false;

			Material swMaterial = Material.getTexture(tileTexture);
			Material seMaterial = Material.getTexture(tileTexture);
			Material neMaterial = Material.getTexture(tileTexture);
			Material nwMaterial = Material.getTexture(tileTexture);

			WaterType waterType = proceduralGenerator.tileWaterType(tile, sceneTilePaint);

			if (waterType != WaterType.NONE)
			{
				swMaterial = seMaterial = neMaterial = nwMaterial = waterType.getGroundMaterial().getMaterials()[0];

				// set colors for the shoreline to create a foam effect in the water shader

				swColor = seColor = nwColor = neColor = 127;

				if (proceduralGenerator.vertexIsWater.containsKey(swVertexKey) && proceduralGenerator.vertexIsLand.containsKey(swVertexKey))
				{
					swColor = 0;
				}
				if (proceduralGenerator.vertexIsWater.containsKey(seVertexKey) && proceduralGenerator.vertexIsLand.containsKey(seVertexKey))
				{
					seColor = 0;
				}
				if (proceduralGenerator.vertexIsWater.containsKey(nwVertexKey) && proceduralGenerator.vertexIsLand.containsKey(nwVertexKey))
				{
					nwColor = 0;
				}
				if (proceduralGenerator.vertexIsWater.containsKey(neVertexKey) && proceduralGenerator.vertexIsLand.containsKey(neVertexKey))
				{
					neColor = 0;
				}
			}
			else if (hdPlugin.configGroundBlending && !proceduralGenerator.useDefaultColor(tile) && sceneTilePaint.getTexture() == -1)
			{
				// get the vertices' colors and textures from hashmaps

				swColor = proceduralGenerator.vertexTerrainColor.getOrDefault(swVertexKey, swColor);
				seColor = proceduralGenerator.vertexTerrainColor.getOrDefault(seVertexKey, seColor);
				neColor = proceduralGenerator.vertexTerrainColor.getOrDefault(neVertexKey, neColor);
				nwColor = proceduralGenerator.vertexTerrainColor.getOrDefault(nwVertexKey, nwColor);

				if (hdPlugin.configGroundTextures)
				{
					swMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(swVertexKey, swMaterial);
					seMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(seVertexKey, seMaterial);
					neMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(neVertexKey, neMaterial);
					nwMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(nwVertexKey, nwMaterial);
				}
			}
			else if (hdPlugin.configGroundTextures)
			{
				GroundMaterial groundMaterial;

				if (client.getScene().getOverlayIds()[tileZ][tileX][tileY] != 0)
				{
					Overlay overlay = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client);
					overlay = proceduralGenerator.getSeasonalOverlay(overlay);
					groundMaterial = overlay.getGroundMaterial();

					swColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(swColor)));
					seColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(seColor)));
					nwColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(nwColor)));
					neColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(neColor)));
				}
				else
				{
					Underlay underlay = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client);
					underlay = proceduralGenerator.getSeasonalUnderlay(underlay);
					groundMaterial = underlay.getGroundMaterial();

					swColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(swColor)));
					seColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(seColor)));
					nwColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(nwColor)));
					neColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(neColor)));
				}

				swMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY);
				seMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY);
				nwMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY + 1);
				neMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY + 1);
			}
			else if (hdPlugin.configWinterTheme)
			{
				if (client.getScene().getOverlayIds()[tileZ][tileX][tileY] != 0)
				{
					Overlay overlay = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client);
					overlay = proceduralGenerator.getSeasonalOverlay(overlay);

					swColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(swColor)));
					seColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(seColor)));
					nwColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(nwColor)));
					neColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(neColor)));
				}
				else
				{
					Underlay underlay = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client);
					underlay = proceduralGenerator.getSeasonalUnderlay(underlay);

					swColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(swColor)));
					seColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(seColor)));
					nwColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(nwColor)));
					neColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(neColor)));
				}
			}

			if (proceduralGenerator.vertexIsOverlay.containsKey(neVertexKey) && proceduralGenerator.vertexIsUnderlay.containsKey(neVertexKey))
			{
				neVertexIsOverlay = true;
			}
			if (proceduralGenerator.vertexIsOverlay.containsKey(nwVertexKey) && proceduralGenerator.vertexIsUnderlay.containsKey(nwVertexKey))
			{
				nwVertexIsOverlay = true;
			}
			if (proceduralGenerator.vertexIsOverlay.containsKey(seVertexKey) && proceduralGenerator.vertexIsUnderlay.containsKey(seVertexKey))
			{
				seVertexIsOverlay = true;
			}
			if (proceduralGenerator.vertexIsOverlay.containsKey(swVertexKey) && proceduralGenerator.vertexIsUnderlay.containsKey(swVertexKey))
			{
				swVertexIsOverlay = true;
			}

			float[] swNormals = new float[]{0,-1,0};
			float[] seNormals = new float[]{0,-1,0};
			float[] neNormals = new float[]{0,-1,0};
			float[] nwNormals = new float[]{0,-1,0};

			// retrieve normals from hashmap

			if (waterType == WaterType.NONE)
			{
				swNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(swVertexKey, swNormals);
				seNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(seVertexKey, seNormals);
				neNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(neVertexKey, neNormals);
				nwNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(nwVertexKey, nwNormals);
			}

			int swTerrainData = packTerrainData(0, WaterType.NONE, tileZ);
			int seTerrainData = packTerrainData(0, WaterType.NONE, tileZ);
			int nwTerrainData = packTerrainData(0, WaterType.NONE, tileZ);
			int neTerrainData = packTerrainData(0, WaterType.NONE, tileZ);

			normalBuffer.ensureCapacity(24);
			normalBuffer.put(neNormals[0], neNormals[2], neNormals[1], neTerrainData);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], nwTerrainData);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], seTerrainData);

			normalBuffer.put(swNormals[0], swNormals[2], swNormals[1], swTerrainData);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], seTerrainData);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], nwTerrainData);

			vertexBuffer.ensureCapacity(24);
			vertexBuffer.put(localNeVertexX, neHeight, localNeVertexY, neColor);
			vertexBuffer.put(localNwVertexX, nwHeight, localNwVertexY, nwColor);
			vertexBuffer.put(localSeVertexX, seHeight, localSeVertexY, seColor);

			vertexBuffer.put(localSwVertexX, swHeight, localSwVertexY, swColor);
			vertexBuffer.put(localSeVertexX, seHeight, localSeVertexY, seColor);
			vertexBuffer.put(localNwVertexX, nwHeight, localNwVertexY, nwColor);

			bufferLength += 6;

			int packedMaterialDataSW = modelPusher.packMaterialData(Material.getIndex(swMaterial), swVertexIsOverlay);
			int packedMaterialDataSE = modelPusher.packMaterialData(Material.getIndex(seMaterial), seVertexIsOverlay);
			int packedMaterialDataNW = modelPusher.packMaterialData(Material.getIndex(nwMaterial), nwVertexIsOverlay);
			int packedMaterialDataNE = modelPusher.packMaterialData(Material.getIndex(neMaterial), neVertexIsOverlay);

			uvBuffer.ensureCapacity(24);
			uvBuffer.put(packedMaterialDataNE, 1.0f, 1.0f, 0f);
			uvBuffer.put(packedMaterialDataNW, 0.0f, 1.0f, 0f);
			uvBuffer.put(packedMaterialDataSE, 1.0f, 0.0f, 0f);

			uvBuffer.put(packedMaterialDataSW, 0.0f, 0.0f, 0f);
			uvBuffer.put(packedMaterialDataSE, 1.0f, 0.0f, 0f);
			uvBuffer.put(packedMaterialDataNW, 0.0f, 1.0f, 0f);

			uvBufferLength += 6;
		}

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int[] uploadHDTilePaintUnderwater(Tile tile, SceneTilePaint sceneTilePaint, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{

		int baseX = client.getBaseX();
		int baseY = client.getBaseY();

		if (baseX + offsetX >= 2816 && baseX + offsetX <= 2970 && baseY + offsetY <= 5375 && baseY + offsetY >= 5220)
		{
			// fix for God Wars Dungeon's water rendering over zamorak bridge
			return new int[]{0, 0, 0};
		}

		final int[][][] tileHeights = client.getTileHeights();
		int swHeight = tileHeights[tileZ][tileX][tileY];
		int seHeight = tileHeights[tileZ][tileX + 1][tileY];
		int neHeight = tileHeights[tileZ][tileX + 1][tileY + 1];
		int nwHeight = tileHeights[tileZ][tileX][tileY + 1];

		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		int localSwVertexX = offsetX;
		int localSwVertexY = offsetY;
		int localSeVertexX = offsetX + Perspective.LOCAL_TILE_SIZE;
		int localSeVertexY = offsetY;
		int localNwVertexX = offsetX;
		int localNwVertexY = offsetY + Perspective.LOCAL_TILE_SIZE;
		int localNeVertexX = offsetX + Perspective.LOCAL_TILE_SIZE;
		int localNeVertexY = offsetY + Perspective.LOCAL_TILE_SIZE;

		int[] vertexKeys = proceduralGenerator.tileVertexKeys(tile);
		int swVertexKey = vertexKeys[0];
		int seVertexKey = vertexKeys[1];
		int nwVertexKey = vertexKeys[2];
		int neVertexKey = vertexKeys[3];

		if (hdPlugin.configWaterEffects == WaterEffects.ALL && proceduralGenerator.tileIsWater[tileZ][tileX][tileY])
		{
			// underwater terrain

			underwaterTerrain = 1;

			int swColor = 6676;
			int seColor = 6676;
			int neColor = 6676;
			int nwColor = 6676;

			int swDepth = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(swVertexKey, 0);
			int seDepth = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(seVertexKey, 0);
			int nwDepth = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(nwVertexKey, 0);
			int neDepth = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(neVertexKey, 0);

			float[] swNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(swVertexKey, new float[]{0,-1,0});
			float[] seNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(seVertexKey, new float[]{0,-1,0});
			float[] nwNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(nwVertexKey, new float[]{0,-1,0});
			float[] neNormals = proceduralGenerator.vertexTerrainNormals.getOrDefault(neVertexKey, new float[]{0,-1,0});

			Material swMaterial = Material.NONE;
			Material seMaterial = Material.NONE;
			Material nwMaterial = Material.NONE;
			Material neMaterial = Material.NONE;

			if (hdPlugin.configGroundTextures)
			{
				GroundMaterial groundMaterial = GroundMaterial.UNDERWATER_GENERIC;

				swMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY);
				seMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY);
				nwMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY + 1);
				neMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY + 1);
			}

			WaterType waterType = proceduralGenerator.tileWaterType(tile, sceneTilePaint);

			int swTerrainData = packTerrainData(swDepth, waterType, tileZ);
			int seTerrainData = packTerrainData(seDepth, waterType, tileZ);
			int nwTerrainData = packTerrainData(nwDepth, waterType, tileZ);
			int neTerrainData = packTerrainData(neDepth, waterType, tileZ);

			normalBuffer.ensureCapacity(24);
			normalBuffer.put(neNormals[0], neNormals[2], neNormals[1], neTerrainData);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], nwTerrainData);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], seTerrainData);

			normalBuffer.put(swNormals[0], swNormals[2], swNormals[1], swTerrainData);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], seTerrainData);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], nwTerrainData);

			vertexBuffer.ensureCapacity(24);
			vertexBuffer.put(localNeVertexX, neHeight + neDepth, localNeVertexY, neColor);
			vertexBuffer.put(localNwVertexX, nwHeight + nwDepth, localNwVertexY, nwColor);
			vertexBuffer.put(localSeVertexX, seHeight + seDepth, localSeVertexY, seColor);

			vertexBuffer.put(localSwVertexX, swHeight + swDepth, localSwVertexY, swColor);
			vertexBuffer.put(localSeVertexX, seHeight + seDepth, localSeVertexY, seColor);
			vertexBuffer.put(localNwVertexX, nwHeight + nwDepth, localNwVertexY, nwColor);

			bufferLength += 6;

			int packedMaterialDataSW = modelPusher.packMaterialData(Material.getIndex(swMaterial), false);
			int packedMaterialDataSE = modelPusher.packMaterialData(Material.getIndex(seMaterial), false);
			int packedMaterialDataNW = modelPusher.packMaterialData(Material.getIndex(nwMaterial), false);
			int packedMaterialDataNE = modelPusher.packMaterialData(Material.getIndex(neMaterial), false);

			uvBuffer.ensureCapacity(24);
			uvBuffer.put(packedMaterialDataNE, 1.0f, 1.0f, 0f);
			uvBuffer.put(packedMaterialDataNW, 0.0f, 1.0f, 0f);
			uvBuffer.put(packedMaterialDataSE, 1.0f, 0.0f, 0f);

			uvBuffer.put(packedMaterialDataSW, 0.0f, 0.0f, 0f);
			uvBuffer.put(packedMaterialDataSE, 1.0f, 0.0f, 0f);
			uvBuffer.put(packedMaterialDataNW, 0.0f, 1.0f, 0f);

			uvBufferLength += 6;
		}

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int[] upload(Tile tile, SceneTileModel sceneTileModel, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		int[] bufferLengths;

		bufferLengths = uploadHDTileModelSurface(tile, sceneTileModel, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
		bufferLength += bufferLengths[0];
		uvBufferLength += bufferLengths[1];
		underwaterTerrain += bufferLengths[2];

		bufferLengths = uploadHDTileModelUnderwater(tile, sceneTileModel, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
		bufferLength += bufferLengths[0];
		uvBufferLength += bufferLengths[1];
		underwaterTerrain += bufferLengths[2];

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int[] uploadHDTileModelSurface(Tile tile, SceneTileModel sceneTileModel, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		if (proceduralGenerator.skipTile[tileZ][tileX][tileY])
		{
			return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
		}

		final int[] faceColorA = sceneTileModel.getTriangleColorA();
		final int[] faceColorB = sceneTileModel.getTriangleColorB();
		final int[] faceColorC = sceneTileModel.getTriangleColorC();

		final int[] faceTextures = sceneTileModel.getTriangleTextureId();

		final int faceCount = sceneTileModel.getFaceX().length;

		int baseX = client.getBaseX();
		int baseY = client.getBaseY();

		for (int face = 0; face < faceCount; ++face)
		{
			int colorA = faceColorA[face];
			int colorB = faceColorB[face];
			int colorC = faceColorC[face];

			if (colorA == 12345678)
			{
				continue;
			}

			int[][] localVertices = proceduralGenerator.faceLocalVertices(tile, face);

			int[] vertexKeys = proceduralGenerator.faceVertexKeys(tile, face);
			int vertexKeyA = vertexKeys[0];
			int vertexKeyB = vertexKeys[1];
			int vertexKeyC = vertexKeys[2];

			boolean vertexAIsOverlay = false;
			boolean vertexBIsOverlay = false;
			boolean vertexCIsOverlay = false;

			Material materialA = Material.NONE;
			Material materialB = Material.NONE;
			Material materialC = Material.NONE;

			if (faceTextures != null)
			{
				materialA = Material.getTexture(faceTextures[face]);
				materialB = Material.getTexture(faceTextures[face]);
				materialC = Material.getTexture(faceTextures[face]);
			}

			materialA = proceduralGenerator.getSeasonalMaterial(materialA);
			materialB = proceduralGenerator.getSeasonalMaterial(materialB);
			materialC = proceduralGenerator.getSeasonalMaterial(materialC);

			WaterType waterType = proceduralGenerator.faceWaterType(tile, face, sceneTileModel);

			if (waterType != WaterType.NONE)
			{
				// apply WaterType-specific texture to use as an identifier in the water shader

				materialA = materialB = materialC = waterType.getGroundMaterial().getMaterials()[0];

				// set colors for the shoreline to create a foam effect in the water shader

				colorA = colorB = colorC = 127;

				if (proceduralGenerator.vertexIsWater.containsKey(vertexKeyA) && proceduralGenerator.vertexIsLand.containsKey(vertexKeyA))
				{
					colorA = 0;
				}
				if (proceduralGenerator.vertexIsWater.containsKey(vertexKeyB) && proceduralGenerator.vertexIsLand.containsKey(vertexKeyB))
				{
					colorB = 0;
				}
				if (proceduralGenerator.vertexIsWater.containsKey(vertexKeyC) && proceduralGenerator.vertexIsLand.containsKey(vertexKeyC))
				{
					colorC = 0;
				}
			}
			else if (hdPlugin.configGroundBlending && !(proceduralGenerator.isOverlayFace(tile, face) && proceduralGenerator.useDefaultColor(tile)) && materialA == Material.NONE)
			{
				// get the vertices' colors and textures from hashmaps

				colorA = proceduralGenerator.vertexTerrainColor.getOrDefault(vertexKeyA, colorA);
				colorB = proceduralGenerator.vertexTerrainColor.getOrDefault(vertexKeyB, colorB);
				colorC = proceduralGenerator.vertexTerrainColor.getOrDefault(vertexKeyC, colorC);

				if (hdPlugin.configGroundTextures)
				{
					materialA = proceduralGenerator.vertexTerrainTexture.getOrDefault(vertexKeyA, materialA);
					materialB = proceduralGenerator.vertexTerrainTexture.getOrDefault(vertexKeyB, materialB);
					materialC = proceduralGenerator.vertexTerrainTexture.getOrDefault(vertexKeyC, materialC);
				}
			}
			else if (hdPlugin.configGroundTextures)
			{
				// ground textures without blending

				GroundMaterial groundMaterial;

				if (proceduralGenerator.isOverlayFace(tile, face))
				{
					Overlay overlay = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client);
					overlay = proceduralGenerator.getSeasonalOverlay(overlay);
					groundMaterial = overlay.getGroundMaterial();

					colorA = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorA)));
					colorB = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorB)));
					colorC = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorC)));
				}
				else
				{
					Underlay underlay = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client);
					underlay = proceduralGenerator.getSeasonalUnderlay(underlay);
					groundMaterial = underlay.getGroundMaterial();

					colorA = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorA)));
					colorB = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorB)));
					colorC = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorC)));
				}

				materialA = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + (int) Math.floor((float) localVertices[0][0] / Perspective.LOCAL_TILE_SIZE), baseY + tileY + (int) Math.floor((float) localVertices[0][1] / Perspective.LOCAL_TILE_SIZE));
				materialB = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + (int) Math.floor((float) localVertices[1][0] / Perspective.LOCAL_TILE_SIZE), baseY + tileY + (int) Math.floor((float) localVertices[1][1] / Perspective.LOCAL_TILE_SIZE));
				materialC = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + (int) Math.floor((float) localVertices[2][0] / Perspective.LOCAL_TILE_SIZE), baseY + tileY + (int) Math.floor((float) localVertices[2][1] / Perspective.LOCAL_TILE_SIZE));
			}
			else if (hdPlugin.configWinterTheme)
			{
				if (proceduralGenerator.isOverlayFace(tile, face))
				{
					Overlay overlay = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client);
					overlay = proceduralGenerator.getSeasonalOverlay(overlay);

					colorA = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorA)));
					colorB = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorB)));
					colorC = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorC)));
				}
				else
				{
					Underlay underlay = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client);
					underlay = proceduralGenerator.getSeasonalUnderlay(underlay);

					colorA = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorA)));
					colorB = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorB)));
					colorC = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorC)));
				}
			}

			if (proceduralGenerator.vertexIsOverlay.containsKey(vertexKeyA) && proceduralGenerator.vertexIsUnderlay.containsKey(vertexKeyA))
			{
				vertexAIsOverlay = true;
			}
			if (proceduralGenerator.vertexIsOverlay.containsKey(vertexKeyB) && proceduralGenerator.vertexIsUnderlay.containsKey(vertexKeyB))
			{
				vertexBIsOverlay = true;
			}
			if (proceduralGenerator.vertexIsOverlay.containsKey(vertexKeyC) && proceduralGenerator.vertexIsUnderlay.containsKey(vertexKeyC))
			{
				vertexCIsOverlay = true;
			}

			float[] normalsA = new float[]{0,-1,0};
			float[] normalsB = new float[]{0,-1,0};
			float[] normalsC = new float[]{0,-1,0};

			// retrieve normals from hashmap
			if (waterType == WaterType.NONE)
			{
				normalsA = proceduralGenerator.vertexTerrainNormals.getOrDefault(vertexKeyA, normalsA);
				normalsB = proceduralGenerator.vertexTerrainNormals.getOrDefault(vertexKeyB, normalsB);
				normalsC = proceduralGenerator.vertexTerrainNormals.getOrDefault(vertexKeyC, normalsC);
			}

			int aTerrainData = packTerrainData(0, WaterType.NONE, tileZ);
			int bTerrainData = packTerrainData(0, WaterType.NONE, tileZ);
			int cTerrainData = packTerrainData(0, WaterType.NONE, tileZ);

			normalBuffer.ensureCapacity(12);
			normalBuffer.put(normalsA[0], normalsA[2], normalsA[1], aTerrainData);
			normalBuffer.put(normalsB[0], normalsB[2], normalsB[1], bTerrainData);
			normalBuffer.put(normalsC[0], normalsC[2], normalsC[1], cTerrainData);

			vertexBuffer.ensureCapacity(12);
			vertexBuffer.put(localVertices[0][0] + offsetX, localVertices[0][2], localVertices[0][1] + offsetY, colorA);
			vertexBuffer.put(localVertices[1][0] + offsetX, localVertices[1][2], localVertices[1][1] + offsetY, colorB);
			vertexBuffer.put(localVertices[2][0] + offsetX, localVertices[2][2], localVertices[2][1] + offsetY, colorC);

			bufferLength += 3;

			int packedMaterialDataA = modelPusher.packMaterialData(Material.getIndex(materialA), vertexAIsOverlay);
			int packedMaterialDataB = modelPusher.packMaterialData(Material.getIndex(materialB), vertexBIsOverlay);
			int packedMaterialDataC = modelPusher.packMaterialData(Material.getIndex(materialC), vertexCIsOverlay);

			uvBuffer.ensureCapacity(12);
			uvBuffer.put(packedMaterialDataA, localVertices[0][0] / 128f, localVertices[0][1] / 128f, 0f);
			uvBuffer.put(packedMaterialDataB, localVertices[1][0] / 128f, localVertices[1][1] / 128f, 0f);
			uvBuffer.put(packedMaterialDataC, localVertices[2][0] / 128f, localVertices[2][1] / 128f, 0f);

			uvBufferLength += 3;
		}

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int[] uploadHDTileModelUnderwater(Tile tile, SceneTileModel sceneTileModel, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		if (proceduralGenerator.skipTile[tileZ][tileX][tileY])
		{
			return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
		}

		final int[] faceColorA = sceneTileModel.getTriangleColorA();
		final int[] faceColorB = sceneTileModel.getTriangleColorB();
		final int[] faceColorC = sceneTileModel.getTriangleColorC();

		final int faceCount = sceneTileModel.getFaceX().length;

		int baseX = client.getBaseX();
		int baseY = client.getBaseY();

		if (baseX + offsetX >= 2816 && baseX + offsetX <= 2970 && baseY + offsetY <= 5375 && baseY + offsetY >= 5220)
		{
			// fix for God Wars Dungeon's water rendering over zamorak bridge
			return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
		}

		if (hdPlugin.configWaterEffects == WaterEffects.ALL && proceduralGenerator.tileIsWater[tileZ][tileX][tileY])
		{
			underwaterTerrain = 1;

			// underwater terrain
			for (int face = 0; face < faceCount; ++face)
			{
				int colorA = 6676;
				int colorB = 6676;
				int colorC = 6676;

				if (faceColorA[face] == 12345678)
				{
					continue;
				}

				int[][] localVertices = proceduralGenerator.faceLocalVertices(tile, face);

				Material materialA = Material.NONE;
				Material materialB = Material.NONE;
				Material materialC = Material.NONE;

				int[] vertexKeys = proceduralGenerator.faceVertexKeys(tile, face);
				int vertexKeyA = vertexKeys[0];
				int vertexKeyB = vertexKeys[1];
				int vertexKeyC = vertexKeys[2];

				int depthA = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(vertexKeyA, 0);
				int depthB = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(vertexKeyB, 0);
				int depthC = proceduralGenerator.vertexUnderwaterDepth.getOrDefault(vertexKeyC, 0);

				if (hdPlugin.configGroundTextures)
				{
					GroundMaterial groundMaterial = GroundMaterial.UNDERWATER_GENERIC;

					int tileVertexX = Math.round((float)localVertices[0][0] / (float)Perspective.LOCAL_TILE_SIZE) + tileX + baseX;
					int tileVertexY = Math.round((float)localVertices[0][1] / (float)Perspective.LOCAL_TILE_SIZE) + tileY + baseY;
					materialA = groundMaterial.getRandomMaterial(tileZ, tileVertexX, tileVertexY);

					tileVertexX = Math.round((float)localVertices[1][0] / (float)Perspective.LOCAL_TILE_SIZE) + tileX + baseX;
					tileVertexY = Math.round((float)localVertices[1][1] / (float)Perspective.LOCAL_TILE_SIZE) + tileY + baseY;
					materialB = groundMaterial.getRandomMaterial(tileZ, tileVertexX, tileVertexY);

					tileVertexX = Math.round((float)localVertices[2][0] / (float)Perspective.LOCAL_TILE_SIZE) + tileX + baseX;
					tileVertexY = Math.round((float)localVertices[2][1] / (float)Perspective.LOCAL_TILE_SIZE) + tileY + baseY;
					materialC = groundMaterial.getRandomMaterial(tileZ, tileVertexX, tileVertexY);
				}

				float[] normalsA = proceduralGenerator.vertexTerrainNormals.getOrDefault(vertexKeyA, new float[]{0,-1,0});
				float[] normalsB = proceduralGenerator.vertexTerrainNormals.getOrDefault(vertexKeyB, new float[]{0,-1,0});
				float[] normalsC = proceduralGenerator.vertexTerrainNormals.getOrDefault(vertexKeyC, new float[]{0,-1,0});

				if (normalsA == null)
				{
					normalsA = new float[]{0,-1,0};
				}
				if (normalsB == null)
				{
					normalsB = new float[]{0,-1,0};
				}
				if (normalsC == null)
				{
					normalsC = new float[]{0,-1,0};
				}

				WaterType waterType = proceduralGenerator.faceWaterType(tile, face, sceneTileModel);

				int aTerrainData = packTerrainData(depthA, waterType, tileZ);
				int bTerrainData = packTerrainData(depthB, waterType, tileZ);
				int cTerrainData = packTerrainData(depthC, waterType, tileZ);

				normalBuffer.ensureCapacity(12);
				normalBuffer.put(normalsA[0], normalsA[2], normalsA[1], aTerrainData);
				normalBuffer.put(normalsB[0], normalsB[2], normalsB[1], bTerrainData);
				normalBuffer.put(normalsC[0], normalsC[2], normalsC[1], cTerrainData);

				vertexBuffer.ensureCapacity(12);
				vertexBuffer.put(localVertices[0][0] + offsetX, localVertices[0][2] + depthA, localVertices[0][1] + offsetY, colorA);
				vertexBuffer.put(localVertices[1][0] + offsetX, localVertices[1][2] + depthB, localVertices[1][1] + offsetY, colorB);
				vertexBuffer.put(localVertices[2][0] + offsetX, localVertices[2][2] + depthC, localVertices[2][1] + offsetY, colorC);

				bufferLength += 3;

				int packedMaterialDataA = modelPusher.packMaterialData(Material.getIndex(materialA), false);
				int packedMaterialDataB = modelPusher.packMaterialData(Material.getIndex(materialB), false);
				int packedMaterialDataC = modelPusher.packMaterialData(Material.getIndex(materialC), false);

				uvBuffer.ensureCapacity(12);
				uvBuffer.put(packedMaterialDataA, localVertices[0][0] / 128f, localVertices[0][1] / 128f, 0f);
				uvBuffer.put(packedMaterialDataB, localVertices[1][0] / 128f, localVertices[1][1] / 128f, 0f);
				uvBuffer.put(packedMaterialDataC, localVertices[2][0] / 128f, localVertices[2][1] / 128f, 0f);

				uvBufferLength += 3;
			}
		}

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	private int packTerrainData(int waterDepth, WaterType underwaterType, int plane)
	{
		byte isTerrain = 0b1;
		return ((waterDepth << 4 | underwaterType.getValue()) << 2 | plane) << 1 | isTerrain;
	}
}
