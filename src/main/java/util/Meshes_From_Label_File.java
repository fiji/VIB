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
/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

/*

   Call this plugin with something like:
 
      -eval "run('Meshes From Label File','source=[/home/mark/arnim-brain/CantonF41c.labels] output=[/home/mark/tmp/meshes]');"
  
 */

package util;

import amira.AmiraParameters;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;

import java.io.File;

import marchingcubes.ExportMesh_;

public class Meshes_From_Label_File implements PlugIn {

	@Override
	public void run(String ignore) {
	
		String macroOptions = Macro.getOptions();

		String sourceFilename = null;
		String outputDirectory = null;

		if (macroOptions == null) {
			IJ.error("Currently this can only be called as macro with options.");
			return;
		}

		sourceFilename = Macro.getValue(macroOptions, "source", null);
		if (sourceFilename == null) {
			IJ.error("No source filename specified. (Macro option 'source'.)");
			return;
		}

		outputDirectory = Macro.getValue(macroOptions, "output", null);
		if (outputDirectory == null) {
			IJ.error("No output directory specified. (Macro option 'output'.)");
			return;
		}

		File outputDirectoryAsFile = new File(outputDirectory);
		if (!(outputDirectoryAsFile.exists() && outputDirectoryAsFile.isDirectory())) {
			IJ.error("The output (" + outputDirectory + ") must both exist and be a directory. ");
			return;
		}

		ImagePlus imagePlus = BatchOpener.openFirstChannel(sourceFilename);
		if( imagePlus == null ) {
			IJ.error( "File not found: "+sourceFilename );
			return;
		}

		int type = imagePlus.getType();
		
		if( ! (type == ImagePlus.GRAY8 || type == ImagePlus.COLOR_256) ) {
			IJ.error("Something's wrong: '"+sourceFilename+"' doesn't seem to be an 8 bit file.");
			return;
		}
			
		String [] materialList = null;
		AmiraParameters parameters = null;

		if (AmiraParameters.isAmiraLabelfield(imagePlus)) {
			parameters = new AmiraParameters(imagePlus);
			materialList = parameters.getMaterialList();
		} else {
			IJ.error("The file '"+sourceFilename+"' isn't an Amira labelfield!");
			return;
		}		

		int width = imagePlus.getWidth();
		int height = imagePlus.getHeight();
		int depth = imagePlus.getStackSize();
		
		for( int m = 1; m < materialList.length; ++m ) {
			
			double [] c = parameters.getMaterialColor(m);
			String materialName = materialList[m];

			ImageStack newStack = new ImageStack(width,height);
			
			ImageStack stack = imagePlus.getStack();
			
			for( int z = 0; z < depth; ++z ) {
				byte [] newPixels=new byte[width*height];
				byte [] pixels = (byte[]) stack.getPixels(z+1);
				for( int p = 0; p < width * height; ++p ) {
					int value = pixels[p] & 0xFF;
					if( value == m )
						newPixels[p] = (byte)255;
				}
				ByteProcessor bp=new ByteProcessor(width,height);
				bp.setPixels(newPixels);
				newStack.addSlice("",bp);
			}

			ImagePlus newImagePlus=new ImagePlus("",newStack);

			ExportMesh_ exporter=new ExportMesh_();
			boolean channels[] = { true, true, true };
			
			String outputFilename = outputDirectory + File.separator +
				c[0] + "_" + c[1] + "_" + c[2] + "_" + materialName + ".obj";
			exporter.exportToMesh( newImagePlus, 128, 2, channels, outputFilename );

			newImagePlus.close();
		}			
	}
}
