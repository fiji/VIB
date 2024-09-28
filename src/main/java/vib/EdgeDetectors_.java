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
package vib;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class EdgeDetectors_ implements PlugInFilter {
	ImagePlus image;

	final static double[][] isotropic={{-1,-1.4142135623731,-1},{0,0,0},{1,1.4142135623731,1}};
	static double roberts[][]={{0,-1},{1,0}};
	static double prewitt[][]={{-1,-1,-1},{0,0,0},{1,1,1}};
	static double sobel[][]={{-1,-2,-1},{0,0,0},{1,2,1}};

	@Override
	public void run(ImageProcessor ip) {
		GenericDialog gd = new GenericDialog("Parameters");
		String list[]={"Roberts","Prewitt","Sobel","Isotropic"};
		gd.addChoice("detector", list, "Sobel");
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		int detector = gd.getNextChoiceIndex();

		double[][] matrix=(detector==0?roberts:detector==1?prewitt:detector==2?sobel:isotropic);
		int N=(detector==0?2:3);

		double[][] matrix2=new double[N][N];

		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
				matrix2[i][j]=(N==2?matrix[1-i][j]:matrix[j][i]);

		ImageStack stack = image.getStack();
		ImageStack res = new ImageStack(stack.getWidth(), stack.getHeight());

		for (int s = 1; s <= stack.getSize(); s++) {
			res.addSlice("", doit(stack.getProcessor(s), matrix,matrix2,N));
		}
		new ImagePlus("Edges" + detector, res).show();
	}


	private ByteProcessor doit(ImageProcessor ip, double[][] matrix1,double[][] matrix2,int N) {
		int w=ip.getWidth(),h=ip.getHeight();
		double[] res=new double[w*h];
		double mmax=-1e9,mmin=1e9;

		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++) {
				float res2=0,res3=0;
				for(int ii=0;ii<N;ii++)
					for(int jj=0;jj<N;jj++)
						if(i+ii-1>=0 && i+ii-1<w && j+jj-1>=0 && j+jj-1<h) {
							float value=ip.getPixel(i+ii-1,j+jj-1);
							res2+=matrix1[ii][jj]*value;
							res3+=matrix2[ii][jj]*value;
						}
				res[i+w*j]=Math.abs(res2)+Math.abs(res3);
				if(res[i+w*j]>mmax) mmax=res[i+w*j];
				if(res[i+w*j]<mmin) mmin=res[i+w*j];
			}

		byte[] res2=new byte[w*h];
		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++)
				res2[i+w*j] = (byte)((res[i+w*j]-mmin)*255.999/(mmax-mmin));
				
		ByteProcessor ip2 = new ByteProcessor(w, h, res2, null);
		return ip2;
	}


	@Override
	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_8G + DOES_16 + DOES_32;
	}

}

