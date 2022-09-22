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
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/*

  This plugin implements the "triangle algorithm" of Zack et al,

  Zack, G. W., Rogers, W. E. and Latt, S. A., 1977,
  Automatic Measurement of Sister Chromatid Exchange Frequency,
  Journal of Histochemistry and Cytochemistry 25 (7), pp. 741-753

*/

public class Triangle_Algorithm implements PlugInFilter {
	protected ImagePlus image;

	@Override
	public void run(ImageProcessor ip) {
		int[] histogram = getHistogram(ip);
		int split = triangleAlgorithm(histogram);
		ip.setThreshold(split, 256, ImageProcessor.RED_LUT);
		image.updateAndDraw();
	}

	@Override
	public int setup(String args, ImagePlus imp) {
		this.image = imp;
		return DOES_8G | NO_CHANGES;
	}

	int[] getHistogram(ImageProcessor ip) {
		int w = ip.getWidth(), h = ip.getHeight();
		byte[] pixels = (byte[])ip.getPixels();

		int[] result = new int[256];
		for (int i = 0; i < w * h; i++)
			result[pixels[i] & 0xff]++;

		return result;
	}

	int triangleAlgorithm(int[] histogram) {
		// find min and max
		int min = 0, max = 0;
		for (int i = 1; i < histogram.length; i++)
			if (histogram[min] > histogram[i])
				min = i;
			else if (histogram[max] < histogram[i])
				max = i;

		if (min == max)
			return min;

		// describe line by nx * x + ny * y - d = 0
		double nx, ny, d;
		nx = histogram[max] - histogram[min];
		ny = min - max;
		d = Math.sqrt(nx * nx + ny * ny);
		nx /= d;
		ny /= d;
		d = nx * min + ny * histogram[min];

		// find split point
		int split = min;
		double splitDistance = 0;
		for (int i = min + 1; i <= max; i++) {
			double newDistance = nx * i + ny * histogram[i] - d;
			if (newDistance > splitDistance) {
				split = i;
				splitDistance = newDistance;
			}
		}

		return split;
	}
}
