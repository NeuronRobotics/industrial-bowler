package com.neuronrobotics.industrial.popup;

import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.HSVSlider;
import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.jniloader.IObjectDetector;
import com.neuronrobotics.jniloader.IOnSlider;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVJNILoader;
import com.neuronrobotics.jniloader.RGBColorDetector;
import com.neuronrobotics.jniloader.WhiteBlobDetect;

public class PopUpMain implements IOnSlider {
	private HSVSlider upperThreshhold;
	private HSVSlider lowerThreshhold;
	private ArrayList<IObjectDetector> detectors;
	
	public void run() {
		//FaceDetector faceDetectorObject = new FaceDetector(0);
		Mat inputImage= new Mat();
		Mat displayImage= new Mat();
		JFrame frame = new JFrame();
		JFrame controlFrame= new JFrame();
		controlFrame.setSize(640, 580);
		controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		JTabbedPane  tabs = new JTabbedPane();
		frame .setContentPane(tabs);
		frame.setSize(640, 580);
		frame.setVisible(true);
		frame .setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		detectors = new ArrayList<IObjectDetector>();
		ArrayList<AbstractImageProvider> imageProviders = new ArrayList<AbstractImageProvider>();
		ArrayList<ImageIcon> iconsCaptured = new ArrayList<ImageIcon>();
		ArrayList<ImageIcon> iconsProcessed = new ArrayList<ImageIcon>();
		
		Scalar upper =new Scalar(30, 150, 0, 0);
		Scalar lower =new Scalar(240, 166, 0, 0);
		
		Scalar upper1 =new Scalar(360, 255, 255, 0);
		Scalar lower1 =new Scalar(240, 0, 0, 0);
		
		JPanel sliders = new JPanel(new MigLayout());
		upperThreshhold = new HSVSlider(this,lower,upper);
		lowerThreshhold = new HSVSlider(this,lower1,upper1);
		sliders.add(new JLabel("Threshhold 1"));
		sliders.add(upperThreshhold, "wrap");
		sliders.add(new JLabel("Threshhold 2"));
		sliders.add(lowerThreshhold, "wrap");
		controlFrame.setContentPane(sliders);
		controlFrame.setVisible(true);
		
		//tabs.addTab("Controls ",sliders);
		
		imageProviders.add(new OpenCVImageProvider(0));
		imageProviders.get(0).getLatestImage(inputImage,displayImage);
		
		//detectors.add(new WhiteBlobDetect((int) upper.val[0],(int) upper.val[1], lower));
		detectors.add(new HaarDetector(HaarDetector.class.getResource("haarcascades/haarcascade_mcs_upperbody.xml")));
		detectors.add(new HaarDetector());
		int x=0;
		for (AbstractImageProvider img:imageProviders){
			img.getLatestImage(inputImage,displayImage);
			
			ImageIcon tmp = new ImageIcon(img.getLatestImage());
			iconsCaptured.add(tmp);
			
			tabs.addTab("Camera "+x, new JLabel(tmp));
			
			
			for (int i=0;i<detectors.size();i++){
				detectors.get(i).getObjects(inputImage, displayImage);
				ImageIcon ptmp = new ImageIcon(img.getLatestImage());
				iconsProcessed.add(ptmp);
				tabs.addTab("Processed "+x+"."+i, new JLabel(ptmp));
			}
			x++;
		}
		
		while (true){

			for (int i=0;i< imageProviders.size();i++){
				imageProviders.get(i).getLatestImage(inputImage,displayImage);
				iconsCaptured.get(i).setImage(AbstractImageProvider.matToBufferedImage(inputImage));
//				
				for (int j=0;j<detectors.size();j++){
					Rect [] data = detectors.get(j).getObjects(inputImage, displayImage);
					iconsProcessed.get(i*j).setImage(AbstractImageProvider.matToBufferedImage(displayImage));	
					
					//System.out.println("Got: "+data.length);
					
				}
				frame.repaint();
			}
			
		}
		   
		    
	  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OpenCVJNILoader.load();
        
        new PopUpMain().run();
	}

	@Override
	public void onSlider(HSVSlider source, Scalar upper, Scalar lower) {
		if(source == upperThreshhold){
			detectors.get(0).setThreshhold(lower, upper);
			System.out.println("First threshholds, Upper: "+upper+" Lower: "+lower);
		}
		if(source == lowerThreshhold){
			detectors.get(0).setThreshhold2(lower, upper);
			System.out.println("Second threshholds, Upper: "+upper+" Lower: "+lower);
		}
	}


}
