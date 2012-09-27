package com.neuronrobotics.commercial.oggie;


import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

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
	
	private static final int width = 17;
	private static final int hight = 2;
	
	public TableDisplay(String name){
		setLayout(new MigLayout());
		add(new JLabel(name),"wrap");		
		getTable().setBorder(BorderFactory.createLoweredBevelBorder());		
		setBorder(BorderFactory.createLoweredBevelBorder());		
		// Disable auto resizing
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Set the first visible column to 100 pixels wide
//		int vColIndex = 0;
//		TableColumn col = getTable().getColumnModel().getColumn(vColIndex);
//		int width = 40;
//		col.setPreferredWidth(width);		
		getTable().getColumnModel().getColumn(0).setPreferredWidth(56);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(56);

		add(getTable(),"wrap");
		setEditable(false);
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
