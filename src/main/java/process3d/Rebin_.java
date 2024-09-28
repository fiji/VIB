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
package process3d;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Plugin which takes an ImagePlus and rebins the pixel values 
 * to the specified range.
 */
public class Rebin_ extends Rebin implements PlugInFilter {
	private static final String MIN_KEY = "VIB.Rebin.min";
	private static final String MAX_KEY = "VIB.Rebin.max";
	private static final String N_BINS_KEY = "VIB.Rebin.nBins";
	private static final double DEFAULT_MIN = 0.0;
	private static final double DEFAULT_MAX = 255.0;
	private static final int DEFAULT_N_BINS = 256;

	private ImagePlus image;
	private GenericDialog settingsDialog;
	private double min;
	private double max;
	private int nBins;

	@Override
	public void run(ImageProcessor ip) {
		loadSettings();
		createSettingsDialog();

		settingsDialog.showDialog();
		if(settingsDialog.wasCanceled()) {
			return;
		}

		readSettings();
		saveSettings();

		rebin(image, (float) min, (float) max, nBins).show();
	}

	@Override
	public int setup(String arg, ImagePlus img) {
		this.image = img;
		return DOES_32;
	}

	private void saveSettings() {
		Prefs.set(MIN_KEY, min);
		Prefs.set(MAX_KEY, max);
		Prefs.set(N_BINS_KEY, nBins);
	}

	private void readSettings() {
		min = settingsDialog.getNextNumber();
		max = settingsDialog.getNextNumber();
		nBins = (int) settingsDialog.getNextNumber();
	}

	private void loadSettings() {
		min = Prefs.get(MIN_KEY, DEFAULT_MIN);
		max = Prefs.get(MAX_KEY, DEFAULT_MAX);

		// Can't get Prefs.getInt to work
		String bins = Prefs.get(N_BINS_KEY, String.valueOf(DEFAULT_N_BINS));
		nBins = Integer.parseInt(bins);
	}

	private void createSettingsDialog() {
		settingsDialog = new GenericDialog("Rebin_");
		settingsDialog.addNumericField("min", min, 3);
		settingsDialog.addNumericField("max", max, 3);
		settingsDialog.addNumericField("nbins", nBins, 0);
		settingsDialog.addHelp("http://imagej.net/3D_Binary_Filters");
	}

}
