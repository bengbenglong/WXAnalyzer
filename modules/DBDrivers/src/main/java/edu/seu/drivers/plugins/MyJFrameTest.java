/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.drivers.plugins;

import edu.seu.database.drivers.DatabaseTableSelector;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author hp-6380
 */
public class MyJFrameTest extends javax.swing.JFrame {
    private DefaultTableModel defaultTableModel = new DefaultTableModel();

    private MyProcessBar myProcessBar = new MyProcessBar(defaultTableModel);
    private OptionsPanel optionsPanel = new OptionsPanel();

    private MySwingWorker swingWorker;
    
    //test
    private List<String> selectedRowData;

    /**
     * Creates new form MyJFrameTest
     */
    public MyJFrameTest() {

        initComponents();
        
        //添加组件
        processBarPanel.add(myProcessBar);
        optionPanel.add(optionsPanel);

        initAction();
    }
    
    


    /**
     * 根据数据库提取的数据结果重新设置jTable的列名称.
     *
     * @param rs ResultSet
     */
    private int resetTableColumnName(ResultSet rs) {
        int numOfColumn = 0;
        //设置列名
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            numOfColumn = rsmd.getColumnCount();
            String[] newIdentiters = new String[numOfColumn];
            for (int i = 0; i < numOfColumn; i++) {
                newIdentiters[i] = rsmd.getColumnName(i + 1);//注意：rsmd中Column从下标1开始算起
            }
            defaultTableModel.setColumnIdentifiers(newIdentiters);
        } catch (SQLException ex) {
            Logger.getLogger(MyJFrameTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //设置默认列宽
        for(int i=0; i<numOfColumn; i++){
            jTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        return numOfColumn;
    }
    
    /**
     * 根据列表内容长度来重新设置列宽.
     */
    private synchronized void resetTableColumnWidth(){
        int columnNum=jTable.getColumnCount();
        int[] columnWidths=new int[columnNum];
        
        //计算列宽
        for(int col=0; col<columnNum; col++){
            TableColumn column=jTable.getColumnModel().getColumn(col);
            int maxWidth=0;
            int countNum=jTable.getRowCount()<=100 ? jTable.getRowCount():100;//若行数小于等于100行，则按照行数计算，若行数多于100行，则只计算100行
            for(int row=0; row<countNum; row++){
                TableCellRenderer renderer = jTable.getCellRenderer (row, col);  
                Object value = jTable.getValueAt (row, col);  
                Component comp =renderer.getTableCellRendererComponent (jTable, value, false, false, row, col);  
                maxWidth = Math.max (comp.getPreferredSize().width,  maxWidth);  
            }
            //为了避免列名不能完全显示，设置列宽至少等于列名宽度。
            TableCellRenderer headerRenderer=column.getHeaderRenderer();
            if (headerRenderer == null){  
                headerRenderer = jTable.getTableHeader().getDefaultRenderer();  
                Object headerValue = column.getHeaderValue();  
                Component headerComp =headerRenderer.getTableCellRendererComponent (jTable, headerValue, false, false, 0, col);  
                maxWidth = Math.max (maxWidth, headerComp.getPreferredSize().width);  
            }
            //过滤某些单元格尺寸，使之不能太宽。设置单元格最大宽度为500.
            if(maxWidth<=500){
                columnWidths[col] = maxWidth+5;//+5是为了使添加列与列之间的宽度
            }else{
                columnWidths[col] = 500;
            }
        }
        
        //根据表的宽度调整列宽，使列表宽度能铺满列表内容
        int sumWidth=0,offset=0;
        int scrollPaneWidth=jScrollPane.getWidth();
        int scrollBarWidth=jScrollPane.getVerticalScrollBar().getWidth()+5;//在scrollBar宽度上再增加5个像素点距离
                
        for(int i=0; i<columnWidths.length; i++){
            System.out.println("column width:\t"+columnWidths[i]);
            sumWidth+=columnWidths[i];
        }
        if(scrollPaneWidth>sumWidth){
            offset=(scrollPaneWidth-sumWidth-scrollBarWidth)/columnNum;
        }
        
        //设置每一列列宽
        for(int i=0; i<columnNum; i++){
            jTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]+offset);
        }
        System.out.println("sumWidth:\t"+(sumWidth+offset*columnNum)+"\tjscr:\t"+jScrollPane.getWidth()+"\tjscrBar\t"+jScrollPane.getVerticalScrollBar().getWidth());
        
    }

    class MySwingWorker extends SwingWorker<Integer, Integer> {

        @Override
        protected Integer doInBackground() throws Exception {
            //绘制processBar
            myProcessBar.startloadLabelChange();
            //读取数据库
            DatabaseTableSelector dbTableSelector = new DatabaseTableSelector(optionsPanel.getSelectedTable(), optionsPanel.getSelectedTime());
            ResultSet resultSet = dbTableSelector.getData();
            //根据取出的数据表重置表头列数和列名.
            int numOfColumn = resetTableColumnName(resultSet);
            jTable.getTableHeader().setEnabled(false);//在加载数据过程中禁用表头功能
            //读取表中数据到Jtable中
            defaultTableModel.setRowCount(0);
            while (resultSet.next() && !isCancelled()) {
                String[] rowData = new String[numOfColumn];
                //注意：遍历resultSet的下标得从1开始。
                for (int i = 1; i <= numOfColumn; i++) {
                    rowData[i - 1] = resultSet.getString(i);
                }
                defaultTableModel.addRow(rowData);
            }
            //关闭数据库链接
            dbTableSelector.close();
            publish(defaultTableModel.getRowCount());
            return defaultTableModel.getRowCount();
        }

        @Override
        protected void done() {
            if (isCancelled() == true) {
                loadButton.setEnabled(true);
                loadButton.setText("加载");
                //启用OptionsPanel中数据表选择组合框
                optionsPanel.setComboBoxEnabledT();
            } else {
                myProcessBar.endLodaLabelChange();
            }
            //启用表头功能
            jTable.getTableHeader().setEnabled(true);
            resetTableColumnWidth();
        }

        @Override
        protected void process(java.util.List<Integer> chunks) {
            //更新GUI
            loadButton.setEnabled(true);
            loadButton.setText("加载");
            //启用OptionsPanel中数据表选择组合框
            optionsPanel.setComboBoxEnabledT();
            
            myProcessBar.cancelLoadLabelChange();
        }
    }
    
    /**
     * 为组件添加事件监听器.
     */
    private void initAction(){
        loadButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                swingWorker = new MySwingWorker();
                swingWorker.execute();
                //禁用OptionsPanel中的数据表选择组合框
                optionsPanel.setComboBoxEnabledF();
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (swingWorker != null && !swingWorker.isDone()) {
                    swingWorker.cancel(true);
                    swingWorker = null;
                }
            }
        });
        
        filterButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
                
        
        
        jTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(final MouseEvent e){
                if (e.getButton() == MouseEvent.BUTTON3) {
                    RightClickMenu pMenu = new RightClickMenu(jTable);
                    pMenu.showPMenu(e);
                }
            }
        });
        
        //添加列头的
        jTable.getTableHeader().addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(final MouseEvent e){
                final int column=jTable.columnAtPoint(e.getPoint());
                if( jTable.getTableHeader().isEnabled() && e.getButton()==MouseEvent.BUTTON3 ){
                    TableHeaderRightClick pMenu = new TableHeaderRightClick(jTable,column);
                    pMenu.showMenu(e);
                }
            }
        });
        
        //监听jScrollPane窗口大小改变，则动态更改列表中的列宽。
        jScrollPane.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                resetTableColumnWidth();
            }
        });
        
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MyJFrameTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MyJFrameTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MyJFrameTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MyJFrameTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MyJFrameTest().setVisible(true);
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

        loadButton = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        filterButton = new javax.swing.JButton();
        optionPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        processBarPanel = new javax.swing.JPanel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        loadButton.setText("加载");

        jTable.setModel(defaultTableModel);
        jTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable.setRowSorter(new TableRowSorter<>(defaultTableModel));
        jScrollPane.setViewportView(jTable);

        filterButton.setText("过滤公共");

        optionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        optionPanel.setLayout(new java.awt.BorderLayout());

        cancelButton.setText("停止加载");

        processBarPanel.setToolTipText("");
        processBarPanel.setPreferredSize(new java.awt.Dimension(0, 22));
        processBarPanel.setLayout(new java.awt.BorderLayout());

        searchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/lizhenglong/drivers/image/searchButton.png"))); // NOI18N
        searchButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/lizhenglong/drivers/image/buttonBackground_lomo.png"))); // NOI18N
        searchButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/lizhenglong/drivers/image/buttonBackground_lomo.png"))); // NOI18N

        jButton1.setText("过滤面板");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(optionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(searchField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filterButton))
                            .addComponent(jButton1)))
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 803, Short.MAX_VALUE)
                    .addComponent(processBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 803, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {filterButton, loadButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loadButton)
                            .addComponent(cancelButton)
                            .addComponent(filterButton))
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(searchField)
                            .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(jScrollPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton filterButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTable jTable;
    private javax.swing.JButton loadButton;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JPanel processBarPanel;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
