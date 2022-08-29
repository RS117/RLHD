package rs117.hd.data.area;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import rs117.hd.HdPlugin;
import rs117.hd.data.WaterType;
import rs117.hd.data.area.effects.LargeTile;
import rs117.hd.data.area.effects.TileData;
import rs117.hd.data.materials.GroundMaterial;
import rs117.hd.data.materials.Material;
import rs117.hd.scene.SceneUploader;
import rs117.hd.utils.buffer.GpuFloatBuffer;
import rs117.hd.utils.buffer.GpuIntBuffer;

import javax.inject.Inject;
import java.util.List;

public class TileManager {
    private static final ListMultimap<Integer, TileData> GROUND_MATERIAL_MAP_OVERLAY = ArrayListMultimap.create();
    private static final ListMultimap<Integer, TileData> GROUND_MATERIAL_MAP_UNDERLAY = ArrayListMultimap.create();
    private static final TileData DEFAULT = new TileData(-1, GroundMaterial.DIRT);
    public static final TileData WINTER_GRASS = new TileData(-999,
            GroundMaterial.SNOW_1,
            0,
            0,
            40,
            true
    );
    public static final TileData WINTER_DIRT = new TileData(-999,
            GroundMaterial.DIRT,
            0,
            0,
            40,
            true
    );

    @Inject
    private HdPlugin plugin;

    public void loadOverlays() {
        GROUND_MATERIAL_MAP_OVERLAY.clear();
        GROUND_MATERIAL_MAP_UNDERLAY.clear();
        plugin.getAreaManager().areas.stream().filter(it -> !it.getOverlays().isEmpty()).forEach(overlays -> overlays.getOverlays().forEach(overlay -> GROUND_MATERIAL_MAP_OVERLAY.put(overlay.getId(), overlay)));
        plugin.getAreaManager().areas.stream().filter(it -> !it.getUnderlays().isEmpty()).forEach(underlays -> underlays.getUnderlays().forEach(underlay -> GROUND_MATERIAL_MAP_UNDERLAY.put(underlay.getId(), underlay)));
    }

    public TileData getTile(int id, Tile tile, Client client, boolean underlay)
    {
        WorldPoint worldPoint = tile.getWorldLocation();

        if (client.isInInstancedRegion())
        {
            LocalPoint localPoint = tile.getLocalLocation();
            worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
        }

        List<TileData> tileData = underlay ? GROUND_MATERIAL_MAP_UNDERLAY.get(id) : GROUND_MATERIAL_MAP_OVERLAY.get(id);

        final WorldPoint finalWorldPoint = worldPoint;

        return tileData.stream().filter(data ->
                data.getArea().containsPoint(finalWorldPoint)
        ).findFirst().orElse(DEFAULT);

    }

    @Inject
    private SceneUploader sceneUploader;

    public void renderFakeTile(GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer) {
        LargeTile tile = plugin.getAreaManager().getCurrentArea().getLargeTile();
        int color = 127;
        int size = 10000 * Perspective.LOCAL_TILE_SIZE;
        int height = 0;

        if(tile.getMaterialBelow() != null) {
            int materialData = sceneUploader.modelPusher.packMaterialData(Material.getIndex(Material.DIRT_1), false);
            int terrainData = sceneUploader.packTerrainData(600, WaterType.WATER, 0);

            vertexBuffer.put(-size, height, size, color);
            uvBuffer.put(materialData, -size, size, 0);
            // South-west
            vertexBuffer.put(-size, height, -size, color);
            uvBuffer.put(materialData, -size, -size, 0);
            // North-east
            vertexBuffer.put(size, height, size, color);
            uvBuffer.put(materialData, size, size, 0);
            // South-west
            vertexBuffer.put(-size, height, -size, color);
            uvBuffer.put(materialData, -size, -size, 0);
            // South-east
            vertexBuffer.put(size, height, -size, color);
            uvBuffer.put(materialData, size, -size, 0);
            // North-east
            vertexBuffer.put(size, height, size, color);
            uvBuffer.put(materialData, size, size, 0);
            for (int i = 0; i < 6; i++) {
                normalBuffer.put(0, 1, 0, terrainData);
            }
        }

        int materialData = sceneUploader.modelPusher.packMaterialData(Material.getIndex(Material.valueOf(tile.getMaterial())), true);
        int terrainData = sceneUploader.packTerrainData(0, WaterType.valueOf(tile.getWaterType()), 0);

        // North-west
        vertexBuffer.put(-size, height, size, color);
        uvBuffer.put(materialData, -size, size, 0);
        // South-west
        vertexBuffer.put(-size, height, -size, color);
        uvBuffer.put(materialData, -size, -size, 0);
        // North-east
        vertexBuffer.put(size, height, size, color);
        uvBuffer.put(materialData, size, size, 0);
        // South-west
        vertexBuffer.put(-size, height, -size, color);
        uvBuffer.put(materialData, -size, -size, 0);
        // South-east
        vertexBuffer.put(size, height, -size, color);
        uvBuffer.put(materialData, size, -size, 0);
        // North-east
        vertexBuffer.put(size, height, size, color);
        uvBuffer.put(materialData, size, size, 0);

        for (int i = 0; i < 6; i++) {
            normalBuffer.put(0, 1, 0, terrainData);
        }

    }

}
