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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
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
			int iChannel = 4;
			
			int iCvType = CvType.CV_8UC4;
			if(img.getAlphaRaster()==null)
			{
				iCvType = CvType.CV_8UC3;
				iChannel = 3;
			}
			
	        mat = new Mat(img.getHeight(), img.getWidth(), iCvType);
			DataBuffer datBuf = img.getRaster().getDataBuffer();
			byte[] dataBytes = null;
			
			switch (datBuf.getDataType())
			{
		    	case DataBuffer.TYPE_BYTE:
		    		dataBytes = ((DataBufferByte)datBuf).getData();
		    		break;
		    	case DataBuffer.TYPE_INT:
		    		int[] dataInts = ((DataBufferInt)datBuf).getData();
		    		ByteBuffer byteBuf = ByteBuffer.allocate(iChannel * dataInts.length);
		            for (int i = 0; i < dataInts.length; i++)
		            {
		            	byteBuf.putInt(i, dataInts[i]);
		            }
		            dataBytes = byteBuf.array();
		    		break;
			}
			
			mat.put(0, 0, dataBytes);
			
			if(iChannel==4)
			{
				//BufferedImage.TYPE_4BYTE_ABGR
				//Imgproc.COLOR_BGR2BGRA
				Vector<Mat> vMat = new Vector<>();
				Core.split(mat, vMat);
				Mat matAlpha = vMat.remove(0);
				vMat.add(matAlpha);
				Core.merge(vMat, mat);
			}
			
		}
        return mat;
	}
	
	public static BufferedImage mat2BufferedImage(Mat aMat){
		Mat mat = aMat.clone();
		int type = BufferedImage.TYPE_BYTE_GRAY;
		switch(mat.channels())
		{
			case 3 : type = BufferedImage.TYPE_3BYTE_BGR; break;
			case 4 : type = BufferedImage.TYPE_4BYTE_ABGR; 
			
				Vector<Mat> vMat = new Vector<>();
				Core.split(mat, vMat);
				Mat matAlpha = vMat.remove(vMat.size()-1);
				vMat.add(0,matAlpha);
				Core.merge(vMat, mat);
				break;
		}
		int bufferSize = mat.channels()* mat.cols()* mat.rows();
		byte [] b = new byte[bufferSize];
		mat.get(0,0,b); // get all the pixels
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		
		try {
			System.arraycopy(b, 0, targetPixels, 0, b.length);  
		}catch(Exception ex)
		{
			System.err.println(">>> aMat.rows="+mat.rows());
			System.err.println(">>> aMat.cols="+mat.cols());
			System.err.println(">>> aMat.channels="+mat.channels());
			System.err.println(">>> bufferSize="+bufferSize);
			System.err.println(">>> b.length="+b.length);
			System.err.println(">>> targetPixels.length="+targetPixels.length);
			
			throw ex;
		}
		
		return image;
	}
	//
	
	public static Mat resizeByWidth(final Mat aMatImg, int aNewWidth)
	{
		return resizeByWidth(aMatImg, aNewWidth, Imgproc.INTER_LINEAR);
	}
	
	public static Mat resizeByWidth(final Mat aMatImg, int aNewWidth, int aMode)
	{
		double dImageW = (double)aMatImg.width();
		double dImageH = (double)aMatImg.height();
		
		double dScaleW 	= aNewWidth>0 ? ((double)aNewWidth) / dImageW : 1.0;
		int iNewHeight =(int)(dImageH * dScaleW);

		return resize(aMatImg, aNewWidth, iNewHeight, false, aMode);
	}
	
	public static Mat resize(final Mat aMatImg, int aNewWidth, int aNewHeight, boolean isMainAspectRatio)
	{
		return resize(aMatImg, aNewWidth, aNewHeight, isMainAspectRatio, Imgproc.INTER_LINEAR);
	}
	
	public static Mat resize(final Mat aMatImg, int aNewWidth, int aNewHeight, boolean isMainAspectRatio, int aMode)
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
		Imgproc.resize(aMatImg, matSized, new Size(aNewWidth, aNewHeight), aMode);
		return matSized;
	}
	//
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold) throws Exception
	{
		return extractFGMask(matInput, matBackground, aDiffThreshold, 600, 500);
	}
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize) throws Exception
	{
		if(aDiffThreshold<0 || aDiffThreshold>1)
		{
			throw new Exception("Threshold value should be 0.0-1.0");
		}
		
		//downscale to width=360
		int iProcessWidth = 360;
		
		if(aProcessWidth>0)
			iProcessWidth = aProcessWidth;
		
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
				Scalar.all(255));

		try {
			Core.absdiff(matBgResized, matInResized, matMask);
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
				
				matMask = resize(matMask, matInput.width(), matInput.height(), false);
			}
		}
		
		return matMask;
	}
	//
	
	public static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea)
	{
		return removeMaskContourAreas(aMatMask, aMinContourArea, aMaxContourArea,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	}
	
	public static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea,
			int iFindContourMode, int iFindContourMethod)
	{
		if(aMatMask!=null && !aMatMask.empty())
		{
			if(aMinContourArea>0 || aMaxContourArea>0) 
			{
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
				
				Imgproc.findContours(aMatMask, contours, new Mat(), 
						iFindContourMode, iFindContourMethod);
				
				List<MatOfPoint> contours_remove = new ArrayList<MatOfPoint>();
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
		
		return aMatMask;
	}
	
	//
	public static Mat colorToWhiteMask(Mat aMat)
	{
		Mat matMask = new Mat();
		Imgproc.threshold(aMat, matMask, 5, 255, Imgproc.THRESH_BINARY);
		return grayscale(matMask, false);
	}
	
	//
	public static Mat adjust(Mat aMat, double aBeta, Scalar aScalar)
	{

//alpha = 1.5 # Contrast control (1.0-3.0)
//beta = 0 # Brightness control (0-100)

		if(aBeta<0) aBeta = 0;
		if(aBeta>1) aBeta = 1;
		
	    Mat mat2 = aMat;
	    if(aScalar!=null)
	    {
	    	mat2 = new Mat(aMat.height(), aMat.width(), aMat.type(), aScalar);
	    }
	    
	    double alpha = 1.0 - aBeta;
	    
	    Mat matAdjusted = new Mat();
	    Core.addWeighted(aMat, alpha, mat2, aBeta, 0.0, matAdjusted);
	    return matAdjusted;
	}
	
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
	
	public static Mat toHSV(Mat aMat)
	{
		Mat matHSV = aMat.clone();
		int iOrigChannel = aMat.channels();
		
		switch(iOrigChannel)
		{
			case 1 :  
				Imgproc.cvtColor(matHSV, matHSV, Imgproc.COLOR_GRAY2BGR);
				//let it continue to convert to HSV
			case 3 :  
			case 4 :  
				Imgproc.cvtColor(matHSV, matHSV, Imgproc.COLOR_BGR2HSV);
				break;
		}
		
		return matHSV;
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
		
		Mat matMask = colorToWhiteMask(aMat);
		
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
			aPixelateScale = 0;
		
		int iNewWidth = (int)((1.0 - aPixelateScale) * aMat.width());
		Mat matPixelated = resizeByWidth(aMat.clone(), iNewWidth, Imgproc.INTER_LINEAR);
		return resizeByWidth(matPixelated, aMat.width(), Imgproc.INTER_NEAREST);
	}
	
	//
	
	public static Mat adjBrightness(Mat aMatIn, double aBrightness)
	{
		double dContrast = 1 + (aBrightness/100);
		return adjBrightness(aMatIn, dContrast, aBrightness);
	}
	
	public static Mat adjBrightness(Mat aMatIn, double aContrast, double aBrightness)
	{
		Mat aMatOut = new Mat();
		//Contrast control (1.0-3.0)
		//Brightness control (0-100)
		Core.convertScaleAbs(aMatIn, aMatOut, aContrast, aBrightness);
		return aMatOut;
	}
	
	public static double calcBrightnessDiff(Mat aMat1, Mat aMat2)
	{
		double dDiff = 0;
		
		Mat mat1 = OpenCvUtil.resizeByWidth(aMat1.clone(), 100);
		Mat mat2 = OpenCvUtil.resizeByWidth(aMat2.clone(), 100);
		
		Mat matHSV1 = OpenCvUtil.toHSV(mat1);
		Mat matHSV2 = OpenCvUtil.toHSV(mat2);
		
		Scalar scalar1 = Core.mean(matHSV1);
		Scalar scalar2 = Core.mean(matHSV2);

		if(scalar1!=null && scalar2!=null && scalar1.val.length>0)
		{
			//H=color S=gray V=brightness
			double dVal1 = (scalar1.val)[1]; //2
			double dVal2 = (scalar2.val)[1]; //2
			dDiff = (dVal1 - dVal2);
		}
		
		return dDiff;
	}
	
	public static Mat matchImageTemplate(Mat matImage, Mat matTempl)
	{
		Mat matResult = new Mat();
		Imgproc.matchTemplate(matImage, matTempl, matResult, 0);
		return matResult;
	}
	
	public static double calcBlurriness(Mat matImage)
	{
		Mat matLaplacian = new Mat();
		Mat matGray = matImage.clone();
		
		Imgproc.Laplacian(matGray, matLaplacian, 3); 
		MatOfDouble median = new MatOfDouble();
		MatOfDouble std= new MatOfDouble();        
		Core.meanStdDev(matLaplacian, median , std);

		double dSharpness = Math.pow(std.get(0,0)[0],2);
		
		if(dSharpness>100)
			dSharpness = 100;
		
		return 1-(dSharpness/100);
	}
	
	public static double compareSimilarity(Mat matImage1, Mat matImage2, int iMode)
	{
		Mat matResized1 = matImage1.clone();
		Mat matResized2 = matImage2.clone();
				
		if(matImage1.width()>640)
		{
			matResized1 = resizeByWidth(matResized1, 640);
		}
		
		if(matImage2.width()!=matResized1.width())
		{
			matResized2 = resizeByWidth(matResized2, matResized1.width());
		}
		//String sOutputPath = new File("./test/images/output").getAbsolutePath();
		
		Mat matGray1 = grayscale(matResized1, false);
		Mat matGray2 = grayscale(matResized2, false);
		
		matGray1 = medianBlur(matGray1, 0.08);
		matGray2 = medianBlur(matGray2, 0.08);
		//saveImageAsFile(matGray1, sOutputPath+"/matGray1.jpg");
		//saveImageAsFile(matGray2, sOutputPath+"/matGray2.jpg");
		
		int iEdgeThreshold = 50;
		Mat matEdge1 = cannyEdge(matGray1, iEdgeThreshold, false);
		Mat matEdge2 = cannyEdge(matGray2, iEdgeThreshold, false);

		//saveImageAsFile(matEdge1, sOutputPath+"/matEdge1_"+iEdgeThreshold+".jpg");
		//saveImageAsFile(matEdge2, sOutputPath+"/matEdge2_"+iEdgeThreshold+".jpg");

		double dScore1 = Imgproc.matchShapes(
				matEdge1, matEdge2, iMode, 0);
		
		
		return 1-dScore1;
	}
	
	public static Mat addAlphaChannel(Mat matInput)
	{
		if(matInput.channels()==4)
		{
			return matInput;
		}
		
		Mat matWithAlpha = matInput.clone();
		switch(matInput.channels())
		{
			case 1:
				Imgproc.cvtColor(matInput, matWithAlpha, Imgproc.COLOR_GRAY2BGRA);
				break;
			case 3:
				Imgproc.cvtColor(matInput, matWithAlpha, Imgproc.COLOR_BGR2BGRA);
				break;
		}
		
		return matWithAlpha;
	}
	
	public static Mat removeAlphaChannel(Mat matInput)
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
		return Imgcodecs.imread(aImageURI, Imgcodecs.IMREAD_UNCHANGED);
	}
	
	public static void saveImageAsFile(Mat aMatInput, String aFileName)
	{
		Imgcodecs.imwrite(aFileName, aMatInput);
	}
	
}
