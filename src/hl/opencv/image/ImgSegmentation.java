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

package hl.opencv.image;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import hl.opencv.util.OpenCvUtil;

public class ImgSegmentation {
	
	private static Logger logger = Logger.getLogger(ImgSegmentation.class.getName());
	
	//
	private Mat background_ref_mat 			= null;
	private double min_bgref_threshold 		= 0.18;
	//
	
	public ImgSegmentation()
	{
		this.background_ref_mat = null;
	}
	
	public Mat getBackground_ref_mat() {
		return background_ref_mat;
	}
	public void setBackground_ref_mat(Mat background_ref_mat) {
		this.background_ref_mat = background_ref_mat;
	}
	////
	public double getMin_bgref_threshold() {
		return min_bgref_threshold;
	}
	public void setMin_bgref_threshold(double min_bgref_threshold) {
		this.min_bgref_threshold = min_bgref_threshold;
	}
	////
	
	public Mat extractForeground(Mat aMatImage)
	{
		Mat matForeground 	= aMatImage;
		
		if(aMatImage!=null)
		{
			if(this.background_ref_mat!=null && !this.background_ref_mat.empty())
			{
				try {
					Mat matMask 	= null;
					
					try {
						matMask = OpenCvUtil.extractFGMask(
										aMatImage, 
										this.background_ref_mat, 
										this.min_bgref_threshold);
						
						if(matMask!=null)
						{
							matForeground = new Mat();
							Core.copyTo(aMatImage, matForeground, matMask);
						}
					}
					finally
					{
						if(matMask!=null)
							matMask.release();
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage());
					return null;
				}
			}
		}
		return matForeground;
	}
	
}
