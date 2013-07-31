package com.neuronrobotics.industrial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.neuronrobotics.industrial.device.BathMoniterDevice;
import com.neuronrobotics.industrial.device.BathMoniterEvent;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class BathMoniter extends JPanel implements IBathMoniterUpdateListener{
	private XYSeries ozHour = new XYSeries("Oz/Minutes");
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
	private BathMoniterDevice dyio;
	private long startTimestamp;


	
	public BathMoniter(BathMoniterDevice dyio){
		this();
		this.setDyio(dyio);
		
		updateName(dyio.getName());
		dyio.addBathUi(this);
		textField_3.setText(new Integer(dyio.getPollingRate()).toString());
		textField_3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getDyio().setPollingRate(Integer.parseInt(textField_3.getText()));
			}
		});
		
		textField_1.setText(new Double(dyio.getScale()).toString());
		textField_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getDyio().setScale(getScaleValue());
			}
		});
		
		btnClear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getDyio().clearData();
			}
		});
		
	}
	
	private double getScaleValue(){
		return Double.parseDouble(textField_1.getText());
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
		
		setRecentCurrentRating(new JTextField());
		getRecentCurrentRating().setText("<value>");
		Controls.add(getRecentCurrentRating(), "cell 1 1,growx");
		getRecentCurrentRating().setColumns(10);
		
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
				"Oz Gold",
				xyDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);
		
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 250));

		xyDataset.addSeries(ozHour);
		
		add(chartPanel, "cell 0 1,grow");
		XYPlot plot = (XYPlot) chart.getPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setFixedAutoRange((60*24)/5);
		startTimestamp =System.currentTimeMillis();
		
		updateName(tmpmyName);
	}
	
	private void updateName(String newName){
		 myName=newName;
		 if(getDyio()!=null)
			 getDyio().setName(newName);
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

	public BathMoniterDevice getDyio() {
		return dyio;
	}

	public void setDyio(BathMoniterDevice dyio) {
		this.dyio = dyio;
	}

	public JTextField getRecentCurrentRating() {
		return recentCurrentRating;
	}

	public void setRecentCurrentRating(JTextField recentCurrentRating) {
		this.recentCurrentRating = recentCurrentRating;
	}

	@Override
	public void onNameChange(String newName) {
		if(mainWindow!=null)
			mainWindow.onNameChange(newName);
	}

	@Override
	public void onValueChange(BathMoniterEvent event) {
		getRecentCurrentRating().setText(new Double(event.getCurrentOzHrRate()).toString());
		ozHour.add(((event.getTimestamp()-startTimestamp)/1000), event.getCurrentOzHrRate());
		textField_2.setText(new Double(event.getTotalUsedToday() ).toString());
		if(mainWindow!=null)
			mainWindow.onValueChange(event);
	}

	@Override
	public void onAlarmEvenFire(String bathName, long timestamp,double currentOzHrRate, double alarmThreshhold) {
		if(mainWindow!=null)
			mainWindow.onAlarmEvenFire(bathName, timestamp, currentOzHrRate, alarmThreshhold);
	}
	




}
