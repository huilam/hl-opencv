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

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class VideoFileDecoder extends VideoCaptureDecoder {
	
	private static Logger logger = Logger.getLogger(VideoFileDecoder.class.getName());
	
	private File video_file = null;
	
	private int default_preview_width 				= 200;
	private JSONObject json_video_meta				= null;
	private JSONObject json_video_default_preview	= null;
	
	
	private static long MAX_RES_8K 		= 8192;
	private static long MAX_FPS_100 	= 100;
	private static String[] SUPP_VIDEO_FORMAT_EXT = new String[] {".mp4",".mkv",".avi"};
	
	////
	
	public VideoFileDecoder(File aVideoFile)
	{
		initVideoFile(aVideoFile, Videoio.CAP_ANY);
	}
	
	public VideoFileDecoder(File aVideoFile, int aApiPreference)
	{
		initVideoFile(aVideoFile, aApiPreference);
	}
	
	private void initVideoFile(File aVideoFile, int aApiPreference)
	{
		//
		VideoCapture vid = 
				new VideoCapture(aVideoFile.getAbsolutePath(), aApiPreference);
		super.setVideoCapture(vid);
		this.video_file = aVideoFile;
		//
		super.setVideoCaptureName(aVideoFile.getName());
	}
	
	public JSONObject getVideoFileMetadata()
	{
		return getVideoFileMetadata(false);
	}
	
	public JSONObject getVideoFileMetadata(boolean isShowPreview)
	{
		return getVideoFileMetadata(isShowPreview, default_preview_width);
	}
	
	private JSONObject getCachedVideoMeta(boolean isShowPreview, int aPreviewWidth)
	{
		JSONObject jsonMeta = null;
		
		if(json_video_meta!=null)
		{
			jsonMeta = new JSONObject(json_video_meta.toString());
			if(isShowPreview)
			{
				if(aPreviewWidth!=default_preview_width || json_video_default_preview==null)
				{
					jsonMeta = null;
				}
				else
				{
					jsonMeta.put("PREVIEW_FRAMES", new JSONObject(json_video_default_preview.toString()));
				}
			}
		}
		
		return jsonMeta;
	}
	
	private void updateCachedVideoMeta(JSONObject jsonMeta, boolean isShowPreview, int aPreviewWidth)
	{
		if(jsonMeta!=null)
		{
			json_video_meta = new JSONObject(jsonMeta.toString());
			
			JSONObject jsonPreviews = (JSONObject)json_video_meta.remove("PREVIEW_FRAMES");
			
			if(jsonPreviews!=null && aPreviewWidth==default_preview_width)
			{
				json_video_default_preview = new JSONObject(jsonPreviews.toString());
			}
		}
		else
		{
			json_video_meta = null;
			json_video_default_preview = null;
		}
	}
	
	public JSONObject getVideoFileMetadata(boolean isShowPreview, int aPreviewWidth)
	{
		JSONObject jsonMeta = getCachedVideoMeta(isShowPreview, aPreviewWidth);
		
		if(jsonMeta==null)
		{
		
			File videoFile = this.video_file;
			
			jsonMeta = null;
			if(validateFileInput(videoFile,0,0)==0)
			{
				jsonMeta = super.getVidCapMetadata(isShowPreview, aPreviewWidth);
				//
				if(jsonMeta!=null)
				{
					jsonMeta.put("SOURCE", videoFile.getAbsolutePath());
					jsonMeta.put("FILE_SIZE", videoFile.length());
					jsonMeta.put("FILE_LAST_MODIFIED", videoFile.lastModified());
				}
			}
		
			//update cache
			updateCachedVideoMeta(jsonMeta, isShowPreview, aPreviewWidth);	
		}
		
		return jsonMeta;
	}
	
	
	private int validateFileInput(File aVideoFile, final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		int iErrCode = 0;
		if(aVideoFile==null || !aVideoFile.isFile())
		{
			iErrCode = -1;
			logger.log(Level.SEVERE, "Please make sure input file is a valid video file.");
		}
		else
		{
			iErrCode = -2;
			String sFileName = aVideoFile.getName().toLowerCase();
			for(String sExt : SUPP_VIDEO_FORMAT_EXT)
			{
				if(sFileName.endsWith(sExt))
				{
					iErrCode = 0;
					break;
				}
			}
		}
		
		if(iErrCode==0)
		{
			JSONObject jsonMeta = getCachedVideoMeta(false,0);
			
			if(jsonMeta==null)
			{
				jsonMeta = this.getVidCapMetadata(false, 0);
				updateCachedVideoMeta(jsonMeta, false, 0);
			}
			
			if(jsonMeta!=null)
			{
				long lMetaFrameCount 	= jsonMeta.getLong("Videoio.CAP_PROP_FRAME_COUNT");
				double dMetaFps 		= jsonMeta.getLong("Videoio.CAP_PROP_FPS");
				long lMetaFrameWidth 	= jsonMeta.getLong("Videoio.CAP_PROP_FRAME_WIDTH");
				long lMetaFrameHeight 	= jsonMeta.getLong("Videoio.CAP_PROP_FRAME_HEIGHT");
				
				if(dMetaFps<=0 || dMetaFps>MAX_FPS_100)
				{
					//invalid FPS
					iErrCode = -3;
				}
				
				else if(lMetaFrameWidth <= 0 || lMetaFrameWidth > MAX_RES_8K)
				{
					//invalid Resolution
					iErrCode = -4;
				}
				else if(lMetaFrameHeight <= 0 || lMetaFrameHeight > MAX_RES_8K)
				{
					//invalid Resolution
					iErrCode = -4;
				}
				
				else if(lMetaFrameCount <= dMetaFps)
				{
					//Total frame count cannot smaller than fps
					iErrCode = -5;
				}
				
			}
			
			
		}
		
		if(iErrCode<0)
		{
			String sErrMsg = "";
			switch (iErrCode)
			{
				case -5:
					sErrMsg = "Input video file's FPS is higher than total frame count.";
					break;
				case -4:
					sErrMsg = "Input file contain invalid video frame resolution or greater than "+MAX_RES_8K+".";
					break;
				case -3:
					sErrMsg = "Input file contain invalid FPS value or greater than "+MAX_FPS_100+".";
					break;
				case -2:
					sErrMsg = "Please make sure input file is a supported video format.";
					break;
				case -1:
					sErrMsg = "Please make sure input file is a valid video file.";
					break;
			}
			
			logger.log(Level.SEVERE, "(ErrCode:"+iErrCode+") "+sErrMsg+" - "+aVideoFile.getName());
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
	
	public JSONObject processVideoFile()
	{
		return processVideoFile(0, -1);
	}
	
	public JSONObject processVideoFile(final long aSelectedTimestampFrom) 
	{
		return processVideoFile(aSelectedTimestampFrom, -1);
	}
	
	public JSONObject processVideoFile(final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		File videoFile = this.video_file;
		
		int iErrCode = validateFileInput(videoFile, aSelectedTimestampFrom, aSelectedTimestampTo);
		if(iErrCode<0)
		{
			JSONObject jsonErr = new JSONObject();
			jsonErr.put("error_code", iErrCode);
			return jsonErr;
		}
		return super.processVideo(aSelectedTimestampFrom, aSelectedTimestampTo);
	}
	
	public Mat getOneFrameByIndex(long aIndex)
	{
		Map<Long,Mat> mapFrames = getVideoFileFrames(Videoio.CAP_PROP_POS_FRAMES, new long[] {aIndex});
		
		if(mapFrames.size()>0)
			return mapFrames.get(aIndex);
		
		return null;
	}
	
	public Map<Long,Mat> getFramesByIndex(long aIndexes[])
	{
		return getVideoFileFrames(Videoio.CAP_PROP_POS_FRAMES, aIndexes);
	}

	public Mat getOneFrameByTimestamp(long aTimestamp)
	{
		Map<Long,Mat> mapFrames = getVideoFileFrames(Videoio.CAP_PROP_POS_MSEC, new long[] {aTimestamp});
		
		if(mapFrames.size()>0)
			return mapFrames.get(aTimestamp);
		
		return null;
	}
	
	public Map<Long,Mat> getFramesByTimestamp(long aTimestamps[])
	{
		return getVideoFileFrames(Videoio.CAP_PROP_POS_MSEC, aTimestamps);
	}

	private Map<Long,Mat> getVideoFileFrames(int aPosType, long aPosValue[])
	{ 
		return super.getVideoCapFrames(aPosType, aPosValue);
	}
	
	
}
