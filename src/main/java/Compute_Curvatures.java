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
/*
 * A wrapper for Stephan Preibisch's Compute_Curvatures class so that
 * there's a top-level PlugIn as originally.
 */

import features.ComputeCurvatures;
import ij.plugin.PlugIn;

public class Compute_Curvatures implements PlugIn
{
	protected ComputeCurvatures hidden;

	public Compute_Curvatures( ) {
		hidden = new ComputeCurvatures();
	}

	@Override
	public void run(String arg) {
		hidden.runAsPlugIn(arg);
	}

}
