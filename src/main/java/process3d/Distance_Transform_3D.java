package process3d;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

// TODO take calibration into account

public class Distance_Transform_3D extends DistanceTransform3D
		implements PlugInFilter {

	public void run(ImageProcessor ip) {
		getTransformed(image, 255).show();
	}

	public int setup(String arg, ImagePlus imp) {
		this.image = imp;
		return DOES_8G;
	}
}
