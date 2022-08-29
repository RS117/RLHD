package rs117.hd.data.area;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import org.apache.commons.lang3.tuple.Pair;
import rs117.hd.HdPlugin;
import rs117.hd.data.area.effects.Environment;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;
import rs117.hd.utils.Rect;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    @Inject
    private EnvironmentManager environmentManager;
    public static String ENV_AREA_CONFIG = "RLHD_AREA_PATH";
    public final String FILE_NAME = "areas.json";
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
                fileWatcher = new FileWatcher().watchFile(Objects.requireNonNull(Env.getPath(ENV_AREA_CONFIG))).addChangeHandler(path -> {
                    areas.clear();
                    environmentManager.sceneEnvironments.clear();
                    environmentManager.currentEnvironment = null;
                    load();
                });
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

        Reader reader;
        if(ENV_AREA_CONFIG == null) {
            InputStream inputStream = HdPlugin.class.getResourceAsStream(FILE_NAME);
            reader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
        } else {
            reader = Files.newBufferedReader(Objects.requireNonNull(Env.getPath(ENV_AREA_CONFIG)));
        }

        loadAreas(reader);
        if(ENV_AREA_CONFIG == null) {
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
            data.loadTileData();
            if(data.getEnvironment() != null) {
                data.loadEnvironment();
                environments.add(data.getEnvironment());
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
        log.info("Area Changed to: " + currentArea.getDescription());
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
