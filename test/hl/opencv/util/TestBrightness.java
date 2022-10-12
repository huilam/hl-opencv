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
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.OpenCvLibLoader;

public class TestBrightness{
	
	private static void initOpenCV()
	{
		OpenCvLibLoader cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME,"/");
		if(!cvLib.init())
		{
			throw new RuntimeException("OpenCv is NOT loaded !");
		}
	}
	
	private static boolean processImage(File f)
	{
		boolean isProcessed = false;
		if(f!=null && f.isFile())
		{
			String sFileName = f.getName().toLowerCase();
			
			if(sFileName.endsWith(".jpg") || sFileName.endsWith(".png"))
			{
				isProcessed = true;
				System.out.println(f.getName());
				Mat mat = OpenCvUtil.loadImage(f.getAbsolutePath());
				double dBrightness = OpenCvUtil.calcBrightness(mat);
				
				System.out.println(" - Brightness:"+dBrightness);
				System.out.println(" - Brightness (excl black):"+OpenCvUtil.calcBrightness(mat, true));
				
				Scalar scalarFrom = new Scalar( (0 *0.5) , (0.07 *255) , 20);
				Scalar scalarTo = new Scalar( (50 *0.5), (0.80 *255) , 255); 
				System.out.println(" - Brightness2:"+OpenCvUtil.calcBrightness(mat, scalarFrom, scalarTo));
				
				System.out.println();
			}
		}
		return isProcessed;
	}
	
	private static boolean processVideo(File f)
	{
		boolean isProcessed = false;
		if(f!=null && f.isFile())
		{
			String sFileName = f.getName().toLowerCase();
			
			if(sFileName.endsWith(".mp4") || sFileName.endsWith(".mkv"))
			{
				isProcessed = true;
				String sVidFileName = f.getAbsolutePath();
				
				System.out.println(sVidFileName);
				
				VideoCapture vid = null;
				
				try{
					Mat matFrame = new Mat();
					vid = new VideoCapture(sVidFileName);
					if(vid.isOpened())
					{
						double dFps = vid.get(Videoio.CAP_PROP_FPS);
						double dTotalFrames = vid.get(Videoio.CAP_PROP_FRAME_COUNT);

						System.out.println("Total Frames = "+dTotalFrames);
						System.out.println("FPS = "+dFps);
						
						double dDuration = dTotalFrames/dFps;
						
						System.out.println("Duration (secs) = "+dDuration);
						
						vid.read(matFrame);
						System.out.println("read WxH = "+matFrame.width()+"x"+matFrame.height());
						
					}
				}finally
				{
					vid.release();
				}
			}
		}
		return isProcessed;
	}
	
	private static int processFiles(File folderImages, boolean isRecursive)
	{
		int iCount = 0;
		for(File f : folderImages.listFiles())
		{
			if(f.isFile())
			{
				if(processImage(f))
				{
					//images
				}
				else
				{
					// try video
					processVideo(f);
				}
				
			}
			else if(isRecursive && f.isDirectory())
			{
				iCount += processFiles(f, isRecursive);
			}
			
		}
		return iCount;
	}
	
	
	public static void main(String[] args) throws Exception
	{
		initOpenCV();
		System.out.println();

		File folderImages = new File("./test/images/ace");
		
		boolean isRecursive = true;
		
		int iCount = processFiles(folderImages, isRecursive);
		
		if(iCount<=0)
		{
			System.out.println("No files found in "+folderImages.getAbsolutePath());
		}
		
	}
	
}
