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

package hl.opencv;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Core;

import hl.common.FileUtil;
import hl.opencv.util.OpenCvUtil;

public class OpenCvLibLoader{
	
	private String opencv_jarpath 	= null;
	private String native_libpath 	= null;
	private String native_libname 	= null;
	private ClassLoader classLoader = null;
	private boolean loaded 			= false;
	private Exception errInit		= null;
	private String opencv_version	= null;
	private String opencv_buildinfo	= null;
	
	private static Object synObj = new Object();
	private static Object synInitObj = new Object();
	
	private static Map<String, OpenCvLibLoader> mapOpenCvLibLoader = new HashMap<>();
	
	public static OpenCvLibLoader getInstance()
	{
		return getInstance(Core.NATIVE_LIBRARY_NAME, "/");
	}
	
	public static OpenCvLibLoader getInstance(String aLibFileName, String aCustomPath)
	{
		if(aCustomPath==null || aCustomPath.trim().length()==0)
			aCustomPath = "/";
		else if(!aCustomPath.endsWith("/"))
			aCustomPath = aCustomPath + "/";
		
		OpenCvLibLoader instance = mapOpenCvLibLoader.get(aCustomPath+aLibFileName);
		
		if(instance==null)
		{
			synchronized(synObj) 
			{
				instance = new OpenCvLibLoader(aLibFileName, aCustomPath);
				if(instance.init())
					mapOpenCvLibLoader.put(aCustomPath+aLibFileName, instance);
			}
		}
		return instance;
	}

	public OpenCvLibLoader(String NativeLibName, String NativeLibPath)
	{
		setNative_libname(NativeLibName);
		setNative_libpath(NativeLibPath);
	}
	//
	public String getOpencv_jarpath() {
		return opencv_jarpath;
	}
	public void setOpencv_jarpath(String opencv_jarpath) {
		this.opencv_jarpath = opencv_jarpath;
	}
	//
	public String getNative_libpath() {
		return native_libpath;
	}
	public void setNative_libpath(String native_libpath) {
		this.native_libpath = native_libpath;
	}
	//
	public String getNative_libname() {
		return native_libname;
	}
	public void setNative_libname(String native_libname) {
		this.native_libname = native_libname;
	}
	//
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	//
	public Exception getInitException()
	{
		return this.errInit;
	}
	//
	public String getVersion()
	{
		return this.opencv_version;
	}
	//
	public String getBuildInfo()
	{
		return this.opencv_buildinfo;
	}
	//
	public boolean init()
	{
		if(this.loaded)
			return true;
		
		try {
			if(!this.loaded)
			{
				synchronized(synInitObj)
				{
					this.errInit= null;
					this.loaded = FileUtil.loadNativeLib(this.native_libname, this.native_libpath);
					
					if(this.loaded)
					{
						ClassLoader cl = OpenCvLibLoader.class.getClassLoader();
						
						if(getOpencv_jarpath()!=null)
						{
							File fileJar = new File(getOpencv_jarpath());
							if(fileJar.isFile())
							{
								cl = new URLClassLoader(new URL[]{ new URL(fileJar.getCanonicalPath())});
							}
							else
							{
								throw new Exception("File NOT found ! - "+getOpencv_jarpath());
							}
						}
						
						if(cl!=null)
						{
							Class<?> classCV = cl.loadClass("org.opencv.core.Core");
							if(classCV!=null)
							{
								Field f = classCV.getDeclaredField("VERSION");
								if(f!=null)
								{
									this.opencv_version = (String) f.get(null);
									
									Method m = classCV.getMethod("getBuildInformation");
									if(m!=null)
									{
										this.opencv_buildinfo = (String) m.invoke(null);
									}
								}
							}
							
						}
					}
				}
				
				if(!this.loaded)
				{
					System.err.println(getInitException());
				}
				else
				{
					System.out.println(OpenCvUtil.getDefaultFeatureInfo());
				}
			}
			
			
		} catch (Exception e) {
			this.loaded = false;
			this.errInit = e;
		}
		return this.loaded;
	}
	
	
	public static void main(String args[]) throws Exception
	{
		OpenCvLibLoader cvLib = OpenCvLibLoader.getInstance();
		System.out.println(OpenCvUtil.getDefaultFeatureInfo());
	}
}
