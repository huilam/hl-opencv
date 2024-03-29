/*
 Copyright (c) 2021 onghuilam@gmail.com
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 The Software shall be used for Good, not Evil.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 
 */

package hl.opencv.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCvFilters{
	
	public static void grayscale(Mat aMat)
	{
		grayscale(aMat, true);
	}
	
	public static void grayscale(Mat aMat, boolean isConvertBackOrigType)
	{
		if(aMat==null || aMat.empty())
		{
			return;
		}
		
		int iOrigChannel = aMat.channels();
		
		switch(iOrigChannel)
		{
			case 3 :  
				Imgproc.cvtColor(aMat, aMat, Imgproc.COLOR_RGB2GRAY);
				break;
			case 4 :  
				Imgproc.cvtColor(aMat, aMat, Imgproc.COLOR_RGBA2GRAY);
				break;
		}
		
		if(isConvertBackOrigType)
		{
			grayToMultiChannel(aMat, iOrigChannel);
		}
	}
	
	public static void toMask(Mat aMat)
	{
		grayscale(aMat, false);
	}
	
	protected static void grayToMultiChannel(Mat aMatGray, int aNewChannelNo)
	{
		if(aMatGray.channels()==1)
		{
			switch(aNewChannelNo)
			{
				case 3 : 
					Imgproc.cvtColor(aMatGray, aMatGray, Imgproc.COLOR_GRAY2RGB);
					break;
				case 4 :  
					Imgproc.cvtColor(aMatGray, aMatGray, Imgproc.COLOR_GRAY2RGBA);
					break;
			}
		}
	}
	
	public static void solidfill(Mat aMat, Scalar aScalar)
	{
		Mat matSolid 	= null;
		Mat matMask 	= null;
		try {
			matSolid = new Mat(aMat.size(), aMat.type(), aScalar);
			matMask = aMat.clone();
			OpenCvUtil.colorToMask(matMask, 5);
			Core.copyTo(matSolid, aMat, matMask);
		}
		finally
		{
			if(matSolid!=null)
				matSolid.release();
			
			if(matMask!=null)
				matMask.release();
		}
	}
	
	public static void medianBlur(Mat aMat, double aBlurScale)
	{
		if(aBlurScale>1)
			aBlurScale = 1;
		
		if(aBlurScale<0)
			aBlurScale = 0;
		
		int iBlurScale = (int)(aBlurScale * 100);
		if(iBlurScale%2==0) iBlurScale += 1;
		if(iBlurScale>=100) iBlurScale = 99;
		
		Imgproc.medianBlur(aMat, aMat, iBlurScale);

	}
	
	public static void blur(Mat aMat, double aBlurScale)
	{
		if(aBlurScale>1)
			aBlurScale = 1;
		
		if(aBlurScale<0)
			aBlurScale = 0;
		
		int iH = (int)(80 * aBlurScale);
		int iW = (int)(80 * aBlurScale);
		
		if(iH>0 && iW>0)
		{
			Size ksize = new Size(iW,iH);
			Imgproc.blur(aMat, aMat, ksize);
		}
	}
	
	public static void gaussianBlur(Mat aMat, double aBlurScale)
	{
		if(aBlurScale>1)
			aBlurScale = 1;
		
		if(aBlurScale<0)
			aBlurScale = 0;
		
		if(aBlurScale>0)
		{
			int iThreshold = (int)(121 * aBlurScale);
			
			if(iThreshold%2==0)
				iThreshold += 1;
			
			Size ksize = new Size(iThreshold, iThreshold);
			Imgproc.GaussianBlur(aMat, aMat, ksize, 0);
		}
	}
	
	public static void cannyEdge(Mat aMat, int aThreshold, boolean isinvert)
	{
		cannyEdge(aMat, aThreshold, 3, isinvert);
	}
	
	public static void cannyEdge(Mat aMat, int aThreshold, int aKernelSize, boolean isinvert)
	{
		cannyEdge(aMat, aThreshold, aThreshold*3, aKernelSize, isinvert);
	}
	
	private static void cannyEdge(Mat aMat, int aThreshold1, int aThreshold2, int aKernelSize, boolean isinvert)
	{
		if(aMat==null || aMat.empty())
		{
			System.err.println("cannyEdge input image is NULL or Empty !");
			return;
		}
		
		if(aKernelSize<3)
			aKernelSize = 3;
		else if(aKernelSize>7)
			aKernelSize = 7;
		
		int iOrgChannels = aMat.channels();
		
		if(aMat.channels()>1)
		{
			OpenCvFilters.grayscale(aMat, false);
		}
		Imgproc.Canny(aMat, aMat, aThreshold1, aThreshold2, aKernelSize, false);
		
		if(isinvert)
		{
			Core.bitwise_not(aMat, aMat);
		}
		grayToMultiChannel(aMat, iOrgChannels);	
	}
	
	public static void pixelate(Mat aMat, double aPixelateScale)
	{
		if(aPixelateScale>1)
			aPixelateScale = 1;
		
		if(aPixelateScale<=0)
			aPixelateScale = 0.1;

		Rect rectOrg = new Rect(0,0,aMat.width(), aMat.height());
		
		//downsize for faster processing
		if(aMat.width()>960)
		{
			OpenCvUtil.resizeByWidth(aMat, 960);
		}
		
		double dRatio = (double)rectOrg.height / (double)rectOrg.width;
		double dBlockScale = Math.round(6 * aPixelateScale);
		if(dBlockScale<1)
			dBlockScale = 1;
		double dBlockSize = (10 + dBlockScale) * dRatio;
		
		for(int iX=0; iX<aMat.width(); iX+=dBlockSize)
		{
			for(int iY=0; iY<aMat.height(); iY+=dBlockSize)
			{
				Imgproc.rectangle(aMat, 
						new Rect(iX, iY, (int)dBlockSize, (int)dBlockSize), 
						new Scalar(aMat.get(iY, iX)), 
						-1);
			}
		}
		
		if(aMat.size()!= rectOrg.size())
		{
			OpenCvUtil.resize(aMat, rectOrg.width, rectOrg.height, false, Imgproc.INTER_NEAREST); 
		}
		
	}
	
	//
}
