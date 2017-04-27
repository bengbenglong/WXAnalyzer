/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.screenshot;

/**
 *
 * @author hp-6380
 */
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
  
import javax.swing.JButton;  
import javax.swing.JFileChooser;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
  
public class FileChooser extends JFrame implements ActionListener{  
	
	public static void main(String[] args) {
		FileChooser fc=new FileChooser();
		fc.execute();
	}
	
    JButton open=null;  
    public void execute() {  
    	 open=new JButton("open");  
         this.add(open);  
         this.setBounds(400, 200, 100, 100);  
         this.setVisible(true);  
//         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
         open.addActionListener(this);  
    }  
//    public FileChooser(){  
//        open=new JButton("open");  
//        this.add(open);  
//        this.setBounds(400, 200, 100, 100);  
//        this.setVisible(true);  
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
//        open.addActionListener(this);  
//    }  
    @Override  
    public void actionPerformed(ActionEvent e) {  
        // TODO Auto-generated method stub  
        JFileChooser jfc=new JFileChooser();  
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );  
        jfc.showDialog(new JLabel(), "选择");  
    }  
} 
