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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class MaxEntQuantization_ implements PlugInFilter {
	ImagePlus image;
	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G;
	}
	@Override
	public void run(ImageProcessor ip) {
		ImageStack stack = image.getStack();

		GenericDialog gd = new GenericDialog("Maximum Entropy Parameters");
		gd.addNumericField("numberOfBins", 16, 0);
		gd.addCheckbox("showIndex", false);
		gd.addCheckbox("useNaive", false);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		int numberOfBins = (int)gd.getNextNumber();
		boolean showIndex = gd.getNextBoolean();
		boolean useNaive = gd.getNextBoolean();

		MaxEntHistogram histogram=new MaxEntHistogram(image);
		// if numberOfBins=-7, show histogram of entropies over bins
		if(numberOfBins==-7) {
			calculateEntropyHistogram(histogram);
			return;
		}

		if(useNaive)
			histogram.quantizeNaive(numberOfBins);
		else
			histogram.quantize(numberOfBins);
		byte[] mapping=histogram.getMapping(showIndex);

		ImageStack res = new ImageStack(stack.getWidth(), stack.getHeight());

		for (int s = 1; s <= stack.getSize(); s++) {
			res.addSlice("", doit(stack.getProcessor(s), mapping));
			IJ.showProgress(s/(double)stack.getSize());
		}
		ImagePlus result = new ImagePlus("Maximum entropy quantized "+image.getTitle()+" "+numberOfBins+" bins", res);
		result.setCalibration(image.getCalibration());
		result.show();
	}

	private ByteProcessor doit(ImageProcessor ip, byte[] mapping) {
		byte[] pixels=(byte[])ip.getPixels();
		int w=ip.getWidth(),h=ip.getHeight();
		byte[] result=new byte[w*h];
		for(int i=0;i<w*h;i++) {
			int value=pixels[i];
			if(value<0)
				value+=256;
			result[i]=mapping[value];
		}
		return new ByteProcessor(w, h, result, null);
	}

	private void calculateEntropyHistogram(MaxEntHistogram histogram) {
		double[] entropyHistogram = new double[255];
		for(int i=1;i<256;i++) {
			entropyHistogram[i-1]=histogram.quantize(i);
			System.err.println("Entropy for "+i+": "+entropyHistogram[i-1]);
			IJ.showProgress(i,255);
		}
		new ShowHistogram(entropyHistogram,1,1);
	}
}


