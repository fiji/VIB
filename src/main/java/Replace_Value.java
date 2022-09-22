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
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Replace_Value implements PlugInFilter {
	
	private ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Replace value");
		gd.addNumericField("Pattern: [0..255] ", 0, 0);
		gd.addNumericField("Replacement: [0.255] ", 0, 0);
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		doit((int)gd.getNextNumber(), (int)gd.getNextNumber());
	}

	public void doit(int PATTERN, int REPLACEMENT) {
		int w = image.getWidth(), h = image.getHeight();
		int d = image.getStackSize();
		ImageStack stack = image.getStack();
		for(int z = 0; z < d; z++) {
			byte[] b = (byte[])stack.getProcessor(z+1).getPixels();
			for(int i = 0; i < w*h; i++) {
				if(((int)(b[i] & 0xff)) == PATTERN)
					b[i] = (byte)REPLACEMENT;
			}
		}
	}

	@Override
	public int setup(String args, ImagePlus imp) {
		this.image = imp;
		return DOES_8G | DOES_8C;
	}
}
