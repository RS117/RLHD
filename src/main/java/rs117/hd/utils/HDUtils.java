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
package rs117.hd.utils;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class HDUtils
{

	// directional vectors approximately opposite of the directional light used by the client
	private static final float[] inverseLightDirectionZ0 = new float[]{
		0.70710678f, 0.70710678f, 0f
	};
	private static final float[] inverseLightDirectionZ1 = new float[]{
		0.57735026f, 0.57735026f, 0.57735026f
	};

	// The epsilon for floating point values used by jogl
	private static final float EPSILON = 1.1920929E-7f;

	public static float[] vectorAdd(float[] vec1, float[] vec2)
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

	public static int vertexHash(int[] vPos)
	{
		// simple custom hashing function for vertex position data
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < vPos.length; i++)
		{
			s.append(vPos[i]).append(",");
		}
		return s.toString().hashCode();
	}

	public static float[] calculateSurfaceNormals(int[] vertexX, int[] vertexY, int[] vertexZ)
	{
		// calculate normals
		float[] a = new float[3];
		a[0] = vertexX[0] - vertexX[1];
		a[1] = vertexY[0] - vertexY[1];
		a[2] = vertexZ[0] - vertexZ[1];

		float[] b = new float[3];
		b[0] = vertexX[0] - vertexX[2];
		b[1] = vertexY[0] - vertexY[2];
		b[2] = vertexZ[0] - vertexZ[2];

		// cross
		float[] n = new float[3];
		n[0] = a[1] * b[2] - a[2] * b[1];
		n[1] = a[2] * b[0] - a[0] * b[2];
		n[2] = a[0] * b[1] - a[1] * b[0];
		return n;
	}

	public static int[] colorIntToHSL(int colorInt)
	{
		int[] outHSL = new int[3];
		outHSL[0] = colorInt >> 10 & 0x3F;
		outHSL[1] = colorInt >> 7 & 0x7;
		outHSL[2] = colorInt & 0x7F;
		return outHSL;
	}

	public static int colorHSLToInt(int[] colorHSL)
	{
		return (colorHSL[0] << 3 | colorHSL[1]) << 7 | colorHSL[2];
	}

	public static int[] colorIntToRGB(int colorInt)
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

	// Conversion functions to and from sRGB and linear color space.
	// The implementation is based on the sRGB EOTF given in the Khronos Data Format Specification.
	// Source: https://web.archive.org/web/20220808015852/https://registry.khronos.org/DataFormat/specs/1.3/dataformat.1.3.pdf
	// Page number 130 (146 in the PDF)
	public static float linearToSrgb(float c)
	{
		return c <= 0.0031308 ?
			c * 12.92f :
			(float) (1.055 * Math.pow(c, 1 / 2.4) - 0.055);
	}

	public static float srgbToLinear(float c)
	{
		return c <= 0.04045f ?
			c / 12.92f :
			(float) Math.pow((c + 0.055) / 1.055, 2.4);
	}

	public static float dotNormal3Lights(float[] normals)
	{
		return dotNormal3Lights(normals, true);
	}

	public static float dotNormal3Lights(float[] normals, final boolean includeZ)
	{
		final float lengthSq = normals[0] * normals[0] + normals[1] * normals[1] + normals[2] * normals[2];
		if (abs(lengthSq) < EPSILON)
		{
			return 0f;
		}
		else if (includeZ)
		{
			return (normals[0] * inverseLightDirectionZ1[0] + normals[1] * inverseLightDirectionZ1[1] + normals[2] * inverseLightDirectionZ1[2]) / (float) sqrt(lengthSq);
		}
		else
		{
			return (normals[0] * inverseLightDirectionZ0[0] + normals[1] * inverseLightDirectionZ0[1]) / (float) sqrt(lengthSq);
		}
	}
}
