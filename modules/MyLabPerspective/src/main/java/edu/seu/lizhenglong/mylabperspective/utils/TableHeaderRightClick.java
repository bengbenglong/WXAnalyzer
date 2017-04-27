/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.lizhenglong.mylabperspective.utils;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author hp-6380
 */
public class TableHeaderRightClick {
    private JPopupMenu popupMenu;
    
    public TableHeaderRightClick(final JTable jTable,final int column){
        super();
        
        JMenuItem filterM=new JMenuItem("过滤",new ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/filter_16.png")));
        filterM.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        String keywords=(String)JOptionPane.showInputDialog(
                                popupMenu,
                                "请输入关键词：\t",
                                "过滤列”"+jTable.getColumnName(column)+"”",
                                JOptionPane.YES_NO_OPTION,
                                new ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/filter_32.png")),
                                null,
                                null
                        );
                        if(keywords!=null){
                            TableRowSorter sorter=(TableRowSorter) jTable.getRowSorter();
                            
                            sorter.setRowFilter(RowFilter.regexFilter(keywords,column));
                        }
                    }
                });
            }
            
        });
        
        
        JMenuItem clearSorterM=new JMenuItem("取消排序/过滤",null);
        clearSorterM.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(ActionEvent e){
                jTable.setRowSorter( new TableRowSorter<>(jTable.getModel()) );
                jTable.getTableHeader().updateUI();
            }
            
        });
        
        
        JMenuItem removeColumnM=new JMenuItem("隐藏此列",null);
        removeColumnM.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        //弹出确认删除对话框
                        int num=JOptionPane.showConfirmDialog(
                                    popupMenu, 
                                    "确认隐藏列“"+jTable.getColumnName(column)+"”？" , "提醒", 
                                    JOptionPane.YES_NO_OPTION , 
                                    JOptionPane.QUESTION_MESSAGE , 
                                    new ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/warning_32.png")));
                        if(num==JOptionPane.YES_OPTION){
                            String columnName=jTable.getColumnName(column);
                            jTable.getColumnModel().removeColumn(jTable.getColumn(columnName));
                        }
                    }
                    
                });
            }
            
        });
        
        popupMenu=new JPopupMenu();
        popupMenu.add(filterM);
        popupMenu.add(clearSorterM);
        popupMenu.add(removeColumnM);
        
    }
    
    public void showMenu(MouseEvent e){
            Point p=e.getPoint();
            popupMenu.show(e.getComponent(), p.x, p.y);
            
        }
}
