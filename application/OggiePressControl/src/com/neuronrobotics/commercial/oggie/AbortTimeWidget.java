package com.neuronrobotics.commercial.oggie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class AbortTimeWidget extends JPanel implements IPressHardwareListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8841780303108489096L;
	private RoundButton abort = new RoundButton("Abort Cycle",new Dimension(100, 100));
	private JLabel timeRemaining   = new JLabel("000 min 00 sec");
	private PressHardware pressControler;
	private boolean usePress0;
	private boolean usePress1;
	public AbortTimeWidget(final boolean usePress0, final boolean usePress1,PressHardware p){
		this.usePress0 = usePress0;
		this.usePress1 = usePress1;
		this.pressControler = p;
		abort.setColor(Color.red);
		abort.setEnabled(false);
		timeRemaining.setFont(new Font("Dialog", Font.PLAIN, 24));
		abort.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(usePress0)
					pressControler.abortCycle(0);
				if(usePress1)
					pressControler.abortCycle(1);
			}
		});
		setLayout(new MigLayout());
		add(abort,"wrap");
		add(timeRemaining);
		p.addPressHardwareListener(this);
	}
	
	@Override
	public void setEnabled(boolean b){
		if(b){
			abort.setEnabled(true);
//			timeRemaining.setVisible(true);
		}else{
//			timeRemaining.setVisible(false);
			abort.setEnabled(false);
			abort.setText("Abort Cycle");
			abort.setColor(Color.red);
			timeRemaining.setText("000 min 00 sec");
		}
	}

	@Override
	public void onCycleStart(int i, CycleConfig config) {
		// TODO Auto-generated method stub
		if((usePress0 && i==0) ||(usePress1 && i==1))
			setEnabled(true);
	}

	@Override
	public void onAbortCycle(int i) {
		// TODO Auto-generated method stub
		if((usePress0 && i==0) ||(usePress1 && i==1))
			setEnabled(false);
	}

	@Override
	public void onPressureChange(int i, double pressureTons) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTempretureChange(int i, double degreesFarenhight) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCycleIndexUpdate(int currentTableIndex,
			double currentTableTime, double timeRemaining, int press,
			double newTargetTemp) {
		if((usePress0 && press==0) ||(usePress1 &&  press==1)){
			double minFract =  (double)((int) timeRemaining);
			double seconds = (timeRemaining - minFract )*59;
			this.timeRemaining.setText(new DecimalFormat( "000" ).format(minFract)+" min "+new DecimalFormat( "00" ).format(seconds)+" sec");
			if(currentTableIndex == CycleConfig.dataSize-1){
				abort.setText("Open Press");
				abort.setColor(Color.green);
			}
		}
	}

}
