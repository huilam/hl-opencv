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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.image.ImgROI;
import hl.opencv.image.ImgSegmentation;
import hl.opencv.util.OpenCvUtil;

@Deprecated
public class VideoDecoder {
	
	private static Logger logger = Logger.getLogger(VideoDecoder.class.getName());
	
	private static long SECOND_MS 	= 1000;
	private static long MINUTE_MS 	= SECOND_MS * 60;
	private static long HOUR_MS 	= MINUTE_MS * 60;
	
	private static String EVENT_BRIGHTNESS 		= "BRIGHTNESS";
	private static String EVENT_SIMILARITY 		= "SIMILARITY";
	private static String EVENT_NULLFRAME 		= "NULL_FRAME";
	
	////
	private double min_similarity_skip_threshold = 0.0;
	private double min_brightness_skip_threshold = 0.0;
	
	private int max_brightness_calc_width	 	 = 0;
	private int max_similarity_compare_width	 = 0;
	
	private Mat mat_roi_mask 	= null;
	private Rect rect_crop_roi 	= null;
	
	private Mat mat_seg_bgref 	= null;
	
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
	
	protected static JSONArray listCameras(boolean isIncludeSampleBase64)
	{
		JSONArray jsonArrCams 	= new JSONArray();
		VideoCapture vid 		= null;
		Mat matSample			= null;
		for(int i=0; i<50; i++)
		{
			try {
				if(isIncludeSampleBase64)
				{
					matSample = new Mat();
				}
				vid = new VideoCapture(i);
				if(vid.isOpened())
				{
					int iWidth 	= (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
					int iHeight = (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
					int iFps 	= (int) vid.get(Videoio.CAP_PROP_FPS);
					if(matSample!=null)
					{
						vid.retrieve(matSample);
					}
					vid.release();
					
					JSONObject jsonCam = new JSONObject();
					jsonCam.put("id", i);
					jsonCam.put("width", iWidth);
					jsonCam.put("height", iHeight);
					jsonCam.put("fps", iFps);
					if(matSample!=null)
					{
						jsonCam.put("base64", OpenCvUtil.mat2base64Img(matSample, "JPG"));
					}
					jsonArrCams.put(jsonCam);
				}
				else
				{
					break;
				}
			}
			finally
			{
				if(vid!=null)
					vid.release();
				
				if(matSample!=null)
					matSample.release();
			}
		}
			

		
		return jsonArrCams;
	}
	
	public static JSONObject getVideoFileMetadata(File aVideoFile)
	{
		return getVideoFileMetadata(aVideoFile, false);
	}
	
	public static JSONObject getVideoFileMetadata(File aVideoFile, boolean isShowPreview)
	{
		return getVideoFileMetadata(aVideoFile, isShowPreview, 200);
	}
	
	public static JSONObject getVideoFileMetadata(File aVideoFile, boolean isShowPreview, int aPreviewWidth)
	{
		JSONObject jsonMeta = new JSONObject();
		if(validateFileInput(aVideoFile,0,0)==0)
		{
			VideoCapture vid = null;
			try {
				vid = new VideoCapture(aVideoFile.getAbsolutePath());
				jsonMeta = getVidCapMetadata(vid, isShowPreview, aPreviewWidth);
			}finally
			{
				if(vid!=null)
					vid.release();
			}
			//
			jsonMeta.put("SOURCE", aVideoFile.getAbsolutePath());
			jsonMeta.put("FILE_SIZE", aVideoFile.length());
			jsonMeta.put("FILE_LAST_MODIFIED", aVideoFile.lastModified());
		}
		
		return jsonMeta;
	}
	
	public static JSONObject getCameraMetadata(int aCapDeviceID)
	{
		return getCameraMetadata(aCapDeviceID, false, 0);
	}
	
	public static JSONObject getCameraMetadata(int aCapDeviceID, boolean isShowPreview)
	{
		return getCameraMetadata(aCapDeviceID, isShowPreview, 0);
	}
	
	public static JSONObject getCameraMetadata(int aCapDeviceID, boolean isShowPreview, int aPreviewWidth)
	{
		JSONObject jsonMeta = new JSONObject();
		if(aCapDeviceID>=0)
		{
			VideoCapture vid = new VideoCapture(aCapDeviceID);
			jsonMeta = getVidCapMetadata(vid, isShowPreview, aPreviewWidth);
			//
			jsonMeta.put("SOURCE", aCapDeviceID);
			jsonMeta.put("CAPTURE_DEVICE_ID", aCapDeviceID);
		}
		
		return jsonMeta;
	}
	
	private static JSONObject getVidCapMetadata(VideoCapture aVideoCap, boolean isShowPreview, int aPreviewWidth)
	{
		JSONObject jsonMeta = new JSONObject();

		VideoCapture vid = aVideoCap;
		if(vid!=null && vid.isOpened())
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
	
	private static int validateFileInput(File aVideoFile, final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		int iErrCode = 0;
		if(aVideoFile==null || !aVideoFile.isFile())
		{
			iErrCode = -1;
			logger.log(Level.SEVERE, "Please make sure input file is a valid video file.");
		}
		else
		{
			String sFileName = aVideoFile.getName().toLowerCase();
			if(sFileName.endsWith(".mp4") || sFileName.endsWith(".mkv") || sFileName.endsWith(".avi"))
			{
			}else
			{
				iErrCode = -2;
			}
		}
		
		if(iErrCode<0)
		{
			logger.log(Level.SEVERE, "Please make sure input file is a valid video file.");
		}
		///////////////////////
		
		if(iErrCode==0 && aSelectedTimestampFrom >-1 && aSelectedTimestampTo >-1)
		{
			if(aSelectedTimestampFrom > aSelectedTimestampTo)
			{
				iErrCode = -3;
				logger.log(Level.SEVERE, "Please make sure 'to' is greater than 'from' timestamp.");
			}
		}
		
		return iErrCode;
	}
	
	public long processVideoFile(File fileVideo)
	{
		return processVideoFile(fileVideo, 0, -1);
	}
	
	public long processVideoFile(File aVideoFile, final long aSelectedTimestampFrom) 
	{
		return processVideoFile(aVideoFile, aSelectedTimestampFrom, -1);
	}
	
	public long processVideoFile(File aVideoFile, final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		int iErrCode = validateFileInput(aVideoFile, aSelectedTimestampFrom, aSelectedTimestampTo);
		if(iErrCode<0)
		{
			return iErrCode;
		}
		
		VideoCapture vid = null;
		try{
			String sFileName = aVideoFile.getName();
			vid = new VideoCapture(aVideoFile.getAbsolutePath());
			return processVideoCap(vid, sFileName, aSelectedTimestampFrom, aSelectedTimestampTo);
		}
		finally
		{
			if(vid!=null)
				vid.release();
		}
	}
	
	public Map<Long,Mat> getFramesByIndex(File aVideoFile, long aIndexes[])
	{
		return getVideoFileFrames(aVideoFile, Videoio.CAP_PROP_POS_FRAMES, aIndexes);
	}
	
	public Map<Long,Mat> getFramesByTimestamp(File aVideoFile, long aTimestamp[])
	{
		return getVideoFileFrames(aVideoFile, Videoio.CAP_PROP_POS_MSEC, aTimestamp);
	}
	
	private Map<Long,Mat> getVideoFileFrames(File aVideoFile, int aPosType, long aPosValue[])
	{
		if(aVideoFile.isFile() && aPosValue.length>0)
		{
			VideoCapture vid 	= null;
			try{
				vid = new VideoCapture(aVideoFile.getAbsolutePath());
				return getVideoCapFrames(vid, aPosType, aPosValue);
			}finally
			{
				if(vid!=null)
					vid.release();
			}
		}
		return new LinkedHashMap<Long,Mat>();
	}
	
	private Map<Long,Mat> getVideoCapFrames(final VideoCapture aVideoCap, int aPosType, long aPosValue[])
	{
		Map<Long,Mat> mapFrames = new LinkedHashMap<Long,Mat>();
		if(aVideoCap!=null && aPosValue.length>0)
		{
			for(long lFramePos : aPosValue)
			{
				aVideoCap.set(aPosType, lFramePos);
				if(aVideoCap.isOpened())
				{
					Mat matFrame = new Mat();
					if(aVideoCap.read(matFrame))
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
	
	public long processCamera(int aCamID)
	{
		return processCamera(aCamID, -1);
	}
	
	public long processCamera(int aCamID, final long aSelectedTimestampTo)
	{
		VideoCapture vid = null;
		try{
			vid = new VideoCapture(aCamID);
			return processVideoCap(vid, String.valueOf(aCamID), 0, aSelectedTimestampTo);
		}
		finally
		{
			if(vid!=null)
				vid.release();
		}
	}
	
	private long processVideoCap(VideoCapture aVideoCapture, String aVidCapName,
			final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		boolean isLiveCam 	= true;
		
		VideoCapture vid 	= null;
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
			
			vid = aVideoCapture;
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
				
				boolean isProcessVideo = processStarted( aVidCapName, 
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
							lCurFrameTimestamp += dFrameMs;
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
								skippedVideoFrame(aVidCapName, matFrame, 
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
										skippedVideoFrame(aVidCapName, matFrame, 
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
											aVidCapName, matFrame, 
											lCurrentFrameNo, lCurFrameTimestamp, dProgressPercentage);
								}
							}
						}
						
						if(matFrame==null)
						{
							processAborted(aVidCapName, matFrame, 
									lCurrentFrameNo, lCurFrameTimestamp, dProgressPercentage, EVENT_NULLFRAME);
							break;
						}
					}
				}
			}
		}finally
		{
			long lTotalElapsedMs = System.currentTimeMillis() - lElapseStartMs;
			processEnded(aVidCapName, lAdjSelFrameMsFrom, lAdjSelFrameMsTo, 
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
	public boolean processStarted(String aVideoFileName, 
			long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth, int aResHeight, 
			long aTotalSelectedFrames, double aFps, long aSelectedDurationMs)
	{
		return true;
	}
	
	public void processEnded(String aVideoFileName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, 
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs)
	{
		
	}
	
	public Mat skippedVideoFrame(String aVideoFileName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs, double aProgressPercentage, String aReason, double aScore)
	{
		return matFrame;
	}
	
	public Mat processAborted(String aVideoFileName, Mat matFrame, 
			long aCurFrameNo, long aCurFrameMs,  double aProgressPercentage, String aReason)
	{
		return matFrame;
	}

	public Mat decodedVideoFrame(String aVideoFileName, Mat matFrame, 
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
