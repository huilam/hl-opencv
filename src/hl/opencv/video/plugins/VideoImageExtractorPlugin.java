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

package hl.opencv.video.plugins;

import java.io.File;
import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.opencv.util.OpenCvUtil;

public class VideoImageExtractorPlugin implements IVideoProcessorPlugin {
	
	private File folder_imgoutput = null;
	private long extract_success_count = 0;
	private long extract_failed_count = 0;
	
	public boolean setOutputFolder(String aOutputFolder)
	{
		File folder = new File(aOutputFolder);
		folder.mkdirs();
		return folder.isDirectory();
	}

	@Override
	public boolean processStarted(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth,
			int aResHeight, long aTotalSelectedFrames, double aFps, long aSelectedDurationMs) {
		return true;
	}

	@Override
	public Mat decodedVideoFrame(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage) {
		
		String sImageFileName = aVideoSourceName+"_"+aCurFrameNo+"_"+aCurFrameMs+".jpg";
		
		if(OpenCvUtil.saveImageAsFile(matFrame, folder_imgoutput.getAbsolutePath()+"/"+sImageFileName))
			extract_success_count++;
		else
			extract_failed_count++;
		
		if(aCurFrameNo%50==0 || aCurFrameNo==1)
		{
			System.out.println();
			System.out.print("#"+aCurFrameNo+" "+aProgressPercentage+"% ");
		}
		System.out.print(".");		
		
		return matFrame;
	}

	@Override
	public Mat skippedVideoFrame(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage, String aReason, double aScore) {
		
		return matFrame;
	}

	@Override
	public Mat processAborted(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage, String aReason) {
		
		return matFrame;
	}

	@Override
	public Map<?,?> processEnded(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo,
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs) {
		
		System.out.println();
		System.out.println("[COMPLETED] "+aVideoSourceName);
		System.out.println(" - Total Elapsed : "+aElpasedMs+" ms");
		System.out.println(" - Total Processed : "+aTotalProcessed);
		System.out.println(" - Images Extract Location: "+folder_imgoutput.getAbsolutePath());
		System.out.println(" - Images Extract Success: "+extract_success_count);
		System.out.println(" - Images Extract Failed : "+extract_failed_count);
		return null;
	}

	@Override
	public boolean initPlugin(JSONObject aMetaJson) {
		
		extract_success_count = 0;
		extract_failed_count = 0;
		
		String sVideoSource = aMetaJson.getString("SOURCE");
		
		File fileVideo = new File(sVideoSource).getParentFile();
		if(fileVideo==null)
		{
			//Live Camera
			folder_imgoutput = new File("./test/"+"cameras/"+sVideoSource+"/output/"+System.currentTimeMillis());
			return folder_imgoutput.mkdirs();
		}
		else if(fileVideo.isDirectory())
		{
			folder_imgoutput = new File(fileVideo.getAbsolutePath()+"/output/"+System.currentTimeMillis());
			return folder_imgoutput.mkdirs();
		}
		
		System.err.println("Error creating output folder for VideoSource : "+sVideoSource);
		return false;
			
	}


	@Override
	public void destroyPlugin(JSONObject aMetaJson) {
		folder_imgoutput = null;
	}
	
}
