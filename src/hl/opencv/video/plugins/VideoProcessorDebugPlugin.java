/*
 Copyright (c) 2023 onghuilam@gmail.com
 
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

package hl.opencv.video.plugins;

import org.opencv.core.Mat;

import hl.opencv.video.VideoDecoder;

public class VideoProcessorDebugPlugin implements IVideoProcessorPlugin {
	
	private int _DEBUG_FRAME_COUNT = 50;

	@Override
	public boolean processStarted(String aVideoFileName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth,
			int aResHeight, long aTotalSelectedFrames, double aFps, long aSelectedDurationMs) {
		
		System.out.print("[START] "+aVideoFileName+" from:"+aAdjSelFrameMsFrom+" to:"+aAdjSelFrameMsTo+" res:"+aResWidth+"x"+aResHeight);
		System.out.print(" fps:"+aFps+" duration:"+aSelectedDurationMs+"ms");
		System.out.println();
		
		return true;
	}

	@Override
	public Mat decodedVideoFrame(String aVideoFileName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage) {
		
		StringBuffer sbStatusMsg = new StringBuffer();
		sbStatusMsg.append("#").append(aCurFrameNo).append(" - ").append(matFrame.width()).append("x").append(matFrame.height());
		sbStatusMsg.append(" ").append(aCurFrameMs).append("ms ").append(toDurationStr(aCurFrameMs)).append(" ...");
		
		if(aCurFrameNo % _DEBUG_FRAME_COUNT ==0 || aCurFrameNo==1)
		{
			if(aProgressPercentage>=99.99)
			{
				sbStatusMsg.append(" live");
			}
			else
			{
				sbStatusMsg.append(" ").append(aProgressPercentage).append("%");
			}
			System.out.println(sbStatusMsg.toString());		
		}
		return matFrame;
	}

	@Override
	public Mat skippedVideoFrame(String aVideoFileName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage, String aReason, double aScore) {
		
		System.out.print("[SKIPPED] #"+aCurFrameNo+" - "+aCurFrameMs+"ms - "+aReason+":"+aReason+" ... "+aProgressPercentage+"%");
		System.out.println();
		
		return matFrame;
	}

	@Override
	public Mat processAborted(String aVideoFileName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage, String aReason) {
		
		System.out.print("[ABORTED] #"+aCurFrameNo+" - "+aCurFrameMs+"ms - "+aReason+":"+aReason+" ... "+aProgressPercentage+"%");
		System.out.println();
	
		return matFrame;
	}

	@Override
	public void processEnded(String aVideoFileName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo,
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs) {
		System.out.println();
		System.out.println("[COMPLETED] "+aVideoFileName);
		long lDurationMs = aAdjSelFrameMsTo - aAdjSelFrameMsFrom;
		System.out.println(" - Process From/To: "+toDurationStr(aAdjSelFrameMsFrom)+" / "+toDurationStr(aAdjSelFrameMsTo)+" ("+lDurationMs +" ms)");
		System.out.println(" - Total Elapsed : "+aElpasedMs+" ms");
		System.out.println(" - Total Processed : "+aTotalProcessed);
		System.out.println(" - Total Skipped : "+aTotalSkipped);
		
		double dMsPerFrame = 0;
		double dFps = 0;
		if(aTotalProcessed>0)
		{
			dFps = (((double) aTotalProcessed) / (((double)aElpasedMs) /1000.0));
			
			dMsPerFrame = ((double)aElpasedMs) / ((double)aTotalProcessed);
		}
		
		System.out.println(" - Processed FPS : "+dFps);
		System.out.println(" - Processing Time/Frame : "+dMsPerFrame+" ms");
	}

	@Override
	public boolean initPlugin(String aVideoSource) {
		_DEBUG_FRAME_COUNT = 10;
		return true;
	}

	@Override
	public void destroyPlugin(String aVideoSource) {
	}
	
	/////////////////////////////////
	
	private String toDurationStr(long aTimeMs)
	{
		return VideoDecoder.toDurationStr(aTimeMs);
	}
}
