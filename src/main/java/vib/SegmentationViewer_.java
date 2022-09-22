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

import amira.AmiraParameters;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;

import java.util.Vector;

/** This plugin is an example showing how to add a non-destructive 
  graphic overlay to an image or stack. */
public class SegmentationViewer_ implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();

		if(AmiraParameters.isAmiraLabelfield(imp)) {
			IJ.error("The current image is a labelfield!");
			return;
		}

		// find labelfields
		Vector labelfields=new Vector();
		ImagePlus labels;
		for(int i=1; (labels=WindowManager.getImage(i))!=null; i++) {
			// TODO: check dimensions
			if(AmiraParameters.isAmiraLabelfield(labels))
				labelfields.add(labels);
		}
		// TODO: show dialog to create a labelfield
		if(labelfields.size()<1) {
			IJ.error("No labelfields loaded");
			return;
		}

		if(labelfields.size()>1) {
			String[] list=new String[labelfields.size()];
			for(int i=0;i<list.length;i++)
				list[i]=((ImagePlus)labelfields.get(i)).getTitle();

			GenericDialog gd=new GenericDialog("Parameters");
			gd.addChoice("Labelfield",list,list[0]);
			gd.showDialog();
			if(gd.wasCanceled())
				return;

			labels=(ImagePlus)labelfields.get(gd.getNextChoiceIndex());
		} else
			labels=(ImagePlus)labelfields.get(0);

		SegmentationViewerCanvas cc = new SegmentationViewerCanvas(imp, labels);
		if (imp.getStackSize()>1)
			new StackWindow(imp, cc);
		else
			new ImageWindow(imp, cc);
	}
}

