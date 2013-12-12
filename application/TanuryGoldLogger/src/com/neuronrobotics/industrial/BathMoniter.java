package com.neuronrobotics.industrial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Timestamp;

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
	private JTextField recentCurrentRating;
	private JLabel lblAmphourToOz;
	private JTextField textFieldScale;
	private JLabel lblTotalOzdaily;
	private JTextField recentTotal;
	private JLabel lblSampleRate;
	private JTextField PollingRateTextField;
	private JLabel lblClearDataFor;
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
		//getBathDevice().dumpLogs(1);
		
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
		Controls.add(txtbathName, "cell 1 0,growx");
		txtbathName.setColumns(10);
		txtbathName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String newName = txtbathName.getText();
				updateName(newName);
			}
		});
		
		lblAlarm=new JLabel("Alarm Level ("+units+")");;
		btnAlarm = new JTextField();
		btnAlarm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getBathDevice().setAlarmLevel(Double.parseDouble(btnAlarm.getText()));
			}
		});
		Controls.add(lblAlarm, "cell 0 1,alignx trailing");
		Controls.add(btnAlarm, "cell 1 1,growx");
		
		lblAmphourcurrent = new JLabel(""+units+" (Recent)");
		Controls.add(lblAmphourcurrent, "cell 0 2,alignx trailing");
		
		setRecentCurrentRating(new JTextField());
		getRecentCurrentRating().setText("<value>");
		Controls.add(getRecentCurrentRating(), "cell 1 2,growx");
		getRecentCurrentRating().setColumns(10);
		
		lblAmphourToOz = new JLabel("Amp-Hour to Oz. Scale");
		Controls.add(lblAmphourToOz, "cell 0 3,alignx trailing");
		
		textFieldScale = new JTextField();
		textFieldScale.setText("<value>");
		Controls.add(textFieldScale, "cell 1 3,growx");
		textFieldScale.setColumns(10);
		
		lblTotalOzdaily = new JLabel("Total Oz. (Daily)");
		Controls.add(lblTotalOzdaily, "cell 0 4,alignx trailing");
		
		recentTotal = new JTextField();
		recentTotal.setText("<value>");
		Controls.add(recentTotal, "cell 1 4,growx");
		recentTotal.setColumns(10);
		
		lblSampleRate = new JLabel("Sample Rate");
		Controls.add(lblSampleRate, "cell 0 5,alignx trailing");
		
		PollingRateTextField = new JTextField();
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
		if (startTime == null)
			startTime = new Long((long) event.getTimestamp()); 
		double timestamp = ((double)(event.getTimestamp()-startTime))/(1000.0*60) ;

		if((event.getTimestamp()-startTime)>1000){// one second of leeway
			ozHour.add( timestamp , 
							event.getCurrentOzHrRate()); 
			if(ozHour.getItemCount()>range){
				ozHour.remove(0);
			}
		}else{
			System.err.println("Timestamp is old "+new Timestamp(event.getTimestamp())+", current is: "+new Timestamp(System.currentTimeMillis()));
			System.err.println("Started at "+new Timestamp(startTime));
		}
		recentTotal.setText(new Double(	event.getScaledTotalUsedToday() 
										).toString());
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
