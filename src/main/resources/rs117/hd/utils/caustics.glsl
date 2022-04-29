/*
 * Copyright (c) 2022, Hooder <ahooder@protonmail.com>
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

#define CAUSTICS_MAP_ID 240

float sampleCausticsChannel(const vec2 flow1, const vec2 flow2, const vec2 aberration) {
    return min(
        texture(texturesHD, vec3(flow1 + aberration, CAUSTICS_MAP_ID)).r,
        texture(texturesHD, vec3(flow2 + aberration, CAUSTICS_MAP_ID)).r
    );
}

vec3 sampleCaustics(const vec2 uv, const float aberration) {
    const ivec2 direction = ivec2(1, -1);

    vec2 flow1 = uv + animationFrame(19) * direction;
    vec2 flow2 = uv * 1.5 + animationFrame(37) * -direction;

    float r = sampleCausticsChannel(flow1, flow2, aberration * vec2( 1,  1));
    float g = sampleCausticsChannel(flow1, flow2, aberration * vec2( 1, -1));
    float b = sampleCausticsChannel(flow1, flow2, aberration * vec2(-1, -1));
    return vec3(r, g, b);
}
