/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.database.settings;

import edu.seu.database.drivers.DatabaseConnection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author hp-6380
 */
public class DBSettings extends javax.swing.JPanel {

    public static final String USER_NAME="USER_NAME";
    public static final String PASSWORD="PASSWORD";
    public static final String ADDRESS="ADDRESS";
    public static final String PORT="PORT";
    public static final String DB_NAME="DB_NAME";
    
    /**
     * Creates new form DBSettings
     */
    public DBSettings() {
        initComponents();
        initAction();
        initMyComponent();
        loadDefaultProperties();
    }
    
    private void initMyComponent(){
        ButtonGroup group=new ButtonGroup();
        group.add(radioButton1);
        group.add(radioButton2);
        group.setSelected(radioButton1.getModel(), true);
        setUserDif(false);
        defaultField.setEnabled(true);
    }
    
    private void loadDefaultProperties(){
        final Properties p=new Properties();
        FileInputStream fin=null;
        FileOutputStream fout=null;
        try{
            fin=new FileInputStream("DBSettings.properties");
        }catch(FileNotFoundException ex){
            try{
                fout=new FileOutputStream("DBSettings.properties");
            }catch(FileNotFoundException e){}
        }catch(IOException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "读取数据库默认连接配置文件出错");
        }
        try {
            fin=new FileInputStream("DBSettings.properties");
            p.load(fin);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                userNameText.setText(p.getProperty(USER_NAME));
                passwordText.setText(p.getProperty(PASSWORD));
                String address=p.getProperty(ADDRESS);
                String port=p.getProperty(PORT);
                String dbName=p.getProperty(DB_NAME);
                String url="jdbc:oracle:thin:"+address+":"+port+":"+dbName;
                defaultField.setText(url);
            }
        });
    }
    
    private boolean testConnection(){
        String userName=userNameText.getText();
        String password=new String(passwordText.getPassword());
        System.out.println("password"+password);
        String url;
        if(radioButton1.isSelected()){
            url=defaultField.getText();
        }else{
            url="jdbc:oracle:thin:"+addressText.getText()+":"+portText.getText()+":"+dbNameText.getText();
        }
        DatabaseConnection dbTest=new DatabaseConnection();
        return dbTest.testConnection(url ,userName, password);
    }
    
    
    private void setUserDif(boolean flag){
        addressField.setEnabled(flag);
        portField.setEnabled(flag);
        dbNameField.setEnabled(flag);
        addressText.setEnabled(flag);
        portText.setEnabled(flag);
        dbNameText.setEnabled(flag);
    }
    
    private void initAction(){
        radioButton1.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(radioButton1.isSelected()){
                    defaultField.setEnabled(true);
                    setUserDif(false);
                }
            }
        });
        
        radioButton2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(radioButton2.isSelected()){
                    setUserDif(true);
                    defaultField.setEnabled(false);
                }
            }
        });
        
        testConnectionButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                testResField.setText("数据库连接测试...");
                if(testConnection()){
                    testResField.setText("数据库连接成功！");
                }else{
                    testResField.setText("数据库连接失败");
                }
            }
        });
        
        saveButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                Properties pro=new Properties();
                FileOutputStream fos=null;
                try{
//                    String path=this.getClass().getResource("/edu/seu/database/settings/DBSettings.properties").toURI().getPath();
////                    String path=this.getClass().getResource("/DBSettings.properties").toURI().getPath();
//                    
                    fos=new FileOutputStream("DBSettings.properties");
                    
                    pro.setProperty(USER_NAME, userNameText.getText());
                    pro.setProperty(PASSWORD, new String(passwordText.getPassword()));
                    pro.setProperty(ADDRESS, addressText.getText());
                    pro.setProperty(PORT, portText.getText());
                    pro.setProperty(DB_NAME, dbNameText.getText());
                    
                    pro.store(fos, null);
//                    pro.put(USER_NAME, userNameText.getText());
//                    pro.put(PASSWORD, new String(passwordText.getPassword()));
//                    pro.put(ADDRESS, addressText.getText());
//                    pro.put(PORT, portText.getText());
//                    pro.put(DB_NAME, dbNameText.getText());
//                    pro.store(fos, null);
                    JOptionPane.showMessageDialog(null, "保存成功！");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    JOptionPane.showMessageDialog(null, "保存失败！");
                }finally{
                    try{
                        if(fos!=null){
                            fos.close();
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        refreshButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDefaultProperties();
            }
        });
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        userNameText = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        passwordText = new javax.swing.JPasswordField();
        jPanel2 = new javax.swing.JPanel();
        radioButton1 = new javax.swing.JRadioButton();
        radioButton2 = new javax.swing.JRadioButton();
        dbNameField = new javax.swing.JLabel();
        dbNameText = new javax.swing.JTextField();
        addressField = new javax.swing.JLabel();
        addressText = new javax.swing.JTextField();
        portField = new javax.swing.JLabel();
        portText = new javax.swing.JTextField();
        defaultField = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        testConnectionButton = new javax.swing.JButton();
        testResField = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setFont(new java.awt.Font("微软雅黑", 0, 18)); // NOI18N

        jLabel1.setFont(new java.awt.Font("微软雅黑", 0, 18)); // NOI18N
        jLabel1.setText("用户名："); // NOI18N

        userNameText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        userNameText.setText("wxgk"); // NOI18N

        jLabel2.setFont(new java.awt.Font("微软雅黑", 0, 18)); // NOI18N
        jLabel2.setText("密码："); // NOI18N

        passwordText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        passwordText.setText("jPasswordField1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        radioButton1.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        radioButton1.setText("默认设置"); // NOI18N

        radioButton2.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        radioButton2.setText("自定义设置"); // NOI18N

        dbNameField.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        dbNameField.setText("数据库名称："); // NOI18N

        dbNameText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        dbNameText.setText("orcl"); // NOI18N

        addressField.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        addressField.setText("主机地址："); // NOI18N

        addressText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        addressText.setText("127.0.0.1"); // NOI18N

        portField.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        portField.setText("端口号："); // NOI18N

        portText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        portText.setText("1521"); // NOI18N

        defaultField.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        defaultField.setText(org.openide.util.NbBundle.getMessage(DBSettings.class, "DBSettings.defaultField.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButton2)
                            .addComponent(radioButton1)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(dbNameField)
                                        .addGap(18, 18, 18)
                                        .addComponent(dbNameText))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(portField)
                                            .addComponent(addressField))
                                        .addGap(34, 34, 34)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(addressText, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                                            .addComponent(portText)))))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(defaultField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(defaultField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(radioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressField)
                    .addComponent(addressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portField)
                    .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbNameField)
                    .addComponent(dbNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        saveButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        saveButton.setText("保存设置"); // NOI18N

        testConnectionButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        testConnectionButton.setText("测试连接"); // NOI18N

        testResField.setText(org.openide.util.NbBundle.getMessage(DBSettings.class, "DBSettings.testResField.text")); // NOI18N

        refreshButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        refreshButton.setText("刷新"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userNameText)
                            .addComponent(passwordText)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(testResField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnectionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {refreshButton, saveButton, testConnectionButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(userNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(testConnectionButton)
                    .addComponent(saveButton)
                    .addComponent(testResField)
                    .addComponent(refreshButton))
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressField;
    private javax.swing.JTextField addressText;
    private javax.swing.JLabel dbNameField;
    private javax.swing.JTextField dbNameText;
    private javax.swing.JLabel defaultField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField passwordText;
    private javax.swing.JLabel portField;
    private javax.swing.JTextField portText;
    private javax.swing.JRadioButton radioButton1;
    private javax.swing.JRadioButton radioButton2;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton testConnectionButton;
    private javax.swing.JLabel testResField;
    private javax.swing.JTextField userNameText;
    // End of variables declaration//GEN-END:variables
}
