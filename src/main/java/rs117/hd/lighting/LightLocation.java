package rs117.hd.lighting;

import lombok.Getter;
import rs117.hd.lighting.data.Alignment;

@Getter
public final class LightLocation
{

    private final int x;
    private final int y;
    private final int plane;
    private final Alignment alignment;

    public LightLocation(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.plane = 0;
        this.alignment = Alignment.CENTER;
    }

    public LightLocation(int x, int y, int plane, Alignment alignment)
    {
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.alignment = alignment;
    }

    public LightLocation(int x, int y, Alignment alignment)
    {
        this.x = x;
        this.y = y;
        this.plane = 0;
        this.alignment = alignment;
    }

    public LightLocation(int x, int y, int plane)
    {
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.alignment = Alignment.CENTER;
    }

}
