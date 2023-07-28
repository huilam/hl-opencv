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

import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import hl.opencv.video.VideoDecoder;
import hl.opencv.video.encoder.VideoEncoder;


public class VideoFileReEncodingPlugin implements IVideoProcessorPlugin {
	
	private static final String OUTPUT_VIDEO_ENCODER 	= "h264";
	private Size encodeResolution = new Size();
	private VideoEncoder videoEnc = null;

	@Override
	public boolean processStarted(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth,
			int aResHeight, long aTotalSelectedFrames, double aFps, long aSelectedDurationMs) {
		
		return true;
	}

	@Override
	public Mat decodedVideoFrame(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage) {
		
		videoEnc.encodeFrame(matFrame);

		return matFrame;
	}

	@Override
	public Mat skippedVideoFrame(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage, String aReason, double aScore) {
		
		System.out.print("[SKIPPED] #"+aCurFrameNo+" - "+aCurFrameMs+"ms - "+aReason+":"+aReason+" ... "+aProgressPercentage+"%");
		System.out.println();
		
		return matFrame;
	}

	@Override
	public Mat processAborted(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage, String aReason) {

		return matFrame;
	}

	@Override
	public Map<String, ?> processEnded(String aVideoSourceName, 
			long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo,
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs) {
		
		System.out.println();
		System.out.println("[COMPLETED] "+aVideoSourceName);
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
		return null;
	}

	@Override
	public boolean initPlugin(JSONObject aMetaJson) {
		
		String sVideoSource = aMetaJson.getString("SOURCE");
		
		videoEnc = new VideoEncoder(OUTPUT_VIDEO_ENCODER, 
				(int)encodeResolution.width, 
				(int)encodeResolution.height);
		
		return true;
	}

	@Override
	public void destroyPlugin(JSONObject aMetaJson)
	{ 
	}
	
	/////////////////////////////////
	/////////////////////////////////
	
	private String toDurationStr(long aTimeMs)
	{
		return VideoDecoder.toDurationStr(aTimeMs);
	}
}
