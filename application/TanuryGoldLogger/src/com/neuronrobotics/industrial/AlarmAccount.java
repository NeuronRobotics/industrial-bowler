package com.neuronrobotics.industrial;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;

import net.miginfocom.swing.MigLayout;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class AlarmAccount extends JPanel implements IBathMoniterUpdateListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3719573762649982776L;
	
	JTextField adminAccount  = new JTextField("artillect.artificial.intillect");
	JPasswordField password = new JPasswordField("Asusserver37");
	ArrayList<JTextField> accounts = new ArrayList<JTextField>();

	public AlarmAccount(){
		setName("Alarm Accounts");
		setLayout(new MigLayout("", "[][]", "[][]"));
		
		add(new JLabel("Admin Account"), 		"cell 0 0,growx");
		add(adminAccount, 	"cell 1 0,growx");
		add(new JLabel("@gmail.com"), 			"cell 2 0,growx");
		add(password, 		"cell 3 0,growx");
		
		for(int i=1;i<11;i++){
			JTextField tmp = new JTextField(10);
			accounts.add(tmp);
			add(new JLabel("To Notify"), 		"cell 0 "+i+",growx");
			add(tmp, 			"cell 1 "+i+",growx");
			add(new JLabel("@gmail.com"), 		"cell 2 "+i+",growx");
		}
		
	}

	@Override
	public void onNameChange(String newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValueChange(BathMoniterEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAlarmEvenFire(BathAlarmEvent ev) {
		System.out.println("Sending Emails");
		for(JTextField jtf: accounts){
			if(jtf.getText().length()>0){
				sendEmail(jtf.getText()+"@gmail.com", adminAccount + "@gmail.com", new String(password.getPassword()), ev.toString());
			}
		}
		//sendEmail("kharrington@neuronrobotics.com", "mad.hephaestus@gmail.com", "4CoreCube", ev.toString());
	}

	@Override
	public void onClearData() {
		// TODO Auto-generated method stub
		
	}
	
	private void sendEmail(String to, final String from, final String password, String content){

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.put("mail.smtp.auth", "true");
	      properties.put("mail.smtp.starttls.enable", "true");
	      properties.put("mail.smtp.host", "smtp.gmail.com");
	      properties.put("mail.smtp.port", "587");

	      // Get the default Session object.
	      Session session = Session.getInstance( properties,
	    		  new javax.mail.Authenticator() {
	    			protected PasswordAuthentication getPasswordAuthentication() {
	    				return new PasswordAuthentication(from, password);
	    			}
	    		  });
	      try{

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
				message.setSubject("ALARM from Bath Monitor");
				message.setText(content);
	 
				Transport.send(message);
	 
				System.out.println("Done");

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   
	}

}
