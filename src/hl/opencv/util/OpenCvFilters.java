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
	
	public static Mat grayscale(Mat aMat)
	{
		return grayscale(aMat, true);
	}
	
	public static Mat grayscale(Mat aMat, boolean isConvertBackOrigType)
	{
		if(aMat!=null && !aMat.empty())
		{
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
			
			return aMat;
		}
		return null;
	}
	
	public static Mat toMask(Mat aMat)
	{
		return grayscale(aMat, false);
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
	
	public static Mat solidfill(Mat aMat, Scalar aScalar)
	{
		Mat matSolid 	= null;
		Mat matMask 	= null;
		try {
			matSolid = new Mat(aMat.size(), aMat.type(), aScalar);
			matMask = OpenCvUtil.colorToMask(aMat, 5);
			Core.copyTo(matSolid, aMat, matMask);
		}
		finally
		{
			if(matSolid!=null)
				matSolid.release();
			
			if(matMask!=null)
				matMask.release();
		}
		return aMat;
	}
	
	public static Mat medianBlur(Mat aMat, double aBlurScale)
	{
		if(aBlurScale>1)
			aBlurScale = 1;
		
		if(aBlurScale<0)
			aBlurScale = 0;
		
		int iBlurScale = (int)(aBlurScale * 100);
		if(iBlurScale%2==0) iBlurScale += 1;
		if(iBlurScale>=100) iBlurScale = 99;
		
		Imgproc.medianBlur(aMat, aMat, iBlurScale);
		
		return aMat;
	}
	
	public static Mat blur(Mat aMat, double aBlurScale)
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
		return aMat;
	}
	
	public static Mat gaussianBlur(Mat aMat, double aBlurScale)
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
		return aMat;
	}
	
	public static Mat cannyEdge(Mat aMat, int aThreshold, boolean isinvert)
	{
		return cannyEdge(aMat, aThreshold, 3, isinvert);
	}
	
	public static Mat cannyEdge(Mat aMat, int aThreshold, int aKernelSize, boolean isinvert)
	{
		cannyEdge(aMat, aThreshold, aThreshold*3, aKernelSize, isinvert);
		return aMat;
	}
	
	private static void cannyEdge(Mat aMat, int aThreshold1, int aThreshold2, int aKernelSize, boolean isinvert)
	{
		if(aMat==null || aMat.empty())
			return;
		
		if(aKernelSize<3)
			aKernelSize = 3;
		else if(aKernelSize>7)
			aKernelSize = 7;
		
		int iOrgChannels = aMat.channels();
		Mat matTmp = new Mat();
		try {
			Imgproc.Canny(aMat, matTmp, aThreshold1, aThreshold2, aKernelSize, false);
			
			if(isinvert)
			{
				Mat matInvert = null;
				try {			
					matInvert = new Mat(matTmp.rows(), matTmp.cols(), matTmp.type(), new Scalar(255,255,255));
					Core.subtract(matInvert, matTmp, matTmp);
					aMat.release();
					aMat = matTmp.clone();
				}
				finally
				{
					if(matInvert!=null)
						matInvert.release();
				}
			}
			
			grayToMultiChannel(matTmp, iOrgChannels);			
		}
		finally
		{
			if(matTmp!=null)
				matTmp.release();
		}
	}
	
	public static Mat pixelate(Mat aMat, double aPixelateScale)
	{
		if(aPixelateScale>1)
			aPixelateScale = 1;
		
		if(aPixelateScale<=0)
			aPixelateScale = 0.1;

		Mat matTmp = aMat.clone();
		
		try {
			if(matTmp.width()>960)
			{
				OpenCvUtil.resizeByWidth(matTmp, 960);
			}
			
			double dRatio = (double)matTmp.width() / (double)matTmp.width();
			double dBlockScale = Math.round(6 * aPixelateScale);
			if(dBlockScale<1)
				dBlockScale = 1;
			double dBlockSize = (10 + dBlockScale) * dRatio;
			
			for(int iX=0; iX<matTmp.width(); iX+=dBlockSize)
			{
				for(int iY=0; iY<matTmp.height(); iY+=dBlockSize)
				{
					Imgproc.rectangle(matTmp, 
							new Rect(iX, iY, (int)dBlockSize, (int)dBlockSize), 
							new Scalar(matTmp.get(iY, iX)), 
							-1);
				}
			}
			
			if(matTmp.width()!= aMat.width())
			{
				OpenCvUtil.resize(matTmp, aMat.width(), aMat.height(), false, Imgproc.INTER_NEAREST); 
				aMat.release();
				aMat = matTmp.clone();
			}
			
		}finally
		{
			if(matTmp!=null)
				matTmp.release();
		}
		return aMat;
	}
	
	//
}
