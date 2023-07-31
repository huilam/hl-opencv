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
	////
	
	public VideoFileDecoder(File aVideoFile)
	{
		//
		VideoCapture vid = new VideoCapture(aVideoFile.getAbsolutePath());
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
		return getVideoFileMetadata(isShowPreview, 200);
	}
	
	public JSONObject getVideoFileMetadata(boolean isShowPreview, int aPreviewWidth)
	{
		File videoFile = this.video_file;
		
		JSONObject jsonMeta = new JSONObject();
		if(validateFileInput(videoFile,0,0)==0)
		{
			jsonMeta = super.getVidCapMetadata(isShowPreview, aPreviewWidth);
			//
			jsonMeta.put("SOURCE", videoFile.getAbsolutePath());
			jsonMeta.put("FILE_SIZE", videoFile.length());
			jsonMeta.put("FILE_LAST_MODIFIED", videoFile.lastModified());
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
	
	public long processVideoFile()
	{
		return processVideoFile(0, -1);
	}
	
	public long processVideoFile(final long aSelectedTimestampFrom) 
	{
		return processVideoFile(aSelectedTimestampFrom, -1);
	}
	
	public long processVideoFile(final long aSelectedTimestampFrom, final long aSelectedTimestampTo)
	{
		File videoFile = this.video_file;
		
		int iErrCode = validateFileInput(videoFile, aSelectedTimestampFrom, aSelectedTimestampTo);
		if(iErrCode<0)
		{
			return iErrCode;
		}
		return super.processVideo(aSelectedTimestampFrom, aSelectedTimestampTo);
	}
	
	public Mat getOneFrameByIndex(long aIndexes)
	{
		Map<Long,Mat> mapFrames = getVideoFileFrames(Videoio.CAP_PROP_POS_FRAMES, new long[] {aIndexes});
		
		if(mapFrames.size()>0)
			return mapFrames.get(0l);
		
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
			return mapFrames.get(0l);
		
		return null;
	}
	
	public Map<Long,Mat> getFramesByTimestamp(long aTimestamp[])
	{
		return getVideoFileFrames(Videoio.CAP_PROP_POS_MSEC, aTimestamp);
	}

	private Map<Long,Mat> getVideoFileFrames(int aPosType, long aPosValue[])
	{ 
		return super.getVideoCapFrames(aPosType, aPosValue);
	}
	
	
}
