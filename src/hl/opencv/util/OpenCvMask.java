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

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCvMask{
	
	
	protected static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold) throws Exception
	{
		int iProcWidth = 1080;
		
		if(matInput.width()<iProcWidth)
			iProcWidth = matInput.width();
		
		int iMinObjSize = iProcWidth/5;
		
		return extractFGMask(matInput, matBackground, aDiffThreshold, iProcWidth, iMinObjSize);
	}
	
	protected static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize) throws Exception
	{
		return extractFGMask(matInput, matBackground, aDiffThreshold, aProcessWidth, minContourPixelSize, false);
	}
	
	protected static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize, boolean isGrayscale) throws Exception
	{
		if(matBackground==null || matBackground.empty())
			return null;
		
		Mat matMask = null;
		Mat matBgResized = matBackground.clone();
		Mat matInResized = matInput.clone();
		
		try {
			if(aDiffThreshold<0 || aDiffThreshold>1)
			{
				throw new Exception("Threshold value should be 0.0-1.0");
			}
			
			//downscale to width=360
			int iProcessWidth = 360;
			
			if(aProcessWidth>0)
				iProcessWidth = aProcessWidth;
			
			if(matInResized.width()>iProcessWidth)
			{
				OpenCvUtil.resizeByWidth(matInResized, iProcessWidth);
			}
			else
			{
				iProcessWidth = matInResized.width();
			}
			
			if(matBgResized.width()!=iProcessWidth)
			{
				OpenCvUtil.resizeByWidth(matBgResized, iProcessWidth);
			}
			
			//
			matMask = new Mat(
					new Size(matInput.width(), matInput.height()), 
					matInput.type(), 
					Scalar.all(255));
	
			try {
				
				if(isGrayscale)
				{
					matBgResized = OpenCvFilters.grayscale(matBgResized);
					matInResized = OpenCvFilters.grayscale(matInResized);
				}
				
				if(!matBgResized.empty())
				{
					Core.absdiff(matBgResized, matInResized, matMask);
				}
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			
			if(matMask!=null && matMask.width()>0)
			{
				Imgproc.GaussianBlur(matMask, matMask, new Size(11,11), 0);
				
				switch(matMask.channels())
				{
					case 3 : 
						Imgproc.cvtColor(matMask, matMask, Imgproc.COLOR_BGR2GRAY);
						break;
						
					case 4 : 
						Imgproc.cvtColor(matMask, matMask, Imgproc.COLOR_BGRA2GRAY);
						break;
				}	
				Imgproc.threshold(matMask, matMask, aDiffThreshold*100, 255, Imgproc.THRESH_BINARY);	
				//
				if(matMask.total()!=matInput.total())
				{
					if(minContourPixelSize>0)
					{
						matMask = removeMaskContourAreas(matMask,minContourPixelSize,0);
					}
					
					OpenCvUtil.resize(matMask, matInput.width(), matInput.height(), false);
				}
			}
		}
		finally
		{
			if(matBgResized!=null)
				matBgResized.release();
			
			if(matInResized!=null)
				matInResized.release();
		}
		
		return matMask;
	}
	//
	
	protected static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea)
	{
		return removeMaskContourAreas(aMatMask, aMinContourArea, aMaxContourArea,
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	}
	
	protected static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea,
			int iFindContourMode, int iFindContourMethod)
	{
		if(aMatMask!=null && !aMatMask.empty())
		{
			if(aMinContourArea>0 || aMaxContourArea>0) 
			{
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
				List<MatOfPoint> contours_remove = null;
				
				try {
				
					Imgproc.findContours(aMatMask, contours, new Mat(), 
							iFindContourMode, iFindContourMethod);
					
					if(contours.size()>0)
					{
						contours_remove = new ArrayList<MatOfPoint>();
						for(MatOfPoint c : contours)
						{
							if(aMinContourArea>0 && Imgproc.contourArea(c)<aMinContourArea)
								contours_remove.add(c);
							
							if(aMaxContourArea>0 && Imgproc.contourArea(c)>aMaxContourArea)
								contours_remove.add(c);
						}
						if(contours_remove.size()>0)
							Imgproc.drawContours(aMatMask, contours_remove, -1, new Scalar(0), -1);
					}
				}
				finally
				{
					if(contours!=null)
					{
						for(MatOfPoint c : contours)
						{
							c.release();
						}
					}
					
					if(contours_remove!=null)
					{
						for(MatOfPoint c : contours_remove)
						{
							c.release();
						}
					}
				}
			}
		}	
		
		return aMatMask;
	}
	
	//
	
	protected static Mat colorToMask(Mat aMat)
	{
		return colorToMask(aMat, 50);
	}
	
	protected static Mat colorToMask(Mat aMat, int aThreshold)
	{
		Imgproc.threshold(aMat, aMat, aThreshold, 255, Imgproc.THRESH_BINARY);
		return OpenCvFilters.grayscale(aMat, false);
	}
	
	protected static Mat getMask(final Mat aMat1, Scalar aFromScalar, Scalar aToScalar)
	{
		Mat matMask = null;
		
		if(aMat1!=null && aFromScalar!=null && aToScalar!=null)
		{
			Mat matHsv = null;
			
			try
			{
				matMask = new Mat();
				matHsv = OpenCvUtil.toHSV(aMat1);
				Core.inRange(matHsv, aFromScalar, aToScalar, matMask);
			}
			finally
			{
				if(matHsv!=null)
					matHsv.release();
			}

		}
		return matMask;
	}
	
	protected static Rect calcMaskTrimRect(Mat aMask)
	{
		Rect rect = null;
		
		if(aMask!=null && aMask.channels()==1)
		{
			rect = new Rect(0,0,aMask.width(), aMask.height());
			
			int iMinVal = 255;
			
			int iLeftX = getMinX(aMask, iMinVal);
			int iTopY = getMinY(aMask, iMinVal);
			int iRightX = rect.width;
			int iBottomY = rect.height;
			
			Mat flippedMask = new Mat();
			
			try {
				Core.flip(aMask, flippedMask, -1);
				iRightX -= getMinX(flippedMask, iMinVal);
				iBottomY -= getMinY(flippedMask, iMinVal);
			}
			finally
			{
				if(flippedMask!=null)
					flippedMask.release();
			}
			rect.x =  iLeftX;
			rect.y =  iTopY;
			rect.width =  iRightX;
			rect.height =  iBottomY;
		}
		return rect;
	}
	
	
	private static int getMinX(Mat aBinaryMask, int aTargetMinVal)
	{
		if(aBinaryMask!=null && aBinaryMask.channels()==1)
		{
			int iW = aBinaryMask.width();
			int iH = aBinaryMask.height();
			
			for(int x=0; x<iW; x++)
			{
				for(int y=0; y<iH; y++)
				{
					double[] dVal = aBinaryMask.get(y, x);
					
					if(dVal[0]>=aTargetMinVal)
					{
						return x;
					}
				}
			}
		}
		return -1;
	}
	
	private static int getMinY(Mat aBinaryMask, int aTargetMinVal)
	{
		if(aBinaryMask!=null && aBinaryMask.channels()==1)
		{
			int iW = aBinaryMask.width();
			int iH = aBinaryMask.height();
			
			for(int y=0; y<iH; y++)
			{
				for(int x=0; x<iW; x++)
				{
					double[] dVal = aBinaryMask.get(y, x);
					
					if(dVal[0]>=aTargetMinVal)
					{
						return y;
					}
				}
			}
		}
		return -1;
	}
	
	//////////////////////////////////////////////////
	
	protected static void reduceMaskNoise(Mat aBinaryMask)
	{
		reduceMaskNoise(aBinaryMask, 50);
	}
	
	protected static void reduceMaskNoise(Mat aBinaryMask, int aMinNoiseArea)
	{
		if(aBinaryMask!=null && aBinaryMask.channels()==1)
		{
			int iMinContour = aMinNoiseArea;
			
			long lTotalMaskArea = aBinaryMask.width()*aBinaryMask.height();
			
			if(iMinContour>=(lTotalMaskArea*0.01))
				iMinContour = (int)(iMinContour * 0.2);
			
			Core.bitwise_not(aBinaryMask, aBinaryMask);
			aBinaryMask = OpenCvUtil.removeMaskContourAreas(aBinaryMask,iMinContour,0);
			
			Core.bitwise_not(aBinaryMask, aBinaryMask);
			aBinaryMask = OpenCvUtil.removeMaskContourAreas(aBinaryMask,iMinContour,0);			
		}
	}	
	
}
