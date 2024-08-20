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

package hl.opencv.video.encoder;

import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

public class VideoEncoder {
	
	private static Logger logger = Logger.getLogger(VideoEncoder.class.getName());
	private static String DEF_ENCODING_FORMAT = "h264";
	
	private String video_file_name 		= null;
	private String encoding_format 		= DEF_ENCODING_FORMAT;
	private Size encoding_resolution 	= null;
	
	private VideoWriter videoWriter = null;
	
	public VideoEncoder(String aEncodingFormat, int aResWidth, int aResHeight)
	{
		init(aEncodingFormat, aResWidth, aResHeight);
		
	}
	public VideoEncoder(int aResWidth, int aResHeight)
	{
		init(this.encoding_format, aResWidth, aResHeight);
	}
	
	private void init(String aEncodingFormat, int aResWidth, int aResHeight)
	{
		setEncodingFormat(aEncodingFormat);
		setResolution(aResWidth, aResHeight);
		videoWriter = new VideoWriter();
	}
	
	
	public void setEncodingFormat(String aEncodingFormat)
	{
		this.encoding_format = aEncodingFormat;
	}
	
	public void setOutputFilename(String aOutputFileName)
	{
		this.video_file_name = aOutputFileName;
	}
	
	public String getOutputFilename()
	{
		return this.video_file_name;
	}
	
	public void setResolution(int iWidth, int iHeight)
	{
		this.encoding_resolution = new Size(iWidth, iHeight);
	}
	
	
	////
	public boolean startEncoding(int aFps) throws Exception
	{
		StringBuffer sbError = new StringBuffer();
		
		if(encoding_format==null)
		{
			sbError.append("Invalid encoding format : ").append(encoding_format);
		}
		if(video_file_name==null)
		{
			sbError.append("Invalid video filename : ").append(video_file_name);
		}
		if(encoding_resolution==null)
		{
			sbError.append("Invalid resolution : ").append(encoding_resolution);
		}
		if(aFps<0)
		{
			sbError.append("Invalid fps : ").append(aFps);
		}
		
		///
		if(sbError.length()>0)
		{
			throw new Exception(sbError.toString());
		}
		/////////////
		
		int iEncodeFormatFourCC = VideoWriter.fourcc(
				encoding_format.charAt(0), encoding_format.charAt(1),
				encoding_format.charAt(2), encoding_format.charAt(3));
		
		videoWriter.open(video_file_name, iEncodeFormatFourCC, 
				aFps, encoding_resolution, true);
		
		return (videoWriter.isOpened());
	}
	
	public boolean encodeFrame(Mat aMatFrame)
	{	
		if(videoWriter.isOpened())
		{
			videoWriter.write(aMatFrame);
		}
		return true;
	}
	
	public boolean endEncoding()
	{
		if(videoWriter!=null)
		{
			videoWriter.release();
			return true;
		}
		return false;	
	}
	
	//////////
	public static void main(String args[]) throws Exception
	{
	}
	
}
