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
package rs117.hd.utils.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GpuBuffer
{
	public static final int SCALAR_SIZE = 4;
	private ByteBuffer buffer;

	public GpuBuffer() {
		this(65536);
	}

	public GpuBuffer(int initialCapacity) {
		buffer = allocateDirect(initialCapacity * SCALAR_SIZE);
	}

	public GpuBuffer put(int i) {
		buffer.putInt(i);
		return this;
	}

	public GpuBuffer put(int x, int y) {
		buffer.putInt(x).putInt(y);
		return this;
	}

	public GpuBuffer put(int x, int y, int z)
	{
		buffer.putInt(x).putInt(y).putInt(z);
		return this;
	}

	public GpuBuffer put(int x, int y, int z, int c)
	{
		buffer.putInt(x).putInt(y).putInt(z).putInt(c);
		return this;
	}

	public GpuBuffer putVertex(int x, int y, int z, int c, float n_x, float n_y, float n_z, int terrainData)
	{
		buffer
			.putInt(x).putInt(y).putInt(z).putInt(c)
			.putFloat(n_x).putFloat(n_y).putFloat(n_z).putInt(terrainData);
		return this;
	}

	public GpuBuffer put(int[] ints) {
		buffer.asIntBuffer().put(ints);
		advance(ints.length);
		return this;
	}

	public GpuBuffer put(float texture, float u, float v, float pad)
	{
		buffer.putFloat(texture).putFloat(u).putFloat(v).putFloat(pad);
		return this;
	}

	public GpuBuffer put(float[] floats) {
		buffer.asFloatBuffer().put(floats);
		advance(floats.length);
		return this;
	}

	public GpuBuffer advance(int numScalars) {
		buffer.position(buffer.position() + numScalars * SCALAR_SIZE);
		return this;
	}

	public GpuBuffer flip()
	{
		buffer.flip();
		return this;
	}

	public GpuBuffer clear()
	{
		buffer.clear();
		return this;
	}

	public GpuBuffer rewind()
	{
		buffer.rewind();
		return this;
	}

	public GpuBuffer alignVec4() {
		int pos = buffer.position() / SCALAR_SIZE;
		return advance(4 - pos % 4);
	}

	public GpuBuffer ensureCapacity(int size)
	{
		int capacity = buffer.capacity() / SCALAR_SIZE;
		final int position = buffer.position() / SCALAR_SIZE;
		if ((capacity - position) < size)
		{
			do
			{
				capacity *= 2;
			}
			while ((capacity - position) < size);

			ByteBuffer newB = allocateDirect(capacity);
			buffer.flip();
			newB.put(buffer);
			buffer = newB;
		}

		return this;
	}

	public ByteBuffer getBuffer()
	{
		return buffer;
	}

	public static ByteBuffer allocateDirect(int size)
	{
		return ByteBuffer.allocateDirect(size * SCALAR_SIZE)
			.order(ByteOrder.nativeOrder());
	}
}
