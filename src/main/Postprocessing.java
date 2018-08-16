package main;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Postprocessing{ 
	//constants
	public static final int MAX_IMG_SIZE = 10000, MAX_BOX_SIZE=2048;
	public static final int IMAGE_INTENSITY = 256;
	
	//members
	private DataIO dI;//DataIO Object to save and load images
	
	private int[][] mData;//pixel array
	private int[][] mResData;//pixel array calculated image
	private int mCentreX, mCentreY;//coordinates of the centre between the boxes
	private int mBoxDist;
	private int mBoxSize;
	private int mCutoff;//value after which phase jumps to zero
	private int mOffset;//phase offset
	private int mRotAngle;//rotation
	
	private double mNormFactor=1.0; //Factor to normalize the result image
	public boolean mNormalize;//Factor to let all values in the result image go from black to white
	
	public boolean staticObject;//Object box stays static and only reference box moves

	private BufferedImage mImage;//original image
	private BufferedImage mCutImage, mResImage; //image with cutoff and result image
	private int mImgW, mImgH; //width and height of original image
	

	
	//Constructor
	public Postprocessing(String fileName) throws IOException, ImageTooLarge, ImageFormatNotSupported{
		
		this.loadImage(fileName);
		
		mImgH = mData.length;
		mImgW = mData[0].length;
		
		mImage = DataIO.arrayToImageBW(mData);
		
		this.initialize();

		this.getResult();
	}
	//Constructor with settings from previous image
	public Postprocessing(String fileName, int[] settings) throws IOException, ImageTooLarge, ImageFormatNotSupported {
		
		this.loadImage(fileName);
		
		mImgH = mData.length;
		mImgW = mData[0].length;
		
		mImage = DataIO.arrayToImageBW(mData);
		
		try {
			setCentreX(settings[0]);
			setCentreY(settings[1]);
			setBoxDist(settings[2]);
			setBoxSize(settings[3]);
			setRotAngle(settings[4]);
			setCutoff(settings[5]);
			setOffset(settings[6]);
		}
		catch(OutsideOfDomain o) {
			initialize();
		}
		catch(TooSmall t) {
			initialize();
		}
		catch (ImageTooLarge i) {
			initialize();
		}
		
		this.getResult();
	}
	
	private void initialize() throws ImageTooLarge {
		
		int sizeMin = Math.min(mImgH,mImgW);
		int sizeMax = Math.max(mImgH,mImgW);
		
		if(sizeMax > MAX_IMG_SIZE)
			throw new ImageTooLarge();
		
		mCentreX = sizeMin/2;
		mCentreY = sizeMin/2;
		mBoxDist = sizeMin/4+mImgH/8;
		mBoxSize = (sizeMin/4 < MAX_BOX_SIZE) ? (sizeMin / 4) : MAX_BOX_SIZE;
		mRotAngle = 0;		
		mCutoff = IMAGE_INTENSITY;
		mOffset = 0;
	}
	
	//fileIO:
	public void loadImage(String fileName) throws IOException, ImageTooLarge, ImageFormatNotSupported{
		dI = new DataIO(fileName);
		mData = dI.getData();
	}
	public void saveImage(String fileName) throws IOException, ImageFormatNotSupported{
		// retrieve image
		BufferedImage bi = getResImage();
		//scale if too small:
		if(Math.min(bi.getHeight(),bi.getWidth())<512)
			bi=scaleImage(bi, 512);
		DataIO.saveImage(bi, fileName);
	}
	public void saveSrcImage(String fileName) throws IOException, ImageFormatNotSupported{
		// retrieve image
		BufferedImage bi = getCutImage();
		//scale if too small:
		if(Math.min(bi.getHeight(),bi.getWidth())<512)
			bi=scaleImage(bi, 512);
		DataIO.saveImage(bi, fileName);
	}
	//end fileIO
	
	//---
	
	//Setter methods
	public void setCentreX(int x)throws OutsideOfDomain{
		if(x>=mBoxSize/2&&x<=mImgW-mBoxSize/2)
			mCentreX = x;
		else
			throw new OutsideOfDomain();
	}
	public void setCentreY(int y) throws OutsideOfDomain {
		if(y>=Math.abs(mBoxDist/2)+mBoxSize/2&&y<=mImgH-Math.abs(mBoxDist/2)-mBoxSize/2)
			mCentreY = y;
		else
			throw new OutsideOfDomain();
	}
	public void setBoxDist(int d) throws OutsideOfDomain {
		int difference = d-mBoxDist; //for static object mode
		
		if((mCentreY>=Math.abs(d/2)+mBoxSize/2&&mCentreY<=mImgH-Math.abs(d/2)-mBoxSize/2)
				&& !staticObject)
			mBoxDist = d;
		else if((mCentreY-difference>=Math.abs(d/2)+mBoxSize/2&&mCentreY-difference<=mImgH-Math.abs(d/2)-mBoxSize/2)
				&& staticObject) {
			//readjust to only move reference field
			mBoxDist = d;
			setCentreY(mCentreY - (int)(difference/(double)2));
		}
		else
			throw new OutsideOfDomain();
	}
	public void setBoxSize(int s) throws OutsideOfDomain, TooSmall, ImageTooLarge {
		if(s<=0)
			throw new TooSmall();
		else if(!(mCentreY>=Math.abs(mBoxDist/2)+s/2&&mCentreY<=mImgH-Math.abs(mBoxDist/2)-s/2
				&& mCentreX>=s/2&&mCentreX<=mImgW-s/2))
			throw new OutsideOfDomain();
		else if(s>MAX_BOX_SIZE)
			throw new ImageTooLarge();
		else
			mBoxSize = s;
	}
	public void setRotAngle(int r) {
		mRotAngle = r;
		//calculate rotated image
		rotateImage();
	}
	public void setCutoff(int c) throws TooSmall {
		if(c<=0)
			throw new TooSmall();
		else if(c>IMAGE_INTENSITY)
			throw new TooSmall();
		mCutoff = c;
	}
	public void setOffset(int o) {
		if(o < 0)
			o = mCutoff - ((-o)%mCutoff);
		mOffset = o % mCutoff;
	}
	//end setter methods
	
	//---
	
	//Getter Methods
	public int[] getSettings() {
		int[] res = {mCentreX,mCentreY,mBoxDist,mBoxSize,mRotAngle,mCutoff,mOffset};
		return res;
	}
	public int[] getImageSize() {
		int[] res = {mImgW,mImgH};
		return res;
	}
	public void getResult() {
		mResData = new int[mBoxSize][mBoxSize];
		System.gc();
		//calculate resulting image
		addImages();
	}
	public BufferedImage getOriginalImage() {
		return mImage;
	}
	//method to convert array to image with cutoff
	public BufferedImage getCutImage() {
        if(mCutImage==null)
        	mCutImage = new BufferedImage(mImgW, mImgH, BufferedImage.TYPE_INT_RGB);
        
        int value, rgb;
        for(int i = 0; i < mImgH; i++) {
        	for(int j = 0; j < mImgW; j++) {
        		//set red, green and blue equal to the brightness value
        		value = mData[i][j];
        		
        		value = applyCutoff(value);
        		value = makeSmall(value);

        		rgb = value << 16 | value << 8 | value;
        		mCutImage.setRGB(j, i, rgb);
        	}
        }
        
        return mCutImage;
    }
	//method to convert resulting array to grayscale image
	public BufferedImage getResImage() {
		mResImage = new BufferedImage(mBoxSize, mBoxSize, BufferedImage.TYPE_INT_RGB);
		System.gc();

		assert(mResData != null);
		
        int value, rgb;
        for(int i = 0; i < mBoxSize; i++) {
        	for(int j = 0; j < mBoxSize; j++) {
        		//set red, green and blue equal to the brightness value
        		value = mResData[i][j];

        		value = applyCutoff(value);
        		if(mNormalize)
        			value = normalize(value);
        		value = makeSmall(value);
        		
        		rgb = value << 16 | value << 8 | value;
        		mResImage.setRGB(j, i, rgb);
        	}
        }
        return mResImage;
	}
	//end getter methods
	
	//---
	
	//computation methods:
	
	/**	convert array to grayscale:
	*	The following three functions need to be executed in order with normalize being 
	*			the optional step intended for the subtracted pixels of the result image
	**/
	//calculate array with adjusted 2pi jumps, normalized to still have full range
	private int applyCutoff(int input) {
		int cutoff = (int)(mCutoff*DataIO.GRAY_FACTOR);
		int offset = (int)(mOffset*DataIO.GRAY_FACTOR);
		
		int res = (input + offset) % cutoff;
		
		if(res < 0)
			res += cutoff;
		
		double norm = IMAGE_INTENSITY/mCutoff;
		return (int)(res*norm);
	}
	//multiply pixel from image with pixels in range (minpixel, maxpixel) so that maxpixel is white
	private int normalize(int input) {
		int value=(int)(input*mNormFactor);
		return value;
	}
	// converts RGB to grayscale value
	private int makeSmall(int input) {
		
		return (int)(input/DataIO.GRAY_FACTOR);
	};
	//end convert array to grayscale
	
	//method to subtract one image from the other
	private void addImages() throws ArrayIndexOutOfBoundsException{
		//1 is object (lower box), 2 is reference (higher box)
		int offsetx1 = mCentreX - mBoxSize/2;
		int offsety1 = mCentreY + mBoxDist/2 - mBoxSize/2;
		int offsetx2 = mCentreX - mBoxSize/2;
		int offsety2 = mCentreY - mBoxDist/2 - mBoxSize/2;
		
		int pixel1, pixel2, resPixel;
		
		int max=0; //to find brightest pixel
		
		for(int i = 0; i < mBoxSize; i++) {
			for(int j = 0; j < mBoxSize; j++) {
				pixel1 = mData[offsety1+i][offsetx1+j];
				pixel2 = mData[offsety2+i][offsetx2+j];
				resPixel = pixel1-pixel2;
				
				applyCutoff(resPixel);
				
				mResData[i][j]=resPixel;
				
				if(resPixel>max)
					max=resPixel;
			}
		};
		int maxAllowed = (int)((IMAGE_INTENSITY * DataIO.GRAY_FACTOR) % (mCutoff * DataIO.GRAY_FACTOR));
		if(max < maxAllowed && mNormalize) {
			mNormFactor = maxAllowed/max;
		}
		
	}
	//image rotation, always from original image
	private void rotateImage() {
		if(mRotAngle!=0) {
			BufferedImage newImage = new BufferedImage(mImgW, mImgH, BufferedImage.TYPE_INT_RGB);
			
			AffineTransform aT = new AffineTransform();
			aT.rotate(Math.toRadians(mRotAngle), mImgW/2, mImgH/2);
			
			AffineTransformOp op = new AffineTransformOp(aT, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			
			op.filter(DataIO.arrayToImageRGB(dI.getData()), newImage);
			
			mData = DataIO.imageRGBToArray(newImage);
		}
		else
		{
			mData = dI.getData();
		}
	}
	//scale image
	public static BufferedImage scaleImage(BufferedImage img, int size) {
		BufferedImage newImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		double sx = size/img.getWidth();
		double sy = size/img.getHeight();
		
		AffineTransform aT = new AffineTransform();
		aT.scale(sx, sy);
		
		AffineTransformOp op = new AffineTransformOp(aT, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		
		op.filter(img, newImage);
		
		return newImage;
	}
}