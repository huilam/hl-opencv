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

public interface IVideoProcessorPlugin {
	
	public boolean initPlugin(JSONObject aMetaJson);
	
	public boolean processStarted(String aVideoSourceName, 
			long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, int aResWidth, int aResHeight, 
			long aTotalSelectedFrames, double aFps, long aSelectedDurationMs);
	
	public Mat decodedVideoFrame(String aVideoSourceName, Mat matFrame, 
			long aCurFrameIdx, long aCurFrameMs, double aProgressPercentage);
	
	public Mat skippedVideoFrame(String aVideoSourceName, Mat matFrame, 
			long aCurFrameIdx, long aCurFrameMs, double aProgressPercentage, String aReason, double aScore);
	
	public Mat processAborted(String aVideoSourceName, Mat matFrame, 
			long aCurFrameIdx, long aCurFrameMs,  double aProgressPercentage, String aReason);
	
	public JSONObject processEnded(String aVideoSourceName, long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo, 
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs);
	
	public void destroyPlugin(JSONObject aMetaJson);
}
