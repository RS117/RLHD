package rs117.hd.data.area;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Perspective;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import org.apache.commons.lang3.tuple.Pair;
import rs117.hd.HdPlugin;
import rs117.hd.data.WaterType;
import rs117.hd.data.area.effects.LargeTile;
import rs117.hd.data.materials.GroundMaterial;
import rs117.hd.data.materials.Material;
import rs117.hd.scene.SceneUploader;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;
import rs117.hd.utils.Rect;
import rs117.hd.utils.buffer.GpuFloatBuffer;
import rs117.hd.utils.buffer.GpuIntBuffer;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class AreaManager {

    @Getter
    @Setter
    Area currentArea = null;

    @Inject
    private HdPlugin plugin;

    @Inject
    private ClientThread clientThread;

    private Area DEFAULT = null;

    @Inject
    private Client client;

    public static String ENV_AREA_CONFIG = "RLHD_AREA_PATH";
    private FileWatcher fileWatcher;

    @Getter
    public ArrayList<Area> areas = new ArrayList<>();

    public void startUp() {
        load();

        if(ENV_AREA_CONFIG != null) {
            try {
                fileWatcher = new FileWatcher().watchFile(Env.getPath(ENV_AREA_CONFIG)).addChangeHandler(path -> load());
            } catch (IOException ex) {
                log.info("Failed to initialize file watcher", ex);
            }
        }

    }

    public void shutDown() {
        if (fileWatcher != null) {
            fileWatcher.close();
            fileWatcher = null;
        }
    }

    @SneakyThrows
    public void load() {
        areas.clear();
        if(ENV_AREA_CONFIG == null) {
            String filename = "areas.json";
            InputStream is = HdPlugin.class.getResourceAsStream(filename);
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            loadAreas(reader);
        } else {
            Path lightsConfigPath = Env.getPath(ENV_AREA_CONFIG);
            Reader reader = Files.newBufferedReader(lightsConfigPath);
            loadAreas(reader);
            if(client.getGameState() == GameState.LOGGED_IN) {
                clientThread.invoke(() -> client.setGameState(GameState.LOADING));
            }
        }
        log.info("Loaded " + areas.size() + " Areas");
    }

    public void loadAreas(Reader reader) {
        Gson gson = new GsonBuilder().setLenient().create();
        Area[] area = gson.fromJson(reader, Area[].class);
        for (Area data : area) {
            List<Rect> list = new ArrayList<>();
            if(!data.getRects().isEmpty()) {
                data.getRects().forEach(it -> {
                    list.add(new Rect(it[0],it[1],it[2],it[3],it.length == 4 ? 0 : it[4]));
                });
                data.setRectangles(list);
            }
            areas.add(data);
        }

        DEFAULT = getArea("ALL");
    }

    public void update(WorldPoint point) {
        currentArea = areas.stream().filter(area -> area.contains(point)).map(area ->
              area.mostSpecificArea(point, 0)).sorted(
              Comparator.comparingInt(p -> ((Pair<Integer, Area>) p).getLeft()).reversed())
              .map(Pair::getRight)
        .findFirst().orElse(DEFAULT);
        System.out.println(currentArea.getDescription());
    }

    public Area getArea(String name) {
        Area area = areas.stream().filter(data -> data.getDescription().equalsIgnoreCase(name)).findFirst().orElse(DEFAULT);
        if(area.getDescription().equalsIgnoreCase("all") && !name.equalsIgnoreCase("all")) {
            log.debug("Unable to Find: " + name + " Switching to ALL");
        }
        return area;
    }

    public boolean shouldHide(WorldPoint point) {
        if(!plugin.configHideAreas) {
            return false;
        }

        if(currentArea.isHideOtherRegions()) {
            return !currentArea.contains(point);
        }

        return false;
    }

    @Inject
    private SceneUploader sceneUploader;

    public void addTileData(GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer) {
        LargeTile tile = getCurrentArea().getLargeTile();
        int color = 127;
        int size = 10000 * Perspective.LOCAL_TILE_SIZE;
        int height = 0;

        if(tile.getMaterialBelow() != null) {
            int materialData = sceneUploader.modelPusher.packMaterialData(Material.getIndex(Material.DIRT_1), false);
            int terrainData = sceneUploader.packTerrainData(600, WaterType.WATER, 0);
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
