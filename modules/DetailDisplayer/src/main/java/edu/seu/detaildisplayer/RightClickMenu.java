/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.detaildisplayer;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.openide.util.ImageUtilities;

/**
 *
 * @author hp-6380
 */
public class RightClickMenu {

    private JPopupMenu pMenu;
    
    public RightClickMenu(final JTable jTable){
        
        super();
        
//        JMenuItem selectAllM=new JMenuItem("全选",ImageUtilities.loadImageIcon("edu/seu/dataselector/oldPerspective/select.png", false));
//        selectAllM.setAccelerator(KeyStroke.getKeyStroke('A',java.awt.Event.CTRL_MASK,true));
//        selectAllM.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e){
//                int columns=jTable.getColumnCount();
//                int rows=jTable.getRowCount();
//                jTable.setColumnSelectionInterval(0, columns-1);
//                jTable.setRowSelectionInterval(0, rows-1);
//            }
//        });
        
        JMenuItem copyM=new JMenuItem("复制",ImageUtilities.loadImageIcon("edu/seu/dataselector/oldPerspective/copy.png", false));
        copyM.setAccelerator(KeyStroke.getKeyStroke('C',java.awt.Event.CTRL_MASK,true));
        copyM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows=jTable.getSelectedRows();
                int numOfColumns=jTable.getColumnCount();
                for(int i=0;i<selectedRows.length;i++){
                    for(int j=0;j<numOfColumns;j++){
                        Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();  
                        Transferable tText = new StringSelection((String) jTable.getValueAt(selectedRows[i], j));   
                        clip.setContents(tText, null);   
                    }
                }
            }
        });
        
//        JMenuItem clearAllM=new JMenuItem("清空",ImageUtilities.loadImageIcon("edu/seu/dataselector/oldPerspective/delete.png", false));
//        clearAllM.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int rowNums=jTable.getRowCount();
//                DefaultTableModel tableModel=(DefaultTableModel) jTable.getModel();
//                for(int i=0;i<rowNums;i++)
//                    tableModel.removeRow(0);
//            }
//            
//            
//        });
        
        
        
        
        pMenu=new JPopupMenu();
//        pMenu.add(selectAllM);
        pMenu.add(copyM);
//        pMenu.add(clearAllM);
//        pMenu.addSeparator();
        
    }
    
    public void showPMenu(MouseEvent e){
        Point p=e.getPoint();
        pMenu.show(e.getComponent(), p.x, p.y);
        
    }
}
