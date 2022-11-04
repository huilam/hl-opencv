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
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;
import hl.opencv.OpenCvLibLoader;

public class TestBrightness2{

	private static String POIAPI_DETECT_URL = "http://203.127.252.55/scc/poiapi/human/detect";
	private static String POIAPI_SEARCH_URL = "http://203.127.252.55/scc/poiapi/face/search";
	
	private static String OUTPUT_FOLDER = null;
	
	private static void initOpenCV()
	{
		OpenCvLibLoader cvLib = new OpenCvLibLoader(Core.NATIVE_LIBRARY_NAME,"/");
		if(!cvLib.init())
		{
			throw new RuntimeException("OpenCv is NOT loaded !");
		}
	}
	
	private static Mat resizeImageToFaceWidth(Mat aMatFace, double aTargetFaceWidth)
	{
		if(aMatFace==null || aTargetFaceWidth<=0)
			return null;
		
		int iFaceWidth = getFaceWidth(aMatFace);
		
		if(iFaceWidth>0)
		{
			double dResizeScale = 1.0;
			if(iFaceWidth>1)
			{
				dResizeScale = aTargetFaceWidth / iFaceWidth;
			}
			
			if(dResizeScale!=1.0)
			{
				int iWidth = (int) (aMatFace.width() * dResizeScale);
				int iHeight = (int) (aMatFace.height() * dResizeScale);
				
				return OpenCvUtil.resize(aMatFace, iWidth, iHeight, true);
			}
		}
		return null;
	}
	
	private static int getFaceWidth(Mat aMatFace)
	{
		if(aMatFace==null)
			return -1;
		
		JSONArray jArrFaces = detectFaces(aMatFace); 
		if(jArrFaces!=null && jArrFaces.length()>0)
		{
			JSONObject jsonPerson = jArrFaces.optJSONObject(0);
			
			JSONObject jsonFace = jsonPerson.optJSONObject("face", null);
			if(jsonFace!=null)
			{
				int iWidth = jsonFace.optInt("width", -2);
				if(iWidth>0)
					return iWidth;
				System.out.println("jsonFace="+jsonFace.toString());
			}
			else
			{
				System.out.println("jsonPerson="+jsonPerson);
			}
		}
		else
		{
			System.out.println("jArrFaces="+jArrFaces);
		}
		
		return -3;
	}
	
	private static double calcFaceBrightness(Mat aMatFace)
	{
		Scalar scalarFrom = new Scalar( (0 *0.5) , (0.07 *255) , 20);
		Scalar scalarTo = new Scalar( (50 *0.5), (0.80 *255) , 255); 
		double dFaceBrightness = OpenCvUtil.calcBrightness(aMatFace, scalarFrom, scalarTo);
		
		return dFaceBrightness;
		
	}
	
	private static JSONArray detectFaces(final Mat matInput)
	{	
		JSONArray jarrOutput = new JSONArray();
		
		String sJpgBase64 = OpenCvUtil.mat2base64Img(matInput, "JPG");
		
		JSONObject jsonInput = new JSONObject();
		jsonInput.put("image", sJpgBase64);
		
		try {
			
			HttpResp resp = RestApiUtil.httpPost(POIAPI_DETECT_URL, "application/json", jsonInput.toString());
			
			if(resp.isSuccess())
			{
				JSONObject jsonDetects = new JSONObject(resp.getContent_data());
				
				//System.out.println("##### "+jsonDetects.toString());
				
				JSONArray jArrPersons = jsonDetects.optJSONArray("detections");
				
				for(int i=0; i<jArrPersons.length(); i++)
				{
					JSONObject jsonPerson = jArrPersons.getJSONObject(i);
					
					if(jsonPerson!=null)
					{
						//System.out.println(jsonPerson.toString());
						
						JSONObject jsonRegion = new JSONObject();
						
						
						JSONObject jsonHead = jsonPerson.optJSONObject("head", null);
						JSONObject jsonface = jsonPerson.optJSONObject("face", null);
						
						if(jsonHead!=null)
						{
							jsonRegion.put("head", jsonHead.optJSONObject("headRegion"));
						}
						
						if(jsonface!=null)
						{
							JSONObject jsonFaceRegion = jsonface.optJSONObject("faceRegion");
							
							JSONObject jsonEyes = jsonface.optJSONObject("eyes", null);
							
							if(jsonEyes!=null)
							{
								JSONObject jsonLeftEyePos = jsonEyes.optJSONObject("leftEye").optJSONObject("position");
								JSONObject jsonRightEyePos = jsonEyes.optJSONObject("rightEye").optJSONObject("position");
								
								int iLeftX = jsonLeftEyePos.optInt("left");
								int iLeftY = jsonLeftEyePos.optInt("top");
								
								int iRightX = jsonRightEyePos.optInt("left");
								int iRightY = jsonRightEyePos.optInt("top");
								
								int iEyeDistance = Math.abs(iLeftX-iRightX);
								if(iLeftY!=iRightY)
								{
									iEyeDistance = (int) Math.hypot(Math.abs(iRightX-iLeftX), Math.abs(iRightY-iLeftY));
								}
								
								jsonFaceRegion.put("eyeDistance", iEyeDistance);
								jsonFaceRegion.put("leftEye", jsonLeftEyePos);
								jsonFaceRegion.put("rightEye", jsonRightEyePos);
							}
							
							jsonRegion.put("face", jsonFaceRegion);
						}
						
						if(jsonRegion!=null)
						{
							
							jarrOutput.put(jsonRegion);
						}
					}
				}
			
				return jarrOutput;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(jarrOutput.toString());
		
		return jarrOutput;
	}
	
	private static JSONObject poiMatching(Mat matFace)
	{	
		JSONObject jsonOutput = new JSONObject();
		
		String sJpgBase64 = OpenCvUtil.mat2base64Img(matFace, "JPG");
		
		try {
			
			JSONObject jsonInput = new JSONObject();
			jsonInput.put("image", sJpgBase64);
			jsonInput.put("searchProfileName", "SIT_SEARCH_PROFILE");

			HttpResp resp = RestApiUtil.httpPost(POIAPI_SEARCH_URL, "application/json", jsonInput.toString());
			
			//System.out.println(POIAPI_SEARCH_URL+" - "+resp.getHttp_status());
			if(resp.isSuccess())
			{
				jsonOutput = new JSONObject(resp.getContent_data());
				return jsonOutput;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonOutput;
	}
	
	private static boolean processImage(File f)
	{
		boolean isProcessed = false;
		
		if(f!=null && f.isFile())
		{
			String sOutputFolder = OUTPUT_FOLDER;
			
			new File(sOutputFolder).mkdirs();
			
			String sFileName = f.getName().toLowerCase();
			
			if(sFileName.endsWith(".jpg") || sFileName.endsWith(".png"))
			{
				isProcessed = true;
				System.out.print("* Loading "+f.getName()+" ...");
				Mat matOrg = OpenCvUtil.loadImage(f.getAbsolutePath());
				System.out.println("  - "+matOrg.width()+"x"+matOrg.height());
			
				double dBrightnessScore1 = calcImageBrightness(matOrg, true);
				double dBrightnessScore2 = calcImageBrightness(matOrg, false);
				
				/**
				
				int iTargetWidth[] = new int[] {30, 34, 90}; //, 30, 20, 50, -10, -20, -50};
				
				int iOrgFaceWidth = getFaceWidth(matOrg);
				System.out.println(" - Org FaceWidth = "+iOrgFaceWidth);
				
				
				for(int iWidth : iTargetWidth)
				{
					System.out.println(" - Resizing face width to "+iWidth+" ...");
					
					Mat matResized = resizeImageToFaceWidth(matOrg, iWidth);
					
					if(matResized!=null)
					{
						int iResizedImgFaceWidth = getFaceWidth(matResized);
						System.out.println("   - Resized = "+matResized.width()+"x"+matResized.height()+" faceWidth:"+iResizedImgFaceWidth);
					
						OpenCvUtil.saveImageAsFile(matResized, sOutputFolder+"/"+f.getName()+"_"+iResizedImgFaceWidth+"_"+matResized.width()+"x"+matResized.height()+".jpg");
					}
					else
					{
						System.out.println("   - Skipped");
					}
				}
				
				**/
				
				System.out.println();
			}
		}
		return isProcessed;
	}
	
	private static boolean processVideo(File f)
	{
		boolean isProcessed = false;
		if(f!=null && f.isFile())
		{
			String sFileName = f.getName().toLowerCase();
			
			if(sFileName.endsWith(".mp4") || sFileName.endsWith(".mkv"))
			{
				isProcessed = true;
				String sVidFileName = f.getAbsolutePath();
				
				System.out.println(sVidFileName);
				
				VideoCapture vid = null;
				
				try{
					Mat matFrame = new Mat();
					vid = new VideoCapture(sVidFileName);
					if(vid.isOpened())
					{
						double dFps = vid.get(Videoio.CAP_PROP_FPS);
						double dTotalFrames = vid.get(Videoio.CAP_PROP_FRAME_COUNT);

						System.out.println("Total Frames = "+dTotalFrames);
						System.out.println("FPS = "+dFps);
						
						double dDuration = dTotalFrames/dFps;
						
						System.out.println("Duration (secs) = "+dDuration);
						
						vid.read(matFrame);
						System.out.println("read WxH = "+matFrame.width()+"x"+matFrame.height());
					}
				}finally
				{
					vid.release();
				}
			}
		}
		return isProcessed;
	}
	
	private static int processFiles(File folderImages, boolean isRecursive)
	{
		int iCount = 0;
		for(File f : folderImages.listFiles())
		{
			if(f.isFile())
			{
				if(processVideo(f))
				{
					iCount ++; 
				}
				else if(processImage(f))
				{
					iCount ++; 
				}
				
			}
			else if(isRecursive && f.isDirectory())
			{
				if(OUTPUT_FOLDER.equals(f.getAbsolutePath()))
				{
					System.out.println(" ** [SKIP] "+f.getAbsolutePath());
					continue;
					//skip output folder
				}
				iCount += processFiles(f, isRecursive);
			}
			
		}
		return iCount;
	}
	

	private static double calcImageBrightness(Mat aMaImage, boolean isTargetSkins)
	{
		Scalar scalarFrom 	= null;
		Scalar scalarTo 	= null; 
		
		if(isTargetSkins)
		{
			scalarFrom = new Scalar( (0 *0.5) , (0.07 *255) , 20);
			scalarTo = new Scalar( (50 *0.5), (0.80 *255) , 255); 
		}
		
		
		double dFaceBrightness = OpenCvUtil.calcBrightness(aMaImage, scalarFrom, scalarTo);
		System.out.println("isTargetSkins="+isTargetSkins);
		System.out.println("dFaceBrightness="+dFaceBrightness);
		return dFaceBrightness;
	}
	
	
	public static void main(String[] args) throws Exception
	{
		initOpenCV();
		System.out.println();
		
		File folderImages = new File("./test/images/sunn");
		
		OUTPUT_FOLDER = folderImages.getAbsolutePath()+"/output";
		
		boolean isRecursive = true;
		
		int iCount = processFiles(folderImages, isRecursive);
		
		if(iCount<=0)
		{
			System.out.println("No files found in "+folderImages.getAbsolutePath());
		}
		
	}
	
}
