package com.neuronrobotics.commercial.oggie;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Jama.Matrix;

public class PressGraph extends JPanel implements IPressHardwareListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1342874101887400385L;
	
	private XYSeries targetPressure = new XYSeries("Target Pressure");
	private XYSeries targetTemp = new XYSeries("Target Tempreture");
	private XYSeries measuredPressure = new XYSeries("Measured Pressure");
	private XYSeries measuredTemp = new XYSeries("Measured Tempreture");
	private long startTime = System.currentTimeMillis();
	
	private XYSeriesCollection xyDataset;
	private ChartPanel chartPanel;
	
	
	public PressGraph(String name){
		setName(name);
		
		xyDataset = new XYSeriesCollection();

		JFreeChart chart = ChartFactory.createXYLineChart(
				name, 
				"Minutes", 
				"Press(1/100th tons)/Temp(F)",
				xyDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);
		
		chartPanel = new ChartPanel(chart);
		XYPlot plot = (XYPlot) chart.getPlot();
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 250));
		add(chartPanel, BorderLayout.CENTER);
		xyDataset.addSeries(targetPressure);
		xyDataset.addSeries(targetTemp);
		xyDataset.addSeries(measuredPressure);
		xyDataset.addSeries(measuredTemp);
		
	}


	@Override
	public void onCycleStart(int index, CycleConfig config) {
		startTime = System.currentTimeMillis();
		double [] times = config.getTimes();
		double [] temps = config.getTempretures();
		double pressure = config.getPressure();
		targetPressure.clear();
		targetTemp.clear();
		for(int i=0;i<CycleConfig.dataSize;i++){
			targetPressure.add(times[i], pressure*100);
			targetTemp.add(times[i], temps[i]);
		}
		measuredPressure.clear();
		measuredTemp.clear();
	}


	@Override
	public void onAbortCycle(int i) {
		targetPressure.clear();
		targetTemp.clear();
		measuredPressure.clear();
		measuredTemp.clear();
	}


	@Override
	public void onPressureChange(int i, double pressureTons) {
		double time = (double)(System.currentTimeMillis()-startTime)/1000.0/60.0;
		System.out.println("Pressure change at time: "+time);
		measuredPressure.add(time, pressureTons*100);
	}


	@Override
	public void onTempretureChange(int i, double degreesFarenhight) {
		double time = (double)(System.currentTimeMillis()-startTime)/1000.0/60.0;
		System.out.println("Temp change at time: "+time);
		measuredTemp.add(time, degreesFarenhight);
	}

}
