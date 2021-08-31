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

// translates a value from a custom range into 0-1
float translateRange(float rangeStart, float rangeEnd, float value)
{
    return (value - rangeStart) / (rangeEnd - rangeStart);
}

// returns a value between 0-1 representing a frame of animation
// based on the length of the animation
float animationFrame(float animationDuration)
{
    if (animationDuration == 0.0)
    {
        return 0.0;
    }
    return mod(animationCurrent, animationDuration) / animationDuration;
}

vec2 worldUvs(float scale)
{
    scale *= 128.0;
    return vec2(position.z / scale, position.x / scale);
}

// used for blending overlays into underlays.
// adapted from http://www.alecjacobson.com/weblog/?p=1486
float pointToLine(vec2 lineA, vec2 lineB, vec2 point)
{
    // vector from A to B
    vec2 AB = lineB - lineA;

    // squared distance from A to B
    float ABsquared = dot(AB, AB);

    if (ABsquared == 0)
    {
        // A and B are the same point
        return 1.0;
    }
    else
    {
        // vector from A to p
        vec2 Ap = (point - lineA);
        float t = dot(Ap, AB) / ABsquared;
        return t;
    }
}
