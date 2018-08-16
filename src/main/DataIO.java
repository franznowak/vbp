package main;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class DataIO {
	
	//constants
	public static final String[] AllowedArrayFormat = {"vbp", "txt"};
	public static final String[] AllowedImageFormat = {"png", "bmp", "jpg"};
	public static final int RESOLUTION = 16777215; //RGB-Maxvalue
	public static final double GRAY_FACTOR = RESOLUTION/Postprocessing.IMAGE_INTENSITY; //Factor to convert RGB to grayvalue;
	
	//members
	private String mFileName;
	private double[][] mArrayInputData;
	private int[][] mData;
	private int mW,mH;
	
	//Constructor
	public DataIO(String fileName) throws ImageFormatNotSupported, IOException {
		
		mFileName = fileName;
		String ext = getExtension(fileName);

		if(Arrays.asList(AllowedImageFormat).contains(ext)) 
		{
			loadImage();
		}
		else if(Arrays.asList(AllowedArrayFormat).contains(ext)) {
			loadArray();
		}
		else {
			throw new ImageFormatNotSupported();
		}
	}
	//Load image file
	private void loadImage() throws IOException {
		BufferedImage img = ImageIO.read(new File(mFileName));
		mW = img.getWidth();
		mH = img.getHeight();
		mData = imageBWToArray(img);
	}
	//Load text file with appropriate formatting
	private void loadArray() throws IOException {
		int lines = countLines(mFileName);
		int size = (int)Math.sqrt(lines);
		mW = size; mH = size;
		mArrayInputData = new double[mH][mW];
		mData = new int[mH][mW];
		readLargeFile(mFileName);
		normalizeArrayData();
	}
	//Save image file
	public static void saveImage(BufferedImage img, String fileName) throws ImageFormatNotSupported, IOException {
		String ext = getExtension(fileName);
		if(Arrays.asList(AllowedImageFormat).contains(ext)) 
		{
			File outputfile = new File(fileName);
			ImageIO.write(img, ext, outputfile);
		}
		else {
			throw new ImageFormatNotSupported();
		}
	}
	//get number of lines of file
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	//read in text file
	private void readLargeFile(String filename) throws IOException {
		Scanner scan;
	    File file = new File(filename);
        scan = new Scanner(file);
        double vList[] = new double[mW*mH];
        int a = 0, b = 0;
        Double d;
	    try {
	        while(scan.hasNextDouble())
	        {
	        	d = scan.nextDouble();
	        	if(a%8 == 7) {
		            vList[b] = d;
		            b++;
	        	}
	        	a++;
	        }
	        if (b<vList.length) {
	        	throw new IOException();
	        }
	        for(int i = 0; i < mH; i++) {
	        	for(int j = 0; j < mW; j++) {
			    	mArrayInputData[i][j] = vList[i*mW+j];
		    	}
	        }
	        
	    } 
	    finally{
	    	scan.close();
	    }
	}
	//make numbers in array RGB values
	private void normalizeArrayData() {
		double max = getLargestValue(mArrayInputData);
		double min = getSmallestValue(mArrayInputData);
		double offset = -min;
		double factor = RESOLUTION/(max+offset);
		
		for(int i = 0; i < mH; i++) {
	    	for(int j = 0; j < mW; j++) {
	    		mData[i][j] = (int)(factor*(mArrayInputData[i][j]+offset));
	    	}
	    }
	}
	//public getter method 
	public int[][] getData(){
		int[][] copy = new int[mH][mW];
		for(int i = 0; i < mH; i++) {
			for(int j = 0; j < mW; j++) {
				copy[i][j] = mData[i][j];
			}
		}
		return copy;
	}
	// largest grayscale pixel value
	public int getLargestValue(int[][] data) {
		int max=0;
		for(int i = 0; i < mH; i++) {
        	for(int j = 0; j < mW; j++) {
        		if(data[i][j]>max)
        			max = data[i][j];
        	}
		}
		return max;
	}
	//largest grayscale pixel value double
	public double getLargestValue(double[][] data) {
		double max=Double.NEGATIVE_INFINITY;
		for(int i = 0; i < mH; i++) {
        	for(int j = 0; j < mW; j++) {
        		if(data[i][j]>max)
        			max = data[i][j];
        	}
		}
		return max;
	}
	//smallest grayscale pixes value
	public int getSmallestValue(int[][] data) {
		int min=0;
		for(int i = 0; i < mH; i++) {
        	for(int j = 0; j < mW; j++) {
        		if(data[i][j]<min)
        			min = data[i][j];
        	}
		}
		return min;
	}
	//smallest grayscale pixes value double
	public double getSmallestValue(double[][] data) {
		double min=Double.POSITIVE_INFINITY;
		for(int i = 0; i < mH; i++) {
        	for(int j = 0; j < mW; j++) {
        		if(data[i][j]<min)
        			min = data[i][j];
        	}
		}
		return min;
	}
	
	//static image processing methods:
	//method to extract color pixel data from an image object
		public static int[][] imageRGBToArray(BufferedImage image) {
		
		int height = image.getHeight();
		int width = image.getWidth();
		
		int[][] data = new int[height][width];
		
		int rgb;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				rgb=image.getRGB(j, i);
				data[i][j] = rgb;
			}
		}
		return data;
	}
	//method to convert array of pixels to RGB image
	public static BufferedImage arrayToImageRGB(int[][] data) {
		int height = data.length;
		int width = data[0].length;
		
		BufferedImage img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
		
		int value;
        for(int i = 0; i < height; i++) {
        	for(int j = 0; j < width; j++) {
        		//set red, green and blue equal to the brightness value
        		value = data[i][j];
        		value = (int)(value);
        		img.setRGB(j, i, value);
        	}
        }
		return img;
	}
	//---
	//method to extract grayscale pixel data from an image object
	public static int[][] imageBWToArray(BufferedImage image) {
		
		//extract pixel data
		Raster raster = image.getData();
		
		int height = image.getHeight();
		int width = image.getWidth();
		
		int[][] data = new int[height][width];
		
		int gray;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				gray = raster.getSample(j, i, 0);
				gray=(int)(gray*GRAY_FACTOR);
				data[i][j] = gray;
			}
		}
		return data;
	}
	//method to convert array of grayscale pixels to image
	public static BufferedImage arrayToImageBW(int[][] data) {
		int height = data.length;
		int width = data[0].length;
		
		BufferedImage img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
		
		int value, rgb;
        for(int i = 0; i < height; i++) {
        	for(int j = 0; j < width; j++) {
        		//set red, green and blue equal to the brightness value
        		value = data[i][j];
        		value = (int)(value/GRAY_FACTOR);
        		rgb = value << 16 | value << 8 | value;
        		img.setRGB(j, i, rgb);
        	}
        }
		return img;
	}
	//end static image processing methods
	
	//get file extension
	public static String getExtension(String input) {
		String extension = "";
		int i = input.lastIndexOf('.');
		if (i > 0) {
		    extension = input.substring(i+1);
		}
		return extension;
	}

}