package rs117.hd;

import com.google.common.primitives.Ints;
import com.jogamp.opengl.math.VectorUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import rs117.hd.materials.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

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

    public void clearModelCache() {
        modelCache.clear();
    }

    public int[] pushModel(Model model, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int tileX, int tileY, int tileZ, ObjectProperties objectProperties, ObjectType objectType, boolean noCache) {
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

        ModelData modelData = getCachedModelData(model, objectProperties, objectType, tileX, tileY, tileZ, faceCount, noCache);

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

    private ModelData getCachedModelData(Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int faceCount, boolean noCache) {
        if (noCache) {
            tempModelData.setColors(getColorsForModel(model, objectProperties, objectType, tileX, tileY, tileZ, faceCount));
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
            modelData = new ModelData().setColors(getColorsForModel(model, objectProperties, objectType, tileX, tileY, tileZ, faceCount)).setFaceCount(model.getFaceCount());
            modelCache.put(hash, modelData);
        }

        return modelData;
    }

    private int[] getColorsForModel(Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int faceCount) {
        for (int face = 0; face < faceCount; face++) {
            System.arraycopy(getColorsForFace(model, objectProperties, objectType, tileX, tileY, tileZ, face), 0, modelColors, face*4, 4);
        }

        return Arrays.copyOfRange(modelColors, 0, faceCount*4);
    }

    private int[] getColorsForFace(Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int face) {
        int color1 = model.getFaceColors1()[face];
        int color2 = model.getFaceColors2()[face];
        int color3 = model.getFaceColors3()[face];
        final short[] faceTextures = model.getFaceTextures();
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
