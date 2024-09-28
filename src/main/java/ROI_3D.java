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
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ROI_3D implements PlugIn {
	@Override
	public void run(String arg) {
		ImagePlus image = IJ.getImage();
		ImageCanvas canvas = image.getCanvas();
		image.setWindow(new StackWindowWith3dRoi(image, canvas));
	}

	static class StackWindowWith3dRoi extends StackWindow {
		StackWindowWith3dRoi(ImagePlus image, ImageCanvas canvas) {
			super(image, canvas);
			int i = image.getCurrentSlice();
			sliceSelector.addAdjustmentListener(new Listener(i));
		}

		class Listener implements AdjustmentListener {
			Roi[] rois;
			int oldSlice;

			Listener(int currentSlice) {
				rois = new Roi[imp.getStack().getSize() + 1];
				oldSlice = currentSlice;
			}

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				rois[oldSlice] = imp.getRoi();
				oldSlice = e.getValue();
				if (rois[oldSlice] == null)
					imp.killRoi();
				else
					imp.setRoi(rois[oldSlice]);
			}
		}
	}
}

