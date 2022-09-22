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
package leica;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

import java.io.File;
import java.io.IOException;

/**
 * Opens multi-image 8-bits tiff files created by Leica confocal microscope
 * systems using each channels own LUT.  Modified by Nico Stuurman June 2000
 * Modified to set the real dimensions by J. Schindelin 2006
 * Modified to read the new Leica tiff format by B. Schmid 2007
 */
public class Leica_SP_Reader extends LeicaSPReader implements PlugIn {

	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.18h"))
			return;
		boolean showIt = (arg == null || arg.trim().equals("")) &&
						 !Interpreter.isBatchMode();
		String dir = "";
		String file = "";
		if(arg==null || arg.equals("")) {
			OpenDialog od = new OpenDialog("Leica Tiff", null);
			dir = od.getDirectory();
			file = od.getFileName();
		} else {
			File f = new File(arg.trim());
			dir = f.getParent() + File.separator;
			file = f.getName();
		}
		if(arg==null)
			return;

		try {
			FileInfo[] fi =  getFileInfo(dir, file);
			nr_channels = fi.length / nr_frames;
			images = new ImagePlus[nr_channels];
			for(int channel = 0; channel < nr_channels; channel++) {
				ImageStack stack = openStack(fi, channel);
				if (stack != null){
					int l = channel + 1;
					fi[0].fileName = arg;
					fi[0].directory = dir;
					Calibration cal = new Calibration();
					cal.pixelWidth = fi[0].pixelWidth;
					cal.pixelHeight = fi[0].pixelHeight;
					cal.pixelDepth = fi[0].pixelDepth;
					if(channel == 0) {
						this.setStack(file + "(channel1)", stack);
						this.setCalibration(cal);
						this.setFileInfo(fi[0]);
					}
					images[channel] = new ImagePlus(
							file + " (channel" + l + ")", stack);
					images[channel].setCalibration(cal);
					images[channel].setFileInfo(fi[0]);
					images[channel].setProperty("Info",
							imageInfo);
					if(showIt)
						images[channel].show();
				}
			}
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null || msg.equals(""))
				msg = ""+e;
			IJ.showMessage("Leica SP Reader", msg);
		}
	}
}
