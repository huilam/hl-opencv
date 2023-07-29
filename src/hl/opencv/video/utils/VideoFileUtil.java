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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultFragmenterImpl;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.decoder.VideoCaptureDecoder;
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
			if(vidFileDecoder!=null)
				vidFileDecoder.release();
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
