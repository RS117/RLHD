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

#define PI 3.1415926535897932384626433832795f
#define UNIT PI / 1024.0f

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

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

uniform mat4 projectionMatrix;
uniform mat4 lightProjectionMatrix;

in ivec3 vPosition[];
in vec4 vNormal[];
in vec4 vColor[];
in vec4 vUv[];
in float vFogAmount[];

out float fogAmount;
out vec4 vColor1;
out vec4 vColor2;
out vec4 vColor3;
flat out vec2 vUv1;
flat out vec2 vUv2;
flat out vec2 vUv3;
out vec3 normals;
out vec3 position;
out vec3 texBlend;
flat out ivec3 materialId;
flat out ivec3 waterData;
flat out ivec3 isOverlay;

out vec4 shadowOut;

void main() {
    int material1 = int(vUv[0].x) >> 1;
    int material2 = int(vUv[1].x) >> 1;
    int material3 = int(vUv[2].x) >> 1;
    materialId = ivec3(material1, material2, material3);

    waterData = ivec3(int(vNormal[0].w), int(vNormal[1].w), int(vNormal[2].w));

    isOverlay = ivec3(0, 0, 0);
    isOverlay[0] = int(vUv[0].x) & 1;
    isOverlay[1] = int(vUv[1].x) & 1;
    isOverlay[2] = int(vUv[2].x) & 1;

    vColor1 = vColor[0];
    vColor2 = vColor[1];
    vColor3 = vColor[2];

    vUv1 = vUv[0].yz;
    vUv2 = vUv[1].yz;
    vUv3 = vUv[2].yz;

    // fast normals
    vec3 a = vPosition[0] - vPosition[1];
    vec3 b = vPosition[0] - vPosition[2];
    vec3 flatNormals = normalize(cross(a,b));

    texBlend = vec3(1, 0, 0);
    fogAmount = vFogAmount[0];
    gl_Position = projectionMatrix * vec4(vPosition[0], 1.f);
    shadowOut = lightProjectionMatrix * vec4(vPosition[0], 1.f);
    if (abs(vNormal[0].x) < 0.01 && abs(vNormal[0].y) < 0.01 && abs(vNormal[0].z) < 0.01)
    {
        normals = flatNormals;
    }
    else
    {
        normals = vNormal[0].xyz;
    }
    position = vPosition[0];
    EmitVertex();



    texBlend = vec3(0, 1, 0);
    fogAmount = vFogAmount[1];
    gl_Position = projectionMatrix * vec4(vPosition[1], 1.f);
    shadowOut = lightProjectionMatrix * vec4(vPosition[1], 1.f);
    if (abs(vNormal[1].x) < 0.01 && abs(vNormal[1].y) < 0.01 && abs(vNormal[1].z) < 0.01)
    {
        normals = flatNormals;
    }
    else
    {
        normals = vNormal[1].xyz;
    }
    position = vPosition[1];
    EmitVertex();



    texBlend = vec3(0, 0, 1);
    fogAmount = vFogAmount[2];
    gl_Position = projectionMatrix * vec4(vPosition[2], 1.f);
    shadowOut = lightProjectionMatrix * vec4(vPosition[2], 1.f);
    if (abs(vNormal[2].x) < 0.01 && abs(vNormal[2].y) < 0.01 && abs(vNormal[2].z) < 0.01)
    {
        normals = flatNormals;
    }
    else
    {
        normals = vNormal[2].xyz;
    }
    position = vPosition[2];
    EmitVertex();


    EndPrimitive();
}