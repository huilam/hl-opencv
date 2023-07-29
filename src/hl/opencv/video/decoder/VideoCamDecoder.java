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
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class VideoCamDecoder extends VideoCaptureDecoder {
	
	private static Logger logger = Logger.getLogger(VideoCamDecoder.class.getName());
	private int cam_id 	= -1;
	private int cam_fps = -1;
	
	public VideoCamDecoder(int aCapDeviceID)
	{
		initCamera(aCapDeviceID);
	}
	
	private void initCamera(int aCapDeviceID)
	{
		this.cam_id = aCapDeviceID;
		VideoCapture vid = new VideoCapture(aCapDeviceID);
		super.setVideoCapture(vid);
		//
		String sCapSourceName = String.valueOf(aCapDeviceID);
		super.setVideoCaptureName(sCapSourceName);
	}
	
	public int getCamDeviceId()
	{
		return this.cam_id;
	}
	
	public void setCamFps(int aFps)
	{
		this.cam_fps = aFps;
	}
	
	public int getCamFps()
	{
		return this.cam_fps;
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
	
	public long processCamera()
	{
		return processCamera(-1);
	}
	
	public long processCamera(final long aSelectedTimestampTo)
	{
		if(getCamFps()>0)
		{
			super.getVideoCapture().set(Videoio.CAP_PROP_FPS, getCamFps());
		}
		return super.processVideo(0, aSelectedTimestampTo);
	}

}
