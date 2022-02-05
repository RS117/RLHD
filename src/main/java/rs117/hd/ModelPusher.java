package rs117.hd;

import com.google.common.primitives.Ints;
import com.jogamp.opengl.math.VectorUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import rs117.hd.materials.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class ModelData {
    private int[] colors;

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

    private final static FixedLengthHashCode hasher = new FixedLengthHashCode(6144);

    private final HashMap<Integer, ModelData> modelCache = new HashMap<>();

    public void clearModelCache() {
        modelCache.clear();
    }

    public int[] pushModel(Model model, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer, GpuFloatBuffer normalBuffer, int tileX, int tileY, int tileZ, ObjectProperties objectProperties, ObjectType objectType, boolean noCache) {
        final int faceCount = Math.min(model.getFaceCount(), HdPlugin.MAX_TRIANGLE);

        // skip models with zero faces
        // this does seem to happen sometimes (mostly during loading)
        // should save some CPU cycles here and there
        if (faceCount == 0) {
            return new int[]{0, 0};
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

        return new int[]{vertexLength, uvLength};
    }

    private int[] getVertexDataForFace(Model model, ModelData modelData, int face) {
        final int[] xVertices = model.getVerticesX();
        final int[] yVertices = model.getVerticesY();
        final int[] zVertices = model.getVerticesZ();
        final int triA = model.getFaceIndices1()[face];
        final int triB = model.getFaceIndices2()[face];
        final int triC = model.getFaceIndices3()[face];

        return new int[]{
                xVertices[triA],
                yVertices[triA],
                zVertices[triA],
                modelData.getColorForFace(face, 3) | modelData.getColorForFace(face, 0),
                xVertices[triB],
                yVertices[triB],
                zVertices[triB],
                modelData.getColorForFace(face, 3) | modelData.getColorForFace(face, 1),
                xVertices[triC],
                yVertices[triC],
                zVertices[triC],
                modelData.getColorForFace(face, 3) | modelData.getColorForFace(face, 2),
        };
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
        return new float[]{
                xVertexNormals[triA],
                yVertexNormals[triA],
                zVertexNormals[triA],
                0,
                xVertexNormals[triB],
                yVertexNormals[triB],
                zVertexNormals[triB],
                0,
                xVertexNormals[triC],
                yVertexNormals[triC],
                zVertexNormals[triC],
                0
        };
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

            return new float[]{
                    packedMaterialData,
                    uv[idx],
                    uv[idx + 1],
                    0,
                    packedMaterialData,
                    uv[idx + 2],
                    uv[idx + 3],
                    0,
                    packedMaterialData,
                    uv[idx + 4],
                    uv[idx + 5],
                    0
            };
        } else if (material != null) {
            final int triA = model.getFaceIndices1()[face];
            final int triB = model.getFaceIndices2()[face];
            final int triC = model.getFaceIndices3()[face];

            final int[] xVertices = model.getVerticesX();
            final int[] zVertices = model.getVerticesZ();

            material = proceduralGenerator.getSeasonalMaterial(material);
            int packedMaterialData = packMaterialData(Material.getIndexFromDiffuseID(material.getDiffuseMapId()), false);

            if (objectProperties.getUvType() == UvType.GROUND_PLANE) {
                return new float[]{
                        packedMaterialData,
                        (xVertices[triA] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE,
                        (zVertices[triA] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE,
                        0,
                        packedMaterialData,
                        (xVertices[triB] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE,
                        (zVertices[triB] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE,
                        0,
                        packedMaterialData,
                        (xVertices[triC] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE,
                        (zVertices[triC] % Perspective.LOCAL_TILE_SIZE) / (float) Perspective.LOCAL_TILE_SIZE,
                        0
                };
            } else {
                return new float[]{
                        packedMaterialData,
                        0,
                        0,
                        0,
                        packedMaterialData,
                        1,
                        0,
                        0,
                        packedMaterialData,
                        0,
                        1,
                        0
                };
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
            return new ModelData().setColors(getColorsForModel(model, objectProperties, objectType, tileX, tileY, tileZ, faceCount));
        }

        // note for future spelunkers:
        // this hash code is accurate for caching the model colors but will probably need to be expanded if you're attempting to include other data
        int hash = hasher.hashCode(new int[]{ hasher.hashCode(model.getFaceColors1()), hasher.hashCode(model.getFaceColors2()), hasher.hashCode(model.getFaceColors3()) });

        ModelData modelData = modelCache.get(hash);
        if (modelData == null) {
            modelData = new ModelData().setColors(getColorsForModel(model, objectProperties, objectType, tileX, tileY, tileZ, faceCount));
            modelCache.put(hash, modelData);
        }

        return modelData;
    }

    private int[] getColorsForModel(Model model, ObjectProperties objectProperties, ObjectType objectType, int tileX, int tileY, int tileZ, int faceCount) {
        ArrayList<Integer> modelColors = new ArrayList<>();

        for (int face = 0; face < faceCount; face++) {
            Arrays.stream(getColorsForFace(model, objectProperties, objectType, tileX, tileY, tileZ, face)).forEach(modelColors::add);
        }

        return modelColors.stream().mapToInt(i -> i).toArray();
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
            return new int[] {
                    0,
                    0,
                    0,
                    0xFF << 24
            };
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

        return new int[]{
                color1,
                color2,
                color3,
                packedAlphaPriority
        };
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
