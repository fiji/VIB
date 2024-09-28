/*-
 * #%L
 * VIB plugin for Fiji.
 * %%
 * Copyright (C) 2009 - 2024 Fiji developers.
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
import amira.AmiraMeshEncoder;
import amira.AmiraParameters;
import amira.AmiraTableEncoder;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;

public class AmiraMeshWriter_ implements PlugIn {

	@Override
	public void run(String arg) {
		GenericDialog gd = new GenericDialog("Choose Window");
		int c = AmiraParameters.addWindowList(gd, "window", false);
		if (c == 0) {
			IJ.error("No window?");
			return;
		}
		if (c > 1) {
			gd.showDialog();
			if (gd.wasCanceled())
				return;
		}

		String title = gd.getNextChoice();
		Object frame = WindowManager.getImage(title);
		if (frame == null)
			frame = WindowManager.getFrame(title);
		else {
			int type = ((ImagePlus)frame).getType();
			if (type != ImagePlus.GRAY8 &&
					type != ImagePlus.COLOR_256) {
				IJ.error("Invalid image type");
				return;
			}
		}
		if (frame == null) {
			IJ.error("No window?");
			return;
		}

		writeImage(frame);

	}

	public static void writeImage(Object frame) {
		SaveDialog od = new SaveDialog("AmiraFile", null, ".am");
		String dir=od.getDirectory();
		String name=od.getFileName();
		if(name==null) {
			IJ.error("No name was chosen: not saved");
			return;
		}

		if (frame instanceof TextWindow) {
			TextWindow t = (TextWindow)frame;
			AmiraTableEncoder e = new AmiraTableEncoder(t);
			if (!e.write(dir + name))
				IJ.error("Could not write to " + dir + name);
			return;
		}

		AmiraMeshEncoder e=new AmiraMeshEncoder(dir+name);

		if(!e.open()) {
			IJ.error("Could not write "+dir+name);
			return;
		}

		if(!e.write((ImagePlus)frame))
			IJ.error("Error writing "+dir+name);
	}
}
