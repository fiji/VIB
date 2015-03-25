import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Select two images with a Line ROI in each, and rotate/translate/scale one to
 * the other.
 * <p>
 * Stacks are not explicitly supported, but a macro can easily use this plugin
 * for the purpose by iterating over all slices.
 * </p>
 *
 * @author Johannes Schindelin
 * @author Michel Teussink
 */
public class Align_Image implements PlugIn {

	private boolean isSupported(final int type) {
		switch (type) {
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32:
			case ImagePlus.COLOR_RGB:
				return true;
		}
		return false;
	}

	@Override
	public void run(final String arg) {

		// Find all images that have a LineRoi in them
		final int[] ids = WindowManager.getIDList();
		if (null == ids) return; // no images open
		final ArrayList<ImagePlus> validImages = new ArrayList<ImagePlus>();
		for (int i = 0; i < ids.length; i++) {
			final ImagePlus imp = WindowManager.getImage(ids[i]);
			final Roi roi = imp.getRoi();
			if (roi instanceof Line && isSupported(imp.getType())) {
				validImages.add(imp);
			}
		}
		if (validImages.size() < 2) {
			IJ.showMessage("Need 2 images with a line roi in each.\n"
				+ "Images must be 8, 16 or 32-bit.");
			return;
		}

		// create choice arrays
		final String[] titles = new String[validImages.size()];
		int k = 0;
		for (final Iterator<ImagePlus> it = validImages.iterator(); it.hasNext();)
			titles[k++] = it.next().getTitle();

		final GenericDialog gd = new GenericDialog("Align Images");
		final String current = WindowManager.getCurrentImage().getTitle();
		gd.addChoice("source", titles, current.equals(titles[0]) ? titles[1]
			: titles[0]);
		gd.addChoice("target", titles, current);
		gd.addCheckbox("scale", true);
		gd.addCheckbox("rotate", true);
		gd.showDialog();
		if (gd.wasCanceled()) return;

		final ImagePlus source =
			WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
		final Line line1 = (Line) source.getRoi();

		final ImagePlus target =
			WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
		final Line line2 = (Line) target.getRoi();
		final boolean withScaling = gd.getNextBoolean();
		final boolean withRotation = gd.getNextBoolean();

		final ImageProcessor result =
			align(source.getProcessor(), line1, target.getProcessor(), line2,
				withScaling, withRotation);
		final ImagePlus imp =
			new ImagePlus(source.getTitle() + " aligned to " + target.getTitle(),
				result);
		imp.setCalibration(source.getCalibration());
		imp.setRoi(line2);
		imp.show();
	}

	/**
	 * Align an image to another image given line selections in each.
	 *
	 * @param source the image to align
	 * @param line1 the line selection in the source image
	 * @param target the image to align to
	 * @param line2 the line selection in the target image
	 * @return the aligned image
	 */
	public static ImageProcessor align(final ImageProcessor source,
		final Line line1, final ImageProcessor target, final Line line2)
	{
		return align(source, line1, target, line2, true, true);
	}

	/**
	 * Align an image to another image given line selections in each.
	 *
	 * @param source the image to align
	 * @param line1 the line selection in the source image
	 * @param target the image to align to
	 * @param line2 the line selection in the target image
	 * @param withScaling scale the image if necessary
	 * @param withRotation rotate the image if necessary
	 * @return the aligned image
	 */
	public static ImageProcessor align(final ImageProcessor source,
		final Line line1, final ImageProcessor target, final Line line2,
		final boolean withScaling, final boolean withRotation)
	{
		final int w = target.getWidth(), h = target.getHeight();
		if (source instanceof ColorProcessor) {
			ColorProcessor cp = (ColorProcessor) source;
			final int sourceWidth = source.getWidth(), sourceHeight =
				source.getHeight();
			final byte[][] channels = new byte[3][sourceWidth * sourceHeight];
			cp.getRGB(channels[0], channels[1], channels[2]);
			for (int i = 0; i < 3; i++) {
				final ByteProcessor unaligned =
					new ByteProcessor(sourceWidth, sourceHeight, channels[i], null);
				final ImageProcessor aligned =
					align(unaligned, line1, target, line2, withScaling, withRotation);
				aligned.setMinAndMax(0, 255);
				channels[i] = (byte[]) aligned.convertToByte(true).getPixels();
			}
			cp = new ColorProcessor(w, h);
			cp.setRGB(channels[0], channels[1], channels[2]);
			return cp;
		}
		final ImageProcessor result = new FloatProcessor(w, h);
		final float[] pixels = (float[]) result.getPixels();

		final Interpolator inter = new BilinearInterpolator(source);

		/* the linear mapping to map line1 onto line2 */
		float a00, a01, a02, a10, a11, a12;

		final float dx1 = line1.x2 - line1.x1;
		final float dy1 = line1.y2 - line1.y1;
		final float dx2 = line2.x2 - line2.x1;
		final float dy2 = line2.y2 - line2.y1;

		if (!withRotation) {
			a10 = a01 = 0;
			if (withScaling && (dx2 != 0 || dy2 != 0)) {
				final float length1 = dx1 * dx1 + dy1 * dy1;
				final float length2 = dx2 * dx2 + dy2 * dy2;
				a00 = a11 = (float) Math.sqrt(length1 / length2);
			}
			else {
				a00 = a11 = 1;
			}
		}
		else if (withScaling) {
			final float det = dx2 * dx2 + dy2 * dy2;
			a00 = (dx2 * dx1 + dy2 * dy1) / det;
			a10 = (dx2 * dy1 - dy2 * dx1) / det;
			a01 = -a10;
			a11 = a00;
		}
		else {
			final double aTan = Math.atan2(dy1, dx1) - Math.atan2(dy2, dx2);
			a00 = (float) Math.cos(aTan);
			a10 = (float) Math.sin(aTan);
			a01 = (float) -Math.sin(aTan);
			a11 = (float) Math.cos(aTan);
		}

		final float sourceX = line1.x1 + dx1 / 2.0f;
		final float sourceY = line1.y1 + dy1 / 2.0f;
		final float targetX = line2.x1 + dx2 / 2.0f;
		final float targetY = line2.y1 + dy2 / 2.0f;

		a02 = sourceX - a00 * targetX - a01 * targetY;
		a12 = sourceY - a10 * targetX - a11 * targetY;

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				final float x = i * a00 + j * a01 + a02;
				final float y = i * a10 + j * a11 + a12;
				pixels[i + j * w] = inter.get(x, y);
			}
			IJ.showProgress(j + 1, h);
		}

		result.setMinAndMax(source.getMin(), source.getMax());
		return result;
	}

	protected static abstract class Interpolator {

		ImageProcessor ip;
		int w, h;

		public Interpolator(final ImageProcessor ip) {
			this.ip = ip;
			w = ip.getWidth();
			h = ip.getHeight();
		}

		public abstract float get(float x, float y);
	}

	protected static class BilinearInterpolator extends Interpolator {

		public BilinearInterpolator(final ImageProcessor ip) {
			super(ip);
		}

		@Override
		public float get(final float x, final float y) {
			final int i = (int) x;
			final int j = (int) y;
			final float fx = x - i;
			final float fy = y - j;
			final float v00 = ip.getPixelValue(i, j);
			final float v01 = ip.getPixelValue(i + 1, j);
			final float v10 = ip.getPixelValue(i, j + 1);
			final float v11 = ip.getPixelValue(i + 1, j + 1);
			return (1 - fx) * (1 - fy) * v00 + fx * (1 - fy) * v01 + (1 - fx) * fy *
				v10 + fx * fy * v11;
		}
	}
}
