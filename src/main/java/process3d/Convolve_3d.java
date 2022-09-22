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
/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

package process3d;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Convolve_3d extends Convolve3d implements PlugInFilter {
	protected ImagePlus image;

	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		return DOES_8G | DOES_16 | DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		GenericDialogPlus gd = new GenericDialogPlus("Convolve (3D)");
		gd.addImageChoice("kernel:", null);
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		ImagePlus kernelImage = gd.getNextImage();
		if (kernelImage.getType() != ImagePlus.GRAY32) {
			IJ.error("Need a 32-bit image!");
			return;
		}

		int w = kernelImage.getWidth();
		int h = kernelImage.getHeight();
		int d = kernelImage.getStackSize();
		float[][][] kernel = new float[w][h][d];
		for (int k = 0; k < d; k++) {
			float[] pixels = (float[])kernelImage.getStack().getProcessor(k + 1).getPixels();
			for (int j = 0; j < h; j++)
				for (int i = 0; i < w; i++)
					kernel[i][j][k] = pixels[i + w * j];
		}

		convolve(image, kernel).show();
	}
}
