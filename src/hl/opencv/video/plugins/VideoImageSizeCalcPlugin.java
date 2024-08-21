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

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

public class VideoImageSizeCalcPlugin implements IVideoProcessorPlugin {
	
	private long _total_jpg_file_count 	= 0;
	private long _total_jpg_file_size 	= 0;
	
	private String image_file_format   = ".jpg";
	private int jpg_quality 			= 90;
	
	private MatOfByte matbyte_frame 	= null;
	private MatOfInt matint_jpg_params 	= null;

	@Override
	public boolean processStarted(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth,
			int aResHeight, long aTotalSelectedFrames, double aFps, long aSelectedDurationMs) {
		
		image_file_format = image_file_format.toLowerCase();
		if(!image_file_format.startsWith("."))
			image_file_format = "."+image_file_format;
		
		matint_jpg_params = new MatOfInt(new int[] {Imgcodecs.IMWRITE_JPEG_QUALITY, jpg_quality});
		return true;
	}

	@Override
	public Mat decodedVideoFrame(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage) {
		
		matbyte_frame = new MatOfByte(); 
		Imgcodecs.imencode(image_file_format, matFrame, matbyte_frame, matint_jpg_params); 
		long lImageJpgSize = matbyte_frame.toArray().length;
		matbyte_frame.release();
		
		_total_jpg_file_size += lImageJpgSize;
		_total_jpg_file_count++;
		 
		if(aCurFrameNo%100==0 || aCurFrameNo==1)
		{
			System.out.println();
			System.out.print(aProgressPercentage+"% - "+_total_jpg_file_count+" jpg = "+bytesToWords(_total_jpg_file_size));
		}
		//System.out.print(".");		
		
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
	public JSONObject processEnded(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo,
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs) {
		
		System.out.println();
		System.out.println("[COMPLETED] "+aVideoSourceName);
		System.out.println(" - Total Elapsed : "+aElpasedMs+" ms");
		System.out.println(" - Image File Format : "+image_file_format);
		System.out.println(" - "+image_file_format+" Quality : "+jpg_quality+"%");
		System.out.println(" - Total "+image_file_format+" File Count : "+_total_jpg_file_count);
		System.out.println(" - Total "+image_file_format+" File Size  : "+bytesToWords(_total_jpg_file_size));
		
		
		JSONObject json = new JSONObject();
		json.put("VideoSourceName", aVideoSourceName);
		json.put("FrameMsFrom", aAdjSelFrameMsFrom);
		json.put("FrameMsTo", aAdjSelFrameMsTo);
		json.put("TotalProcessed", aTotalProcessed);
		json.put("TotalSkipped", aTotalSkipped);
		json.put("ElpasedMs", aElpasedMs);
		
		return json;
	}

	@Override
	public boolean initPlugin(JSONObject aMetaJson) {
		return true;
	}


	@Override
	public void destroyPlugin(JSONObject aMetaJson) {
	}
	
	private static String bytesToWords(long aBytes)
    {
    	long _KB = 1000;
    	long _MB = 1000 * _KB;
    	long _GB = 1000 * _MB;
    	
    	StringBuffer sb = new StringBuffer();
    	
    	if(aBytes >= _GB)
    	{
    		long lGB = aBytes / _GB;
    		sb.append(lGB).append(" GB ");
    		aBytes = aBytes % _GB;
    	}
    	
    	if(aBytes >= _MB)
    	{
    		double dMB = ((double)aBytes) / ((double)_MB);
    		sb.append(dMB).append(" MB ");
    		aBytes = aBytes % _MB;
    		aBytes = 0;
    	}
    	
    	if(aBytes >= _KB)
    	{
    		double dKB = ((double)aBytes) / ((double)_KB);
    		sb.append(dKB).append(" kb ");
    		aBytes = 0;
    	}
    	
    	if(aBytes>0)
    	{
    		sb.append(aBytes).append(" bytes ");
    	}
    	
    	
    	if(sb.length()==0)
    	{
    		sb.append("0 byte");
    	}
    	
    	return sb.toString().trim();
    }
	
}
