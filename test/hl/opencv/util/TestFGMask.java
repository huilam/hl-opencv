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

import hl.opencv.image.ImageProcessor;

public class TestFGMask extends TestFileBaseProcessor {
	
	private static ImageProcessor imgProcessor = null;
	private static File fileImageOutput = null;
	
	@Override 
	public void processImageFile(int aSeqNo, File aImageFile)
	{
		Mat matFile = OpenCvUtil.loadImage(aImageFile.getAbsolutePath());
		System.out.println(" - "+aImageFile.getName()+" : "+matFile.width()+"x"+matFile.height());
		
		if(imgProcessor.processImage(matFile))
		{
			String sOutputFileName = fileImageOutput.getAbsolutePath()+"/"+aImageFile.getName();
			System.out.println(sOutputFileName);
			OpenCvUtil.saveImageAsFile(matFile, sOutputFileName+"_01.jpg");
		}
	}
	
	public static void main(String[] args)
	{
		OpenCvUtil.initOpenCV();
		
		File fileImages = new File("./test/images/b01/input");
		
		fileImageOutput = new File("./test/images/b01/output");
		fileImageOutput.mkdirs();
		
		File fileBgImage = new File("./test/images/b01/background.jpg");
		
		Mat matBgRefImage = OpenCvUtil.loadImage(fileBgImage.getAbsolutePath());
		
		imgProcessor = new ImageProcessor();
		imgProcessor.setBackground_ref_mat(matBgRefImage);
		
		TestFileBaseProcessor processor = new TestFGMask();
		
		processor.processFolder(fileImages);
	}
	
}
