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

package hl.opencv.video.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.util.OpenCvUtil;

public class CamUtil {
	
	public static int[] VID_CAP_PRIORITIES = 
			new int[] {Videoio.CAP_DSHOW, Videoio.CAP_FFMPEG,  Videoio.CAP_ANY};
	
	private static int DEF_VID_CAP_ID = -1;
	
	public static int getDefVidCapDriverId()
	{
		return getDefVidCapDriverId(0);
	}
	
	public static int getDefVidCapDriverId(int aDeviceId)
	{
		if(DEF_VID_CAP_ID<0)
		{
			boolean isWindows = false;
			
			String osName = System.getProperty("os.name");
			if(osName!=null)
			{
				osName = osName.toLowerCase();
				isWindows = osName.contains("windows");
			}
			
			VideoCapture vid = null;
			try {
				
				for(int iCAP_ID : VID_CAP_PRIORITIES)
				{
					if(!isWindows && iCAP_ID==Videoio.CAP_DSHOW)
					{
						System.out.println("Skip CAP_"+iCAP_ID);
						continue;
					}
					
					System.out.println("Trying CAP_"+iCAP_ID);
					
					vid = new VideoCapture(aDeviceId, iCAP_ID);
					if(vid.isOpened())
					{
						DEF_VID_CAP_ID = iCAP_ID;
						break;
					}
					else
					{
						if(vid!=null)
						{
							vid.release();
						}
					}
				}
			}
			finally
			{
				if(vid!=null)
				{
					vid.release();
				}
			}
			System.out.println("Init with CAP_"+DEF_VID_CAP_ID);
		}
		return DEF_VID_CAP_ID;
	}

	public static JSONArray listLocalCameras(boolean isIncludeSampleBase64)
	{
		JSONArray jsonArrCams 	= new JSONArray();
		VideoCapture vid 		= null;
		Mat matSample			= null;
		
		int iCAPid = getDefVidCapDriverId(0);
		
		for(int i=0; i<50; i++)
		{
			try {
				if(isIncludeSampleBase64)
				{
					matSample = new Mat();
				}
				vid = new VideoCapture(i, iCAPid);
				if(vid.isOpened())
				{
					int iWidth 	= (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
					int iHeight = (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
					int iFps 	= (int) vid.get(Videoio.CAP_PROP_FPS);
					if(matSample!=null)
					{
						vid.read(matSample);
					}
					vid.release();
					
					JSONObject jsonCam = new JSONObject();
					jsonCam.put("id", i);
					jsonCam.put("width", iWidth);
					jsonCam.put("height", iHeight);
					jsonCam.put("fps", iFps);
					if(matSample!=null && !matSample.empty())
					{
						jsonCam.put("base64", OpenCvUtil.mat2base64Img(matSample, "JPG"));
					}
					jsonArrCams.put(jsonCam);
				}
				else
				{
					break;
				}
			}
			finally
			{
				if(vid!=null)
					vid.release();
				
				if(matSample!=null)
					matSample.release();
			}
		}
			

		
		return jsonArrCams;
	}
	
	
	//////////
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		CamUtil.listLocalCameras(true);
	}
	
}
