package rs117.hd.data.area.effects;

import com.google.common.collect.ListMultimap;
import lombok.Data;
import rs117.hd.data.WaterType;
import rs117.hd.data.materials.GroundMaterial;
import rs117.hd.utils.Rect;

import java.util.List;

@Data
public class Overlay {
    public Overlay(int id, GroundMaterial groundMaterial, int hue, int saturation, int shiftLightness,boolean blended) {
        this.id = id;
        this.blended = blended;
        this.hue = hue;
        this.saturation = saturation;
        this.shiftLightness = shiftLightness;
        this.groundMaterial = groundMaterial;
    }

    private int id;

    public Overlay(int id, GroundMaterial groundMaterial) {
        this.id = id;
        this.groundMaterial = groundMaterial;
    }

    public Overlay() {}

    private GroundMaterial groundMaterial = null;
    private WaterType waterType = WaterType.NONE;
    private boolean blended = true;
    private boolean blendedAsUnderlay = false;
    private int hue = -1;
    private int shiftHue = 0;
    private int saturation = -1;
    private int shiftSaturation = 0;
    private int lightness = -1;
    private int shiftLightness = 0;
    private Rect area;



}

