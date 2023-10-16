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
import java.io.File;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import hl.common.ImgUtil;

public class OpenCvUtilTest{
	
	private static long getElapsedMs(long aStartTime)
	{
		return System.currentTimeMillis() - aStartTime;
	}
	
	public static void main(String[] args)
	{
		File fileImages = new File("./test/images");
		File fileImageOutput = new File("./test/images/output");
		fileImageOutput.mkdirs();
		
		OpenCvUtil.initOpenCV();
		
		long lStart 	= 0;
		long lElapsed1 	= 0;
		long lElapsed2 	= 0;
		
		for(File fImg : fileImages.listFiles())
		{
			if(!fImg.isFile())
				continue;
			
			String sFileName = fImg.getName().toLowerCase();
			if(!(sFileName.endsWith(".jpg") || sFileName.endsWith(".png")))
			{
				//NOT image file
				continue;
			}
			
			BufferedImage img = null;
			Mat mat = null;
			

			boolean saved = false;
			
			try {
				lStart = System.currentTimeMillis();
				img = ImgUtil.loadImage(fImg.getAbsolutePath());
				lElapsed1 = getElapsedMs(lStart);
				if(img!=null)
				{
					System.out.println(" Loaded bufferedImg "+fImg.getName()+" "+img.getWidth()+"x"+img.getHeight()+" elapsed:"+lElapsed1+"ms");
				}
				
				lStart = System.currentTimeMillis();
				mat = OpenCvUtil.loadImage(fImg.getAbsolutePath());
				lElapsed2 = getElapsedMs(lStart);
				if(mat!=null)
				{
					System.out.println(" Loaded mat "+fImg.getName()+" "+mat.width()+"x"+mat.height()+" elapsed:"+lElapsed2+"ms");
				}
				
				///////////////
				
				System.out.println("  mat.channels = "+mat.channels());
				
				double dBrightness1 = OpenCvUtil.calcBrightness(mat);
				System.out.println("  calcBrightness-1 = "+dBrightness1);
				
				double dBrightness2 = OpenCvUtil.calcBrightness(mat, true);
				System.out.println("  calcBrightness-2 = "+dBrightness2);
		
				double dBrightness3 = OpenCvUtil.calcBrightness(mat, 
						new Scalar(0.0, 17.85, 20.0, 0.0), new Scalar(25.0, 204.0, 255.0, 0.0));				
				System.out.println("  calcBrightness-3 = "+dBrightness3);
				
				
				////////////////
				System.out.println();
				
				lStart = System.currentTimeMillis();
				img = ImgUtil.addAlpha(img);
				lElapsed1 = getElapsedMs(lStart);
				System.out.println("  ImgUtil.addAlpha() "+" elapsed:"+lElapsed1+"ms");
				
				
				lStart = System.currentTimeMillis();
				OpenCvUtil.addAlphaChannel(mat);
				lElapsed2 = getElapsedMs(lStart);
				System.out.println("  OpenCvUtil.addAlphaChannel() "+" elapsed:"+lElapsed2+"ms");
				
				////////////////
				System.out.println();
				String sImgExt = "png";
				
				lStart = System.currentTimeMillis();
				String sBase64_1 = ImgUtil.imageToBase64(img,sImgExt);
				lElapsed1 = getElapsedMs(lStart);
				System.out.println("  ImgUtil.imageToBase64() "+sBase64_1.length()+" elapsed:"+lElapsed1+"ms");
				
				String sSaveFileName1 = "imageToBase64."+sImgExt;
				System.out.print("Saving "+sSaveFileName1+" ... ");
				saved = ImgUtil.saveAsFile(img, new File(fileImageOutput.getAbsolutePath()+"/"+sSaveFileName1));
				File fBase64_1 = new File(fileImageOutput.getAbsolutePath()+"/"+sSaveFileName1);
				System.out.println(saved+" - "+fBase64_1.length());
				
				System.out.println();
				lStart = System.currentTimeMillis();
				String sBase64_2 = OpenCvUtil.mat2base64Img(mat,sImgExt);
				lElapsed2 = getElapsedMs(lStart);
				System.out.println("  ImgUtil.mat2base64Img() "+sBase64_2.length()+" elapsed:"+lElapsed2+"ms");
				
				String sSaveFileName2 = "mat2base64Img."+sImgExt;
				System.out.print("  Saving "+sSaveFileName2+" ... ");
				saved = OpenCvUtil.saveImageAsFile(mat, fileImageOutput.getAbsolutePath()+"/"+sSaveFileName2);
				File fBase64_2 = new File(fileImageOutput.getAbsolutePath()+"/"+sSaveFileName2);
				System.out.println(saved+" - "+fBase64_2.length());

				
				
				////////////////
				System.out.println();
				double iBlur = .5; 
				double iPixelate = .6; 
				
				lStart = System.currentTimeMillis();
				Mat matBlur1 = mat.clone();
				OpenCvFilters.blur(matBlur1, iBlur);
				lElapsed1 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				Mat matBlur2 = mat.clone();
				OpenCvFilters.medianBlur(matBlur2, iBlur);
				lElapsed2 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				Mat matBlur3 = mat.clone();
				OpenCvFilters.gaussianBlur(matBlur3, iBlur);
				long lElapsed3 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				Mat matPixelate = mat.clone();
				OpenCvFilters.pixelate(matPixelate, iPixelate);
				long lElapsed4 = getElapsedMs(lStart);
				
				File f = new File(fileImageOutput.getAbsolutePath()+"/blur."+sImgExt);
				System.out.print("  Saving "+f.getName()+" ... ");
				saved = OpenCvUtil.saveImageAsFile(matBlur1, f.getAbsolutePath());
				System.out.println(saved+"  - "+lElapsed1+" ms");
				
				f = new File(fileImageOutput.getAbsolutePath()+"/medianBlur."+sImgExt);
				System.out.print("  Saving "+f.getName()+" ... ");
				OpenCvUtil.saveImageAsFile(matBlur2, f.getAbsolutePath());
				System.out.println(saved+"  - "+lElapsed2+" ms");
				
				f = new File(fileImageOutput.getAbsolutePath()+"/gaussianBlur."+sImgExt);
				System.out.print("  Saving "+f.getName()+" ... ");
				OpenCvUtil.saveImageAsFile(matBlur3, f.getAbsolutePath());
				System.out.println(saved+"  - "+lElapsed3+" ms");
			
				f = new File(fileImageOutput.getAbsolutePath()+"/MatPixelate."+sImgExt);
				System.out.print("  Saving "+f.getName()+" ... ");
				OpenCvUtil.saveImageAsFile(matPixelate, f.getAbsolutePath());
				System.out.println(saved+"  - "+lElapsed4+" ms");
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}

			
		}

		
		
		getElapsedMs(lStart);
	}
	
}
