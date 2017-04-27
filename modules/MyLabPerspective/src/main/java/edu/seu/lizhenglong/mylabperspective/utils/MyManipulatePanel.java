/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.lizhenglong.mylabperspective.utils;

import edu.seu.networkbuild.api.BuildAttribute;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.ImageUtilities;

/**
 *
 * @author hp-6380
 */
public class MyManipulatePanel extends javax.swing.JPanel {

    private ComboBoxModel<String> comboBoxModel=new DefaultComboBoxModel<>(new String[]{"TB_WX_ACCOUNT","TB_WX_FRIENDS","TB_WX_GROUP","TB_WX_GROUPMEMBER","TB_WX_GROUPMESSAGE","TB_WX_MSGMONITOR"});
    
    /**
     * Creates new form OptionsPanel
     */
    public MyManipulatePanel() {
        initComponents();
        
        //注册起始和结束日期选择器
        MyCalendar calendar1 = MyCalendar.getInstance("yyyy-MM-dd");
        MyCalendar calendar2 = MyCalendar.getInstance("yyyy-MM-dd");
        calendar1.register(startTimeField);
        calendar2.register(endTimeField);
        startTimeField.setToolTipText("点击选择起始时间");
        endTimeField.setToolTipText("点击选择结束时间");
        add(startTimeField);
        add(endTimeField);
                
        
        jLabel2.setEnabled(false);
        jLabel3.setEnabled(false);
        startTimeField.setEnabled(false);
        endTimeField.setEnabled(false);
        resetButton.setVisible(false);
        

        initAction();
    }
    
    /**
     * 取得所选取的时间间隔.返回的时间格式为 起始时间+“，”+结束时间。
     * @return 
     */
    public String getSelectedTime(){
        String selectedTime=startTimeField.getText()+","+endTimeField.getText();
        return selectedTime;
    }
    
    /**
     * 取得所选择的数据表名称
     * @return 
     */
    public String getSelectedTable(){
        return (String)tableComboBox.getSelectedItem();
    }
    
    /**
     * 启用comboBox组合框.
     */
    public void setComboBoxEnabledT(){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                tableComboBox.setEnabled(true);
            }
        });
    }
    
    /**
     * 禁用comboBox组合框.
     */
    public void setComboBoxEnabledF(){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                tableComboBox.setEnabled(false);
            }
        });
    }
    
    private void initAction(){
        //为tableComboBox添加监听器
        tableComboBox.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                final String selectedTable=(String) tableComboBox.getSelectedItem();
                
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        //如果选择了表TB_WX_GROUPMESSAGE或者表TB_WX_MSGMONITOR，则激活时间选择控件
                        //否则时间选择控件不能使用。
                        if(selectedTable.equals("TB_WX_GROUPMESSAGE")||selectedTable.equals("TB_WX_MSGMONITOR")){
                            startTimeField.setEnabled(true);
                            endTimeField.setEnabled(true);
                            jLabel2.setEnabled(true);
                            jLabel3.setEnabled(true);
                            resetButton.setVisible(true);
                        }else{
                            startTimeField.setEnabled(false);
                            endTimeField.setEnabled(false);
                            jLabel2.setEnabled(false);
                            jLabel3.setEnabled(false);
                            resetButton.setVisible(false);
                            netTypeComboBox.setEnabled(false);
                        }
                        if(selectedTable.equals("TB_WX_GROUPMEMBER")){
                            netTypeComboBox.setEnabled(true);
                            ComboBoxModel<String> netTypeModel=new DefaultComboBoxModel<>(new String[]{"群与群关系网络","群成员网络"});
                            netTypeComboBox.setModel(netTypeModel);
                        }
                        if(selectedTable.equals("TB_WX_FRIENDS")){
                            netTypeComboBox.setEnabled(true);
                            ComboBoxModel<String> netTypeModel=new DefaultComboBoxModel<>(new String[]{"好友与好友关系网络","好友成员网络"});
                            netTypeComboBox.setModel(netTypeModel);
                        }
                    }
                });
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
    
    public String getBuildType(){
        String s=(String)netTypeComboBox.getSelectedItem();
        switch(s){
            case "群与群关系网络": 
                return BuildAttribute.GROUP_MEMBER_RELATION;
            case "群成员网络": 
                return BuildAttribute.GROUP_MEMBER_PLAIN;
            case "好友与好友关系网络": 
                return BuildAttribute.FRIENDS_RELATION;
            case "好友成员网络":
                return BuildAttribute.FRIENDS_PLAIN;
            default :
                return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tableComboBox = new javax.swing.JComboBox<>();
        startTimeField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        endTimeField = new javax.swing.JTextField();
        resetButton = new javax.swing.JButton();
        netTypeComboBox = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jLabel1.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        jLabel1.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        jLabel2.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.jLabel2.text")); // NOI18N

        tableComboBox.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        tableComboBox.setModel(comboBoxModel);

        startTimeField.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        startTimeField.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.startTimeField.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        jLabel3.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.jLabel3.text")); // NOI18N

        endTimeField.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        endTimeField.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.endTimeField.text")); // NOI18N

        resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/reset_16.png"))); // NOI18N
        resetButton.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.resetButton.text")); // NOI18N
        resetButton.setContentAreaFilled(false);
        resetButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/reset_16_dark.png"))); // NOI18N
        resetButton.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/reset_16_dark.png"))); // NOI18N

        jLabel4.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        jLabel4.setText(org.openide.util.NbBundle.getMessage(MyManipulatePanel.class, "MyManipulatePanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tableComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(netTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(netTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(resetButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField endTimeField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> netTypeComboBox;
    private javax.swing.JButton resetButton;
    private javax.swing.JTextField startTimeField;
    private javax.swing.JComboBox<String> tableComboBox;
    // End of variables declaration//GEN-END:variables
}
