package process3d;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Minimum_ implements PlugInFilter {
	
	private ImagePlus image;

	public void run(ImageProcessor ip) {
		MinMaxMedian.convolve(image, MinMaxMedian.MINIMUM).show();
	}

	public int setup(String arg, ImagePlus img) {
		this.image = img;
		return DOES_8G | DOES_16 | DOES_32 |NO_CHANGES;
	}
}
