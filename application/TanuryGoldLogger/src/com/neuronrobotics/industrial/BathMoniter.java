package com.neuronrobotics.industrial;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.BoxLayout;

import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;

import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;

public class BathMoniter extends JPanel implements IAnalogInputListener {
	private XYSeries ozHour = new XYSeries("Oz/Hour");
	private XYSeriesCollection xyDataset;
	private ChartPanel chartPanel;
	/**
	 * 
	 */
	private static final long serialVersionUID = -7734077989616188631L;
	private static int index=1;
	private String myName;
	private JTextField txtbathName;
	private JFreeChart chart;
	private JLabel lblName;
	private JLabel lblAmphourcurrent;
	private JTextField recentCurrentRating;
	private JLabel lblAmphourToOz;
	private JTextField textField_1;
	private JLabel lblTotalOzdaily;
	private JTextField textField_2;
	private JLabel lblSampleRate;
	private JTextField textField_3;
	private JLabel lblClearDataFor;
	private JButton btnClear;
	private MainWindow mainWindow;
	private DyIO dyio;
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalVoltage;
	
	private double reference;
	private double signal;
	
	public BathMoniter(DyIO dyio){
		this();
		this.setDyio(dyio);
		updateName(getDyio().getInfo());
	}
	
	public BathMoniter(){
		String tmpmyName = "Bath Moniter "+ index++;
		
		setLayout(new MigLayout("", "[grow][]", "[grow][]"));
		
		JPanel Controls = new JPanel();
		add(Controls, "cell 0 0,grow");
		Controls.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][grow]"));
		
		lblName = new JLabel("Name");
		Controls.add(lblName, "cell 0 0,alignx trailing");
		
		txtbathName = new JTextField();
		Controls.add(txtbathName, "cell 1 0,growx");
		txtbathName.setColumns(10);
		txtbathName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String newName = txtbathName.getText();
				updateName(newName);
			}
		});
		
		lblAmphourcurrent = new JLabel("Amp-Hour (Current)");
		Controls.add(lblAmphourcurrent, "cell 0 1,alignx trailing");
		
		recentCurrentRating = new JTextField();
		recentCurrentRating.setText("<value>");
		Controls.add(recentCurrentRating, "cell 1 1,growx");
		recentCurrentRating.setColumns(10);
		
		lblAmphourToOz = new JLabel("Amp-Hour to Oz. Scale");
		Controls.add(lblAmphourToOz, "cell 0 2,alignx trailing");
		
		textField_1 = new JTextField();
		textField_1.setText("<value>");
		Controls.add(textField_1, "cell 1 2,growx");
		textField_1.setColumns(10);
		
		lblTotalOzdaily = new JLabel("Total Oz. (Daily)");
		Controls.add(lblTotalOzdaily, "cell 0 3,alignx trailing");
		
		textField_2 = new JTextField();
		textField_2.setText("<value>");
		Controls.add(textField_2, "cell 1 3,growx");
		textField_2.setColumns(10);
		
		lblSampleRate = new JLabel("Sample Rate");
		Controls.add(lblSampleRate, "cell 0 4,alignx trailing");
		
		textField_3 = new JTextField();
		textField_3.setText("<time in seconds>");
		Controls.add(textField_3, "cell 1 4,growx");
		textField_3.setColumns(10);
		
		lblClearDataFor = new JLabel("Clear Data For Day");
		Controls.add(lblClearDataFor, "cell 0 5,alignx trailing");
		
		btnClear = new JButton("Clear");
		Controls.add(btnClear, "cell 1 5");
		
		xyDataset = new XYSeriesCollection();

		chart = ChartFactory.createXYLineChart(
				"", 
				"Minutes", 
				"Press(1/100th tons)/Temp(F)",
				xyDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);
		
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 250));

		xyDataset.addSeries(ozHour);
		
		add(chartPanel, "cell 0 1,grow");
		
		
		
		 updateName(tmpmyName);
	}
	
	private void updateName(String newName){
		 myName=newName;
		 if(getDyio()!=null)
			 getDyio().setInfo(newName);
		 chart.setTitle(newName);
		 setName(newName);
		 txtbathName.setText(newName);
		 if(mainWindow!=null)
			 mainWindow.updateTabData();
	}

	public void setMainWindow(MainWindow mainWindow) {
		if(mainWindow != this.mainWindow)
			this.mainWindow = mainWindow;		
	}

	public DyIO getDyio() {
		return dyio;
	}

	public void setDyio(DyIO dyio) {
		this.dyio = dyio;
		referenceVoltage = 	new AnalogInputChannel(dyio, 10);
		signalVoltage = 	new AnalogInputChannel(dyio, 11);
		referenceVoltage.configAdvancedAsyncAutoSample(500);
		signalVoltage.configAdvancedAsyncAutoSample(500);
		referenceVoltage.addAnalogInputListener(this);
		signalVoltage.addAnalogInputListener(this);
	}
	
	public double getCurrent(){
		
		double scale = (4096.0//Reference voltage actual volts
				*1024.0)
				/reference;
		double i=100.0;//Ohms of shunt
		
		return (signal*scale)/i;
	}

	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalVoltage){
			signal =  value;
		}
		recentCurrentRating.setText(new Double(getCurrent()).toString());
	}

}
