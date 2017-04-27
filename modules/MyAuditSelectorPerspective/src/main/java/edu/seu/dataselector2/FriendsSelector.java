/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector2;

import edu.seu.dataselector.*;
import edu.seu.dataselector.drivers.SelectorDriver;
import edu.seu.dataselector.timeComponent.DateSetting;
import edu.seu.layout.LayoutImpl3;
import edu.seu.layout.MyLayoutController;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author hp-6380
 */
public class FriendsSelector extends javax.swing.JPanel {

    private final String relFriendNet="多关联人关系网络";
    private volatile static FriendsSelector instance;
    private DefaultTableModel tableModel=new DefaultTableModel();
    
    //时间选择面板
    DateSetting dateSetting=new DateSetting();
    private boolean dateSetedFlag;
    
    /**
     * Creates new form UserSelector
     */
    private FriendsSelector() {
        initLookAndFeel();
        initComponents();
        initAction();
        
//        new SwingWorker<Void,Void>(){
//            @Override
//            protected Void doInBackground() throws Exception {
//                getListData();
//                return null;
//            }
//            
//            @Override
//            protected void done(){
//                resultNum.setText("结果数量："+tableModel.getRowCount());
//            }
//        }.execute();
    }
    
    public static synchronized FriendsSelector getInstance(){
        if(instance==null){
            instance=new FriendsSelector();
        }
        return instance;
    }
    
    private void initAction(){
        
        timeSelectorButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimeInteval();//设置时间间隔
            }
        });
        
        searchButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                 //判断有没有设置过时间
                if(dateSetedFlag==false){
                    Object[] options={"好","跳过","取消"};
                    int type=JOptionPane.showOptionDialog(null, "您还未选择时间区间，是否现在选择？", "时间区间未选择提示",JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
                    switch (type) {
                        case JOptionPane.YES_OPTION:
                            setTimeInteval();
                            break;
                        case JOptionPane.NO_OPTION:
                            dateSetting.selectedStartTime=DateSetting.SELECTED_ALL;
                            getListData();
                            break;
                        default:
                    }
                }else{
                    getListData();
                }
            }
        });
        
        buildNetButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //根据不同选项来决定创建不同的网络
                new SwingWorker<Void,Void>(){
                    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                    ProgressTicket ticket = progressProvider.createTicket("", null);

                    @Override
                    protected Void doInBackground() throws Exception {
                        //先判断数据量够不够创建网络
                        int[] selectedRows=table.getSelectedRows();
                        if(selectedRows.length<2){
                            JOptionPane.showMessageDialog(null, "您所选择的数据量无法构建多关联人关系网络，请至少选择两条数据。", "提示", JOptionPane.INFORMATION_MESSAGE);
                            return null;
                        }
                            
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

                        try{
                            ticket.start();
                            ticket.setDisplayName("构建"+relFriendNet+"中...");
                            buildRelation3();

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

                            //进行节点布局
                            GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                            final LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
                            ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
                                @Override
                                public boolean cancel() {
                                    layout.cancel(true);
                                    return true;
                                }
                            });
                            ticket.start();
                            MyLayoutController layoutContainer=new MyLayoutController(layout);
                            if(graphModel==null||layout==null){

                            }else if(layout.isRunning()){
                                layoutContainer.stopLayout();
                            }else{
                                layoutContainer.startLayout();
                            }
                            ticket.finish();

                        }catch(Exception ex){
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "构建网络中出现错误");
                        }
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
                if(keywordText.getText().equals("微信ID/微信账号/QQ号码/手机号码")){
                    keywordText.setText("");
                }
            }
        });
        
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(keywordText.getText().equals("")||keywordText.getText().equals(" ")){
                    keywordText.setText("微信ID/微信账号/QQ号码/手机号码");
                }
            }
        });
        
    }
    
    
    private void buildRelation3(){
        
        //先构建网络，将节点一个个添加进入，然后再提取数据库中数据并且
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }else{
            Workspace workspace=pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph graph=graphModel.getUndirectedGraph();
        graph.setAttribute("GraphDes", "多关联人关系网络");
        graph.setAttribute("FirstNodeColor", "紫色");
        graph.setAttribute("FirstNodeDes", "已关注关联人");
        graph.setAttribute("SecondNodeColor", "红色");
        graph.setAttribute("SecondNodeDes", "已关注关联人的好友");

        //
        int[] selectedRows=table.getSelectedRows();
        int friendIdColumn=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("目标账号")){
                friendIdColumn=i;
                break;
            }
        }
        //提取出所有被选择数据行中的friendID数据
        List<String> friendIDs=new ArrayList<>();
        for(int i=0;i<selectedRows.length;i++){
            friendIDs.add((String)table.getValueAt(selectedRows[i], friendIdColumn));
        }

        SelectorDriver driver=new SelectorDriver();
        ResultSet rs=driver.getAllFriendsInfo2(friendIDs.toArray(new String[0]));

        try{
            //先预存节点属性，稍后等该删除的节点都删除之后再将属性一次性添加
            ResultSetMetaData rsmd=rs.getMetaData();
            Map<String,String[]> map=new HashMap<>();
            
            //初始化节点属性列表
            String[] columnName=new String[rsmd.getColumnCount()];
            Column[] columns=new Column[rsmd.getColumnCount()];
            for(int i=0;i<rsmd.getColumnCount();i++){
                columnName[i]=rsmd.getColumnName(i+1);
                columns[i]=initAttributeColumns(graphModel,rsmd.getColumnName(i+1),String.class);
            }
            Column classColumn=initAttributeColumns(graphModel,"节点性质",String.class);
            initAttributeColumns(graphModel, "defaultColor", String.class);

            while(rs.next()){
                String userId=rs.getString("USERID");
                String friendId=rs.getString("FRIENDID");
                if(graph.getNode(friendId)==null){
                    Node n=graphModel.factory().newNode(friendId);
                    n.setColor(new Color(204,0,153));//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "紫色");
                    n.setAttribute(classColumn, "已关注关联人");
                    graph.addNode(n);
                }
                if(graph.getNode(userId)==null){
                    Node n=graphModel.factory().newNode(userId);
                    n.setColor(Color.RED);//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "红色");
                    n.setAttribute(classColumn, "已关注关联人的好友");
                    graph.addNode(n);
                    
                    //==================
                    String[] att=new String[columnName.length];
                    for(int i=0;i<columnName.length;i++){
                        att[i]=rs.getString(columnName[i]);
                    }
                    map.put(userId, att);
                    //=================
                }

                if(graph.getEdge(graph.getNode(friendId), graph.getNode(userId))==null){
                    Edge e=graphModel.factory().newEdge(graph.getNode(friendId), graph.getNode(userId), false);
                    graph.addEdge(e);
                }
            }
            
            //删除多于节点
            removeUnjointedNodes(graph);
            
            if(graph.getNodeCount()!=0){
                //将节点属性重新添加进图（已关注关联人信息另外添加）
                Iterator<Node> ite=graph.getNodes().iterator();
                while(ite.hasNext()){
                    Node n=ite.next();
                    String[] attr=map.get((String)n.getId());
                    if(attr!=null){
                        for(int i=0;i<attr.length;i++){
                            if(!columnName[i].equals("FRIENDID")){
                                n.setAttribute(columnName[i], attr[i] );
                            }
                        }
                    }
                }

                //然后将已关注关联人的信息加入到图中
                //这里不能用friendIDs了，因为一些friendIDs在构建网络过程中已经被删除了。
                List<String> filteredFriend=new ArrayList<>();
                Iterator<Node> nodeIte=graph.getNodes().iterator();
                while(nodeIte.hasNext()){
                    Node n=nodeIte.next();
                    String type=(String)n.getAttribute("节点性质");
                    if(type.equals("已关注关联人")){
                        filteredFriend.add((String) n.getId());
                    }
                }
                this.addAccountInfo(graph, filteredFriend.toArray(new String[0]));

                //然后修改workspace名字
                Node n=graph.getNode(filteredFriend.get(0));
                String nickName=(String) n.getAttribute("NICKNAME");
                if(nickName==null){
                    pc.renameWorkspace(pc.getCurrentWorkspace(), "“"+friendIDs.get(0)+"”...等多关联人关系网络");
                }else{
                    pc.renameWorkspace(pc.getCurrentWorkspace(), "“"+nickName+"”...等多关联人关系网络");
                }
                
                
            }
            
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }finally{
            driver.close();
            if(graph.getNodeCount()==0){
                WindowManager.getDefault().invokeWhenUIReady(new Runnable(){
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, "网络中节点数量为0", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                pc.closeCurrentWorkspace();
            }
        }
            
    }
    
    
    
    /**
     * 2017-03-15添加，用这个方法来将度为1的关联人给删除，如果删除度为1的关联人之后导致已关注关联人也被删除，那就将已关注关联人也一并删除
     * @param graph 
     */
    private void removeUnjointedNodes(Graph graph){
        String nodeType;
        Node n;
        List<Node> list=new ArrayList<>();//保存度为1的关联人，在Iterator中删除节点会有问题
        Iterator<Node> ite=graph.getNodes().iterator();
        while(ite.hasNext()){
            n=ite.next();
            nodeType=(String) n.getAttribute("节点性质");
            if(nodeType.contains("已关注关联人的好友")&&graph.getDegree(n)<2){
                list.add(n);
            }
        }
        //删除节点
        for(int i=0;i<list.size();i++){
            graph.removeNode(list.get(i));
        }
        
        //再遍历一次图，看看是否有度为0的节点
        List<Node> list2=new ArrayList<>();
        ite=graph.getNodes().iterator();
        while(ite.hasNext()){
            n=ite.next();
            if(graph.getDegree(n)<1){
                list2.add(n);
            }
        }
        for(int i=0;i<list2.size();i++){
            graph.removeNode(list2.get(i));
        }
            
    }
    
    /**
     * 将种子节点信息加入到图中
     * @param graph
     * @param friendIDs 种子节点id
     */
    private void addAccountInfo(Graph graph, String[] friendIDs){
        SelectorDriver driver=new SelectorDriver();
        try{
            ResultSet rs=driver.getAllAccountInfo(friendIDs);
            ResultSetMetaData rsmd=rs.getMetaData();
            Column[] columns=new Column[rsmd.getColumnCount()];
            String[] columnNames=new String[rsmd.getColumnCount()];
            for(int i=0;i<columns.length;i++){
                columns[i]=initAttributeColumns(graph.getModel(),rsmd.getColumnName(i+1),String.class);
                columnNames[i]=rsmd.getColumnName(i+1);
            }


            while(rs.next()){
                
                //===============这部分说明============
                //这部分是查询种子节点的信息是否保存在Account表格中，如果保存了，则将种子节点的详细信息存入到图中
                //不过因为数据库中的问题：查询种子节点是可以根据微信账号，微信ID，qq号码和手机号码查询，所以friendID有四种不同类型的值
                //并且数据库中friend表中也有问题，有的种子节点把自身也算作自身的好友，有的种子节点没有将自身纳入到自己的好友中。
                //上述这种情况出现似乎是随机的，和按照那种类型查询种子节点没有关系。
                //这就导致图中可能会出现两个种子节点，并且一个作为种子节点，一个作为种子节点的好友节点。
                //在给种子节点添加信息时，需要将信息添加到图中的种子节点中，而不要添加到另一个“分身”非种子节点中。
                String[] nodeID=new String[4];
                nodeID[0]=rs.getString("USERID");
                nodeID[1]=rs.getString("ACCOUNTNAME");
                nodeID[2]=rs.getString("IMID");
                nodeID[3]=rs.getString("MOBILEPHONE");

                Node n=null;
                for(int i=0;i<nodeID.length;i++){//需要遍历完所有四种情况
                    if(nodeID[i]!=null&&graph.getNode(nodeID[i])!=null){
                        n=graph.getNode(nodeID[i]);
                        if(n.getAttribute("节点性质").equals("已关注关联人")){//如果当前节点不仅满足，还是种子节点，则返回该节点
                            break;
                        }
                    }
                }
                //==================添加属性===================
                if(n!=null){
                    for(int i=0;i<columnNames.length;i++){
                        n.setAttribute(columns[i], rs.getString(columnNames[i]));
                    }
                }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }finally{
            driver.close();
        }
    }
    
    
    private void filterTableList(String keyword){

        //找到keywork列表对应的列的序号
        int columnIndex=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("目标账号")){
                columnIndex=i;
                break;
            }
        }
        //开始遍历表格
        for(int i=0;i<tableModel.getRowCount();){
            String rowData=(String)table.getModel().getValueAt(i, columnIndex);
            if(rowData.equals(keyword)){
                i++;
            }else{
                tableModel.removeRow(i);
            }
        }
    }
    
    private void getListData(){
        Map<String,String> type=new HashMap<>();
        type.put("0", "手机号码");
        type.put("1", "微信账号");
        type.put("2", "QQ号码");
        type.put("5", "微信ID");
        ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
        ProgressTicket ticket = progressProvider.createTicket("正在从数据库中提取数据...", null);
        ticket.start();
        
        //设置一些按钮是不是被禁用
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                table.getTableHeader().setEnabled(false);//禁用表头功能
            }
        });
        
        SelectorDriver keywordSelector=new SelectorDriver();
        try{
            String startTime=dateSetting.selectedStartTime;
            
            ResultSet rs;
            if(startTime==null||startTime.equals(DateSetting.SELECTED_ALL)){
                rs=keywordSelector.getFriendsList();
            }else{
                rs=keywordSelector.getFriendListWithTime(dateSetting.selectedStartTime, dateSetting.selectedEndTime);
            }
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            int numOfColumn = resetTableColumnName(rs);
            //读取表中数据到Jtable中
            while (rs.next()) {
                String[] rowData = new String[numOfColumn];
                //注意：遍历resultSet的下标得从1开始。
                for (int i = 1; i <= numOfColumn; i++) {
                    if(i==2&&type.get(rs.getString(i))!=null){
                        rowData[i - 1] = type.get(rs.getString(i));
                    }else{
                        rowData[i - 1] = rs.getString(i);
                    }
                }
                tableModel.addRow(rowData);
            }
        }catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "列表显示数据过程出错");
        }finally{
            //关闭数据库链接
            keywordSelector.close();
            ticket.finish();
        }
        
        //重新启用一些按钮,以及显示一些信息
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                String keyword=keywordText.getText();
                String timeIntever;
                if(dateSetting.selectedStartTime.equals(DateSetting.SELECTED_ALL)){
                    timeIntever="已选择时间范围：不设限";
                }else{
                    timeIntever="已选择时间范围："+dateSetting.selectedStartTime+"-"+dateSetting.selectedEndTime;
                }
                if(keyword.equals("")||keyword.equals(" ")||keyword.equals("微信ID/微信账号/QQ号码/手机号码")){
                    resultNum.setText(timeIntever+"   结果数量："+table.getRowCount());
                }else{
                    filterTableList(keyword);
                    resultNum.setText(timeIntever+"   关键词：\""+keyword+"\"\t   结果数量："+table.getRowCount());
                }
                table.getTableHeader().setEnabled(true);//启用表头功能
                if(table.getRowCount()>0){
                    resetTableColumnWidth();
                }
            }
        });
        
    }
    
    //设置时间间隔
    private void setTimeInteval(){
        String oldStartTime=dateSetting.startTime;
        String oldEndTime=dateSetting.endTime;
        DialogDescriptor dd=new DialogDescriptor(dateSetting,"时间选择");
        DialogDisplayer.getDefault().notify(dd);
        Integer value=(Integer)dd.getValue();
        if(value==0){
            //判断用户自己选择的时间是不是符合要求
            if(dateSetting.jrb5SelectedFlag){
                if(dateSetting.getStartField().contains("----年--月--日")||dateSetting.getEndField().contains("----年--月--日")||dateSetting.startTime.compareTo(dateSetting.endTime)>0){
                    JOptionPane.showMessageDialog(null,"时间选择错误，请重新选择","警告",JOptionPane.ERROR_MESSAGE,ImageUtilities.loadImageIcon("edu/seu/lizhenglong/image/warning_32.png", false));
                    setTimeInteval();
                    return;
                }
            }
            dateSetting.selectedStartTime=dateSetting.startTime;
            dateSetting.selectedEndTime=dateSetting.endTime;
            dateSetedFlag=true;
            
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    if(dateSetting.jrb5SelectedFlag){
                        resultNum.setText("已选择时间范围："+dateSetting.getStartField()+"00:00:00 至 "+dateSetting.getEndField()+"23:59:59");
                    }else if(dateSetting.selectedStartTime.equals(DateSetting.SELECTED_ALL)){
                        resultNum.setText("已选择时间范围：不设限");
                    }else{
                        resultNum.setText("已选择时间范围："+dateSetting.selectedStartTime+" 至 "+dateSetting.selectedEndTime);
                    }
                }
            });
        }else{
            dateSetting.selectedStartTime=oldStartTime;
            dateSetting.selectedEndTime=oldEndTime;
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
    
    //给每个节点设置默认的节点位置，大小，颜色
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
            node.setSize(70f+neighborCount*2);
            
            //随机分配节点坐标
            float x=(float) ((0.01 + Math.random()) * 2000)-1000;
            float y=(float) ((0.01 + Math.random()) * 2000)-1000;
            node.setX(x);
            node.setY(y);
        }
    }
    
    private int resetTableColumnName(ResultSet rs) {
        Map<String,String> ccn=new HashMap<>();
        ccn.put("ACCOUNT", "目标账号");
        ccn.put("NUMBERTYPE", "目标类型");
        ccn.put("APPLYTIME", "审批申请时间");
        ccn.put("AUDITTIME", "审批时间");
        ccn.put("CMDID", "CMDID");
        int numOfColumn = 0;
        //设置列名
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            numOfColumn = rsmd.getColumnCount();
            String[] newIdentiters = new String[numOfColumn];
            for (int i = 0; i < numOfColumn; i++) {
                newIdentiters[i] = ccn.get(rsmd.getColumnName(i + 1));//注意：rsmd中Column从下标1开始算起
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
        separator2 = new javax.swing.JToolBar.Separator();
        timeSelectorButton = new javax.swing.JButton();

        keywordText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        keywordText.setText("微信ID/微信账号/QQ号码/手机号码"); // NOI18N

        buildNetButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildNetButton, "构建关联人好友网络"); // NOI18N
        buildNetButton.setToolTipText(null);

        searchButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchButton, "检索"); // NOI18N
        searchButton.setToolTipText(org.openide.util.NbBundle.getMessage(FriendsSelector.class, "FriendsSelector.searchButton.toolTipText")); // NOI18N

        table.setModel(tableModel);
        table.setRowSorter(new TableRowSorter(tableModel));
        jScrollPane.setViewportView(table);

        resultNum.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultNum, org.openide.util.NbBundle.getMessage(FriendsSelector.class, "FriendsSelector.resultNum.text")); // NOI18N

        separator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        timeSelectorButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(timeSelectorButton, "选择时间"); // NOI18N
        timeSelectorButton.setToolTipText(org.openide.util.NbBundle.getMessage(FriendsSelector.class, "FriendsSelector.timeSelectorButton.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(keywordText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSelectorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildNetButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultNum)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {searchButton, timeSelectorButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(keywordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buildNetButton)
                        .addComponent(searchButton)
                        .addComponent(timeSelectorButton))
                    .addComponent(separator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultNum)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildNetButton;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextField keywordText;
    private javax.swing.JLabel resultNum;
    private javax.swing.JButton searchButton;
    private javax.swing.JToolBar.Separator separator2;
    private javax.swing.JTable table;
    private javax.swing.JButton timeSelectorButton;
    // End of variables declaration//GEN-END:variables
}
