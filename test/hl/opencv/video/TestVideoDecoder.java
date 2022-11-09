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

package hl.opencv.video;

import java.io.File;
import org.opencv.core.Mat;
import hl.opencv.util.OpenCvUtil;

public class TestVideoDecoder extends VideoDecoder {
	
	public File fileOutput = null;
	
	@Override 
	public boolean processStarted(
			String aVideoFileName, int aResWidth, int aResHeight, 
			 long aSelectedFrameCount, double aFps, long aSelectedDurationMs)
	{
		System.out.println();
		System.out.println("[START] "+aVideoFileName);
		System.out.println(" - Resolution : "+aResWidth+"x"+aResHeight);
		System.out.println(" - FPS : "+aFps);
		System.out.println(" - Duration : "+ toDurationStr(aSelectedDurationMs));
		System.out.println(" - Est. TotalFrames : "+ aSelectedFrameCount);
		System.out.println();
		return true;
	}
	
	@Override 
	public Mat decodedVideoFrame(
			String aVideoFileName, Mat matFrame, long aFrameNo, long aFrameMs)
	{
		System.out.print("#"+aFrameNo+" - "+aFrameMs+"ms "+toDurationStr(aFrameMs));
				
		System.out.println();
		return matFrame;
	}
	
	@Override 
	public Mat skippedVideoFrame(
			String aVideoFileName, Mat matFrame, long aFrameNo, long aFrameMs, String aReasonCode, double aScore)
	{
		System.out.print("[SKIPPED] #"+aFrameNo+" - "+aFrameMs+"ms - "+aReasonCode+":"+aScore);
		System.out.println();
		
		if(fileOutput!=null)
		{
			String sOutputPath = fileOutput.getAbsolutePath()+"/skipped/"+aReasonCode+"/";
			new File(sOutputPath).mkdirs();
			OpenCvUtil.saveImageAsFile(matFrame, sOutputPath+aVideoFileName+"_"+aFrameNo+"_skipped.jpg");
		}
		
		return matFrame;
	}
	
	@Override 
	public void processEnded(String aVideoFileName, long aFromTimeMs, long aToTimeMs, 
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs)
	{
		System.out.println();
		System.out.println("[COMPLETED] "+aVideoFileName);
		double dMsPerFrame = ((double)aElpasedMs) / ((double)aTotalProcessed);
		long lDurationMs = aToTimeMs - aFromTimeMs;
		System.out.println(" - Process From/To: "+toDurationStr(aFromTimeMs)+" / "+toDurationStr(aToTimeMs)+" ("+lDurationMs +" ms)");
		System.out.println(" - Total Elapsed/Processed : "+aElpasedMs+" ms / "+aTotalProcessed+" = "+dMsPerFrame+" ms");
		System.out.println(" - Total Skipped : "+aTotalSkipped);
	}
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		
		File file = new File("./test/videos/bdd100k/cc3f1794-f4868199.mp4");
		TestVideoDecoder vidDecoder = new TestVideoDecoder();
		vidDecoder.fileOutput = null; //new File("./test/videos/bdd100k/output");
		//
		System.out.println(vidDecoder.getVideoMetadata(file));
		//
		vidDecoder.setBgref_mat(null);
		//
		vidDecoder.setMin_brightness_skip_threshold(0.15);
		vidDecoder.setMax_brightness_calc_width(200);
		//
		vidDecoder.setMin_similarity_skip_threshold(0.98);
		vidDecoder.setMax_similarity_compare_width(500);
		//
		vidDecoder.processVideo(file, 2000, 5000);
	}
		
}
