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
/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

import amira.AmiraParameters;
import amira.AmiraTableEncoder;
import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;

public class AmiraTableWriter_ implements PlugIn {

	@Override
	public void run(String arg) {
		GenericDialog gd = new GenericDialog("Choose Window");
		if (!AmiraParameters.addAmiraTableList(gd, "window"))
			// addAmiraTableList reports errors
			return;
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		String title = gd.getNextChoice();
		Object frame = WindowManager.getFrame(title);
		if (frame == null) {
			IJ.error("[BUG] No window from WindowManager.getFrame()");
			return;
		}

		SaveDialog od = new SaveDialog("AmiraFile", null, ".am");
		String dir=od.getDirectory();
		String name=od.getFileName();
		if(name==null) {
			IJ.error("No name was chosen: not saved");
			return;
		}

		if (!(frame instanceof TextWindow)) {
			IJ.error("[BUG] frame wasn't an instance of TextWindow");
			return;
		}

		TextWindow t = (TextWindow)frame;
		AmiraTableEncoder e = new AmiraTableEncoder(t);
		if (!e.write(dir + name))
			IJ.error("Could not write to " + dir + name);
		return;
	}
}
