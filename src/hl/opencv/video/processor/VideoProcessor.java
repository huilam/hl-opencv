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

package hl.opencv.video;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.opencv.video.plugins.IVideoProcessorPlugin;

public class VideoProcessor {
	
	private static Logger logger = Logger.getLogger(VideoProcessor.class.getName());
	
	public void processLiveCamera(int aCamID, String aProcessorPluginName)
	{
		processLiveCamera(aCamID, aProcessorPluginName, -1);
	}
	
	public void processLiveCamera(int aCamID, String aProcessorPluginName, long aMsDuration)
	{
		JSONObject jsonMeta = VideoDecoder.getCameraMetadata(aCamID);
		if(jsonMeta==null || jsonMeta.length()==0)
		{
			logger.log(Level.SEVERE, "Invalid camera - "+aCamID);
			return;
		}
		
		IVideoProcessorPlugin plugin = initNewPlugin(aProcessorPluginName, jsonMeta);
		if(plugin!=null)
		{
			VideoDecoder vidDecoder = initVideoDecoderWithPlugin(plugin);
			if(vidDecoder!=null)
			{
				vidDecoder.processCamera(aCamID, aMsDuration);
				plugin.destroyPlugin(jsonMeta);
			}
		}
		else
		{
			logger.log(Level.SEVERE, "Invalid plugin - "+aProcessorPluginName);
		}
	}
	
	//////////////////////////////////////
	
	public void processVideoFile(File aVidFile, String aProcessorPluginName)
	{
		processVideoFile(aVidFile, aProcessorPluginName, 0, -1);
	}
	
	public void processVideoFile(File aVidFile, String aProcessorPluginName,
			long aFrameDurationFrom, long aFrameDurationTo)
	{
		JSONObject jsonMeta = VideoDecoder.getVideoFileMetadata(aVidFile);
		if(jsonMeta==null || jsonMeta.length()==0)
		{
			logger.log(Level.SEVERE, "Invalid video file - "+aVidFile.getAbsolutePath());
			return;
		}
		
		IVideoProcessorPlugin plugin = initNewPlugin(aProcessorPluginName, jsonMeta);
		if(plugin!=null)
		{
			VideoDecoder vidDecoder = initVideoDecoderWithPlugin(plugin);
			if(vidDecoder!=null)
			{
				vidDecoder.processVideoFile(aVidFile);
				plugin.destroyPlugin(jsonMeta);
			}
		}
		else
		{
			logger.log(Level.SEVERE, "Invalid plugin - "+aProcessorPluginName);
		}
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
	
	private VideoDecoder initVideoDecoderWithPlugin(IVideoProcessorPlugin aProcessorPlugin)
	{
		VideoDecoder vidDecoder = new VideoDecoder()
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
		
		return vidDecoder;
	}
	
}