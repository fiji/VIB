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
import amira.AmiraParameters;
import amira.AmiraTable;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.text.TextPanel;
import ij.util.Tools;

import java.util.ArrayList;
import java.util.List;

public class Show_Label_Centers implements PlugInFilter {

	private ImagePlus image;
	
	public Show_Label_Centers(){}

	@Override
	public int setup(String arg, ImagePlus img) {
		this.image = img;
		return DOES_8G | DOES_8C;
	}

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Show Label Centers");
		AmiraParameters.addAmiraTableList(gd, "Statistics file");
		gd.addNumericField("Radius", 10, 2);
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		String title = gd.getNextChoice();
		AmiraTable table = (AmiraTable)WindowManager.getFrame(title);
		double radius = gd.getNextNumber();
		calculateCenters(image, table, radius).show();
	}
		
	public static ImagePlus calculateCenters(
				ImagePlus image, AmiraTable table, double r) {
		double r_sq = r*r;
		Point3D[] centers = getList(table);
		ImageStack stack = image.getStack();
		Calibration cal = image.getCalibration();
		double pw = cal.pixelWidth;
		double ph = cal.pixelHeight;
		double pd = cal.pixelDepth;
		int w = image.getWidth(), h = image.getHeight();
		int d = image.getStackSize();
		ImageStack newStack = new ImageStack(w, h);

		for(int z = 0; z < d; z++) {
			byte[] b = new byte[w*h];
			for(int y = 0; y < h; y++) {
				for(int x = 0; x < w; x++) {
					int idx = y*w + x;
					for(int i=0; i<centers.length;i++) {
						if(check(centers[i],new Point3D(
							x*pw,y*ph,z*pd), r_sq))

							b[idx] = (byte)255;
					}
				}
			}
			newStack.addSlice("",new ByteProcessor(w, h, b, null));
		}
		ImagePlus ret = new ImagePlus("Centers", newStack);
		ret.setCalibration(cal);
		return ret;
	}



	private static boolean check(Point3D fst, Point3D snd, double r_sq) {
		return ((snd.x-fst.x)*(snd.x-fst.x) + 
			(snd.y-fst.y)*(snd.y-fst.y) + 
			(snd.z-fst.z)*(snd.z-fst.z)) < r_sq;
	}

	public static Point3D[] getList(AmiraTable table) {
		TextPanel panel = table.getTextPanel();
		int count = panel.getLineCount();
		List points = new ArrayList();
		// start with 1, since 0 is 'Exterior'
		for (int i = 1; i < count; i++) {
			String[] line = Tools.split(panel.getLine(i), "\t");
			int voxelCount = Integer.parseInt(line[2]);
			// only take materials into account which have more
			// than 0 voxels (which are labelled)
			if(voxelCount == 0)
				continue;
			points.add(new Point3D(Double.parseDouble(line[4]),
					Double.parseDouble(line[5]),
					Double.parseDouble(line[6])));
		}
		return (Point3D[])points.toArray(new Point3D[]{});
	}
	
	private static class Point3D {
		private double x, y, z;
		public Point3D(double x, double y, double z) {
			this.x = x; this.y = y; this.z = z;
		}
	}
}
