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
	private HdPlugin gpuPlugin;

	@Inject
	ProceduralGenerator proceduralGenerator;

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
						if (gpuPlugin.configHdMode)
						{
							uploadHD(tile, vertexBuffer, uvBuffer, normalBuffer);
						}
						else
						{
							upload(tile, vertexBuffer, uvBuffer);
						}
					}
				}
			}
		}

		stopwatch.stop();
		log.debug("Scene upload time: {}", stopwatch);
	}

	private void upload(Tile tile, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer)
	{
		Tile bridge = tile.getBridge();
		if (bridge != null)
		{
			upload(bridge, vertexBuffer, uvBuffer);
		}

		SceneTilePaint sceneTilePaint = tile.getSceneTilePaint();
		if (sceneTilePaint != null)
		{
			sceneTilePaint.setBufferOffset(offset);
			if (sceneTilePaint.getTexture() != -1)
			{
				sceneTilePaint.setUvBufferOffset(uvoffset);
			}
			else
			{
				sceneTilePaint.setUvBufferOffset(-1);
			}
			Point tilePoint = tile.getSceneLocation();
			int len = upload(sceneTilePaint,
				tile.getRenderLevel(), tilePoint.getX(), tilePoint.getY(),
				vertexBuffer, uvBuffer,
				0, 0, false);
			sceneTilePaint.setBufferLen(len);
			offset += len;
			if (sceneTilePaint.getTexture() != -1)
			{
				uvoffset += len;
			}
		}

		SceneTileModel sceneTileModel = tile.getSceneTileModel();
		if (sceneTileModel != null)
		{
			sceneTileModel.setBufferOffset(offset);
			if (sceneTileModel.getTriangleTextureId() != null)
			{
				sceneTileModel.setUvBufferOffset(uvoffset);
			}
			else
			{
				sceneTileModel.setUvBufferOffset(-1);
			}
			Point tilePoint = tile.getSceneLocation();
			int len = upload(sceneTileModel,
				tilePoint.getX(), tilePoint.getY(),
				vertexBuffer, uvBuffer,
				0, 0, false);
			sceneTileModel.setBufferLen(len);
			offset += len;
			if (sceneTileModel.getTriangleTextureId() != null)
			{
				uvoffset += len;
			}
		}

		WallObject wallObject = tile.getWallObject();
		if (wallObject != null)
		{
			Renderable renderable1 = wallObject.getRenderable1();
			if (renderable1 instanceof Model)
			{
				uploadModel((Model) renderable1, vertexBuffer, uvBuffer, null, 0, 0, 0, null, null);
			}

			Renderable renderable2 = wallObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				uploadModel((Model) renderable2, vertexBuffer, uvBuffer, null, 0, 0, 0, null, null);
			}
		}

		GroundObject groundObject = tile.getGroundObject();
		if (groundObject != null)
		{
			Renderable renderable = groundObject.getRenderable();
			if (renderable instanceof Model)
			{
				uploadModel((Model) renderable, vertexBuffer, uvBuffer, null, 0, 0, 0, null, null);
			}
		}

		DecorativeObject decorativeObject = tile.getDecorativeObject();
		if (decorativeObject != null)
		{
			Renderable renderable = decorativeObject.getRenderable();
			if (renderable instanceof Model)
			{
				uploadModel((Model) renderable, vertexBuffer, uvBuffer, null, 0, 0, 0, null, null);
			}

			Renderable renderable2 = decorativeObject.getRenderable2();
			if (renderable2 instanceof Model)
			{
				uploadModel((Model) renderable2, vertexBuffer, uvBuffer, null, 0, 0, 0, null, null);
			}
		}

		GameObject[] gameObjects = tile.getGameObjects();
		for (GameObject gameObject : gameObjects)
		{
			if (gameObject == null)
			{
				continue;
			}

			Renderable renderable = gameObject.getRenderable();
			if (renderable instanceof Model)
			{
				uploadModel((Model) gameObject.getRenderable(), vertexBuffer, uvBuffer, null, 0, 0, 0, null, null);
			}
		}
	}

	int upload(SceneTilePaint tile, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer,
		int offsetX, int offsetY, boolean padUvs)
	{
		final int[][][] tileHeights = client.getTileHeights();

		final int localX = offsetX;
		final int localY = offsetY;

		final int swHeight = tileHeights[tileZ][tileX][tileY];
		final int seHeight = tileHeights[tileZ][tileX + 1][tileY];
		final int neHeight = tileHeights[tileZ][tileX + 1][tileY + 1];
		final int nwHeight = tileHeights[tileZ][tileX][tileY + 1];

		final int neColor = tile.getNeColor();
		final int nwColor = tile.getNwColor();
		final int seColor = tile.getSeColor();
		final int swColor = tile.getSwColor();

		if (neColor == 12345678)
		{
			return 0;
		}

		vertexBuffer.ensureCapacity(24);
		uvBuffer.ensureCapacity(24);

		// 0,0
		int vertexDx = localX;
		int vertexDy = localY;
		int vertexDz = swHeight;
		final int c1 = swColor;

		// 1,0
		int vertexCx = localX + Perspective.LOCAL_TILE_SIZE;
		int vertexCy = localY;
		int vertexCz = seHeight;
		final int c2 = seColor;

		// 1,1
		int vertexAx = localX + Perspective.LOCAL_TILE_SIZE;
		int vertexAy = localY + Perspective.LOCAL_TILE_SIZE;
		int vertexAz = neHeight;
		final int c3 = neColor;

		// 0,1
		int vertexBx = localX;
		int vertexBy = localY + Perspective.LOCAL_TILE_SIZE;
		int vertexBz = nwHeight;
		final int c4 = nwColor;

		vertexBuffer.put(vertexAx, vertexAz, vertexAy, c3);
		vertexBuffer.put(vertexBx, vertexBz, vertexBy, c4);
		vertexBuffer.put(vertexCx, vertexCz, vertexCy, c2);

		vertexBuffer.put(vertexDx, vertexDz, vertexDy, c1);
		vertexBuffer.put(vertexCx, vertexCz, vertexCy, c2);
		vertexBuffer.put(vertexBx, vertexBz, vertexBy, c4);

		if (padUvs || tile.getTexture() != -1)
		{
			int packedTextureData = packTextureData(tile.getTexture(), false);

			uvBuffer.put(packedTextureData, 1.0f, 1.0f, 0f);
			uvBuffer.put(packedTextureData, 0.0f, 1.0f, 0f);
			uvBuffer.put(packedTextureData, 1.0f, 0.0f, 0f);

			uvBuffer.put(packedTextureData, 0.0f, 0.0f, 0f);
			uvBuffer.put(packedTextureData, 1.0f, 0.0f, 0f);
			uvBuffer.put(packedTextureData, 0.0f, 1.0f, 0f);
		}

		return 6;
	}

	int upload(SceneTileModel sceneTileModel, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer,
		int offsetX, int offsetY, boolean padUvs)
	{
		final int[] faceX = sceneTileModel.getFaceX();
		final int[] faceY = sceneTileModel.getFaceY();
		final int[] faceZ = sceneTileModel.getFaceZ();

		final int[] vertexX = sceneTileModel.getVertexX();
		final int[] vertexY = sceneTileModel.getVertexY();
		final int[] vertexZ = sceneTileModel.getVertexZ();

		final int[] triangleColorA = sceneTileModel.getTriangleColorA();
		final int[] triangleColorB = sceneTileModel.getTriangleColorB();
		final int[] triangleColorC = sceneTileModel.getTriangleColorC();

		final int[] triangleTextures = sceneTileModel.getTriangleTextureId();

		final int faceCount = faceX.length;

		vertexBuffer.ensureCapacity(faceCount * 12);
		uvBuffer.ensureCapacity(faceCount * 12);

		int baseX = Perspective.LOCAL_TILE_SIZE * tileX;
		int baseY = Perspective.LOCAL_TILE_SIZE * tileY;

		int cnt = 0;
		for (int i = 0; i < faceCount; ++i)
		{
			final int triangleA = faceX[i];
			final int triangleB = faceY[i];
			final int triangleC = faceZ[i];

			final int colorA = triangleColorA[i];
			final int colorB = triangleColorB[i];
			final int colorC = triangleColorC[i];

			if (colorA == 12345678)
			{
				continue;
			}

			cnt += 3;

			// vertexes are stored in scene local, convert to tile local
			int vertexXA = vertexX[triangleA] - baseX;
			int vertexZA = vertexZ[triangleA] - baseY;

			int vertexXB = vertexX[triangleB] - baseX;
			int vertexZB = vertexZ[triangleB] - baseY;

			int vertexXC = vertexX[triangleC] - baseX;
			int vertexZC = vertexZ[triangleC] - baseY;

			vertexBuffer.put(vertexXA + offsetX, vertexY[triangleA], vertexZA + offsetY, colorA);
			vertexBuffer.put(vertexXB + offsetX, vertexY[triangleB], vertexZB + offsetY, colorB);
			vertexBuffer.put(vertexXC + offsetX, vertexY[triangleC], vertexZC + offsetY, colorC);

			if (padUvs || triangleTextures != null)
			{
				if (triangleTextures != null && triangleTextures[i] != -1)
				{
					int packedTextureData = packTextureData(triangleTextures[i], false);

					uvBuffer.put(packedTextureData, vertexXA / 128f, vertexZA / 128f, 0f);
					uvBuffer.put(packedTextureData, vertexXB / 128f, vertexZB / 128f, 0f);
					uvBuffer.put(packedTextureData, vertexXC / 128f, vertexZC / 128f, 0f);
				}
				else
				{
					uvBuffer.put(0, 0, 0, 0f);
					uvBuffer.put(0, 0, 0, 0f);
					uvBuffer.put(0, 0, 0, 0f);
				}
			}
		}

		return cnt;
	}

	private void uploadModel(Model model, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int tileZ, int tileX, int tileY, ObjectProperties objectProperties, ObjectType objectType)
	{
		if (model.getSceneId() == sceneId)
		{
			return; // model has already been uploaded
		}

		byte skipObject = 0;

		if (objectType == ObjectType.GROUND_OBJECT || objectType == ObjectType.DECORATIVE_OBJECT)
		{
			skipObject = 0b1;
		}
		// pack a bit into bufferoffset that we can use later to hide
		// some low-importance objects based on Level of Detail setting
		model.setBufferOffset(offset << 1 | skipObject);
		if (model.getFaceTextures() != null || (objectProperties != null && objectProperties.getMaterial() != Material.NONE))
		{
			model.setUvBufferOffset(uvoffset);
		}
		else
		{
			model.setUvBufferOffset(-1);
		}
		model.setSceneId(sceneId);

		final int faceCount = model.getTrianglesCount();
		int len = 0;
		for (int face = 0; face < faceCount; ++face)
		{
			len += pushFace(model, face, false, vertexBuffer, uvBuffer, normalBuffer, 0, 0, 0, 0, tileZ, tileX, tileY, objectProperties, objectType);
		}

		offset += len;
		if (model.getFaceTextures() != null || (objectProperties != null && objectProperties.getMaterial() != Material.NONE))
		{
			uvoffset += len;
		}
	}

	private void uploadHD(Tile tile, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer)
	{
		Tile bridge = tile.getBridge();
		if (bridge != null)
		{
			uploadHD(bridge, vertexBuffer, uvBuffer, normalBuffer);
		}

		final Point tilePoint = tile.getSceneLocation();
		final int tileX = tilePoint.getX();
		final int tileY = tilePoint.getY();
		final int tileZ = tile.getRenderLevel();

		SceneTilePaint sceneTilePaint = tile.getSceneTilePaint();
		if (sceneTilePaint != null)
		{
			int[] uploadedTilePaintData = uploadHD(
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
			int packedBufferLength = bufferLength << 1;
			if (underwaterTerrain == 1)
			{
				// set the boolean bit to 1 if we generated underwater terrain
				packedBufferLength = packedBufferLength | 1;
			}
			sceneTilePaint.setBufferOffset(offset);
			sceneTilePaint.setUvBufferOffset(uvBufferLength > 0 ? uvoffset : -1);
			sceneTilePaint.setBufferLen(packedBufferLength);
			offset += bufferLength;
			uvoffset += uvBufferLength;
		}

		SceneTileModel sceneTileModel = tile.getSceneTileModel();
		if (sceneTileModel != null)
		{
			int[] uploadedTileModelData = uploadHD(
				tile, sceneTileModel,
				tileZ, tileX, tileY,
				vertexBuffer, uvBuffer, normalBuffer,
				0, 0);

			final int bufferLength = uploadedTileModelData[0];
			final int uvBufferLength = uploadedTileModelData[1];
			final int underwaterTerrain = uploadedTileModelData[2];
			// pack a boolean into the buffer length of tiles so we can tell
			// which tiles have procedurally-generated underwater terrain
			int packedBufferLength = bufferLength << 1;
			if (underwaterTerrain == 1)
			{
				packedBufferLength = packedBufferLength | 1;
			}
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

	int[] uploadHD(Tile tile, SceneTilePaint sceneTilePaint, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		if (gpuPlugin.configHdMode)
		{
			// Underwater terrain needs to be uploaded first when compute mode is disabled
			// else it will be drawn in the wrong order
			if (gpuPlugin.computeMode == HdPlugin.ComputeMode.NONE)
			{
				int[] uploadResult;

				uploadResult = uploadHDTilePaintUnderwater(tile, sceneTilePaint, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];

				uploadResult = uploadHDTilePaintSurface(tile, sceneTilePaint, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];
			}
			else
			{
				int[] uploadResult;

				uploadResult = uploadHDTilePaintSurface(tile, sceneTilePaint, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, 0, 0);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];

				uploadResult = uploadHDTilePaintUnderwater(tile, sceneTilePaint, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, 0, 0);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];
			}
		}

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
			else if (!proceduralGenerator.useDefaultColor(tile) && sceneTilePaint.getTexture() == -1)
			{
				// get the vertices' colors and textures from hashmaps

				swColor = proceduralGenerator.vertexTerrainColor.getOrDefault(swVertexKey, swColor);
				seColor = proceduralGenerator.vertexTerrainColor.getOrDefault(seVertexKey, seColor);
				neColor = proceduralGenerator.vertexTerrainColor.getOrDefault(neVertexKey, neColor);
				nwColor = proceduralGenerator.vertexTerrainColor.getOrDefault(nwVertexKey, nwColor);

				if (gpuPlugin.configGroundTextures)
				{
					swMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(swVertexKey, swMaterial);
					seMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(seVertexKey, seMaterial);
					neMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(neVertexKey, neMaterial);
					nwMaterial = proceduralGenerator.vertexTerrainTexture.getOrDefault(nwVertexKey, nwMaterial);
				}
			}
			else
			{
				GroundMaterial groundMaterial;

				if (client.getScene().getOverlayIds()[tileZ][tileX][tileY] != 0)
				{
					Overlay overlay = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client);
					groundMaterial = overlay.getGroundMaterial();

					swColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(swColor)));
					seColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(seColor)));
					nwColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(nwColor)));
					neColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(neColor)));
				}
				else
				{
					Underlay underlay = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client);
					groundMaterial = underlay.getGroundMaterial();

					swColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(swColor)));
					seColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(seColor)));
					nwColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(nwColor)));
					neColor = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(neColor)));
				}

				if (gpuPlugin.configGroundTextures)
				{
					swMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY);
					seMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY);
					nwMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY + 1);
					neMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY + 1);
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

			normalBuffer.ensureCapacity(24);
			normalBuffer.put(neNormals[0], neNormals[2], neNormals[1], 0);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], 0);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], 0);

			normalBuffer.put(swNormals[0], swNormals[2], swNormals[1], 0);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], 0);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], 0);

			vertexBuffer.ensureCapacity(24);
			vertexBuffer.put(localNeVertexX, neHeight, localNeVertexY, neColor);
			vertexBuffer.put(localNwVertexX, nwHeight, localNwVertexY, nwColor);
			vertexBuffer.put(localSeVertexX, seHeight, localSeVertexY, seColor);

			vertexBuffer.put(localSwVertexX, swHeight, localSwVertexY, swColor);
			vertexBuffer.put(localSeVertexX, seHeight, localSeVertexY, seColor);
			vertexBuffer.put(localNwVertexX, nwHeight, localNwVertexY, nwColor);

			bufferLength += 6;

			int packedTextureDataSW = packTextureData(Material.getIndex(swMaterial), swVertexIsOverlay);
			int packedTextureDataSE = packTextureData(Material.getIndex(seMaterial), seVertexIsOverlay);
			int packedTextureDataNW = packTextureData(Material.getIndex(nwMaterial), nwVertexIsOverlay);
			int packedTextureDataNE = packTextureData(Material.getIndex(neMaterial), neVertexIsOverlay);

			uvBuffer.ensureCapacity(24);
			uvBuffer.put(packedTextureDataNE, 1.0f, 1.0f, 0f);
			uvBuffer.put(packedTextureDataNW, 0.0f, 1.0f, 0f);
			uvBuffer.put(packedTextureDataSE, 1.0f, 0.0f, 0f);

			uvBuffer.put(packedTextureDataSW, 0.0f, 0.0f, 0f);
			uvBuffer.put(packedTextureDataSE, 1.0f, 0.0f, 0f);
			uvBuffer.put(packedTextureDataNW, 0.0f, 1.0f, 0f);

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

		if (gpuPlugin.configWaterEffects == WaterEffects.ALL && proceduralGenerator.tileIsWater[tileZ][tileX][tileY])
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

			if (gpuPlugin.configGroundTextures)
			{
				GroundMaterial groundMaterial = GroundMaterial.UNDERWATER_GENERIC;

				swMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY);
				seMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY);
				nwMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX, baseY + tileY + 1);
				neMaterial = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + 1, baseY + tileY + 1);
			}

			WaterType waterType = proceduralGenerator.tileWaterType(tile, sceneTilePaint);

			int swWaterData = swDepth << 5 | waterType.getValue();
			int seWaterData = seDepth << 5 | waterType.getValue();
			int nwWaterData = nwDepth << 5 | waterType.getValue();
			int neWaterData = neDepth << 5 | waterType.getValue();

			normalBuffer.ensureCapacity(24);
			normalBuffer.put(neNormals[0], neNormals[2], neNormals[1], neWaterData);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], nwWaterData);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], seWaterData);

			normalBuffer.put(swNormals[0], swNormals[2], swNormals[1], swWaterData);
			normalBuffer.put(seNormals[0], seNormals[2], seNormals[1], seWaterData);
			normalBuffer.put(nwNormals[0], nwNormals[2], nwNormals[1], nwWaterData);

			vertexBuffer.ensureCapacity(24);
			vertexBuffer.put(localNeVertexX, neHeight + neDepth, localNeVertexY, neColor);
			vertexBuffer.put(localNwVertexX, nwHeight + nwDepth, localNwVertexY, nwColor);
			vertexBuffer.put(localSeVertexX, seHeight + seDepth, localSeVertexY, seColor);

			vertexBuffer.put(localSwVertexX, swHeight + swDepth, localSwVertexY, swColor);
			vertexBuffer.put(localSeVertexX, seHeight + seDepth, localSeVertexY, seColor);
			vertexBuffer.put(localNwVertexX, nwHeight + nwDepth, localNwVertexY, nwColor);

			bufferLength += 6;

			int packedTextureDataSW = packTextureData(Material.getIndex(swMaterial), false);
			int packedTextureDataSE = packTextureData(Material.getIndex(seMaterial), false);
			int packedTextureDataNW = packTextureData(Material.getIndex(nwMaterial), false);
			int packedTextureDataNE = packTextureData(Material.getIndex(neMaterial), false);

			uvBuffer.ensureCapacity(24);
			uvBuffer.put(packedTextureDataNE, 1.0f, 1.0f, 0f);
			uvBuffer.put(packedTextureDataNW, 0.0f, 1.0f, 0f);
			uvBuffer.put(packedTextureDataSE, 1.0f, 0.0f, 0f);

			uvBuffer.put(packedTextureDataSW, 0.0f, 0.0f, 0f);
			uvBuffer.put(packedTextureDataSE, 1.0f, 0.0f, 0f);
			uvBuffer.put(packedTextureDataNW, 0.0f, 1.0f, 0f);

			uvBufferLength += 6;
		}

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int[] uploadHD(Tile tile, SceneTileModel sceneTileModel, int tileZ, int tileX, int tileY, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int offsetX, int offsetY)
	{
		int bufferLength = 0;
		int uvBufferLength = 0;
		int underwaterTerrain = 0;

		if (gpuPlugin.configHdMode)
		{
			// Underwater terrain needs to be uploaded first when compute mode is disabled
			// else it will be drawn in the wrong order
			if (gpuPlugin.computeMode == HdPlugin.ComputeMode.NONE)
			{
				int[] uploadResult;

				uploadResult = uploadHDTileModelUnderwater(tile, sceneTileModel, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];

				uploadResult = uploadHDTileModelSurface(tile, sceneTileModel, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];
			}
			else
			{
				int[] uploadResult;

				uploadResult = uploadHDTileModelSurface(tile, sceneTileModel, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];

				uploadResult = uploadHDTileModelUnderwater(tile, sceneTileModel, tileZ, tileX, tileY, vertexBuffer, uvBuffer, normalBuffer, offsetX, offsetY);
				bufferLength += uploadResult[0];
				uvBufferLength += uploadResult[1];
				underwaterTerrain += uploadResult[2];
			}
		}

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
			else if (!(proceduralGenerator.isOverlayFace(tile, face) && proceduralGenerator.useDefaultColor(tile)) && materialA == Material.NONE)
			{
				// get the vertices' colors and textures from hashmaps

				colorA = proceduralGenerator.vertexTerrainColor.getOrDefault(vertexKeyA, colorA);
				colorB = proceduralGenerator.vertexTerrainColor.getOrDefault(vertexKeyB, colorB);
				colorC = proceduralGenerator.vertexTerrainColor.getOrDefault(vertexKeyC, colorC);

				if (gpuPlugin.configGroundTextures)
				{
					materialA = proceduralGenerator.vertexTerrainTexture.getOrDefault(vertexKeyA, materialA);
					materialB = proceduralGenerator.vertexTerrainTexture.getOrDefault(vertexKeyB, materialB);
					materialC = proceduralGenerator.vertexTerrainTexture.getOrDefault(vertexKeyC, materialC);
				}
			}
			else
			{
				// ground textures without blending

				GroundMaterial groundMaterial;

				if (proceduralGenerator.isOverlayFace(tile, face))
				{
					Overlay overlay = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client);
					groundMaterial = overlay.getGroundMaterial();

					colorA = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorA)));
					colorB = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorB)));
					colorC = HDUtils.colorHSLToInt(proceduralGenerator.recolorOverlay(overlay, HDUtils.colorIntToHSL(colorC)));
				}
				else
				{
					Underlay underlay = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client);
					groundMaterial = underlay.getGroundMaterial();

					colorA = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorA)));
					colorB = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorB)));
					colorC = HDUtils.colorHSLToInt(proceduralGenerator.recolorUnderlay(underlay, HDUtils.colorIntToHSL(colorC)));
				}

				if (gpuPlugin.configGroundTextures)
				{
					materialA = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + (int) Math.floor((float) localVertices[0][0] / Perspective.LOCAL_TILE_SIZE), baseY + tileY + (int) Math.floor((float) localVertices[0][1] / Perspective.LOCAL_TILE_SIZE));
					materialB = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + (int) Math.floor((float) localVertices[1][0] / Perspective.LOCAL_TILE_SIZE), baseY + tileY + (int) Math.floor((float) localVertices[1][1] / Perspective.LOCAL_TILE_SIZE));
					materialC = groundMaterial.getRandomMaterial(tileZ, baseX + tileX + (int) Math.floor((float) localVertices[2][0] / Perspective.LOCAL_TILE_SIZE), baseY + tileY + (int) Math.floor((float) localVertices[2][1] / Perspective.LOCAL_TILE_SIZE));
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

			normalBuffer.ensureCapacity(12);
			normalBuffer.put(normalsA[0], normalsA[2], normalsA[1], 0);
			normalBuffer.put(normalsB[0], normalsB[2], normalsB[1], 0);
			normalBuffer.put(normalsC[0], normalsC[2], normalsC[1], 0);

			vertexBuffer.ensureCapacity(12);
			vertexBuffer.put(localVertices[0][0] + offsetX, localVertices[0][2], localVertices[0][1] + offsetY, colorA);
			vertexBuffer.put(localVertices[1][0] + offsetX, localVertices[1][2], localVertices[1][1] + offsetY, colorB);
			vertexBuffer.put(localVertices[2][0] + offsetX, localVertices[2][2], localVertices[2][1] + offsetY, colorC);

			bufferLength += 3;

			int packedTextureDataA = packTextureData(Material.getIndex(materialA), vertexAIsOverlay);
			int packedTextureDataB = packTextureData(Material.getIndex(materialB), vertexBIsOverlay);
			int packedTextureDataC = packTextureData(Material.getIndex(materialC), vertexCIsOverlay);

			uvBuffer.ensureCapacity(12);
			uvBuffer.put(packedTextureDataA, localVertices[0][0] / 128f, localVertices[0][1] / 128f, 0f);
			uvBuffer.put(packedTextureDataB, localVertices[1][0] / 128f, localVertices[1][1] / 128f, 0f);
			uvBuffer.put(packedTextureDataC, localVertices[2][0] / 128f, localVertices[2][1] / 128f, 0f);

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

		if (gpuPlugin.configWaterEffects == WaterEffects.ALL && proceduralGenerator.tileIsWater[tileZ][tileX][tileY])
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

				if (gpuPlugin.configGroundTextures)
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

				int aWaterData = depthA << 5 | waterType.getValue();
				int bWaterData = depthB << 5 | waterType.getValue();
				int cWaterData = depthC << 5 | waterType.getValue();

				normalBuffer.ensureCapacity(12);
				normalBuffer.put(normalsA[0], normalsA[2], normalsA[1], aWaterData);
				normalBuffer.put(normalsB[0], normalsB[2], normalsB[1], bWaterData);
				normalBuffer.put(normalsC[0], normalsC[2], normalsC[1], cWaterData);

				vertexBuffer.ensureCapacity(12);
				vertexBuffer.put(localVertices[0][0] + offsetX, localVertices[0][2] + depthA, localVertices[0][1] + offsetY, colorA);
				vertexBuffer.put(localVertices[1][0] + offsetX, localVertices[1][2] + depthB, localVertices[1][1] + offsetY, colorB);
				vertexBuffer.put(localVertices[2][0] + offsetX, localVertices[2][2] + depthC, localVertices[2][1] + offsetY, colorC);

				bufferLength += 3;

				int packedTextureDataA = packTextureData(Material.getIndex(materialA), false);
				int packedTextureDataB = packTextureData(Material.getIndex(materialB), false);
				int packedTextureDataC = packTextureData(Material.getIndex(materialC), false);

				uvBuffer.ensureCapacity(12);
				uvBuffer.put(packedTextureDataA, localVertices[0][0] / 128f, localVertices[0][1] / 128f, 0f);
				uvBuffer.put(packedTextureDataB, localVertices[1][0] / 128f, localVertices[1][1] / 128f, 0f);
				uvBuffer.put(packedTextureDataC, localVertices[2][0] / 128f, localVertices[2][1] / 128f, 0f);

				uvBufferLength += 3;
			}
		}

		return new int[]{bufferLength, uvBufferLength, underwaterTerrain};
	}

	int pushFace(Model model, int face, boolean padUvs, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int xOffset, int yOffset, int zOffset, int orientation, int tileZ, int tileX, int tileY, ObjectProperties objectProperties, ObjectType objectType)
	{
		final int[] vertexX = model.getVerticesX();
		final int[] vertexY = model.getVerticesY();
		final int[] vertexZ = model.getVerticesZ();

		final int[] trianglesX = model.getTrianglesX();
		final int[] trianglesY = model.getTrianglesY();
		final int[] trianglesZ = model.getTrianglesZ();

		final int[] color1s = model.getFaceColors1();
		final int[] color2s = model.getFaceColors2();
		final int[] color3s = model.getFaceColors3();

		final byte[] transparencies = model.getTriangleTransparencies();
		final short[] faceTextures = model.getFaceTextures();
		final byte[] facePriorities = model.getFaceRenderPriorities();

		int triangleA = trianglesX[face];
		int triangleB = trianglesY[face];
		int triangleC = trianglesZ[face];

		int color1 = color1s[face];
		int color2 = color2s[face];
		int color3 = color3s[face];

		int packedAlphaPriority = packAlphaPriority(faceTextures, transparencies, facePriorities, face);

		int sin = 0, cos = 0;
		if (orientation != 0)
		{
			sin = Perspective.SINE[orientation];
			cos = Perspective.COSINE[orientation];
		}

		if (color3 == -1)
		{
			color2 = color3 = color1;
		}
		else if (color3 == -2)
		{
			vertexBuffer.ensureCapacity(12);
			vertexBuffer.put(0, 0, 0, 0);
			vertexBuffer.put(0, 0, 0, 0);
			vertexBuffer.put(0, 0, 0, 0);

			if (gpuPlugin.configHdMode && normalBuffer != null)
			{
				normalBuffer.ensureCapacity(12);
				normalBuffer.put(0, 0, 0, 0);
				normalBuffer.put(0, 0, 0, 0);
				normalBuffer.put(0, 0, 0, 0);
			}

			if (padUvs || faceTextures != null || (gpuPlugin.configHdMode && objectProperties != null && objectProperties.getMaterial() != Material.NONE))
			{
				uvBuffer.ensureCapacity(12);
				uvBuffer.put(0, 0, 0, 0f);
				uvBuffer.put(0, 0, 0, 0f);
				uvBuffer.put(0, 0, 0, 0f);
			}
			return 3;
		}

		int vnAX = 0, vnAY = 0, vnAZ = 0;
		int vnBX = 0, vnBY = 0, vnBZ = 0;
		int vnCX = 0, vnCY = 0, vnCZ = 0;

		if (gpuPlugin.configHdMode)
		{
			int[] vertexNormalsX = model.getVertexNormalsX();
			int[] vertexNormalsY = model.getVertexNormalsY();
			int[] vertexNormalsZ = model.getVertexNormalsZ();

			vnAX = vertexNormalsX[triangleA];
			vnAY = vertexNormalsY[triangleA];
			vnAZ = vertexNormalsZ[triangleA];

			vnBX = vertexNormalsX[triangleB];
			vnBY = vertexNormalsY[triangleB];
			vnBZ = vertexNormalsZ[triangleB];

			vnCX = vertexNormalsX[triangleC];
			vnCY = vertexNormalsY[triangleC];
			vnCZ = vertexNormalsZ[triangleC];
		}

		if (gpuPlugin.configHdMode)
		{
			int[] color1HSL = HDUtils.colorIntToHSL(color1);
			int[] color2HSL = HDUtils.colorIntToHSL(color2);
			int[] color3HSL = HDUtils.colorIntToHSL(color3);

			// reduce the effect of the baked shading by approximately inverting the process by which
			// the shading is added initially.
			// first, take a directional vector approximately opposite of the directional light
			// used by the client..
			float[] inverseLightDirection = VectorUtil.normalizeVec3(new float[]{0.5f, 0.5f, 0.5f});

			// multiplier applied to vertex' lightness value.
			// results in greater lightening of lighter colors
			float lightnessMultiplier = 3f;
			// the minimum amount by which each color will be lightened
			int baseLighten = 10;
			// subtracts the X lowest lightness levels from the formula.
			// helps keep darker colors appropriately dark
			int ignoreLowLightness = 3;

			int lightenA = (int) (Math.max((color1HSL[2] - ignoreLowLightness), 0) * lightnessMultiplier) + baseLighten;
			// use the dot product of the inverse light vector and each vertex' normal vector to
			// interpolate between the lightened color value and the original color value
			float dotA = VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[]{vnAX, vnAY, vnAZ}), inverseLightDirection);
			dotA = Math.max(dotA, 0);
			color1HSL[2] = (int) HDUtils.lerp(color1HSL[2], lightenA, dotA);

			int lightenB = (int) (Math.max((color2HSL[2] - ignoreLowLightness), 0) * lightnessMultiplier) + baseLighten;
			float dotB = VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[]{vnBX, vnBY, vnBZ}), inverseLightDirection);
			dotB = Math.max(dotB, 0);
			color2HSL[2] = (int) HDUtils.lerp(color2HSL[2], lightenB, dotB);

			int lightenC = (int) (Math.max((color3HSL[2] - ignoreLowLightness), 0) * lightnessMultiplier) + baseLighten;
			float dotC = VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[]{vnCX, vnCY, vnCZ}), inverseLightDirection);
			dotC = Math.max(dotC, 0);
			color3HSL[2] = (int) HDUtils.lerp(color3HSL[2], lightenC, dotC);

			if (faceTextures != null && faceTextures[face] != -1)
			{
				// set textured faces to pure white as they are harder to remove shadows from for some reason
				color1HSL = color2HSL = color3HSL = new int[]{0, 0, 127};
			}

			if (objectProperties != null && objectProperties.isInheritTileColor())
			{
				Tile tile = client.getScene().getTiles()[tileZ][tileX][tileY];

				if (tile != null && (tile.getSceneTilePaint() != null || tile.getSceneTileModel() != null))
				{
					int[] tileColorHSL;

					if (tile.getSceneTilePaint() != null && tile.getSceneTilePaint().getTexture() == -1)
					{
						// pull any corner color as either one should be OK
						tileColorHSL = HDUtils.colorIntToHSL(tile.getSceneTilePaint().getSwColor());

						// average saturation and lightness
						tileColorHSL[1] =
							(
								tileColorHSL[1] +
									HDUtils.colorIntToHSL(tile.getSceneTilePaint().getSeColor())[1] +
									HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNwColor())[1] +
									HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNeColor())[1]
							) / 4;

						tileColorHSL[2] =
							(
								tileColorHSL[2] +
									HDUtils.colorIntToHSL(tile.getSceneTilePaint().getSeColor())[2] +
									HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNwColor())[2] +
									HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNeColor())[2]
							) / 4;

						color1HSL = color2HSL = color3HSL = tileColorHSL;
					}
					else if (tile.getSceneTileModel() != null && tile.getSceneTileModel().getTriangleTextureId() == null)
					{
						int faceColorIndex = -1;
						for (int i = 0; i < tile.getSceneTileModel().getTriangleColorA().length; i++)
						{
							if (!proceduralGenerator.isOverlayFace(tile, i))
							{
								// get a color from an underlay face as it's generally more desirable
								// than pulling colors from paths and other overlays
								faceColorIndex = i;
								break;
							}
						}

						if (faceColorIndex != -1)
						{
							tileColorHSL = HDUtils.colorIntToHSL(tile.getSceneTileModel().getTriangleColorA()[faceColorIndex]);
							color1HSL = color2HSL = color3HSL = tileColorHSL;
						}
					}
				}
			}

			if (gpuPlugin.configTzhaarHD && objectProperties != null && objectProperties.getTzHaarRecolorType() != TzHaarRecolorType.NONE)
			{
				int[][] reskinnedData = proceduralGenerator.recolorTzHaar(objectProperties, vertexY[triangleA], vertexY[triangleB], vertexY[triangleC], color1HSL, color2HSL, color3HSL, packedAlphaPriority, objectType);
				color1HSL = reskinnedData[0].clone();
				color2HSL = reskinnedData[1].clone();
				color3HSL = reskinnedData[2].clone();
				packedAlphaPriority = reskinnedData[3][0];
			}

			// adjust overly-bright vertex colors to reduce ugly washed-out areas of
			// brightly-colored models
			int maxBrightness = 55;
			if (faceTextures != null && faceTextures[face] != -1)
			{
				maxBrightness = 90;
			}
			color1HSL[2] = Ints.constrainToRange(color1HSL[2], 0, maxBrightness);
			color2HSL[2] = Ints.constrainToRange(color2HSL[2], 0, maxBrightness);
			color3HSL[2] = Ints.constrainToRange(color3HSL[2], 0, maxBrightness);

			color1 = HDUtils.colorHSLToInt(color1HSL);
			color2 = HDUtils.colorHSLToInt(color2HSL);
			color3 = HDUtils.colorHSLToInt(color3HSL);
		}

		if (gpuPlugin.configHdMode)
		{
			if (orientation != 0)
			{
				int x = vnAZ * sin + vnAX * cos >> 16;
				int z = vnAZ * cos - vnAX * sin >> 16;

				vnAX = x;
				vnAZ = z;
			}

			if (orientation != 0)
			{
				int x = vnBZ * sin + vnBX * cos >> 16;
				int z = vnBZ * cos - vnBX * sin >> 16;

				vnBX = x;
				vnBZ = z;
			}

			if (orientation != 0)
			{
				int x = vnCZ * sin + vnCX * cos >> 16;
				int z = vnCZ * cos - vnCX * sin >> 16;

				vnCX = x;
				vnCZ = z;
			}

			normalBuffer.ensureCapacity(12);

			if (objectProperties != null && objectProperties.isFlatNormals())
			{
				normalBuffer.put(0, 0, 0, 0);
				normalBuffer.put(0, 0, 0, 0);
				normalBuffer.put(0, 0, 0, 0);
			}
			else
			{
				normalBuffer.put(vnAX, vnAY, vnAZ, 0);
				normalBuffer.put(vnBX, vnBY, vnBZ, 0);
				normalBuffer.put(vnCX, vnCY, vnCZ, 0);
			}
		}

		vertexBuffer.ensureCapacity(12);

		int aX = vertexX[triangleA];
		int aY = vertexY[triangleA];
		int aZ = vertexZ[triangleA];

		if (orientation != 0)
		{
			int x = aZ * sin + aX * cos >> 16;
			int z = aZ * cos - aX * sin >> 16;

			aX = x;
			aZ = z;
		}

		aX += xOffset;
		aY += yOffset;
		aZ += zOffset;

		vertexBuffer.put(aX, aY, aZ, packedAlphaPriority | color1);

		int bX = vertexX[triangleB];
		int bY = vertexY[triangleB];
		int bZ = vertexZ[triangleB];

		if (orientation != 0)
		{
			int x = bZ * sin + bX * cos >> 16;
			int z = bZ * cos - bX * sin >> 16;

			bX = x;
			bZ = z;
		}

		bX += xOffset;
		bY += yOffset;
		bZ += zOffset;

		vertexBuffer.put(bX, bY, bZ, packedAlphaPriority | color2);

		int cX = vertexX[triangleC];
		int cY = vertexY[triangleC];
		int cZ = vertexZ[triangleC];

		if (orientation != 0)
		{
			int x = cZ * sin + cX * cos >> 16;
			int z = cZ * cos - cX * sin >> 16;

			cX = x;
			cZ = z;
		}

		cX += xOffset;
		cY += yOffset;
		cZ += zOffset;

		vertexBuffer.put(cX, cY, cZ, packedAlphaPriority | color3);

		float[][] u = model.getFaceTextureUCoordinates();
		float[][] v = model.getFaceTextureVCoordinates();
		float[] uf, vf;
		Material material;

		if (faceTextures != null && u != null && v != null && (uf = u[face]) != null && (vf = v[face]) != null)
		{
			int packedTextureData = faceTextures[face] + 1;
			if (gpuPlugin.configHdMode)
			{
				material = Material.getTexture(faceTextures[face]);
				packedTextureData = packTextureData(Material.getIndex(material), false);
			}

			uvBuffer.ensureCapacity(12);
			uvBuffer.put(packedTextureData, uf[0], vf[0], 0f);
			uvBuffer.put(packedTextureData, uf[1], vf[1], 0f);
			uvBuffer.put(packedTextureData, uf[2], vf[2], 0f);
		}
		else if (gpuPlugin.configHdMode && objectProperties != null && objectProperties.getMaterial() != Material.NONE)
		{
			material = gpuPlugin.configObjectTextures ? objectProperties.getMaterial() : Material.NONE;
			int packedTextureData = packTextureData(Material.getIndex(material), false);

			uvBuffer.ensureCapacity(12);

			if (objectProperties.getUvType() == UvType.GROUND_PLANE)
			{
				float aU = (aX % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
				float aV = (aZ % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
				float bU = (bX % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
				float bV = (bZ % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
				float cU = (cX % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;
				float cV = (cZ % Perspective.LOCAL_TILE_SIZE) / (float)Perspective.LOCAL_TILE_SIZE;

				uvBuffer.put(packedTextureData, aU, aV, 0f);
				uvBuffer.put(packedTextureData, bU, bV, 0f);
				uvBuffer.put(packedTextureData, cU, cV, 0f);
			}
			else
			{
				// UvType.GEOMETRY
				uvBuffer.put(packedTextureData, 0f, 0f, 0f);
				uvBuffer.put(packedTextureData, 1f, 0f, 0f);
				uvBuffer.put(packedTextureData, 0f, 1f, 0f);
			}
		}
		else if (padUvs || faceTextures != null)
		{
			uvBuffer.ensureCapacity(12);
			uvBuffer.put(0, 0f, 0f, 0f);
			uvBuffer.put(0, 0f, 0f, 0f);
			uvBuffer.put(0, 0f, 0f, 0f);
		}

		return 3;
	}

	private static int packAlphaPriority(short[] faceTextures, byte[] faceTransparencies, byte[] facePriorities, int face)
	{
		int alpha = 0;
		if (faceTransparencies != null && (faceTextures == null || faceTextures[face] == -1))
		{
			alpha = (faceTransparencies[face] & 0xFF) << 24;
		}
		int priority = 0;
		if (facePriorities != null)
		{
			priority = (facePriorities[face] & 0xff) << 16;
		}
		return alpha | priority;
	}

	private int packTextureData(int texture, boolean isOverlay)
	{
		if (gpuPlugin.configHdMode)
		{
			return texture << 1 | (isOverlay ? 1 : 0);
		}
		else
		{
			return texture + 1;
		}
	}
}
