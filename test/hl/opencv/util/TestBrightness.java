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

import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import hl.opencv.OpenCvLibLoader;

public class TestBrightness{
	
	private static void initOpenCV()
	{
		OpenCvLibLoader cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME,"/");
		cvLib.init();
	}
	
	private static void assessImage(File f)
	{
		if(f!=null && f.isFile())
		{
			String sFileName = f.getName().toLowerCase();
			
			if(sFileName.endsWith(".jpg") || sFileName.endsWith(".png"))
			{
				System.out.println(f.getName());
				Mat mat = OpenCvUtil.loadImage(f.getAbsolutePath());
				Mat matBlur = OpenCvFilters.blur(mat, 0.5);
				Mat matBrighter = OpenCvUtil.adjBrightness(mat, 1.0);
				
				
				System.out.println(" - Brightness:"+OpenCvUtil.calcBrightness(mat)+" vs "+OpenCvUtil.calcBrightness(matBrighter));
				System.out.println(" - Blurriness:"+OpenCvUtil.calcBlurriness(mat)+" vs "+OpenCvUtil.calcBlurriness(matBlur));
				System.out.println();
			}
		}
		
	}
	
	private static int processImages(File folderImages, boolean isRecursive)
	{
		int iCount = 0;
		for(File f : folderImages.listFiles())
		{
			if(f.isFile())
			{
				assessImage(f);
			}
			else if(isRecursive && f.isDirectory())
			{
				iCount += processImages(f, isRecursive);
			}
			
		}
		return iCount;
	}
	
	
	public static void main(String[] args) throws Exception
	{
		initOpenCV();
		System.out.println();
		
		File folderImages = new File("./test/images");
		
		boolean isRecursive = true;
		
		int iCount = processImages(folderImages, isRecursive);
		
		
		if(iCount<=0)
		{
			System.out.println("No files found in "+folderImages.getAbsolutePath());
		}
		
	}
	
}
