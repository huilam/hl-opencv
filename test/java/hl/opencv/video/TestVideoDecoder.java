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

import org.json.JSONObject;
import org.opencv.core.Mat;
import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.decoder.VideoFileDecoder;

public class TestVideoDecoder extends VideoFileDecoder {
	
	public TestVideoDecoder(File aVideoFile) {
		super(aVideoFile);
	}


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
		
		if(aProgressPercentage%5==0 || aCurFrameNo==1)
		{
			System.out.print("#"+aCurFrameNo+" - "+matFrame.width()+"x"+matFrame.height()+" "+aCurFrameMs+"ms "+toDurationStr(aCurFrameMs)+" ... "+aProgressPercentage+"%");		
			System.out.println();
		}
		
		matFrame.release();
		
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
		
		System.out.println(" - Processed FPS : "+(1000/dMsPerFrame));

	}
	
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		File fileVid = 
				new File("./test/videos/crl/trimmed_10sec.mp4");
				//new File("./test/videos/youtube/SG_REQ_NOMASK.mp4");
		
		System.out.println(fileVid.getName()+ " = "+fileVid.exists());
		
		TestVideoDecoder test = new TestVideoDecoder(fileVid);
		
		JSONObject jsonMeta = test.getVideoFileMetadata();
		//System.out.println(jsonMeta);
		
		long lTotalFrameCount = jsonMeta.optLong("FRAME_COUNT",-1);
		System.out.println("FRAME_COUNT="+lTotalFrameCount);

		long lDurationMs = jsonMeta.optLong("EST_DURATION_MS",-1);
		System.out.println("EST_DURATION_MS="+lDurationMs);
		
		long lLastFrameIdx = jsonMeta.optLong("LAST_FRAME_IDX",-1);
		System.out.println("LAST_FRAME_IDX="+lLastFrameIdx);
		
		long lLastFrameMs = jsonMeta.optLong("LAST_FRAME_MS",-1);
		System.out.println("LAST_FRAME_MS="+lLastFrameMs);
		
		Map<Long, Mat> mapFramesByMs = test.getFramesByTimestamp(new long[] {0, lLastFrameMs});
		Map<Long, Mat> mapFramesByIdx = test.getFramesByIndex(new long[] {0, 1, lTotalFrameCount-1, lTotalFrameCount});
		
		
		System.out.println("mapFramesByMs.size()="+mapFramesByMs.size()+" vs 2");
		System.out.println("mapFramesByIdx.size()="+mapFramesByIdx.size()+" vs 3");
		
		//test.processVideoFile(fileVid);
		
		/**
		long lFreeMemory1 = Runtime.getRuntime().freeMemory();
		
		TestVideoDecoder test = new TestVideoDecoder();
		test.processVideoFile(fileVid);
		long lFreeMemory2 = Runtime.getRuntime().freeMemory();
		
		System.out.println("totalMemory="+Runtime.getRuntime().totalMemory());
		System.out.println("freeMemory (before) ="+lFreeMemory1);
		System.out.println("freeMemory (after) ="+lFreeMemory2);
		System.out.println("freeMemorry (after-before) ="+(lFreeMemory2-lFreeMemory1));
		**/
		
	}
}
