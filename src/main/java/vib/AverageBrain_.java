/*-
 * #%L
 * VIB plugin for Fiji.
 * %%
 * Copyright (C) 2009 - 2024 Fiji developers.
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

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import vib.app.module.AverageBrain;

public class AverageBrain_ implements PlugInFilter {
	ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Transform Parameters");
		gd.addStringField("files", "");
		gd.addStringField("matrices", "");
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		String[] fileNames = gd.getNextString().split(",");
		FastMatrix[] matrices = FastMatrix.parseMatrices(
				gd.getNextString());
		new AverageBrain().doit(image, fileNames, matrices);
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G | DOES_8C;
	}
}

