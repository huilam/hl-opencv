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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCvUtil{
	
	private static Decoder base64Decoder = Base64.getDecoder();
	private static Encoder base64Encoder = Base64.getEncoder();
	
	public static Mat base64Img2Mat(String aBase64Img)
	{
		Mat mat = null;
	    byte[] bytes =  base64Decoder.decode(aBase64Img);
	    
	    MatOfByte matOfBytes = null;
	    
	    try {
	    	matOfBytes = new MatOfByte(bytes);
		    mat = Imgcodecs.imdecode(matOfBytes, Imgcodecs.IMREAD_UNCHANGED);
	    }
	    finally
	    {
	    	if(matOfBytes!=null)
	    		matOfBytes.release();
	    }
	    
	    return mat;
	}
	
	public static String mat2base64Img(Mat aMat, String aImgFormat)
	{
		Map<Integer, Integer> mapImgParams = new HashMap<Integer,Integer>();
		
		if(aImgFormat==null)
			aImgFormat = ".jpg";
		
		if(aImgFormat.toLowerCase().endsWith("jpg"))
		{
			mapImgParams.put(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
		}
		
		return mat2base64Img(aMat, aImgFormat, mapImgParams);
	}
	
	public static String mat2base64Img(Mat aMat, String aImgFormat, Map<Integer, Integer> aMapImgParams)
	{
		String sBase64 = null; 
		if(aMat!=null)
		{
			if(aImgFormat==null)
				aImgFormat = ".jpg";
			else if (!aImgFormat.startsWith("."))
				aImgFormat = "." + aImgFormat;
			
			MatOfInt matOfInt = null;
			
			try {
			
				matOfInt = new MatOfInt();
				
				if(aMapImgParams!=null && aMapImgParams.size()>0)
				{
					List<Integer> list = new ArrayList<Integer> ();
					Iterator<Integer> iter = aMapImgParams.keySet().iterator();
					while(iter.hasNext())
					{
						Integer ParamName 	= iter.next();
						Integer ParamValue 	= aMapImgParams.get(ParamName);
						
						if(ParamValue!=null)
						{
							list.add(ParamName);
							list.add(ParamValue);
						}
					}
					matOfInt.fromList(list);
				}
				
				MatOfByte matByteBuf = null;
				try {
					matByteBuf = new MatOfByte();
					Imgcodecs.imencode(aImgFormat, aMat, matByteBuf, matOfInt);
					byte[] bytes = matByteBuf.toArray(); 
					sBase64 = base64Encoder.encodeToString(bytes);
				}
				finally
				{
					if(matByteBuf!=null)
						matByteBuf.release();
				}
			}
			finally
			{
				if(matOfInt!=null)
					matOfInt.release();
			}
		}
	    return sBase64;
	}

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
				Mat matAlpha = null;
				try {
					matAlpha = vMat.remove(0);
					vMat.add(matAlpha);
					Core.merge(vMat, mat);
				}
				finally
				{
					if(matAlpha!=null)
						matAlpha.release();
				}
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
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold) throws Exception
	{
		int iProcWidth = 1080;
		
		if(matInput.width()<iProcWidth)
			iProcWidth = matInput.width();
		
		int iMinObjSize = iProcWidth/5;
		
		return extractFGMask(matInput, matBackground, aDiffThreshold, iProcWidth, iMinObjSize);
	}
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize) throws Exception
	{
		return extractFGMask(matInput, matBackground, aDiffThreshold, aProcessWidth, minContourPixelSize, false);
	}
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize, boolean isGrayscale) throws Exception
	{
		
		Mat matMask = null;
		Mat matBgResized = null;
		Mat matInResized = null;
		
		try {
			if(aDiffThreshold<0 || aDiffThreshold>1)
			{
				throw new Exception("Threshold value should be 0.0-1.0");
			}
			
			//downscale to width=360
			int iProcessWidth = 360;
			
			if(aProcessWidth>0)
				iProcessWidth = aProcessWidth;
			
			matInResized = matInput;
			if(matInput.width()>iProcessWidth)
			{
				matInResized = resizeByWidth(matInput, iProcessWidth);
			}
			else
			{
				iProcessWidth = matInResized.width();
			}
			
			matBgResized = matBackground;
			if(matBackground.width()!=iProcessWidth)
			{
				matBgResized = resizeByWidth(matBackground, iProcessWidth);
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
	
	public static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea)
	{
		return removeMaskContourAreas(aMatMask, aMinContourArea, aMaxContourArea,
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	}
	
	public static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea,
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
		
		Mat matAdjusted = null;
		Mat mat2 = null;
		
		try {
		    mat2 = aMat;
		    if(aScalar!=null)
		    {
		    	mat2 = new Mat(aMat.height(), aMat.width(), aMat.type(), aScalar);
		    }
		    
		    double alpha = 1.0 - aBeta;
		    
		    matAdjusted = new Mat();
		    Core.addWeighted(aMat, alpha, mat2, aBeta, 0.0, matAdjusted);
		}
		finally
		{
			if(mat2!=null)
				mat2.release();
		}
	    return matAdjusted;
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
	
	public static double compareBrightnessDiff(Mat aMat1, Mat aMat2)
	{
		double dMean1 = calcBrightness(aMat1);
		double dMean2 = calcBrightness(aMat2);
		
		return dMean1-dMean2;
	}
	
	public static double calcBrightnessDiff(Mat aMat1, Mat aMat2)
	{
		return compareBrightnessDiff(aMat1, aMat2);
	}
	
	public static double calcBrightness(Mat aMat1)
	{
		if(aMat1==null)
			return 0;
		
		Mat mat1 = null;
		Mat matHSV1 = null;
		
		try {
		
			mat1 = OpenCvUtil.resizeByWidth(aMat1.clone(), 100);
			
			matHSV1 = OpenCvUtil.toHSV(mat1);
			
			Scalar scalar1 = Core.mean(matHSV1);
	
			if(scalar1!=null && scalar1.val.length>0)
			{
				//H=color S=gray V=brightness
				double dVal1 = (scalar1.val)[2];
				return dVal1 / 255;
			}
		}
		finally
		{
			if(mat1!=null)
				mat1.release();
			
			if(matHSV1!=null)
				matHSV1.release();
		}
		
		return 0;
	}
	
	public static Mat matchImageTemplate(Mat matImage, Mat matTempl)
	{
		Mat matResult = new Mat();
		Imgproc.matchTemplate(matImage, matTempl, matResult, 0);
		return matResult;
	}
	
	public static double calcBlurriness(Mat matImage)
	{
		Mat matLaplacian = null;
		Mat matGray = null;
		MatOfDouble median = null;
		MatOfDouble std = null;
		
		try {
			matLaplacian = new Mat();
			matGray = matImage.clone();
			
			Imgproc.Laplacian(matGray, matLaplacian, 3); 
			median = new MatOfDouble();
			std= new MatOfDouble();        
			Core.meanStdDev(matLaplacian, median , std);
			double dSharpness = Math.pow(std.get(0,0)[0],2);
			
			if(dSharpness>100)
				dSharpness = 100;
			
			return 1-(dSharpness/100);
		}
		finally
		{
			if(matLaplacian!=null)
				matLaplacian.release();
			
			if(matGray!=null)
				matGray.release();
			
			if(median!=null)
				median.release();
			
			if(std!=null)
				std.release();
		}
	}
	
	public static double compareSimilarity(Mat matImage1, Mat matImage2, int iMode)
	{
		Mat matResized1 = null;
		Mat matResized2 = null;
		Mat matGray1 = null;
		Mat matGray2 = null;
		Mat matEdge1 = null;
		Mat matEdge2 = null;
		
		try {
		
			matResized1 = matImage1.clone();
			matResized2 = matImage2.clone();
					
			if(matImage1.width()>640)
			{
				matResized1 = resizeByWidth(matResized1, 640);
			}
			
			if(matImage2.width()!=matResized1.width())
			{
				matResized2 = resizeByWidth(matResized2, matResized1.width());
			}
			//String sOutputPath = new File("./test/images/output").getAbsolutePath();
			
			matGray1 = grayscale(matResized1, false);
			matGray2 = grayscale(matResized2, false);
			
			matGray1 = medianBlur(matGray1, 0.08);
			matGray2 = medianBlur(matGray2, 0.08);
			//saveImageAsFile(matGray1, sOutputPath+"/matGray1.jpg");
			//saveImageAsFile(matGray2, sOutputPath+"/matGray2.jpg");
			
			int iEdgeThreshold = 50;
			matEdge1 = cannyEdge(matGray1, iEdgeThreshold, false);
			matEdge2 = cannyEdge(matGray2, iEdgeThreshold, false);
	
			//saveImageAsFile(matEdge1, sOutputPath+"/matEdge1_"+iEdgeThreshold+".jpg");
			//saveImageAsFile(matEdge2, sOutputPath+"/matEdge2_"+iEdgeThreshold+".jpg");
	
			double dScore1 = Imgproc.matchShapes(
					matEdge1, matEdge2, iMode, 0);
			
			return 1-dScore1;
		
		}
		finally
		{
			if(matResized1!=null)
				matResized1.release();
			
			if(matResized2!=null)
				matResized2.release();
			
			if(matGray1!=null)
				matGray1.release();
			
			if(matGray2!=null)
				matGray2.release();
			
			if(matEdge1!=null)
				matEdge1.release();
			
			if(matEdge2!=null)
				matEdge2.release();
		}
		
	
	}
	
	public static Mat addAlphaChannel(Mat matInput)
	{
		if(matInput==null || matInput.empty() || matInput.channels()==4)
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
		if(matInput!=null && !matInput.empty() && matInput.channels()==4)
		{
			Mat matReturn = matInput.clone();
			Vector<Mat> vRgba = null;
			try {
				vRgba = new Vector<Mat>();
				Core.split(matReturn, vRgba);
				vRgba.remove(vRgba.size()-1);
				Core.merge(vRgba, matReturn);
			}
			finally
			{
				if(vRgba!=null && vRgba.size()>0)
				{
					for(Mat m : vRgba)
					{
						if(m!=null)
							m.release();
					}
				}
			}
			return matReturn;
		}
		return matInput;
	}
	
	
	public static Mat loadImage(String aImageURI) 
	{
		Mat mat = null;
				
		try {
			mat = Imgcodecs.imread(aImageURI, Imgcodecs.IMREAD_UNCHANGED);
		}catch(Exception ex)
		{
			mat = null;
			System.err.println("Failed to load "+aImageURI);
		}
		
		return mat;
	}
	
	public static void saveImageAsFile(Mat aMatInput, String aFileName)
	{
		Map<Integer, Integer> mapImageParams = new HashMap<Integer, Integer>();
		
		if(aFileName.toLowerCase().endsWith(".jpg"))
		{
			mapImageParams.put(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
		}
		
		saveImageAsFile(aMatInput, aFileName, mapImageParams);
	}
	
	public static void saveImageAsFile(Mat aMatInput, String aFileName, Map<Integer, Integer> mapImageParams)
	{
		MatOfInt matOfInt = null;
		try {
			matOfInt = new MatOfInt();
			
			if(mapImageParams!=null && mapImageParams.size()>0)
			{
				List<Integer> list = new ArrayList<Integer> ();
				Iterator<Integer> iter = mapImageParams.keySet().iterator();
				while(iter.hasNext())
				{
					Integer ParamName 	= iter.next();
					Integer ParamValue 	= mapImageParams.get(ParamName);
					
					if(ParamValue!=null)
					{
						list.add(ParamName);
						list.add(ParamValue);
					}
				}
				matOfInt.fromList(list);
			}
			
			Imgcodecs.imwrite(aFileName, aMatInput, matOfInt);
		}
		finally
		{
			if(matOfInt!=null)
				matOfInt.release();
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	
	@Deprecated
	public static Mat cannyEdge(Mat aMat, int aThreshold, boolean isinvert)
	{
		return OpenCvFilters.cannyEdge(aMat, aThreshold, isinvert);
	}
	
	@Deprecated
	public static Mat pixelate(Mat aMat, double aPixelateScale)
	{
		return OpenCvFilters.pixelate(aMat, aPixelateScale);
	}
	
	@Deprecated
	public static Mat solidfill(Mat aMat, Scalar aScalar)
	{
		return OpenCvFilters.solidfill(aMat, aScalar);
	}
	
	@Deprecated
	public static Mat medianBlur(Mat aMat, double aBlurScale)
	{
		return OpenCvFilters.medianBlur(aMat, aBlurScale);
	}
	
	@Deprecated
	public static Mat blur(Mat aMat, double aBlurScale)
	{
		return OpenCvFilters.blur(aMat, aBlurScale);
	}
	
	@Deprecated
	public static Mat gaussianBlur(Mat aMat, double aBlurScale)
	{
		return OpenCvFilters.gaussianBlur(aMat, aBlurScale);
	}
	
	@Deprecated
	public static Mat grayscale(Mat aMat)
	{
		return OpenCvFilters.grayscale(aMat, true);
	}
	
	@Deprecated 
	public static Mat grayscale(Mat aMat, boolean isConvertBackOrigType)
	{
		return OpenCvFilters.grayscale(aMat, isConvertBackOrigType);
	}
	
}
