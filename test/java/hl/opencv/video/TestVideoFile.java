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

import org.opencv.core.Mat;

import hl.common.FileUtil;
import hl.opencv.util.OpenCvUtil;

public class TestVideoFile {
	
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		
		File[] videoFIles = FileUtil.getFilesWithExtensions(new File("./test/videos/"), 
				new String[] {".mp4"});
		
		for(File vid : videoFIles)
		{
			TestVideoDecoder vidDecoder = new TestVideoDecoder(vid);
			vidDecoder.fileOutput = new File(vid.getParentFile().getAbsolutePath()+"/output");
			vidDecoder.setBgref_mat(null);
			vidDecoder.close();
			
			//
			File fileROIMask = new File(vid.getParentFile().getAbsolutePath()+"/mask-test.jpg");
			if(fileROIMask.isFile())
			{
				Mat matROImask = OpenCvUtil.loadImage(fileROIMask.getAbsolutePath());
				vidDecoder.setROI_mat(matROImask);
			}
			
			//vidDecoder.setCrop_ROI_rect(new org.opencv.core.Rect(100,100,10,10));
			//
			vidDecoder.setMin_brightness_skip_threshold(0);
			vidDecoder.setMax_brightness_calc_width(200);
			//
			vidDecoder.setMin_similarity_skip_threshold(0);
			vidDecoder.setMax_similarity_compare_width(500);
			//
			vidDecoder.processVideoFile(0, 1000);
			
		}
	}
		
}
