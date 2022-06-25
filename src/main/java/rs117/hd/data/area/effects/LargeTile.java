package rs117.hd.data.area.effects;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Perspective;
import rs117.hd.data.WaterType;
import rs117.hd.data.materials.Material;
import rs117.hd.scene.SceneUploader;
import rs117.hd.utils.buffer.GpuFloatBuffer;
import rs117.hd.utils.buffer.GpuIntBuffer;

import javax.inject.Inject;

@Slf4j
@Data
public class LargeTile
{
    private final String material;
    private final String materialBelow;
    private final Boolean isOverlay;
    private final String waterType;
    private final int height;
    private final int color;

}
