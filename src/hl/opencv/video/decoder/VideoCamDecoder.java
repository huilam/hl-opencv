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

package hl.opencv.video.decoder;

import java.util.logging.Logger;

import org.json.JSONObject;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.video.utils.CamUtil;

public class VideoCamDecoder extends VideoCaptureDecoder {
	
	private static Logger logger = Logger.getLogger(VideoCamDecoder.class.getName());
	private int cam_id 			= -1;
	//
	private int cam_res_width 	= -1;
	private int cam_res_height 	= -1;
	private double cam_fps 		= 0;
	private int[] vidcap_priorities = CamUtil.VID_CAP_PRIORITIES;
	
	//
	public VideoCamDecoder(int aCapDeviceID)
	{
		initCamera(aCapDeviceID, vidcap_priorities);
	}
	
	public VideoCamDecoder(int aCapDeviceID, int aApiPreference)
	{
		initCamera(aCapDeviceID, vidcap_priorities);
	}
	
	private void initCamera(int aCapDeviceID, int[] aVidPriorities)
	{
		this.cam_id = aCapDeviceID;
		
		int iVidCapDriverId = CamUtil.getDefVidCapDriverId(aCapDeviceID);
		
		VideoCapture vid = new VideoCapture(aCapDeviceID, iVidCapDriverId);
		super.setVideoCapture(vid);
		//
		String sCapSourceName = String.valueOf(aCapDeviceID);
		super.setVideoCaptureName(sCapSourceName);
	}
	
	public int getCamDeviceId()
	{
		return this.cam_id;
	}
	
	
	public boolean setCamResAndFPS(Size aResolution, double aFps)
	{
		boolean isFpsOk = setCamFPS(aFps);
		boolean isResOk = setCamResolution(aResolution);
		
		return isFpsOk && isResOk;
	}
	
	public boolean setCamFPS(double aFps)
	{
		VideoCapture vid = super.getVideoCapture();
		if(vid!=null && aFps>-1)
		{
			vid.set(Videoio.CAP_PROP_FPS, aFps);
			if(vid.get(Videoio.CAP_PROP_FPS)==aFps)
			{
				this.cam_fps = aFps;
				return true;
			}
		}
		return false;
	}
	
	public boolean setCamResolution(Size aResolution)
	{
		VideoCapture vid = super.getVideoCapture();
		if(vid!=null && aResolution!=null)
		{
			int iOrgWidth 	= (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
			int iOrgHeight 	= (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
			
			boolean isSetWidthOk 	= setCamResWidth((int)aResolution.width);
			boolean isSetHeightOk 	= setCamResHeight((int)aResolution.height);
			
			boolean isSetNewResOk = isSetWidthOk && isSetHeightOk;
			
			if(!isSetNewResOk)
			{
				vid.set(Videoio.CAP_PROP_FRAME_WIDTH, iOrgWidth);
				vid.set(Videoio.CAP_PROP_FRAME_HEIGHT, iOrgHeight);
			}
			
			return isSetNewResOk;
		}
		return false;
	}
	
	public boolean setCamResWidth(int aWidth)
	{
		VideoCapture vid = super.getVideoCapture();
		if(vid!=null && aWidth>-1)
		{
			vid.set(Videoio.CAP_PROP_FRAME_WIDTH, aWidth);
				
			if((vid.get(Videoio.CAP_PROP_FRAME_WIDTH)==aWidth))
			{
				this.cam_res_width = aWidth;
				//
				this.cam_res_height = (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				return true;
			}
			
		}
		return false;
	}
	
	public boolean setCamResHeight(int aHeight)
	{
		VideoCapture vid = super.getVideoCapture();
		if(vid!=null && aHeight>-1)
		{
			vid.set(Videoio.CAP_PROP_FRAME_HEIGHT, aHeight);
				
			if((vid.get(Videoio.CAP_PROP_FRAME_HEIGHT)==aHeight))
			{
				this.cam_res_height = aHeight;
				//
				this.cam_res_width = (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
				return true;
			}
			
		}
		return false;
	}
	
	public double getCamFPS()
	{
		return this.cam_fps;
	}
	
	public Size getCamResolution()
	{
		return new Size(this.cam_res_width, this.cam_res_height);
	}
	
	
	public JSONObject getCameraMetadata()
	{
		return getCameraMetadata(false, 0);
	}
	
	public JSONObject getCameraMetadata(boolean isShowPreview)
	{
		return getCameraMetadata(isShowPreview, 0);
	}
	
	public JSONObject getCameraMetadata(boolean isShowPreview, int aPreviewWidth)
	{
		return super.getVidCapMetadata(isShowPreview, aPreviewWidth);
	}
	
	public JSONObject processCamera()
	{
		return processCamera(-1);
	}
	
	public JSONObject processCamera(final long aSelectedTimestampTo)
	{
		return super.processVideo(0, aSelectedTimestampTo);
	}
	
	public void release()
	{
		super.release();
		this.cam_id  = -1;
		this.cam_fps = 0d;
	}

}
