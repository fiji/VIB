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
package process3d;

import java.awt.image.ColorModel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * This class implements the erosion filter.
 * The kernel size is fixed with a diameter of 3 pixels. This 
 * makes sense the operation gets rapidly computationally more
 * expensive with increasing diameter. Computational complexity 
 * is related to the third power of the diameter, so 2-fold 
 * diameter means 8-fold computation time.
 * For complexity reasons, the implementation uses a 6-neighbour-
 * hood and not a 27-neighborhood.
 */
public class Erode_ implements PlugInFilter {
	private static final int DEFAULT_ISO_VALUE = 255;
	private static final String ISO_VALUE_KEY = "VIB.Erode.isoValue";
	private int w, h, d;
	private ImagePlus image;
	private byte[][] pixels_in;
	private byte[][] pixels_out;

	@Override
	public void run(ImageProcessor ip) {
		// Can't get Prefs.getInt to work
		String iso = Prefs.get(ISO_VALUE_KEY, String.valueOf(DEFAULT_ISO_VALUE));
		int isoValue = Integer.parseInt(iso);

		GenericDialog gd = new GenericDialog("Erode");
		gd.addNumericField("Iso value", isoValue, 0);
		gd.addHelp("http://imagej.net/3D_Binary_Filters");
		
		gd.showDialog();
		if(gd.wasCanceled()) {
			return;
		}

		isoValue = (int) gd.getNextNumber();
		Prefs.set(ISO_VALUE_KEY, isoValue);
		
		erode(image, isoValue, false).show();
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.image = imp;
		return DOES_8G | DOES_8C | NO_CHANGES;
	}

	public ImagePlus erode(ImagePlus image, int threshold, boolean newWin) {

		// Determine dimensions of the image
		w = image.getWidth(); h = image.getHeight();
		d = image.getStackSize();

		pixels_in = new byte[d][];
		pixels_out = new byte[d][];
		for(int z = 0; z < d; z++) {
			pixels_in[z] = (byte[])image.getStack().getPixels(z+1);
			pixels_out[z] = new byte[w*h];
		}
		
		// iterate
		for(int z = 0; z < d; z++) {
			IJ.showProgress(z, d-1);
			for(int y = 0; y < h; y++) {
				for(int x = 0; x < w; x++) {
					if(get(x, y, z) != threshold) 
						set(x, y, z, get(x, y, z));
					else if(get(x-1, y, z) == threshold &&
						get(x+1, y, z) == threshold &&
						get(x, y-1, z) == threshold &&
						get(x, y+1, z) == threshold &&
						get(x, y, z-1) == threshold &&
						get(x, y, z+1) == threshold)

						set(x, y, z, threshold);
					else
						set(x, y, z, 0);

				}
			}
		}

		ColorModel cm = image.getStack().getColorModel();
		
		// create output image
		ImageStack stack = new ImageStack(w, h);
		for(int z = 0; z < d; z++) {
			stack.addSlice("", new ByteProcessor(
				w, h, pixels_out[z], cm));
		}
		if(!newWin) {
			image.setStack(null, stack);
			return image;
		}
		ImagePlus result = new ImagePlus(
					image.getTitle() + "_eroded", stack);
		result.setCalibration(image.getCalibration());
		return result;
	}

	public int get(int x, int y, int z) {
		x = x < 0 ? 0 : x; x = x >= w ? w-1 : x;
		y = y < 0 ? 0 : y; y = y >= h ? h-1 : y;
		z = z < 0 ? 0 : z; z = z >= d ? d-1 : z;
		return (int)(pixels_in[z][y*w + x] & 0xff);
	}

	public void set(int x, int y, int z, int v) {
		pixels_out[z][y*w + x] = (byte)v;
	}
}
