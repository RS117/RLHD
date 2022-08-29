package rs117.hd.data.area.effects;

import lombok.Data;

@Data
public class Fog {
    private int fogDepth = 65;
    private boolean customFogDepth = false;
    private String fogColor = "#B9D6FF";
    private boolean customFogColor = false;
    private int groundFogStart = -200;
    private int groundFogEnd = -500;
    private float groundFogOpacity = 0;
}
