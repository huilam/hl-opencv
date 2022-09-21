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

public class TestFGMask {
	
	private static long getElapsedMs(long aStartTime)
	{
		return System.currentTimeMillis() - aStartTime;
	}
	
	public static void main(String[] args)
	{
		File fileImages = new File("./test/images/b01/input");
		File fileImageOutput = new File("./test/images/b01/output");
		
		String sBgFileName = new File("./test/images/b01/background.jpg").getAbsolutePath();
		
		fileImageOutput.mkdirs();
		
		OpenCvLibLoader cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME,"/");
		
		cvLib.init();
		
		long lStart 	= 0;
		long lElapsed2 	= 0;
		

		Mat matBg = OpenCvUtil.loadImage(sBgFileName);
		
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
				mat = OpenCvUtil.loadImage(fImg.getAbsolutePath());
				lElapsed2 = getElapsedMs(lStart);
				if(mat!=null)
				{
					System.out.println("Loaded mat "+fImg.getName()+" "+mat.width()+"x"+mat.height()+" elapsed:"+lElapsed2+"ms");
				}
				
				////////////////

				Mat matMask = 
					//OpenCvUtil.extractFGMask(mat, matBg, 0.18, mat.width(), 500);
					OpenCvUtil.extractFGMask(mat, matBg, 0.18);
				
				Mat matOutput = new Mat();
				Core.copyTo(mat, matOutput, matMask);
				
				if(matOutput!=null)
				{
					File f = new File(fileImageOutput.getAbsolutePath()+"/"+fImg.getName()+"_output.png");
					OpenCvUtil.saveImageAsFile(matOutput, f.getAbsolutePath());
					System.out.println(" - Saved "+f.getName());
					
					//System.out.println(" matOutput = "+matOutput.width()+"x"+matOutput.height());
				}
				
				
				

			} catch (Exception e) {
				e.printStackTrace();
			}

			
		}

		
		
		getElapsedMs(lStart);
	}
	
}
