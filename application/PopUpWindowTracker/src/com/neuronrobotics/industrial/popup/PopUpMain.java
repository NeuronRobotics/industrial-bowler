package com.neuronrobotics.industrial.popup;

import java.awt.image.BufferedImage;
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
import org.opencv.features2d.KeyPoint;

import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.HSVSlider;
import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.jniloader.IObjectDetector;
import com.neuronrobotics.jniloader.IOnSlider;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVJNILoader;
import com.neuronrobotics.jniloader.ProcessingPipeline;
import com.neuronrobotics.jniloader.RGBColorDetector;
import com.neuronrobotics.jniloader.WhiteBlobDetect;
import com.neuronrobotics.replicator.driver.DeltaForgeDevice;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.serial.SerialConnection;

public class PopUpMain  {
	private ProcessingPipeline pipe = new ProcessingPipeline();
	ArrayList<ImageIcon> iconsCaptured = new ArrayList<ImageIcon>();
	ArrayList<ImageIcon> iconsProcessed = new ArrayList<ImageIcon>();
	public void run() {

		Mat inputImage= new Mat();
		Mat displayImage= new Mat();
		JFrame frame = new JFrame();

		JTabbedPane  tabs = new JTabbedPane();
		frame .setContentPane(tabs);
		frame.setSize(640, 580);
		frame.setVisible(true);
		frame .setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		DeltaForgeDevice delt = new DeltaForgeDevice();
		delt.setConnection(new SerialConnection("/dev/BowlerDevice.74F726000000"));		
		delt.connect();

		
		pipe.addAbstractImageProvider(new OpenCVImageProvider(0));
		pipe.getLatestImage(0,inputImage,displayImage);
		
		//pipe.addDetector(new HaarDetector(HaarDetector.class.getResource("haarcascades/haarcascade_mcs_upperbody.xml")));
		pipe.addDetector(new HaarDetector());
		int x=0;
		for (int j=0;j<pipe.getProviderSize();j++){
			
			BufferedImage tmpImage =pipe.getLatestImage(j,inputImage,displayImage);
			
			ImageIcon tmp = new ImageIcon(tmpImage);
			iconsCaptured.add(tmp);
			
			tabs.addTab("Camera "+x, new JLabel(tmp));

			for (int i=0;i<pipe.getDetectorSize();i++){
				pipe.getObjects(i,inputImage, displayImage);
				ImageIcon ptmp = new ImageIcon(tmpImage);
				iconsProcessed.add(ptmp);
				tabs.addTab("Processed "+x+"."+i, new JLabel(ptmp));
			}
			x++;
		}
		
		while (true){

			for (int i=0;i< pipe.getProviderSize();i++){
				pipe.getLatestImage(i,inputImage,displayImage);
				iconsCaptured.get(i).setImage(AbstractImageProvider.matToBufferedImage(inputImage));
//				
				for (int j=0;j<pipe.getDetectorSize();j++){
					KeyPoint [] data = pipe.getObjects(j,inputImage, displayImage);
					iconsProcessed.get(j+(i*j)).setImage(AbstractImageProvider.matToBufferedImage(displayImage));
					
					if(data.length>0){
						double size = ((data[0].size-75)/125)*90-45;
						System.out.println("Size= "+size);
						
						TransformNR current = new TransformNR(	size,
																(data[0].pt.y/480.0)*90-45,
																(data[0].pt.x/640.0)*300+50,
																new RotationNR());
						
						delt.sendLinearSection(current, 0, 0,true);
					}
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



}
