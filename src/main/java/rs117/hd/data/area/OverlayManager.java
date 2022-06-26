package rs117.hd.data.area;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import rs117.hd.HdPlugin;
import rs117.hd.data.area.effects.Overlay;
import rs117.hd.data.materials.GroundMaterial;

import javax.inject.Inject;
import java.util.List;

public class OverlayManager {
    private static ListMultimap<Integer, Overlay> GROUND_MATERIAL_MAP;

    private static Overlay DEFAULT = new Overlay(-1, GroundMaterial.DIRT);
    public static Overlay WINTER_GRASS = new Overlay(-1, GroundMaterial.SNOW_1,0,0,40,true);

    @Inject
    HdPlugin plugin;

    public void loadOverlays() {
        if(GROUND_MATERIAL_MAP == null) {
            GROUND_MATERIAL_MAP = ArrayListMultimap.create();
        }
        GROUND_MATERIAL_MAP.clear();
        if(plugin.getAreaManager().currentArea != null) {
            System.out.println(plugin.getAreaManager().currentArea.getTheme());
        }
        plugin.getAreaManager().areas.stream().filter(it -> !it.getOverlays().isEmpty()).forEach(overlays -> {
            overlays.getOverlays().forEach(overlay -> {
                GROUND_MATERIAL_MAP.put(overlay.getId(), overlay);
            });
        });
    }

    public static Overlay getOverlay(int overlayId, Tile tile, Client client)
    {
        WorldPoint worldPoint = tile.getWorldLocation();

        if (client.isInInstancedRegion())
        {
            LocalPoint localPoint = tile.getLocalLocation();
            worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
        }

        List<Overlay> overlays = GROUND_MATERIAL_MAP.get(overlayId);
        for (Overlay overlay : overlays)
        {
            if (overlay.getArea().containsPoint(worldPoint))
            {
                return overlay;
            }
        }

        return DEFAULT;
    }

}
