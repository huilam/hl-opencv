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
import org.opencv.core.Scalar;

public class TestBrightness extends TestFileBaseProcessor {
	
	@Override 
	public void processImageFile(int aSeqNo, File aImageFile)
	{
		Mat matFile = OpenCvUtil.loadImage(aImageFile.getAbsolutePath());
		System.out.println(aSeqNo+". "+aImageFile.getName()+" ("+matFile.width()+"x"+matFile.height()+")");
		System.out.println("   - Brightness : "+calcBrightness(matFile, false));
		System.out.println("   - Brightness (Skins) : "+calcBrightness(matFile, true));
		System.out.println("   - Similarity : "+OpenCvUtil.calcImageSimilarity(matFile, matFile));
		System.out.println();
		
	}
	
	private double calcBrightness(Mat aMatImage, boolean isSkinTonesOnly)
	{
		Scalar scalarFrom 	= null;
		Scalar scalarTo 	= null;
		
		if(isSkinTonesOnly)
		{
			//H = 0-30 degree
			//S = 0-200
			//V = 20-255
			scalarFrom = new Scalar( (0 *0.5) , (0.07 *255) , 20);
			scalarTo = new Scalar( (50 *0.5), (0.80 *255) , 255); 
		}

		double dBrightness = OpenCvUtil.calcBrightness(aMatImage, scalarFrom, scalarTo);
		return dBrightness;
	}
	
	
	public static void main(String[] args)
	{

		OpenCvUtil.initOpenCV();
		
		File fileImages = new File("./test/images/ace");

		TestBrightness processor = new TestBrightness();
		processor.processFolder(fileImages);
	}
	
}
