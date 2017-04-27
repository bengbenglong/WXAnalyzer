/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector.timeComponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.ImageUtilities;

/**
 *
 * @author hp-6380
 */
public class DateSetting extends javax.swing.JPanel {

    public boolean settedFlag;//判断面板有没有设置过时间
    public boolean jrb5SelectedFlag;

    public static final String SELECTED_ALL="ALL";
    
    public String startTime;
    public String endTime;
    
    public  volatile String selectedStartTime;
    public  volatile String selectedEndTime;
    
    private ButtonGroup group=new ButtonGroup();
    private SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * Creates new form DataSetting
     */
    public DateSetting() {
        initComponents();
        enableTimeCalendar(false);
        group.add(jrb1);
        group.add(jrb2);
        group.add(jrb3);
        group.add(jrb4);
        group.add(jrb5);
        group.add(jrb6);
        initAction();
        
        //注册起始和结束日期选择器
        edu.seu.dataselector.oldPerspective.MyCalendar calendar1 = edu.seu.dataselector.oldPerspective.MyCalendar.getInstance("yyyy-MM-dd");
        edu.seu.dataselector.oldPerspective.MyCalendar calendar2 = edu.seu.dataselector.oldPerspective.MyCalendar.getInstance("yyyy-MM-dd");
        calendar1.register(startTimeField);
        calendar2.register(endTimeField);
        startTimeField.setToolTipText("点击选择起始时间");
        endTimeField.setToolTipText("点击选择结束时间");
        add(startTimeField);
        add(endTimeField);
        
    }
    
    public String getStartField(){
        return startTimeField.getText();
    }
    
    public String getEndField(){
        return endTimeField.getText();
    }
    
    private void initAction(){
        jrb1.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTimeCalendar(false);
                Calendar now=Calendar.getInstance();
                now.add(Calendar.DATE, -1);
                startTime=dateFormat.format(now.getTime());
                endTime=dateFormat.format(Calendar.getInstance().getTime());
                jrb5SelectedFlag=false;
            }
        });
        
        jrb2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTimeCalendar(false);
                Calendar now=Calendar.getInstance();
                now.add(Calendar.DATE, -7);
                startTime=dateFormat.format(now.getTime());
                endTime=dateFormat.format(Calendar.getInstance().getTime());
                jrb5SelectedFlag=false;
            }
        });
        
        jrb3.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTimeCalendar(false);
                Calendar now=Calendar.getInstance();
                now.add(Calendar.MONTH, -1);
                startTime=dateFormat.format(now.getTime());
                endTime=dateFormat.format(Calendar.getInstance().getTime());
                jrb5SelectedFlag=false;
            }
        });
        
        jrb4.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTimeCalendar(false);
                Calendar now=Calendar.getInstance();
                now.add(Calendar.YEAR, -1);
                startTime=dateFormat.format(now.getTime());
                endTime=dateFormat.format(Calendar.getInstance().getTime());
                jrb5SelectedFlag=false;
            }
        });
        
        jrb5.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTimeCalendar(true);
                jrb5SelectedFlag=true;
            }
        });
        
        jrb6.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTimeCalendar(false);
                startTime=DateSetting.SELECTED_ALL;
                endTime=DateSetting.SELECTED_ALL;
                jrb5SelectedFlag=false;
            }
        });
        
        //添加文本文本框内容监听器，监听文本框内容改变（选择不同时间）
        startTimeField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                //判断时间区间选择是否正确，不正确时弹出提示对话框
                //当起始时间和结束时间都选择是判断，其他情形不做判断
                if(!startTimeField.getText().equals("----年--月--日")&&!endTimeField.getText().equals("----年--月--日")){
                    if(startTimeField.getText().compareTo(endTimeField.getText())>0){
                        //弹出提示框提示时间间隔选择错误并重置时间
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run(){
                                JOptionPane.showMessageDialog(null,"结束时间不能早于开始时间","警告",JOptionPane.ERROR_MESSAGE,ImageUtilities.loadImageIcon("edu/seu/lizhenglong/image/warning_32.png", false));
                                startTimeField.setText("----年--月--日");
                            }
                        });
                    }
                }
                startTime=startTimeField.getText()+" 00:00:00";
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        
        endTimeField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                //判断时间区间选择是否正确，不正确时弹出提示对话框
                //当起始时间和结束时间都选择是判断，其他情形不做判断
               if(!startTimeField.getText().equals("----年--月--日")&&!endTimeField.getText().equals("----年--月--日")){
                   if(startTimeField.getText().compareTo(endTimeField.getText())>0){
                       //弹出时间间隔选择错误提示框并重置时间
                       SwingUtilities.invokeLater(new Runnable(){
                           @Override
                           public void run() {
                               JOptionPane.showMessageDialog(null,"结束时间不能早于开始时间","警告",JOptionPane.ERROR_MESSAGE,ImageUtilities.loadImageIcon("edu/seu/lizhenglong/image/warning_32.png", false));
                               endTimeField.setText("----年--月--日");
                           }
                       });
                   }
               }
               endTime=endTimeField.getText()+" 23:59:59";
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        
        resetButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        startTimeField.setText("----年--月--日");
                        endTimeField.setText("----年--月--日");
                    }
                });
            }
        });
    }
    
    private void enableTimeCalendar(boolean flag){
        startTimeField.setEnabled(flag);
        endTimeField.setEnabled(flag);
        jLabel3.setEnabled(flag);
        resetButton.setEnabled(flag);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startTimeField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        endTimeField = new javax.swing.JTextField();
        resetButton = new javax.swing.JButton();
        jrb2 = new javax.swing.JRadioButton();
        jrb1 = new javax.swing.JRadioButton();
        jrb3 = new javax.swing.JRadioButton();
        jrb4 = new javax.swing.JRadioButton();
        jrb5 = new javax.swing.JRadioButton();
        jrb6 = new javax.swing.JRadioButton();

        setMinimumSize(new java.awt.Dimension(313, 204));

        startTimeField.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        startTimeField.setText("----年--月--日"); // NOI18N

        jLabel3.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "至"); // NOI18N

        endTimeField.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        endTimeField.setText("----年--月--日"); // NOI18N

        resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/oldPerspective/reset.png"))); // NOI18N
        resetButton.setContentAreaFilled(false);
        resetButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/oldPerspective/reset_selected.png"))); // NOI18N
        resetButton.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/oldPerspective/reset_selected.png"))); // NOI18N

        jrb2.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jrb2, " 7天内数据"); // NOI18N

        jrb1.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jrb1, " 1天内数据"); // NOI18N

        jrb3.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jrb3, " 1个月内数据"); // NOI18N

        jrb4.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jrb4, " 1年内数据"); // NOI18N

        jrb5.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jrb5, " 自由选择时间："); // NOI18N

        jrb6.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jrb6, " 不设限"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jrb6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jrb2)
                            .addComponent(jrb1)
                            .addComponent(jrb3)
                            .addComponent(jrb4)
                            .addComponent(jrb5))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jrb1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrb2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrb3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrb4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrb5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jrb6))
                    .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField endTimeField;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JRadioButton jrb1;
    private javax.swing.JRadioButton jrb2;
    private javax.swing.JRadioButton jrb3;
    private javax.swing.JRadioButton jrb4;
    private javax.swing.JRadioButton jrb5;
    private javax.swing.JRadioButton jrb6;
    private javax.swing.JButton resetButton;
    private javax.swing.JTextField startTimeField;
    // End of variables declaration//GEN-END:variables
}
