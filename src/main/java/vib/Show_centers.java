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

import amira.AmiraParameters;
import amira.AmiraTable;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.text.TextPanel;
import ij.util.Tools;

import java.util.ArrayList;
import java.util.List;

import math3d.Point3d;

public class Show_centers implements PlugInFilter {
	
	private ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Center Transformation");
		AmiraParameters.addAmiraTableList(gd, "Statistics");
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		String titleM = gd.getNextChoice();
		AmiraTable statistics = (AmiraTable)WindowManager.getFrame(titleM);
		
		ImagePlus centers = getCenters(statistics);
		centers.show();
		centers.updateAndDraw();
	}

	@Override
	public int setup(String arg, ImagePlus img) {
		this.image = img;
		return DOES_8G;
	}

	public void setImage(ImagePlus image) {
		this.image = image;
	}
		
	public ImagePlus getCenters(AmiraTable statistics) { 
		Point3d[] points = getList(statistics);
		ImagePlus ret = new InterpolatedImage(image).
					cloneDimensionsOnly().getImage();
		ret.setTitle("Centers");
		int w = ret.getWidth(), h = ret.getHeight();
		int d = ret.getStackSize();
		Calibration cal = ret.getCalibration();

		for(int z = 0; z < d; z++) {
			double coordz = (double)Math.abs(z * cal.pixelDepth);
			byte[] p = (byte[])ret.getStack().
						getProcessor(z+1).getPixels();
			for(int x = 0; x < w; x++) {
				for(int y = 0; y < h; y++) {
					int index = y * w + x;
					double coordx = x * cal.pixelWidth; 
					double coordy = y * cal.pixelHeight;
					Point3d p2 = new Point3d(coordx, 
								coordy, coordz);
					for(int i = 0; i < points.length; i++) {
						if(p2.distance2(points[i]) 
									< 200) {
							p[index] = (byte)255;
						}
					}
				}
			}
		}
		return ret;
	}

	public static Point3d[] getList(AmiraTable table) {
		TextPanel panel = table.getTextPanel();
		int count = panel.getLineCount();
		List<Point3d> points = new ArrayList<Point3d>();
		// start with 1, since 0 is 'Exterior'
		for (int i = 1; i < count; i++) {
			String[] line = Tools.split(panel.getLine(i), "\t");
			int voxelCount = Integer.parseInt(line[2]);
			// only take materials into account which have more
			// than 0 voxels (which are labelled)
			if(voxelCount == 0)
				continue;
			points.add(new Point3d(Double.parseDouble(line[4]),
					Double.parseDouble(line[5]),
					Double.parseDouble(line[6])));
		}
		return (Point3d[])points.toArray(new Point3d[]{});
	}
}
