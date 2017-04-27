/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.noderankingdisplayer;

import edu.seu.ranking.api.Ranking;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphDiff;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.Degree;
import static org.gephi.statistics.plugin.Degree.DEGREE;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import static org.gephi.statistics.plugin.EigenvectorCentrality.EIGENVECTOR;
import org.gephi.statistics.plugin.GraphDistance;
import static org.gephi.statistics.plugin.GraphDistance.BETWEENNESS;
import static org.gephi.statistics.plugin.GraphDistance.CLOSENESS;
import static org.gephi.statistics.plugin.GraphDistance.ECCENTRICITY;
import org.gephi.visualization.VizController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *  这个类需要重新写一遍，能每个一段时间监控一下有无新图生成或者
 * @author hp-6380
 */
@ServiceProvider(service= Ranking.class)
public class RankingPanel extends javax.swing.JPanel implements Ranking{

    private final MyDefaultTableModel tableModel=new RankingPanel.MyDefaultTableModel();
    private int delay=50;
    
    private String idColumnName="node";
    
    GraphModel graphModel;
    Graph graphVisible;
    Graph graph;
    
    /**
     * Creates new form RankingPanel
     */
    public RankingPanel() {
        initLookAndFeel();
        initComponents();
        
        initAction();
    }
    
    @Override
    public void execute() {
//        initAction();
    }
    
    
    //初始化一个graphObserver，观察graph中节点变动情况，每半秒钟更新一次
    private void initObserver(){
        
        final GraphObserver observer=graphModel.createGraphObserver(graph, true);
        ActionListener taskPerformer=new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //如果发生了变化
                if(observer.hasGraphChanged()){
                    GraphDiff graphDiff=observer.getDiff();
                    Iterator<Node> removedNodeIte=graphDiff.getRemovedNodes().iterator();
                    
                    List<String> nodes=new ArrayList<>();
                    while(removedNodeIte.hasNext()){
                        nodes.add( (String)removedNodeIte.next().getId() );
                    }
                    
                    synchronized(table){
                        synchronized(tableModel){
                            int nodeColumnIndex=table.getColumnModel().getColumnIndex(idColumnName);
                            int rowCounts=table.getRowCount();
                            for(int i=0; i<rowCounts; ){
                                String nodeId=(String) tableModel.getValueAt(i, nodeColumnIndex);
                                if(nodes.contains(nodeId)){
                                    tableModel.removeRow(i);
                                    rowCounts--;
                                }else{
                                    i++;
                                }
                            }
                            //重新计算中心性参数并更新表格信息
                            tableModel.setRowCount(0);
                            initTable();
                            calCentrality();
                            //如果之前有排序，去除排序（因为重新计算中心性并写入table后是非排序状态，但是列标题栏还是会有排序箭头）
                            table.setRowSorter(new TableRowSorter(tableModel));
                        }
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        nodeNumLabel.setText("节点数量:"+table.getRowCount());
                    }
                });
            }
        };
        new Timer(delay, taskPerformer).start();
    }
    
    
    //将选中的节点高亮
    private void highlightSelectedNode(){
        
        Iterator<Node> ite=graph.getNodes().iterator();
        Color blue=new Color(0,150,255);
        while(ite.hasNext()){
            ite.next().setColor(blue);
        }
        
        String[] nodesId;
        synchronized(table){
            synchronized(tableModel){
                int[] selectedNode =table.getSelectedRows();
                nodesId=new String[selectedNode.length];
                int nodeColumnIndex=table.getColumnModel().getColumnIndex(idColumnName);
                for(int i=0; i<selectedNode.length; i++){
                    nodesId[i]=(String) table.getValueAt(selectedNode[i], nodeColumnIndex);
                }
            }
        }
       
        Node node;
        for(int i=0; i<nodesId.length; i++){
            node=graph.getNode(nodesId[i]);
            node.setColor(new Color(204,0,153));
        }
    }
    
    //初始化table并且计算各种中心性参数
    private void initAction(){
        executeButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //初始化table
                initTable();
                //计算中心性
                calCentrality();
                //同时启动观察者观察graph中节点的变化情况
                initObserver();
            }
        });
        
        highlightButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightSelectedNode();
            }
        });
        
        //将视图视角拉到选中节点
        focusButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowIndex=table.getSelectedRow();
                int columnIndex=table.getColumnModel().getColumnIndex(idColumnName);
                String nodeName=(String)table.getValueAt(rowIndex, columnIndex);
                Iterator<Node> ite=graph.getNodes().iterator();
                Node node=null;
                while(ite.hasNext()){
                    Node n=ite.next();
                    if(n.getId().equals(nodeName)){
                        node=n;
                        break;
                    }
                }
                VizController.getInstance().getSelectionManager().centerOnNode(node);
            }
        });
        
        deleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized(table){
                    synchronized(tableModel){
                        String nodeName;
                        int[] selectedRowsIndex=table.getSelectedRows();
                        int columnIndex=table.getColumnModel().getColumnIndex(idColumnName);
                        
                        for(int i=0; i<selectedRowsIndex.length; i++){
                            nodeName=(String)table.getValueAt(selectedRowsIndex[i], columnIndex);
                            Node n=graph.getNode(nodeName);
                            graph.removeNode(n);
                            tableModel.removeRow(selectedRowsIndex[i]);
                        }
                    }
                }
            }
        });
        
    }
    
    private void calCentrality(){
        new EigenvectorCentrality().execute(graph);
        new Degree().execute(graph);
        new GraphDistance().execute(graph);

        addAttrColumn(EIGENVECTOR);
        addAttrColumn(DEGREE);
        addAttrColumn(BETWEENNESS);
        addAttrColumn(CLOSENESS);
        addAttrColumn(ECCENTRICITY);
    }
    
    private void initTable(){
        
        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph=graphModel.getGraph();
        
        Iterator<Node> ite=graph.getNodes().iterator();
        List<Node> nodes=new ArrayList<>();
        
        while(ite.hasNext()){
            nodes.add(ite.next());
        }

        synchronized(table){
            synchronized(tableModel){
                if(table.getColumnModel().getColumnCount()==0){
                    tableModel.addColumn(idColumnName);
                }else{
                    tableModel.setRowCount(0);
                }
                
                for(int i=0; i<nodes.size(); i++){
                    String[] s=new String[1];
                    s[0]=(String) nodes.get(i).getId();
                    tableModel.addRow(s);
                }
                resetTableColumnWidth();
            }
        }
    }
    

    private synchronized void addAttrColumn(String columnName){

        
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph graph=graphModel.getGraph();
        
        int columnIndex=-1;
        boolean flag=false;//表示是否需要新建一个column列
        try{
            columnIndex=table.getColumnModel().getColumnIndex(columnName);
        }catch(IllegalArgumentException e){
            flag=true;
        }
        
        DecimalFormat df=new DecimalFormat("#.###");
        Number value;
        if(flag==true){
            tableModel.addColumn(columnName);
            int nodeIdIndex=table.getColumnModel().getColumnIndex(idColumnName);
            int addedIndex=table.getColumnModel().getColumnIndex(columnName);
            
            String nodeName;
            for(int i=0; i<table.getRowCount(); i++ ){
                nodeName=(String)table.getValueAt(i, nodeIdIndex);
                value=(Number)graph.getNode(nodeName).getAttribute(columnName);
                table.setValueAt(Double.parseDouble(df.format(value)), i, addedIndex);//注意这里和下面那句的区别
            }
        }else{
            int nodeIdIndex=table.getColumnModel().getColumnIndex(idColumnName);
            for(int i=0; i<table.getRowCount(); i++){
                String nodeName=(String) table.getValueAt(i, nodeIdIndex);
                value=(Number)graph.getNode(nodeName).getAttribute(columnName);
                table.setValueAt(Double.parseDouble(df.format(value)), i, columnIndex);
            }
        }
        
    }
    
    static class MyDefaultTableModel extends DefaultTableModel{
        @Override
        public Class getColumnClass(int c){
            if(c==0)
                return String.class;
            else
                return Double.class;
        }
    }
    
    private synchronized void resetTableColumnWidth(){
        int columnNum=table.getColumnCount();
        int[] columnWidths=new int[columnNum];
        
        //计算列宽
        for(int col=0; col<columnNum; col++){
            TableColumn column=table.getColumnModel().getColumn(col);
            int maxWidth=0;
            int countNum=table.getRowCount()<=100 ? table.getRowCount():100;//若行数小于等于100行，则按照行数计算，若行数多于100行，则只计算100行
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
            if(maxWidth<=500){
                columnWidths[col] = maxWidth+5;//+5是为了使添加列与列之间的宽度
            }else{
                columnWidths[col] = 500;
            }
        }
        
        //根据表的宽度调整列宽，使列表宽度能铺满列表内容
        int sumWidth=0,offset=0;
        int tableWidth=table.getWidth();
                
        for(int i=0; i<columnWidths.length; i++){
            sumWidth+=columnWidths[i];
        }
        if(tableWidth>sumWidth){
            offset=(tableWidth-sumWidth)/columnNum;
        }
        
        //设置每一列列宽
        for(int i=0; i<columnNum; i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]+offset);
        }
    }
    
    private void initLookAndFeel(){
        //========look&feel========
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RankingPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RankingPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RankingPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RankingPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //========look&feel========
    }
    
//    private void tableMethod(){
//        Table gephiTable=
//    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        executeButton = new javax.swing.JButton();
        highlightButton = new javax.swing.JButton();
        nodeNumLabel = new javax.swing.JLabel();
        focusButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        table.setModel(tableModel);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        table.setRowSorter(new TableRowSorter(tableModel));
        jScrollPane1.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(executeButton, org.openide.util.NbBundle.getMessage(RankingPanel.class, "RankingPanel.executeButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(highlightButton, org.openide.util.NbBundle.getMessage(RankingPanel.class, "RankingPanel.highlightButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nodeNumLabel, org.openide.util.NbBundle.getMessage(RankingPanel.class, "RankingPanel.nodeNumLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(focusButton, org.openide.util.NbBundle.getMessage(RankingPanel.class, "RankingPanel.focusButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(RankingPanel.class, "RankingPanel.deleteButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nodeNumLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deleteButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(focusButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(highlightButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(executeButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(executeButton)
                    .addComponent(highlightButton)
                    .addComponent(nodeNumLabel)
                    .addComponent(focusButton)
                    .addComponent(deleteButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton executeButton;
    private javax.swing.JButton focusButton;
    private javax.swing.JButton highlightButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nodeNumLabel;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    
}
