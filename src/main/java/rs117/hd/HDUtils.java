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
package rs117.hd;

import static com.jogamp.opengl.math.VectorUtil.crossVec3;
import static com.jogamp.opengl.math.VectorUtil.subVec3;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

@Slf4j
@Singleton
public
class HDUtils
{
	@Inject
	private Client client;

	@Inject
	private SceneUploader sceneUploader;

	@Inject
	private HdPlugin hdPlugin;

	@Inject
	private ProceduralGenerator proceduralGenerator;

	static float[] vectorAdd(float[] vec1, float[] vec2)
	{
		float[] out = new float[vec1.length];
		for (int i = 0; i < vec1.length; i++)
		{
			out[i] = vec1[i] + vec2[i];
		}
		return out;
	}

	static float[] vectorAdd(float[] vec1, int[] vec2) {
		float[] out = new float[vec1.length];
		for (int i = 0; i < vec1.length; i++) {
			out[i] = vec1[i] + vec2[i];
		}
		return out;
	}

	static int[] vectorAdd(int[] vec1, int[] vec2)
	{
		int[] out = new int[vec1.length];
		for (int i = 0; i < vec1.length; i++)
		{
			out[i] = vec1[i] + vec2[i];
		}
		return out;
	}

	static double[] vectorAdd(double[] vec1, double[] vec2)
	{
		double[] out = new double[vec1.length];
		for (int i = 0; i < vec1.length; i++)
		{
			out[i] = vec1[i] + vec2[i];
		}
		return out;
	}

	static Double[] vectorAdd(Double[] vec1, Double[] vec2)
	{
		Double[] out = new Double[vec1.length];
		for (int i = 0; i < vec1.length; i++)
		{
			out[i] = vec1[i] + vec2[i];
		}
		return out;
	}

	static float[] vectorDivide(float[] vec1, float divide)
	{
		float[] out = new float[vec1.length];
		for (int i = 0; i < vec1.length; i++) {
			if (divide == 0)
			{
				out[i] = 0;
			} else
			{
				out[i] = vec1[i] / divide;
			}
		}
		return out;
	}

	public static float lerp(float a, float b, float t) {
		return a + ((b - a) * t);
	}

	public static float[] lerpVectors(float[] vecA, float[] vecB, float t)
	{
		float[] out = new float[Math.min(vecA.length, vecB.length)];
		for (int i = 0; i < out.length; i++)
		{
			out[i] = lerp(vecA[i], vecB[i], t);
		}
		return out;
	}

	static int[] lerpVectors(int[] vecA, int[] vecB, float t)
	{
		int[] out = new int[Math.min(vecA.length, vecB.length)];
		for (int i = 0; i < out.length; i++)
		{
			out[i] = (int)lerp(vecA[i], vecB[i], t);
		}
		return out;
	}

	static int vertexHash(int[] vPos)
	{
		// simple custom hashing function for vertex position data
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < vPos.length; i++)
		{
			s.append(vPos[i]).append(",");
		}
		return s.toString().hashCode();
	}

	static float[] calculateSurfaceNormals(int[] vertexX, int[] vertexY, int[] vertexZ)
	{
		float[][] vPosition = new float[3][3];

		for (int i = 0; i < 3; i++)
		{
			vPosition[i][0] = vertexX[i];
			vPosition[i][1] = vertexY[i];
			vPosition[i][2] = vertexZ[i];
		}

		// calculate normals
		float[] a = subVec3(new float[3], vPosition[0], vPosition[1]);
		float[] b = subVec3(new float[3], vPosition[0], vPosition[2]);
		// cross
		return crossVec3(new float[3], a,b);
	}

	static int[] colorIntToHSL(int colorInt)
	{
		int[] outHSL = new int[3];
		outHSL[0] = colorInt >> 10 & 0x3F;
		outHSL[1] = colorInt >> 7 & 0x7;
		outHSL[2] = colorInt & 0x7F;
		return outHSL;
	}

	static int colorHSLToInt(int[] colorHSL)
	{
		return (colorHSL[0] << 3 | colorHSL[1]) << 7 | colorHSL[2];
	}

	static int[] colorIntToRGB(int colorInt)
	{
		int[] outHSL = new int[3];
		outHSL[0] = colorInt >> 10 & 0x3F;
		outHSL[1] = colorInt >> 7 & 0x7;
		outHSL[2] = colorInt & 0x7F;
		return colorHSLToRGB(outHSL[0], outHSL[1], outHSL[2]);
	}

	public static int colorRGBToInt(float[] colorRGB) {
		int[] colorRGBInt = new int[3];
		for (int i = 0; i < colorRGB.length; i++) {
			colorRGBInt[i] = (int)(colorRGB[i] * 255);
		}
		return (colorRGBInt[0] << 8 | colorRGBInt[1]) << 8 | colorRGBInt[2] | 134217728;
	}

	static int[] colorHSLToRGB(float h, float s, float l)
	{
		h /= 64f;
		s /= 8f;
		l /= 128f;

		float q = 0;

		if (l < 0.5)
			q = l * (1 + s);
		else
			q = (l + s) - (s * l);

		float p = 2 * l - q;

		float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
		float g = Math.max(0, HueToRGB(p, q, h));
		float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

		r = Math.min(r, 1.0f);
		g = Math.min(g, 1.0f);
		b = Math.min(b, 1.0f);

		return new int[]{(int)(r * 255f), (int)(g * 255f), (int)(b * 255f)};
	}

	static float HueToRGB(float p, float q, float h)
	{
		if (h < 0) h += 1;

		if (h > 1 ) h -= 1;

		if (6 * h < 1)
		{
			return p + ((q - p) * 6 * h);
		}

		if (2 * h < 1 )
		{
			return  q;
		}

		if (3 * h < 2)
		{
			return p + ( (q - p) * 6 * ((2.0f / 3.0f) - h) );
		}

		return p;
	}

	public static float linearToGamma(float c)
	{
		float gamma = 2.2f;
		return (float)Math.pow(c, 1.0f / gamma);
	}

	public static float gammaToLinear(float c)
	{
		float gamma = 2.2f;
		return (float)Math.pow(c, gamma);
	}
}
