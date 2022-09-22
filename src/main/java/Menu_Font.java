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
import ij.ImageJ;
import ij.Menus;
import ij.gui.GenericDialog;

import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;

public class Menu_Font implements ij.plugin.PlugIn {
	@Override
	public void run(String arg) {
		GenericDialog gd = new GenericDialog("New Menu Font Size");
		gd.addNumericField("menuFontSize", 16, 0);
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		int size = (int)gd.getNextNumber();
		MenuBar menuBar = Menus.getMenuBar();
		Font font = menuBar.getFont();
		int oldSize = font.getSize();

		// this does not work, because default is "fixed"
		//menuBar.setFont(font.deriveFont(size));
		menuBar.setFont(Font.decode("sansserif-" + size));

		// work around AWT not recalculating the menu bar size
		int i, count = menuBar.getMenuCount();
		Menu[] menus = new Menu[count];
		for (i = 0; i < count; i++) {
			menus[i] = menuBar.getMenu(0);
			menuBar.remove(menus[i]);
		}
		for (i = 0; i < count; i++)
			menuBar.add(menus[i]);
		ImageJ ij = IJ.getInstance();
		ij.pack();
		ij.setSize(new java.awt.Dimension(ij.getWidth()
					* size / oldSize, ij.getHeight()));
	}
}
