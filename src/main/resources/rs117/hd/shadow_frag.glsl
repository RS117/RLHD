/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

uniform sampler2DArray texturesHD;
uniform vec2 textureOffsets[128];

in float alpha;
in vec2 fUv;
flat in int materialId;

out vec4 FragColor;

void main()
{
    // skip water surfaces
    switch (material[materialId].diffuseMapId)
    {
        case 7001:
        case 7025:
        case 7997:
        case 7998:
        case 7999:
            discard;
    }

    vec2 uv = fUv + textureOffsets[material[materialId].diffuseMapId];
    uv = vec2((uv.x - 0.5) / material[materialId].textureScale.x + 0.5, (uv.y - 0.5) / material[materialId].textureScale.y + 0.5);
    vec4 texture = texture(texturesHD, vec3(uv, material[materialId].diffuseMapId));

    if (min(texture.a, alpha) < 0.25)
    {
        discard;
    }

    FragColor = vec4(1.0);
}
