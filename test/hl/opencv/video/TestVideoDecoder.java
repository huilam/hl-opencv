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

import org.opencv.core.Core;
import org.opencv.core.Mat;

import hl.opencv.OpenCvLibLoader;

public class TestVideoDecoder extends VideoDecoder {
	
	public void decodedMetadata(long aFps, long aTotalFrames)
	{
		System.out.println("FPS : "+aFps);
		System.out.println("Total frames : "+aTotalFrames);
		System.out.println("Duration : "+ toDurationStr(aTotalFrames/aFps*1000));
	}
	
	public Mat decodedVideoFrame(Mat matFrame, long aFrameNo, long aFrameMs)
	{
		System.out.println(aFrameNo+" - "+toDurationStr(aFrameMs));
		return matFrame;
	}
	
	private static void initOpenCV()
	{
		OpenCvLibLoader cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME,"/");
		if(!cvLib.init())
		{
			throw new RuntimeException("OpenCv is NOT loaded !");
		}
	}
	
	public static void main(String args[]) throws Exception
	{
		initOpenCV();
		
		File file = new File("./test/videos/XXX/XXX.mp4");
		new TestVideoDecoder().processVideo(file);
	}
}
