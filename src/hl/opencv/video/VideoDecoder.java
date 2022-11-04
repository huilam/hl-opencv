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

import hl.opencv.image.ImageProcessor;

public class VideoDecoder {
	
	private static long SECOND_MS = 1000;
	private static long MINUTE_MS = SECOND_MS * 60;
	private static long HOUR_MS = MINUTE_MS * 60;
	
	////
	private double min_brightness_score = 0.0;
	private Mat bgref_mat = null;
	
	////
	public double getMin_brightness_score() {
		return min_brightness_score;
	}
	public void setMin_brightness_score(double min_brightness_score) {
		this.min_brightness_score = min_brightness_score;
	}
	////
	public Mat getBgref_mat() {
		return bgref_mat;
	}
	public void setBgref_mat(Mat bgref_mat) {
		this.bgref_mat = bgref_mat;
	}
	////
	
	public long processVideo(File aVideoFile, long aFrameTimestamp)
	{
		return processVideo(aVideoFile, aFrameTimestamp, aFrameTimestamp);
	}
	
	public long processVideo(File aVideoFile, long aFrameTimestampFrom, long aFrameTimestampTo)
	{
		VideoCapture vid = null;
		Mat matFrame = null;
		
		long lProcessed = 0;

		try{
			matFrame = new Mat();
			vid = new VideoCapture(aVideoFile.getAbsolutePath());
			if(vid.isOpened())
			{
				double dFps = vid.get(Videoio.CAP_PROP_FPS);
				double dTotalFrames = vid.get(Videoio.CAP_PROP_FRAME_COUNT);
				double dFrameMs = 1000.0 / dFps;
				
				decodedMetadata((long)dFps, (long)dTotalFrames);
				long lFrameTimestamp = 0;
				
				ImageProcessor imgProcessor = new ImageProcessor();
				imgProcessor.setMin_brightness_score(this.min_brightness_score);
				imgProcessor.setBackground_ref_mat(this.bgref_mat);
				
				while(vid.read(matFrame))
				{
					lProcessed++;
					matFrame = imgProcessor.processImage(matFrame);
					
					if(matFrame!=null)
					{
					
						lFrameTimestamp = (long)((lProcessed-1)*dFrameMs);
						
						if(lFrameTimestamp<aFrameTimestampFrom)
							continue;
						
						if(aFrameTimestampTo!=-1 && lFrameTimestamp>aFrameTimestampTo)
							break;
						
						if(lFrameTimestamp>=aFrameTimestampFrom)
						{
							if(lFrameTimestamp<=aFrameTimestampTo || aFrameTimestampTo==-1)
							{
								matFrame = decodedVideoFrame(matFrame, lProcessed, lFrameTimestamp);
							}
						}
						
						if(matFrame==null)
							break;
					}
				}
			}
		}finally
		{
			vid.release();
		}
		
		return lProcessed;
	}
	
	public long processVideo(File fileVideo)
	{
		return processVideo(fileVideo, 0, -1);
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
