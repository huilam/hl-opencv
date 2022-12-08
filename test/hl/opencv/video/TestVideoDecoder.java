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
import java.util.Map;

import org.opencv.core.Mat;

import hl.opencv.util.OpenCvUtil;

public class TestVideoDecoder extends VideoDecoder {
	
	public File fileOutput = null;
	
	@Override 
	public boolean processStarted(String aVideoFileName, 
			long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth, int aResHeight, 
			long aTotalEstSelectedFrames, double aFps, long aSelectedDurationMs)
	{
		System.out.println();
		System.out.println("[START] "+aVideoFileName);
		System.out.println(" - From :"+toDurationStr(aAdjSelFrameMsFrom)+" To :"+toDurationStr(aAdjSelFrameMsTo));
		System.out.println(" - Resolution : "+aResWidth+"x"+aResHeight);
		System.out.println(" - FPS : "+aFps);
		System.out.println(" - Duration : "+ toDurationStr(aSelectedDurationMs));
		System.out.println(" - Est. TotalFrames : "+ aTotalEstSelectedFrames);
		System.out.println();
		return true;
	}
	
	@Override 
	public Mat decodedVideoFrame(String aVideoFileName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs, double aProgressPercentage)
	{
		System.out.print("#"+aCurFrameNo+" - "+matFrame.width()+"x"+matFrame.height()+" "+aCurFrameMs+"ms "+toDurationStr(aCurFrameMs)+" ... "+aProgressPercentage+"%");
				
		System.out.println();
		return matFrame;
	}
	
	@Override 
	public Mat skippedVideoFrame(String aVideoFileName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs, double aProgressPercentage, String aReason, double aScore)
	{
		System.out.print("[SKIPPED] #"+aCurFrameNo+" - "+aCurFrameMs+"ms - "+aReason+":"+aScore+" ... "+aProgressPercentage+"%");
		System.out.println();
		
		if(fileOutput!=null)
		{
			String sOutputPath = fileOutput.getAbsolutePath()+"/skipped/"+aReason+"/";
			new File(sOutputPath).mkdirs();
			OpenCvUtil.saveImageAsFile(matFrame, sOutputPath+aVideoFileName+"_"+aCurFrameNo+"_skipped.jpg");
		}
		
		return matFrame;
	}
	
	@Override 
	public Mat processAborted(String aVideoFileName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs,  double aProgressPercentage, String aReason)
	{
		return matFrame;
	}
	
	@Override 
	public void processEnded(String aVideoFileName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, 
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs)
	{
		System.out.println();
		System.out.println("[COMPLETED] "+aVideoFileName);
		double dMsPerFrame = ((double)aElpasedMs) / ((double)aTotalProcessed);
		long lDurationMs = aAdjSelFrameMsTo - aAdjSelFrameMsFrom;
		System.out.println(" - Process From/To: "+toDurationStr(aAdjSelFrameMsFrom)+" / "+toDurationStr(aAdjSelFrameMsTo)+" ("+lDurationMs +" ms)");
		System.out.println(" - Total Elapsed/Processed : "+aElpasedMs+" ms / "+aTotalProcessed+" = "+dMsPerFrame+" ms");
		System.out.println(" - Total Skipped : "+aTotalSkipped);
	}
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		//System.out.println(vidDecoder.listCameras());
		
		//File file = new File("./test/videos/crl/trimmed_10sec.mp4");
		File file = new File("./test/videos/bdd100k/cc3f1794-f4868199.mp4");
		//File file = new File("./test/videos/nls/Sunn.mp4");
		TestVideoDecoder vidDecoder = new TestVideoDecoder();
		vidDecoder.fileOutput = new File(file.getParentFile().getAbsolutePath()+"/output");
		//
		//
		vidDecoder.setBgref_mat(null);
		
		//
		File fileROIMask = new File(file.getParentFile().getAbsolutePath()+"/mask-test.jpg");
		Mat matROImask = OpenCvUtil.loadImage(fileROIMask.getAbsolutePath());
		vidDecoder.setROI_mat(matROImask);
		
		//vidDecoder.setCrop_ROI_rect(new org.opencv.core.Rect(100,100,10,10));
		//
		vidDecoder.setMin_brightness_skip_threshold(0);
		vidDecoder.setMax_brightness_calc_width(200);
		//
		vidDecoder.setMin_similarity_skip_threshold(0);
		vidDecoder.setMax_similarity_compare_width(500);
		//
		//vidDecoder.processVideoFile(file, 0, -1);
		
		Map<Long, Mat> mapFrames = vidDecoder.getFramesByIndex(file, new long[]{0,99,5000,2000});
		
		for(Long lTimestamp : mapFrames.keySet())
		{
			Mat matFrame = mapFrames.get(lTimestamp);
			System.out.println(lTimestamp+" : "+matFrame.width()+"x"+matFrame.height());
		}
		
	}
		
}
