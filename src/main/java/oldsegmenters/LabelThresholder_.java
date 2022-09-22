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
package oldsegmenters;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

/**
 * User: Tom Larkworthy
 * Date: 21-Jun-2006
 * Time: 13:52:04
 */
public class LabelThresholder_ implements PlugInFilter{
    static Mem mem;

    public static int min = 5;
    public static int max = 100;


    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        SegmentatorModel model = new SegmentatorModel(IJ.getImage());


        int currentSlice = model.data.getCurrentSlice();
        ImageProcessor imageData = model.data.getStack().getProcessor(currentSlice);
        ImageProcessor labelData = model.getLabelImagePlus().getStack().getProcessor(currentSlice);

        if(model.getCurrentMaterial() == null){
            IJ.showMessage("please select a label first");
            return;
        }
        if(model.data.getRoi()==null) {
            IJ.error("You need to have a region of interest selected in order to threshold an area.");
            return;
        }
        threshold(model.data.getRoi(), min, max, imageData, labelData, model.getCurrentMaterial().id);

        model.updateSlice(currentSlice);
    }

    /**
     * reverts to label  image before last threshold was applied
     */
    public static void rollback(){
        SegmentatorModel model = new SegmentatorModel(IJ.getImage());
        int currentSlice = model.data.getCurrentSlice();
        if(mem!=null) mem.restore();
        model.updateSlice(currentSlice);
    }

	public static void commit(){
		if(mem != null)
			mem.clear();
	}


    public static void threshold(Roi roi, int min, int max, ImageProcessor imageData, ImageProcessor labelData, int label) {
        mem = new Mem(labelData);

        Rectangle bounds = roi.getBounds();
        for (int x = bounds.x; x <= bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y <= bounds.y + bounds.height; y++) {
                if (roi.contains(x,y) && imageData.get(x, y) >= min && imageData.get(x, y) <= max) {
                    mem.rememberPixel(x,y);
                    labelData.set(x, y, label);
                }
            }
        }
    }

    static class Mem {
        ImageProcessor data;
        HashMap<Point, Integer> memory = new HashMap<Point, Integer>();

        public Mem(ImageProcessor data) {
            this.data = data;
        }

        public void rememberPixel(int x, int y) {
            memory.put(new Point(x, y), data.get(x, y));
        }

        public void restore() {
            for (Point p : memory.keySet()) {
                data.set(p.x, p.y, memory.get(p));
            }
        }

		public void clear() {
			memory.clear();
		}
	}
}
