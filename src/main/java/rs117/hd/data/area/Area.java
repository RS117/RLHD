package rs117.hd.data.area;

import lombok.Data;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.tuple.Pair;
import rs117.hd.data.WaterType;
import rs117.hd.data.area.effects.Environment;
import rs117.hd.data.area.effects.LargeTile;
import rs117.hd.data.area.effects.TileData;
import rs117.hd.utils.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Area {
    private ArrayList<Rect> rectangles = new ArrayList<>();
    private List<int[]> rects = Collections.emptyList();
    private String description = "Unknown";
    private AreaTheme theme = AreaTheme.NORMAL;
    private boolean hideOtherRegions = false;
    private Environment environment = null;
    private LargeTile largeTile = null;
    private List<Area> children = Collections.emptyList();

    private List<TileData> overlays = Collections.emptyList();
    private List<TileData> underlays = Collections.emptyList();


    public boolean contains(WorldPoint point) {
        return rectangles.stream().anyMatch(rect -> rect.containsPoint(point));
    }

    public Pair<Integer, Area> mostSpecificArea(WorldPoint point, int areaDepth) {
        Pair<Integer, Area> parent = Pair.of(areaDepth, this);
        return children.stream().filter(area -> area.contains(point)).map(child ->
            child.mostSpecificArea(point, areaDepth + 1))
        .reduce(parent, (a, b) -> b.getLeft() > a.getLeft() ? b : a);
    }

    public void loadTileData() {
        rects.forEach(rect-> {
            Rect area = new Rect(rect[0],rect[1],rect[2],rect[3],rect.length == 4 ? 0 : rect[4]);
            rectangles.add(area);
            getOverlays().forEach(overlay -> {
                if (overlay.getWaterType() != WaterType.NONE && overlay.getGroundMaterial() == null) {
                    overlay.setGroundMaterial(overlay.getWaterType().getGroundMaterial());
                }
                overlay.setArea(area);
            });
            getUnderlays().forEach(underlay -> {
                if (underlay.getWaterType() != WaterType.NONE && underlay.getGroundMaterial() == null) {
                    underlay.setGroundMaterial(underlay.getWaterType().getGroundMaterial());
                }
                underlay.setArea(area);
            });
        });
    }

    public void loadEnvironment() {
        environment.setRects(rectangles);
        environment.setName(description);

        if(environment.getCaustics().getUnderwaterCausticsColor().isEmpty()) {
            environment.getCaustics().setUnderwaterCausticsColor(environment.getLighting().getDirectionalColor());
        }

        if (environment.getFog().getFogDepth() != 65) {
            environment.getFog().setFogDepth(environment.getFog().getFogDepth() * 10);
            environment.getFog().setCustomFogDepth(true);
        }

        environment.getFog().setCustomFogColor(environment.getFog().getFogColor().equals("B9D6FF"));
        environment.getLighting().setCustomDirectionalStrength(environment.getLighting().getDirectionalStrength() != 4.0f);
        environment.getLighting().setCustomDirectionalColor(!environment.getLighting().getDirectionalColor().equals("#FFFFFF"));
        environment.getLighting().setCustomAmbientStrength(environment.getLighting().getAmbientStrength() != 1.0f);
        environment.getLighting().setCustomAmbientColor(!environment.getLighting().getAmbientColor().equals("#97BAFF"));
    }

}


