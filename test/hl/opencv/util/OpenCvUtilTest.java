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

import org.opencv.core.Core;
import org.opencv.core.Mat;

import hl.common.ImgUtil;
import hl.opencv.OpenCvLibLoader;

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
				
				////////////////
				System.out.println();
				
				lStart = System.currentTimeMillis();
				img = ImgUtil.addAlpha(img);
				lElapsed1 = getElapsedMs(lStart);
				System.out.println("  ImgUtil.addAlpha() "+" elapsed:"+lElapsed1+"ms");
				
				
				lStart = System.currentTimeMillis();
				mat = OpenCvUtil.addAlphaChannel(mat);
				lElapsed2 = getElapsedMs(lStart);
				System.out.println("  OpenCvUtil.addAlphaChannel() "+" elapsed:"+lElapsed2+"ms");
				
				////////////////
				System.out.println();
				String sImgExt = "jpg";
				
				lStart = System.currentTimeMillis();
				String sBase64_1 = ImgUtil.imageToBase64(img,sImgExt);
				lElapsed1 = getElapsedMs(lStart);
				System.out.println("  ImgUtil.imageToBase64() "+sBase64_1.length()+" elapsed:"+lElapsed1+"ms");
				
				String sSaveFileName1 = "imageToBase64."+sImgExt;
				ImgUtil.saveAsFile(img, new File(fileImageOutput.getAbsolutePath()+"/"+sSaveFileName1));
				File fBase64_1 = new File(fileImageOutput.getAbsolutePath()+"/"+sSaveFileName1);
				System.out.println("  Saved "+sSaveFileName1+" - "+fBase64_1.length());
				
				System.out.println();
				lStart = System.currentTimeMillis();
				String sBase64_2 = OpenCvUtil.mat2base64Img(mat,sImgExt);
				lElapsed2 = getElapsedMs(lStart);
				System.out.println("  ImgUtil.mat2base64Img() "+sBase64_2.length()+" elapsed:"+lElapsed2+"ms");
				
				String sSaveFileName2 = "mat2base64Img."+sImgExt;
				OpenCvUtil.saveImageAsFile(mat, fileImageOutput.getAbsolutePath()+"/"+sSaveFileName2);
				File fBase64_2 = new File(fileImageOutput.getAbsolutePath()+"/"+sSaveFileName2);
				System.out.println("  Saved "+sSaveFileName2+" - "+fBase64_2.length());
				
				
				////////////////
				System.out.println();
				double iBlur = .5; 
				double iPixelate = .6; 
				
				lStart = System.currentTimeMillis();
				Mat matBlur1 = OpenCvUtil.blur(mat, iBlur);
				lElapsed1 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				Mat matBlur2 = OpenCvUtil.medianBlur(mat, iBlur);
				lElapsed2 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				Mat matBlur3 = OpenCvUtil.gaussianBlur(mat, iBlur);
				long lElapsed3 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				Mat matPixelate = OpenCvUtil.pixelate(mat, iPixelate);
				long lElapsed4 = getElapsedMs(lStart);
				
				lStart = System.currentTimeMillis();
				BufferedImage imgPixelate = ImgUtil.pixelize(img, (float)iPixelate);
				long lElapsed5 = getElapsedMs(lStart);
				
				File f = new File(fileImageOutput.getAbsolutePath()+"/blur."+sImgExt);
				OpenCvUtil.saveImageAsFile(matBlur1, f.getAbsolutePath());
				System.out.println("  Saved "+f.getName()+" - "+lElapsed1+" ms");
				
				f = new File(fileImageOutput.getAbsolutePath()+"/medianBlur."+sImgExt);
				OpenCvUtil.saveImageAsFile(matBlur2, f.getAbsolutePath());
				System.out.println("  Saved "+f.getName()+" - "+lElapsed2+" ms");
				
				f = new File(fileImageOutput.getAbsolutePath()+"/gaussianBlur."+sImgExt);
				OpenCvUtil.saveImageAsFile(matBlur3, f.getAbsolutePath());
				System.out.println("  Saved "+f.getName()+" - "+lElapsed3+" ms");
			
				f = new File(fileImageOutput.getAbsolutePath()+"/MatPixelate."+sImgExt);
				OpenCvUtil.saveImageAsFile(matPixelate, f.getAbsolutePath());
				System.out.println("  Saved "+f.getName()+" - "+lElapsed4+" ms");
				
				f = new File(fileImageOutput.getAbsolutePath()+"/ImgPixelate."+sImgExt);
				ImgUtil.saveAsFile(imgPixelate, f);
				System.out.println("  Saved "+f.getName()+" - "+lElapsed5+" ms");				
				mat.release();
				
			} catch (IOException e) {
				e.printStackTrace();
			}

			
		}

		
		
		getElapsedMs(lStart);
	}
	
}
