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

import hl.common.FileUtil;
import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.plugins.VideoFileReEncodingPlugin;
import hl.opencv.video.plugins.VideoFrameExtractorPlugin;
import hl.opencv.video.plugins.VideoImageSizeCalcPlugin;
import hl.opencv.video.plugins.VideoProcessorDebugPlugin;
import hl.opencv.video.processor.VideoProcessor;

public class TestVideoProcessor {
	
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		
		File[] videoFIles = FileUtil.getFilesWithExtensions(new File("./test/videos/"), 
				new String[] {".mp4"});
		
		for(File vid : videoFIles)
		{
			System.out.println("Processing "+vid.getName()+" ...");
		
			VideoProcessor test = new VideoProcessor();
			String sPluginClassName =  VideoFileReEncodingPlugin.class.getName();
			
			int iPluginId = 1;
		
			switch(iPluginId)
			{
				case 1 : 
					break;
				case 2 : 
					sPluginClassName = VideoProcessorDebugPlugin.class.getName();
					break;
				case 3 : 
					sPluginClassName = VideoFrameExtractorPlugin.class.getName();
					break;
				case 4 : 
					sPluginClassName = VideoImageSizeCalcPlugin.class.getName();
					break;
				default :
			}
			test.processVideoFile(vid, sPluginClassName, 0, -1);
		}
		
	}
}
