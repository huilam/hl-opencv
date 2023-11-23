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

package hl.opencv.util;

import java.io.File;

import org.opencv.core.Mat;

public class TestSimilarity {
	
	public static void main(String[] args)
	{
		OpenCvUtil.initOpenCV();
		
		File fileImages = new File("./test/images/xinlai");

		Mat mat1 = OpenCvUtil.loadImage(fileImages.getAbsolutePath()+"/xinlai_02.jpg");
		Mat mat2 = OpenCvUtil.loadImage(fileImages.getAbsolutePath()+"/xinlai_03.jpg");
		
		System.out.println("mat1="+mat1.width()+"x"+mat1.height());
		System.out.println("mat2="+mat2.width()+"x"+mat2.height());
		
		System.out.println(OpenCvUtil.calcImageSimilarity(mat1, mat2));
	}
	
}
