package com.rbg.strike;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Strike extends JFrame {
	private static final long serialVersionUID = 1L;

	private Connection con = null;
	private Statement st = null;
	private ResultSet rs = null;
	private String sql;
	private String tdir="Towers";
	
	public void fillCombo(JComboBox combo,String topdir, boolean dir, String filemask) {
		File filelist = new File(topdir);
		String[] names=filelist.list();
		
		combo.removeAllItems();
		for (String name : names) {
			if (dir) if (new File(topdir+"\\" + name).isDirectory()) {
				combo.addItem(name);
				System.out.println("Added "+name);
			}
		}
	}	
	
	public void fillDirCombo(JComboBox combo, String topdir) {
		fillCombo(combo, topdir, true, "");
	}
	
	public Strike() {
		 try {
			JPanel mainPanel = new JPanel();
			add(mainPanel);
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
		    
	    	File topdir = new File(tdir);
	    	String[] names = topdir.list();
//rbg	    	 
/*			File dir = new File(tdir);
			File[] files = dir.listFiles(new FilenameFilter() {
			        @Override
			        public boolean accept(File dir, String filename) {
			            return filename.endsWith(".rbg");
			        }
			 }); */
			
			final JComboBox tower = new JComboBox();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(5,5,10,10);
		    c.gridx = 0;
		    c.gridy = 3;
		    c.gridwidth = 3;
			mainPanel.add(tower,c);
			fillDirCombo(tower,tdir);
			
			JButton addTower = new JButton("ADD");
			c.gridx = 4;
		    c.gridy = 3;
		    c.gridwidth = 1;
			mainPanel.add(addTower,c);
			addTower.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { 
				   String answer = JOptionPane.showInputDialog(null, "Enter a name for the new Tower","Enter name", JOptionPane.PLAIN_MESSAGE);
				   if (!answer.equals("")) {
					   System.out.println("creating "+answer);
					   new File(tdir+"\\"+answer).mkdirs();
					   fillDirCombo(tower,tdir);
				   }

				} 
			});
			
			JButton delTower = new JButton("DEL");
			c.gridx = 5;
		    c.gridy = 3;
		    c.gridwidth = 1;
			mainPanel.add(delTower,c);
			delTower.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { 
					String towername=(String)tower.getSelectedItem();
					int response = JOptionPane.showConfirmDialog(null, "All the training files and touches for "+towername+" will be deleted. Do you want to continue?", "Confirm",
					        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				   if (response == JOptionPane.YES_OPTION) {
					   System.out.println("deleting ");
					   File index = new File(tdir+"\\"+towername);
					   String[]entries = index.list();
					   for(String s: entries){
					       File currentFile = new File(index.getPath(),s);
					       currentFile.delete();
					   }
					   index.delete();
					   fillDirCombo(tower,tdir);
					   
				   }

				} 
			});
			//
			//
			//
			JComboBox trainingdata = new JComboBox();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(5,5,10,10);
		    c.gridx = 0;
		    c.gridy = 4;
		    c.gridwidth = 3;
			mainPanel.add(trainingdata,c);
			
			JButton addtrainingdata = new JButton("ADD");
			c.gridx = 4;
		    c.gridy = 4;
		    c.gridwidth = 1;
			mainPanel.add(addtrainingdata,c);
			
			JButton deltrainingdata = new JButton("DEL");
			c.gridx = 5;
		    c.gridy = 4;
		    c.gridwidth = 1;
			mainPanel.add(deltrainingdata,c);
		
			
			pack();
//			st.close();
		 } catch (Exception e) {e.printStackTrace();}	
			
	}
	
	
	
	
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Strike ex = new Strike();
					ex.setLocationRelativeTo(null);
					ex.setTitle("Grimbles Strikeometer");
					ex.setVisible(true);
				}
			});
		} catch (Exception e) {e.printStackTrace();}
	}
}
