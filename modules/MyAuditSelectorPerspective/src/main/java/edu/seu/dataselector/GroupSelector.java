/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector;

import edu.seu.dataselector.drivers.SelectorDriver;
import edu.seu.dataselector.singleton.GroupMemberSingleton;
import edu.seu.layout.MyLayoutController;
import edu.seu.layout.LayoutImpl3;
import edu.seu.networkbuild.api.BuildAttribute;
import edu.seu.networkbuild.api.BuildNet;
import edu.seu.networkbuild.api.MyTableAttr;
import edu.seu.networkbuild.impl.GroupMemberNetBuilder;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
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
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author hp-6380
 */
public class GroupSelector extends javax.swing.JPanel {

    private final String egoGroupNet="群的自中心网络";
    private final String relGroupNet="群组之间的关系网";
    private Color blueColor=new Color(0,150,255);
    
    private volatile static GroupSelector instance;
    private DefaultTableModel tableModel=new DefaultTableModel();
    private ComboBoxModel<String> comboBoxModel=new DefaultComboBoxModel<>(new String[]{egoGroupNet,relGroupNet});
    
    /**
     * Creates new form GroupSelector
     */
    private GroupSelector() {
        initLookAndFeel();
        initComponents();
        initAction();
    }
    
    public static synchronized GroupSelector getInstance(){
        if(instance==null){
            instance=new GroupSelector();
        }
        return instance;
    }
    
    private void initAction(){
        refreshButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                table.getTableHeader().setEnabled(false);//禁用表头功能
                resultNum.setText("提取数据中...");
                getListData();
                resultNum.setText("数量："+tableModel.getRowCount());
                
                table.getTableHeader().setEnabled(true);//启用表头功能
                if(table.getRowCount()>0){
                    resetTableColumnWidth();
                }
            }
        });
        
        searchButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword=keywordText.getText();
                if(keyword.equals("")||keyword.equals(" ")){
                    getListData();
                    resultNum.setText("关键词：\""+keyword+"\"\t 结果数量："+table.getRowCount());
                    return;
                }
                getListData();
                filterTableList(keyword);
                resultNum.setText("关键词：\""+keyword+"\"\t 结果数量："+table.getRowCount());
            }
        });
        
        buildNetButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                new SwingWorker<Void,Void>(){
                    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                    ProgressTicket ticket = progressProvider.createTicket("", null);
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                buildNetButton.setEnabled(false);
                            }
                        });
                        
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
                        
                        if(comboBox.getSelectedItem().equals(egoGroupNet)){
                            ticket.start();
                            ticket.setDisplayName("构建"+egoGroupNet+"中...");
                            //构建ego-Network
                            String selectedGroupId=buildEgoNet();
                            //更改workspace名字
                            renameWorkspace("群"+selectedGroupId+"的自中心网络");
                        }else if(comboBox.getSelectedItem().equals(relGroupNet)){
                            ticket.start();
                            ticket.setDisplayName("构建"+relGroupNet+"中...");
                            //构建网络
                            List<String> groupIDs=buildRelationNet();
                            //更改workspace名字
                            renameWorkspace("群“"+groupIDs.get(0)+"”...等的群组间关系网");
                        }
                        
                        //设置默认的颜色和大小
                        setDefaultSizeAndPosition();
                        ticket.finish();
                        
                        
                        //在开始布局之前便可以让“构建网络”按钮启用
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                buildNetButton.setEnabled(true);
                            }
                        });

                        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                        LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
                        MyLayoutController layoutContainer=new MyLayoutController(layout);
                        layoutContainer.startLayout();

                        return null;
                    }
                    
                    @Override
                    protected void done(){
                        if(ticket!=null){
                            ticket.finish();
                        }
                        buildNetButton.setEnabled(true);
                    }
                }.execute();
            }
        });
        
        keywordText.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyChar()==KeyEvent.VK_ENTER ){   //按回车键执行相应操作; { 
                    searchButton.doClick();
                }
            }
        });
        
        //获得鼠标焦点时自动清除默认提示
        keywordText.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(keywordText.getText().equals("群组ID/群昵称/创建者ID")){
                    keywordText.setText("");
                }
            }
        });
    }
    
    private List<String> buildRelationNet(){
        int[] selectedRows=table.getSelectedRows();
        int groupIdColumn=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("GROUPID")){
                groupIdColumn=i;
                break;
            }
        }
        //提取出所有被选择数据行中的groupID数据
        List<String> groupIDs=new ArrayList<>();
        for(int i=0;i<selectedRows.length;i++){
            groupIDs.add((String)table.getValueAt(selectedRows[i], groupIdColumn));
        }
        
        SelectorDriver driver=new SelectorDriver();
        ResultSet rs=driver.getAllGroupMemberInfo(groupIDs.toArray(new String[0]));
        
        //只选择构建网络所必须的列信息以及所选择的行数据(这个属于过滤，是不是可以把这部分功能单独提取到另一个模块中比较好？）
//        String[] columnNames=buildNet.getNeedfulColumnNames();
        //在这里，直接选择GROUPID和USERID字段
        
        try{
            BuildNet buildNet=new GroupMemberNetBuilder();
            List<String[]> minNeedfulData=new ArrayList<>();
            ResultSetMetaData rsmd=rs.getMetaData();
            String[] columnNames=new String[rsmd.getColumnCount()];
            for(int i=0;i<columnNames.length;i++){
                columnNames[i]=rsmd.getColumnName(i+1);
            }
            MyTableAttr tableAttr=new MyTableAttr(columnNames);
            while(rs.next()){
                String[] rowData=new String[2];
                rowData[0]=rs.getString("GROUPID");
                rowData[1]=rs.getString("USERID");
                minNeedfulData.add(rowData);
                
                //添加属性信息
                String[] attrs=new String[columnNames.length];
                for(int i=0;i<columnNames.length;i++){
                    attrs[i]=rs.getString(columnNames[i]);
                }
                tableAttr.addAttributes(rowData[0], attrs);
            }
            //初始化需要构建的网络信息
            buildNet.init(minNeedfulData,BuildAttribute.GROUP_MEMBER_RELATION,tableAttr);
            buildNet.build();
            
            //将群组defaultColor全部设置为蓝色
            Graph graph=Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            Iterator<Node> ite=graph.getNodes().iterator();
            initAttributeColumns(graph.getModel(), "defaultColor",String.class);
            while(ite.hasNext()){
               Node n=ite.next();
               n.setColor(blueColor);
               n.setAttribute("defaultColor", "蓝色");
            }
            
             //添加图的一些基本信息
            graph.setAttribute("GraphDes", "群组间关系网");
            graph.setAttribute("FirstNodeDes", "群");
            graph.setAttribute("FirstNodeColor", "蓝色");
            graph.setAttribute("SecondNodeDes", " ");
            graph.setAttribute("SecondNodeColor", " ");
            
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }
        
        return groupIDs;
    }
    
    private String buildEgoNet(){
        
        //选择一个种子群的群id，且只选择一个
        int[] selectedRow=table.getSelectedRows();
        int groupIdColumn=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("GROUPID")){
                groupIdColumn=i;
                break;
            }
        }
        String selectedGroupId=(String)table.getValueAt(selectedRow[0], groupIdColumn);//只选择一个，用户多选的话，也是只选择一个，忽略其他
        
        //先构建网络，将节点一个个添加进入，然后再提取数据库中数据并且
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }else{
            Workspace workspace=pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph graph=graphModel.getDirectedGraph();
        //添加图的一些基本信息
        graph.setAttribute("GraphDes", "群“"+selectedGroupId+"”的自我中心网络");
        graph.setAttribute("FirstNodeColor", "蓝色");
        graph.setAttribute("FirstNodeDes", "群");
        graph.setAttribute("SecondNodeDes", "群成员");
        graph.setAttribute("SecondNodeColor", "红色");
        
        //
        SelectorDriver driver=new SelectorDriver();
        ResultSet rs=driver.getSingleGroupMemberInfo(selectedGroupId);
        
        try{
            //初始化节点属性列表
            ResultSetMetaData rsmd=rs.getMetaData();
            String[] columnName=new String[rsmd.getColumnCount()];
            Column[] columns=new Column[rsmd.getColumnCount()];
            for(int i=0;i<rsmd.getColumnCount();i++){
                columnName[i]=rsmd.getColumnName(i+1);
                columns[i]=initAttributeColumns(graphModel,rsmd.getColumnName(i+1),String.class);
            }
            Column classColumn=initAttributeColumns(graphModel,"节点性质",String.class);
            initAttributeColumns(graphModel,"defaultColor",String.class);
            
            //先添加目标节点的一阶ego-network
            while(rs.next()){
                String groupId=rs.getString("GROUPID");
                String userId=rs.getString("USERID");
                if(graph.getNode(groupId)==null){
                    Node n=graphModel.factory().newNode(groupId);
                    n.setColor(blueColor);//蓝色代表群
                    n.setAttribute("defaultColor", "蓝色");
                    n.setAttribute(classColumn, "群");
                    //这个好像不对吧
//                    for(int i=0;i<columns.length;i++){
//                        n.setAttribute(columns[i], rs.getString(columnName[i]));
//                    }
                    graph.addNode(n);
                }
                if(graph.getNode(userId)==null){
                    Node n=graphModel.factory().newNode(userId);
                    n.setColor(Color.RED);//红色代表群成员
                    n.setAttribute("defaultColor", "红色");
                    for(int i=0;i<columnName.length;i++){
                        if(!columnName[i].equals("USERID")){//因为userid和节点的id是重复的，所以不必重复添加
                            n.setAttribute(columns[i], rs.getString(columnName[i]));
                        }
                    }
                    n.setAttribute(classColumn, "群成员");
                    graph.addNode(n);
                }
                Edge e=graphModel.factory().newEdge(graph.getNode(groupId), graph.getNode(userId), true);
                if(graph.contains(e)==false){
                    graph.addEdge(e);
                }
            }
            //再遍历邻居节点是否有种子节点，如果有种子节点，把该种子节点加入到网络中
            GroupMemberSingleton groupNetSin=GroupMemberSingleton.getInstance();
            Map<String,Set<String>> groupNet=groupNetSin.getGroupNet();
            Map<String,Set<String>> usersNet=groupNetSin.getUserNet();
            List<String> zhongziGroupId=new ArrayList<>();//存放种子选手的列表，这些种子选手的具体信息要提取出来并添加到网络中
            zhongziGroupId.add(selectedGroupId);
            if(groupNet.get(selectedGroupId)!=null){
                for(String firstOrderUser:groupNet.get(selectedGroupId)){
                    if(usersNet.get(firstOrderUser)!=null){
                        for(String secondOrderGroup:usersNet.get(firstOrderUser)){
                            //如果已经在节点中了，也不能添加边，要不变成所有边全是双向的了
                            if(graph.getNode(secondOrderGroup)==null){
                                zhongziGroupId.add(secondOrderGroup);
                                Node n=graphModel.factory().newNode(secondOrderGroup);
                                n.setColor(new Color(0,150,255));
                                n.setAttribute(classColumn, "群");
                                n.setAttribute("defaultColor", "蓝色");
                                graph.addNode(n);

                                Edge e=graphModel.factory().newEdge(graph.getNode(firstOrderUser), graph.getNode(secondOrderGroup));
                                if(graph.contains(e)==false){
                                    graph.addEdge(e);
                                }
                            }
                        }
                    }
                }
            }
                
            
            //然后将种子Group的信息加入到图中
            this.addGroupInfo(graph, zhongziGroupId.toArray(new String[0]));
            
            if(graph.getNodeCount()==0){
                JOptionPane.showMessageDialog(null, "网络中节点数量为0");
            }
            
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }
        return selectedGroupId;
    }
    
    
    private void addGroupInfo(Graph graph,String[] groupIds){
        try{
            SelectorDriver driver=new SelectorDriver();
            ResultSet rs=driver.getAllGroupInfo(groupIds);
            ResultSetMetaData rsmd=rs.getMetaData();
            Column[] columns=new Column[rsmd.getColumnCount()];
            String[] columnNames=new String[rsmd.getColumnCount()];
            for(int i=0;i<columns.length;i++){
                columns[i]=initAttributeColumns(graph.getModel(),rsmd.getColumnName(i+1),String.class);
                columnNames[i]=rsmd.getColumnName(i+1);
            }


            while(rs.next()){
                   String groupId=rs.getString("GROUPID");
                   Node n=graph.getNode(groupId);
                   for(int i=0;i<columnNames.length;i++){
                       if(!columnNames[i].equals("GROUPID")){//因为groupID和节点的ID本身是重复的，所以不必重复添加
                           n.setAttribute(columns[i], rs.getString(columnNames[i]));
                       }
                   }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }
            
    }
    
//    private void buildGroupNet(){
//        try{
//            SelectorDriver driver=new SelectorDriver();
//            ResultSet rs=driver.getGroupNet();
//            
//            while(rs.next()){
//                String groupId=rs.getString("GROUPID");
//                String userId=rs.getString("USERID");
//                Set<String> targetSet=groupNet.get(groupId);
//                if(targetSet==null){
//                    Set<String> newTargetSet=new HashSet<>();
//                    newTargetSet.add(userId);
//                    groupNet.put(groupId, newTargetSet);
//                }else if(targetSet.contains(userId)==false){
//                    targetSet.add(userId);
//                    groupNet.put(groupId, targetSet);
//                }
//            }
//        }catch(SQLException ex){
//            ex.printStackTrace();
//            JOptionPane.showMessageDialog(null, "出现错误！");
//        }
//    }
    
    private void filterTableList(String keyword){
        //找到keywork列表对应的列的序号
        int idIndex=-1;
        int nameIndex=-1;
        int creatorIndex=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("GROUPID")){
                idIndex=i;
            }
            if(table.getColumnName(i).equals("GROUPNAME")){
                nameIndex=i;
            }
            if(table.getColumnName(i).equals("CREATOR")){
                creatorIndex=i;
            }
        }
        //开始遍历表格
        for(int i=0;i<tableModel.getRowCount();){
            String name=(String) table.getModel().getValueAt(i, nameIndex);
            String id=(String) table.getModel().getValueAt(i, idIndex);
            String creator=(String) table.getModel().getValueAt(i, creatorIndex);
            if(keyword.equals(name)||keyword.equals(id)||keyword.equals(creator)){
                i++;
            }else{
                tableModel.removeRow(i);
            }
        }
    }
    
    private void getListData(){
        try{
            SelectorDriver keywordSelector=new SelectorDriver();
            ResultSet rs=keywordSelector.getGroupList();
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
    
    private void renameWorkspace(String name){
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        Workspace w=pc.getCurrentWorkspace();
        pc.renameWorkspace(w, name);
    }
    
    private void setDefaultSizeAndPosition(){
        
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
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

        keywordText = new javax.swing.JTextField();
        buildNetButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        resultNum = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        comboBox = new javax.swing.JComboBox<>();
        separator2 = new javax.swing.JToolBar.Separator();

        keywordText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        keywordText.setText("群组ID/群昵称/创建者ID");

        buildNetButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildNetButton, "构建网络"); // NOI18N
        buildNetButton.setToolTipText("构建Group的自我中心网络"); // NOI18N

        searchButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchButton, "检索"); // NOI18N
        searchButton.setToolTipText("根据关键词，检索包含关键词的历史查询记录"); // NOI18N

        table.setModel(tableModel);
        jScrollPane.setViewportView(table);

        resultNum.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultNum, org.openide.util.NbBundle.getMessage(GroupSelector.class, "GroupSelector.resultNum.text")); // NOI18N

        refreshButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, "刷新列表"); // NOI18N

        comboBox.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        comboBox.setModel(comboBoxModel
        );

        separator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(keywordText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildNetButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultNum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buildNetButton, searchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(keywordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buildNetButton)
                        .addComponent(searchButton)
                        .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(separator2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultNum)
                    .addComponent(refreshButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildNetButton;
    private javax.swing.JComboBox<String> comboBox;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextField keywordText;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel resultNum;
    private javax.swing.JButton searchButton;
    private javax.swing.JToolBar.Separator separator2;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
