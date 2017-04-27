/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector;

import edu.seu.dataselector.drivers.SelectorDriver;
import edu.seu.dataselector.singleton.FriendsNetSingleton;
import edu.seu.layout.LayoutImpl;
import edu.seu.networkbuild.api.BuildAttribute;
import edu.seu.networkbuild.api.BuildNet;
import edu.seu.networkbuild.api.MyTableAttr;
import edu.seu.networkbuild.impl.FriendsNetBuilder;
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
public class FriendSelector extends javax.swing.JPanel {

    private final String egoFriendNet="关联人的自中心网络";
    private final String relFriendNet="关联人之间关系网";
    private volatile static FriendSelector instance;
    private DefaultTableModel tableModel=new DefaultTableModel();
    private ComboBoxModel<String> comboBoxModel=new DefaultComboBoxModel<>(new String[]{egoFriendNet,relFriendNet});
    
    /**
     * Creates new form UserSelector
     */
    private FriendSelector() {
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
    
    public static synchronized FriendSelector getInstance(){
        if(instance==null){
            instance=new FriendSelector();
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
                resultNum.setText("结果数量："+tableModel.getRowCount());
                
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
                    resultNum.setText("结果数量："+table.getRowCount());
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
                
                //根据不同选项来决定创建不同的网络
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
                        
                        try{
                            if(comboBox.getSelectedItem().equals(egoFriendNet)){
                                ticket.start();
                                ticket.setDisplayName("构建"+egoFriendNet+"中...");
                                buildEgoNet();
                            }else if(comboBox.getSelectedItem().equals(relFriendNet)){
                                ticket.start();
                                ticket.setDisplayName("构建"+relFriendNet+"中...");
                                buildRelation2();
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
                            GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                            layout.forceAtlasLayout(graphModel);

                            ticket.finish();

                        }catch(Exception ex){
                            ex.printStackTrace();
//                            JOptionPane.showMessageDialog(null, "出现错误");
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
    
    private void buildRelationNet(){
        int[] selectedRows=table.getSelectedRows();
        int friendIdColumn=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("ACCOUNT")){
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
        ResultSet rs=driver.getAllFriendsInfo(friendIDs.toArray(new String[0]));
        
        try{
            BuildNet buildNet=new FriendsNetBuilder();
            List<String[]> minNeedfulData=new ArrayList<>();
            ResultSetMetaData rsmd=rs.getMetaData();
            String[] columnNames=new String[rsmd.getColumnCount()];
            for(int i=0;i<columnNames.length;i++){
                columnNames[i]=rsmd.getColumnName(i+1);
            }
            MyTableAttr tableAttr=new MyTableAttr(columnNames);
            while(rs.next()){
                String[] rowData=new String[2];
                rowData[0]=rs.getString("FRIENDID");
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
            buildNet.init(minNeedfulData,BuildAttribute.FRIENDS_RELATION,tableAttr);
            buildNet.build();
            
            //将构建好的节点设置颜色，用户为红色
            Graph graph=Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            Iterator<Node> ite=graph.getNodes().iterator();
            initAttributeColumns(graph.getModel(),"节点",String.class);
            while(ite.hasNext()){
                Node n=ite.next();
                n.setColor(Color.RED);
                n.setAttribute("defaultColor", "红色");
            }
            
            //添加图的一些基本信息
            graph.setAttribute("GraphDes", "关联人关系网络");
            graph.setAttribute("FirstNodeColor", "红色");
            graph.setAttribute("FirstNodeDes", "种子成员");
            graph.setAttribute("SecondNodeColor", " ");
            graph.setAttribute("SecondNodeDes", " ");
            
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "构建网络中出错");
        }
    }
    
    private void buildRelation2(){
        //先构建网络，将节点一个个添加进入，然后再提取数据库中数据并且
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace;
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }else{
            workspace=pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph graph=graphModel.getDirectedGraph();
        
        //
        int[] selectedRows=table.getSelectedRows();
        int friendIdColumn=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("ACCOUNT")){
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
            //初始化节点属性列表
            ResultSetMetaData rsmd=rs.getMetaData();
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
                    n.setColor(Color.MAGENTA);//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "紫色");
                    n.setAttribute(classColumn, "种子成员");
                    graph.addNode(n);
                }
                if(graph.getNode(userId)==null){
                    Node n=graphModel.factory().newNode(userId);
                    n.setColor(Color.RED);//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "红色");
                    for(int i=0;i<columns.length;i++){
                        n.setAttribute(columns[i], rs.getString(columnName[i]));
                    }
                    n.setAttribute(classColumn, "好友成员");
                    graph.addNode(n);
                }
                Edge e=graphModel.factory().newEdge(graph.getNode(friendId), graph.getNode(userId), false);
                if(graph.contains(e)==false){
                    graph.addEdge(e);
                }
                
                
                //rename workspace
                pc.renameWorkspace(pc.getCurrentWorkspace(), "好友“"+friendIDs.get(0)+"”...等之间的关联人关系网络");
                
                //然后将种子Friend的信息加入到图中
                this.addAccountInfo(graph, friendIDs.toArray(new String[0]));
            
                if(graph.getNodeCount()==0){
                    JOptionPane.showMessageDialog(null, "网络中节点数量为0");
                }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
//            JOptionPane.showMessageDialog(null, "出现错误");
        }
    }
    
    private void buildEgoNet(){
        
        
        //选择待构建网络的种子节点
        int[] selectedRow=table.getSelectedRows();
        int friendIdColumn=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("ACCOUNT")){
                friendIdColumn=i;
                break;
            }
        }
        String targetFriendId=(String)table.getValueAt(selectedRow[0], friendIdColumn);//只选择一个，用户多选的话，也是只选择一个，忽略其他
        
        //先构建网络，将节点一个个添加进入，然后再提取数据库中数据并且
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace;
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }else{
            workspace=pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph graph=graphModel.getUndirectedGraph();
        //添加图的一些基本信息
        graph.setAttribute("GraphDes", "关联人“"+targetFriendId+"”的自我中心网络");
        graph.setAttribute("FirstNodeColor", "紫色");
        graph.setAttribute("FirstNodeDes", "种子成员");
        graph.setAttribute("SecondNodeColor", "红色");
        graph.setAttribute("SecondNodeDes", "好友成员");
        //rename workspace
        pc.renameWorkspace(pc.getCurrentWorkspace(),"关联人“"+targetFriendId+"”的自我中心网络" );
        
        
        SelectorDriver driver=new SelectorDriver();
        ResultSet rs=driver.getSingleFriendsInfo(targetFriendId);
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
            initAttributeColumns(graphModel, "defaultColor", String.class);
            
            //先添加目标节点的一阶ego-network
            while(rs.next()){
                String friendId=rs.getString("FRIENDID");
                String userId=rs.getString("USERID");
                if(graph.getNode(friendId)==null){
                    Node n=graphModel.factory().newNode(friendId);
                    n.setColor(Color.MAGENTA);//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "紫色");
                    n.setAttribute(classColumn, "种子成员");
                    //这个好像不对吧
//                    for(int i=0;i<columns.length;i++){
//                        n.setAttribute(columns[i], rs.getString(columnName[i]));
//                    }
                    graph.addNode(n);
                }
                if(graph.getNode(userId)==null){
                    Node n=graphModel.factory().newNode(userId);
                    n.setColor(Color.RED);//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "红色");
                    for(int i=0;i<columns.length;i++){
                        n.setAttribute(columns[i], rs.getString(columnName[i]));
                    }
                    n.setAttribute(classColumn, "好友成员");
                    graph.addNode(n);
                }
                Edge e=graphModel.factory().newEdge(graph.getNode(friendId), graph.getNode(userId), false);
                if(graph.contains(e)==false){
                    graph.addEdge(e);
                }
            }
            //再遍历邻居节点是否有种子节点，如果有种子节点，把该种子节点加入到网络中
            FriendsNetSingleton friendsNetSin=FriendsNetSingleton.getInstance();
            Map<String,Set<String>> friendsNet=friendsNetSin.getFriendNet();
            Map<String,Set<String>> usersNet=friendsNetSin.getUserNet();
            List<String> zhongziFriend=new ArrayList<>();
            zhongziFriend.add(targetFriendId);
            if(friendsNet.get(targetFriendId)!=null){
                for(String firstOrderUser:friendsNet.get(targetFriendId)){
                    if(usersNet.get(firstOrderUser)!=null){
                        for(String secondOrderFriend:usersNet.get(firstOrderUser)){
                            //如果已经在节点中了，也不能添加边，要不变成所有边全是双向的了
                            if(graph.getNode(secondOrderFriend)==null){
                                zhongziFriend.add(secondOrderFriend);
                                Node n=graphModel.factory().newNode(secondOrderFriend);
                                n.setColor(Color.MAGENTA);//种子节点为紫色
                                n.setAttribute("defaultColor", "紫色");
                                n.setAttribute(classColumn, "种子成员");
                                graph.addNode(n);

                                Edge e=graphModel.factory().newEdge(graph.getNode(firstOrderUser), graph.getNode(secondOrderFriend));
                                if(graph.contains(e)==false){
                                    graph.addEdge(e);
                                }
                            }
                        }
                    }
                }
            }
            
            //然后将种子Friend的信息加入到图中
            this.addAccountInfo(graph, zhongziFriend.toArray(new String[0]));
            
            if(graph.getNodeCount()==0){
                JOptionPane.showMessageDialog(null, "网络中节点数量为0");
            }
            
        }catch(SQLException ex){
            ex.printStackTrace();
//            JOptionPane.showMessageDialog(null, "出现错误");
        }finally{
            driver.close();
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
                        if(n.getAttribute("节点性质").equals("种子节点")){//如果当前节点不仅满足，还是种子节点，则返回该节点
                            break;
                        }
                    }
                }
                //==================添加属性===================
                for(int i=0;i<columnNames.length;i++){
                    n.setAttribute(columns[i], rs.getString(columnNames[i]));
                }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
//            JOptionPane.showMessageDialog(null, "出现错误");
        }finally{
            driver.close();
        }
    }
    
//    private void buildFriendsNet(){
//        try{
//            SelectorDriver driver=new SelectorDriver();
//            ResultSet rs=driver.getFriendsNet();
//            
//            while(rs.next()){
//                String friend=rs.getString("FRIENDID");
//                String targetId=rs.getString("USERID");
//                
//                //添加
//                Set<String> targetSet=friendsNet.get(friend);
//                if(targetSet==null){
//                    Set<String> newTargetSet=new HashSet<>();
//                    newTargetSet.add(targetId);
//                    friendsNet.put(friend, newTargetSet);
//                }else if(targetSet.contains(targetId)==false){
//                    targetSet.add(targetId);
//                    friendsNet.put(friend, targetSet);
//                }
//            }
//        }catch(SQLException ex){
//            ex.printStackTrace();
//            JOptionPane.showMessageDialog(null, "出现错误！");
//        }
//    }
    
    private void filterTableList(String keyword){

        //找到keywork列表对应的列的序号
        int columnIndex=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("ACCOUNT")){
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
        SelectorDriver keywordSelector=new SelectorDriver();
        try{
            ResultSet rs=keywordSelector.getFriendsList();
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
        }catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "列表显示数据过程出错");
        }finally{
            //关闭数据库链接
            keywordSelector.close();
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
            node.setSize(50f+neighborCount*2);
            
            //随机分配节点坐标
            float x=(float) ((0.01 + Math.random()) * 2000)-1000;
            float y=(float) ((0.01 + Math.random()) * 2000)-1000;
            node.setX(x);
            node.setY(y);
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

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        keywordText.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        keywordText.setText("微信ID/微信账号/QQ号码/手机号码"); // NOI18N

        buildNetButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildNetButton, "构建网络"); // NOI18N
        buildNetButton.setToolTipText("建立目标用户的二阶自我中心网络（ego-network）"); // NOI18N

        searchButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchButton, "检索"); // NOI18N
        searchButton.setToolTipText(org.openide.util.NbBundle.getMessage(FriendSelector.class, "FriendSelector.searchButton.toolTipText")); // NOI18N

        table.setModel(tableModel);
        table.setRowSorter(new TableRowSorter(tableModel));
        jScrollPane.setViewportView(table);

        resultNum.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultNum, org.openide.util.NbBundle.getMessage(FriendSelector.class, "FriendSelector.resultNum.text")); // NOI18N

        refreshButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, "刷新列表"); // NOI18N

        comboBox.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        comboBox.setModel(comboBoxModel);

        separator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(keywordText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buildNetButton, refreshButton, searchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(keywordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buildNetButton)
                        .addComponent(searchButton)
                        .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(separator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
