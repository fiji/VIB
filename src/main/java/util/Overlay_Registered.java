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

package util;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.util.ArrayList;

public class Overlay_Registered extends OverlayRegistered implements PlugIn {
	@Override
	public void run(String ignored) {

		String macroOptions = Macro.getOptions();

		String mustHaveSubstring = "";

		String titleSubstring = null;
		boolean closeAllOthers = false;

		if (macroOptions != null) {
			String value = Macro.getValue(macroOptions, "substring", null);
			if( value != null ) {
				mustHaveSubstring = value;
			}
			value = Macro.getValue(macroOptions, "keep", null);
			if( value != null ) {
				System.out.println("Got keep!: '"+value+"'");
			}
			value = Macro.getValue(macroOptions, "close", null);
			if( value != null ) {
				System.out.println("Got close!: '"+value+"'");
			}
		}

		int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.error("No images are open.");
			return;
		}
		if (wList.length < 2 ) {
			IJ.error("Must have at least two images open.");
			return;
		}

		ArrayList<String> matchingTitles = new ArrayList<String>();
		ArrayList<ImagePlus> matchingImages = new ArrayList<ImagePlus>();

		ImagePlus [] matchingImagePlus=new ImagePlus[wList.length];
		ImagePlus [] allImages=new ImagePlus[wList.length];

		int totalMatchingTitles = 0;
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			String title = imp != null ? imp.getTitle() : "";
			if ( title.indexOf(mustHaveSubstring) >= 0 ) {
				System.out.println("Yes, matched: "+title);
				matchingTitles.add(title);
				matchingImages.add(imp);
			} else
				System.out.println("No, didn't match '"+"' in: "+title);
		}

		if( matchingTitles.size() < 2 ) {
			IJ.error("Fewer than two images matched the substring '"+mustHaveSubstring+"'");
			return;
		}

		GenericDialog gd = new GenericDialog("Overlay Transformed");
		gd.addChoice("A:", (String[])matchingTitles.toArray(new String[1]), matchingTitles.get(0));
		gd.addChoice("B:", (String[])matchingTitles.toArray(new String[1]), matchingTitles.get(1));
		gd.addCheckbox("Keep source images", true);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}

		int[] index = new int[2];
		index[0] = gd.getNextChoiceIndex();
		index[1] = gd.getNextChoiceIndex();

		ImagePlus [] sourceImages = new ImagePlus[2];

		sourceImages[0] = matchingImages.get(index[0]);
		sourceImages[1] = matchingImages.get(index[1]);

		ImagePlus rgbResult = overlayToImagePlus( sourceImages[0], sourceImages[1] );
		rgbResult.show();

		ModelessQuestions q=new ModelessQuestions("Rate This Registration",rgbResult);

		q.addTextField("I dunno", 20, "Your message:" );

		q.addTextField("Well", 10, "Something");

		q.addLabel("Just a label.");

		q.addCompletingButton("ok","Done");
		q.addCompletingButton("unhappy","Rubbish!");

		for( int i = 0; i <= 10; ++i ) {
			q.addRadio("rating",""+i);
		}

		q.waitForAnswers();

		IJ.error("Finished waiting for answers!");

		if(closeAllOthers) {
			for( int i=0; i < allImages.length; ++i ) {
				allImages[i].close();
			}
		}
	}
}
