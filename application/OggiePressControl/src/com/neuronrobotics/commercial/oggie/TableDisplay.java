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
import javax.swing.JOptionPane;
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


public class TableDisplay extends JPanel implements IPressHardwareListener {
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
	private RoundButton ready = new RoundButton("Running..",new Dimension(50, 50));
	private RoundButton abort = new RoundButton("Abort",new Dimension(100, 100));
	private PressGraph  graph = new PressGraph("Data");
	
	private IPressControler press;
	
	private static final int width = CycleConfig.dataSize;
	private static final int hight = 2;
	
	private static final double bound = .1;
	private final boolean usePress0;
	private final boolean usePress1;
	
	private double lowestTemp = 200;
	private double highestTemp = 500;
	
	public TableDisplay(boolean usePress0, boolean usePress1, IPressControler p){
		this.usePress0 = usePress0;
		this.usePress1 = usePress1;
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
					press.onCycleStart(new CycleConfig(getTableDataMatrix(),getPressureSetpoint() ));
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
					press.abortCycle();
				}
			}
		});
		abort.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				press.abortCycle();
			}
		});
		
		ready.setVisible(false);
		ready.setEnabled(false);
		
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
		
		JPanel interfacePanel = new JPanel(new MigLayout());
		
		interfacePanel.add(tablePanel);
		interfacePanel.add(controlsPanel);
		
		add(interfacePanel,"wrap");
		add(graph,"wrap");
		graph.onCycleStart(0,new CycleConfig(getTableDataMatrix(),getPressureSetpoint()));
	}
	
	private double getPressureSetpoint(){
		double t = 3.7;
		try{
			t = Double.parseDouble(tons.getText());
		}catch (NumberFormatException ex){
			tons.setText(" 003.700 ");
			t = Double.parseDouble(tons.getText());
		}
		return t;
	}
	
	private boolean isPressReady(){
		double t = Double.parseDouble(tons.getText());
		double c = press.getCurrentPressure();
		return (		c>=t-bound && 
						c<=t+bound);
	}
	
	private void abort(){
		System.out.println("Press Aborted");
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
		load.setEnabled(b);
		abort.setEnabled(false);
		ready.setVisible(false);
		
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
				{0,210},
				{10,240},
				{20,270},
				{30,290},
				{40,290},
				{60,290},
				{70,290},
				{80,290},
				{90,290},
				{100,290},
				{110,270},
				{120,250},
				{130,230},
				{140,210},
				{150,200},
				{160,200},
				{170,200},
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
        	double newVal=Double.parseDouble(value.toString());
        	
        	if(col==1&&(newVal<lowestTemp || newVal>highestTemp)){
				Object[] options = {"Yes, use value",
                "No, that is a mistake"};
				int n = JOptionPane.showOptionDialog(null,
				    "Value entered, " +newVal+"(F), is not between "+lowestTemp+"(F) and "+highestTemp+"(F)",
				    "Verify entry",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[1]);
				if(n==1){
					return;
				}
        	}
        	
        	data[row][col] = new DecimalFormat( "000.000" ).format(newVal);
            fireTableCellUpdated(row, col);
            graph.onCycleStart(0,new CycleConfig(getTableDataMatrix(),getPressureSetpoint()));
        }
	}

	@Override
	public void onCycleStart(int i, CycleConfig config) {
		// TODO Auto-generated method stub
		if(i==0 && usePress0||i==1 && usePress1){
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
		if(i==0 && usePress0||i==1 && usePress1)
			graph.onCycleStart(i, config);
		
	}

	@Override
	public void onAbortCycle(int i) {
		if(i==0 && usePress0||i==1 && usePress1){
			abort();
			//graph.onAbortCycle(i);
		}
		
	}

	@Override
	public void onPressureChange(int i, double pressureTons) {
		if(i==0 && usePress0||i==1 && usePress1)
			graph.onPressureChange(i, pressureTons);
	}

	@Override
	public void onTempretureChange(int i, double degreesFarenhight) {
		if(i==0 && usePress0||i==1 && usePress1)
			graph.onTempretureChange(i, degreesFarenhight);
	} 
}
