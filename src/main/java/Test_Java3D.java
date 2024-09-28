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
import org.scijava.java3d.utils.geometry.ColorCube;
import org.scijava.java3d.utils.universe.SimpleUniverse;

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Panel;

import org.scijava.java3d.Alpha;
import org.scijava.java3d.BoundingSphere;
import org.scijava.java3d.BranchGroup;
import org.scijava.java3d.Canvas3D;
import org.scijava.java3d.RotationInterpolator;
import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TransformGroup;


public class Test_Java3D implements PlugIn {

	@Override
	public void run(String args) {
		GenericDialog gd = new GenericDialog("3D Test");
		Panel p = createPanel();
		gd.addPanel(p);
		gd.showDialog();
	}

	public Panel createPanel() {
		Panel p = new Panel();
		p.setPreferredSize(new Dimension(512, 512));
		p.setLayout(new BorderLayout());
		Canvas3D canvas3D = new Canvas3D(
			SimpleUniverse.getPreferredConfiguration());
		p.add("Center", canvas3D);

		BranchGroup scene = createSceneGraph();
		scene.compile();

		SimpleUniverse simpleU = new SimpleUniverse(canvas3D);
		simpleU.getViewingPlatform().setNominalViewingTransform();

		simpleU.addBranchGraph(scene);
		return p;
	} // end of HelloJava3Dd (constructor)

	public BranchGroup createSceneGraph() {
		BranchGroup objRoot = new BranchGroup();

		// rotate object has composited transformation matrix
		Transform3D rotate = new Transform3D();
		Transform3D tempRotate = new Transform3D();

		rotate.rotX(Math.PI/4.0d);
		tempRotate.rotY(Math.PI/5.0d);
		rotate.mul(tempRotate);

		TransformGroup objRotate = new TransformGroup(rotate);

		// Create the transform group node and initialize it to the
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.  Add it to the
		// root of the subgraph.
		TransformGroup objSpin = new TransformGroup();
		objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		objRoot.addChild(objRotate);
		objRotate.addChild(objSpin);

		// Create a simple shape leaf node, add it to the scene graph.
		// ColorCube is a Convenience Utility class
		objSpin.addChild(new ColorCube(0.4));

		// Create a new Behavior object that will perform the desired
		// operation on the specified transform object and add it into
		// the scene graph.
		Transform3D yAxis = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, 4000);

		RotationInterpolator rotator = new RotationInterpolator(
					rotationAlpha, objSpin, yAxis,
					0.0f, (float) Math.PI*2.0f);

		// a bounding sphere specifies a region a behavior is active
		// create a sphere centered at the origin with radius of 1
		BoundingSphere bounds = new BoundingSphere();
		rotator.setSchedulingBounds(bounds);
		objSpin.addChild(rotator);

		return objRoot;
	} // end of CreateSceneGraph method of HelloJava3Dd

} // end of class HelloJava3Dd
