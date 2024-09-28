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
		gd.addHelp("http://imagej.net/3D_Binary_Filters");

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
