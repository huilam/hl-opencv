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

package hl.opencv.video.decoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.image.ImgROI;
import hl.opencv.image.ImgSegmentation;
import hl.opencv.util.OpenCvUtil;

public class VideoCaptureDecoder {
	
	private static Logger logger = Logger.getLogger(VideoCaptureDecoder.class.getName());
	
	protected static long SECOND_MS 	= 1000;
	protected static long MINUTE_MS 	= SECOND_MS * 60;
	protected static long HOUR_MS 		= MINUTE_MS * 60;
	
	protected static String EVENT_BRIGHTNESS 		= "BRIGHTNESS";
	protected static String EVENT_SIMILARITY 		= "SIMILARITY";
	protected static String EVENT_NULLFRAME 		= "NULL_FRAME";
	
	////
	private VideoCapture videocap 		= null;
	private String videocap_name		= null;
	private boolean is_auto_release  	= true;
	
	protected double min_similarity_skip_threshold = 0.0;
	protected double min_brightness_skip_threshold = 0.0;
	
	protected int max_brightness_calc_width	 	 = 0;
	protected int max_similarity_compare_width	 = 0;
	
	protected Mat mat_roi_mask 		= null;
	protected Rect rect_crop_roi 	= null;
	
	protected Mat mat_seg_bgref 	= null;
	
	protected VideoCaptureDecoder()
	{
	}
	
	public void setVideoCapture(VideoCapture aVideoCap)
	{
		this.videocap = aVideoCap;
		
		if(aVideoCap!=null)
		{
			if(getVideoCaptureName()==null)
			{
				setVideoCaptureName(aVideoCap.toString());
			}
		}
	}
	
	public VideoCapture getVideoCapture()
	{
		return this.videocap;
	}
	
	protected void setVideoCaptureName(String aVideoCapName)
	{
		this.videocap_name = aVideoCapName;
	}
	
	public String getVideoCaptureName()
	{
		return this.videocap_name;
	}
	
	public void setAutoRelease(boolean isAutoRelease)
	{
		this.is_auto_release = isAutoRelease;
	}
	
	public boolean getAutoRelease()
	{
		return this.is_auto_release;
	}
	
	public void release()
	{
		if(this.videocap!=null && is_auto_release)
		{
			this.videocap.release();
		}
	}
	
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
	public Mat getROI_mat() {
		return mat_roi_mask;
	}
	public void setROI_mat(Mat roi_mat) {
		this.mat_roi_mask = roi_mat;
	}
	////
	public Rect getCrop_ROI_rect() {
		return rect_crop_roi;
	}
	public void setCrop_ROI_rect(Rect crop_roi_rect) {
		this.rect_crop_roi = crop_roi_rect;
	}
	////
	public Mat getBgref_mat() {
		return mat_seg_bgref;
	}
	public void setBgref_mat(Mat bgref_mat) {
		this.mat_seg_bgref = bgref_mat;
	}
	////
	public int getMax_similarity_compare_width() {
		return max_similarity_compare_width;
	}
	public void setMax_similarity_compare_width(int max_similarity_compare_width) {
		this.max_similarity_compare_width = max_similarity_compare_width;
	}
	////
	
	protected JSONObject getVidCapMetadata(boolean isShowPreview, int aPreviewWidth)
	{
		JSONObject jsonMeta = new JSONObject();

		VideoCapture vid = this.videocap;			
		if(vid.isOpened())
		{
			double dTotalFrameCount = vid.get(Videoio.CAP_PROP_FRAME_COUNT);
			double dFps = vid.get(Videoio.CAP_PROP_FPS);
			
			double dEstDurationMs = Math.floor((dTotalFrameCount / dFps)*1000);
			//
			jsonMeta.put("FPS", dFps);
			jsonMeta.put("EST_DURATION", toDurationStr((long)dEstDurationMs));
			jsonMeta.put("EST_DURATION_MS", dEstDurationMs);
			//
			jsonMeta.put("FRAME_COUNT", (int)dTotalFrameCount);
			jsonMeta.put("FRAME_WIDTH", vid.get(Videoio.CAP_PROP_FRAME_WIDTH));
			jsonMeta.put("FRAME_HEIGHT", vid.get(Videoio.CAP_PROP_FRAME_HEIGHT));
			//
			
			if(isShowPreview)
			{
				JSONObject jsonSampling = new JSONObject();
				String sJpgBase64 = null;
				Mat matSample = new Mat();
				try {
				
					int iSearchFrame = 2;
					double dBrightness = 0.0;
					
					vid.set(Videoio.CAP_PROP_POS_FRAMES, iSearchFrame);
					while(vid.read(matSample)) 
					{	
						if(!matSample.empty())
						{
							dBrightness = OpenCvUtil.calcBrightness(matSample, null, 100);
							if(dBrightness>0.15)
							{
								if(aPreviewWidth>0)
								{
									OpenCvUtil.resizeByWidth(matSample, aPreviewWidth);
								}
								sJpgBase64 = OpenCvUtil.mat2base64Img(matSample, "JPG");
								jsonSampling.put(String.valueOf(iSearchFrame), sJpgBase64);
								
								break;
							}
						}
						iSearchFrame += Math.ceil(dFps);
						if(iSearchFrame>dTotalFrameCount)
						{
							break;
						}
						vid.set(Videoio.CAP_PROP_POS_FRAMES, iSearchFrame);
					}
				}
				finally
				{
					if(matSample!=null)
						matSample.release();
				}
				jsonMeta.put("PREVIEW_FRAMES", jsonSampling);
			}
			
		}

		return jsonMeta;
	}
	
	protected Map<Long,Mat> getVideoCapFrames(int aPosType, long aPosValue[])
	{
		Map<Long,Mat> mapFrames = new LinkedHashMap<Long,Mat>();
		
		VideoCapture videoCap = this.videocap;
		
		if(videoCap!=null && aPosValue.length>=0)
		{
			for(long lFramePos : aPosValue)
			{
				videoCap.set(aPosType, lFramePos);
				if(videoCap.isOpened())
				{
					Mat matFrame = new Mat();
					if(videoCap.read(matFrame))
					{
						mapFrames.put(lFramePos, matFrame);
					}
					if(matFrame.width()==0)
						matFrame.release();
				}
			}

		}
		return mapFrames;
	}
	
	public long processVideo()
	{
		return processVideo(0, -1);
	}

	public long processVideo(
			final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		boolean isLiveCam 	= true;
		
		VideoCapture vid 	= null;
		String sVidCapName  = this.videocap_name;
		Mat matFrame 		= new Mat();
		
		Mat matPrevDescriptors 	= null;
		Mat matCurDescriptors 	= null;
		
		long lCurrentFrameNo 	= 0;
		long lActualProcessed 	= 0;
		long lActualSkipped 	= 0;
		
		long lAdjSelFrameMsFrom	= aSelectedTimestampFrom;
		long lAdjSelFrameMsTo	= aSelectedTimestampTo;
		
		if(lAdjSelFrameMsFrom<0)
			lAdjSelFrameMsFrom = 0;

		long lElapseStartMs 	= System.currentTimeMillis();
		long lCurFrameTimestamp = 0;
		
		try{
			
			vid = this.videocap;
			if(vid.isOpened())
			{
				double dFps = Math.floor(vid.get(Videoio.CAP_PROP_FPS)*1000.0)/1000.0;
				double dTotalFrames = vid.get(Videoio.CAP_PROP_FRAME_COUNT);
				int iWidth 	= (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
				int iHeight = (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				
				double dFrameMs = dFps/1000;
						
				if(dTotalFrames>0)
				{
					isLiveCam = false;
					
					double dTotalDurationMs = Math.floor((dTotalFrames / dFps)*1000);
					//
					dFrameMs = Math.floor(dTotalDurationMs / dTotalFrames);
					//
	
					if(lAdjSelFrameMsTo > dTotalDurationMs)
					{
						lAdjSelFrameMsTo = (long) dTotalDurationMs;
					}
					
					// Adjust
					if(lAdjSelFrameMsFrom>0)
					{
						long lAdjFrameStartMs = 0;
						while(lAdjFrameStartMs < lAdjSelFrameMsFrom)
						{
							lCurrentFrameNo++;
							lAdjFrameStartMs += dFrameMs;
						}
						lAdjSelFrameMsFrom = lAdjFrameStartMs;
					}
					
					if(aSelectedTimestampFrom == aSelectedTimestampTo)
					{
						if(lAdjSelFrameMsFrom != aSelectedTimestampFrom)
						{
							if(lAdjSelFrameMsFrom > aSelectedTimestampTo)
							{
								lAdjSelFrameMsFrom = 0;
							}
						}
					}
							
					if(lAdjSelFrameMsTo>=0)
					{
						long lAdjFrameEndMs = 0;
						while(lAdjFrameEndMs < lAdjSelFrameMsTo)
						{
							lAdjFrameEndMs += dFrameMs;
						}
						if(lAdjFrameEndMs>lAdjSelFrameMsTo)
						{
							lAdjFrameEndMs -= dFrameMs;
						}
						lAdjSelFrameMsTo = lAdjFrameEndMs;
					}
					else
					{
						lAdjSelFrameMsTo = (long)dTotalDurationMs;
					}
				
				}
				////////////
				
				
				double dTotalSelectedDurationMs = lAdjSelFrameMsTo - lAdjSelFrameMsFrom;
				double dTotalSelectedFrames = Math.ceil(dTotalSelectedDurationMs/1000 * dFps);
				
				if(dFrameMs%10>0)
				{
					dTotalSelectedFrames++;
				}
				
				boolean isProcessVideo = processStarted( sVidCapName, 
						lAdjSelFrameMsFrom, lAdjSelFrameMsTo, iWidth, iHeight, 
						(long)dTotalSelectedFrames, dFps, (long) dTotalSelectedDurationMs );
				
				if(!isProcessVideo)
					return 0;
				
				ImgSegmentation imgSegment = null;
				if(this.mat_seg_bgref!=null)
				{
					imgSegment = new ImgSegmentation();
					imgSegment.setBackground_ref_mat(this.mat_seg_bgref);
				}
				
				ImgROI imgROI = null;
				if(this.mat_roi_mask!=null || this.rect_crop_roi!=null)
				{
					imgROI = new ImgROI();
					imgROI.setROI_mask(this.mat_roi_mask);
					imgROI.setCrop_ROI_rect(this.rect_crop_roi);
				}
				
				double dProgressPercentage = 0.0;
				
				if(!isLiveCam && lAdjSelFrameMsFrom>0)
				{
					//Jump video to 'from' 
					vid.set(Videoio.CAP_PROP_POS_MSEC, lAdjSelFrameMsFrom);
				}
				
				lElapseStartMs = System.currentTimeMillis();
				if(vid.grab())
				{
					while(vid.retrieve(matFrame))
					{
						if(isLiveCam)
						{
							lCurFrameTimestamp = System.currentTimeMillis() - lElapseStartMs;
						}
						else
						{
							//lCurFrameTimestamp += dFrameMs;
							lCurFrameTimestamp = (long) vid.get(Videoio.CAP_PROP_POS_MSEC);
						}
						
						if(lAdjSelFrameMsTo>-1 && lCurFrameTimestamp > lAdjSelFrameMsTo)
						{
							break;
						}
						
						lCurrentFrameNo++;
						lActualProcessed++;
							
						if(vid.grab())
						{
							if(lAdjSelFrameMsTo==-1 && isLiveCam)
							{
								if(dProgressPercentage<99)
								{
									dProgressPercentage += 0.5;
								}
								else
								{
									dProgressPercentage = 99.99;
								}
							}
							else
							{
								dProgressPercentage = Math.ceil((double)lActualProcessed * 10000.00 / dTotalSelectedFrames) / 100 ;
							}
						}
						else
						{
							dProgressPercentage = 100.0;
						}
						
						if(this.min_brightness_skip_threshold>0)
						{
							double dBrightness = OpenCvUtil.calcBrightness(matFrame, null, this.max_brightness_calc_width);
							if(dBrightness < this.min_brightness_skip_threshold)
							{
								skippedVideoFrame(sVidCapName, matFrame, 
										lCurrentFrameNo, lCurFrameTimestamp, 
										dProgressPercentage, EVENT_BRIGHTNESS, dBrightness);
								lActualSkipped++;
								continue;
							}
						}
						
						if(imgSegment!=null)
						{
							matFrame = imgSegment.extractForeground(matFrame);
						}
						
						if(imgROI!=null)
						{
							imgROI.extractImageROI(matFrame);
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
										skippedVideoFrame(sVidCapName, matFrame, 
												lCurrentFrameNo, lCurFrameTimestamp, 
												dProgressPercentage, EVENT_SIMILARITY, dSimilarityScore);
										lActualSkipped++;
										continue;
									}
								}
								
								matPrevDescriptors = matCurDescriptors;
							}
							
							if(lCurFrameTimestamp>=lAdjSelFrameMsFrom)
							{
								if(lCurFrameTimestamp<=lAdjSelFrameMsTo || lAdjSelFrameMsTo==-1)
								{
									matFrame = decodedVideoFrame(
											sVidCapName, matFrame, 
											lCurrentFrameNo, lCurFrameTimestamp, dProgressPercentage);
								}
							}
						}
						
						if(matFrame==null)
						{
							processAborted(sVidCapName, matFrame, 
									lCurrentFrameNo, lCurFrameTimestamp, dProgressPercentage, EVENT_NULLFRAME);
							break;
						}
					}
				}
			}
		}finally
		{
			long lTotalElapsedMs = System.currentTimeMillis() - lElapseStartMs;
			processEnded(sVidCapName, lAdjSelFrameMsFrom, lAdjSelFrameMsTo, 
					(long)lActualProcessed, lActualSkipped, lTotalElapsedMs);
			
			if(vid!=null)
				vid.release();
			
			if(matFrame!=null)
				matFrame.release();
			
			if(matCurDescriptors!=null)
				matCurDescriptors.release();
			
			if(matPrevDescriptors!=null)
				matPrevDescriptors.release();
		}
		
		return (long)lActualProcessed;
	}
	
	///// 
	public boolean processStarted(String aVideoSourceName, 
			long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth, int aResHeight, 
			long aTotalSelectedFrames, double aFps, long aSelectedDurationMs)
	{
		return true;
	}
	
	public void processEnded(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, 
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs)
	{
		
	}
	
	public Mat skippedVideoFrame(String aVideoSourceName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs, double aProgressPercentage, String aReason, double aScore)
	{
		return matFrame;
	}
	
	public Mat processAborted(String aVideoSourceName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs,  double aProgressPercentage, String aReason)
	{
		return matFrame;
	}

	public Mat decodedVideoFrame(String aVideoSourceName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs, double aProgressPercentage)
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
