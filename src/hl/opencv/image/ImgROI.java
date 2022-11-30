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
import org.opencv.core.Rect;

import hl.opencv.util.OpenCvFilters;
import hl.opencv.util.OpenCvUtil;

public class ImgROI {
	
	private static Logger logger = Logger.getLogger(ImgSegmentation.class.getName());
	
	//
	private Rect rect_crop_roi 	= null;
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
	
	public Rect getCrop_ROI_rect() {
		return this.rect_crop_roi;
	}
	
	public void setCrop_ROI_rect(Rect aCropROIRect) {
		
		this.rect_crop_roi = aCropROIRect;
	}

	///
	
	public void extractImageROI(Mat aMatImage)
	{
		if(aMatImage!=null)
		{
			if(this.mat_roi_mask!=null && !this.mat_roi_mask.empty())
			{
				try {
					
					if(this.mat_roi_mask.width() != aMatImage.width()
					|| this.mat_roi_mask.height() != aMatImage.height())
					{
						OpenCvUtil.resize(
								this.mat_roi_mask, aMatImage.width(), aMatImage.height(), 
								false);
					}
					Core.copyTo(aMatImage, aMatImage, this.mat_roi_mask);
					
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage());
				}
			}
			
			if(this.rect_crop_roi!=null)
			{
				Mat matsub = null;
				try {
					matsub = aMatImage.submat(rect_crop_roi);
					Core.copyTo(matsub, aMatImage, new Mat());
				}
				finally
				{
					if(matsub!=null)
						matsub.release();
				}
			}
			
		}
		
	}
	
}
