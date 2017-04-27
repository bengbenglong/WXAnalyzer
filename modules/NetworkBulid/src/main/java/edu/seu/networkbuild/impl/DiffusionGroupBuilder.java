/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.impl;

import edu.seu.networkbuild.api.BuildAttribute;
import edu.seu.networkbuild.api.BuildDiffNet;
import edu.seu.networkbuild.diffusion.DiffusionNet;
import edu.seu.networkbuild.drivers.GroupInfoFetcher;
import edu.seu.networkbuild.util.GraphManipulateUtils;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=BuildDiffNet.class)
public class DiffusionGroupBuilder implements BuildDiffNet{
    
    private Color blueColor=new Color(0,150,255);
    
    private ProjectController pc;
    private GraphModel graphModel;
    private Graph graph;
    
    @Override
    public void build() {
        DiffusionNet diffGraph=DiffusionNet.getInstance();
        Map<String,List<String>> map=diffGraph.getTopologyGraphMap();
        this.convertMapToGephi(map);  
    }
    
    private void convertMapToGephi(Map<String,List<String>> map){
        pc=Lookup.getDefault().lookup(ProjectController.class);
        Workspace w;
        if(pc.getCurrentProject()==null){
            pc.newProject();
            w=pc.getCurrentWorkspace();
        }else{
            w=pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(w);
        }
        pc.renameWorkspace(w, "消息传播概率路径网络");
        
        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph=graphModel.getDirectedGraph();
        
        graph.setAttribute(BuildAttribute.BUILD_TYPE, BuildAttribute.DIFF_GROUP_TYPE);
        initAttributeColunms(graphModel, "defaultColor","defaultColor");
        for(String s:map.keySet()){
            
            if(graph.getNode(s)==null){
                Node n=graphModel.factory().newNode(s);
                n.setColor(blueColor);
                n.setAttribute("defaultColor", "蓝色");
                graph.addNode(n);
            }
            List<String> neighbors=map.get(s);
            for(int i=0; i<neighbors.size(); i++){
                String target=neighbors.get(i);
                //先添加节点，然后添加边
                if(graph.getNode(target)==null){
                    Node n=graphModel.factory().newNode(target);
                    n.setColor(blueColor);
                    n.setAttribute("defaultColor", "蓝色");
                    graph.addNode(n);
                }
                //添加边
                Node sourceNode=graph.getNode(s);
                Node targetNode=graph.getNode(target);
                Edge edge=graphModel.factory().newEdge(sourceNode, targetNode);
                if(!graph.contains(edge)){
                    graph.addEdge(edge);
                }
            }
        }
        GraphManipulateUtils.setNodeAttributes(graph);
        //给每个节点添加属性
        fetchNodeAttribute();
        //给图添加信息
        graph.setAttribute("GraphDes", "消息传播概率路径网络");
        graph.setAttribute("FirstNodeDes", "源节点");
        graph.setAttribute("FirstNodeColor", "橙色");
        graph.setAttribute("SecondNodeDes", "群");
        graph.setAttribute("SecondNodeColor", "蓝色");
    }
    
    //在读取消息传播网络的时候，也读取TB_WX_GROUP表格中的数据，因为消息传播网络也需要显示详情
    private void fetchNodeAttribute(){
        GroupInfoFetcher dif=new GroupInfoFetcher();
        ResultSet rs=dif.getGroupInfo();
        String[] columnName;
        try {
            ResultSetMetaData rsmd=rs.getMetaData();
            columnName=new String[rsmd.getColumnCount()];
            for(int i=0;i<columnName.length;i++){
                columnName[i]=rsmd.getColumnName(i+1);
            }
            
            Column[] columns=new Column[columnName.length];
            //初始化属性表
            for(int i=0;i<columnName.length;i++){
                String s1=columnName[i];
                if(s1!=null){
                    columns[i]=initAttributeColunms(graphModel,s1,s1);
                }else{
                    columns[i]=initAttributeColunms(graphModel,columnName[i],columnName[i]);
                }
            }
            
            //开始读数据
            while(rs.next()){
                String groupId=rs.getString("GROUPID");
                if(graph.getNode(groupId)!=null){
                    for(int j=1;j<columns.length;j++){
                        if(rs.getString(j+1)!=null){
                            graph.getNode(groupId).setAttribute(columns[j], rs.getString(j+1));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }finally{
            dif.close();
        }
    }
    
    private Column initAttributeColunms(GraphModel graphModel,String attr,String attrColumnName) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(attr);
        if (eigenCol == null) {
            eigenCol = nodeTable.addColumn(attr, attrColumnName, String.class, "");
        }
        return eigenCol;
    }
    
}
