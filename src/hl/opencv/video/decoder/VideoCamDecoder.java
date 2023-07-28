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

public class VideoCamDecoder extends VideoCaptureDecoder {
	
	private static Logger logger = Logger.getLogger(VideoCamDecoder.class.getName());
	private int capture_id = -1;
	
	public VideoCamDecoder(int aCapDeviceID)
	{
		VideoCapture vid = new VideoCapture(this.capture_id);
		super.setVideoCapture(vid);
		this.capture_id = aCapDeviceID;
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
		return super.processVideoCap(String.valueOf(this.capture_id), 0, aSelectedTimestampTo);
	}

}
