package rs117.hd.data.area;

import com.google.common.collect.ListMultimap;
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
import rs117.hd.data.area.effects.Environment;
import rs117.hd.data.area.effects.LargeTile;
import rs117.hd.data.area.effects.TileData;
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
    public ArrayList<Environment> environments = new ArrayList<>();

    public Environment OVERWORLD = new Environment();
    public Environment PLAYER_OWNED_HOUSE;
    public Environment PLAYER_OWNED_HOUSE_SNOWY;
    public Environment THE_GAUNTLET;
    public Environment THE_GAUNTLET_CORRUPTED;
    public Environment WINTER;

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
                    Rect rect = new Rect(it[0],it[1],it[2],it[3],it.length == 4 ? 0 : it[4]);
                    list.add(rect);
                    if(!data.getOverlays().isEmpty()) {
                        List<TileData> overlays = new ArrayList<>();
                        data.getOverlays().forEach(overlay -> {
                            TileData overlayData = overlay;
                            if (overlay.getWaterType() != WaterType.NONE && overlay.getGroundMaterial() == null) {
                                overlayData.setGroundMaterial(overlay.getWaterType().getGroundMaterial());
                            }
                            overlayData.setArea(rect);
                            overlays.add(overlayData);
                        });
                        data.setOverlays(overlays);
                    }
                    if(!data.getUnderlays().isEmpty()) {
                        List<TileData> underlays = new ArrayList<>();
                        data.getUnderlays().forEach(overlay -> {
                            TileData underlayData = overlay;
                            if (overlay.getWaterType() != WaterType.NONE && overlay.getGroundMaterial() == null) {
                                underlayData.setGroundMaterial(overlay.getWaterType().getGroundMaterial());
                            }
                            underlayData.setArea(rect);
                            underlays.add(underlayData);
                        });
                        data.setUnderlays(underlays);
                    }
                });
                data.setRectangles(list);
                if(data.getEnvironment() != null) {

                    Environment environment = data.getEnvironment();

                    environment.setRects(list);
                    environment.setName(data.getDescription());


                    if(environment.getCaustics().getUnderwaterCausticsColor().isEmpty()) {
                        environment.getCaustics().setUnderwaterCausticsColor(environment.getLighting().getDirectionalColor());
                    }

                    if (environment.getFog().getFogDepth() != 65) {
                        environment.getFog().setFogDepth(environment.getFog().getFogDepth() * 10);
                        environment.getFog().setCustomFogDepth(true);
                    }

                    if (environment.getFog().getFogColor().equals("B9D6FF")) {
                        environment.getFog().setCustomFogColor(true);
                    }

                    if(environment.getLighting().getDirectionalStrength() != 4.0f) {
                        environment.getLighting().setCustomDirectionalStrength(true);
                    }

                    if(!environment.getLighting().getDirectionalColor().equals("#FFFFFF")) {
                        environment.getLighting().setCustomDirectionalColor(true);
                    }

                    if(environment.getLighting().getAmbientStrength() != 1.0f) {
                        environment.getLighting().setCustomAmbientStrength(true);
                    }

                    if(!environment.getLighting().getAmbientColor().equals("#97BAFF")) {
                        environment.getLighting().setCustomAmbientColor(true);
                    }

                    data.setEnvironment(environment);
                    environments.add(data.getEnvironment());

                }
            }

            areas.add(data);

        }
        plugin.getTileManager().loadOverlays();
        DEFAULT = getArea("ALL");

        PLAYER_OWNED_HOUSE = getEnvironment("PLAYER_OWNED_HOUSE");
        PLAYER_OWNED_HOUSE_SNOWY = getEnvironment("PLAYER_OWNED_HOUSE_SNOWY");
        THE_GAUNTLET = getEnvironment("THE_GAUNTLET");
        THE_GAUNTLET_CORRUPTED = getEnvironment("THE_GAUNTLET_CORRUPTED");
        WINTER = getEnvironment("WINTER");
    }

    public Environment getEnvironment(String name) {
        return getArea(name).getEnvironment();
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

}
