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

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class TestEdge{
	
	private static long getElapsedMs(long aStartTime)
	{
		return System.currentTimeMillis() - aStartTime;
	}
	
	
	public static void main(String[] args) throws InterruptedException
	{
		
		OpenCvUtil.initOpenCV();
		
		int iW = 200;
		int iH = 100;
		Mat mat = new Mat(iH,iW,CvType.CV_16FC4);
		
		System.out.println("iW*iH="+iW*iH);
		System.out.println("mat.size().area()="+mat.size().area());
		
		/**
		File fileImages = 
				//new File("./test/images/nini/Scoliosis");
				new File("./test/images/weijian");
		File fileImageOutput = new File(fileImages.getAbsoluteFile()+"/output");
		fileImageOutput.mkdirs();
		
		long lStart 	= 0;
		
		System.out.println("Processing "+fileImages.getAbsolutePath()+" "+fileImages.listFiles().length+" files ...");
		
		long iSeq = 0;
		for(File fImg : fileImages.listFiles())
		{
			iSeq++;
			
			System.out.print("  "+iSeq+". "+fImg.getName());
			
			if(!fImg.isFile())
			{
				System.out.println(" skipped.");
				continue;
			}
			
			String sFileName = fImg.getName().toLowerCase();
			if(!(sFileName.endsWith(".jpg") || sFileName.endsWith(".png")))
			{
				//NOT image file
				continue;
			}
			
			System.out.println("");
			
			
			Mat matOrig = null;
			File f = null;
			Mat matGrabcutOutput = null;
			Mat matCannyEdgeOutput = null;
			Mat matPixelateOutput = null;
			try {
				lStart = System.currentTimeMillis();
				matOrig = OpenCvUtil.loadImage(fImg.getAbsolutePath());
				if(matOrig!=null)
				{
					System.out.println("      - Loaded mat "+fImg.getName()+" "+matOrig.width()+"x"+matOrig.height()+" elapsed:"+getElapsedMs(lStart)+"ms");
				}
				
				////////////////
				String sImgExt = "jpg";
				
				Rect rect = new Rect();
				lStart = System.currentTimeMillis();
				matGrabcutOutput = OpenCvUtil.grabcutFG(matOrig, rect, 0.0);
				System.out.println("      - matGrabcutOutput - "+getElapsedMs(lStart)+" ms");
				f = new File(fileImageOutput.getAbsolutePath()+"/"+fImg.getName()+"_Grabcut."+sImgExt);
				OpenCvUtil.saveImageAsFile(matGrabcutOutput, f.getAbsolutePath());
				System.out.println("      - Saved "+f.getAbsolutePath());
				
				
				for(int iThreshold = 90; iThreshold<=90; iThreshold+=10)
				{ 
					matCannyEdgeOutput = matOrig.clone();
					OpenCvFilters.cannyEdge(matCannyEdgeOutput, iThreshold, 3, false);
					
					System.out.println("      - CannyEdgeOutput - "+getElapsedMs(lStart)+" ms");
					f = new File(fileImageOutput.getAbsolutePath()+"/"+fImg.getName()+"_CannyEdge_"+iThreshold+"."+sImgExt);
					OpenCvUtil.saveImageAsFile(matCannyEdgeOutput, f.getAbsolutePath());
					System.out.println("      - Saved "+f.getAbsolutePath());
					
					
					//
					matPixelateOutput = matOrig.clone();
					OpenCvFilters.pixelate(matPixelateOutput, iThreshold);
					
					System.out.println("      - PixelateOutput - "+getElapsedMs(lStart)+" ms");
					f = new File(fileImageOutput.getAbsolutePath()+"/"+fImg.getName()+"_Pixelate_"+iThreshold+"."+sImgExt);
					OpenCvUtil.saveImageAsFile(matPixelateOutput, f.getAbsolutePath());
					System.out.println("      - Saved "+f.getAbsolutePath());
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally
			{
				if(matGrabcutOutput!=null)
					matGrabcutOutput.release();
				
				if(matCannyEdgeOutput!=null)
					matCannyEdgeOutput.release();
				
				if(matPixelateOutput!=null)
					matPixelateOutput.release();
				
				if(matOrig!=null) 
					matOrig.release();
			}

			
		}

		 **/
	}
	
}
