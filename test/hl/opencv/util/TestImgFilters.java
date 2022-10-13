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
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;
import hl.opencv.OpenCvLibLoader;

public class TestImgFilters{

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
			String sOutputFolder = f.getParentFile().getAbsolutePath()+"/output";
			
			new File(sOutputFolder).mkdirs();
			
			String sFileName = f.getName().toLowerCase();
			
			if(sFileName.endsWith(".jpg") || sFileName.endsWith(".png"))
			{
				isProcessed = true;
				System.out.print("* Loading "+f.getName()+" ...");
				Mat matOrg = OpenCvUtil.loadImage(f.getAbsolutePath());
				System.out.println("  - "+matOrg.width()+"x"+matOrg.height());
			
				double dAdjs[] = new double[] {0};//, 10, 20, 50, -10, -20, -50};
				
				for(int i=0; i<dAdjs.length; i++)
				{
					Mat mat = matOrg;
					
					if(dAdjs[i]!=0)
					{
						mat = OpenCvUtil.adjBrightness(matOrg, dAdjs[i]);
					}
				
					Mat matPixelated = OpenCvFilters.pixelate(mat, 0.6);
					
					String sOutputFileName = sOutputFolder+"/"+f.getName()+"_pixelated.jpg";
					
					OpenCvUtil.saveImageAsFile(matPixelated, sOutputFileName);
					System.out.println();
					System.out.println("  - saved "+sOutputFileName);
					
					
				}
				
				
				
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
				if(processVideo(f))
				{
					iCount ++; 
				}
				else if(processImage(f))
				{
					iCount ++; 
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
		
		File folderImages = new File("./test/images/tuii");
		
		boolean isRecursive = true;
		
		int iCount = processFiles(folderImages, isRecursive);
		
		if(iCount<=0)
		{
			System.out.println("No files found in "+folderImages.getAbsolutePath());
		}
		
	}
	
}
