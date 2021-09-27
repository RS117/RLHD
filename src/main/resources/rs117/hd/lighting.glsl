/*
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

vec4 specular(vec3 viewDir, vec3 reflectDir, vec3 specularGloss, vec3 specularStrength, vec3 lightColor, float lightStrength)
{
    float vDotR = max(dot(viewDir, reflectDir), 0.0);
    vec3 specX = vec3(pow(vDotR, specularGloss.x) * lightColor * specularStrength.x);
    vec3 specY = vec3(pow(vDotR, specularGloss.y) * lightColor * specularStrength.y);
    vec3 specZ = vec3(pow(vDotR, specularGloss.z) * lightColor * specularStrength.z);
    float specAmount = clamp(vDotR * lightStrength, 0.0, 1.0);
    vec4 combined = vec4(specX * texBlend.x + specY * texBlend.y + specZ * texBlend.z, specAmount);
    return vDotR > 0.0 ? combined : vec4(0.0);
}