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
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import vib.BilateralFilter;

/*

 This plugin implements the Bilateral Filter, described in

  C. Tomasi and R. Manduchi, "Bilateral Filtering for Gray and Color Images",
  Proceedings of the 1998 IEEE International Conference on Computer Vision,
  Bombay, India.

 Basically, it does a Gaussian blur taking into account the intensity domain
 in addition to the spatial domain (i.e. pixels are smoothed when they are
 close together _both_ spatially and by intensity.

*/
public class Bilateral_Filter implements PlugInFilter {
	ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Bilateral Parameters");
		gd.addNumericField("spatial radius", 3, 0);
		gd.addNumericField("range radius", 50, 0);
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		double spatialRadius = gd.getNextNumber();
		double rangeRadius = gd.getNextNumber();
		BilateralFilter.filter(
			image, spatialRadius, rangeRadius).show();
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G;
	}
}

