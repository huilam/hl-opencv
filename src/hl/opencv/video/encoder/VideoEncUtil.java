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

public class VideoEncUtil {

	// Requires https://github.com/sannies/mp4parser
	
	public static String HANDLER_VIDEO = "vide";
	public static String HANDLER_AUDIO = "audi";
	
	protected static Movie mergeMp4(File[] aVidFiles) throws IOException
	{
		Movie combinedVid = new Movie();
		List<Track> listVidTrack = new LinkedList<Track>();
		List<Track> listAudTrack = new LinkedList<Track>();
		
		for(File f : aVidFiles)
		{
			Movie m = MovieCreator.build(f.getAbsolutePath());
			for(Track t: m.getTracks())
			{
				//TrackMetaData tMeta = t.getTrackMetaData();
				//System.out.println("[add] "+t.getHandler()+" = "+t.getDuration()+" resolution="+tMeta.getWidth()+"x"+tMeta.getHeight());
				
				if(HANDLER_VIDEO.equals(t.getHandler()))
				{
					listVidTrack.add(t);
				}
				else if(HANDLER_AUDIO.equals(t.getHandler()))
				{
					listAudTrack.add(t);
				}
					
			}
		}
		
		if(listVidTrack.size()>0)
		{
			AppendTrack combinedVidTracks = new AppendTrack(listVidTrack.toArray(new Track[listVidTrack.size()]));
			combinedVid.addTrack(combinedVidTracks);
		}
		
		if(listAudTrack.size()>0)
		{
			AppendTrack combinedAudTracks = new AppendTrack(listAudTrack.toArray(new Track[listAudTrack.size()]));
			combinedVid.addTrack(combinedAudTracks);
		}
		
		return combinedVid;
	}
	
	protected static Movie[] splitMp4(File aVidFile) throws IOException
	{
		List<Movie> listSplitVids = new LinkedList<Movie>();
		
		File f = aVidFile;

		Movie m = MovieCreator.build(f.getAbsolutePath());
		for(Track t: m.getTracks())
		{
			//TrackMetaData tMeta = t.getTrackMetaData();
			//System.out.println("[add] "+t.getHandler()+" = "+t.getDuration()+" resolution="+tMeta.getWidth()+"x"+tMeta.getHeight());
			
			if(HANDLER_VIDEO.equals(t.getHandler()))
			{

			}	
		}
		
		return listSplitVids.toArray(new Movie[listSplitVids.size()]);
	}
	
	protected static boolean writeMp4ToFile(Movie mp4Movie, File aOutputFile)
	{
		//DefaultMp4Builder
		//FragmentedMp4Builder
		FragmentedMp4Builder mp4Builder = new FragmentedMp4Builder();
		mp4Builder.setFragmenter(new DefaultFragmenterImpl(2));
		Container mp4Container = mp4Builder.build(mp4Movie);
		
		FileOutputStream fs = null;
		FileChannel fc = null;
		try
		{
			fs = new FileOutputStream(aOutputFile);
			fc = fs.getChannel();
			mp4Container.writeContainer(fc);
			return true;
		}
		catch(IOException ex)
		{
			return false;
		}
		finally
		{
			if(fs!=null)
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			
			if(fc!=null)
				try {
					fc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	

	protected static JSONArray listCameras(boolean isIncludeSampleBase64)
	{
		JSONArray jsonArrCams 	= new JSONArray();
		VideoCapture vid 		= null;
		Mat matSample			= null;
		for(int i=0; i<50; i++)
		{
			try {
				if(isIncludeSampleBase64)
				{
					matSample = new Mat();
				}
				vid = new VideoCapture(i);
				if(vid.isOpened())
				{
					int iWidth 	= (int) vid.get(Videoio.CAP_PROP_FRAME_WIDTH);
					int iHeight = (int) vid.get(Videoio.CAP_PROP_FRAME_HEIGHT);
					int iFps 	= (int) vid.get(Videoio.CAP_PROP_FPS);
					if(matSample!=null)
					{
						vid.retrieve(matSample);
					}
					vid.release();
					
					JSONObject jsonCam = new JSONObject();
					jsonCam.put("id", i);
					jsonCam.put("width", iWidth);
					jsonCam.put("height", iHeight);
					jsonCam.put("fps", iFps);
					if(matSample!=null)
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

		File[] fileVids = new File[]{
				 new File("./test/videos/privacy-demo-h264.mp4")
				 ,new File("./test/videos/crl/trimmed_10sec.mp4")
				
				};
		
		File fileOutput = new File("./test/videos/combined_"+System.currentTimeMillis()+".mp4");
		
		/////////////
		Movie combinedMp4 = mergeMp4(fileVids);
		writeMp4ToFile(combinedMp4, fileOutput);
		
		
	}
	
}
