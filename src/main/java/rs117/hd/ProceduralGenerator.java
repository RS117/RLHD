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

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.jogamp.opengl.math.VectorUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import rs117.hd.config.WaterEffects;
import rs117.hd.materials.GroundMaterial;
import rs117.hd.materials.Material;
import rs117.hd.materials.ObjectProperties;
import rs117.hd.materials.Overlay;
import rs117.hd.materials.TzHaarRecolorType;
import rs117.hd.materials.Underlay;

@Slf4j
@Singleton
class ProceduralGenerator
{
	@Inject
	private Client client;
	
	@Inject
	private HdPlugin hdPlugin;

	private final int VERTICES_PER_FACE = 3;

	// terrain data
	Map<Integer, Integer> vertexTerrainColor;
	Map<Integer, Material> vertexTerrainTexture;
	Map<Integer, float[]> vertexTerrainNormals;
	// used for overriding potentially low quality vertex colors
	HashMap<Integer, Boolean> highPriorityColor;

	// water-related data
	boolean[][][] tileIsWater;
	Map<Integer, Boolean> vertexIsWater;
	Map<Integer, Boolean> vertexIsLand;
	Map<Integer, Boolean> vertexIsOverlay;
	Map<Integer, Boolean> vertexIsUnderlay;
	boolean[][][] skipTile;
	Map<Integer, Integer> vertexUnderwaterDepth;
	int[][][] underwaterDepthLevels;
	int[] depthLevelSlope = new int[]{150, 300, 470, 610, 700, 750, 820, 920, 1080, 1300, 1350, 1380};

	/**
	 * Iterates through all Tiles in a given Scene, producing color and
	 * material data for each vertex of each Tile. Then adds the resulting
	 * data to appropriate HashMaps.
	 *
	 * @param scene
	 */
	void generateTerrainData(Scene scene)
	{
		vertexTerrainColor = new HashMap<>();
		// used for overriding potentially undesirable vertex colors
		// for example, colors that aren't supposed to be visible
		highPriorityColor = new HashMap<>();
		vertexTerrainTexture = new HashMap<>();
		// for faces without an overlay is set to true
		vertexIsUnderlay = new HashMap<>();
		// for faces with an overlay is set to true
		// the result of these maps can be used to determine the vertices
		// between underlays and overlays for custom blending
		vertexIsOverlay = new HashMap<>();
		Tile[][][] tiles = scene.getTiles();

		// first loop - assign land colors and textures to hashmap
		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				for (int y = 0; y < Constants.SCENE_SIZE; ++y)
				{
					if (tiles[z][x][y] != null)
					{
						if (tiles[z][x][y].getBridge() != null)
						{
							generateDataForTile(tiles[z][x][y].getBridge());
						}
						generateDataForTile(tiles[z][x][y]);
					}
				}
			}
		}
	}

	/**
	 * Produces color and material data for the vertices of the provided Tile.
	 * Then adds the resulting data to appropriate HashMaps.
	 *
	 * @param tile
	 */
	void generateDataForTile(Tile tile)
	{
		int faceCount;
		if (tile.getSceneTilePaint() != null)
		{
			faceCount = 2;
		}
		else if (tile.getSceneTileModel() != null)
		{
			faceCount = tile.getSceneTileModel().getFaceX().length;
		}
		else
		{
			return;
		}

		int[] vertexHashes = new int[faceCount * VERTICES_PER_FACE];
		int[] vertexColors = new int[faceCount * VERTICES_PER_FACE];
		int[] vertexOverlays = new int[faceCount * VERTICES_PER_FACE];
		int[] vertexUnderlays = new int[faceCount * VERTICES_PER_FACE];
		boolean[] vertexDefaultColor = new boolean[faceCount * VERTICES_PER_FACE];

		int z = tile.getRenderLevel();
		int x = tile.getSceneLocation().getX();
		int y = tile.getSceneLocation().getY();
		int worldX = tile.getWorldLocation().getX();
		int worldY = tile.getWorldLocation().getY();

		if (tile.getSceneTilePaint() != null)
		{
			// tile paint

			if (tileWaterType(tile, tile.getSceneTilePaint()) != WaterType.NONE)
			{
				// skip water tiles
				return;
			}

			int swColor = tile.getSceneTilePaint().getSwColor();
			int seColor = tile.getSceneTilePaint().getSeColor();
			int nwColor = tile.getSceneTilePaint().getNwColor();
			int neColor = tile.getSceneTilePaint().getNeColor();

			vertexHashes = tileVertexKeys(tile);

			if (x >= Constants.SCENE_SIZE - 2 && y >= Constants.SCENE_SIZE - 2)
			{
				// reduce the black scene edges by assigning surrounding colors
				neColor = swColor;
				nwColor = swColor;
				seColor = swColor;
			}
			else if (y >= Constants.SCENE_SIZE - 2)
			{
				nwColor = swColor;
				neColor = seColor;
			}
			else if (x >= Constants.SCENE_SIZE - 2)
			{
				neColor = nwColor;
				seColor = swColor;
			}

			vertexColors[0] = swColor;
			vertexColors[1] = seColor;
			vertexColors[2] = nwColor;
			vertexColors[3] = neColor;

			vertexOverlays[0] = vertexOverlays[1] = vertexOverlays[2] = vertexOverlays[3] = client.getScene().getOverlayIds()[z][x][y];
			vertexUnderlays[0] = vertexUnderlays[1] = vertexUnderlays[2] = vertexUnderlays[3] = client.getScene().getUnderlayIds()[z][x][y];
			if (useDefaultColor(tile))
			{
				vertexDefaultColor[0] = vertexDefaultColor[1] =vertexDefaultColor[2] =vertexDefaultColor[3] = true;
			}
		}
		else if (tile.getSceneTileModel() != null)
		{
			// tile model

			SceneTileModel sceneTileModel = tile.getSceneTileModel();

			final int[] faceColorsA = sceneTileModel.getTriangleColorA();
			final int[] faceColorsB = sceneTileModel.getTriangleColorB();
			final int[] faceColorsC = sceneTileModel.getTriangleColorC();

			for (int face = 0; face < faceCount; face++)
			{
				int[] faceColors = new int[]{faceColorsA[face], faceColorsB[face], faceColorsC[face]};

				int[] vertexKeys = faceVertexKeys(tile, face);

				for (int vertex = 0; vertex < VERTICES_PER_FACE; vertex++)
				{
					if (faceWaterType(tile, face, sceneTileModel) != WaterType.NONE)
					{
						// skip water faces
						continue;
					}

					vertexHashes[face * VERTICES_PER_FACE + vertex] = vertexKeys[vertex];

					int color = faceColors[vertex];
					vertexColors[face * VERTICES_PER_FACE + vertex] = color;

					if (isOverlayFace(tile, face))
					{
						vertexOverlays[face * VERTICES_PER_FACE + vertex] = client.getScene().getOverlayIds()[z][x][y];
					}
					vertexUnderlays[face * VERTICES_PER_FACE + vertex] = client.getScene().getUnderlayIds()[z][x][y];

					if (isOverlayFace(tile, face) && useDefaultColor(tile))
					{
						vertexDefaultColor[face * VERTICES_PER_FACE + vertex] = true;
					}
				}
			}
		}

		for (int vertex = 0; vertex < vertexHashes.length; vertex++)
		{
			if (vertexHashes[vertex] == 0)
			{
				continue;
			}
			if (vertexColors[vertex] < 0 || vertexColors[vertex] > 65535)
			{
				// skip invalid tile color
				continue;
			}
			boolean lowPriorityColor = false;
			// if this vertex already has a 'high priority' color assigned
			// skip assigning a 'low priority' color unless there is no color assigned
			if (vertexColors[vertex] <= 2)
			{
				// near-solid-black tiles that are used in some places under wall objects
				lowPriorityColor = true;
			}

			int[] colorHSL = HDUtils.colorIntToHSL(vertexColors[vertex]);

			float[] inverseLightDirection = VectorUtil.normalizeVec3(new float[]{1.0f, 1.0f, 0.0f});

			float lightenMultiplier = 1.5f;
			int lightenBase = 15;
			int lightenAdd = 3;
			float darkenMultiplier = 0.5f;
			int darkenBase = 0;
			int darkenAdd = 0;

			float[] vNormals = vertexTerrainNormals.getOrDefault(vertexHashes[vertex], new float[]{0.0f, 0.0f, 0.0f});

			float dot = VectorUtil.dotVec3(VectorUtil.normalizeVec3(vNormals), inverseLightDirection);
			int lighten = (int) (Math.max((colorHSL[2] - lightenAdd), 0) * lightenMultiplier) + lightenBase;
			colorHSL[2] = (int) HDUtils.lerp(colorHSL[2], lighten, Math.max(dot, 0));
			int darken = (int) (Math.max((colorHSL[2] - darkenAdd), 0) * darkenMultiplier) + darkenBase;
			colorHSL[2] = (int) HDUtils.lerp(colorHSL[2], darken, Math.abs(Math.min(dot, 0)));
			colorHSL[2] *= 1.25f;

			boolean isOverlay = false;
			Material material = Material.DIRT_1;
			if (vertexOverlays[vertex] != 0)
			{
				Overlay overlay = Overlay.getOverlay(vertexOverlays[vertex], tile, client);
				GroundMaterial groundMaterial = overlay.getGroundMaterial();
				material = groundMaterial.getRandomMaterial(z, worldX, worldY);
				isOverlay = !overlay.isBlendedAsUnderlay();
				colorHSL = recolorOverlay(overlay, colorHSL);
			}
			else if (vertexUnderlays[vertex] != 0)
			{
				Underlay underlay = Underlay.getUnderlay(vertexUnderlays[vertex], tile, client);
				GroundMaterial groundMaterial = underlay.getGroundMaterial();
				material = groundMaterial.getRandomMaterial(z, worldX, worldY);
				isOverlay = underlay.isBlendedAsOverlay();
				colorHSL = recolorUnderlay(underlay, colorHSL);
			}

			final int maxBrightness = 55; // reduces overexposure
			colorHSL[2] = Ints.constrainToRange(colorHSL[2], 0, maxBrightness);
			vertexColors[vertex] = HDUtils.colorHSLToInt(colorHSL);

			// mark the vertex as either an overlay or underlay.
			// this is used to determine how to blend between vertex colors
			if (isOverlay)
			{
				vertexIsOverlay.put(vertexHashes[vertex], true);
			}
			else
			{
				vertexIsUnderlay.put(vertexHashes[vertex], true);
			}

			// add color and texture to hashmap
			if ((!lowPriorityColor || !highPriorityColor.containsKey(vertexHashes[vertex])) && !vertexDefaultColor[vertex])
			{
				if (vertexOverlays[vertex] != 0 || !vertexTerrainColor.containsKey(vertexHashes[vertex]) || !highPriorityColor.containsKey(vertexHashes[vertex]))
				{
					vertexTerrainColor.put(vertexHashes[vertex], vertexColors[vertex]);
				}
				if (vertexOverlays[vertex] != 0 || !vertexTerrainTexture.containsKey(vertexHashes[vertex]) || !highPriorityColor.containsKey(vertexHashes[vertex]))
				{
					vertexTerrainTexture.put(vertexHashes[vertex], material);
				}
				if (!lowPriorityColor)
				{
					highPriorityColor.put(vertexHashes[vertex], true);
				}
			}
		}
	}

	/**
	 * Generates underwater terrain data by iterating through all Tiles in a given
	 * Scene, increasing the depth of each tile based on its distance from the shore.
	 * Then stores the resulting data in a HashMap.
	 *
	 * @param scene
	 */
	void generateUnderwaterTerrain(Scene scene)
	{
		// true if a tile contains at least 1 face which qualifies as water
		tileIsWater = new boolean[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];
		// true if a vertex is part of a face which qualifies as water; non-existent if not
		vertexIsWater = new HashMap<>();
		// true if a vertex is part of a face which qualifies as land; non-existent if not
		// tiles along the shoreline will be true for both vertexIsWater and vertexIsLand
		vertexIsLand = new HashMap<>();
		// if true, the tile will be skipped when the scene is drawn
		// this is due to certain edge cases with water on the same X/Y on different planes
		skipTile = new boolean[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];
		// the height adjustment for each vertex, to be applied to the vertex'
		// real height to create the underwater terrain
		vertexUnderwaterDepth = new HashMap<>();
		// the basic 'levels' of underwater terrain, used to sink terrain based on its distance
		// from the shore, then used to produce the world-space height offset
		// 0 = land
		underwaterDepthLevels = new int[Constants.MAX_Z][Constants.SCENE_SIZE + 1][Constants.SCENE_SIZE + 1];
		// the world-space height offsets of each vertex on the tile grid
		// these offsets are interpolated to calculate offsets for vertices not on the grid (tilemodels)
		final int[][][] underwaterDepths = new int[Constants.MAX_Z][Constants.SCENE_SIZE + 1][Constants.SCENE_SIZE + 1];

		if (hdPlugin.configWaterEffects == WaterEffects.SIMPLE)
		{
			return;
		}

		Tile[][][] tiles = scene.getTiles();
		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				// set the array to 1 initially
				// this assumes that all vertices are water;
				// we will set non-water vertices to 0 in the next loop
				Arrays.fill(underwaterDepthLevels[z][x], 1);
			}
		}

		// figure out which vertices are water and assign some data
		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				for (int y = 0; y < Constants.SCENE_SIZE; ++y)
				{
					if (tiles[z][x][y] == null)
					{
						underwaterDepthLevels[z][x][y] = 0;
						underwaterDepthLevels[z][x+1][y] = 0;
						underwaterDepthLevels[z][x][y+1] = 0;
						underwaterDepthLevels[z][x+1][y+1] = 0;
						continue;
					}

					Tile tile = tiles[z][x][y];
					if (tile.getBridge() != null)
					{
						tile = tile.getBridge();
					}
					if (tile.getSceneTilePaint() != null)
					{
						int[] vertexKeys = tileVertexKeys(tile);

						if (tileWaterType(tile, tile.getSceneTilePaint()) == WaterType.NONE)
						{
							for (int vertexKey : vertexKeys)
							{
								if (tile.getSceneTilePaint().getNeColor() != 12345678)
								{
									vertexIsLand.put(vertexKey, true);
								}
							}

							underwaterDepthLevels[z][x][y] = 0;
							underwaterDepthLevels[z][x+1][y] = 0;
							underwaterDepthLevels[z][x][y+1] = 0;
							underwaterDepthLevels[z][x+1][y+1] = 0;
						}
						else
						{
							// Stop tiles on the same X,Y coordinates on different planes from
							// each generating water. Prevents undesirable results in certain places.
							if (z > 0)
							{
								boolean continueLoop = false;

								for (int checkZ = 0; checkZ < z; ++checkZ)
								{
									if (tileIsWater[checkZ][x][y])
									{
										underwaterDepthLevels[z][x][y] = 0;
										underwaterDepthLevels[z][x+1][y] = 0;
										underwaterDepthLevels[z][x][y+1] = 0;
										underwaterDepthLevels[z][x+1][y+1] = 0;

										skipTile[z][x][y] = true;

										continueLoop = true;

										break;
									}
								}

								if (continueLoop)
									continue;
							}

							tileIsWater[z][x][y] = true;

							for (int vertexKey : vertexKeys)
							{
								vertexIsWater.put(vertexKey, true);
							}
						}
					}
					else if (tile.getSceneTileModel() != null)
					{
						SceneTileModel sceneTileModel = tile.getSceneTileModel();

						int faceCount = sceneTileModel.getFaceX().length;

						// Stop tiles on the same X,Y coordinates on different planes from
						// each generating water. Prevents undesirable results in certain places.
						if (z > 0)
						{
							boolean tileIncludesWater = false;

							for (int face = 0; face < faceCount; face++)
							{
								if (faceWaterType(tile, face, sceneTileModel) != WaterType.NONE)
								{
									tileIncludesWater = true;
									break;
								}
							}

							if (tileIncludesWater)
							{
								boolean continueLoop = false;

								for (int checkZ = 0; checkZ < z; ++checkZ)
								{
									if (tileIsWater[checkZ][x][y])
									{
										underwaterDepthLevels[z][x][y] = 0;
										underwaterDepthLevels[z][x+1][y] = 0;
										underwaterDepthLevels[z][x][y+1] = 0;
										underwaterDepthLevels[z][x+1][y+1] = 0;

										skipTile[z][x][y] = true;

										continueLoop = true;

										break;
									}
								}

								if (continueLoop)
									continue;
							}
						}

						for (int face = 0; face < faceCount; face++)
						{
							int[][] vertices = faceVertices(tile, face);
							int[] vertexKeys = faceVertexKeys(tile, face);

							if (faceWaterType(tile, face, sceneTileModel) == WaterType.NONE)
							{
								for (int vertex = 0; vertex < VERTICES_PER_FACE; vertex++)
								{
									if (sceneTileModel.getTriangleColorA()[face] != 12345678)
									{
										vertexIsLand.put(vertexKeys[vertex], true);
									}

									if (vertices[vertex][0] % Perspective.LOCAL_TILE_SIZE == 0 && vertices[vertex][1] % Perspective.LOCAL_TILE_SIZE == 0)
									{
										int vX = vertices[vertex][0] / Perspective.LOCAL_TILE_SIZE;
										int vY = vertices[vertex][1] / Perspective.LOCAL_TILE_SIZE;

										underwaterDepthLevels[z][vX][vY] = 0;
									}
								}
							}
							else
							{
								tileIsWater[z][x][y] = true;

								for (int vertex = 0; vertex < VERTICES_PER_FACE; vertex++)
								{
									vertexIsWater.put(vertexKeys[vertex], true);
								}
							}
						}
					}
					else
					{
						underwaterDepthLevels[z][x][y] = 0;
						underwaterDepthLevels[z][x+1][y] = 0;
						underwaterDepthLevels[z][x][y+1] = 0;
						underwaterDepthLevels[z][x+1][y+1] = 0;
					}
				}
			}
		}

		// Sink terrain further from shore by desired levels.
		for (int level = 0; level < depthLevelSlope.length - 1; level++)
		{
			for (int z = 0; z < Constants.MAX_Z; ++z)
			{
				for (int x = 0; x < underwaterDepthLevels[z].length; x++)
				{
					for (int y = 0; y < underwaterDepthLevels[z][x].length; y++)
					{
						if (underwaterDepthLevels[z][x][y] == 0)
						{
							// Skip the tile if it isn't water.
							continue;
						}
						// If it's on the edge of the scene, reset the depth so
						// it creates a 'wall' to prevent fog from passing through.
						// Not incredibly effective, but better than nothing.
						if (x == 0 || y == 0 || x == Constants.SCENE_SIZE || y == Constants.SCENE_SIZE)
						{
							underwaterDepthLevels[z][x][y] = 0;
							continue;
						}

						int tileHeight = underwaterDepthLevels[z][x][y];
						if (underwaterDepthLevels[z][x - 1][y] < tileHeight)
						{
							// West
							continue;
						}
						if (x < underwaterDepthLevels[z].length - 1 && underwaterDepthLevels[z][x + 1][y] < tileHeight)
						{
							// East
							continue;
						}
						if (underwaterDepthLevels[z][x][y - 1] < tileHeight)
						{
							// South
							continue;
						}
						if (y < underwaterDepthLevels[z].length - 1 && underwaterDepthLevels[z][x][y + 1] < tileHeight)
						{
							// North
							continue;
						}
						// At this point, it's surrounded only by other depth-adjusted vertices.
						underwaterDepthLevels[z][x][y]++;
					}
				}
			}
		}

		// Adjust the height levels to world coordinate offsets and add to an array.
		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			for (int x = 0; x < underwaterDepthLevels[z].length; x++)
			{
				for (int y = 0; y < underwaterDepthLevels[z][x].length; y++)
				{
					if (underwaterDepthLevels[z][x][y] == 0)
					{
						continue;
					}
					int maxRange = depthLevelSlope[underwaterDepthLevels[z][x][y] - 1];
					int minRange = (int) (depthLevelSlope[underwaterDepthLevels[z][x][y] - 1] * 0.1f);
					// Range from noise-generated terrain is 10-60.
					// Translate the result from range 0-1.
//					float noiseOffset = (HeightCalc.calculate(baseX + x + 0xe3b7b, baseY + y + 0x87cce) - 10) / 50f;
					float noiseOffset = 0.5f;
					// limit range of variation
					float minOffset = 0.25f;
					float maxOffset = 0.75f;
					noiseOffset = HDUtils.lerp(minOffset, maxOffset, noiseOffset);
					// apply offset to vertex height range
					int heightOffset = (int) HDUtils.lerp(minRange, maxRange, noiseOffset);
					underwaterDepths[z][x][y] = heightOffset;
				}
			}
		}

		// Store the height offsets in a hashmap and calculate interpolated
		// height offsets for non-corner vertices.
		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				for (int y = 0; y < Constants.SCENE_SIZE; ++y)
				{
					if (!tileIsWater[z][x][y])
					{
						continue;
					}
					Tile tile = tiles[z][x][y];
					if (tile.getBridge() != null)
					{
						tile = tile.getBridge();
					}
					if (tile.getSceneTilePaint() != null)
					{
						int[] vertexKeys = tileVertexKeys(tile);

						int swVertexKey = vertexKeys[0];
						int seVertexKey = vertexKeys[1];
						int nwVertexKey = vertexKeys[2];
						int neVertexKey = vertexKeys[3];

						vertexUnderwaterDepth.put(swVertexKey, underwaterDepths[z][x][y]);
						vertexUnderwaterDepth.put(seVertexKey, underwaterDepths[z][x + 1][y]);
						vertexUnderwaterDepth.put(nwVertexKey, underwaterDepths[z][x][y + 1]);
						vertexUnderwaterDepth.put(neVertexKey, underwaterDepths[z][x + 1][y + 1]);
					}
					else if (tile.getSceneTileModel() != null)
					{
						SceneTileModel sceneTileModel = tile.getSceneTileModel();

						int faceCount = sceneTileModel.getFaceX().length;

						for (int face = 0; face < faceCount; face++)
						{
							int[][] vertices = faceVertices(tile, face);
							int[] vertexKeys = faceVertexKeys(tile, face);

							for (int vertex = 0; vertex < VERTICES_PER_FACE; vertex++)
							{
								if (vertices[vertex][0] % Perspective.LOCAL_TILE_SIZE == 0 && vertices[vertex][1] % Perspective.LOCAL_TILE_SIZE == 0)
								{
									// The vertex is at the corner of the tile;
									// simply use the offset in the tile grid array.

									int vX = vertices[vertex][0] / Perspective.LOCAL_TILE_SIZE;
									int vY = vertices[vertex][1] / Perspective.LOCAL_TILE_SIZE;

									vertexUnderwaterDepth.put(vertexKeys[vertex], underwaterDepths[z][vX][vY]);
								}
								else
								{
									// If the tile is a tile model and this vertex is shared only by faces that are water,
									// interpolate between the height offsets at each corner to get the height offset
									// of the vertex.

									int localVertexX = vertices[vertex][0] - (x * Perspective.LOCAL_TILE_SIZE);
									int localVertexY = vertices[vertex][1] - (y * Perspective.LOCAL_TILE_SIZE);
									float lerpX = (float) localVertexX / (float) Perspective.LOCAL_TILE_SIZE;
									float lerpY = (float) localVertexY / (float) Perspective.LOCAL_TILE_SIZE;
									float northHeightOffset = HDUtils.lerp(underwaterDepths[z][x][y+1], underwaterDepths[z][x+1][y+1], lerpX);
									float southHeightOffset = HDUtils.lerp(underwaterDepths[z][x][y], underwaterDepths[z][x+1][y], lerpX);
									int heightOffset = (int) HDUtils.lerp(southHeightOffset, northHeightOffset, lerpY);

									if (!vertexIsLand.containsKey(vertexKeys[vertex]))
									{
										vertexUnderwaterDepth.put(vertexKeys[vertex], heightOffset);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Iterates through all Tiles in a given Scene, calculating vertex normals
	 * for each one, then stores resulting normal data in a HashMap.
	 *
	 * @param scene
	 */
	void calculateTerrainNormals(Scene scene)
	{
		vertexTerrainNormals = new HashMap<>();
		Tile[][][] tiles = scene.getTiles();

		for (int tileZ = 0; tileZ < tiles.length; tileZ++)
		{
			for (int tileX = 0; tileX < tiles[tileZ].length; tileX++)
			{
				for (int tileY = 0; tileY < tiles[tileZ][tileX].length; tileY++)
				{
					if (tiles[tileZ][tileX][tileY] != null)
					{
						boolean isBridge = false;

						if (tiles[tileZ][tileX][tileY].getBridge() != null)
						{
							calculateNormalsForTile(tiles[tileZ][tileX][tileY].getBridge(), false);
							isBridge = true;
						}
						calculateNormalsForTile(tiles[tileZ][tileX][tileY], isBridge);
					}
				}
			}
		}
	}

	/**
	 * Calculates vertex normals for a given Tile,
	 * then stores resulting normal data in a HashMap.
	 *
	 * @param tile
	 * @param isBridge
	 */
	void calculateNormalsForTile(Tile tile, boolean isBridge)
	{
		// Make array of tile's tris with vertices
		int[][][] faceVertices; // Array of tile's tri vertices
		int[][] faceVertexKeys;

		if (tile.getSceneTileModel() != null)
		{
			// Tile model
			SceneTileModel tileModel = tile.getSceneTileModel();
			faceVertices = new int[tileModel.getFaceX().length][VERTICES_PER_FACE][3];
			faceVertexKeys = new int[tileModel.getFaceX().length][VERTICES_PER_FACE];

			for (int face = 0; face < tileModel.getFaceX().length; face++)
			{
				int[][] vertices = faceVertices(tile, face);

				faceVertices[face][0] = new int[]{vertices[0][0], vertices[0][1], vertices[0][2]};
				faceVertices[face][2] = new int[]{vertices[1][0], vertices[1][1], vertices[1][2]};
				faceVertices[face][1] = new int[]{vertices[2][0], vertices[2][1], vertices[2][2]};

				int[] vertexKeys = faceVertexKeys(tile, face);
				faceVertexKeys[face][0] = vertexKeys[0];
				faceVertexKeys[face][2] = vertexKeys[1];
				faceVertexKeys[face][1] = vertexKeys[2];
			}
		}
		else
		{
			faceVertices = new int[2][VERTICES_PER_FACE][3];
			faceVertexKeys = new int[VERTICES_PER_FACE][3];
			int[][] vertices = tileVertices(tile);
			faceVertices[0] = new int[][]{vertices[3], vertices[1], vertices[2]};
			faceVertices[1] = new int[][]{vertices[0], vertices[2], vertices[1]};

			int[] vertexKeys = tileVertexKeys(tile);
			faceVertexKeys[0] = new int[]{vertexKeys[3], vertexKeys[1], vertexKeys[2]};
			faceVertexKeys[1] = new int[]{vertexKeys[0], vertexKeys[2], vertexKeys[1]};
		}

		// Loop through tris to calculate and accumulate normals
		for (int face = 0; face < faceVertices.length; face++)
		{
			// XYZ
			int[] vertexHeights = new int[]{faceVertices[face][0][2], faceVertices[face][1][2], faceVertices[face][2][2]};
			if (!isBridge)
			{
				vertexHeights[0] += vertexUnderwaterDepth.getOrDefault(faceVertexKeys[face][0], 0);
				vertexHeights[1] += vertexUnderwaterDepth.getOrDefault(faceVertexKeys[face][1], 0);
				vertexHeights[2] += vertexUnderwaterDepth.getOrDefault(faceVertexKeys[face][2], 0);
			}

			float[] vertexNormals = HDUtils.calculateSurfaceNormals(
				// Vertex Xs
				new int[]{faceVertices[face][0][0], faceVertices[face][1][0], faceVertices[face][2][0]},
				// Vertex Ys
				new int[]{faceVertices[face][0][1], faceVertices[face][1][1], faceVertices[face][2][1]},
				// Vertex Zs
				new int[]{vertexHeights[0], vertexHeights[1], vertexHeights[2]}
			);

			for (int vertex = 0; vertex < VERTICES_PER_FACE; vertex++)
			{
				int vertexKey = faceVertexKeys[face][vertex];
				// accumulate normals to hashmap
				vertexTerrainNormals.merge(vertexKey, vertexNormals, (a, b) -> HDUtils.vectorAdd(b, a));
			}
		}
	}

	/**
	 * Returns the WaterType of the provided SceneTilePaint Tile.
	 *
	 * @param tile
	 * @return the WaterType of the specified Tile
	 */
	WaterType tileWaterType(Tile tile, SceneTilePaint sceneTilePaint)
	{
		if (tile.getBridge() != null)
		{
			return WaterType.NONE;
		}
		int tileZ = tile.getRenderLevel();
		int tileX = tile.getSceneLocation().getX();
		int tileY = tile.getSceneLocation().getY();

		WaterType waterType = WaterType.NONE;

		if (sceneTilePaint != null)
		{
			if (client.getScene().getOverlayIds()[tileZ][tileX][tileY] != 0)
			{
				waterType = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client).getWaterType();
			}
			else
			{
				waterType = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client).getWaterType();
			}
		}

		if (hdPlugin.configWaterEffects == WaterEffects.SIMPLE)
		{
			switch(waterType)
			{
				case WATER:
					waterType = WaterType.WATER_FLAT;
					break;
				case SWAMP_WATER:
					waterType = WaterType.SWAMP_WATER_FLAT;
					break;
				case POISON_WASTE:
					waterType = WaterType.POISON_WASTE_FLAT;
					break;
				case ICE:
					waterType = WaterType.ICE_FLAT;
					break;
			}
		}

		return waterType;
	}

	/**
	 * Returns the WaterType of the provided SceneTileModel Tile's specified face.
	 *
	 * @param tile
	 * @param face the index of the specified face
	 * @return
	 */
	WaterType faceWaterType(Tile tile, int face, SceneTileModel sceneTileModel)
	{
		if (tile.getBridge() != null)
		{
			return WaterType.NONE;
		}
		int tileZ = tile.getRenderLevel();
		int tileX = tile.getSceneLocation().getX();
		int tileY = tile.getSceneLocation().getY();

		WaterType waterType = WaterType.NONE;

		if (sceneTileModel != null)
		{
			if (isOverlayFace(tile, face))
			{
				waterType = Overlay.getOverlay(client.getScene().getOverlayIds()[tileZ][tileX][tileY], tile, client).getWaterType();
			}
			else
			{
				waterType = Underlay.getUnderlay(client.getScene().getUnderlayIds()[tileZ][tileX][tileY], tile, client).getWaterType();
			}
		}

		if (hdPlugin.configWaterEffects == WaterEffects.SIMPLE)
		{
			switch(waterType)
			{
				case WATER:
					waterType = WaterType.WATER_FLAT;
					break;
				case SWAMP_WATER:
					waterType = WaterType.SWAMP_WATER_FLAT;
					break;
				case POISON_WASTE:
					waterType = WaterType.POISON_WASTE_FLAT;
					break;
			}
		}

		return waterType;
	}

	boolean[][] tileOverlayTris = new boolean[][]
		{
			/*  0 */ new boolean[]{true, true, true, true}, // Used by tilemodels of varying tri counts?
			/*  1 */ new boolean[]{false, true},
			/*  2 */ new boolean[]{false, false, true},
			/*  3 */ new boolean[]{false, false, true},
			/*  4 */ new boolean[]{false, true, true},
			/*  5 */ new boolean[]{false, true, true},
			/*  6 */ new boolean[]{false, false, true, true},
			/*  7 */ new boolean[]{false, false, false, true},
			/*  8 */ new boolean[]{false, true, true, true},
			/*  9 */ new boolean[]{false, false, false, true, true, true},
			/* 10 */ new boolean[]{true, true, true, false, false, false},
			/* 11 */ new boolean[]{true, true, false, false, false, false},
		};

	boolean[] getTileOverlayTris(int tileShapeIndex)
	{
		if (tileShapeIndex >= tileOverlayTris.length)
		{
			log.debug("getTileOverlayTris(): unknown tileShapeIndex ({})", tileShapeIndex);
			return new boolean[]{false, false, false, false, false, false, false, false, false, false};
		}
		else
		{
			return tileOverlayTris[tileShapeIndex];
		}
	}

	boolean isOverlayFace(Tile tile, int face)
	{
		int tileShapeIndex = tile.getSceneTileModel().getShape() - 1;
		if (face >= getTileOverlayTris(tileShapeIndex).length)
		{
			return false;
		}
		return getTileOverlayTris(tileShapeIndex)[face];
	}

	int[][] tileVertices(Tile tile)
	{
		int x = tile.getSceneLocation().getX();
		int y = tile.getSceneLocation().getY();
		int z = tile.getRenderLevel();
		int[][][] tileHeights = client.getTileHeights();

		int[] swVertex = new int[]{x * Perspective.LOCAL_TILE_SIZE, y * Perspective.LOCAL_TILE_SIZE, tileHeights[z][x][y]};
		int[] seVertex = new int[]{(x + 1) * Perspective.LOCAL_TILE_SIZE, y * Perspective.LOCAL_TILE_SIZE, tileHeights[z][x + 1][y]};
		int[] nwVertex = new int[]{x * Perspective.LOCAL_TILE_SIZE, (y + 1) * Perspective.LOCAL_TILE_SIZE, tileHeights[z][x][y + 1]};
		int[] neVertex = new int[]{(x + 1) * Perspective.LOCAL_TILE_SIZE, (y + 1) * Perspective.LOCAL_TILE_SIZE, tileHeights[z][x + 1][y + 1]};

		return new int[][]{swVertex, seVertex, nwVertex, neVertex};
	}

	int[][] faceVertices(Tile tile, int face)
	{
		SceneTileModel sceneTileModel = tile.getSceneTileModel();

		final int[] faceA = sceneTileModel.getFaceX();
		final int[] faceB = sceneTileModel.getFaceY();
		final int[] faceC = sceneTileModel.getFaceZ();

		final int[] vertexX = sceneTileModel.getVertexX();
		final int[] vertexY = sceneTileModel.getVertexY();
		final int[] vertexZ = sceneTileModel.getVertexZ();

		int vertexFacesA = faceA[face];
		int vertexFacesB = faceB[face];
		int vertexFacesC = faceC[face];

		// scene X
		int sceneVertexXA = vertexX[vertexFacesA];
		int sceneVertexXB = vertexX[vertexFacesB];
		int sceneVertexXC = vertexX[vertexFacesC];
		// scene Y
		int sceneVertexZA = vertexZ[vertexFacesA];
		int sceneVertexZB = vertexZ[vertexFacesB];
		int sceneVertexZC = vertexZ[vertexFacesC];
		// scene Z - heights
		int sceneVertexYA = vertexY[vertexFacesA];
		int sceneVertexYB = vertexY[vertexFacesB];
		int sceneVertexYC = vertexY[vertexFacesC];

		int[] vertexA = new int[]{sceneVertexXA, sceneVertexZA, sceneVertexYA};
		int[] vertexB = new int[]{sceneVertexXB, sceneVertexZB, sceneVertexYB};
		int[] vertexC = new int[]{sceneVertexXC, sceneVertexZC, sceneVertexYC};

		return new int[][]{vertexA, vertexB, vertexC};
	}

	int[][] tileLocalVertices(Tile tile)
	{
		int x = tile.getSceneLocation().getX();
		int y = tile.getSceneLocation().getY();
		int z = tile.getRenderLevel();
		int[][][] tileHeights = client.getTileHeights();

		int[] swVertex = new int[]{0, 0, tileHeights[z][x][y]};
		int[] seVertex = new int[]{Perspective.LOCAL_TILE_SIZE, 0, tileHeights[z][x + 1][y]};
		int[] nwVertex = new int[]{0, Perspective.LOCAL_TILE_SIZE, tileHeights[z][x][y + 1]};
		int[] neVertex = new int[]{Perspective.LOCAL_TILE_SIZE, Perspective.LOCAL_TILE_SIZE, tileHeights[z][x + 1][y + 1]};

		return new int[][]{swVertex, seVertex, nwVertex, neVertex};
	}

	int[][] faceLocalVertices(Tile tile, int face)
	{
		int x = tile.getSceneLocation().getX();
		int y = tile.getSceneLocation().getY();
		int baseX = x * Perspective.LOCAL_TILE_SIZE;
		int baseY = y * Perspective.LOCAL_TILE_SIZE;

		if (tile.getSceneTileModel() == null)
		{
			return new int[0][0];
		}

		SceneTileModel sceneTileModel = tile.getSceneTileModel();

		final int[] faceA = sceneTileModel.getFaceX();
		final int[] faceB = sceneTileModel.getFaceY();
		final int[] faceC = sceneTileModel.getFaceZ();

		final int[] vertexX = sceneTileModel.getVertexX();
		final int[] vertexY = sceneTileModel.getVertexY();
		final int[] vertexZ = sceneTileModel.getVertexZ();

		int vertexFacesA = faceA[face];
		int vertexFacesB = faceB[face];
		int vertexFacesC = faceC[face];

		// scene X
		int sceneVertexXA = vertexX[vertexFacesA];
		int sceneVertexXB = vertexX[vertexFacesB];
		int sceneVertexXC = vertexX[vertexFacesC];
		// scene Y
		int sceneVertexZA = vertexZ[vertexFacesA];
		int sceneVertexZB = vertexZ[vertexFacesB];
		int sceneVertexZC = vertexZ[vertexFacesC];
		// scene Z - heights
		int sceneVertexYA = vertexY[vertexFacesA];
		int sceneVertexYB = vertexY[vertexFacesB];
		int sceneVertexYC = vertexY[vertexFacesC];

		int[] vertexA = new int[]{sceneVertexXA - baseX, sceneVertexZA - baseY, sceneVertexYA};
		int[] vertexB = new int[]{sceneVertexXB - baseX, sceneVertexZB - baseY, sceneVertexYB};
		int[] vertexC = new int[]{sceneVertexXC - baseX, sceneVertexZC - baseY, sceneVertexYC};

		return new int[][]{vertexA, vertexB, vertexC};
	}

	/**
	 * Gets the vertex keys of a Tile Paint tile for use in retrieving data from hashmaps.
	 *
	 * @param tile
	 * @return Vertex keys in following order: SW, SE, NW, NE
	 */
	int[] tileVertexKeys(Tile tile)
	{
		int[][] tileVertices = tileVertices(tile);
		int[] vertexHashes = new int[tileVertices.length];

		for (int vertex = 0; vertex < tileVertices.length; ++vertex)
		{
			vertexHashes[vertex] = HDUtils.vertexHash(tileVertices[vertex]);
		}

		return vertexHashes;
	}

	int[] faceVertexKeys(Tile tile, int face)
	{
		int[][] faceVertices = faceVertices(tile, face);
		int[] vertexHashes = new int[faceVertices.length];

		for (int vertex = 0; vertex < faceVertices.length; ++vertex)
		{
			vertexHashes[vertex] = HDUtils.vertexHash(faceVertices[vertex]);
		}

		return vertexHashes;
	}

	int[] recolorOverlay(Overlay overlay, int[] colorHSL)
	{
		colorHSL[0] = overlay.getHue() >= 0 ? overlay.getHue() : colorHSL[0];
		colorHSL[0] += overlay.getShiftHue();
		colorHSL[0] = Ints.constrainToRange(colorHSL[0], 0, 63);

		colorHSL[1] = overlay.getSaturation() >= 0 ? overlay.getSaturation() : colorHSL[1];
		colorHSL[1] += overlay.getShiftSaturation();
		colorHSL[1] = Ints.constrainToRange(colorHSL[1], 0, 7);

		colorHSL[2] = overlay.getLightness() >= 0 ? overlay.getLightness() : colorHSL[2];
		colorHSL[2] += overlay.getShiftLightness();
		colorHSL[2] = Ints.constrainToRange(colorHSL[2], 0, 127);

		return colorHSL;
	}

	int[] recolorUnderlay(Underlay underlay, int[] colorHSL)
	{
		colorHSL[0] = underlay.getHue() >= 0 ? underlay.getHue() : colorHSL[0];
		colorHSL[0] += underlay.getShiftHue();
		colorHSL[0] = Ints.constrainToRange(colorHSL[0], 0, 63);

		colorHSL[1] = underlay.getSaturation() >= 0 ? underlay.getSaturation() : colorHSL[1];
		colorHSL[1] += underlay.getShiftSaturation();
		colorHSL[1] = Ints.constrainToRange(colorHSL[1], 0, 7);

		colorHSL[2] = underlay.getLightness() >= 0 ? underlay.getLightness() : colorHSL[2];
		colorHSL[2] += underlay.getShiftLightness();
		colorHSL[2] = Ints.constrainToRange(colorHSL[2], 0, 127);

		return colorHSL;
	}

	boolean useDefaultColor(Tile tile)
	{
		int z = tile.getRenderLevel();
		int x = tile.getSceneLocation().getX();
		int y = tile.getSceneLocation().getY();

		if (!hdPlugin.configGroundBlending || (tile.getSceneTilePaint() != null && tile.getSceneTilePaint().getTexture() >= 0) ||
			(tile.getSceneTileModel() != null && tile.getSceneTileModel().getTriangleTextureId() != null))
		{
			// skip tiles with textures provided by default
			return true;
		}

		if (client.getScene().getOverlayIds()[z][x][y] != 0)
		{
			if (!Overlay.getOverlay(client.getScene().getOverlayIds()[z][x][y], tile, client).isBlended())
			{
				return true;
			}
		}
		else if (client.getScene().getUnderlayIds()[z][x][y] != 0)
		{
			if (!Underlay.getUnderlay(client.getScene().getUnderlayIds()[z][x][y], tile, client).isBlended())
			{
				return true;
			}
		}
		return false;
	}

	int[][] tzHaarRecolored = new int[4][3];
	// used when calculating the gradient to apply to the walls of TzHaar
	// to emulate the style from 2008 HD rework
	final int[] gradientBaseColor = new int[]{3, 4, 26};
	final int[] gradientDarkColor = new int[]{3, 4, 10};
	final int gradientBottom = 200;
	final int gradientTop = -200;

	int[][] recolorTzHaar(ObjectProperties objectProperties, int aY, int bY, int cY, int packedAlphaPriority, ObjectType objectType, int color1H, int color1S, int color1L, int color2H, int color2S, int color2L, int color3H, int color3S, int color3L)
	{
		// recolor tzhaar to look like the 2008+ HD version
		if (objectType == ObjectType.GROUND_OBJECT)
		{
			// remove the black parts of floor objects to allow the ground to show
			// so we can apply textures, ground blending, etc. to it
			if (color1S <= 1)
			{
				packedAlphaPriority = 0xFF << 24;
			}
		}

		// shift model hues from red->yellow
		int hue = 7;
		color1H = hue;
		color2H = hue;
		color3H = hue;

		if (objectProperties.getTzHaarRecolorType() == TzHaarRecolorType.GRADIENT)
		{
			// apply coloring to the rocky walls
			if (color1L < 20)
			{
				float pos = Floats.constrainToRange((float) (aY - gradientTop) / (float) gradientBottom, 0.0f, 1.0f);
				color1H = (int)HDUtils.lerp(gradientDarkColor[0], gradientBaseColor[0], pos);
				color1S = (int)HDUtils.lerp(gradientDarkColor[1], gradientBaseColor[1], pos);
				color1L = (int)HDUtils.lerp(gradientDarkColor[2], gradientBaseColor[2], pos);
			}

			if (color2L < 20)
			{
				float pos = Floats.constrainToRange((float) (bY - gradientTop) / (float) gradientBottom, 0.0f, 1.0f);
				color2H = (int)HDUtils.lerp(gradientDarkColor[0], gradientBaseColor[0], pos);
				color2S = (int)HDUtils.lerp(gradientDarkColor[1], gradientBaseColor[1], pos);
				color2L = (int)HDUtils.lerp(gradientDarkColor[2], gradientBaseColor[2], pos);
			}

			if (color3L < 20)
			{
				float pos = Floats.constrainToRange((float) (cY - gradientTop) / (float) gradientBottom, 0.0f, 1.0f);
				color3H = (int)HDUtils.lerp(gradientDarkColor[0], gradientBaseColor[0], pos);
				color3S = (int)HDUtils.lerp(gradientDarkColor[1], gradientBaseColor[1], pos);
				color3L = (int)HDUtils.lerp(gradientDarkColor[2], gradientBaseColor[2], pos);
			}
		}
		else if (objectProperties.getTzHaarRecolorType() == TzHaarRecolorType.HUE_SHIFT)
		{
			// objects around the entrance to The Inferno only need a hue-shift
			// and very slight lightening to match the lightened terrain
			color1L += 1;
			color2L += 1;
			color3L += 1;
		}

		tzHaarRecolored[0][0] = color1H;
		tzHaarRecolored[0][1] = color1S;
		tzHaarRecolored[0][2] = color1L;
		tzHaarRecolored[1][0] = color2H;
		tzHaarRecolored[1][1] = color2S;
		tzHaarRecolored[1][2] = color2L;
		tzHaarRecolored[2][0] = color3H;
		tzHaarRecolored[2][1] = color3S;
		tzHaarRecolored[2][2] = color3L;
		tzHaarRecolored[3][0] = packedAlphaPriority;

		return tzHaarRecolored;
	}
}
