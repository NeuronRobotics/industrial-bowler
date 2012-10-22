package com.neuronrobotics.commercial.oggie;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class RoundButton extends JButton {
  /**
	 * 
	 */
	private static final long serialVersionUID = -5236632037590342098L;
	private Color color = Color.green;
	ImageIcon iconRed = new ImageIcon(RoundButton.class.getResource("red.png"));
	ImageIcon iconRedPressed = new ImageIcon(RoundButton.class.getResource("red_pressed.png"));
	
	ImageIcon iconGreen = new ImageIcon(RoundButton.class.getResource("green.png"));
	ImageIcon iconGreenPressed = new ImageIcon(RoundButton.class.getResource("green_pressed.png"));
	
	ImageIcon iconYellow= new ImageIcon(RoundButton.class.getResource("yellow.png"));
	ImageIcon iconYellowPressed = new ImageIcon(RoundButton.class.getResource("yellow_pressed.png"));
	public RoundButton(String label,Dimension size) {
	    super(label);
		setIcon(iconRed); 
		setBorderPainted(false); 
		setFocusPainted(false); 
		setContentAreaFilled(false);  
	  }
	
	// Paint the round background and label.
	  protected void paintComponent(Graphics g) {
	    if (getModel().isArmed()) {	
		    if(color.equals(Color.red)){
				setIcon(iconRedPressed); 
			}
			if(color.equals(Color.green)){
				setIcon(iconGreenPressed); 
			}
			if(color.equals(Color.yellow)){
				setIcon(iconYellowPressed); 
			} 
	    } else {
			if(color.equals(Color.red)){
				setIcon(iconRed); 
			}
			if(color.equals(Color.green)){
				setIcon(iconGreen); 
			}
			if(color.equals(Color.yellow)){
				setIcon(iconYellow); 
			}
	    }
	
	// This call will paint the label and the 
	   // focus rectangle.
	    super.paintComponent(g);
	  }
	
	// Paint the border of the button using a simple stroke.
	  protected void paintBorder(Graphics g) {
	    g.setColor(getForeground());
	    //g.drawOval(0, 0, getSize().width-1, getSize().height-1);
	  }
	
	// Hit detection.
	  Shape shape;
	  public boolean contains(int x, int y) {
	// If the button has changed size, 
	   // make a new shape object.
	    if (shape == null || 
	      !shape.getBounds().equals(getBounds())) {
	      shape = new Ellipse2D.Float(0, 0, 
	        getWidth(), getHeight());
	    }
	    return shape.contains(x, y);
	  }
//	@Override
//	public void setEnabled(boolean b){
//		super.setEnabled(b);
//		
//	}
	public Color getColor() {
		if(isEnabled())
			return color;
		return Color.lightGray;
	}
	
	public void setColor(Color color) {
		if(color.equals(Color.red)){
			setIcon(iconRed); 
		}
		if(color.equals(Color.green)){
			setIcon(iconGreen); 
		}
		if(color.equals(Color.yellow)){
			setIcon(iconYellow); 
		}
		
		this.color = color;
	}

}
