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
package rs117.hd.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import rs117.hd.utils.HDUtils;

@Getter
@RequiredArgsConstructor
public enum DefaultSkyColor
{
	DEFAULT("117HD (Blue)", 185, 214, 255),
	RUNELITE("RuneLite Skybox", -1, -1, -1),
	OSRS("Old School (Black)", 0, 0, 0),
	HD2008("2008 HD (Tan)", 200, 192, 169);

	private final String name;
	private final int r;
	private final int g;
	private final int b;

	@Override
	public String toString()
	{
		return name;
	}

	public float[] getRgb(Client client) {
		int r = this.r;
		int g = this.g;
		int b = this.b;
		if (this == RUNELITE)
		{
			int sky = client.getSkyboxColor();
			r = sky >> 16 & 0xFF;
			g = sky >> 8 & 0xFF;
			b = sky & 0xFF;
		}
		return new float[]{
			HDUtils.srgbToLinear(r / 255f),
			HDUtils.srgbToLinear(g / 255f),
			HDUtils.srgbToLinear(b / 255f)
		};
	}
}
