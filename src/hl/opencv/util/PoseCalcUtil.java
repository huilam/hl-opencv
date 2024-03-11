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

import org.opencv.core.KeyPoint;

public class PoseCalcUtil{
	
    public static double calcPoseSimilarity(KeyPoint[] aKp1, KeyPoint[] aKp2) {
    	
        double[] vector1 = keypointsToVector(aKp1);
        double[] vector2 = keypointsToVector(aKp2);
        
        vector1 = normalizeVector(vector1);
        vector2 = normalizeVector(vector2);

        return cosineSimilarity(vector1, vector2);
    }

    private static double[] keypointsToVector(KeyPoint[] aKeypoints) {
        double[] vector = new double[aKeypoints.length * 2];
        for (int i = 0; i < aKeypoints.length; i++) {
            vector[2 * i] = aKeypoints[i].pt.x;
            vector[2 * i + 1] = aKeypoints[i].pt.y;
        }
        return vector;
    }

    private static double[] normalizeVector(double[] aVector) {
        double sumSq = 0.0;
        for (double v : aVector) {
            sumSq += v * v;
        }
        double magnitude = Math.sqrt(sumSq);
        for (int i = 0; i < aVector.length; i++) {
        	aVector[i] = aVector[i] / magnitude;
        }
        return aVector;
    }

    private static double cosineSimilarity(double[] aVector1, double[] aVector2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < aVector1.length; i++) {
            dotProduct += aVector1[i] * aVector2[i];
            normA += Math.pow(aVector1[i], 2);
            normB += Math.pow(aVector2[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
	public static void main(String args[]) throws Exception
	{
	}
}
