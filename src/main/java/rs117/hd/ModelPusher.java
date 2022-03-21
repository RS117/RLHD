package rs117.hd;

import com.google.common.primitives.Ints;
import com.jogamp.opengl.math.VectorUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import rs117.hd.materials.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

class ModelData {
    private int[] colors;
    private int faceCount;

    public int getFaceCount() {
        return faceCount;
    }

    public ModelData setFaceCount(int faceCount) {
        this.faceCount = faceCount;
        return this;
    }

    public ModelData setColors(int[] colors) {
        this.colors = colors;
        return this;
    }

    public int getColorForFace(int face, int index) {
        return this.colors[(face * 4) + index];
    }
}

/**
 * Pushes models
 */
@Singleton
@Slf4j
public class ModelPusher {
    @Inject
    private HdPlugin hdPlugin;

    @Inject
    private HdPluginConfig config;

    @Inject
    private Client client;

    @Inject
    private ProceduralGenerator proceduralGenerator;

    // subtracts the X lowest lightness levels from the formula.
    // helps keep darker colors appropriately dark
    private static final int ignoreLowLightness = 3;
    // multiplier applied to vertex' lightness value.
    // results in greater lightening of lighter colors
    private static final float lightnessMultiplier = 3f;
    // the minimum amount by which each color will be lightened
    private static final int baseLighten = 10;
    // a directional vector approximately opposite of the directional light
    // used by the client
    private static final float[] inverseLightDirection = new float[]{
            0.57735026f, 0.57735026f, 0.57735026f
    };

    // same thing but for the normalBuffer and uvBuffer
    private final static float[] zeroFloats = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final static int[] twoInts = new int[2];
    private final static int[] fourInts = new int[4];
    private final static int[] eightInts = new int[8];
    private final static int[] twelveInts = new int[12];
    private final static float[] twelveFloats = new float[12];
    private final static int[] modelColors = new int[HdPlugin.MAX_TRIANGLE * 4];
    private final static ModelData tempModelData = new ModelData();

    private final static FixedLengthHashCode hasher = new FixedLengthHashCode(HdPlugin.MAX_TRIANGLE);

    private final Map<Integer, ModelData> modelCache = new WeakHashMap<>();

    private final Set<Integer> npcsWithBakedEffects = new HashSet<>(Arrays.asList(
            10, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 137, 139, 142, 154, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 281, 284, 285, 286, 313, 326, 327, 404, 412, 413, 414, 423, 433, 434, 435, 436, 470, 472, 473, 474, 498, 499, 505, 506, 507, 509, 762, 763, 764, 817, 852, 853, 854, 855, 891, 920, 948, 1039, 1060, 1061, 1062, 1120, 1161, 1362, 1363, 1364, 1366, 1367, 1368, 1543, 1554, 1555, 1609, 1625, 1632, 1635, 1636, 1637, 1638, 1640, 1641, 1642, 1643, 1644, 1645, 1646, 1647, 1648, 1650, 1651, 1652, 1653, 1654, 1668, 1680, 1681, 1682, 1689, 1690, 1691, 1692, 1693, 1786, 1801, 1802, 1803, 1804, 1805, 1806, 1840, 1841, 1842, 1843, 1844, 1845, 1847, 1848, 1849, 1850, 1851, 1871, 1872, 2005, 2006, 2007, 2008, 2018, 2025, 2026, 2027, 2028, 2029, 2030, 2031, 2032, 2048, 2049, 2050, 2051, 2052, 2054, 2085, 2086, 2087, 2088, 2089, 2090, 2091, 2092, 2093, 2097, 2098, 2099, 2100, 2101, 2102, 2103, 2136, 2137, 2138, 2139, 2140, 2141, 2142, 2143, 2144, 2189, 2190, 2205, 2206, 2215, 2235, 2236, 2237, 2238, 2239, 2240, 2242, 2243, 2244, 2463, 2464, 2465, 2466, 2467, 2468, 2510, 2511, 2512, 2527, 2528, 2529, 2530, 2531, 2532, 2533, 2534, 2583, 2642, 2644, 2827, 2829, 2834, 2837, 2838, 2839, 2849, 2856, 2857, 2858, 2859, 2860, 2861, 2862, 2863, 2864, 2865, 2866, 2867, 2868, 2869, 2916, 2917, 2918, 2919, 2956, 2957, 2958, 2959, 2960, 2961, 2962, 2963, 3092, 3116, 3117, 3129, 3130, 3131, 3132, 3139, 3162, 3163, 3164, 3165, 3166, 3167, 3168, 3169, 3170, 3171, 3172, 3173, 3174, 3175, 3176, 3177, 3178, 3179, 3180, 3181, 3182, 3183, 3201, 3202, 3204, 3291, 3313, 3314, 3315, 3357, 3423, 3424, 3425, 3472, 3474, 3518, 3588, 3589, 3590, 3607, 3608, 3609, 3616, 3617, 3625, 3626, 3627, 3713, 3714, 3715, 3716, 3717, 3718, 3719, 3720, 3725, 3726, 3727, 3728, 3729, 3730, 3731, 3732, 3835, 3837, 3838, 3839, 3840, 3841, 3842, 3843, 3844, 3850, 3851, 3852, 3904, 3905, 3906, 3907, 3908, 3909, 3910, 3911, 3968, 3969, 3970, 3971, 3975, 3976, 3977, 3978, 3979, 3998, 4053, 4289, 4290, 4385, 4504, 4534, 4535, 4562, 4674, 4688, 4689, 4690, 4692, 4734, 4735, 4738, 4796, 4797, 4798, 4809, 4820, 4821, 4881, 5194, 5240, 5241, 5341, 5352, 5355, 5370, 5371, 5548, 5549, 5550, 5551, 5552, 5590, 5597, 5631, 5632, 5633, 5735, 5736, 5737, 5757, 5758, 5759, 5760, 5761, 5762, 5763, 5764, 5765, 5775, 5796, 5797, 5798, 5799, 5800, 5801, 5802, 5803, 5804, 5805, 5806, 5807, 5835, 5836, 5837, 5838, 5839, 5867, 5868, 5869, 5872, 5873, 5874, 5875, 5876, 5877, 5878, 5879, 5880, 5881, 5882, 6118, 6120, 6298, 6309, 6323, 6324, 6325, 6335, 6339, 6349, 6351, 6359, 6370, 6384, 6385, 6386, 6396, 6400, 6492, 6493, 6494, 6495, 6502, 6503, 6505, 6587, 6588, 6593, 6594, 6609, 6631, 6632, 6633, 6634, 6636, 6639, 6642, 6643, 6644, 6646, 6647, 6652, 6655, 6674, 6689, 6696, 6716, 6723, 6738, 6762, 6793, 6795, 6824, 7023, 7024, 7027, 7036, 7037, 7039, 7061, 7063, 7101, 7102, 7103, 7233, 7234, 7242, 7243, 7244, 7245, 7246, 7247, 7248, 7249, 7253, 7254, 7255, 7258, 7261, 7262, 7263, 7264, 7270, 7271, 7272, 7273, 7274, 7275, 7287, 7302, 7332, 7333, 7391, 7394, 7397, 7398, 7401, 7402, 7403, 7404, 7405, 7406, 7409, 7410, 7411, 7416, 7513, 7576, 7577, 7578, 7579, 7649, 7656, 7657, 7664, 7668, 7692, 7693, 7694, 7695, 7696, 7697, 7698, 7699, 7700, 7702, 7703, 7704, 7748, 7750, 7859, 7861, 7862, 7863, 7865, 7866, 7867, 7868, 7869, 7870, 7871, 7872, 7873, 7874, 7875, 7876, 7878, 7879, 7880, 7932, 7936, 7938, 7940, 7955, 8015, 8016, 8017, 8018, 8027, 8030, 8031, 8033, 8049, 8065, 8066, 8073, 8074, 8075, 8076, 8077, 8078, 8079, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8090, 8091, 8092, 8093, 8158, 8166, 8174, 8195, 8234, 8337, 8338, 8339, 8340, 8341, 8344, 8347, 8350, 8353, 8356, 8359, 8366, 8367, 8371, 8372, 8375, 8383, 8384, 8385, 8386, 8389, 8424, 8425, 8426, 8428, 8429, 8430, 8431, 8434, 8435, 8438, 8439, 8440, 8442, 8444, 8445, 8446, 8447, 8448, 8449, 8450, 8451, 8452, 8453, 8454, 8455, 8456, 8459, 8460, 8463, 8464, 8467, 8470, 8471, 8472, 8473, 8482, 8483, 8488, 8489, 8490, 8492, 8494, 8495, 8504, 8505, 8512, 8513, 8517, 8519, 8520, 8539, 8540, 8548, 8549, 8550, 8551, 8552, 8553, 8554, 8555, 8556, 8557, 8558, 8559, 8560, 8561, 8605, 8606, 8607, 8608, 8609, 8614, 8615, 8616, 8620, 8621, 8623, 8697, 8699, 8713, 8714, 8715, 8729, 8730, 8736, 8737, 8738, 8741, 8742, 8743, 8744, 8745, 8746, 8747, 8748, 8749, 8750, 8751, 8752, 8753, 8754, 8755, 8756, 8757, 8775, 8776, 8777, 8778, 8779, 8780, 8781, 8782, 8783, 8784, 8921, 8994, 8995, 8996, 9024, 9025, 9026, 9027, 9028, 9029, 9030, 9031, 9032, 9033, 9034, 9038, 9039, 9040, 9041, 9042, 9043, 9044, 9045, 9046, 9047, 9048, 9050, 9194, 9199, 9258, 9293, 9294, 9295, 9296, 9297, 9454, 9455, 9457, 9465, 9466, 9467, 9483, 9647, 9648, 9649, 9650, 9651, 9652, 9653, 9654, 9655, 9656, 9672, 9673, 9674, 10374, 10375, 10376, 10402, 10474, 10475, 10492, 10493, 10506, 10523, 10524, 10525, 10526, 10534, 10535, 10536, 10537, 10538, 10541, 10544, 10545, 10620, 10623, 10625, 10628, 10654, 10665, 10666, 10689, 10690, 10691, 10692, 10693, 10694, 10695, 10696, 10762, 10765, 10766, 10767, 10768, 10769, 10770, 10771, 10772, 10773, 10776, 10779, 10782, 10785, 10788, 10793, 10796, 10799, 10802, 10805, 10809, 10812, 10813, 10820, 10821, 10828, 10829, 10832, 10833, 10836, 10843, 10844, 10845, 10846, 10849, 10850, 10853, 10860, 10861, 10862, 10863, 10866, 10869, 10871, 10874, 10888, 10951, 10953, 10954, 10955, 10956, 10961, 10962, 11157, 11158, 11159, 11160, 11179, 11184, 11187, 11188, 11191, 11195, 11237, 11238, 11297
    ));

    public final Set<Integer> objectsWithBakedEffects = new HashSet<>(Arrays.asList(
            2050, 2052, 2491, 2492, 3351, 3352, 4502, 5252, 6188, 10436, 10487, 11468, 11832, 11835, 11876, 12005, 12006, 13378, 14373, 15182, 15183, 15184, 15257, 15510, 15511, 15514, 15515, 17208, 17324, 18960, 19204, 20212, 20213, 20214, 20215, 24560, 26149, 27131, 29146, 29151, 29152, 29253, 29753, 29754, 29755, 29756, 29757, 29758, 29759, 29760, 29761, 29762, 29774, 29794, 29795, 29796, 29797, 29798, 29799, 29867, 29991, 30015, 30016, 30017, 30018, 30034, 30520, 30656, 30657, 30658, 30659, 30660, 30661, 30741, 30868, 31108, 31109, 31110, 31111, 31112, 31114, 31115, 31116, 31117, 31118, 31121, 31122, 31123, 31124, 31126, 31127, 31128, 31130, 31136, 31144, 31145, 31146, 31147, 31148, 31156, 31157, 31158, 31159, 31160, 31181, 31212, 31213, 31214, 31216, 31217, 31218, 31219, 31220, 31222, 31223, 31224, 31225, 31226, 31228, 31229, 31230, 31231, 31232, 31234, 31235, 31236, 31237, 31246, 31247, 31248, 31249, 31250, 31253, 31254, 31255, 31256, 31258, 31259, 31260, 31261, 31262, 31264, 31265, 31266, 31267, 31268, 31270, 31271, 31272, 31273, 31274, 31276, 31277, 31278, 31279, 31280, 31294, 31295, 31296, 31297, 31298, 31677, 31920, 31922, 31924, 31936, 31957, 31958, 31959, 31960, 33199, 33200, 33201, 33202, 33203, 33204, 33205, 33206, 33207, 33208, 33209, 33210, 34420, 34681, 35075, 35852, 35900, 35901, 35967, 35968, 36240, 36501, 37492, 37493, 37494, 37495, 37496, 37497, 37498, 37499, 37500, 37501, 37502, 37503, 37504, 37505, 37506, 37507, 37508, 37509, 37510, 37511, 37512, 37513, 37514, 37515, 37516, 37517, 37518, 37519, 37520, 37521, 37522, 37523, 37524, 37525, 37526, 37527, 37528, 37529, 37530, 37531, 37532, 37533, 37534, 37535, 37536, 37537, 37538, 37539, 37540, 37541, 37542, 37543, 37544, 37545, 37546, 37866, 38500, 38501, 38505, 40246, 40250, 40274, 40281, 40955, 41006, 41007, 41809, 41875, 41876, 41881
    ));

    public void clearModelCache() {
        modelCache.clear();
    }

    public int[] pushModel(Renderable renderable, Model model, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int tileX, int tileY, int tileZ, ObjectProperties objectProperties, ObjectType objectType, boolean noCache) {
        final int faceCount = Math.min(model.getFaceCount(), HdPlugin.MAX_TRIANGLE);

        // skip models with zero faces
        // this does seem to happen sometimes (mostly during loading)
        // should save some CPU cycles here and there
        if (faceCount == 0) {
            twoInts[0] = 0;
            twoInts[1] = 0;
            return twoInts;
        }

        // ensure capacity upfront
        vertexBuffer.ensureCapacity(12 * 2 * faceCount);
        normalBuffer.ensureCapacity(12 * 2 * faceCount);
        uvBuffer.ensureCapacity(12 * 2 * faceCount);

        ModelData modelData = getCachedModelData(renderable, model, objectProperties, objectType, tileX, tileY, tileZ, faceCount, noCache);

        int vertexLength = 0;
        int uvLength = 0;
        for (int face = 0; face < faceCount; face++) {
            vertexBuffer.put(getVertexDataForFace(model, modelData, face));
            vertexLength += 3;

            normalBuffer.put(getNormalDataForFace(model, objectProperties, face));

            float[] uvData = getUvDataForFace(model, objectProperties, face);
            if (uvData != null) {
                uvBuffer.put(uvData);
                uvLength += 3;
            }
        }

        twoInts[0] = vertexLength;
        twoInts[1] = uvLength;

        return twoInts;
    }

    private int[] getVertexDataForFace(Model model, ModelData modelData, int face) {
        final int[] xVertices = model.getVerticesX();
        final int[] yVertices = model.getVerticesY();
        final int[] zVertices = model.getVerticesZ();
        final int triA = model.getFaceIndices1()[face];
        final int triB = model.getFaceIndices2()[face];
        final int triC = model.getFaceIndices3()[face];

        twelveInts[0] = xVertices[triA];
        twelveInts[1] = yVertices[triA];
        twelveInts[2] = zVertices[triA];
        twelveInts[3] = modelData.getColorForFace(face, 3) | modelData.getColorForFace(face, 0);
        twelveInts[4] = xVertices[triB];
        twelveInts[5] = yVertices[triB];
        twelveInts[6] = zVertices[triB];
        twelveInts[7] = modelData.getColorForFace(face, 3) | modelData.getColorForFace(face, 1);
        twelveInts[8] = xVertices[triC];
        twelveInts[9] = yVertices[triC];
        twelveInts[10] = zVertices[triC];
        twelveInts[11] = modelData.getColorForFace(face, 3) | modelData.getColorForFace(face, 2);

        return twelveInts;
    }

    private float[] getNormalDataForFace(Model model, ObjectProperties objectProperties, int face) {
        if (model.getFaceColors3()[face] == -1 || (objectProperties != null && objectProperties.isFlatNormals())) {
            return zeroFloats;
        }

        final int triA = model.getFaceIndices1()[face];
        final int triB = model.getFaceIndices2()[face];
        final int triC = model.getFaceIndices3()[face];
        final int[] xVertexNormals = model.getVertexNormalsX();
        final int[] yVertexNormals = model.getVertexNormalsY();
        final int[] zVertexNormals = model.getVertexNormalsZ();

        twelveFloats[0] = xVertexNormals[triA];
        twelveFloats[1] = yVertexNormals[triA];
        twelveFloats[2] = zVertexNormals[triA];
        twelveFloats[3] = 0;
        twelveFloats[4] = xVertexNormals[triB];
        twelveFloats[5] = yVertexNormals[triB];
        twelveFloats[6] = zVertexNormals[triB];
        twelveFloats[7] = 0;
        twelveFloats[8] = xVertexNormals[triC];
        twelveFloats[9] = yVertexNormals[triC];
        twelveFloats[10] = zVertexNormals[triC];
        twelveFloats[11] = 0;

        return twelveFloats;
    }

    private float[] getUvDataForFace(Model model, ObjectProperties objectProperties, int face) {
        final short[] faceTextures = model.getFaceTextures();
        final float[] uv = model.getFaceTextureUVCoordinates();

        Material material = null;
        if (objectProperties != null && objectProperties.getMaterial() != Material.NONE) {
            material = hdPlugin.configObjectTextures ? objectProperties.getMaterial() : Material.NONE;
        }

        if (faceTextures != null && faceTextures[face] != -1 && uv != null) {
            material = proceduralGenerator.getSeasonalMaterial(Material.getTexture(faceTextures[face]));
            int packedMaterialData = packMaterialData(Material.getIndexFromDiffuseID(material.getDiffuseMapId()), false);
            int idx = face * 6;

            twelveFloats[0] = packedMaterialData;
            twelveFloats[1] = uv[idx];
            twelveFloats[2] = uv[idx + 1];
            twelveFloats[3] = 0;
            twelveFloats[4] = packedMaterialData;
            twelveFloats[5] = uv[idx + 2];
            twelveFloats[6] = uv[idx + 3];
            twelveFloats[7] = 0;
            twelveFloats[8] = packedMaterialData;
            twelveFloats[9] = uv[idx + 4];
            twelveFloats[10] = uv[idx + 5];
            twelveFloats[11] = 0;

            return twelveFloats;
        } else if (material != null) {
            final int triA = model.getFaceIndices1()[face];
            final int triB = model.getFaceIndices2()[face];
            final int triC = model.getFaceIndices3()[face];

            final int[] xVertices = model.getVerticesX();
            final int[] zVertices = model.getVerticesZ();

            material = proceduralGenerator.getSeasonalMaterial(material);
            int packedMaterialData = packMaterialData(Material.getIndexFromDiffuseID(material.getDiffuseMapId()), false);

            if (objectProperties.getUvType() == UvType.GROUND_PLANE) {
                twelveFloats[0] = packedMaterialData;
                twelveFloats[1] = (xVertices[triA] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
                twelveFloats[2] = (zVertices[triA] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
                twelveFloats[3] = 0;
                twelveFloats[4] = packedMaterialData;
                twelveFloats[5] = (xVertices[triB] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
                twelveFloats[6] = (zVertices[triB] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
                twelveFloats[7] = 0;
                twelveFloats[8] = packedMaterialData;
                twelveFloats[9] = (xVertices[triC] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
                twelveFloats[10] = (zVertices[triC] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE;
                twelveFloats[11] = 0;

                return twelveFloats;
            } else {
                twelveFloats[0] = packedMaterialData;
                twelveFloats[1] = 0;
                twelveFloats[2] = 0;
                twelveFloats[3] = 0;
                twelveFloats[4] = packedMaterialData;
                twelveFloats[5] = 1;
                twelveFloats[6] = 0;
                twelveFloats[7] = 0;
                twelveFloats[8] = packedMaterialData;
                twelveFloats[9] = 0;
                twelveFloats[10] = 1;
                twelveFloats[11] = 0;

                return twelveFloats;
            }
        } else if (faceTextures != null) {
            return zeroFloats;
        }

        return null;
    }

    public int packMaterialData(int materialId, boolean isOverlay) {
        if (materialId == Material.getIndex(Material.INFERNAL_CAPE) && hdPlugin.configHdInfernalTexture) {
            materialId = Material.getIndex(Material.HD_INFERNAL_CAPE);
        }

        if (hdPlugin.configObjectTextures) {
            if (materialId == Material.getIndex(Material.BRICK)) {
                materialId = Material.getIndex(Material.HD_BRICK);
            } else if (materialId == Material.getIndex(Material.ROOF_SHINGLES_1)) {
                materialId = Material.getIndex(Material.HD_ROOF_SHINGLES_1);
            } else if (materialId == Material.getIndex(Material.MARBLE_DARK)) {
                materialId = Material.getIndex(Material.HD_MARBLE_DARK);
            } else if (materialId == Material.getIndex(Material.BRICK_BROWN)) {
                materialId = Material.getIndex(Material.HD_BRICK_BROWN);
            } else if (materialId == Material.getIndex(Material.LAVA)) {
                materialId = Material.getIndex(Material.HD_LAVA_3);
            } else if (materialId == Material.getIndex(Material.ROOF_SHINGLES_2)) {
                materialId = Material.getIndex(Material.HD_ROOF_SHINGLES_2);
            }
        }

        return materialId << 1 | (isOverlay ? 0b1 : 0b0);
    }

    private ModelData getCachedModelData(Renderable renderable, Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int faceCount, boolean noCache) {
        if (noCache) {
            tempModelData.setColors(getColorsForModel(renderable, model, objectProperties, objectType, tileX, tileY, tileZ, faceCount));
            return tempModelData;
        }

        // note for future spelunkers:
        // this hash code is accurate for caching the model colors but will probably need to be expanded if you're attempting to include other data
        eightInts[0] = hasher.hashCode(model.getFaceColors1());
        eightInts[1] = hasher.hashCode(model.getFaceColors2());
        eightInts[2] = hasher.hashCode(model.getFaceColors3());
        eightInts[3] = Arrays.hashCode(model.getFaceTransparencies());
        eightInts[4] = model.getOverrideAmount();
        eightInts[5] = model.getOverrideHue();
        eightInts[6] = model.getOverrideSaturation();
        eightInts[7] = model.getOverrideLuminance();
        int hash = hasher.hashCode(eightInts);

        ModelData modelData = modelCache.get(hash);
        if (modelData == null || modelData.getFaceCount() != model.getFaceCount()) {
            // get new data if there was no cache or if we detected an exception causing hash collision
            modelData = new ModelData().setColors(getColorsForModel(renderable, model, objectProperties, objectType, tileX, tileY, tileZ, faceCount)).setFaceCount(model.getFaceCount());
            modelCache.put(hash, modelData);
        }

        return modelData;
    }

    private int[] getColorsForModel(Renderable renderable, Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int faceCount) {
        for (int face = 0; face < faceCount; face++) {
            System.arraycopy(getColorsForFace(renderable, model, objectProperties, objectType, tileX, tileY, tileZ, face), 0, modelColors, face * 4, 4);
        }

        return Arrays.copyOfRange(modelColors, 0, faceCount * 4);
    }

    private int[] removeBakedGroundShading(int face, int triA, int triB, int triC, byte[] faceTransparencies, short[] faceTextures, int[] yVertices) {
        if (faceTransparencies != null && (faceTextures == null || faceTextures[face] == -1) && (faceTransparencies[face] & 0xFF) > 100) {
            int aHeight = yVertices[triA];
            int bHeight = yVertices[triB];
            int cHeight = yVertices[triC];
            if (aHeight >= -8 && aHeight == bHeight && aHeight == cHeight) {
                fourInts[0] = 0;
                fourInts[1] = 0;
                fourInts[2] = 0;
                fourInts[3] = 0xFF << 24;
                return fourInts;
            }
        }

        return null;
    }

    private int[] getColorsForFace(Renderable renderable, Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int face) {
        int color1 = model.getFaceColors1()[face];
        int color2 = model.getFaceColors2()[face];
        int color3 = model.getFaceColors3()[face];
        final short[] faceTextures = model.getFaceTextures();
        final byte[] faceTransparencies = model.getFaceTransparencies();
        final byte overrideAmount = model.getOverrideAmount();
        final byte overrideHue = model.getOverrideHue();
        final byte overrideSat = model.getOverrideSaturation();
        final byte overrideLum = model.getOverrideLuminance();
        final int triA = model.getFaceIndices1()[face];
        final int triB = model.getFaceIndices2()[face];
        final int triC = model.getFaceIndices3()[face];
        final int[] yVertices = model.getVerticesY();
        final int[] xVertexNormals = model.getVertexNormalsX();
        final int[] yVertexNormals = model.getVertexNormalsY();
        final int[] zVertexNormals = model.getVertexNormalsZ();
        final Tile tile = client.getScene().getTiles()[tileZ][tileX][tileY];

        if (config.hideBakedEffects()) {
            // hide the shadows and lights that are often baked into models by setting the colors for the shadow faces to transparent
            NPC npc = renderable instanceof NPC ? (NPC) renderable : null;
            GraphicsObject graphicsObject = renderable instanceof GraphicsObject ? (GraphicsObject) renderable : null;
            if ((npc != null && npcsWithBakedEffects.contains(npc.getId())) || (graphicsObject != null && objectsWithBakedEffects.contains(graphicsObject.getId()))) {
                int[] transparency = removeBakedGroundShading(face, triA, triB, triC, faceTransparencies, faceTextures, yVertices);
                if (transparency != null) {
                    return transparency;
                }
            }
        }

        if (color3 == -2) {
            fourInts[0] = 0;
            fourInts[1] = 0;
            fourInts[2] = 0;
            fourInts[3] = 0xFF << 24;
            return fourInts;
        } else if (color3 == -1) {
            color2 = color3 = color1;
        } else if ((faceTextures == null || faceTextures[face] == -1) && overrideAmount > 0) {
            // HSL override is not applied to flat shade faces or to textured faces
            color1 = interpolateHSL(color1, overrideHue, overrideSat, overrideLum, overrideAmount);
            color2 = interpolateHSL(color2, overrideHue, overrideSat, overrideLum, overrideAmount);
            color3 = interpolateHSL(color3, overrideHue, overrideSat, overrideLum, overrideAmount);
        }

        int color1H = color1 >> 10 & 0x3F;
        int color1S = color1 >> 7 & 0x7;
        int color1L = color1 & 0x7F;
        int color2H = color2 >> 10 & 0x3F;
        int color2S = color2 >> 7 & 0x7;
        int color2L = color2 & 0x7F;
        int color3H = color3 >> 10 & 0x3F;
        int color3S = color3 >> 7 & 0x7;
        int color3L = color3 & 0x7F;

        // reduce the effect of the baked shading by approximately inverting the process by which
        // the shading is added initially.
        int lightenA = (int) (Math.max((color1L - ignoreLowLightness), 0) * lightnessMultiplier) + baseLighten;
        float dotA = Math.max(VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[]{
                xVertexNormals[triA],
                yVertexNormals[triA],
                zVertexNormals[triA],
        }), inverseLightDirection), 0);
        color1L = (int) HDUtils.lerp(color1L, lightenA, dotA);

        int lightenB = (int) (Math.max((color2L - ignoreLowLightness), 0) * lightnessMultiplier) + baseLighten;
        float dotB = Math.max(VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[]{
                xVertexNormals[triB],
                yVertexNormals[triB],
                zVertexNormals[triB],
        }), inverseLightDirection), 0);
        color2L = (int) HDUtils.lerp(color2L, lightenB, dotB);

        int lightenC = (int) (Math.max((color3L - ignoreLowLightness), 0) * lightnessMultiplier) + baseLighten;
        float dotC = Math.max(VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[]{
                xVertexNormals[triC],
                yVertexNormals[triC],
                zVertexNormals[triC],
        }), inverseLightDirection), 0);
        color3L = (int) HDUtils.lerp(color3L, lightenC, dotC);

        int maxBrightness = 55;
        if (faceTextures != null && faceTextures[face] != -1) {
            maxBrightness = 90;
            // set textured faces to pure white as they are harder to remove shadows from for some reason
            color1H = color2H = color3H = 0;
            color1S = color2S = color3S = 0;
            color1L = color2L = color3L = 127;
        }

        if (objectProperties != null && objectProperties.isInheritTileColor()) {
            if (tile != null && (tile.getSceneTilePaint() != null || tile.getSceneTileModel() != null)) {
                int[] tileColorHSL;

                if (tile.getSceneTilePaint() != null && tile.getSceneTilePaint().getTexture() == -1) {
                    // pull any corner color as either one should be OK
                    tileColorHSL = HDUtils.colorIntToHSL(tile.getSceneTilePaint().getSwColor());

                    // average saturation and lightness
                    tileColorHSL[1] =
                            (
                                    tileColorHSL[1] +
                                            HDUtils.colorIntToHSL(tile.getSceneTilePaint().getSeColor())[1] +
                                            HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNwColor())[1] +
                                            HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNeColor())[1]
                            ) / 4;

                    tileColorHSL[2] =
                            (
                                    tileColorHSL[2] +
                                            HDUtils.colorIntToHSL(tile.getSceneTilePaint().getSeColor())[2] +
                                            HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNwColor())[2] +
                                            HDUtils.colorIntToHSL(tile.getSceneTilePaint().getNeColor())[2]
                            ) / 4;

                    int overlayId = client.getScene().getOverlayIds()[tileZ][tileX][tileY];
                    int underlayId = client.getScene().getUnderlayIds()[tileZ][tileX][tileY];
                    if (overlayId != 0) {
                        Overlay overlay = Overlay.getOverlay(overlayId, tile, client);
                        overlay = proceduralGenerator.getSeasonalOverlay(overlay);
                        tileColorHSL = proceduralGenerator.recolorOverlay(overlay, tileColorHSL);
                    } else {
                        Underlay underlay = Underlay.getUnderlay(underlayId, tile, client);
                        underlay = proceduralGenerator.getSeasonalUnderlay(underlay);
                        tileColorHSL = proceduralGenerator.recolorUnderlay(underlay, tileColorHSL);
                    }

                    color1H = color2H = color3H = tileColorHSL[0];
                    color1S = color2S = color3S = tileColorHSL[1];
                    color1L = color2L = color3L = tileColorHSL[2];
                } else if (tile.getSceneTileModel() != null && tile.getSceneTileModel().getTriangleTextureId() == null) {
                    int faceColorIndex = -1;
                    for (int i = 0; i < tile.getSceneTileModel().getTriangleColorA().length; i++) {
                        if (!proceduralGenerator.isOverlayFace(tile, i)) {
                            // get a color from an underlay face as it's generally more desirable
                            // than pulling colors from paths and other overlays
                            faceColorIndex = i;
                            break;
                        }
                    }

                    if (faceColorIndex != -1) {
                        tileColorHSL = HDUtils.colorIntToHSL(tile.getSceneTileModel().getTriangleColorA()[faceColorIndex]);

                        int underlayId = client.getScene().getUnderlayIds()[tileZ][tileX][tileY];
                        Underlay underlay = Underlay.getUnderlay(underlayId, tile, client);
                        underlay = proceduralGenerator.getSeasonalUnderlay(underlay);
                        tileColorHSL = proceduralGenerator.recolorUnderlay(underlay, tileColorHSL);

                        color1H = color2H = color3H = tileColorHSL[0];
                        color1S = color2S = color3S = tileColorHSL[1];
                        color1L = color2L = color3L = tileColorHSL[2];
                    }
                }
            }
        }

        int packedAlphaPriority = getPackedAlphaPriority(model, face);

        if (hdPlugin.configTzhaarHD && objectProperties != null && objectProperties.getTzHaarRecolorType() != TzHaarRecolorType.NONE) {
            int[][] tzHaarRecolored = proceduralGenerator.recolorTzHaar(objectProperties, yVertices[triA], yVertices[triB], yVertices[triC], packedAlphaPriority, objectType, color1H, color1S, color1L, color2H, color2S, color2L, color3H, color3S, color3L);
            color1H = tzHaarRecolored[0][0];
            color1S = tzHaarRecolored[0][1];
            color1L = tzHaarRecolored[0][2];
            color2H = tzHaarRecolored[1][0];
            color2S = tzHaarRecolored[1][1];
            color2L = tzHaarRecolored[1][2];
            color3H = tzHaarRecolored[2][0];
            color3S = tzHaarRecolored[2][1];
            color3L = tzHaarRecolored[2][2];
            packedAlphaPriority = tzHaarRecolored[3][0];
        }

        color1L = Ints.constrainToRange(color1L, 0, maxBrightness);
        color2L = Ints.constrainToRange(color2L, 0, maxBrightness);
        color3L = Ints.constrainToRange(color3L, 0, maxBrightness);

        color1 = (color1H << 3 | color1S) << 7 | color1L;
        color2 = (color2H << 3 | color2S) << 7 | color2L;
        color3 = (color3H << 3 | color3S) << 7 | color3L;

        fourInts[0] = color1;
        fourInts[1] = color2;
        fourInts[2] = color3;
        fourInts[3] = packedAlphaPriority;

        return fourInts;
    }

    private static int interpolateHSL(int hsl, byte hue2, byte sat2, byte lum2, byte lerp) {
        int hue = hsl >> 10 & 63;
        int sat = hsl >> 7 & 7;
        int lum = hsl & 127;
        int var9 = lerp & 255;
        if (hue2 != -1) {
            hue += var9 * (hue2 - hue) >> 7;
        }

        if (sat2 != -1) {
            sat += var9 * (sat2 - sat) >> 7;
        }

        if (lum2 != -1) {
            lum += var9 * (lum2 - lum) >> 7;
        }

        return (hue << 10 | sat << 7 | lum) & 65535;
    }

    private int getPackedAlphaPriority(Model model, int face) {
        final short[] faceTextures = model.getFaceTextures();
        final byte[] faceTransparencies = model.getFaceTransparencies();
        final byte[] facePriorities = model.getFaceRenderPriorities();

        int alpha = 0;
        if (faceTransparencies != null && (faceTextures == null || faceTextures[face] == -1)) {
            alpha = (faceTransparencies[face] & 0xFF) << 24;
        }
        int priority = 0;
        if (facePriorities != null) {
            priority = (facePriorities[face] & 0xff) << 16;
        }
        return alpha | priority;
    }
}
