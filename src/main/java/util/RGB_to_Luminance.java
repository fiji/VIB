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

package util;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class RGB_to_Luminance extends RGBToLuminance implements PlugIn {
	@Override
	public void run(String ignored) {

		ImagePlus colourImage = IJ.getImage();
		if( colourImage == null ) {
			IJ.error("No current image found");
			return;
		}
		if( colourImage.getType() != ImagePlus.COLOR_RGB ) {
			IJ.error("This plugin requires an RGB image");
			return;
		}

		ImagePlus converted=convertToLuminance(colourImage);
		converted.show();

	}

}
