package rs117.hd.data.area.effects;

import lombok.Data;
import rs117.hd.utils.Rect;

import java.util.ArrayList;


@Data
public class Environment {

    private String waterColor = "#7EADFF";
    private boolean lightningEnabled = false;
    private boolean customWaterColor = false;
    private boolean allowSkyOverride = true;
    private Fog fog = new Fog();
    private Caustics caustics = new Caustics();
    private Lighting lighting = new Lighting();
    private ArrayList<Rect> rects = null;
    private String name = null;
}

