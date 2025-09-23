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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.util.OpenCvUtil;

public class CamUtil {
	
	private static int DEF_VID_CAP_ID = -1;
	private static List<Integer> LIST_VID_CAP_PRIORITIES = null;
	
	public static int getDefVidCapDriverId()
	{
		return getDefVidCapDriverId(0);
	}
	
	public static void setVidCapDriverPriorities(int aVidCapDrivers[])
	{
		if(aVidCapDrivers!=null && aVidCapDrivers.length>0)
		{
			if(LIST_VID_CAP_PRIORITIES==null)
				LIST_VID_CAP_PRIORITIES = new ArrayList<Integer>();
			LIST_VID_CAP_PRIORITIES.clear();
			for(int iVidCapDriver : aVidCapDrivers)
			{
				LIST_VID_CAP_PRIORITIES.add(iVidCapDriver);
			}
		}
	}
	public static List<Integer> getVidCapDriverPriorities()
	{
		if(LIST_VID_CAP_PRIORITIES!=null)
			return LIST_VID_CAP_PRIORITIES;
		
		
		List<Integer> listDriverPriorities = new ArrayList<>();
		
		String osName = System.getProperty("os.name");
		if(osName!=null)
		{
			osName = osName.toLowerCase();
			if(osName.contains("windows"))
			{
				listDriverPriorities.add(Videoio.CAP_DSHOW);
				listDriverPriorities.add(Videoio.CAP_MSMF);
			}
			else if(osName.contains("linux"))
			{
				listDriverPriorities.add(Videoio.CAP_V4L2);
			}
			else if(osName.contains("mac") || osName.contains("darwin"))
			{
				listDriverPriorities.add(Videoio.CAP_AVFOUNDATION);
			}
		}
		listDriverPriorities.add(Videoio.CAP_FFMPEG);
		listDriverPriorities.add(Videoio.CAP_ANY);
		
		LIST_VID_CAP_PRIORITIES = listDriverPriorities;
		
		return LIST_VID_CAP_PRIORITIES;
	}
	
	public static int getDefVidCapDriverId(int aDeviceId)
	{
		if(DEF_VID_CAP_ID<0)
		{
			VideoCapture vid = null;
			try {
				
				for(Integer iCAP_ID : getVidCapDriverPriorities())
				{
					String sCapName = "CAP_"+iCAP_ID;
					switch(iCAP_ID)
					{
						case Videoio.CAP_ANY: 
							sCapName = "CAP_ANY";
							break;
						case Videoio.CAP_DSHOW: 
							sCapName = "CAP_DSHOW";
							break;
						case Videoio.CAP_MSMF: 
							sCapName = "CAP_MSMF";
							break;
						case Videoio.CAP_FFMPEG: 
							sCapName = "CAP_FFMPEG";
							break;
						case Videoio.CAP_AVFOUNDATION: 
							sCapName = "CAP_AVFOUNDATION";
							break;
						case Videoio.CAP_V4L2: 
							sCapName = "CAP_V4L2";
							break;
						case Videoio.CAP_GSTREAMER: 
							sCapName = "CAP_GSTREAMER";
							break;
					}
					
					System.out.print("Trying "+sCapName+" ("+iCAP_ID+")");
					
					vid = new VideoCapture(aDeviceId, iCAP_ID);
					if(vid.isOpened())
					{
						DEF_VID_CAP_ID = iCAP_ID;
						System.out.println(" Success !");
						break;
					}
					else
					{
						if(vid!=null)
						{
							vid.release();
						}
						System.out.println();
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
		}
		
		if(DEF_VID_CAP_ID==-1)
		{
			System.out.print("NO Capture Driver Available !");
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
