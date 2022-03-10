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
		Mat matGray = aMat.clone();
		int iOrigChannel = aMat.channels();
		
		switch(iOrigChannel)
		{
			case 3 :  
				Imgproc.cvtColor(aMat, matGray, Imgproc.COLOR_RGB2GRAY);
				break;
			case 4 :  
				Imgproc.cvtColor(aMat, matGray, Imgproc.COLOR_RGBA2GRAY);
				break;
		}
		
		if(isConvertBackOrigType)
		{
			matGray = grayToMultiChannel(matGray, iOrigChannel);
		}
		
		//System.out.println("grayscale.channels="+matGray.channels());
		return matGray;
	}
	
	private static Mat grayToMultiChannel(Mat aMatGray, int aNewChannelNo)
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
		return aMatGray;
	}
	
	public static Mat solidfill(Mat aMat, Scalar aScalar)
	{
		Mat matSolid = new Mat(aMat.size(), aMat.type(), aScalar);
		
		Mat matMask = OpenCvUtil.colorToWhiteMask(aMat);
		
		Mat matReturn = new Mat();
		Core.copyTo(matSolid, matReturn, matMask);
		
		return matReturn;
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
		
		Mat matBlurred = new Mat();
		Imgproc.medianBlur(aMat, matBlurred, iBlurScale);
		
		return matBlurred;
	}
	
	public static Mat blur(Mat aMat, double aBlurScale)
	{
		if(aBlurScale>1)
			aBlurScale = 1;
		
		if(aBlurScale<0)
			aBlurScale = 0;
		
		
		int iH = (int)(80 * aBlurScale);
		int iW = (int)(80 * aBlurScale);
		
		Mat matBlurred = new Mat();
		if(iH>0 && iW>0)
		{
			Size ksize = new Size(iW,iH);
			Imgproc.blur(aMat, matBlurred, ksize);
		}
		else
		{
			matBlurred = aMat;
		}
		return matBlurred;
	}
	
	public static Mat gaussianBlur(Mat aMat, double aBlurScale)
	{
		if(aBlurScale>1)
			aBlurScale = 1;
		
		if(aBlurScale<0)
			aBlurScale = 0;

		Mat matBlurred = new Mat();
		
		if(aBlurScale>0)
		{
			int iThreshold = (int)(121 * aBlurScale);
			
			if(iThreshold%2==0)
				iThreshold += 1;
			
			Size ksize = new Size(iThreshold, iThreshold);
			Imgproc.GaussianBlur(aMat, matBlurred, ksize, 0);
		}
		return matBlurred;
	}
	
	public static Mat cannyEdge(Mat aMat, int aThreshold, boolean isinvert)
	{
		Mat matEdges = new Mat();
		Imgproc.Canny(aMat, matEdges, aThreshold, aThreshold*2, 3, false);
		
		if(isinvert)
		{
			Mat matInvert = new Mat(matEdges.rows(), matEdges.cols(), matEdges.type(), new Scalar(255,255,255));
			Core.subtract(matInvert, matEdges, matEdges);
		}
		
		matEdges = grayToMultiChannel(matEdges, aMat.channels());
		return matEdges;
	}
	
	public static Mat pixelate(Mat aMat, double aPixelateScale)
	{
		if(aPixelateScale>1)
			aPixelateScale = 1;
		
		if(aPixelateScale<0)
			aPixelateScale = 0.001;
		
		int iNewWidth = (int)((1.0 - aPixelateScale) * (aMat.width()*0.25));
		int iNewHeight = (int)((1.0 - aPixelateScale) * (aMat.height()*0.25));
		
		Mat matPixelated = OpenCvUtil.resize(aMat.clone(), iNewWidth, iNewHeight, true);
		return OpenCvUtil.resize(matPixelated, aMat.width(), aMat.height(), false);
	}
	
	//
}
