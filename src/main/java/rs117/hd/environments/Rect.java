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
package rs117.hd.environments;

import lombok.Getter;

@Getter
public class Rect
{
	private final int minX;
	private final int minY;
	private final int maxX;
	private final int maxY;
	private final int plane;

	Rect(int pointAX, int pointAY, int pointBX, int pointBY)
	{
		this.minX = Math.min(pointAX, pointBX);
		this.minY = Math.min(pointAY, pointBY);
		this.maxX = Math.max(pointAX, pointBX);
		this.maxY = Math.max(pointAY, pointBY);
		this.plane = -1;
	}

	Rect(int pointAX, int pointAY, int pointBX, int pointBY, int plane)
	{
		this.minX = Math.min(pointAX, pointBX);
		this.minY = Math.min(pointAY, pointBY);
		this.maxX = Math.max(pointAX, pointBX);
		this.maxY = Math.max(pointAY, pointBY);
		this.plane = plane;
	}

	public boolean containsPoint(int pointX, int pointY, int pointZ)
	{
		return pointX <= maxX && pointX >= minX && pointY <= maxY && pointY >= minY && (plane == -1 || plane == pointZ);
	}
}
