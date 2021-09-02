/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2021, 117 <https://twitter.com/117scape>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#version 330

#define MAX_MATERIALS 200
#define MAX_LIGHTS 100

layout(std140) uniform uniforms {
    int cameraYaw;
    int cameraPitch;
    int centerX;
    int centerY;
    int zoom;
    int cameraX;
    int cameraY;
    int cameraZ;
    ivec2 sinCosTable[2048];
};

struct Material
{
    int diffuseMapId;
    float specularStrength;
    float specularGloss;
    float emissiveStrength;
    int displacementMapId;
    float displacementStrength;
    ivec2 displacementDuration;
    ivec2 scrollDuration;
    vec2 textureScale;
};

layout(std140) uniform materials {
    Material material[MAX_MATERIALS];
};

struct PointLight
{
    ivec3 position;
    float size;
    ivec3 color;
    float strength;
};

layout(std140) uniform pointLights {
    PointLight pointLight[MAX_LIGHTS];
};

uniform sampler2D shadowMap;

uniform sampler2DArray texturesHD;
uniform vec2 textureOffsets[128];
uniform float animationCurrent;
uniform int colorBlindMode;
uniform vec4 fogColor;
uniform int fogDepth;
uniform vec3 waterColorLight;
uniform vec3 waterColorMid;
uniform vec3 waterColorDark;
uniform vec3 ambientColor;
uniform float ambientStrength;
uniform vec3 lightColor;
uniform float lightStrength;
uniform vec3 underglowColor;
uniform float underglowStrength;
uniform float groundFogStart;
uniform float groundFogEnd;
uniform float groundFogOpacity;
uniform float lightningBrightness;
uniform float lightX;
uniform float lightY;
uniform float lightZ;
uniform float shadowMaxBias;
uniform int shadowsEnabled;

// general HD settings
uniform int waterEffects;
uniform float saturation;
uniform float contrast;

uniform int pointLightsCount; // number of lights in current frame

in vec4 shadowOut;
in float fogAmount;
in vec4 vColor1;
in vec4 vColor2;
in vec4 vColor3;
flat in vec2 vUv1;
flat in vec2 vUv2;
flat in vec2 vUv3;
in vec3 normals;
in vec3 position;
in vec3 texBlend;
flat in ivec3 materialId;
flat in ivec3 waterData;
flat in ivec3 isOverlay;

out vec4 FragColor;

#include color_utils.glsl
#include lighting.glsl
#include utils.glsl
#include colorblind.glsl

#define WATER 1
#define SWAMP_WATER 3
#define POISON_WASTE 5
#define BLOOD 7
#define ICE 8

void main() {
    vec3 camPos = vec3(cameraX, cameraY, cameraZ);
    vec3 downDir = normalize(vec3(0, -1.0, 0));
    vec3 viewDir = normalize(camPos - position);
    vec3 lightDir = normalize(vec3(lightX, lightY, lightZ));

    Material material1 = material[materialId.x];
    Material material2 = material[materialId.y];
    Material material3 = material[materialId.z];

    // material data
    int bmaterial1 = materialId.x;
    int bmaterial2 = materialId.y;
    int bmaterial3 = materialId.z;

    // water data
    int waterDepth1 = waterData.x >> 5;
    int waterDepth2 = waterData.y >> 5;
    int waterDepth3 = waterData.z >> 5;
    float waterDepth = waterDepth1 * texBlend.x + waterDepth2 * texBlend.y + waterDepth3 * texBlend.z;
    int underwaterType = waterData.x & 5;

    // set initial texture map ids
    int diffuseMapId1 = material1.diffuseMapId;
    int diffuseMapId2 = material2.diffuseMapId;
    int diffuseMapId3 = material3.diffuseMapId;

    // only use one displacement map
    int displacementMapId = material1.displacementMapId;

    int causticsMapId = 240;

    bool isWater = false;
    bool simpleWater = true;
    int waterType = 0;
    float waterSpecularStrength = 0.0;
    float waterSpecularGloss = 500;
    float waterNormalStrength = 0.0;
    float waterBaseOpacity = 1.0;
    float waterFresnelAmount = 0.0;
    vec3 waterSurfaceColor = vec3(1, 0, 0);
    vec3 waterFoamColor = vec3(0, 0, 0);
    int waterHasFoam = 1;
    float waterDuration = 1;
    int waterNormalMap1 = 236;
    int waterNormalMap2 = 236;

    if (
   diffuseMapId1 == 1 || diffuseMapId1 == 24 || diffuseMapId1 == 7001 || diffuseMapId1 == 7024 ||
   diffuseMapId2 == 1 || diffuseMapId2 == 24 || diffuseMapId2 == 7001 || diffuseMapId2 == 7024 ||
   diffuseMapId3 == 1 || diffuseMapId3 == 24 || diffuseMapId3 == 7001 || diffuseMapId3 == 7024)
    {
        isWater = true;
        waterType = WATER;
    }
    else if (
    diffuseMapId1 == 25 || diffuseMapId1 == 7025 ||
    diffuseMapId2 == 25 || diffuseMapId2 == 7025 ||
    diffuseMapId3 == 25 || diffuseMapId3 == 7025)
    {
        isWater = true;
        waterType = SWAMP_WATER;
    }
    else if (
    diffuseMapId1 == 998 || diffuseMapId1 == 7998 ||
    diffuseMapId2 == 998 || diffuseMapId2 == 7998 ||
    diffuseMapId3 == 998 || diffuseMapId3 == 7998)
    {
        isWater = true;
        waterType = POISON_WASTE;
    }
    else if (
    diffuseMapId1 == 999 || diffuseMapId1 == 7999 ||
    diffuseMapId2 == 999 || diffuseMapId2 == 7999 ||
    diffuseMapId3 == 999 || diffuseMapId3 == 7999)
    {
        isWater = true;
        waterType = BLOOD;
    }
    else if (
    diffuseMapId1 == 997 || diffuseMapId1 == 7997 ||
    diffuseMapId2 == 997 || diffuseMapId2 == 7997 ||
    diffuseMapId3 == 997 || diffuseMapId3 == 7997)
    {
        isWater = true;
        waterType = ICE;
    }

    if (isWater)
    {
        if (diffuseMapId1 >= 7000 || diffuseMapId2 >= 7000 || diffuseMapId3 >= 7000)
        {
            simpleWater = false;
        }

        switch (waterType)
        {
            case WATER:
            waterSpecularStrength = 0.5;
            waterSpecularGloss = 500;
            waterNormalStrength = 0.09;
            waterBaseOpacity = 0.5;
            waterFresnelAmount = 1.0;
            waterSurfaceColor = vec3(1, 1, 1);
            waterFoamColor = vec3(176, 164, 146);
            waterHasFoam = 1;
            waterDuration = 1;
            waterNormalMap1 = 236;
            waterNormalMap2 = 236;
            break;
            case SWAMP_WATER:
            waterSpecularStrength = 0.1;
            waterSpecularGloss = 100;
            waterNormalStrength = 0.05;
            waterBaseOpacity = 0.8;
            waterFresnelAmount = 0.3;
            waterSurfaceColor = vec3(38, 58, 31) / 255.0;
            waterFoamColor = vec3(115, 120, 101);
            waterHasFoam = 1;
            waterDuration = 1.2;
            waterNormalMap1 = 236;
            waterNormalMap2 = 236;
            break;
            case POISON_WASTE:
            waterSpecularStrength = 0.1;
            waterSpecularGloss = 100;
            waterNormalStrength = 0.05;
            waterBaseOpacity = 0.9;
            waterFresnelAmount = 0.3;
            waterSurfaceColor = vec3(37, 38, 35) / 255.0;
            waterFoamColor = vec3(106, 108, 100);
            waterHasFoam = 1;
            waterDuration = 1.6;
            waterNormalMap1 = 236;
            waterNormalMap2 = 236;
            break;
            case BLOOD:
            waterSpecularStrength = 0.5;
            waterSpecularGloss = 500;
            waterNormalStrength = 0.05;
            waterBaseOpacity = 0.8;
            waterFresnelAmount = 0.3;
            waterSurfaceColor = vec3(69, 2, 2) / 255.0;
            waterFoamColor = vec3(117, 63, 45);
            waterHasFoam = 1;
            waterDuration = 2;
            waterNormalMap1 = 236;
            waterNormalMap2 = 236;
            break;
            case ICE:
            waterSpecularStrength = 0.3;
            waterSpecularGloss = 200;
            waterNormalStrength = 0.04;
            waterBaseOpacity = 0.85;
            waterFresnelAmount = 1.0;
            waterSurfaceColor = vec3(1, 1, 1);
            waterFoamColor = vec3(150, 150, 150);
            waterHasFoam = 1;
            waterDuration = 0;
            waterNormalMap1 = 246;
            waterNormalMap2 = 246;
            break;
        }
    }

    if (isWater)
    {
        diffuseMapId1 = waterNormalMap1; // wave normal map 1
        diffuseMapId2 = waterNormalMap2; // wave normal map 2
        diffuseMapId3 = 238; // foam diffuse map
        displacementMapId = 237; // wave displacement map
    }

    bool isUnderwater = false;
    vec3 waterDepthColor = vec3(0, 0, 0);
    if (underwaterType != 0)
    {
        isUnderwater = true;

        if (underwaterType == WATER)
        {
            waterDepthColor = vec3(0, 117, 142) / 255.0;
        }
        else if (underwaterType == SWAMP_WATER)
        {
            waterDepthColor = vec3(41, 82, 26) / 255.0;
        }
        else if (underwaterType == POISON_WASTE)
        {
            waterDepthColor = vec3(50, 52, 46) / 255.0;
        }
        else if (underwaterType == BLOOD)
        {
            waterDepthColor = vec3(50, 26, 22) / 255.0;
        }
    }
    if (isUnderwater)
    {
        displacementMapId = 239;
    }




    vec3 compositeColor = vec3(0.5);
    float alpha = 1;

    vec2 blendedUv = vUv1 * texBlend.x + vUv2 * texBlend.y + vUv3 * texBlend.z;
    vec2 baseUv1 = blendedUv;
    vec2 baseUv2 = blendedUv;
    vec2 baseUv3 = blendedUv;

    vec4 fragColor = vColor1 * texBlend.x + vColor2 * texBlend.y + vColor3 * texBlend.z;

    vec2 uv1 = baseUv1 + textureOffsets[diffuseMapId1];
    vec2 uv2 = baseUv2 + textureOffsets[diffuseMapId2];
    vec2 uv3 = baseUv3 + textureOffsets[diffuseMapId3];

    uv1 = vec2((uv1.x - 0.5) / material1.textureScale.x + 0.5, (uv1.y - 0.5) / material1.textureScale.y + 0.5);
    uv2 = vec2((uv2.x - 0.5) / material2.textureScale.x + 0.5, (uv2.y - 0.5) / material2.textureScale.y + 0.5);
    uv3 = vec2((uv3.x - 0.5) / material3.textureScale.x + 0.5, (uv3.y - 0.5) / material3.textureScale.y + 0.5);

    // water uvs
    if (isWater)
    {
        uv1 = vec2(-worldUvs(5).y + animationFrame(31 * waterDuration),
        worldUvs(5).x + animationFrame(31 * waterDuration));
        uv2 = vec2(worldUvs(3).y - animationFrame(24 * waterDuration),
        worldUvs(3).x - animationFrame(24 * waterDuration));
    }

    uv1 -= vec2(animationFrame(material1.scrollDuration.x),
    animationFrame(material1.scrollDuration.y));
    uv2 -= vec2(animationFrame(material2.scrollDuration.x),
    animationFrame(material2.scrollDuration.y));
    uv3 -= vec2(animationFrame(material3.scrollDuration.x),
    animationFrame(material3.scrollDuration.y));

    // get displacement map
    vec2 displacementUv = vec2(baseUv1.x - animationFrame(material1.displacementDuration.x),
    baseUv1.y - animationFrame(material1.displacementDuration.y));
    float displacementStrength = material1.displacementStrength;
    if (isWater)
    {
        displacementUv = vec2(worldUvs(15).x + animationFrame(50 * waterDuration), worldUvs(15).y + animationFrame(50 * waterDuration));
        displacementStrength = 0.025;
    }
    if (isUnderwater)
    {
        displacementUv = vec2(worldUvs(1.5).x + animationFrame(10 * waterDuration), worldUvs(1.5).y - animationFrame(10 * waterDuration));
        displacementStrength = 0.075;
    }
    vec2 displacement = texture(texturesHD, vec3(displacementUv, displacementMapId)).xy;
    uv1 += displacement * displacementStrength;
    uv2 += displacement * displacementStrength;
    uv3 += displacement * displacementStrength;
    if (isWater)
    {
        uv1 = vec2(worldUvs(2).x + animationFrame(20 * waterDuration) + displacement.x * displacementStrength,
        worldUvs(2).y + animationFrame(20 * waterDuration) + displacement.y * displacementStrength);
        uv1 = vec2(worldUvs(3).x - animationFrame(28 * waterDuration) - displacement.x * displacementStrength,
        worldUvs(3).y + animationFrame(28 * waterDuration) + displacement.y * displacementStrength);
    }

    // get emissive output
    float emissive1 = clamp(material1.emissiveStrength, 0.0, 1.0);
    float emissive2 = clamp(material2.emissiveStrength, 0.0, 1.0);
    float emissive3 = clamp(material3.emissiveStrength, 0.0, 1.0);

    // get vertex colors
    vec4 flatColor = vec4(0.5, 0.5, 0.5, 1.0);
    vec4 color1 = vColor1;
    vec4 color2 = vColor2;
    vec4 color3 = vColor3;

    // apply emissive output to color
    color1 = vec4(mix(color1.rgb, vec3(1.0), emissive1), color1.a);
    color2 = vec4(mix(color2.rgb, vec3(1.0), emissive2), color2.a);
    color3 = vec4(mix(color3.rgb, vec3(1.0), emissive3), color3.a);

    // get diffuse textures
    vec4 diffuse1 = vec4(1.0);
    vec4 diffuse2 = vec4(1.0);
    vec4 diffuse3 = vec4(1.0);
    diffuse1 = texture(texturesHD, vec3(uv1, diffuseMapId1));
    diffuse2 = texture(texturesHD, vec3(uv2, diffuseMapId2));
    diffuse3 = texture(texturesHD, vec3(uv3, diffuseMapId3));

    ivec3 isOverlay = isOverlay;
    int overlayCount = isOverlay[0] + isOverlay[1] + isOverlay[2];
    ivec3 isUnderlay = ivec3(1) - isOverlay;
    int underlayCount = isUnderlay[0] + isUnderlay[1] + isUnderlay[2];

    // calculate blend amounts for overlay and underlay vertices
    vec3 underlayBlend = texBlend * isUnderlay;
    vec3 overlayBlend = texBlend * isOverlay;

    if (underlayCount == 0 || overlayCount == 0)
    {
        // if a tile has all overlay or underlay vertices,
        // use the default blend

        underlayBlend = texBlend;
        overlayBlend = texBlend;
    }
    else
    {
        // if there's a mix of overlay and underlay vertices,
        // calculate custom blends for each 'layer'

        float underlayBlendMultiplier = 1.0 / (underlayBlend[0] + underlayBlend[1] + underlayBlend[2]);
        // adjust back to 1.0 total
        underlayBlend *= underlayBlendMultiplier;

        float overlayBlendMultiplier = 1.0 / (overlayBlend[0] + overlayBlend[1] + overlayBlend[2]);
        // adjust back to 1.0 total
        overlayBlend *= overlayBlendMultiplier;
    }


    // get fragment colors by combining vertex colors and texture samples
    vec4 texA = color1;
    if (diffuseMapId1 > -0.5)
    {
        texA = vec4(diffuse1.rgb * texA.rgb, min(diffuse1.a, color1.a));
    }
    vec4 texB = color2;
    if (diffuseMapId2 > -0.5)
    {
        texB = vec4(diffuse2.rgb * texB.rgb, min(diffuse2.a, color2.a));
    }
    vec4 texC = color3;
    if (diffuseMapId3 > -0.5)
    {
        texC = vec4(diffuse3.rgb * texC.rgb, min(diffuse3.a, color3.a));
    }

    // combine fragment colors based on each blend, creating
    // one color for each overlay/underlay 'layer'
    vec4 underlayA = texA * underlayBlend[0];
    vec4 underlayB = texB * underlayBlend[1];
    vec4 underlayC = texC * underlayBlend[2];

    vec4 underlayColor = underlayA + underlayB + underlayC;

    vec4 overlayA = texA * overlayBlend[0];
    vec4 overlayB = texB * overlayBlend[1];
    vec4 overlayC = texC * overlayBlend[2];

    vec4 overlayColor = overlayA + overlayB + overlayC;




    float overlayMix = 0;

    if (overlayCount == 3)
    {
        overlayMix = 0;
    }
    else if (overlayCount == 0)
    {
        overlayMix = 0;
    }
    else
    {
        // custom blending logic for blending overlays into underlays
        // in a style similar to 2008+ HD

        // fragment UV
        vec2 fragUv = baseUv1;
        // standalone UV
        // e.g. if there are 2 overlays and 1 underlay, the underlay is the standalone
        vec2 uvA = vec2(-999);
        // opposite UV A
        vec2 uvB = vec2(-999);
        // opposite UV B
        vec2 uvC = vec2(-999);
        bool inverted = false;

        // assign standalone UV to uvA and others to uvB, uvC
        for (int i = 0; i < 3; i++)
        {
            vec2 uv;

            if (i == 0)
            {
                uv = vUv1;
            }
            else if (i == 1)
            {
                uv = vUv2;
            }
            else if (i == 2)
            {
                uv = vUv3;
            }

            if ((isOverlay[i] == 1 && overlayCount == 1) || (isUnderlay[i] == 1 && underlayCount == 1))
            {
                // assign standalone vertex UV to uvA
                uvA = uv;

                if (overlayCount == 1)
                {
                    // we use this at the end of this logic to invert
                    // the result if there's 1 overlay, 2 underlay
                    // vs the default result from 1 underlay, 2 overlay
                    inverted = true;
                }
            }
            else
            {
                // assign opposite vertex UV to uvB or uvC
                if (uvB == vec2(-999))
                {
                    uvB = uv;
                }
                else
                {
                    uvC = uv;
                }
            }
        }

        // point on side perpendicular to uvA
        vec2 oppositePoint = uvB + pointToLine(uvB, uvC, uvA) * (uvC - uvB);

        // calculate position of fragment's UV relative to
        // line between uvA and oppositePoint
        float result = pointToLine(uvA, oppositePoint, fragUv);

        if (inverted)
        {
            result = 1 - result;
        }

        result = clamp(result, 0, 1);

        float distance = distance(uvA, oppositePoint);

        float cutoff = 0.5;

        result = (result - (1.0 - cutoff)) * (1.0 / cutoff);
        result = clamp(result, 0, 1);

        float maxDistance = 2.5;
        if (distance > maxDistance)
        {
            float multi = distance / maxDistance;
            result = 1.0 - ((1.0 - result) * multi);
            result = clamp(result, 0, 1);
        }

        overlayMix = result;
    }

    vec4 texColor = mix(underlayColor, overlayColor, overlayMix);

    compositeColor = texColor.rgb;
    alpha = texColor.a;

    // blend emissive properties
    float emissive = emissive1 * texBlend[0] + emissive2 * texBlend[1] + emissive3 * texBlend[2];

    // normals
    vec3 normals = normalize(normals);
    if (isWater)
    {
        vec3 norm1 = -vec3((diffuse1.x * 2 - 1) * waterNormalStrength, diffuse1.z, (diffuse1.y * 2 - 1) * waterNormalStrength);
        vec3 norm2 = -vec3((diffuse2.x * 2 - 1) * waterNormalStrength, diffuse2.z, (diffuse2.y * 2 - 1) * waterNormalStrength);
        normals = normalize(norm1 + norm2);
    }




    float lightDotNormals = dot(normals, lightDir);
    float downDotNormals = dot(downDir, normals);
    float viewDotNormals = dot(viewDir, normals);




    // sample shadow map
    float shadow = 0.0;
    if (shadowsEnabled == 1)
    {
        vec3 projCoords = shadowOut.xyz / shadowOut.w * 0.5 + 0.5;
        if (isWater || isUnderwater)
        {
            projCoords += vec3(displacement * 0.00075, 0.0);
        }
        float currentDepth = projCoords.z;
        float shadowMinBias = 0.0005f;
        float shadowBias = max(shadowMaxBias * (1.0 - lightDotNormals), shadowMinBias);
        vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
        for(int x = -1; x <= 1; ++x)
        {
            for(int y = -1; y <= 1; ++y)
            {
                float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
                shadow += currentDepth - shadowBias > pcfDepth ? 1.0 : 0.0;
            }
        }
        shadow /= 9.0;

        // fade out shadows near shadow texture edges
        float cutoff = 0.1;
        if (projCoords.x <= cutoff)
        {
            float amt = projCoords.x / cutoff;
            shadow = mix(0.0, shadow, amt);
        }
        if (projCoords.y <= cutoff)
        {
            float amt = projCoords.y / cutoff;
            shadow = mix(0.0, shadow, amt);
        }
        if (projCoords.x >= 1.0 - cutoff)
        {
            float amt = 1.0 - ((projCoords.x - (1.0 - cutoff)) / cutoff);
            shadow = mix(0.0, shadow, amt);
        }
        if (projCoords.y >= 1.0 - cutoff)
        {
            float amt = 1.0 - ((projCoords.y - (1.0 - cutoff)) / cutoff);
            shadow = mix(0.0, shadow, amt);
        }

        shadow = clamp(shadow, 0.0, 1.0);
        shadow = projCoords.z > 1.0 ? 0.0 : shadow;
    }
    float inverseShadow = 1.0 - shadow;



    // specular
    vec3 vSpecularGloss = vec3(material1.specularGloss, material2.specularGloss, material3.specularGloss);
    vec3 vSpecularStrength = vec3(material1.specularStrength, material2.specularStrength, material3.specularStrength);
    // apply specular highlights to anything semi-transparent
    // this isn't always desirable but adds subtle light reflections to windows, etc.
    if (color1.a < 0.99)
    {
        vSpecularGloss.x = 30.0;
        vSpecularStrength.x = clamp((1.0 - color1.a) * 2, 0.0, 1.0);
    }
    if (color2.a < 0.99)
    {
        vSpecularGloss.y = 30.0;
        vSpecularStrength.y = clamp((1.0 - color2.a) * 2, 0.0, 1.0);
    }
    if (color3.a < 0.99)
    {
        vSpecularGloss.z = 30.0;
        vSpecularStrength.z = clamp((1.0 - color3.a) * 2, 0.0, 1.0);
    }
    float combinedSpecularStrength = vSpecularStrength[0] * texBlend[0] + vSpecularStrength[1] * texBlend[1] + vSpecularStrength[2] * texBlend[2];
    if (isWater)
    {
        vSpecularStrength = vec3(waterSpecularStrength);
        vSpecularGloss = vec3(waterSpecularGloss);
        combinedSpecularStrength = waterSpecularStrength;
    }


    // calculate lighting

    // ambient light
    vec3 ambientLightOut = ambientStrength * ambientColor;

    // directional light
    vec3 lightColor = lightColor;
    float lightStrength = lightStrength * inverseShadow;
    vec3 light = lightColor * lightStrength;
    vec3 lightOut = max(lightDotNormals, 0.0) * light;

    // directional light specular
    vec3 lightReflectDir = reflect(-lightDir, normals);
    vec3 lightSpecularOut = specular(viewDir, lightReflectDir, vSpecularGloss, vSpecularStrength, lightColor, lightStrength).rgb;


    // point lights
    vec3 pointLightsOut = vec3(0.0);
    vec3 pointLightsSpecularOut = vec3(0.0);
    for (int i = 0; i < pointLightsCount; i++)
    {
        vec3 pointLightPos = vec3(pointLight[i].position.x, pointLight[i].position.z, pointLight[i].position.y);
        vec3 pointLightColor = vec3(pointLight[i].color.r / 255.0, pointLight[i].color.g / 255.0, pointLight[i].color.b / 255.0);
        float pointLightStrength = pointLight[i].strength;
        float pointLightSize = pointLight[i].size;
        float distanceToLightSource = length(pointLightPos - position);
        vec3 pointLightDir = normalize(pointLightPos - position);

        if (distanceToLightSource <= pointLightSize)
        {
            float pointLightDotNormals = dot(normals, pointLightDir);
            vec3 pointLightOut = pointLightColor * pointLightStrength * max(pointLightDotNormals, 0.0);

            float attenuation = pow(clamp(1 - (distanceToLightSource / pointLightSize), 0.0, 1.0), 2.0);
            pointLightOut *= attenuation;

            pointLightsOut += pointLightOut;

            vec3 pointLightReflectDir = reflect(-pointLightDir, normals);
            vec4 spec = specular(viewDir, pointLightReflectDir, vSpecularGloss, vSpecularStrength, pointLightColor, pointLightStrength) * attenuation;
            pointLightsSpecularOut += spec.rgb;
        }
    }


    // sky light
    vec3 skyLightColor = vec3(0, 0.5, 1.0);
    skyLightColor = fogColor.rgb;
    float skyLightStrength = 0.5;
    float skyDotNormals = downDotNormals;
    vec3 skyLightOut = max(skyDotNormals, 0.0) * skyLightColor * skyLightStrength;


    // lightning
    vec3 lightningColor = vec3(1.0, 1.0, 1.0);
    float lightningStrength = lightningBrightness;
    float lightningDotNormals = downDotNormals;
    vec3 lightningOut = max(lightningDotNormals, 0.0) * lightningColor * lightningStrength;


    // underglow
    vec3 underglowOut = underglowColor * max(normals.y, 0) * underglowStrength;


    // fresnel reflection
    float baseOpacity = 0.4;
    float fresnel = 1.0 - clamp(viewDotNormals, 0.0, 1.0);
    float finalFresnel = clamp(mix(baseOpacity, 1.0, fresnel * 1.2), 0.0, 1.0);
    vec3 surfaceColor = vec3(0);
    // add sky gradient
    if (finalFresnel < 0.5)
    {
        surfaceColor = mix(waterColorDark, waterColorMid, finalFresnel * 2);
    }
    else
    {
        surfaceColor = mix(waterColorMid, waterColorLight, (finalFresnel - 0.5) * 2);
    }
    vec3 surfaceColorOut = surfaceColor * max(combinedSpecularStrength, 0.2);


    // apply lighting
    vec3 compositeLight = ambientLightOut + lightOut + lightSpecularOut + skyLightOut + lightningOut +
    underglowOut + pointLightsOut + pointLightsSpecularOut + surfaceColorOut;


    if (isWater)
    {
        vec3 baseColor = mix(waterSurfaceColor * compositeLight, surfaceColor, waterFresnelAmount);
        float shadowDarken = 0.15;
        baseColor *= (1.0 - shadowDarken) + inverseShadow * shadowDarken;
        float foamAmount = 1.0 - fragColor.r;
        float foamDistance = 0.7;
        vec3 foamColor = waterFoamColor / 255.0;
        foamColor = foamColor * diffuse3.rgb * compositeLight;
        foamAmount = clamp(pow(1.0 - ((1.0 - foamAmount) / foamDistance), 3), 0.0, 1.0) * waterHasFoam;
        foamAmount *= foamColor.r;
        baseColor = mix(baseColor, foamColor, foamAmount);
        vec3 specularComposite = mix(lightSpecularOut, vec3(0.0), foamAmount);
        float flatFresnel = (1.0 - dot(viewDir, downDir)) * 1.0;
        finalFresnel = max(finalFresnel, flatFresnel);
        finalFresnel -= finalFresnel * shadow * 0.2;
        baseColor += pointLightsSpecularOut + lightSpecularOut;
        alpha = max(waterBaseOpacity, max(foamAmount, max(finalFresnel, length(specularComposite))));
        compositeColor = baseColor;
    }
    else
    {
        vec3 litColor = compositeColor * compositeLight;
        compositeColor = mix(litColor, compositeColor, emissive);
    }


    if (isUnderwater)
    {
        // underwater terrain
        float lowestColorLevel = 500.0;
        float midColorLevel = 150.0;
        float depth = waterDepth; // e.g. 200
        float surfaceLevel = position.y - waterDepth; // e.g. -1600

        vec3 mixed = vec3(1);

        if (depth < midColorLevel)
        {
            mixed = mix(compositeColor, compositeColor * waterDepthColor, translateRange(0.0, midColorLevel, depth));
        }
        else if (depth < lowestColorLevel)
        {
            mixed = mix(compositeColor * waterDepthColor, vec3(0.0), translateRange(midColorLevel, lowestColorLevel, depth));
        }
        else
        {
            mixed = vec3(0.0);
        }
        compositeColor = mixed;


        // caustics
        float maxCausticsDepth = 100;
        maxCausticsDepth += surfaceLevel;
        float causticsDepth = max(min((position.y - maxCausticsDepth) / (surfaceLevel - maxCausticsDepth), 1.0), 0.0);

        if (causticsDepth > 0 && lightDotNormals > 0 && underwaterType == 1)
        {
            float causticsMultiplier = 1;
            vec2 causticsUv = vec2(worldUvs(1).x + displacement.x * displacementStrength * 2, worldUvs(1).y + displacement.y * displacementStrength * 2);
            causticsUv += animationFrame(16);
            float caustics = texture(texturesHD, vec3(causticsUv, causticsMapId)).r;
            caustics *= causticsDepth * causticsMultiplier * lightStrength / 2 * lightDotNormals;
            caustics = 1.0 - clamp(caustics, 0.0, 1.0);
            compositeColor /= max(caustics, 0.001);
        }
    }


    if (isWater && simpleWater)
    {
        alpha = 1.0f;
    }


    vec3 hsv = rgbToHsv(compositeColor);

    // Apply saturation setting
    hsv.y *= saturation;

    // Apply contrast setting
    if (hsv.z > 0.5)
    {
        hsv.z = 0.5 + ((hsv.z - 0.5) * contrast);
    }
    else
    {
        hsv.z = 0.5 - ((0.5 - hsv.z) * contrast);
    }

    compositeColor = hsvToRgb(hsv);

    if (colorBlindMode > 0)
    {
        compositeColor = colorblind(colorBlindMode, compositeColor);
    }

    if (!isUnderwater)
    {
        // apply ground fog
        float distance = distance(position, camPos);
        float closeFadeDistance = 1500;
        float groundFog = 1.0 - clamp((position.y - groundFogStart) / (groundFogEnd - groundFogStart), 0.0, 1.0);
        groundFog = mix(0.0, groundFogOpacity, groundFog);
        groundFog *= clamp(distance / closeFadeDistance, 0.0, 1.0);
        if (isWater)
        {
            alpha = max(alpha, groundFog);
        }
        compositeColor = mix(compositeColor, fogColor.rgb, groundFog);
    }

    // apply distance fog
    compositeColor = mix(compositeColor, fogColor.rgb, fogAmount);

    FragColor = vec4(compositeColor, alpha);
}