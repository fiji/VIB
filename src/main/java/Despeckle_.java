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
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Despeckle_ implements PlugInFilter{

	public static String ACCURATE = "Accurate";
	public static String FAST = "Fast";
	
	private ImagePlus image;
	private byte[] currentSlide;

	private int w;
	private int h;
	
	private int k_diameter;
	private String mode = ACCURATE;
	
	private int FG = 0;
	private int BG = 255;
	
	@Override
	public void run(ImageProcessor ip){
		w = image.getWidth();
		h = image.getHeight();
		GenericDialog gd = new GenericDialog("Despeckle");
		gd.addNumericField("Radius of largest particles to remove", 5, 0);
		gd.addNumericField("Foreground color", 0, 0);
		gd.addNumericField("Background color", 255, 0);
		gd.addChoice("Mode",new String[]{ACCURATE, FAST},FAST);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		k_diameter = (int)gd.getNextNumber() + 2;
		FG = (int)gd.getNextNumber();
		BG = (int)gd.getNextNumber();

		if(FG < 0 || FG > 255 || BG < 0 || BG > 255)
			IJ.showMessage("Wrong color range");

		despeckle();
		image.updateAndDraw();
	}

	public void despeckle(){
		for(int i=0; i<image.getStackSize(); i++){
			currentSlide = (byte[])image.getStack().
										getProcessor(i+1).getPixels();
			despeckleSlide();
		}
	}

	private void despeckleSlide(){
		for(int y=-1; y<h-k_diameter+1; y++){
			for(int x=-1; x<w-k_diameter+1; x++){
				handlePosition(x,y);
			}
		}
	}

	private void handlePosition(int x, int y){
		if(isIsland(x,y))
			removeIsland(x,y);
	}

	private boolean isIsland(int x, int y){
		// check first and last row
		for(int i=0; i<k_diameter; i++)
			if(get(x+i,y) == (byte)FG || get(x+i,y+k_diameter) == (byte)FG)
				return false;

		// check first and last column
		for(int i=1; i<k_diameter-1; i++)
			if(get(x,y+i) == (byte)FG || get(x+k_diameter,y+i) == (byte)FG)
				return false;
		return true;
	}

	private void removeIsland(int x, int y){
		for(int i=1; i<k_diameter-1; i++){
			int yPos = y+i;
			for(int j=1; j<k_diameter-1; j++){
				int xPos = x+j;
				currentSlide[yPos*w+xPos] = (byte)BG;
			}
		}
	}

	private byte get(int x, int y){
		return (x < w && x >= 0 && y >= 0 && y < h)
			? currentSlide[y*w + x] : (byte)BG;
	}

	@Override
	public int setup(String arg, ImagePlus img){
		this.image = img;
		return DOES_8G;
	}
}
