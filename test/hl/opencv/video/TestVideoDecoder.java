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

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import hl.opencv.OpenCvLibLoader;
import hl.opencv.util.OpenCvUtil;

public class TestVideoDecoder extends VideoDecoder {
	
	@Override 
	public boolean decodedMetadata(
			String aVideoFileName, int aResWidth, int aResHeight, 
			int aFps, long aTotalFrameCount)
	{
		System.out.println();
		System.out.println("Resolution : "+aResWidth+"x"+aResHeight);
		System.out.println("FPS : "+aFps);
		System.out.println("Total frames : "+aTotalFrameCount);
		System.out.println("Duration : "+ toDurationStr(aTotalFrameCount/aFps*1000));
		System.out.println();
		return true;
	}
	
	@Override 
	public Mat decodedVideoFrame(Mat matFrame, long aFrameNo, long aFrameMs)
	{
		System.out.print("#"+aFrameNo+" - "+aFrameMs+"ms "+toDurationStr(aFrameMs));
				
		System.out.println();
		return matFrame;
	}
	
	@Override 
	public Mat skippedVideoFrame(Mat matFrame, long aFrameNo, long aFrameMs)
	{
		System.out.print("[SKIPPED] #"+aFrameNo+" - "+aFrameMs+"ms");
		
		double dBrightnessScore = OpenCvUtil.calcBrightness(matFrame, null, 100);
		System.out.print(" brightness:"+dBrightnessScore);
	
		System.out.println();
		return matFrame;
	}
	
	private static void initOpenCV()
	{
		OpenCvLibLoader cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME,"/");
		if(!cvLib.init())
		{
			throw new RuntimeException("OpenCv is NOT loaded !");
		}
	}
	
	public static int compareSimilarity(Mat currentImage, Mat compareImage) {

	    ORB orb = ORB.create();
	    int similarity = 0;

	    MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
	    MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
	    orb.detect(currentImage, keypoints1);
	    orb.detect(compareImage, keypoints2);

	    Mat descriptors1 = new Mat();
	    Mat descriptors2 = new Mat();
	    orb.compute(currentImage, keypoints1, descriptors1);
	    orb.compute(compareImage, keypoints2, descriptors2);

	    if (descriptors1.cols() == descriptors2.cols()) {
	        MatOfDMatch matchMatrix = new MatOfDMatch();
	        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
	        matcher.match(descriptors1, descriptors2, matchMatrix);
	        DMatch[] matches = matchMatrix.toArray();

	        for (DMatch match : matches)
	            if (match.distance <= 50)
	                similarity++;//from   www.java2s.com
	    }
	    return similarity;
	}
	
	public static void main(String args[]) throws Exception
	{
		initOpenCV();
		
		File folder = new File("./test/images/ace");
		
		Mat mat1 = OpenCvUtil.loadImage(folder.getAbsolutePath()+"/ace-cam08-dark-01.jpg");
		Mat mat2 = OpenCvUtil.loadImage(folder.getAbsolutePath()+"/ace-cam08-dark-02.jpg");
		
		double dSimilar = TestVideoDecoder.compareSimilarity(mat1, mat2);
		
		System.out.println(dSimilar);
		
		long lStartMs = System.currentTimeMillis();
		
		File file = new File("./test/videos/nls/XinLai.mp4");
		
		TestVideoDecoder vidDecoder = new TestVideoDecoder();
		vidDecoder.setBgref_mat(null);
		vidDecoder.setMin_brightness_skip_threshold(0.0);
		vidDecoder.setMin_similarity_skip_threshold(0.0);
		//
		vidDecoder.processVideo(file, 0, 5000);
		
		System.out.println();
		System.out.println("Elapsed : "+(System.currentTimeMillis()-lStartMs)+" ms");

		
	}
}
