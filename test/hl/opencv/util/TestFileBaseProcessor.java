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

package hl.opencv.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import hl.opencv.OpenCvLibLoader;
import hl.opencv.video.VideoDecoder;

public class TestFileBaseProcessor {
	
	private List<String> listImageExt = new ArrayList<String>();
	private List<String> listVideoExt = new ArrayList<String>();
	
	public void processFolder(File folder)
	{
		listImageExt.add(".jpg");
		listImageExt.add(".png");
		
		listVideoExt.add(".mkv");
		listVideoExt.add(".mp4");
		
		System.out.println();
		
		int iImageSeqNo = 0;
		int iVideoSeqNo = 0;
		for(File f : folder.listFiles())
		{
			if(!f.isFile())
				continue;
			
			String sFileName = f.getName().toLowerCase();
			String sFileExt = sFileName.substring(sFileName.length()-4);
			
			if(listImageExt.contains(sFileExt))
			{
				processImageFile(++iImageSeqNo, f);
			}
			else if(listVideoExt.contains(sFileExt))
			{
				processVideoFile(++iVideoSeqNo, f);
			}
		}
	}
	
	public void processImageFile(int aSeqNo, File aImageFile)
	{
		Mat matFile = OpenCvUtil.loadImage(aImageFile.getAbsolutePath());
		System.out.println(" - "+aImageFile.getName()+" : "+matFile.width()+"x"+matFile.height());
	}
	
	public void processVideoFile(int aSeqNo, File aVideoFile)
	{
		VideoDecoder vidDecoder = new VideoDecoder()
		{
			@Override 
			public boolean decodedMetadata(String aVideoFileName, int aResWidth, int aResHeight, int aFps, long aTotalFrameCount)
			{
				return video_decodedMetadata(aVideoFileName, aResWidth, aResHeight, aFps, aTotalFrameCount);
			}
			
			@Override 
			public Mat skippedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp)
			{
				return video_skippedVideoFrame(matFrame, aFrameNo, aFrameTimestamp);
			}
			
			@Override 
			public Mat decodedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp)
			{
				return video_decodedVideoFrame(matFrame, aFrameNo, aFrameTimestamp);
			}
		};
		vidDecoder.processVideo(aVideoFile);
	}
	
	protected boolean video_decodedMetadata(String aVideoFileName, int aResWidth, int aResHeight, int aFps, long aTotalFrameCount)
	{
		System.out.println(" - "+aVideoFileName+" : "+aResWidth+"x"+aResHeight);
		return false;
	}
	
	protected Mat video_decodedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp)
	{
		return matFrame;
	}
	
	protected Mat video_skippedVideoFrame(Mat matFrame, long aFrameNo, long aFrameTimestamp)
	{
		return matFrame;
	}
	

	public static void main(String[] args)
	{
		File folder = new File("./test/videos/nls");
		
		TestFileBaseProcessor processor = new TestFileBaseProcessor();
		
		processor.processFolder(folder);
		
		File fileImageOutput = new File("./test/images/output");
		fileImageOutput.mkdirs();
		
	}
	
}
