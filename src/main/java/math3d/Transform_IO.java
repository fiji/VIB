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
package math3d;

import ij.IJ;
import ij.plugin.PlugIn;

import java.util.Arrays;

public class Transform_IO extends TransformIO implements PlugIn {
	@Override
	public void run(String arg) {
		// Only for testing
		float[] mat;
		if(arg.equals("")) mat=openAffineTransform();
		else mat=openAffineTransform(arg);
		IJ.log("fields:="+getFields());
		IJ.log("tags:="+getTags());
		IJ.log("mat = "+Arrays.toString(mat));
		//saveAffineTransform(mat);
	}
}
