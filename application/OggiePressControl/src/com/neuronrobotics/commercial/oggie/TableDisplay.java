package com.neuronrobotics.commercial.oggie;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.neuronrobotics.sdk.util.ThreadUtil;

import Jama.Matrix;
import net.miginfocom.swing.MigLayout;


public class TableDisplay extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9213263491347912407L;
	private MyDefaultTableModel model = new MyDefaultTableModel();
	private JTable table = new JTable(model);
	private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
	private JComboBox< String> cycleName = new JComboBox<String>();
	private JTextField tons = new JTextField(" 003.700 ");
	private JButton save = new JButton("Save As...");
	private JButton load = new JButton("Load file...");
	private RoundButton start = new RoundButton("Start",new Dimension(100, 100));
	private RoundButton ready = new RoundButton("Ready",new Dimension(50, 50));
	private RoundButton abort = new RoundButton("Abort",new Dimension(100, 100));
	
	private IPressControler press;
	
	private static final int width = 17;
	private static final int hight = 2;
	
	private static final double bound = .1;
	
	public TableDisplay(String name,IPressControler p){
		setLayout(new MigLayout());
		press=p;		
		getTable().setBorder(BorderFactory.createLoweredBevelBorder());		
		setBorder(BorderFactory.createLoweredBevelBorder());		
		// Disable auto resizing
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Set the first visible column to 100 pixels wide
//		int vColIndex = 0;
//		TableColumn col = getTable().getColumnModel().getColumn(vColIndex);
//		int width = 40;
//		col.setPreferredWidth(width);		
		getTable().getColumnModel().getColumn(0).setPreferredWidth(70);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(70);
		setEditable(true);
		
		start.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				ButtonModel aModel = start.getModel();
				if(aModel.isArmed() && aModel.isPressed()){
					System.out.println("Starting...");
					double t = Double.parseDouble(tons.getText());
					press.onCycleStart(new CycleConfig(getTableDataMatrix(),t ));
					new Thread(){
						public void run(){
							ButtonModel aModel = start.getModel();
							while(aModel.isArmed() && aModel.isPressed()){
								ThreadUtil.wait(100);
								if(	isPressReady()){
									ready.setVisible(true);
								}
							}
						}
					}.start();
				}
			}
		});
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(	isPressReady()){
					System.out.println("Press Running");
					abort.setEnabled(true);
					start.setEnabled(false);
				}else{
					abort();
				}
			}
		});
		abort.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				abort();
			}
		});
		
		ready.setVisible(false);
		
		JPanel tablePanel = new JPanel(new MigLayout());
		JPanel controlsPanel = new JPanel(new MigLayout());
		
		tablePanel.add(new JLabel("Time(min)  Temp(F)"),"wrap");
		tablePanel.add(getTable(),"wrap");
		
		controlsPanel.add(load,"wrap");
		controlsPanel.add(new JLabel("Cycle Name"));
		controlsPanel.add(cycleName,"wrap");
		controlsPanel.add(new JLabel("Pressure"));
		controlsPanel.add(tons);
		controlsPanel.add(new JLabel("Tons"),"wrap");
		controlsPanel.add(save,"wrap");
		controlsPanel.add(start);
		controlsPanel.add(ready,"wrap");
		ready.setColor(Color.green);
		start.setColor(Color.yellow);
		controlsPanel.add(abort,"wrap");
		abort.setColor(Color.red);
		abort.setEnabled(false);
		
		add(tablePanel);
		add(controlsPanel);
	}
	
	private boolean isPressReady(){
		double t = Double.parseDouble(tons.getText());
		double c = press.getCurrentPressure();
		return (		c>=t-bound && 
						c<=t+bound);
	}
	
	private void abort(){
		System.out.println("Press Aborted");
		press.abortCycle();
		abort.setEnabled(false);
		start.setEnabled(true);
		ready.setVisible(false);
	}
	
	@Override
	public void setEnabled(boolean b){
		table.setEnabled(b);
		tons.setEnabled(b);
		start.setEnabled(b);
		save.setEnabled(b);
		cycleName.setEnabled(b);
		abort.setEnabled(false);
	}

	public void setTransform(Matrix m){
		getTable().setEnabled(false);
		for(TableModelListener l:listeners){
			getTable().getModel().removeTableModelListener(l);
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < hight; j++) {
				getTable().setValueAt(m.get(i, j),i,j);
			}
		}
		for(TableModelListener l:listeners){
			getTable().getModel().addTableModelListener(l);
		}
		getTable().setEnabled(true);
		//System.out.println("Matrix display setting data "+m);
	}
	public double[][] getTableData() {
		double[][] data = new double[width][hight];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < hight; j++) {
				String current = this.getTable().getValueAt(i, j).toString();
				data[i][j] = Double.parseDouble( current);
			}
		}
		return data;
	}
	public Matrix getTableDataMatrix() {
		return new Matrix(getTableData());
	}
	

	public JTable getTable() {
		return table;
	}
	public void addTableModelListener(TableModelListener l){
		listeners.add(l);
		getTable().getModel().addTableModelListener(l);
	}

	public void setEditable(boolean b) {
		model.setEditable(b);
	}

	private class MyDefaultTableModel extends DefaultTableModel { 
		/**
		 * 
		 */
		private static final long serialVersionUID = 7096254212840475488L;
		private boolean edit=false;
		private Object[][] data = {
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0},
		};
		 public MyDefaultTableModel() {  
		   super(width,hight);  
		   
		 }  
		 public boolean isCellEditable(int row, int col) {  
		   return edit;  
		 } 
		public void setEditable(boolean b){
			 edit=b;
		 }
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
        	data[row][col] = new DecimalFormat( "000.000" ).format(Double.parseDouble(value.toString()));
//        	if(col == 3 && row < 3){
//        		data[row][col] = new DecimalFormat( "000.000" ).format(Double.parseDouble(value.toString()));	
//        	} else if(row == 3){
//        		data[row][col] = new DecimalFormat( "0" ).format(Double.parseDouble(value.toString()));	
//        	}        		
//        	else{
//        		data[row][col] = new DecimalFormat( "0.000" ).format(Double.parseDouble(value.toString()));
//        	}        		
            fireTableCellUpdated(row, col);
        }
	} 
}
