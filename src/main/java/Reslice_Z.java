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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ImageProcessor;

public class Reslice_Z implements PlugInFilter {

	private ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		double pd = image.getCalibration().pixelDepth;
		GenericDialog gd = new GenericDialog("Reslice_Z");
		gd.addNumericField("New pixel depth", pd, 3);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		pd = gd.getNextNumber();
		reslice(image, pd).show();
	}

	public static ImagePlus reslice(ImagePlus image, double pixelDepth) {

		int w = image.getWidth();
		int h = image.getHeight();

		Calibration cal = image.getCalibration();

		ImageStack stack = image.getStack();
		int numSlices = (int)Math.round(image.getStackSize() * cal.pixelDepth /
					pixelDepth);

		// Create a new Stack
		ImageStack newStack = new ImageStack(w, h);
		for(int z = 0; z < numSlices; z++) {
			double currentPos = z * pixelDepth;

			// getSliceBefore
			int ind_p = (int)Math.floor(currentPos / cal.pixelDepth);
			int ind_n = ind_p + 1;

			double d_p = currentPos - ind_p*cal.pixelDepth;
			double d_n = ind_n*cal.pixelDepth - currentPos;

			if(ind_n >= stack.getSize())
				ind_n = stack.getSize() - 1;

			ImageProcessor before = stack.getProcessor(ind_p + 1).duplicate();
			ImageProcessor after  = stack.getProcessor(ind_n + 1).duplicate();

			before.multiply(d_n / (d_n + d_p));
			after.multiply(d_p / (d_n + d_p));

			before.copyBits(after, 0, 0, Blitter.ADD);

			newStack.addSlice("", before);
		}
		ImagePlus result = new ImagePlus("Resliced", newStack);
		cal = cal.copy();
		cal.pixelDepth = pixelDepth;
		result.setCalibration(cal);
		return result;
	}

	@Override
	public int setup(String arg, ImagePlus img) {
		this.image = img;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}
}
