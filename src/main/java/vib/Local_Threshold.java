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
package vib;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class Local_Threshold implements PlugInFilter {

	private ImagePlus image;
	private static ImageProcessor copy;
	private static int lastMinThreshold = 10;
	private static int lastMaxThreshold = 255;

	@Override
	public void run(final ImageProcessor ip) {
		if(image.getRoi() == null) {
			IJ.error("Selection required");
			return;
		}
		Roi roiCopy = (Roi)image.getRoi().clone();
		copy = ip.duplicate();
		final GenericDialog gd = 
				new GenericDialog("Adjust local threshold");
		gd.addSlider("min value", 0, 255, lastMinThreshold);
		gd.addSlider("max value", 0, 255, lastMaxThreshold);

		final Scrollbar minSlider = (Scrollbar)gd.getSliders().get(0);
		final Scrollbar maxSlider = (Scrollbar)gd.getSliders().get(1);

		AdjustmentListener listener = new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				applyThreshold(ip, image.getRoi(), 
						minSlider.getValue(),
						maxSlider.getValue());
				lastMinThreshold = minSlider.getValue();
				lastMaxThreshold = maxSlider.getValue();
				image.updateAndDraw();
			}
		};
		minSlider.addAdjustmentListener(listener);
		maxSlider.addAdjustmentListener(listener);

		applyThreshold(ip, image.getRoi(), 
				lastMinThreshold, lastMaxThreshold);
		image.updateAndDraw();
		gd.showDialog();

		// Convert area to selection
		ip.setRoi(image.getRoi());
		ImageProcessor newip = ip.crop();
		newip.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
		ImagePlus tmp = new ImagePlus("", newip);
		ThresholdToSelection ts = new ThresholdToSelection();
		ts.setup("", tmp);
		ts.run(newip);
		newip.resetThreshold();
		ip.insert(copy, 0, 0);
		Rectangle roiCopyR = roiCopy.getBounds();
		if(tmp.getRoi() != null) {
			Rectangle roiTempR = tmp.getRoi().getBounds();
			int xl = roiCopyR.x > 0 ? roiCopyR.x : 0;
			if(roiTempR.x > 0) xl += roiTempR.x;
			int yl = roiCopyR.y > 0 ? roiCopyR.y : 0;
			if(roiTempR.y > 0) yl += roiTempR.y;
			tmp.getRoi().setLocation(xl, yl);
			image.setRoi(tmp.getRoi());
		}
	}

	public static void applyThreshold(ImageProcessor ip, 
						Roi roi, int min, int max) {
		if(roi == null) {
			IJ.error("Selection required");
			return;
		}
		boolean mustCleanUp = copy == null;
		if(copy == null) {
			 copy = ip.duplicate();
		}

		byte[] p = (byte[])ip.getPixels();
		byte[] c = (byte[])copy.getPixels();

		int w = ip.getWidth(), h = ip.getHeight();

		Rectangle bounds = roi.getBounds();
		int x1 = bounds.x > 0 ? bounds.x : 0;
		int y1 = bounds.y > 0 ? bounds.y : 0;
		int x2 = x1 + bounds.width <= w ? x1 + bounds.width : w;
		int y2 = y1 + bounds.height <= h ? y1 + bounds.height : h;


		for(int y = y1; y < y2; y++) {
			for(int x = x1; x < x2; x++) {
				if(!roi.contains(x, y))
					continue;
				int index = y*ip.getWidth() + x;
				if(((int)c[index]&0xff) >= min &&
						((int)c[index]&0xff) <= max) {
					p[index] = (byte)255;
				} else {
					p[index] = c[index];
				}
			}
		}
		if(mustCleanUp) copy = null;
	}

	@Override
	public int setup(String args, ImagePlus imp) {
		this.image = imp;
		return DOES_8G;
	}
}
