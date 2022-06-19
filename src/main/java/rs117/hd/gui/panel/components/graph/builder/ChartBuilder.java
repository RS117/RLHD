/*
 * Copyright (c) 2021 Mark_ <https://github.com/Mark7625/>
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
package rs117.hd.gui.panel.components.graph.builder;

import lombok.Data;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChartBuilder
{

	private int maxAge;
	private String title;
	private String bottomAxisLabel;
	private String rightAxisLabel;

	private NumberFormat numberFormat;
	private List<DataSet> dataset = new ArrayList<>();

	public ChartBuilder()
	{
	}

	public ChartBuilder(int maxAge, String bottomAxisLabel, String rightAxisLabel, String title, List<DataSet> dataset, NumberFormat numberFormat)
	{
		this.maxAge = maxAge;
		this.bottomAxisLabel = bottomAxisLabel;
		this.rightAxisLabel = rightAxisLabel;
		this.title = title;
		this.dataset = dataset;
		this.numberFormat = numberFormat;

	}

	public ChartBuilder setMaxAge(int maxAge)
	{
		this.maxAge = maxAge;
		return this;
	}

	public ChartBuilder setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public ChartBuilder setBottomAxisLabel(String bottomAxisLabel)
	{
		this.bottomAxisLabel = bottomAxisLabel;
		return this;
	}

	public ChartBuilder setRightAxisLabel(String rightAxisLabel)
	{
		this.rightAxisLabel = rightAxisLabel;
		return this;
	}

	public ChartBuilder setNumberFormat(NumberFormat format)
	{
		this.numberFormat = format;
		return this;
	}


	public ChartBuilder addDataset(DataSet dataset)
	{
		this.dataset.add(dataset);
		return this;
	}

	public ChartBuilder build()
	{
		return new ChartBuilder(maxAge, bottomAxisLabel, rightAxisLabel, title, dataset, numberFormat);
	}

}