/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.egoNetBuilder;

import edu.seu.layout.LayoutImpl3;
import edu.seu.layout.MyLayoutController;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class EgoNetBuilder {
    
    private Color blueColor=new Color(0,150,255);
    public static final String GROUP_TYPE="GROUP";
    public static final String FRINED_TYPE="FRIEND";
    
    public void buildEgoNet(final String type,final Node[] nodes){
        new SwingWorker<Void,Void>(){
            ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
            ProgressTicket ticket = progressProvider.createTicket("", null);

            @Override
            protected Void doInBackground() throws Exception {


                try{
                    ticket.start();
                    ticket.setDisplayName("构建关联人二阶关系网络中...");
                    if(type.equals(EgoNetBuilder.FRINED_TYPE)){
                        buildFriendEgo(nodes[0]);
                    }else{
                        buildGroupEgo(nodes[0]);
                    }

                    //设置默认的颜色和大小
                    setDefaultSizeAndPosition();
                    ticket.finish();


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
                    if(graphModel==null){

                    }else if(layout.isRunning()){
                        layoutContainer.stopLayout();
                    }else{
                        layoutContainer.startLayout();
                    }
                    ticket.finish();

                }catch(Exception ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "当前所选数据无法构建二阶关系网络", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done(){
                if(ticket!=null){
                    ticket.finish();
                }
            }
        }.execute();
    }
    
    public void buildGroupEgo(Node node){
        String selectedGroupId=(String)node.getAttribute("id");//只选择一个，用户多选的话，也是只选择一个，忽略其他
        
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
        //添加图的一些基本信息
        graph.setAttribute("GraphDes", "群“"+selectedGroupId+"”的共享成员网络");
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
                if(graph.getEdge(graph.getNode(groupId), graph.getNode(userId))==null){
                    Edge e=graphModel.factory().newEdge(graph.getNode(groupId), graph.getNode(userId), false);
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

                                if(graph.getEdge(graph.getNode(firstOrderUser), graph.getNode(secondOrderGroup))==null){
                                    Edge e=graphModel.factory().newEdge(graph.getNode(firstOrderUser), graph.getNode(secondOrderGroup), false);
                                    graph.addEdge(e);
                                }
                            }
                        }
                    }
                }
            }
                
            
            //然后将种子Group的信息加入到图中
            this.addGroupInfo(graph, zhongziGroupId.toArray(new String[0]));
            
            //然后更改workspace名字
            String groupName=(String) graph.getNode(selectedGroupId).getAttribute("GROUPNAME");
            if(groupName==null){
                pc.renameWorkspace(pc.getCurrentWorkspace(),(String)graph.getAttribute("GraphDes") );
            }else{
                pc.renameWorkspace(pc.getCurrentWorkspace(), "群“"+groupName+"”的共享成员网络");
            }
            
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }finally{
            driver.close();
            if(graph.getNodeCount()==0){
                JOptionPane.showMessageDialog(null, "网络中节点数量为0");
                pc.closeCurrentWorkspace();
            }
        }
    }
    
    
    public void buildFriendEgo(Node node){
        String targetFriendId=(String) node.getAttribute("id");//只选择一个，用户多选的话，也是只选择一个，忽略其他
        
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
        //添加图的一些基本信息
        graph.setAttribute("GraphDes", "关联人“"+targetFriendId+"”的二阶关系网络");
        graph.setAttribute("FirstNodeColor", "紫色");
        graph.setAttribute("FirstNodeDes", "已关注关联人");
        graph.setAttribute("SecondNodeColor", "红色");
        graph.setAttribute("SecondNodeDes", "已关注关联人的好友");
        
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
                    n.setColor(new Color(204,0,153));//种子节点为紫红，普通好友节点为红色
                    n.setAttribute("defaultColor", "紫色");
                    n.setAttribute(classColumn, "已关注关联人");
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
                    n.setAttribute(classColumn, "已关注关联人的好友");
                    graph.addNode(n);
                }
                if(graph.getEdge(graph.getNode(friendId), graph.getNode(userId))==null){
                    Edge e=graphModel.factory().newEdge(graph.getNode(friendId), graph.getNode(userId), false);
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
                                n.setColor(new Color(204,0,153));//种子节点为紫色
                                n.setAttribute("defaultColor", "紫色");
                                n.setAttribute(classColumn, "已关注关联人");
                                graph.addNode(n);

                                
                                if(graph.getEdge(graph.getNode(firstOrderUser), graph.getNode(secondOrderFriend))==null){
                                    Edge e=graphModel.factory().newEdge(graph.getNode(firstOrderUser), graph.getNode(secondOrderFriend), false);
                                    graph.addEdge(e);
                                }
                            }
                        }
                    }
                }
            }

            //然后将种子Friend的信息加入到图中
            this.addAccountInfo(graph, zhongziFriend.toArray(new String[0]));
            
            //rename workspace
            Node targetNode=graph.getNode(targetFriendId);
            String nickName=(String) targetNode.getAttribute("NICKNAME");
            if(nickName==null){
                pc.renameWorkspace(pc.getCurrentWorkspace(),(String)graph.getAttribute("GraphDes") );
            }else{
                pc.renameWorkspace(pc.getCurrentWorkspace(), "关联人“"+nickName+"”的二阶关系网络");
            }
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误");
        }finally{
            driver.close();
            if(graph.getNodeCount()==0){
                JOptionPane.showMessageDialog(null, "网络中节点数量为0");
                pc.closeCurrentWorkspace();
            }
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
    
    
    private Column initAttributeColumns(GraphModel graphModel,String attr,Class type) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(attr);
        if (eigenCol == null) {
            eigenCol=nodeTable.addColumn(attr, type);
        }
        
        return eigenCol;
    }
    
    //给每个节点设置默认的节点位置，大小，颜色
    public void setDefaultSizeAndPosition(){
        
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
}
