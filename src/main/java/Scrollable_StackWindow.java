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
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Scrollable_StackWindow implements PlugIn {
	@Override
	public void run(String arg) {
		ImagePlus image = IJ.getImage();
		image.setWindow(new Window(image, image.getCanvas()));
	}

	static class Window extends StackWindow implements MouseWheelListener {
		public Window(ImagePlus image, ImageCanvas canvas) {
			super(image, canvas);
			addMouseWheelListener(this);
		}

		/* For some funny reason, we get each event twice */
		boolean skip;

		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			synchronized(this) {
				skip = !skip;
				if (skip)
					return;

				int slice = imp.getCurrentSlice()
					+ event.getWheelRotation();
				if (slice < 1)
					slice = 1;
				else if (slice > imp.getStack().getSize())
					slice = imp.getStack().getSize();
				imp.setSlice(slice);
			}
		}
	}
}

