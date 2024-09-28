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
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.StringTokenizer;

import math3d.Point3d;

public class ElasticTransform_ implements PlugInFilter {
	ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Transform Parameters");
		AmiraParameters.addAmiraMeshList(gd, "imageToTransform");
		gd.addStringField("origPoints", "");
		gd.addStringField("transPoints", "");
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		ImagePlus trans = WindowManager.getImage(gd.getNextChoice());
		Point3d[] origPoints = parsePoints(gd.getNextString());
		Point3d[] transPoints = parsePoints(gd.getNextString());

		ElasticTransformedImage t =
			new ElasticTransformedImage(new InterpolatedImage(image), new InterpolatedImage(trans), origPoints, transPoints);
		t.getTransformed().image.show();
	}

	Point3d[] parsePoints(String s) {
		ArrayList array = new ArrayList();
		StringTokenizer t = new StringTokenizer(s);
		while(t.hasMoreTokens())
			array.add(new Point3d(Double.parseDouble(t.nextToken()),
					Double.parseDouble(t.nextToken()),
					Double.parseDouble(t.nextToken())));
		Point3d[] res = new Point3d[array.size()];
		for (int i = 0; i < res.length; i++)
			res[i] = (Point3d)array.get(i);
		return res;
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G | DOES_8C;
	}
}

