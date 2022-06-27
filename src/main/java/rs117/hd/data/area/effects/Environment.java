package rs117.hd.data.area.effects;

import lombok.Data;
import rs117.hd.utils.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Environment {
    private boolean lightningEnabled = false;
    private boolean allowSkyOverride = true;
    private Fog fog = new Fog();
    private Caustics caustics = new Caustics();
    private Lighting lighting = new Lighting();
    private ArrayList<Rect> rects = null;
    private String name = null;
}

