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

vec3 hslToRgb(int hsl) {
  int var5 = hsl / 128;
  float var6 = float(var5 >> 3) / 64.0f + 0.0078125f;
  float var8 = float(var5 & 7) / 8.0f + 0.0625f;

  int var10 = hsl % 128;

  float var11 = float(var10) / 128.0f;
  float var13 = var11;
  float var15 = var11;
  float var17 = var11;

  if(var8 != 0.0f) {
    float var19;
    if(var11 < 0.5f) {
      var19 = var11 * (1.0f + var8);
    } else {
      var19 = var11 + var8 - var11 * var8;
    }

    float var21 = 2.0f * var11 - var19;
    float var23 = var6 + 0.3333333333333333f;
    if(var23 > 1.0f) {
      var23 -= 1.f;
    }

    float var27 = var6 - 0.3333333333333333f;
    if(var27 < 0.0f) {
      var27 += 1.f;
    }

    if(6.0f * var23 < 1.0f) {
      var13 = var21 + (var19 - var21) * 6.0f * var23;
    } else if(2.0f * var23 < 1.0f) {
      var13 = var19;
    } else if(3.0f * var23 < 2.0f) {
      var13 = var21 + (var19 - var21) * (0.6666666666666666f - var23) * 6.0f;
    } else {
      var13 = var21;
    }

    if(6.0f * var6 < 1.0f) {
      var15 = var21 + (var19 - var21) * 6.0f * var6;
    } else if(2.0f * var6 < 1.0f) {
      var15 = var19;
    } else if(3.0f * var6 < 2.0f) {
      var15 = var21 + (var19 - var21) * (0.6666666666666666f - var6) * 6.0f;
    } else {
      var15 = var21;
    }

    if(6.0f * var27 < 1.0f) {
      var17 = var21 + (var19 - var21) * 6.0f * var27;
    } else if(2.0f * var27 < 1.0f) {
      var17 = var19;
    } else if(3.0f * var27 < 2.0f) {
      var17 = var21 + (var19 - var21) * (0.6666666666666666f - var27) * 6.0f;
    } else {
      var17 = var21;
    }
  }

  vec3 rgb = vec3(var13, var15, var17);

  return rgb;
}

// All components are in the range [0…1], including hue.
// Source: http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 rgbToHsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

// All components are in the range [0…1], including hue.
// Source: http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 hsvToRgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 linearToGamma(vec3 c)
{
    float gamma = 2.2;
    return pow(c, vec3(1.0 / gamma));
}

vec3 gammaToLinear(vec3 c)
{
    float gamma = 2.2;
    return pow(c, vec3(gamma));
}
