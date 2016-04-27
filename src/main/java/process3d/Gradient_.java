package process3d;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Gradient_ extends Gradient implements PlugInFilter {

	private static final String CALIBRATION_KEY = "VIB.Gradient.calibration";
	private static final boolean DEFAULT_CALIBRATION = true;
	private ImagePlus image;

	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		return DOES_8G | DOES_16;
	}

	@Override
	public void run(ImageProcessor ip) {
		boolean useCalibration = Prefs.get(CALIBRATION_KEY, DEFAULT_CALIBRATION);

		GenericDialog gd = new GenericDialog("Gradient_");
		gd.addCheckbox("Use calibration", useCalibration);

		gd.showDialog();
		if(gd.wasCanceled()) {
			return;
		}

		useCalibration = gd.getNextBoolean();
		Prefs.set(CALIBRATION_KEY, useCalibration);

		ImagePlus grad = calculateGrad(image, useCalibration);
		Rebin.rebin(grad, 256).show();
	}
}
