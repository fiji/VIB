/*-
 * #%L
 * VIB plugin for Fiji.
 * %%
 * Copyright (C) 2009 - 2022 Fiji developers.
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
/***************************************************************
 *
 * DiffusionInterpol2
 *
 * ported from Amira module
 *
 ***************************************************************/

package vib;

import amira.AmiraParameters;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class DiffusionInterpol2_ extends DiffusionInterpol2
		implements PlugInFilter {
	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G | DOES_8C;
	}

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("DiffusionInterpol2");
		if (!AmiraParameters.addAmiraLabelsList(gd, "TemplateLabels"))
			return;
		if (!AmiraParameters.addAmiraMeshList(gd, "Model"))
			return;
		if (savedDisplace != null)
			gd.addCheckbox("reuseDistortion", true);
		gd.addCheckbox("rememberDistortion", false);
		gd.addStringField("LabelTransformationList",
							"1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1");
		gd.addNumericField("tolerance", 0.5, 2);

		gd.showDialog();
		if (gd.wasCanceled())
			return;
		
		template = new InterpolatedImage(image);
		templateLabels = new InterpolatedImage(
				WindowManager.getImage(gd.getNextChoice()));
		model = new InterpolatedImage(
			WindowManager.getImage(gd.getNextChoice()));
		reuse = gd.getNextBoolean();
		remember = gd.getNextBoolean();
		labelTransformations =FloatMatrix.parseMatrices(gd.getNextString());
		tolerance = (float)gd.getNextNumber();
		doit();
	}
}
