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

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.image.ImageProcessor;
import hl.opencv.util.OpenCvUtil;

public class VideoDecoder {
	
	private static long SECOND_MS = 1000;
	private static long MINUTE_MS = SECOND_MS * 60;
	private static long HOUR_MS = MINUTE_MS * 60;
	
	private static String EVENT_BRIGHTNESS 		= "BRIGHTNESS";
	private static String EVENT_SEGMENTATION 	= "SEGMENTATION";
	private static String EVENT_SIMILARITY 		= "SIMILARITY";
	
	private static String EVENT_NULLFRAME 		= "NULL_FRAME";
	
	////
	private double min_similarity_skip_threshold = 0.0;
	private double min_brightness_skip_threshold = 0.0;
	private int max_brightness_calc_width	 	 = 0;
	private int max_similarity_compare_width	 = 0;
	private Mat bgref_mat = null;
	
	////
	public double getMin_similarity_skip_threshold() {
		return min_similarity_skip_threshold;
	}
	public void setMin_similarity_skip_threshold(double min_similarity_score) {
		this.min_similarity_skip_threshold = min_similarity_score;
	}
	////
	public double getMin_brightness_skip_threshold() {
		return min_brightness_skip_threshold;
	}
	public void setMin_brightness_skip_threshold(double min_brightness_score) {
		this.min_brightness_skip_threshold = min_brightness_score;
	}
	////
	public int getMax_brightness_calc_width() {
		return max_brightness_calc_width;
	}
	public void setMax_brightness_calc_width(int max_brightness_calc_width) {
		this.max_brightness_calc_width = max_brightness_calc_width;
	}	////
	public Mat getBgref_mat() {
		return bgref_mat;
	}
	public void setBgref_mat(Mat bgref_mat) {
		this.bgref_mat = bgref_mat;
	}
	////
	public int getMax_similarity_compare_width() {
		return max_similarity_compare_width;
	}
	public void setMax_similarity_compare_width(int max_similarity_compare_width) {
		this.max_similarity_compare_width = max_similarity_compare_width;
	}
	////
	
	public JSONObject getVideoMetadata(File aVideoFile)
	{
		JSONObject jsonMeta = new JSONObject();
		
		VideoCapture vid = null;
		try {
			vid = new VideoCapture(aVideoFile.getAbsolutePath());
			if(vid.isOpened())
			{
				jsonMeta.put("CAP_PROP_FPS", vid.get(Videoio.CAP_PROP_FPS));
				jsonMeta.put("CAP_PROP_BITRATE", vid.get(Videoio.CAP_PROP_BITRATE));
				//
				jsonMeta.put("CAP_PROP_FRAME_COUNT", vid.get(Videoio.CAP_PROP_FRAME_COUNT));
				jsonMeta.put("CAP_PROP_FRAME_WIDTH", vid.get(Videoio.CAP_PROP_FRAME_WIDTH));
				jsonMeta.put("CAP_PROP_FRAME_HEIGHT", vid.get(Videoio.CAP_PROP_FRAME_HEIGHT));
				//
			}
		}
		finally
		{
			if(vid!=null)
				vid.release();
		}
		return jsonMeta;
	}
	
	public long processVideo(File aVideoFile, long aFrameTimestamp)
	{
		return processVideo(aVideoFile, aFrameTimestamp, -1);
	}
	
	public long processVideo(File aVideoFile, long aFrameTimestampFrom, long aFrameTimestampTo)
	{
		VideoCapture vid = null;
		Mat matFrame = null;
		
		Mat matPrevDescriptors = null;
		Mat matCurDescriptors = null;
		
		long lProcessed = 0;
		long lSkipped 	= 0;

		try{
			matFrame = new Mat();
			vid = new VideoCapture(aVideoFile.getAbsolutePath());
			if(vid.isOpened())
			{
				double dFps = vid.get(Videoio.CAP_PROP_FPS);
				double dTotalFrames = vid.get(Videoio.CAP_PROP_FRAME_COUNT);
				double dFrameMs = 1000.0 / dFps;
				
				int iWidth 	= (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
				int iHeight = (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				
				boolean isProcessVideo = processStarted(
						aVideoFile.getName(), iWidth, iHeight, 
						(int)dFps, (long)dTotalFrames);
				
				if(!isProcessVideo)
					return 0;
				
				ImageProcessor imgProcessor = new ImageProcessor();
				imgProcessor.setBackground_ref_mat(this.bgref_mat);
				
				long lElapseStartMs = System.currentTimeMillis();
				
				long lFrameTimestamp = aFrameTimestampFrom;
				vid.set(Videoio.CAP_PROP_POS_MSEC, aFrameTimestampFrom);
				
				while(vid.read(matFrame))
				{
					if(aFrameTimestampTo!=-1 && lFrameTimestamp>aFrameTimestampTo)
						break;
					
					lProcessed++;
					
					double dBrightness = OpenCvUtil.calcBrightness(matFrame, null, this.max_brightness_calc_width);
					if(dBrightness<this.min_brightness_skip_threshold)
					{
						skippedVideoFrame(matFrame, lProcessed, lFrameTimestamp, EVENT_BRIGHTNESS, dBrightness);
						lSkipped++;
						continue;
					}
					
					boolean isOk = imgProcessor.processImage(matFrame);
					if(!isOk)
					{
						skippedVideoFrame(matFrame, lProcessed, lFrameTimestamp, EVENT_SEGMENTATION, 0);
						lSkipped++;
						continue;
					}
					
					if(matFrame!=null)
					{
						if(this.min_similarity_skip_threshold>0)
						{
							matCurDescriptors = OpenCvUtil.getImageSimilarityDescriptors(
									matFrame, this.max_similarity_compare_width);
							
							if(matPrevDescriptors!=null)
							{
								double dSimilarityScore = OpenCvUtil.calcDescriptorSimilarity(
										matCurDescriptors, 
										matPrevDescriptors);

								if(dSimilarityScore>=this.min_similarity_skip_threshold)
								{	
									skippedVideoFrame(matFrame, lProcessed, lFrameTimestamp, EVENT_SIMILARITY, dSimilarityScore);
									lSkipped++;
									continue;
								}
							}
							
							matPrevDescriptors = matCurDescriptors;
		
						}
						
						if(lFrameTimestamp>=aFrameTimestampFrom)
						{
							if(lFrameTimestamp<=aFrameTimestampTo || aFrameTimestampTo==-1)
							{
								matFrame = decodedVideoFrame(matFrame, lProcessed, lFrameTimestamp);
							}
						}
					}
					
					if(matFrame==null)
					{
						skippedVideoFrame(matFrame, lProcessed, lFrameTimestamp, EVENT_NULLFRAME, 0);
						lSkipped++;
						break;
					}
					
					lFrameTimestamp += dFrameMs;
				}
				
				long lElapsedMs = System.currentTimeMillis() - lElapseStartMs;
				processEnded(aVideoFile.getName(), aFrameTimestampFrom, aFrameTimestampTo, lProcessed, lProcessed-lSkipped, lElapsedMs);
			}
		}finally
		{
			if(vid!=null)
				vid.release();
			
			if(matCurDescriptors!=null)
				matCurDescriptors.release();
			
			if(matPrevDescriptors!=null)
				matPrevDescriptors.release();
		}
		
		return lProcessed;
	}
	
	public long processVideo(File fileVideo)
	{
		return processVideo(fileVideo, 0, -1);
	}
	
	///// 
	public boolean processStarted(String aVideoFileName, int aResWidth, int aResHeight, int aFps, long aTotalFrameCount)
	{
		return true;
	}
	
	public void processEnded(String aVideoFileName, long aFromTimeMs, long aToTimeMs, long aTotalFrames, long aTotalProcessed, long aElpasedMs)
	{
	}
	
	public Mat skippedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp, String aReasonCode, double aScore)
	{
		return matFrame;
	}
	
	public Mat decodedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp)
	{
		return matFrame;
	}
	///// 
	
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
	
}
