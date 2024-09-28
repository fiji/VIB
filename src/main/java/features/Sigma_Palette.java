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
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Polygon;

public class Sigma_Palette extends SigmaPalette implements PlugIn {

	@Override
	public void run( String ignoredArguments ) {

		System.out.println("In the run() method...");

		image = IJ.getImage();
		if( image == null ) {
			IJ.error("There is no current image");
			return;
		}

		Calibration calibration = image.getCalibration();
                double minimumSeparation = 1;
                if( calibration != null )
                        minimumSeparation = Math.min(calibration.pixelWidth,
                                                     Math.min(calibration.pixelHeight,
                                                              calibration.pixelDepth));

		Roi roi = image.getRoi();
		if( roi == null ) {
			IJ.error("There is no current point selection");
			return;
		}

		if( roi.getType() != Roi.POINT ) {
			IJ.error("You must have a point selection");
			return;
		}

		Polygon p = roi.getPolygon();

		if(p.npoints != 1) {
			IJ.error("You must have exactly one point selected");
			return;
		}

		ImageProcessor processor = image.getProcessor();

		int x = p.xpoints[0];
		int y = p.ypoints[0];
		int z = image.getCurrentSlice() - 1;

		int either_side = 40;

		int x_min = x - either_side;
		int x_max = x + either_side;
		int y_min = y - either_side;
		int y_max = y + either_side;
		int z_min = z - either_side;
		int z_max = z + either_side;

		int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();
		int originalDepth = image.getStackSize();

		if( x_min < 0 )
			x_min = 0;
		if( y_min < 0 )
			y_min = 0;
		if( z_min < 0 )
			z_min = 0;
		if( x_max >= originalWidth )
			x_max = originalWidth - 1;
		if( y_max >= originalHeight )
			y_max = originalHeight - 1;
		if( z_max >= originalDepth )
			z_max = originalDepth - 1;

		double [] sigmas = new double[9];
		for( int i = 0; i < sigmas.length; ++i ) {
			sigmas[i] = ((i + 1) * minimumSeparation) / 2;
		}

		makePalette( image, x_min, x_max, y_min, y_max, z_min, z_max, new TubenessProcessor(true), sigmas, 4, 3, 3, z );
	}

}
