/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

import amira.AmiraMeshDecoder;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;

import java.io.File;

public class AmiraMeshReader_ extends ImagePlus implements PlugIn {

	@Override
	public void run(String arg) {
		boolean showIt = (IJ.getInstance() != null && arg.equals(""));
		String dir="";
		if(arg==null || arg.equals("")) {
			OpenDialog od = new OpenDialog("AmiraFile", null);
			dir=od.getDirectory();
			arg=od.getFileName();
		}
		if(arg==null)
			return;
		AmiraMeshDecoder d=new AmiraMeshDecoder();
		if(d.open(dir+arg)) {
			if (d.isTable()) {
				TextWindow table = d.getTable();
				if(showIt)
					table.setVisible(true);
			} else {
				FileInfo fi=new FileInfo();
				File file = new File(dir+arg);
				fi.fileName=file.getName();
				fi.directory=file.getParent();				
				setFileInfo(fi);				
				setStack(arg,d.getStack());
				d.parameters.setParameters(this);
				if (showIt)
					show();
			}
		}
	}

}


