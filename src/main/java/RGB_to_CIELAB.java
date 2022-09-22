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
import color.CIELAB;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class RGB_to_CIELAB implements PlugInFilter {
	protected ImagePlus image;
	protected int w, h;

	@Override
	public void run(ImageProcessor ip) {
		w = image.getWidth();
		h = image.getHeight();
		float[] rgb = new float[3];
		float[] lab = new float[3];

		if (image.getStack().getSize() == 1) {
			int[] pixels = (int[])image.getProcessor().getPixels();
			float[] l = new float[w * h];
			float[] a = new float[w * h];
			float[] b = new float[w * h];

			for (int i = 0; i < w * h; i++) {
				int v = pixels[i];
				CIELAB.int2sRGB(v, rgb);
				CIELAB.sRGB2CIELAB(rgb, lab);
				l[i] = lab[0];
				a[i] = lab[1];
				b[i] = lab[2];
			}
			ImageStack stack = new ImageStack(w, h);
			stack.addSlice("L", new FloatProcessor(w, h, l, null));
			stack.addSlice("a", new FloatProcessor(w, h, a, null));
			stack.addSlice("b", new FloatProcessor(w, h, b, null));
			new ImagePlus(image.getTitle() + " Lab", stack).show();
		} else {
			int[] rgbi = new int[3];
			ImageStack stack = image.getStack();
			float[] l = (float[])stack.getProcessor(1).getPixels();
			float[] a = (float[])stack.getProcessor(2).getPixels();
			float[] b = (float[])stack.getProcessor(3).getPixels();
			int[] pixels = new int[w * h];

			for (int i = 0; i < w * h; i++) {
				lab[0] = l[i];
				lab[1] = a[i];
				lab[2] = b[i];
				CIELAB.CIELAB2sRGB(lab, rgb);
				pixels[i] = CIELAB.sRGB2int(rgb);
			}
			ip = new ColorProcessor(w, h, pixels);
			new ImagePlus(image.getTitle() + " RGB", ip).show();
		}
	}
	@Override
	public int setup(String args, ImagePlus imp) {
		this.image = imp;
		return DOES_RGB | DOES_32 | NO_CHANGES;
	}
}
