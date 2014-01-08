package com.neuronrobotics.industrial;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;

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

import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterDevice;
import com.neuronrobotics.industrial.device.BathMoniterEvent;
import com.neuronrobotics.sdk.common.Log;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class BathMoniter extends JPanel implements IBathMoniterUpdateListener{
	private XYSeries ozHour = new XYSeries("Amps/Minutes");
	private XYSeriesCollection xyDataset;
	private ChartPanel chartPanel;
	/**
	 * 
	 */
	private static final long serialVersionUID = -7734077989616188631L;
	private static int index=1;
	private JTextField txtbathName;
	private JFreeChart chart;
	private JLabel lblName;
	private JLabel lblAmphourcurrent;
	private JLabel recentCurrentRating;
	private JLabel lblAmphourToOz;
	private JTextField textFieldScale;
	private JLabel lblTotalOzdaily;
	private JLabel recentTotal;
	private JLabel lblSampleRate;
	private JTextField PollingRateTextField;
	private JLabel lblClearDataFor;
	private JLabel packetTimeStamp;
	private JButton btnClear;
	private JComboBox howMuchData;
	
	private JLabel lblAlarm;
	private JTextField btnAlarm;
	
	private MainWindow mainWindow;
	private BathMoniterDevice dyio;
	//private long startTimestamp;
	Long startTime=null;

	private String units ="Amps";
	
	private int range = (60*24)/5;
	private JTextField ampTuneData;
	
	public BathMoniter(BathMoniterDevice bath,String address){
		this(address);
		this.setBath(bath);
		
		updateName(bath.getName());
		bath.addBathUi(this);
		if(bath.getPollingRate()<30){
			getBathDevice().setPollingRate(30);
		}
		PollingRateTextField.setText(new Integer(bath.getPollingRate()).toString());
		PollingRateTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PollingRateTextField.setBackground(Color.green);
				PollingRateTextField.setFocusable(false);
				PollingRateTextField.setFocusable(true);
				int rate =Integer.parseInt(PollingRateTextField.getText());
				if(rate<30)
					rate = 30;
				getBathDevice().setPollingRate(rate);
				PollingRateTextField.setText(new Integer(getBathDevice().getPollingRate()).toString());
			}
		});
		
		textFieldScale.setText(new Double(bath.getScale()).toString());
		textFieldScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				textFieldScale.setBackground(Color.green);
				textFieldScale.setFocusable(false);
				textFieldScale.setFocusable(true);
				getBathDevice().setScale(getScaleValue());
			}
		});
		
		btnClear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedOption = JOptionPane.showConfirmDialog(null, 
				                        "Are your sure you want to clear the bath data?", 
				                        "Choose", 
				                        JOptionPane.YES_NO_OPTION); 
				if (selectedOption == JOptionPane.YES_OPTION) {
					recentTotal.setText("0.0");
					getBathDevice().clearData();
					onClearData();
				}
			}
		});
		
		btnAlarm.setText(new Double(getBathDevice().getAlarmLevel()).toString());
		ampTuneData.setText(new Double(getBathDevice().getAmpTune()).toString());
		startTime = System.currentTimeMillis();
	}
	
	private double getScaleValue(){
		return Double.parseDouble(textFieldScale.getText());
	}
	
	public BathMoniter(String address){
		String tmpmyName = "Bath Moniter "+ index++;
		
		setLayout(new MigLayout("", "[grow][]", "[grow][]"));
		
		JPanel Controls = new JPanel();
		add(Controls, "cell 0 0,grow");
		Controls.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][grow]"));
		
		lblName = new JLabel(address+" Name");
		Controls.add(lblName, "cell 0 0,alignx trailing");
		
		txtbathName = new JTextField();
		txtbathName.setBackground(Color.green);
		txtbathName.addFocusListener(new FocusListener()
		{
			@Override
		    public void focusGained(FocusEvent fe)
		    {
		    	fe.getComponent().setBackground(Color.red);
		    }

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		Controls.add(txtbathName, "cell 1 0,growx");
		txtbathName.setColumns(10);
		txtbathName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String newName = txtbathName.getText();
				updateName(newName);
				txtbathName.setBackground(Color.green);
				txtbathName.setFocusable(false);
				txtbathName.setFocusable(true);
			}
		});
		
		lblAlarm=new JLabel("Alarm Level ("+units+")");;
		btnAlarm = new JTextField();
		btnAlarm.setBackground(Color.green);
		btnAlarm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getBathDevice().setAlarmLevel(Double.parseDouble(btnAlarm.getText()));
				btnAlarm.setBackground(Color.green);
				btnAlarm.setFocusable(false);
				btnAlarm.setFocusable(true);
			}
		});
		btnAlarm.addFocusListener(new FocusListener()
		{
			@Override
		    public void focusGained(FocusEvent fe)
		    {
		    	fe.getComponent().setBackground(Color.red);
		    }

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		Controls.add(lblAlarm, "cell 0 1,alignx trailing");
		Controls.add(btnAlarm, "cell 1 1,growx");
		
		lblAmphourcurrent = new JLabel(""+units+" (Recent)");
		Controls.add(lblAmphourcurrent, "cell 0 2,alignx trailing");
		
		setRecentCurrentRating(new JLabel());
		getRecentCurrentRating().setText("<value>");
		Controls.add(getRecentCurrentRating(), "cell 1 2,growx");
		//getRecentCurrentRating().setColumns(10);
		
		lblAmphourToOz = new JLabel("Amp-Hour to Oz. Scale");
		Controls.add(lblAmphourToOz, "cell 0 3,alignx trailing");
		
		textFieldScale = new JTextField();
		textFieldScale.setBackground(Color.green);
		textFieldScale.addFocusListener(new FocusListener()
		{
			@Override
		    public void focusGained(FocusEvent fe)
		    {
		    	fe.getComponent().setBackground(Color.red);
		    }

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		textFieldScale.setText("<value>");
		Controls.add(textFieldScale, "cell 1 3,growx");
		textFieldScale.setColumns(10);
		
		lblTotalOzdaily = new JLabel("Total Oz. (Daily)");
		Controls.add(lblTotalOzdaily, "cell 0 4,alignx trailing");
		
		recentTotal = new JLabel();
		recentTotal.setText("<value>");
		Controls.add(recentTotal, "cell 1 4,growx");
		//recentTotal.setColumns(10);
		
		lblSampleRate = new JLabel("Sample Rate");
		Controls.add(lblSampleRate, "cell 0 5,alignx trailing");
		
		PollingRateTextField = new JTextField();
		PollingRateTextField.setBackground(Color.green);
		PollingRateTextField.addFocusListener(new FocusListener()
		{
			@Override
		    public void focusGained(FocusEvent fe)
		    {
		    	fe.getComponent().setBackground(Color.red);
		    }

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		PollingRateTextField.setText("<time in seconds>");
		Controls.add(PollingRateTextField, "cell 1 5,growx");
		PollingRateTextField.setColumns(10);
		
		
		
		howMuchData = new JComboBox();
		howMuchData.addItem("All Data");
		howMuchData.addItem("Last Day");
		Controls.add(howMuchData, "cell 0 6,alignx trailing");
		JButton howMuchDataReq = new JButton("Request Data");
		howMuchDataReq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(howMuchData.getSelectedItem().toString().contains("All Data")){
					getBathDevice().dumpLogs(0);
				}else if(howMuchData.getSelectedItem().toString().contains("Last Day")){
					getBathDevice().dumpLogs(1);
				}
			}
		});
		Controls.add(howMuchDataReq, "cell 1 6");
		
		lblClearDataFor = new JLabel("Clear Device Logs");
		Controls.add(lblClearDataFor, "cell 0 7,alignx trailing");
		btnClear = new JButton("Clear");
		Controls.add(btnClear, "cell 1 7");
		
		JLabel tuneAmp= new JLabel("Tune amplifier");
		Controls.add(tuneAmp, "cell 0 8,alignx trailing");
		ampTuneData = new JTextField();
		ampTuneData.setColumns(10);
		
		ampTuneData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getBathDevice().setAmpTune(Double.parseDouble(ampTuneData.getText()));
			}
		});
		Controls.add(ampTuneData, "cell 1 8");
		
		Controls.add(new JLabel("Timestamp Of Last Packet"), "cell 0 9,alignx trailing");
		packetTimeStamp = new JLabel("<date>");
		Controls.add(packetTimeStamp, "cell 1 9");
		
		xyDataset = new XYSeriesCollection();

		chart = ChartFactory.createXYLineChart(
				"", 
				"Minutes", 
				units,
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
		axis.setFixedAutoRange(range);
		
		updateName(tmpmyName);
	}
	
	private void updateName(String newName){
		 if(getBathDevice()!=null)
			 getBathDevice().setName(newName);
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

	public BathMoniterDevice getBathDevice() {
		return dyio;
	}

	public void setBath(BathMoniterDevice dyio) {
		this.dyio = dyio;
	}

	public JLabel getRecentCurrentRating() {
		return recentCurrentRating;
	}

	public void setRecentCurrentRating(JLabel recentCurrentRating) {
		this.recentCurrentRating = recentCurrentRating;
	}

	@Override
	public void onNameChange(String newName) {
		if(mainWindow!=null)
			mainWindow.onNameChange(newName);
	}

	@Override
	public void onValueChange(BathMoniterEvent event) {
		
		if (startTime == null){
			startTime = new Long((long) event.getTimestamp()); 
			Log.warning("Start time was null");
		}
		double timestamp = ((double)(event.getTimestamp()-startTime))/(1000.0*60) ;
		// no matter wate, update teh timestamp
		packetTimeStamp.setText(new Date(event.getTimestamp()).toString());
		if((event.getTimestamp()-startTime-(300*60*1000))>1){// one second of leeway
			getRecentCurrentRating().setText(new Double(event.getCurrentOzHrRate()).toString());
			ozHour.add( timestamp , 
							event.getCurrentOzHrRate()); 
			if(ozHour.getItemCount()>range*2){
				ozHour.remove(0);
			}
			recentTotal.setText(new Double(	event.getScaledTotalUsedToday() 
					).toString());
		}else{
			Log.error("Timestamp is old "+new Date(event.getTimestamp())+", current is: "+new Date(System.currentTimeMillis()));
			Log.error("Started at "+new Date(startTime));
		}
		
		if(mainWindow!=null)
			mainWindow.onValueChange(event);
	}


	@Override
	public void onClearData() {
		if(mainWindow!=null)
			mainWindow.onClearData();
	}

	@Override
	public void onAlarmEvenFire(BathAlarmEvent ev) {
		if(mainWindow!=null)
			mainWindow.onAlarmEvenFire(ev);
	}
	




}
