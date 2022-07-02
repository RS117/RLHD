package rs117.hd.data.area.effects;

import lombok.Data;

@Data
public class Lighting {
    private float ambientStrength = 1.0f;
    private boolean customAmbientStrength = false;
    private String ambientColor = "#97BAFF";
    private boolean customAmbientColor = false;
    private float directionalStrength = 4.0f;
    private boolean customDirectionalStrength = false;
    private String directionalColor = "#FFFFFF";
    private boolean customDirectionalColor = false;
    private float underglowStrength = 0.0f;
    private String underglowColor = "#000000";
    private float lightPitch = -128f;
    private float lightYaw = 55f;
}
