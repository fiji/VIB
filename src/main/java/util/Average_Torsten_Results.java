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
import ij.plugin.PlugIn;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vib.Average_Images;

/* An example plugin that averages the images found in the directories
   created by the registration algorithm of Torsten et al, which can be
   found at http://flybrain.stanford.edu/
 */

public class Average_Torsten_Results implements PlugIn {

	public static ImagePlus averageFromDirectoryList( ArrayList<File> entries ) {

		ClassLoader loader = IJ.getClassLoader();
		if (loader == null) {
			IJ.error("IJ.getClassLoader() failed (!)");
			return null;
		}

		Average_Images averager = new Average_Images();

		for( Iterator<File> i = entries.iterator();
		     i.hasNext(); ) {

			File rawImageFile = new File( i.next(), "image.bin.gz" );

			/* Annoyingly this loader is in the fiji
			   plugins, so not available when we compile
			   VIB.  Find it with reflection:
			 */

			ImagePlus imagePlus = null;

			try {

				Class<?> c = loader.loadClass("io.TorstenRaw_GZ_Reader");
				Object newInstance = c.newInstance();

				Class [] parameterTypes = { String.class };
				Object [] parameters = new Object[1];
				parameters[0] = rawImageFile.getAbsolutePath();
				Method m = c.getMethod( "run", parameterTypes );
				m.invoke(newInstance,parameters);

				imagePlus = (ImagePlus) newInstance;

			} catch (IllegalArgumentException e) {
				IJ.error("There was an illegal argument when trying to invoke a method on the TorstenRaw GZ Reader plugin: " + e);
				return null;
			} catch (InvocationTargetException e) {
				Throwable realException = e.getTargetException();
				IJ.error("There was an exception thrown by the TorstenRaw GZ Reader plugin: " + realException);
				return null;
			} catch (ClassNotFoundException e) {
				IJ.error("The TorstenRaw GZ Reader plugin was not found: " + e);
				return null;
			} catch (InstantiationException e) {
				IJ.error("Failed to instantiate the TorstenRaw GZ Reader plugin: " + e);
				return null;
			} catch ( IllegalAccessException e ) {
				IJ.error("IllegalAccessException when trying the TorstenRaw GZ Reader plugin: "+e);
				return null;
			} catch (NoSuchMethodException e) {
				IJ.error("Couldn't find a method in the TorstenRaw GZ Reader plugin: " + e);
				return null;
			} catch (SecurityException e) {
				IJ.error("There was a SecurityException when trying to invoke a method of the TorstenRaw GZ Reader plugin: " + e);
			}

			averager.add(imagePlus);
			imagePlus.close();
		}

		return averager.getAverageImage(false);
	}

	@Override
	public void run( String arg ) {

		File resultsRoot = new File("/Volumes/LaCie/corpus/central-complex/biorad/reformatted");
		Pattern includeDirectoryPattern = Pattern.compile("01_warp");
		Pattern excludeDirectoryPattern = Pattern.compile("\\.study$");

		ArrayList<File> directoriesToAverageFrom = new ArrayList<File>();

		File [] entries = resultsRoot.listFiles();

		for(int i=0;i<entries.length;++i) {

			File entry = entries[i];
			Matcher includeM = includeDirectoryPattern.matcher(entry.getName());
			if( ! includeM.find() ) {
				continue;
			}
			Matcher excludeM = excludeDirectoryPattern.matcher(entry.getName());
			if( excludeM.find() ) {
				continue;
			}
			System.out.println("Using: "+entry);

			directoriesToAverageFrom.add(entry);
		}

		ImagePlus result = averageFromDirectoryList( directoriesToAverageFrom );
		if( result != null )
			result.show();
	}
}
