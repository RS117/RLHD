package rs117.hd.data.area;

import lombok.Data;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.tuple.Pair;
import rs117.hd.data.area.effects.Caustics;
import rs117.hd.data.area.effects.Environment;
import rs117.hd.data.area.effects.LargeTile;
import rs117.hd.data.area.effects.TileData;
import rs117.hd.utils.Rect;

import java.util.Collections;
import java.util.List;

@Data
public class Area {
    private List<Rect> rectangles = Collections.emptyList();
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

}


