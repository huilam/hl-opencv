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
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class VideoDecoder {
	
	private static long SECOND_MS = 1000;
	private static long MINUTE_MS = SECOND_MS * 60;
	private static long HOUR_MS = MINUTE_MS * 60;
	
	public long processVideo(File fileVideo)
	{
		VideoCapture vid = null;
		long lProcessed = 0;
		try{
			Mat matFrame = new Mat();
			vid = new VideoCapture(fileVideo.getAbsolutePath());
			if(vid.isOpened())
			{
				double dFps = vid.get(Videoio.CAP_PROP_FPS);
				double dTotalFrames = vid.get(Videoio.CAP_PROP_FRAME_COUNT);
				double dFrameMs = 1000 / (int)dFps;
				
				decodedMetadata((long)dFps, (long)dTotalFrames);
				double dFrameCount = -1;
				while(vid.read(matFrame))
				{
					dFrameCount++;
					matFrame = decodedVideoFrame(matFrame, (long)dFrameCount+1, (long)(dFrameCount*dFrameMs));
					if(matFrame==null)
						break;
				}
			}
		}finally
		{
			vid.release();
		}
		
		return lProcessed;
	}
	
	public static String toDurationStr(long aTimeMs)
	{
		StringBuffer sbTimeMs = new StringBuffer();
		
		long lHour 	= 0;
		long lMin 	= 0;
		long lSec 	= 0;
		
		lHour = aTimeMs / HOUR_MS;
		aTimeMs = aTimeMs % HOUR_MS;
		
		lMin = aTimeMs / MINUTE_MS;
		aTimeMs = aTimeMs % MINUTE_MS;
		
		lSec = aTimeMs / SECOND_MS;
		aTimeMs = aTimeMs % SECOND_MS;
		
		if(lHour<10)
			sbTimeMs.append("0");
		sbTimeMs.append(lHour).append(":");
		
		if(lMin<10)
			sbTimeMs.append("0");
		sbTimeMs.append(lMin).append(":");
		
		if(lSec<10)
			sbTimeMs.append("0");
		sbTimeMs.append(lSec).append(".");
		
		if(aTimeMs<10)
			sbTimeMs.append("00");
		else if(aTimeMs<100)
			sbTimeMs.append("0");
		sbTimeMs.append(aTimeMs);
		
		return sbTimeMs.toString();
	}
	
	public void decodedMetadata(long aFps, long aTotalFrameCount)
	{
	}
	
	public Mat decodedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp)
	{
		return matFrame;
	}
	
}
