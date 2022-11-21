/*
 Copyright (c) 2022 onghuilam@gmail.com
 
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

import hl.opencv.util.OpenCvFilters;
import hl.opencv.util.OpenCvUtil;

public class ImgROI {
	
	private static Logger logger = Logger.getLogger(ImgSegmentation.class.getName());
	
	//
	private Mat mat_roi_mask = null;
	//
	
	public ImgROI()
	{
		this.mat_roi_mask = null;
	}
	
	public Mat getROI_mask() {
		return this.mat_roi_mask;
	}
	public void setROI_mask(Mat aROIMask) {
		
		if(aROIMask!=null)
		{
			if(aROIMask.channels()!=1)
			{
				aROIMask = OpenCvFilters.toMask(aROIMask);
			}
			
			this.mat_roi_mask = aROIMask;
		}
	}

	///
	
	public Mat getImageROI(Mat aMatImage)
	{
		Mat matROI = null;
		if(aMatImage!=null)
		{
			if(this.mat_roi_mask!=null && !this.mat_roi_mask.empty())
			{
				try {
					
					if(this.mat_roi_mask.width() != aMatImage.width()
					|| this.mat_roi_mask.height() != aMatImage.height())
					{
						this.mat_roi_mask = OpenCvUtil.resize(
								this.mat_roi_mask, aMatImage.width(), aMatImage.height(), 
								false);
					}
					
					matROI = new Mat();
					Core.copyTo(aMatImage, matROI, this.mat_roi_mask);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage());
					return aMatImage;
				}
			}
		}
		
		if(matROI!=null)
			return matROI;
		else
			return aMatImage;
	}
	
}
