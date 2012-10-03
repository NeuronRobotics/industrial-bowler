package com.neuronrobotics.commercial.oggie;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.neuronrobotics.commercial.oggie.fileio.FileSelectionFactory;
import com.neuronrobotics.commercial.oggie.fileio.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.xml.XmlFactory;
import com.neuronrobotics.sdk.common.Log;
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
	private JComboBox cycleName = new JComboBox();
	private JTextField tons = new JTextField(" 003.700 ");
	private JTextField currentTemp = new JTextField(" 000.000 ");
	private JTextField currentPressure = new JTextField(" 000.000 ");
	private JButton save = new JButton("Save As...");
	private JButton load = new JButton("Load file...");
	private JPasswordField passwd = new JPasswordField(15);
	private JButton unlock = new JButton("Unlock");
	private JButton lock = new JButton("Lock");
	private RoundButton start = new RoundButton("Start Cycle",new Dimension(100, 100));
	private RoundButton setTemp = new RoundButton("Set Temp",new Dimension(100, 100));
	private RoundButton ready = new RoundButton("Running..",new Dimension(50, 50));
	private RoundButton abort = new RoundButton("Abort Cycle",new Dimension(100, 100));
	private PressGraph  graph = new PressGraph("Data");
	
	private IPressControler press;
	
	private static final int width = CycleConfig.dataSize;
	private static final int hight = 2;
	
	private static final double bound = .1;
	private final boolean usePress0;
	private final boolean usePress1;
	
	private boolean waitingForTemp = false;
	
	private double lowestTemp = 180;
	private double highestTemp = 350;
	
	private ArrayList<File> availibleFiles = new ArrayList<File>();
	
	private String defaultFileString = "Default";
	private final String adminPassword = "wumpus3742";
	private File currentSave=null;
	private OggiePressConfigFileManager fm;
	
	public TableDisplay(boolean usePress0, boolean usePress1, IPressControler p){
		this.usePress0 = usePress0;
		this.usePress1 = usePress1;
		setLayout(new MigLayout());
		press=p;
		getTable().setBorder(BorderFactory.createLoweredBevelBorder());		
		setBorder(BorderFactory.createLoweredBevelBorder());		
		// Disable auto resizing
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
		getTable().getColumnModel().getColumn(0).setPreferredWidth(70);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(70);
		setEditable(true);
		
		
		setTemp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				press.setTempreture(getStartingTempreture());
				waitingForTemp = true;
			}
		});
		
		start.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				ButtonModel aModel = start.getModel();
				if(aModel.isArmed() && aModel.isPressed()){
					System.out.println("Starting...");
					press.onCycleStart(getCurrentCycleConfig());
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
		
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CycleConfig conf = getCurrentCycleConfig();
				currentSave = FileSelectionFactory.GetFile(currentSave, new XmlFilter());
				System.out.println("Using file: "+currentSave);
				conf.saveToFile(currentSave);
				try {
					//validation step
					conf = new CycleConfig(currentSave);
					setCycleConfig(conf);
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		load.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File nFile = FileSelectionFactory.GetFile(currentSave, new XmlFilter());
				if(nFile!=null){
					CycleConfig conf;
					try {
						conf= new CycleConfig(nFile);
						setCycleConfig(conf);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					currentSave = nFile;
				}
			}
		});
		
		cycleName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = getSelectedFile();
				if(selected.equalsIgnoreCase(defaultFileString)){
					System.out.println("Using default values");
					setCycleConfig(new CycleConfig());
					return;
				}
				for(int i=0;i<availibleFiles.size();i++){
					if(selected.equalsIgnoreCase(availibleFiles.get(i).getName())){
						System.out.println("Using file: "+availibleFiles.get(i).getAbsolutePath());
						try {
							setCycleConfig(new CycleConfig(availibleFiles.get(i)));
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						fm.save();
						return;
					}
				}
			}
		});
		
		lock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDataTebleLockState(true);
			}
		});
		
		unlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireUnlockEvent();
			}
		});
		passwd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fireUnlockEvent();
			}
		});
		
		
		ready.setVisible(false);
		//ready.setEnabled(false);
		abort.setColor(Color.red);
		abort.setEnabled(false);
		ready.setColor(Color.green);
		start.setColor(Color.yellow);
		
		JPanel tablePanel = new JPanel(new MigLayout());
		JPanel controlsPanel = new JPanel(new MigLayout());
		
		tablePanel.add(new JLabel("Time(min)  Temp(F)"),"wrap");
		tablePanel.add(getTable(),"wrap");
		
		controlsPanel.add(load,"wrap");
		controlsPanel.add(new JLabel("Cycle Name"));
		controlsPanel.add(cycleName,"wrap");
		
		controlsPanel.add(new JLabel("Target Pressure (Tons)"));
		controlsPanel.add(tons,"wrap");
		controlsPanel.add(new JLabel("Current Pressure (Tons)"));
		controlsPanel.add(currentPressure,"wrap");
		controlsPanel.add(new JLabel("Current Tempreture (F)"));
		controlsPanel.add(currentTemp,"wrap");
		
		currentPressure.setEditable(false);
		currentTemp.setEditable(false);		
		
		
		controlsPanel.add(save,"wrap");
		controlsPanel.add(setTemp,"wrap");
		controlsPanel.add(start);
		controlsPanel.add(ready,"wrap");
		controlsPanel.add(abort,"wrap");
		
		controlsPanel.add(new JLabel("Administrator Mode"));
		controlsPanel.add(passwd,"wrap");
		controlsPanel.add(unlock);
		controlsPanel.add(lock,"wrap");
		
		
		JPanel interfacePanel = new JPanel(new MigLayout());
		
		interfacePanel.add(tablePanel);
		interfacePanel.add(controlsPanel);
		
		add(interfacePanel,"wrap");
		//add(graph,"wrap");
		graph.onCycleStart(0,new CycleConfig(getTableDataMatrix(),getPressureSetpoint()));
		//Load in default values on startup
		setCycleConfig(new CycleConfig());
		cycleName.addItem(defaultFileString);
		setEnabled(true);
	}
	
	private double getStartingTempreture(){
		return getTableData()[0][1];// first temp in the table
	}
	
	public void setSelectedFile(String name){
		for(int i=0;i<cycleName.getItemCount();i++){
			if(cycleName.getItemAt(i).toString().equalsIgnoreCase(name)){
				cycleName.setSelectedIndex(i);
			}
		}
	}
	
	public String getSelectedFile(){
		return cycleName.getSelectedItem().toString();
	}
	
	private void fireUnlockEvent(){
		String p = new String(passwd.getPassword());
		if(p.equals(adminPassword)){
			setDataTebleLockState(false);
		}else{
			setDataTebleLockState(true);
			JOptionPane.showMessageDialog(null,   "Administration password invalid", "Administration Mode", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void setDataTebleLockState(boolean b){
		passwd.setText("");
		unlock.setEnabled(b);
		lock.setEnabled(!b);
		if(b){
			table.setEnabled(false);
			table.setBackground(Color.white);
		}else{
			table.setEnabled(true);
			table.setBackground(Color.yellow);
			JOptionPane.showMessageDialog(null,   "Administration Mode Unlocked", "Administration Mode", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private CycleConfig getCurrentCycleConfig(){
		return new CycleConfig(getTableDataMatrix(),getPressureSetpoint() );
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
		//System.out.println("Target Pressure = "+t+" current = "+c);
		return (		c>=t-bound && 
						c<=t+bound);
	}
	
	private void abort(){
		System.out.println("Press Aborted");
		abort.setEnabled(false);
		start.setEnabled(false);
		ready.setVisible(false);
		setTemp.setEnabled(true);
	}
	
	@Override
	public void setEnabled(boolean b){
		setDataTebleLockState(true);
		if(!b){
			unlock.setEnabled(false);
			lock.setEnabled(false);
		}
		start.setEnabled(false);
		abort.setEnabled(false);
		ready.setVisible(false);
		
		passwd.setEnabled(b);
		tons.setEnabled(b);
		save.setEnabled(b);
		cycleName.setEnabled(b);
		load.setEnabled(b);
		setTemp.setEnabled(b);

		super.setEnabled(b);
	}
	
	public void setCycleConfig(CycleConfig conf){
		graph.onAbortCycle(0);
		tons.setText(new DecimalFormat( " 000.000 " ).format(conf.getPressure()));
		setTransform(conf.getTimeTemp());
		if(conf.getConfigFile() == null){
			// do nothing with the files
		}else{
			 addAvailibleFile(conf.getConfigFile());
		}
	}
	
	public ArrayList<File> getAvailibleFiles(){
		return availibleFiles;
	}
	
	public boolean addAvailibleFile(File file){
		boolean add = true;
		for(File f : availibleFiles){
			if(f.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())){
				add=false;
			}
		}
		if(add){
			availibleFiles.add(file);
			String name = file.getName();
			cycleName.addItem(name);
			cycleName.setSelectedItem(name);
			fm.save();
		}
		return add;
	}

	private void setTransform(Matrix m){
		//getTable().setEnabled(false);
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
		//getTable().setEnabled(true);
		//System.out.println("Matrix display setting data "+m);
	}
	public double[][] getTableData() {
		double[][] data = new double[width][hight];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < hight; j++) {
				String current = getTable().getValueAt(i, j).toString();
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
            graph.onCycleStart(0,getCurrentCycleConfig());
        }
	}

	@Override
	public void onCycleStart(int i, CycleConfig config) {
		if(i==0 && usePress0||i==1 && usePress1){
			new Thread(){
				public void run(){
					//System.out.println("Starting press and hold thread");
					ButtonModel aModel = start.getModel();
					while(aModel.isArmed() && aModel.isPressed()){
						ThreadUtil.wait(100);
						if(	isPressReady()){
							ready.setVisible(true);
						}
					}
					//System.out.println("End press and hold thread");
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
		if(i==0 && usePress0||i==1 && usePress1){
			graph.onPressureChange(i, pressureTons);
			currentPressure.setText(new DecimalFormat( " 000.000 " ).format(pressureTons));
		}
	}

	@Override
	public void onTempretureChange(int i, final double degreesFarenhight) {
		if(i==0 && usePress0||i==1 && usePress1){
			graph.onTempretureChange(i, degreesFarenhight);
			currentTemp.setText(new DecimalFormat( " 000.000 " ).format(degreesFarenhight));
			new Thread(){
				public void run(){
					ThreadUtil.wait(100);
					if(waitingForTemp){
						if(!(	degreesFarenhight > (getStartingTempreture()+1) || 
								degreesFarenhight < (getStartingTempreture()-1))){
							start.setEnabled(true);
							setTemp.setEnabled(false);
							waitingForTemp=false;
						}
						
					}
				}
			}.start();
			
		}
	}

	public void setConfigFileManager(OggiePressConfigFileManager fm) {
		this.fm = fm;	
	}

	public String getXml() {
		String s ="";
		for(int i=0;i<availibleFiles.size();i++){
			s+="\t<file>"+availibleFiles.get(i).getAbsolutePath()+"</file>\n";
		}
		s+="\t<selected>";
		s+=getSelectedFile();
		s+="</selected>\n";
		return s;
	}

	public void setConfigNode(NodeList p) {
		String sel="Default";
		System.out.println("Config has "+p.getLength());
		for(int i=0;i<p.getLength();i++){
		    Node nNode = p.item(i);
		    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    	Element eElement = (Element)nNode;	
		    	System.out.println("Element = "+eElement);
		    	try{
		    		sel=XmlFactory.getTagValue("selected",eElement);
		    	}catch(Exception ex){
		    		//ex.printStackTrace();
		    	}
		    	try{
		    	    NodeList nlList= eElement.getElementsByTagName("file");
		    	    System.out.println("Have files # "+nlList.getLength());
		    	    for(int j=0;j<nlList.getLength();j++)
		    	    {
		    	    	Node nValue = ((NodeList) nlList.item(j)).item(0); 
		    	    	System.out.println("Value = "+nValue.getNodeValue());
			    		addAvailibleFile(new File(nValue.getNodeValue()));
		    	    }
		    	   // System.out.println("\t\t"+sTag+" = "+nValue.getNodeValue());
		    	    
		    	}catch(Exception ex){
		    		//ex.printStackTrace();
		    	}
		    }else{
		    	System.out.println("Not Element Node");
		    }
		}
		
		setSelectedFile(sel);
	}

	@Override
	public void onCycleIndexUpdate(int index, int press, double newTargetTemp) {
		// TODO Auto-generated method stub
		
	} 
}
