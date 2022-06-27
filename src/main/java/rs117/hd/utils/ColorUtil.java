package rs117.hd.utils;

import java.awt.*;

public class ColorUtil {

    public static float[] rgb(int r, int g, int b)
    {
        return new float[]{
                HDUtils.gammaToLinear(r / 255f),
                HDUtils.gammaToLinear(g / 255f),
                HDUtils.gammaToLinear(b / 255f)
        };
    }

    public static float[] rgb(String name)
    {
        Color color = Color.decode(name);
        return new float[]{
                HDUtils.gammaToLinear(color.getRed() / 255f),
                HDUtils.gammaToLinear(color.getGreen() / 255f),
                HDUtils.gammaToLinear(color.getBlue() / 255f)
        };
    }

    public static String rgbToHex (Color color)
    {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        hex=hex.toUpperCase();
        return hex;
    }

}
