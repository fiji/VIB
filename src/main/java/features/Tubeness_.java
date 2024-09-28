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

package features;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

public class Tubeness_ implements PlugIn {

	static final String PLUGIN_VERSION = "1.2";

	@Override
	public void run(String ignored) {

		ImagePlus original = WindowManager.getCurrentImage();
		if (original == null) {
			IJ.error("No current image to calculate tubeness of.");
			return;
		}

		Calibration calibration = original.getCalibration();

		double minimumSeparation = 1;
		if( calibration != null )
			minimumSeparation = Math.min(calibration.pixelWidth,
						     Math.min(calibration.pixelHeight,
							      calibration.pixelDepth));

		GenericDialog gd = new GenericDialog("\"Tubeness\" Filter (version "+PLUGIN_VERSION+")");
		gd.addNumericField("Sigma: ", (calibration==null) ? 1f : minimumSeparation, 4);
		gd.addMessage("(The default value for sigma is the minimum voxel separation.)");
		gd.addCheckbox("Use calibration information", calibration!=null);

		gd.showDialog();
		if( gd.wasCanceled() )
			return;

		double sigma = gd.getNextNumber();
		if( sigma <= 0 ) {
			IJ.error("The value of sigma must be positive");
			return;
		}
		boolean useCalibration = gd.getNextBoolean();

		TubenessProcessor tp = new TubenessProcessor(sigma,useCalibration);

		ImagePlus result = tp.generateImage(original);
		result.setTitle("tubeness of " + original.getTitle());

		result.show();
	}
}
