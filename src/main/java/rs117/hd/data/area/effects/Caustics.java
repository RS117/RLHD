package rs117.hd.data.area.effects;

import lombok.Data;

@Data
public class Caustics {
    private boolean underwater = false;
    private String underwaterCausticsColor = "";
    private float underwaterCausticsStrength = 0;
}
