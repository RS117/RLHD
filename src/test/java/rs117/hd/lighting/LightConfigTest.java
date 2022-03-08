package rs117.hd.lighting;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class LightConfigTest {
    private static final ArrayList<SceneLight> WORLD_LIGHTS = new ArrayList<>();
    private static final ListMultimap<Integer, Light> NPC_LIGHTS = ArrayListMultimap.create();
    private static final ListMultimap<Integer, Light> OBJECT_LIGHTS = ArrayListMultimap.create();
    private static final ListMultimap<Integer, Light> PROJECTILE_LIGHTS = ArrayListMultimap.create();

    @Test
    public void testLoad() {
        LightConfig.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("lighting/lights.json"), WORLD_LIGHTS, NPC_LIGHTS, OBJECT_LIGHTS, PROJECTILE_LIGHTS);

        // can we get the same light for both of its raw IDs?
        Light spitRoastLight = OBJECT_LIGHTS.get(5608).get(0);
        assertEquals(spitRoastLight, OBJECT_LIGHTS.get(4267).get(0));

        // is its data correct?
        assertEquals("SPIT_ROAST", spitRoastLight.description);
        assertEquals(50, (int) spitRoastLight.height);
        assertEquals("CENTER", spitRoastLight.alignment.toString());
        assertEquals(250, spitRoastLight.radius);
        assertEquals(12.5, spitRoastLight.strength, 0.0);
        assertEquals("FLICKER", spitRoastLight.type.toString());
        assertEquals(0.0, spitRoastLight.duration, 0.0);
        assertEquals(20.0, spitRoastLight.range, 0.0);
        assertEquals(0.9743002, spitRoastLight.color[0], 0.001);
        assertEquals(0.3021255, spitRoastLight.color[1], 0.001);
        assertEquals(5.6921755E-5, spitRoastLight.color[2], 0.001);
    }
}
