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
import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.plugins.VideoProcessorDebugPlugin;

public class TestVideoProcessor {
	
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		File fileVid = new File("./test/videos/youtube/SG_REQ_NOMASK.mp4");
		
		System.out.println(fileVid.getName()+ " = "+fileVid.exists());
		
		long lFreeMemory1 = Runtime.getRuntime().freeMemory();
		
		
		VideoProcessor test = new VideoProcessor();
		
		test.processLiveCamera(0, VideoProcessorDebugPlugin.class.getName(), -1);
		//test.processVideoFile(fileVid, VideoProcessorDebugPlugin.class.getName());
		
		long lFreeMemory2 = Runtime.getRuntime().freeMemory();
		System.out.println();
		System.out.println("totalMemory="+Runtime.getRuntime().totalMemory());
		System.out.println("freeMemory (before) ="+lFreeMemory1);
		System.out.println("freeMemory (after) ="+lFreeMemory2);
		System.out.println("freeMemorry (after-before) ="+(lFreeMemory2-lFreeMemory1));
		
	}
}
