import ij.ImageListener;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class CollectGarbage_ implements PlugIn {
	@Override
	public void run(String arg) {
		if ("onclose".equals(arg))
			registerCloseListener();
		else
			run();
	}

	public static void registerCloseListener() {
		ImagePlus.addImageListener(new ImageListener() {
			@Override
			public void imageOpened(ImagePlus image) {}
			@Override
			public void imageUpdated(ImagePlus image) {}
			@Override
			public void imageClosed(ImagePlus image) {
				run();
			}
		});
	}

	public static void run() {
		System.gc();
		System.gc();
	}
}

