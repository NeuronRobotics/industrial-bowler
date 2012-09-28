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

public class PressGraph extends JPanel{
	
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
	private ValueAxis axis;
	
	
	public PressGraph(String name){
		setName(name);
		
		xyDataset = new XYSeriesCollection();

		JFreeChart chart = ChartFactory.createXYLineChart(
				name, 
				"Minutes", 
				"Pressure(tons)/Tempreture(F)",
				xyDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);
		
		chartPanel = new ChartPanel(chart);
		XYPlot plot = (XYPlot) chart.getPlot();
		axis = plot.getDomainAxis();
		
		chartPanel.setSize(new Dimension(125, 100));
		
		add(chartPanel, BorderLayout.CENTER);
		xyDataset.addSeries(targetPressure);
		xyDataset.addSeries(targetTemp);
		xyDataset.addSeries(measuredPressure);
		xyDataset.addSeries(measuredTemp);
		
	}

}
