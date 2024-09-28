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

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij3d.Image3DUniverse;
import ij3d.Volume;

import java.awt.Color;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import marchingcubes.MCCube;


public class MC_Test implements PlugIn {

	private ImagePlus image;
	private byte[][] data;

	private static final Color3f GREY = new Color3f(0.753f, 0.753f, 0.753f);
	private static final Color3f RED = new Color3f(1f, 0, 0);

	@Override
	public void run(String args) {
		int defaultNo = 1;
		final Image3DUniverse univ = new Image3DUniverse(512, 512);
		displayCube(univ);
		Transform3D initialRot = new Transform3D();
		Transform3D tmp = new Transform3D();
		initialRot.rotY(Math.PI/4);
		tmp.rotX(-Math.PI/4);
		initialRot.mul(tmp);
// 		univ.getGlobalRotate().setTransform(initialRot);
		univ.show();
		displayCase(univ, defaultNo);

		GenericDialog gd = new GenericDialog("ImageJ 3D Viewer");
		gd.addSlider("case: ", 0, 255, defaultNo);
		final Scrollbar slider = (Scrollbar)gd.getSliders().get(0);
		slider.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				displayCase(univ, slider.getValue());
			}
		});
		
		gd.setModal(false);
		gd.showDialog();
		if(gd.wasCanceled())
			return;
	}

	public void displayCase(Image3DUniverse univ, int caseNo) {
		System.out.println("Case no " + caseNo);
		int threshold = 120;
		BranchGroup scene = univ.getScene();
		for(int i = scene.numChildren()-1; i >= 1 ; i--) {
			if (scene.getChild(i) instanceof BranchGroup)
				if (scene.getChild(i).getCapability(BranchGroup.ALLOW_DETACH))
					scene.removeChild(i);
		}
		System.out.println("Nr. of contents:" + univ.getContents().size());
		univ.removeAllContents();
		createCase(caseNo);
		Volume volume = new Volume(image);
		List l = MCCube.getTriangles(volume, threshold);
		univ.addTriangleMesh(l, RED, "case" + caseNo);
		for(int z = 0; z < data.length; z++) {
			for(int y = 0; y < 2; y++) {
				for(int x = 0; x < 2; x++) {
					if(((int)data[z][y*2+x]&0xff)>=threshold) {
						addVertex(univ, RED, x, y, z);
					} else {
						addVertex(univ, GREY, x, y, z);
					}
				}
			}
		}
	}

	public void addVertex(Image3DUniverse univ, Color3f color, 
						float x, float y, float z) {
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		TransformGroup tg = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(new Vector3f(x, y, z));
		tg.setTransform(t3d);
		bg.addChild(tg);
		Appearance app = new Appearance();
		ColoringAttributes cattr = new ColoringAttributes();
		cattr.setColor(color);
		app.setColoringAttributes(cattr);
		tg.addChild(new Sphere(0.1f, app));
		univ.getScene().addChild(bg);
	}

	public void createCase(int confID) {
		byte value = (byte)255;
		ImageStack stack = new ImageStack(2,2);
		stack.addSlice("", new ByteProcessor(2,2));
		stack.addSlice("", new ByteProcessor(2,2));
		
		data = new byte[2][];
		data[0] = (byte[])stack.getProcessor(1).getPixels();
		data[1] = (byte[])stack.getProcessor(2).getPixels();
		if(confID>=128) {data[1][0]=value;confID-=128;}
		if(confID>=64) {data[1][1]=value;confID-=64;}
		if(confID>=32) {data[1][3]=value;confID-=32;}
		if(confID>=16) {data[1][2]=value;confID-=16;}
		if(confID>=8) {data[0][0]=value;confID-=8;}
		if(confID>=4) {data[0][1]=value;confID-=4;}
		if(confID>=2) {data[0][3]=value;confID-=2;}
		if(confID>=1) {data[0][2]=value;confID-=1;}

		image = new ImagePlus("", stack);
	}

	public void displayCube(Image3DUniverse univ) {
		QuadArray qa = new QuadArray(24,
				QuadArray.COORDINATES);
		Point3f[] p = new Point3f[8];
		p[0] = new Point3f(+1f, 0f, 0f);
		p[1] = new Point3f(+1f, +1f, 0f);
		p[2] = new Point3f(+1f, +1f, +1f);
		p[3] = new Point3f(+1f, 0f, +1f);
		p[4] = new Point3f(0f, 0f, 0f);
		p[5] = new Point3f(0f, +1f, 0f);
		p[6] = new Point3f(0f, +1f, +1f);
		p[7] = new Point3f(0f, 0f, +1f);

		Point3f[] coords = new Point3f[24];
		coords[0] = p[0];
		coords[1] = p[0];
		coords[2] = p[0];
		coords[3] = p[0];

		coords[4] = p[1];
		coords[5] = p[5];
		coords[6] = p[6];
		coords[7] = p[2];

		coords[8] = p[5];
		coords[9] = p[4];
		coords[10] = p[7];
		coords[11] = p[6];

		coords[12] = p[4];
		coords[13] = p[0];
		coords[14] = p[3];
		coords[15] = p[7];

		coords[16] = p[4];
		coords[17] = p[5];
		coords[18] = p[1];
		coords[19] = p[0];

		coords[20] = p[3];
		coords[21] = p[2];
		coords[22] = p[6];
		coords[23] = p[7];

		qa.setCoordinates(0, coords);

		Appearance app = new Appearance();
		ColoringAttributes cattr = new ColoringAttributes();
		cattr.setColor(RED);
		app.setColoringAttributes(cattr);
		PolygonAttributes pattr = new PolygonAttributes();
		pattr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		pattr.setCullFace(PolygonAttributes.CULL_NONE);
		pattr.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(pattr);
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(new Shape3D(qa, app));
		univ.getScene().addChild(bg);
	}
}
