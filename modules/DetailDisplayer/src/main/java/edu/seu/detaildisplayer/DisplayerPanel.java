/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.detaildisplayer;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author hp-6380
 */
public class DisplayerPanel extends javax.swing.JPanel{

    Set<String> centrality=new HashSet<>();
    
    //单例模式
    private static volatile DisplayerPanel instance;
    
    private DefaultTableModel tableModel=new DefaultTableModel();
    /**
     * Creates new form DisplayerPanel
     */
    public DisplayerPanel() {
        synchronized(DisplayerPanel.class){
            if(instance!=null){
                return;
            }
        }
        initComponents();
        table.getTableHeader().setVisible(false);
        
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(final MouseEvent e){
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            RightClickMenu pMenu = new RightClickMenu(table);
                            pMenu.showPMenu(e);
                        }
                    }
                });
            }
        });
        
        //这些表示的是计算得到的节点参数，应当放在最后或者最前展示
        centrality.add("特征向量");
        centrality.add("度");
        centrality.add("离心率");
        centrality.add("介数");
        centrality.add("消息传播到达时间");
        centrality.add("接近度");
        centrality.add("出度");
        centrality.add("入度");
    }
    
    
    //单例模式
    public synchronized static DisplayerPanel getInstance(){
        if(instance==null){
           instance=new DisplayerPanel();
        }
        return instance;
    }
    
    
    //展示新数据
    public void showNewObject(String[] s){
        tableModel.setRowCount(0);
        int columnCount=tableModel.getColumnCount();
        if(columnCount!=2){
            tableModel.addColumn("");
            tableModel.addColumn("");
        }
        
        //第一轮循环不显示中心性参数，如果有消息内容，也不显示
        for(int i=0; i<s.length; i++){
            String[] ss=s[i].split("\t");
            if(!centrality.contains(ss[0]) && !ss[0].equals("消息内容")){
                tableModel.addRow(ss);
            }
        }
        //第二轮循环，专门展示中心性参数
        boolean flag=false;
        for(int i=0;i<s.length;i++){
            String[] ss=s[i].split("\t");
            if(centrality.contains(ss[0]) && flag==false){
                String[] blankRow={"  "," "};
                String[] centerRow={"中心性参数："," "};
                tableModel.addRow(blankRow);
                tableModel.addRow(ss);
                flag=true;
            }else if(centrality.contains(ss[0])){
                tableModel.addRow(ss);
            }
        }
        
        //最后，如果有消息内容，展示消息内容。
        for(int i=0;i<s.length;i++){
            String[] ss=s[i].split("\t");
            if(ss[0].equals("消息内容")){
                String[] splictRow={" "," "};
                String[] secondRow={"消息内容："," "};
                tableModel.addRow(splictRow);
                tableModel.addRow(secondRow);
                
                String[] contents=ss[1].split("○");
                for(int j=0;j<contents.length;j++){
                    String[] c=contents[j].split("△");
                    tableModel.addRow(c);
                }
            }
        }
        
        resetColumnSize();
    }
    
    private void resetColumnSize(){
        int columnNum=table.getColumnCount();
        int[] columnWidths=new int[columnNum];
        
        //计算列宽
        for(int col=0; col<columnNum; col++){
            TableColumn column=table.getColumnModel().getColumn(col);
            int maxWidth=0;
            int countNum=table.getRowCount()<=20 ? table.getRowCount():20;//若行数小于等于20行，则按照行数计算，若行数多于20行，则只计算20行
            for(int row=0; row<countNum; row++){
                TableCellRenderer renderer = table.getCellRenderer (row, col);  
                Object value = table.getValueAt (row, col);  
                Component comp =renderer.getTableCellRendererComponent (table, value, false, false, row, col);  
                maxWidth = Math.max (comp.getPreferredSize().width,  maxWidth);  
            }
            //为了避免列名不能完全显示，设置列宽至少等于列名宽度。
            TableCellRenderer headerRenderer=column.getHeaderRenderer();
            if (headerRenderer == null){  
                headerRenderer = table.getTableHeader().getDefaultRenderer();  
                Object headerValue = column.getHeaderValue();  
                Component headerComp =headerRenderer.getTableCellRendererComponent (table, headerValue, false, false, 0, col);  
                maxWidth = Math.max (maxWidth, headerComp.getPreferredSize().width);  
            }
            //过滤某些单元格尺寸，使之不能太宽。设置单元格最大宽度为500.
            if(maxWidth<=50000){
                columnWidths[col] = maxWidth+5;//+5是为了使添加列与列之间的宽度
            }else{
                columnWidths[col] = 50000;
            }
        }
        
        //根据表的宽度调整列宽，使列表宽度能铺满列表内容
        int sumWidth=0,offset=0;
        int tableWidth=scrollPane.getWidth();
                
        for(int i=0; i<columnWidths.length; i++){
            sumWidth+=columnWidths[i];
        }

        if(tableWidth>sumWidth){
            offset=(tableWidth-sumWidth)/columnNum;
        }
        
        //设置每一列列宽
        for(int i=0; i<columnNum; i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
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

        jOptionPane1 = new javax.swing.JOptionPane();
        jLabel1 = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable(){
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jLabel1.setFont(new java.awt.Font("微软雅黑", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DisplayerPanel.class, "DisplayerPanel.jLabel1.text")); // NOI18N

        scrollPane.setBorder(null);

        table.setBackground(new java.awt.Color(240, 240, 240));
        table.setModel(tableModel
        );
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        table.setCellSelectionEnabled(true);
        scrollPane.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(DisplayerPanel.class, "DisplayerPanel.table.columnModel.title0")); // NOI18N
            table.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(DisplayerPanel.class, "DisplayerPanel.table.columnModel.title1")); // NOI18N
            table.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(DisplayerPanel.class, "DisplayerPanel.table.columnModel.title2")); // NOI18N
            table.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(DisplayerPanel.class, "DisplayerPanel.table.columnModel.title3")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JOptionPane jOptionPane1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables


    
}
