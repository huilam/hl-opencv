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
import java.io.IOException;
import java.nio.ByteBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import hl.opencv.OpenCvLibLoader;

public class OpenCvUtil{
	
	private static Decoder base64Decoder = Base64.getDecoder();
	private static Encoder base64Encoder = Base64.getEncoder();
	
	private static int BRIGHTNESS_MAX_SAMPLING_WIDTH = 500;
	
	private static int ORB_MAX_KEYPOINTS = 100;
	private static ORB orb = null;
	
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
					
					if(vMat!=null)
					{
						for(Mat m : vMat)
						{
							m.release();
						}
					}
				}
			}
			
		}
        return mat;
	}
	
	public static BufferedImage mat2BufferedImage(Mat aMat){
		BufferedImage image = null;
		Mat mat = aMat.clone();
		try {
			
			int type = BufferedImage.TYPE_BYTE_GRAY;
			switch(mat.channels())
			{
				case 3 : type = BufferedImage.TYPE_3BYTE_BGR; break;
				case 4 : type = BufferedImage.TYPE_4BYTE_ABGR; 
					Vector<Mat> vMat = new Vector<>();
					try {
						Core.split(mat, vMat);
						Mat matAlpha = vMat.remove(vMat.size()-1);
						vMat.add(0,matAlpha);
						Core.merge(vMat, mat);
					}finally
					{
						if(vMat!=null)
						{
							for(Mat m : vMat)
							{
								m.release();
							}
						}
					}
					break;
			}
			int bufferSize = mat.channels()* mat.cols()* mat.rows();
			byte [] b = new byte[bufferSize];
			mat.get(0,0,b); // get all the pixels
			image = new BufferedImage(mat.cols(), mat.rows(), type);
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
		}finally
		{
			if(mat!=null)
				mat.release();
		}
		
		return image;
	}
	
	public static Mat bytes2Mat(byte[] aImageBytes)
	{
		MatOfByte matBytes = null;
		try{
			matBytes = new MatOfByte(aImageBytes);
			return Imgcodecs.imdecode(matBytes, Imgcodecs.IMREAD_UNCHANGED);
		}
		finally
		{
			if(matBytes!=null)
			{
				matBytes.release();
			}
		}
	}
	
	public static byte[] mat2Bytes(Mat aMatImage, String aImageFormat) throws IOException
	{
		MatOfByte matBytes = null;
		byte[] byteData = null;
		try{
			matBytes = new MatOfByte();
			
			if(aImageFormat!=null && !aImageFormat.startsWith("."))
				aImageFormat = "."+aImageFormat;
			
			if(Imgcodecs.imencode(aImageFormat, aMatImage, matBytes))
			{
				byteData = matBytes.toArray();
			}
		}
		finally
		{
			if(matBytes!=null)
			{
				matBytes.release();
			}
		}
		return byteData;
	}
	
	public static void resizeByWidth(Mat aMatImg, int aNewWidth)
	{
		resizeByWidth(aMatImg, aNewWidth, Imgproc.INTER_LINEAR);
	}
	
	public static void resizeByWidth(Mat aMatImg, int aNewWidth, int aMode)
	{
		double dImageW = (double)aMatImg.width();
		double dImageH = (double)aMatImg.height();
		
		double dScaleW 	= aNewWidth>0 ? ((double)aNewWidth) / dImageW : 1.0;
		int iNewHeight =(int)(dImageH * dScaleW);

		resize(aMatImg, aNewWidth, iNewHeight, false, aMode);
	}
	
	public static void resize(Mat aMatImg, int aNewWidth, int aNewHeight, boolean isMainAspectRatio)
	{
		resize(aMatImg, aNewWidth, aNewHeight, isMainAspectRatio, Imgproc.INTER_LINEAR);
	}
	
	public static void resize(Mat aMatImg, int aNewWidth, int aNewHeight, boolean isMainAspectRatio, int aMode)
	{
		if(aNewWidth>0 && aNewHeight>0)
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
			}
			Imgproc.resize(aMatImg, aMatImg, new Size(aNewWidth, aNewHeight), aMode);
		}
	}
	
	public static Mat toHSV(Mat aMat)
	{
		int iOrigChannel = aMat.channels();
		
		switch(iOrigChannel)
		{
			case 1 :  
				Imgproc.cvtColor(aMat, aMat, Imgproc.COLOR_GRAY2BGR);
				//let it continue to convert to HSV
			case 3 :  
			case 4 :  
				Imgproc.cvtColor(aMat, aMat, Imgproc.COLOR_BGR2HSV);
				break;
		}
		
		return aMat;
	}

	public static void addAlphaChannel(Mat matInput)
	{
		if(matInput==null || matInput.empty() || matInput.channels()==4)
		{
			return;
		}
		
		switch(matInput.channels())
		{
			case 1:
				Imgproc.cvtColor(matInput, matInput, Imgproc.COLOR_GRAY2BGRA);
				break;
			case 3:
				Imgproc.cvtColor(matInput, matInput, Imgproc.COLOR_BGR2BGRA);
				break;
		}
		
		return;
	}
	
	public static void removeAlphaChannel(Mat matInput)
	{
		if(matInput!=null && !matInput.empty() && matInput.channels()==4)
		{
			Vector<Mat> vRgba = null;
			try {
				vRgba = new Vector<Mat>();
				Core.split(matInput, vRgba);
				vRgba.remove(vRgba.size()-1);
				Core.merge(vRgba, matInput);
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
		}
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
	
	public static boolean saveImageAsFile(final Mat aMatInput, String aFileName)
	{
		Map<Integer, Integer> mapImageParams = new HashMap<Integer, Integer>();
		
		Mat matToSave = aMatInput.clone();
		try {
			if(aFileName!=null)
			{
				String sExt = aFileName.toLowerCase();
				if(sExt.endsWith(".jpg"))
				{
					mapImageParams.put(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
				}
				else if(sExt.endsWith(".png"))
				{
					addAlphaChannel(matToSave);
				}
			}
			
			return saveImageAsFile(matToSave, aFileName, mapImageParams);
		}
		finally
		{
			if(matToSave!=null)
				matToSave.release();
		}
	}
	
	public static boolean saveImageAsFile(final Mat aMatInput, String aFileName, Map<Integer, Integer> mapImageParams)
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
			
			return Imgcodecs.imwrite(aFileName, aMatInput, matOfInt);
		}
		finally
		{
			if(matOfInt!=null)
				matOfInt.release();
		}
	}
	
	public static OpenCvLibLoader initOpenCV()
	{
		return initOpenCV("/");
	}
	
	public static OpenCvLibLoader initOpenCV(String aCustomLibPath)
	{
		OpenCvLibLoader cvLib = null;
		
		if(aCustomLibPath==null||aCustomLibPath.trim().length()==0)
			aCustomLibPath = "/";
		
		if(aCustomLibPath.equals("/"))
		{
			cvLib = OpenCvLibLoader.getMasterInstance();
		}
		else
		{
			cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME, aCustomLibPath);
		}
		
		if(!cvLib.init())
		{
			StringBuffer sErr = new StringBuffer();
			sErr.append("OpenCv is NOT loaded ! libname:").append(Core.NATIVE_LIBRARY_NAME);
			sErr.append(", libpath:").append(aCustomLibPath);
			
			Exception e = cvLib.getInitException();
			if(e!=null)
			{
				sErr.append("\n    Exception:").append(e.getMessage());
			}
			throw new RuntimeException(sErr.toString());
		}
		
		return cvLib;
	}
	
	////////////////////////////////
	
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
		double dMean1 = calcBrightness(aMat1, true);
		double dMean2 = calcBrightness(aMat2, true);
		
		return dMean1-dMean2;
	}
	
	public static double calcBrightnessDiff(Mat aMat1, Mat aMat2)
	{
		return compareBrightnessDiff(aMat1, aMat2);
	}
	
	public static double calcBrightness(Mat aMat1)
	{
		return calcBrightness(aMat1, null, aMat1.width());
	}
	
	public static double calcBrightness(Mat aMat1, boolean isExclBlack)
	{
		Mat matMask = null;
		try {
			
			if(isExclBlack)
			{			
				matMask = new Mat(
						new Size(aMat1.width(), aMat1.height()), 
						CvType.CV_8UC1,
						Scalar.all(0));
				Imgproc.threshold(aMat1, matMask, 10, 255, Imgproc.THRESH_BINARY);
				matMask = OpenCvFilters.grayscale(matMask, false);
			}
			
			return calcBrightness(aMat1, matMask, aMat1.width());
		}
		finally
		{
			if(matMask!=null)
				matMask.release();
		}
		
	}
	
	public static double calcBrightness(Mat aMat1, Scalar aFromScalar, Scalar aToScalar)
	{
		double dBrightnessScore = 0;
		
		Mat matMask = getMask(aMat1, aFromScalar, aToScalar);
		try {
			dBrightnessScore = calcBrightness(aMat1, matMask, aMat1.width());
		}
		finally
		{
			if(matMask!=null)
				matMask.release();
		}
		
		return dBrightnessScore;
	}
	
	public static double calcBrightness(final Mat aMat1, final Mat aBgMat, int aSamplingWidth)
	{
		double dBrightnessScore = 0;

		if(aMat1==null)
			return 0;
		
		Mat mat1 		= aMat1.clone();
		Mat matBg 		= null;
		Mat matHSV1 	= null;
		Mat matMask1 	= null;
		
		try {
		
			if(aBgMat!=null)
			{
				matBg = aBgMat.clone();
			}
			
			if(aSamplingWidth>0 && mat1.width()>0)
			{
				if(aSamplingWidth>BRIGHTNESS_MAX_SAMPLING_WIDTH)
					aSamplingWidth = BRIGHTNESS_MAX_SAMPLING_WIDTH;

				OpenCvUtil.resizeByWidth(mat1, aSamplingWidth);	
			}
			
			Scalar scalar1 = null;
			
			if(matBg!=null && !matBg.empty())
			{
				if(matBg.width()!=mat1.width() && mat1.width()>0)
				{
					OpenCvUtil.resize(matBg, mat1.width(), mat1.height(), false);
				}
				
				if(matBg!=null && matBg.channels()==1)
				{
					matMask1 = matBg;
				}
				else
				{
					try {
						matMask1 = extractFGMask(mat1, matBg, 0.18);
					} catch (Exception e) {
						matMask1 = null;
						e.printStackTrace();
					}
				}
			}
			
			
			matHSV1 = OpenCvUtil.toHSV(mat1);
			
			if(matMask1!=null && matHSV1.width() == matMask1.width())
			{
				scalar1 = Core.mean(matHSV1, matMask1);
			}
			else
			{
				scalar1 = Core.mean(matHSV1);
			}
			
			if(scalar1!=null && scalar1.val.length>0)
			{
				//H=Hue S=Saturation V=Value (channel=HSV)
				double dVal1 = (scalar1.val)[2];
				dBrightnessScore = dVal1 / 255;
			}
		}
		finally
		{
			if(mat1!=null)
				mat1.release();
			
			if(matHSV1!=null)
				matHSV1.release();
			
			if(matMask1!=null)
				matMask1.release();
			
			if(matBg!=null)
				matBg.release();
		}
		
		return dBrightnessScore;
	}	
	
	////////////////////////////////
	

	public static Mat grabcutFG(final Mat aMatInput, Rect aRect, double aBlurThreshold)
	{
		return grabcut(aMatInput, aRect, aBlurThreshold, 1, true);
	}
	
	public static Mat grabcutFG(final Mat aMatInput, Rect aRect, double aBlurThreshold, int aIterCount)
	{
		return grabcut(aMatInput, aRect, aBlurThreshold, aIterCount, true);
	}
	
	public static Mat grabcutBG(final Mat aMatInput, Rect aRect, double aBlurThreshold)
	{
		return grabcut(aMatInput, aRect, aBlurThreshold, 1, false);
	}
	
	private static Mat grabcut(final Mat aMatInput, Rect aRect, double aBlurThreshold, int aIterCount, boolean isForeground)
	{
		Mat matOutMask 	= null;
		Mat matGrabcutOutput = null;
		
		Mat matTmpInput = aMatInput.clone();
		try {
			matOutMask 	= new Mat();
			Mat matFg 	= null;
			Mat matbg 	= null;
			try {
				matFg = new Mat();
				matbg = new Mat();
				/**
				if(matTmpInput.width()>720)
				{
					OpenCvUtil.resizeByWidth(matTmpInput, 720);
				}
				**/
				
				if(aBlurThreshold>0)
				{
					//aBlurThreshold = 0.0 - 1.0
					matTmpInput = OpenCvFilters.medianBlur(matTmpInput, aBlurThreshold);
				}
				
				if(aRect==null || (aRect.width==0 && aRect.height==0))
					aRect = new Rect(0, 0, matTmpInput.width()-1, matTmpInput.height()-1);
				
				switch(matTmpInput.channels())
				{
					case 1 : 
					case 2 : 
						OpenCvFilters.grayToMultiChannel(matTmpInput, 3);
						break;
					case 3 : 
						//Do nothing
						break;
					case 4 : 
						OpenCvUtil.removeAlphaChannel(matTmpInput);
							 break;
				}
				
				//Grabcut support CV_8UC3 only
				Imgproc.grabCut(matTmpInput, matOutMask, aRect, matbg, matFg, aIterCount, Imgproc.GC_INIT_WITH_RECT);
				
				
			} finally
			{
				if(matFg!=null)
					matFg.release();
				
				if(matbg!=null)
					matbg.release();
			}
			
			Mat matSegMask	= null;
			try {
				//2 = Background Mask
				//3 = Foreground Mask
				int iSegVal = isForeground ? 3:2;
				matSegMask = new Mat(1, 1, CvType.CV_8U, new Scalar(iSegVal));
				Core.compare(matOutMask, matSegMask, matOutMask, Core.CMP_EQ);
			
				if(matOutMask.size()!=aMatInput.size())
				{
					OpenCvUtil.resize(matOutMask, aMatInput.width(), aMatInput.height(), false);
				}
				
				matGrabcutOutput = new Mat(aMatInput.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
				aMatInput.copyTo(matGrabcutOutput, matOutMask);
				
			}
			finally
			{
				if(matSegMask!=null)
					matSegMask.release();
			}
		}
		finally
		{
			if(matOutMask!=null)
				matOutMask.release();
			
			if(matTmpInput!=null)
				matTmpInput.release();
		}
		
		return matGrabcutOutput;
	}
	
	
	////////////////////////////////
	
	public static Mat matchImageTemplate(Mat matImage, Mat matTempl)
	{
		Mat matResult = new Mat();
		Imgproc.matchTemplate(matImage, matTempl, matResult, 0);
		return matResult;
	}

	public static double calcImageSimilarity(Mat matImage1, Mat matImage2)
	{
		double similarity = 0.0;
		
		if(matImage1!=null && matImage2!=null && matImage1.cols()>0)
		{
			Mat matDesc1 = getImageSimilarityDescriptors(matImage1);
			Mat matDesc2 = getImageSimilarityDescriptors(matImage2);
			try {
				similarity = calcDescriptorSimilarity(matDesc1, matDesc2);
			}
			finally
			{
				if(matDesc1!=null)
					matDesc1.release();
				if(matDesc2!=null)
					matDesc2.release();
			}
		}
		return similarity;
			
	}
	
	private static Mat getImageSimilarityDescriptors(Mat aMatImage)
	{
		return getImageSimilarityDescriptors(aMatImage, 0);
	}
	
	public static Mat getImageSimilarityDescriptors(final Mat aMatImage, int aMaxWidth)
	{
		if(aMatImage==null)
		{
			return null;
		}
		
		Mat matImage = aMatImage.clone();
		Mat d1 = new Mat();
		MatOfKeyPoint kp1 = new MatOfKeyPoint();
		
		if(orb==null)
			orb = ORB.create(ORB_MAX_KEYPOINTS);
		
		try {
			
			if(aMaxWidth>0 && matImage.width()>aMaxWidth)
			{
				resizeByWidth(matImage, aMaxWidth);
			}
			
			orb.detect(matImage, kp1);
			orb.compute(matImage, kp1, d1);
		}
		finally
		{	
			if(kp1!=null)
				kp1.release();
			
			if(matImage!=null)
				matImage.release();
		}
		
		return d1;
	}
	
	public static double calcDescriptorSimilarity(Mat d1, Mat d2)
	{
		if(d1==null || d2==null)
			return -1;
		
	    double similarity = 0.0;

	    MatOfDMatch matchMatrix = null;
	    
	    try {
		    	
		    if (d1.cols() == d2.cols()) {
		    	
		    	matchMatrix = new MatOfDMatch();
		        DescriptorMatcher matcher = 
		        		DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		        matcher.match(d1, d2, matchMatrix);
		        DMatch[] matches = matchMatrix.toArray();
		        
		        for (DMatch m : matches)
		        {
		        	if(m.distance <= 50)
		        		similarity++;
		        }
		        
		        if(similarity>0)
			    	similarity = similarity / ORB_MAX_KEYPOINTS;
		    }
		    
	    }finally
	    {
	    	if(matchMatrix!=null)
	    		matchMatrix.release();
	    }
	    return similarity;		
	}	
	
	////////////////////////////////
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold) throws Exception
	{
		return OpenCvMask.extractFGMask(matInput, matBackground, aDiffThreshold);
	}
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize) throws Exception
	{
		return OpenCvMask.extractFGMask(matInput, matBackground, aDiffThreshold, aProcessWidth, minContourPixelSize);
	}
	
	public static Mat extractFGMask(Mat matInput, Mat matBackground, double aDiffThreshold,
			int aProcessWidth, int minContourPixelSize, boolean isGrayscale) throws Exception
	{
		return OpenCvMask.extractFGMask(matInput, matBackground, aDiffThreshold, aProcessWidth, minContourPixelSize, isGrayscale);
	}
	
	public static Mat getMask(final Mat aMat1, Scalar aFromScalar, Scalar aToScalar)
	{
		return OpenCvMask.getMask(aMat1, aFromScalar, aToScalar);
	}
	
	public static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea)
	{
		return OpenCvMask.removeMaskContourAreas(aMatMask, aMinContourArea, aMaxContourArea);
	}
	
	public static Mat removeMaskContourAreas(Mat aMatMask, double aMinContourArea, double aMaxContourArea,
			int iFindContourMode, int iFindContourMethod)
	{
		return OpenCvMask.removeMaskContourAreas(aMatMask, aMinContourArea, aMaxContourArea, iFindContourMode, iFindContourMethod);
	}
	
	public static Mat colorToMask(Mat aMat)
	{
		return OpenCvMask.colorToMask(aMat);
	}
	
	public static Mat colorToMask(Mat aMat, int aThreshold)
	{
		return OpenCvMask.colorToMask(aMat, aThreshold);
	}
	
	public static Rect calcMaskTrimRect(Mat aMat)
	{
		return OpenCvMask.calcMaskTrimRect(aMat);
	}
	
	public static void reduceMaskNoise(Mat aBinaryMask)
	{
		OpenCvMask.reduceMaskNoise(aBinaryMask);
	}
	
	public static void reduceMaskNoise(Mat aBinaryMask, int aMinNoiseSize)
	{
		OpenCvMask.reduceMaskNoise(aBinaryMask, aMinNoiseSize);
	}
	
	public static void main(String args[]) throws Exception
	{
		//OpenCvUtil.initOpenCV();
		//Core.getBuildInformation();
	}
}
