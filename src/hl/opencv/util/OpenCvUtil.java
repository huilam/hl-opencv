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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCvUtil{
	
	public static Mat bufferedImg2Mat(BufferedImage img)
	{
		Mat mat = null;
		if(img!=null)
		{
			img = removeAlphaChannel(img);
			byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
	        mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
	        mat.put(0, 0, data);
		}
        return mat;
	}
	
	public static BufferedImage mat2BufferedImage(Mat aMat){
		int type = BufferedImage.TYPE_BYTE_GRAY;
		switch(aMat.channels())
		{
			case 3 : type = BufferedImage.TYPE_3BYTE_BGR; break;
			case 4 : type = BufferedImage.TYPE_4BYTE_ABGR; break;
		}
		int bufferSize = aMat.channels()* aMat.cols()* aMat.rows();
		byte [] b = new byte[bufferSize];
		aMat.get(0,0,b); // get all the pixels
		BufferedImage image = new BufferedImage(aMat.cols(), aMat.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);  
		return image;
	}
	//
	
	public static Mat resizeByWidth(final Mat aMatImg, int aNewWidth)
	{
		double dImageW = (double)aMatImg.width();
		double dImageH = (double)aMatImg.height();
		
		double dScaleW 	= aNewWidth>0 ? ((double)aNewWidth) / dImageW : 1.0;
		int iNewHeight =(int)(dImageH * dScaleW);

		return resize(aMatImg, aNewWidth, iNewHeight, false);
	}
	
	public static Mat resize(final Mat aMatImg, int aNewWidth, int aNewHeight, boolean isMainAspectRatio)
	{
		if(isMainAspectRatio)
		{
			double dImageW = (double)aMatImg.width();
			double dImageH = (double)aMatImg.height();
			
			double dScaleW 	= (aNewWidth>0)&&(dImageW>0) ? ((double)aNewWidth) / dImageW : 1.0;
			double dScaleH 	= (aNewHeight>0)&&(dImageH>0) ? ((double)aNewHeight) / dImageH : 1.0;
			double dScale = dScaleW>dScaleH ? dScaleH : dScaleW;
			
			aNewWidth = (int)(dImageW * dScale);
			aNewHeight =(int)(dImageH * dScale);
			//System.out.println("dScale="+dScale);
		}
		
		//System.out.println("aNewWidth="+aNewWidth);
		//System.out.println("aNewHeight="+aNewHeight);
		
		Mat matSized = new Mat();
		Imgproc.resize(aMatImg, matSized, new Size(aNewWidth, aNewHeight));
		return matSized;
	}
	//
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold) throws Exception
	{
		if(aDiffThreshold<0 && aDiffThreshold>1)
		{
			throw new Exception("Threshold value should be 0.0-1.0");
		}
		
		//downscale to width=720
		int iProcessWidth = 720;
		
		Mat matInResized = matInput;
		if(matInput.width()>iProcessWidth)
		{
			matInResized = resizeByWidth(matInput, iProcessWidth);
		}
		else
		{
			iProcessWidth = matInResized.width();
		}
		
		Mat matBgResized = matBackground;
		if(matBackground.width()!=iProcessWidth)
		{
			matBgResized = resizeByWidth(matBackground, iProcessWidth);
		}
		
		//
		Mat matMask = new Mat(
				new Size(matInput.width(), matInput.height()), 
				matInput.type(), 
				new Scalar(255,255,255));

		try {
			Core.absdiff(matBgResized, matInResized, matMask);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		
		if(matMask!=null && matMask.width()>0)
		{
			Imgproc.GaussianBlur(matMask, matMask, new Size(21,21), 0);
			
			if(matMask.channels()>2)
			{
				Imgproc.cvtColor(matMask, matMask, Imgproc.COLOR_BGRA2GRAY);
			}
			Imgproc.threshold(matMask, matMask, aDiffThreshold*100, 255, Imgproc.THRESH_BINARY);	
			//
			if(matMask.total()!=matInput.total())
			{
				matMask = resize(matMask, matInput.width(), matInput.height(), false);
			}
		}
		
		return matMask;
	}
	//
	public static Mat colorToWhiteMask(Mat aMat)
	{
		Mat matMask = new Mat();
		Imgproc.threshold(aMat, matMask, 5, 255, Imgproc.THRESH_BINARY);
		return grayscale(matMask, false);
	}
	
	//
	
	public static Mat grayscale(Mat aMat)
	{
		return grayscale(aMat, true);
	}
	
	protected static Mat grayscale(Mat aMat, boolean isConvertBackOrigType)
	{
		Mat matGray = aMat.clone();
		int iOrigChannel = aMat.channels();
		
		//System.out.println("grayscale.channels="+matGray.channels());
		switch(iOrigChannel)
		{
			case 3 :  
				Imgproc.cvtColor(aMat, matGray, Imgproc.COLOR_RGB2GRAY); 
				if(isConvertBackOrigType)
					Imgproc.cvtColor(matGray, matGray, Imgproc.COLOR_GRAY2RGB);
				break;
			case 4 :  
				Imgproc.cvtColor(aMat, matGray, Imgproc.COLOR_RGBA2GRAY); 
				if(isConvertBackOrigType)
					Imgproc.cvtColor(matGray, matGray, Imgproc.COLOR_GRAY2RGBA);
				break;
		}
		//System.out.println("grayscale.channels="+matGray.channels());
		return matGray;
	}
	
	public static Mat solidfill(Mat aMat, Scalar aScalar)
	{
		Mat matSolid = new Mat(aMat.size(), aMat.type(), aScalar);
		
		Mat matMask = colorToWhiteMask(aMat);
		
		Mat matReturn = new Mat();
		Core.copyTo(matSolid, matReturn, matMask);
		
		return matReturn;
	}
	
	public static Mat mediumBlur(Mat aMat, double aBlurScale)
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
	
	
	public static Mat pixelate(Mat aMat, double aPixelateScale)
	{
		if(aPixelateScale>1)
			aPixelateScale = 1;
		
		if(aPixelateScale<0)
			aPixelateScale = 0;
		
		int iNewWidth = (int)((1.0 - aPixelateScale) * aMat.width());
		Mat matPixelated = resizeByWidth(aMat.clone(), iNewWidth);
		return resizeByWidth(matPixelated, aMat.width());
	}
	
	//
	
	protected static Mat removeAlphaChannel(Mat matInput)
	{
		if(matInput.channels()==4)
		{
			Mat matReturn = matInput.clone();
			Vector<Mat> vRgba = new Vector<Mat>();
			Core.split(matReturn, vRgba);
			vRgba.remove(vRgba.size()-1);
			Core.merge(vRgba, matReturn);
			return matReturn;
		}
		return matInput;
	}
	
	protected static BufferedImage removeAlphaChannel(BufferedImage aImage)
	{
		if(aImage.getAlphaRaster()!=null)
		{
			BufferedImage imgTmp = new BufferedImage(
					aImage.getWidth(), aImage.getHeight(), 
					BufferedImage.TYPE_3BYTE_BGR);
	
			Graphics2D g = null;
			try {
				g = imgTmp.createGraphics();
				g.drawImage(aImage, 0, 0, null);
			}
			finally
			{
				if(g!=null)
					g.dispose();
			}
			return imgTmp;
		}
		else
		{
			return aImage;			
		}
	}
	
	public static Mat loadImage(String aImageURI)
	{
		return Imgcodecs.imread(aImageURI);
	}
	
	public static void saveImageAsFile(Mat aMatInput, String aFileName)
	{
		Imgcodecs.imwrite(aFileName, aMatInput);
	}
	
}
