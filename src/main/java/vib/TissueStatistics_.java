/*-
 * #%L
 * VIB plugin for Fiji.
 * %%
 * Copyright (C) 2009 - 2022 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package vib;

import amira.AmiraParameters;
import amira.AmiraTable;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import vib.app.module.TissueStatistics;
import vib.app.module.TissueStatistics.Statistics;

public class TissueStatistics_ implements PlugInFilter {
	ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		AmiraTable table = calculateStatistics(image);
		table.show();
	}

	public static AmiraTable calculateStatistics(ImagePlus labelfield) {
		if (!AmiraParameters.isAmiraLabelfield(labelfield)) {
			IJ.error("Need a labelfield!");
			return null;
		}
		String title = "Statistics for " + labelfield.getTitle();
		String headings = "Nr\tMaterial\tCount\tVolume\t" + 
							"CenterX\tCenterY\tCenterZ\t" + 
							"MinX\tMaxX\tMinY\tMaxY\tMinZ\tMaxZ";

		Statistics stat = TissueStatistics.getStatistics(labelfield);

		AmiraTable table = new AmiraTable(title, headings,
				stat.getResult(), true);
		return table;
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G | DOES_8C;
	}
}

