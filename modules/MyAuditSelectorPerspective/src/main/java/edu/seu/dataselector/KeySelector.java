/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector;

import edu.seu.dataselector.drivers.SelectorDriver;
import edu.seu.layout.LayoutImpl;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author hp-6380
 */
public class KeySelector extends javax.swing.JPanel {

    private volatile static KeySelector instance;
    private DefaultTableModel tableModel=new DefaultTableModel();
    
    private Color defaultColor=new Color(0,150,255);
    
    /**
     * Creates new form KeySelector
     */
    private KeySelector() {
        initLookAndFeel();
        initComponents();
        initAction();
        
    }
    
    public static synchronized KeySelector getInstance(){
        if(instance==null){
            instance=new KeySelector();
        }
        return instance;
    }
    
    private void initAction(){
        searchButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(keywordField.getText()==null||keywordField.getText().equals("")){
                    getData();
                    resultNum.setText("结果数量："+table.getRowCount());
                    return;
                }
                final String[] keywords=keywordField.getText().split(" ");
                if(keywords.length==0){
                    getData();
                    resultNum.setText("结果数量："+table.getRowCount());
                    return;
                }
                getData();//重新从数据库中提取数据
                filterTableList(keywords);
                resultNum.setText("关键词：\""+keywordField.getText()+"\"\t 结果数量："+table.getRowCount());
            }
        });
        
        refreshButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                table.getTableHeader().setEnabled(false);
                resultNum.setText("提取数据中...");
                getData();

                resultNum.setText("数量："+tableModel.getRowCount());
                //启用表头功能
                table.getTableHeader().setEnabled(true);
                if(table.getRowCount()>0){
                    resetTableColumnWidth();
                }
            }
        });
        
        keywordField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyChar()==KeyEvent.VK_ENTER ){   //按回车键执行相应操作; { 
                    searchButton.doClick();
                }
            }
        });
        
        //获得鼠标焦点时自动清除默认提示
        keywordField.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(keywordField.getText().equals("请输入关键字")){
                    keywordField.setText("");
                }
            }
        });
        
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(keywordField.getText().equals("")||keywordField.getText().equals(" ")){
                    keywordField.setText("请输入关键字");
                }
            }
        });
        
        buildNetButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                new SwingWorker<Void,Void>(){
                    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                    ProgressTicket ticket = null;
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        
                        if(progressProvider!=null){
                            ticket=progressProvider.createTicket("构建网络中...", null);
                        }
                        ticket.start();
                        
                        //将界面切换到可视化界面
                        WindowManager.getDefault().invokeWhenUIReady(new Runnable(){
                            @Override
                            public void run() {
                                PerspectiveController pc=Lookup.getDefault().lookup(PerspectiveController.class);
                                for(Perspective p:pc.getPerspectives()){
                                    if(p.getName().equals("overview")){
                                        pc.selectPerspective(p);
                                    }
                                }
                            }
                        });
                        
                        //开始构建网络
                        GraphModel graphModel= buildGroupMessageNet();
                        //设置默认的颜色和大小
                        setDefaultSizeAndPosition(graphModel);
                        ticket.finish();

                        final LayoutImpl layout=new LayoutImpl();
                        ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
                            @Override
                            public boolean cancel() {
                                layout.cancel(true);
                                return true;
                            }
                        });
                        ticket.start();
                        //建好之后布局
                        layout.forceAtlasLayout(graphModel);
                        ticket.finish();
                        return null;
                    }
                    
                    @Override
                    protected void done(){
                        ticket.finish();
                    }
                }.execute();
            }
        });
    }
    
    private GraphModel buildGroupMessageNet(){
        try{
            
            List<String> cmdids=new ArrayList<>();
            Set<String> keywords=new HashSet<>();

            int[] selectedRows=table.getSelectedRows();
            int columnIndex=-1;
            int applykeywordIndex=-1;
            for(int i=0;i<table.getColumnCount();i++){
                if(table.getColumnName(i).equals("CMDID")){
                    columnIndex=i;
                }
                if(table.getColumnName(i).equals("APPLYKEYWORD")){
                    applykeywordIndex=i;
                }
            }
            
            for(int i=0;i<selectedRows.length;i++){
                cmdids.add((String)tableModel.getValueAt(selectedRows[i], columnIndex));
                String account=(String)tableModel.getValueAt(selectedRows[i], applykeywordIndex);
                String[] keys=account.split(" ");
                for(int j=0;j<keys.length;j++){
                    keywords.add(keys[j]);
                }
            }

            //这个字段是图说明中需要适用的，表示当前是根据什么关键字构建的网络
            String keyword="";
            for(String s:keywords){
                keyword+=s+" ";
            }

            ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
            if(pc.getCurrentProject()==null){
                pc.newProject();
            }else{
                Workspace workspace=pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            }
            GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
            Graph graph=graphModel.getDirectedGraph();
            
            //rename workspace
            Workspace w=pc.getCurrentWorkspace();
            pc.renameWorkspace(w, "关键词“"+keyword+"”的群组间消息传播网");

            //添加图的一些基本信息
            graph.setAttribute("GraphDes", "关键词“"+keyword+"”的群组间消息传播网");
            graph.setAttribute("FirstNodeDes", "群");
            graph.setAttribute("FirstNodeColor", "蓝色");
            graph.setAttribute("SecondNodeDes", "群成员");
            graph.setAttribute("SecondNodeColor", "红色");

            String[] columnName={"ACCOUNTNAME","MSGTIME","CONTENT","节点性质","defaultColor"};
            Column[] columns=new Column[5];
            columns[0]=initAttributeColumns(graphModel,columnName[0],String.class);
            columns[1]=initAttributeColumns(graphModel,columnName[1],String.class);
            columns[2]=initAttributeColumns(graphModel,columnName[2],String.class);
            columns[3]=initAttributeColumns(graphModel,columnName[3],String.class);
            columns[4]=initAttributeColumns(graphModel,columnName[4],String.class);
                    
            SelectorDriver dirver=new SelectorDriver();
            ResultSet rs=dirver.getGroupMessage(cmdids.toArray(new String[0]));

//            Column edgeColumnAttr=initEdgeAttrColumns(graphModel, "消息详情",String.class);


            try{
                while(rs.next()){
                       String groupId=rs.getString("GROUPID");
                       String userId=rs.getString("USERID");

                       String accountName=rs.getString("ACCOUNTNAME");
                       String msgTime=rs.getString("MSGTIME");
                       String content=rs.getString("CONTENT");


                       if(graph.getNode(userId)==null){
                           Node n=graphModel.factory().newNode(userId);
                           n.setColor(Color.RED);//红色为用户
                           n.setAttribute("defaultColor", "红色");
                           n.setAttribute(columns[0], accountName);
                           n.setAttribute(columns[3], "用户");
    //                       if(n.getAttribute(columns[2])!=null){
    //                           String c=(String)n.getAttribute(columns[2]);
    //                           String message="content:\""+content+"\"time\""+msgTime+"\"\t";
    //                           c+=message;
    //                           n.setAttribute(columns[2], c);
    //                       }else{
    //                           String message="content:\""+content+"\"time\""+msgTime+"\"\t";
    //                           n.setAttribute(columns[2], message);
    //                       }
                           graph.addNode(n);

                       }
                       if(graph.getNode(groupId)==null){
                           Node n=graphModel.factory().newNode(groupId);
                           n.setColor(defaultColor);//蓝色为群组
                           n.setAttribute(columns[1], msgTime);
                           n.setAttribute(columns[2],content);
                           n.setAttribute(columns[3], "群");
                           n.setAttribute("defaultColor", "蓝色");
                           graph.addNode(n);
                       }
                       Edge e=graphModel.factory().newEdge(graph.getNode(userId), graph.getNode(groupId), true);
//                       String message="Content:\""+content+"\"time:\""+msgTime+"\"";
//                       e.setAttribute(edgeColumnAttr, message);
                       if(graph.contains(e)==false){
                           graph.addEdge(e);
                       }
                }
                if(graph.getNodeCount()==0){
                    JOptionPane.showMessageDialog(null, "所选取的记录数据在数据库中缺失，请尝试其他关键词");
                }
            }catch(SQLException ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "取TB_WX_GROUPMESSAGE表数据时出错");
            }
            
            return graphModel;
            
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "构建网络过程出现错误");
        }
        
       
        
        return null;
    }
    
    private void filterTableList(String[] keywords){
        Set<String> selectedKeywords=new HashSet<>();
        for(int i=0;i<keywords.length;i++){
            selectedKeywords.add(keywords[i]);
        }

        //找到keywork列表对应的列的序号
        int columnIndex=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("APPLYKEYWORD")){
                columnIndex=i;
                break;
            }
        }
        //开始遍历表格
        for(int i=0;i<tableModel.getRowCount();){
            String rowData=(String)table.getModel().getValueAt(i, columnIndex);
            String[] keys=rowData.split(" ");
            int matchedNum=0;
            for(int j=0;j<keys.length;j++){
                if(selectedKeywords.contains(keys[j])){
                    matchedNum++;
                }
            }
            if(matchedNum==selectedKeywords.size()){
                i++;
            }else{
                tableModel.removeRow(i);
            }
        }
    }
    
    private void getData(){
        try{
            SelectorDriver keywordSelector=new SelectorDriver();
            ResultSet rs=keywordSelector.getKeywordList();
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            int numOfColumn = resetTableColumnName(rs);
            table.getTableHeader().setEnabled(false);//在加载数据过程中禁用表头功能
            //读取表中数据到Jtable中
            while (rs.next()) {
                String[] rowData = new String[numOfColumn];
                //注意：遍历resultSet的下标得从1开始。
                for (int i = 1; i <= numOfColumn; i++) {
                    rowData[i - 1] = rs.getString(i);
                }
                tableModel.addRow(rowData);
            }
            //关闭数据库链接
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "列表显示数据过程出错");
        }
        
    }
    
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
            tableModel.setColumnIdentifiers(newIdentiters);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        //设置默认列宽
        for(int i=0; i<numOfColumn; i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        return numOfColumn;
    }
    
    /**
     * 根据列表内容长度来重新设置列宽.
     */
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
        int tableWidth=jScrollPane.getWidth();
                
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
    
    private Column initAttributeColumns(GraphModel graphModel,String attr,Class type) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(attr);
        if (eigenCol == null) {
            eigenCol=nodeTable.addColumn(attr, type);
        }
        
        return eigenCol;
    }
    
//    private Column initEdgeAttrColumns(GraphModel graphModel, String attr, Class type){
//        Table edgeTable=graphModel.getEdgeTable();
//        Column eigenCol=edgeTable.getColumn(attr);
//        if(eigenCol==null){
//            eigenCol=edgeTable.addColumn(attr, type);
//        }
//        return eigenCol;
//    }
    
    //给每个节点设置默认的节点位置，大小，颜色
    private void setDefaultSizeAndPosition(GraphModel graphModel){
        Node node;
        Iterator<Node> ite=graphModel.getGraph().getNodes().iterator();
        
        while(ite.hasNext()){
            node=ite.next();
            //设置节点大小
            int neighborCount=graphModel.getGraph().getDegree(node);
            if(neighborCount>20){
                neighborCount=20;
            }
            node.setSize(50f+neighborCount*2);
            
            //随机分配节点坐标
            float x=(float) ((0.01 + Math.random()) * 2000)-1000;
            float y=(float) ((0.01 + Math.random()) * 2000)-1000;
            node.setX(x);
            node.setY(y);
        }
    }

    private void initLookAndFeel(){
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DataSelectorTopComponent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

        keywordField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        resultNum = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        buildNetButton = new javax.swing.JButton();
        startTimeField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        endTimeField = new javax.swing.JTextField();
        resetButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        keywordField.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        keywordField.setText("请输入关键字"); // NOI18N
        keywordField.setToolTipText("请输入想要查询的关键词，多个关键字之间用空格分开"); // NOI18N

        searchButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchButton, "检索"); // NOI18N
        searchButton.setToolTipText("根据关键词，检索包含关键词的历史查询记录"); // NOI18N

        table.setModel(tableModel);
        table.setRowSorter(new TableRowSorter(tableModel));
        jScrollPane.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector.class, "KeySelector.table.columnModel.title0")); // NOI18N
            table.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector.class, "KeySelector.table.columnModel.title1")); // NOI18N
            table.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector.class, "KeySelector.table.columnModel.title2")); // NOI18N
            table.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector.class, "KeySelector.table.columnModel.title3")); // NOI18N
        }

        resultNum.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultNum, "结果数量：0"); // NOI18N

        refreshButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, "刷新列表"); // NOI18N
        refreshButton.setToolTipText("重新从数据库中提取数据"); // NOI18N

        buildNetButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildNetButton, "构建网络"); // NOI18N
        buildNetButton.setToolTipText("根据选中的数据构建用户和群之间的二分网络"); // NOI18N

        startTimeField.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        startTimeField.setText("----年--月--日"); // NOI18N

        jLabel3.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "至"); // NOI18N

        endTimeField.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        endTimeField.setText("----年--月--日"); // NOI18N

        resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/oldPerspective/reset_16.png"))); // NOI18N
        resetButton.setContentAreaFilled(false);
        resetButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/oldPerspective/reset_16_dark.png"))); // NOI18N
        resetButton.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/oldPerspective/reset_16_dark.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(keywordField, javax.swing.GroupLayout.PREFERRED_SIZE, 522, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buildNetButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultNum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buildNetButton, refreshButton, searchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(keywordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchButton)
                            .addComponent(buildNetButton)))
                    .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultNum)
                    .addComponent(refreshButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildNetButton;
    private javax.swing.JTextField endTimeField;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextField keywordField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel resultNum;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField startTimeField;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
