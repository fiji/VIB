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
/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

package process3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Laplace_ implements PlugInFilter {

	private ImagePlus image;
	float tolerance = 5.0f;

	@Override
	public int setup(String arg, ImagePlus img) {
		this.image = img;
		return DOES_8G | DOES_16;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus laplace = calculateLaplace_(image);
		ImagePlus rebinned = Rebin_.rebin(laplace, 256);
		rebinned.show();
	}


	public static ImagePlus calculateLaplace_(ImagePlus imp) {
		
		IJ.showStatus("Calculating laplace");

		float[] H_x = new float[] {1.0f, -2.0f, 1.0f};
		ImagePlus g_x = Convolve3d.convolveX(imp, H_x);

		float[] H_y = new float[] {1.0f, -2.0f, 1.0f};
		ImagePlus g_y = Convolve3d.convolveY(imp, H_y);

		float[] H_z = new float[] {1.0f, -2.0f, 1.0f};
		ImagePlus g_z = Convolve3d.convolveZ(imp, H_z);

		int w = imp.getWidth(), h = imp.getHeight();
		int d = imp.getStackSize();
		ImageStack grad = new ImageStack(w, h);
		for(int z = 0; z < d; z++) {
			FloatProcessor res = new FloatProcessor(w, h);
			grad.addSlice("", res);
			float[] values = (float[])res.getPixels();
			float[] x_ = (float[])g_x.getStack().
						getProcessor(z+1).getPixels();
			float[] y_ = (float[])g_y.getStack().
						getProcessor(z+1).getPixels();
			float[] z_ = (float[])g_z.getStack().
						getProcessor(z+1).getPixels();
			for(int i = 0; i < w*h; i++) {
				values[i] = (float)Math.sqrt(
						x_[i]*x_[i] + 
						y_[i]*y_[i] + 
						z_[i]*z_[i]);
			}
		}
		return new ImagePlus("Laplacian", grad);
	}
}
