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

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;
import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.decoder.VideoFileDecoder;

public class VideoFileUtil {

	
	public static JSONObject getVideoFileMetadata(File aVidFile)
	{
		return getVideoFileMetadata(aVidFile, false);
	}
	
	public static JSONObject getVideoFileMetadata(File aVidFile, boolean isShowPreview)
	{
		JSONObject jsonMeta = null;
		
		VideoFileDecoder vidFileDecoder = null;
		
		try{
			vidFileDecoder = new VideoFileDecoder(aVidFile);
			jsonMeta = vidFileDecoder.getVideoFileMetadata(isShowPreview);
		}
		finally
		{
			try {
				if(vidFileDecoder!=null)
					vidFileDecoder.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return jsonMeta;
	}
	
	//////////
	public static void main(String args[]) throws Exception
	{
		
		OpenCvUtil.initOpenCV();

		File[] fileVids = new File[]{
				 new File("./test/videos/crl/trimmed_10sec.mp4")
				 ,new File("./test/videos/privacy-demo-h264.mp4")
				};
		
		for(File f : fileVids)
		{
			JSONObject jsonMeta = VideoFileUtil.getVideoFileMetadata(f);
			System.out.println(f.getName()+" : ");
			System.out.println("   "+jsonMeta.toString());
			System.out.println("----------------");
			System.out.println();
		}
		
	}
	
}
