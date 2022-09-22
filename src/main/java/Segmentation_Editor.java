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
import amira.AmiraParameters;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import vib.segment.CustomStackWindow;
import vib.segment.MaterialList;

public class Segmentation_Editor extends vib.segment.SegmentationEditor {

	// Stuff for macro calls
	private static CustomStackWindow cached = null;

	private static CustomStackWindow getInstance() {
		if(cached != null)
			return cached;
		// check if there's an existing SegmentationEditor
		int[] ids = WindowManager.getIDList();
		for(int id : ids) {
			ImageWindow iw = WindowManager.getImage(id).getWindow();
			if(iw instanceof CustomStackWindow)
				return (CustomStackWindow)iw;
		}
		newSegmentationEditor();
		return cached;
	}

	public static final void newSegmentationEditor() {
		final ImagePlus image = WindowManager.getCurrentImage();
		if (image==null) {
			IJ.error("No image?");
			return;
		}

		cached = new CustomStackWindow(image);
		cached.getLabels().show();
		cached.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("Ok")) {
					image.show();
				}
			}
		});
	}

	public static final void setLabels(ImagePlus labels) {
		getInstance().setLabels(labels);
	}

	public static final void newMaterials() {
		ImagePlus labels = getInstance().getLabels();
		String materials = "Parameters {\n"
				+ "\tMaterials {\n"
				+ "\t\tExterior {\n"
				+ "\t\t\tColor 0.0 0.0 0.0\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}\n";
		AmiraParameters params = new AmiraParameters(materials);
		params.setParameters(labels);
		setLabels(labels);
	}

	public static final void addMaterial(String material, int r, int g, int b) {
		if(hasMaterial(material))
			throw new IllegalArgumentException(material + " already exists");
		MaterialList ml = getInstance().getSidebar().getMaterials();
		ml.addMaterial();
		ml.select(ml.getItemCount() - 1);
		ml.renameMaterial(material);
		ml.setColor(r, g, b);
	}

	public static final boolean hasMaterial(String name) {
		return getInstance().getSidebar().getMaterials().
				getIndexOf(name) != -1;
	}

}
