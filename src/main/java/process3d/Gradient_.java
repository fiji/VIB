package process3d;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Gradient_ extends Gradient implements PlugInFilter {

	private ImagePlus image;

	public int setup(String arg, ImagePlus image) {
		this.image = image;
		return DOES_8G | DOES_16;
	}

	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Gradient_");
		gd.addCheckbox("Use calibration", true);
		if(gd.wasCanceled())
			return;
		boolean useCalibration = gd.getNextBoolean();
		ImagePlus grad = calculateGrad(image, useCalibration);
		Rebin.rebin(grad, 256).show();
	}
}
