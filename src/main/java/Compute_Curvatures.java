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
