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

import org.opencv.core.Mat;

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
		File [] files = folder.listFiles();
		
		if(files!=null)
		{
			System.out.println("Total Files = "+files.length);
			
			for(File f : files)
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
		else
		{
			System.out.println("Folder NOT found ! "+folder.getAbsolutePath());
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
			public boolean processStarted(String aVideoFileName, 
					long aFrameTimestampFrom, long aFrameTimestampTo, int aResWidth, int aResHeight, 
					long aTotalSelectedFrames, double aFps, long aSelectedDurationMs)
			{
				return video_processStarted(aVideoFileName, 
						aFrameTimestampFrom, aFrameTimestampTo, aResWidth, aResHeight, 
						aTotalSelectedFrames, aFps, aSelectedDurationMs);
			}
			
			@Override 
			public Mat skippedVideoFrame(String aVideoFileName, Mat matFrame, 
					long aFrameNo, long aFrameTimestamp, double aProgressPercentage, 
					String aReasonCode, double aScore)
			{
				return video_skippedVideoFrame(aVideoFileName, matFrame, aFrameNo, aFrameTimestamp, aProgressPercentage);
			}
			
			@Override 
			public Mat decodedVideoFrame(
					String aVideoFileName, Mat matFrame, 
					long aFrameNo, long aFrameTimestamp, double aProgressPercentage)
			{
				return video_decodedVideoFrame(aVideoFileName, matFrame, aFrameNo, aFrameTimestamp, aProgressPercentage);
			}
			
			@Override 
			public void processEnded(String aVideoFileName, long aFromTimeMs, long aToTimeMs, 
					long aTotalFrames, long aTotalProcessed, long aElpasedMs)
			{
				System.out.println();
				System.out.println("[COMPLETED] "+aVideoFileName);
				System.out.println("Total processed / total : "+aTotalProcessed+" / "+aTotalFrames);
				System.out.println("Total elapsed time (ms) : "+aElpasedMs);
			}
		};
		vidDecoder.processVideoFile(aVideoFile);
	}
	
	protected boolean video_processStarted(String aVideoFileName, 
			long aFrameTimestampFrom, long aFrameTimestampTo, int aResWidth, int aResHeight, 
			long aTotalSelectedFrames, double aFps, long aSelectedDurationMs)
	{
		System.out.println(" - "+aVideoFileName+" : "+aResWidth+"x"+aResHeight);
		return false;
	}
	
	protected Mat video_decodedVideoFrame(String aVideoFileName, Mat matFrame, 
			long aFrameNo, long aFrameTimestamp, double aProgressPercentage)
	{
		return matFrame;
	}
	
	protected Mat video_skippedVideoFrame(String aVideoFileName, Mat matFrame, 
			long aFrameNo, long aFrameTimestamp, double aProgressPercentage)
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
