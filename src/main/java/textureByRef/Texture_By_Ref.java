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
package textureByRef;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Toolbar;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij3d.Image3DUniverse;

import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TexCoordGeneration;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Texture2D;
import org.jogamp.java3d.TextureAttributes;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.pickfast.PickCanvas;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector4f;

public class Texture_By_Ref implements PlugInFilter,
						ImageListener,
						MouseMotionListener,
						MouseListener {

	private static final int TEX_MODE = Texture.INTENSITY;
	private static final int COMP_TYPE = ImageComponent.FORMAT_CHANNEL8;
	private static final boolean BY_REF = true;
	private static final boolean Y_UP = true;

	private Image3DUniverse univ;

	private ByteProcessor bProcessor;
	private ImageComponent2D bComp;
	private ImageComponent2D.Updater updater;
	private ImagePlus imp;
	private int w = 256;
	private int h = 256;
	
	public static void main(String[] args) {
		new ij.ImageJ();
		ImagePlus img = IJ.openImage("/home/bene/PhD/brains/template.tif");
		img = new ImagePlus("Slice 20", img.getStack().getProcessor(20));
		img.show();
		ij.IJ.runPlugIn("textureByRef.Texture_By_Ref", "");
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		if(imp.getStackSize() > 1 ||
				!isPow2(imp.getWidth()) ||
				!isPow2(imp.getHeight())) {
			IJ.error("Only one slice allowed, whose dimensions must" + 
					" be a power of 2");
			return;
		}
		createImage();
		univ = new Image3DUniverse();
		BranchGroup bg = new BranchGroup();
		bg.addChild(createShape());
		bg.compile();
		univ.getScene().addChild(bg);
		univ.show();

		univ.getCanvas().addMouseListener(this);
		univ.getCanvas().addMouseMotionListener(this);

		updater = new ImageUpdater();
		ImagePlus.addImageListener(this);
		imp.show();
	}

	private static final int r = 5;
	private boolean doDraw = false;

	@Override
	public void mouseDragged(MouseEvent e) {
		if(!doDraw)
			return;
		OvalRoi roi = new OvalRoi(e.getX() - r, e.getY() - r, 2 * r, 2 * r);
		Polygon p = roi.getPolygon();
		int n = p.npoints;
		Polygon q = new Polygon(new int[n], new int[n], n);
		
		for(int i = 0; i < n; i++) {
			Point3d picked = getPickPoint(p.xpoints[i], p.ypoints[i]);
			if(picked == null)
				continue;
			q.xpoints[i] = (int)Math.round(picked.x);
			q.ypoints[i] = (int)Math.round(picked.y);
		}
		bProcessor.fillPolygon(q);
		imp.updateAndDraw();
	}

	private final Point3d getPickPoint(int x, int y) {
		PickCanvas pickCanvas = new PickCanvas(univ.getCanvas(), univ.getScene());
		pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
		pickCanvas.setFlags(PickInfo.CLOSEST_INTERSECTION_POINT);
		pickCanvas.setTolerance(3.0f);
		pickCanvas.setShapeLocation(x, y);
		try {
			PickInfo[] result = pickCanvas.pickAllSorted();
			if(result == null || result.length == 0)
				return null;

			for(int i = 0; i < result.length; i++)
				return result[i].getClosestIntersectionPoint();
	
			return null;
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// MouseListener interfaces
	
	@Override
	public void mousePressed(MouseEvent e) {
		int id = Toolbar.getToolId();
		doDraw = id == Toolbar.SPARE1 || id == Toolbar.SPARE2 ||
			id == Toolbar.SPARE3 || id == Toolbar.SPARE4 ||
			id == Toolbar.SPARE5 || id == Toolbar.SPARE6 ||
			id == Toolbar.SPARE7 || id == Toolbar.SPARE8 ||
			id == Toolbar.SPARE9;
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {}
	
	// ImageListener interfaces

	@Override
	public void imageOpened(ImagePlus image) {}

	@Override
	public void imageClosed(ImagePlus image) {}

	@Override
	public void imageUpdated(ImagePlus image) {
		if(image == imp)
			bComp.updateData(updater, 0, 0, w, h);
	}

	public Appearance createAppearance() {
		Appearance appearance = new Appearance();

		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.COMBINE);
		texAttr.setCombineRgbMode(TextureAttributes.COMBINE_MODULATE);
		texAttr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
		appearance.setTextureAttributes(texAttr);

		TransparencyAttributes transAttr = new TransparencyAttributes();
		transAttr.setTransparency(0.1f);
		transAttr.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		transAttr.setTransparencyMode(TransparencyAttributes.BLENDED);
		appearance.setTransparencyAttributes(transAttr);

		PolygonAttributes polyAttr = new PolygonAttributes();
		polyAttr.setCullFace(PolygonAttributes.CULL_NONE);
		appearance.setPolygonAttributes(polyAttr);

		Material material = new Material();
		material.setLightingEnable(false);
		appearance.setMaterial(material);

		ColoringAttributes colAttr = new ColoringAttributes();
		colAttr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		colAttr.setShadeModel(ColoringAttributes.NICEST);
		colAttr.setColor(1f, 1f, 1f);
		appearance.setColoringAttributes(colAttr);

			  // Avoid rendering of voxels having an alpha value of zero
		RenderingAttributes rendAttr = new RenderingAttributes();
		rendAttr.setCapability(
			RenderingAttributes.ALLOW_ALPHA_TEST_VALUE_WRITE);
 		rendAttr.setAlphaTestValue(0.1f);
		rendAttr.setAlphaTestFunction(RenderingAttributes.GREATER);
		appearance.setRenderingAttributes(rendAttr);

		appearance.setTexture(getTexture());
		appearance.setTexCoordGeneration(getTg());

		return appearance;
	}

	public Shape3D createShape() {
		Shape3D shape = new Shape3D(
			createGeometry(),
			createAppearance());
		return shape;
	}
	
	public BufferedImage createImage() {
		bProcessor = (ByteProcessor)imp.getProcessor();
		byte[] pixels = (byte[])bProcessor.getPixels();
		
		IndexColorModel cm = getDefaultColorModel();
		SampleModel sm = cm.createCompatibleSampleModel(w, h);
		
		DataBufferByte db = new DataBufferByte(pixels, w * h, 0);
		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
     
		return new BufferedImage(cm, raster, false, null);
	}

	private static IndexColorModel getDefaultColorModel() {
		byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		for(int i = 0; i < 256; i++) {
			r[i] = (byte)i;
		}
		return new IndexColorModel(8, 256, r, g, b);
	}

//	public void createImage2() {
//		bImage = new BufferedImage(w, h, B_IMG_TYPE);
//		byte[] pixels = ((DataBufferByte)bImage.getRaster().getDataBuffer()).getData();
//		bProcessor = new ByteProcessor(w, h, pixels, null);
//		imp = new ImagePlus("Please draw", bProcessor);
//	}

	public Texture getTexture() {

		Texture2D tex = new Texture2D(Texture.BASE_LEVEL, TEX_MODE, w, h);
		bComp = new ImageComponent2D(COMP_TYPE, w, h, BY_REF, Y_UP);
		bComp.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
		bComp.set(createImage());

		tex.setImage(0, bComp);
		tex.setEnable(true);
		tex.setMinFilter(Texture.BASE_LEVEL_LINEAR);
		tex.setMagFilter(Texture.BASE_LEVEL_LINEAR);

		tex.setBoundaryModeS(Texture.CLAMP);
		tex.setBoundaryModeT(Texture.CLAMP);
		return tex;
	}

	public TexCoordGeneration getTg() {
		float xTexGenScale = (float)(1.0 / w);
		float yTexGenScale = (float)(1.0 / h);
		TexCoordGeneration tg = new TexCoordGeneration();
		tg.setPlaneS(new Vector4f(xTexGenScale, 0f, 0f, 0f));
		tg.setPlaneT(new Vector4f(0f, yTexGenScale, 0f, 0f));
		return tg;
	}

	public GeometryArray createGeometry() {
		QuadArray quadArray = new QuadArray(4, 
					GeometryArray.COORDINATES |
					GeometryArray.COLOR_3);
		Point3f[] coords = new Point3f[4];
		coords[0] = new Point3f(0, 0, 0);
		coords[1] = new Point3f(w, 0, 0);
		coords[2] = new Point3f(w, h, 0);
		coords[3] = new Point3f(0, h, 0);

		Color3f[] colors = new Color3f[4];
		colors[0] = new Color3f(100, 100, 100);
		colors[1] = new Color3f(100, 100, 100);
		colors[2] = new Color3f(100, 100, 100);
		colors[3] = new Color3f(100, 100, 100);

		quadArray.setCoordinates(0, coords);
		quadArray.setColors(0, colors);
		return quadArray;
	}

	private static final int nextPow2(int n) {
		int retval = 2;
		while (retval < n) {
			retval = retval << 1;
		}
		return retval;
	}
	
	private static final boolean isPow2(int n) {
		int next = nextPow2(n);
		return n == next;
	}

	private class ImageUpdater implements ImageComponent2D.Updater {
		@Override
		public void updateData(ImageComponent2D comp, int x, int y, int w, int h) {
		}
	}

//	private class UpdateBehavior extends Behavior {
//		
//		private final WakeupOnElapsedFrames wakeup;
//		
//		public UpdateBehavior() {
//			wakeup = new WakeupOnElapsedFrames(1, true);
//		}
//		
//		public void initialize() {
//			wakeupOn(wakeup);
//		}
//		
//		public synchronized void processStimulus(Enumeration criteria) {
//			System.out.println("processStim");
//			while(criteria.hasMoreElements()) {
//			}
//			wakeupOn(wakeup);
//		}
//	}
}
