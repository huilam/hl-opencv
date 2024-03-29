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

package hl.opencv.video.processor;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import hl.opencv.video.decoder.VideoCaptureDecoder;
import hl.opencv.video.decoder.VideoFileDecoder;
import hl.opencv.video.plugins.IVideoProcessorPlugin;
import hl.opencv.video.utils.VideoFileUtil;

public class VideoProcessor {
	
	private static Logger logger = Logger.getLogger(VideoProcessor.class.getName());
	
	//////////////////////////////////////
	
	public long processVideoFile(File aVidFile, String aProcessorPluginName)
	{
		return processVideoFile(aVidFile, aProcessorPluginName, 0, -1);
	}
	
	public long processVideoFile(File aVidFile, String aProcessorPluginName,
			long aFrameDurationFrom, long aFrameDurationTo)
	{
		if(!aVidFile.isFile())
		{
			logger.log(Level.SEVERE, "Video file NOT found !- "+aVidFile.getAbsolutePath());
			return 0;
		}
		
		if(aProcessorPluginName==null || aProcessorPluginName.trim().length()==0)
		{
			logger.log(Level.SEVERE, "Invalid Plugin ClassName !- "+aProcessorPluginName);
			return 0;
		}
		
		JSONObject jsonMeta = VideoFileUtil.getVideoFileMetadata(aVidFile);
		IVideoProcessorPlugin plugin = initNewPlugin(aProcessorPluginName, jsonMeta);
		return processVideoFile(aVidFile, plugin, aFrameDurationFrom, aFrameDurationTo);
	}
	
	public long processVideoFile(File aVidFile, IVideoProcessorPlugin plugin)
	{
		return processVideoFile(aVidFile, plugin, 0, -1);
	}
	
	public long processVideoFile(File aVidFile, IVideoProcessorPlugin plugin,
			long aFrameDurationFrom, long aFrameDurationTo)
	{
		long lFramesProcessed = 0;
		VideoFileDecoder vid = null;
		VideoCapture vcap = null;
		
		if(aVidFile!=null && aVidFile.isFile() 
				&& plugin!=null)
		{
		
			try {
				vid = new VideoFileDecoder(aVidFile);
				vcap = vid.getVideoCapture();
				
				JSONObject jsonMeta = vid.getVideoFileMetadata();
				if(jsonMeta==null || jsonMeta.length()==0)
				{
					logger.log(Level.SEVERE, "Invalid video file - "+aVidFile.getAbsolutePath());
				}
				else
				{
					VideoCaptureDecoder vidDecoder = initVideoDecoderWithPlugin(plugin);
					if(vidDecoder!=null)
					{
						vidDecoder.setVideoCapture(vcap);
						vidDecoder.setVideoCaptureName(aVidFile.getName());
						lFramesProcessed = vidDecoder.processVideo(aFrameDurationFrom, aFrameDurationTo);
						plugin.destroyPlugin(jsonMeta);
					}
				}
			}
			finally
			{
				if(vcap!=null)
					vcap.release();
				
				if(vid!=null)
					vid.release();
			}
		
		}
		else
		{
			if(aVidFile==null || !aVidFile.isFile())
				logger.log(Level.SEVERE, "Invalid Video File - "+(aVidFile==null?aVidFile:aVidFile.getAbsolutePath()));
			
			if(plugin==null)
				logger.log(Level.SEVERE, "Invalid plugin - "+plugin);
		}		
		return lFramesProcessed;
	}
	
	//////////////////////////////////////
	
	private IVideoProcessorPlugin initNewPlugin(String aPluginClassName, JSONObject aMetaJson)
	{
		IVideoProcessorPlugin plugin = null;
		
		try {
			Class<?> classPlugin = Class.forName(aPluginClassName);
			if(classPlugin!=null)
			{
				plugin = (IVideoProcessorPlugin) classPlugin.getDeclaredConstructor().newInstance();
				if(plugin.initPlugin(aMetaJson))
				{
					return plugin;
				}
				else throw new Exception("plugin:"+aPluginClassName+" - InitPlugin return false !");
			}
		}catch(Exception ex)
		{
			logger.severe(ex.getMessage());
		}
		return null;
	}
	
	private VideoCaptureDecoder initVideoDecoderWithPlugin(IVideoProcessorPlugin aProcessorPlugin)
	{
		VideoCaptureDecoder vidDecoder = null;
		if(aProcessorPlugin!=null)
		{
			vidDecoder = new VideoCaptureDecoder()
			{
				public boolean processStarted(String aVideoFileName, 
						long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth, int aResHeight, 
						long aTotalSelectedFrames, double aFps, long aSelectedDurationMs)
				{
					return aProcessorPlugin.processStarted(aVideoFileName, aAdjSelFrameMsFrom, aAdjSelFrameMsTo, 
							aResWidth, aResHeight, aTotalSelectedFrames, aFps, aSelectedDurationMs);
				}
	
				public Mat decodedVideoFrame(String aVideoFileName, Mat matFrame, 
						long aCurFrameNo, long aCurFrameMs, double aProgressPercentage)
				{
					return aProcessorPlugin.decodedVideoFrame(aVideoFileName, matFrame, aCurFrameNo, 
							aCurFrameMs, aProgressPercentage);
				}
				
				public Mat skippedVideoFrame(String aVideoFileName, Mat matFrame, 
						long aCurFrameNo, long aCurFrameMs, double aProgressPercentage, String aReason, double aScore)
				{
					return aProcessorPlugin.skippedVideoFrame(aVideoFileName, matFrame, aCurFrameNo, aCurFrameMs, 
							aProgressPercentage, aReason, aScore);
				}
				
				public Mat processAborted(String aVideoFileName, Mat matFrame, 
						long aCurFrameNo, long aCurFrameMs,  double aProgressPercentage, String aReason)
				{
					return aProcessorPlugin.processAborted(aVideoFileName, matFrame, aCurFrameNo, aCurFrameMs, 
							aProgressPercentage, aReason);
				}
				
				public void processEnded(String aVideoFileName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, 
						long aTotalProcessed, long aTotalSkipped, long aElpasedMs)
				{
					aProcessorPlugin.processEnded(aVideoFileName, aAdjSelFrameMsFrom, aAdjSelFrameMsTo, 
							aTotalProcessed, aTotalSkipped, aElpasedMs);
				}
			};
		}
		return vidDecoder;
	}
	
}
