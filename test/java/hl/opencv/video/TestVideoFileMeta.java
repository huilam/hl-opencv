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

package hl.opencv.video;

import java.io.File;

import org.json.JSONObject;

import hl.common.FileUtil;
import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.decoder.VideoFileDecoder;
import hl.opencv.video.utils.VideoFileUtil;

public class TestVideoFileMeta {
	
	public static void main(String args[]) throws Exception
	{
		testVideoMetaData();
	}
	
	public static void testVideoMetaData() throws Exception
	{
		OpenCvUtil.initOpenCV();
		
		File imgFolder = new File("./test/videos/");
		
		File[] files = 
				FileUtil.getFilesWithExtensions(imgFolder, new String[] {"mp4"});
		
		int i = 0;
		for(File fileImg : files)
		{
			
			JSONObject jsonMetaTest = VideoFileUtil.getVideoFileMetadata(fileImg);
			System.out.println("jsonMetaTest= "+jsonMetaTest);
			System.out.println();
			
			VideoFileDecoder decoder = new VideoFileDecoder(fileImg);
			
			JSONObject jsonMeta = decoder.getVideoFileMetadata();
			
			System.out.println((++i)+". "+fileImg.getName());
			
			if(jsonMeta==null)
			{
				System.out.println("   - Video Meta is NULL");
			}
			else
			{
				System.out.println("   - Videoio.CAP_PROP_FRAME_COUNT:"+jsonMeta.optLong("Videoio.CAP_PROP_FRAME_COUNT"));
				System.out.println("   - Videoio.CAP_PROP_FPS:"+jsonMeta.optDouble("Videoio.CAP_PROP_FPS"));
				System.out.println("   - Videoio.CAP_PROP_FRAME_WIDTH:"+jsonMeta.optLong("Videoio.CAP_PROP_FRAME_WIDTH"));
				System.out.println("   - Videoio.CAP_PROP_FRAME_HEIGHT:"+jsonMeta.optLong("Videoio.CAP_PROP_FRAME_HEIGHT"));
				
				JSONObject jsonMeta2 = decoder.getVideoFileMetadata(true);
				System.out.println("   - Videoio.jsonMeta2.FRAME_COUNT:"+jsonMeta2.optLong("FRAME_COUNT"));
				
				JSONObject jsonMeta3 = decoder.getVideoFileMetadata(true);
				System.out.println("   - Videoio.jsonMeta3.PREVIEW_FRAMES:"+jsonMeta3.optJSONObject("PREVIEW_FRAMES"));
				
				JSONObject jsonMeta4 = decoder.getVideoFileMetadata();
				System.out.println("   - Videoio.jsonMeta4.PREVIEW_FRAMES:"+jsonMeta4.optJSONObject("PREVIEW_FRAMES"));

			}
			
			decoder.close();
		}
		
	}
		
}
