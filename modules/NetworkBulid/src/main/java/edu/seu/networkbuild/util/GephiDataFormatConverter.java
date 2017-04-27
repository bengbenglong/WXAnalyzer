/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.util;

import edu.seu.layout.LayoutImpl;
import edu.seu.networkbuild.api.MyTableAttr;
import java.awt.Color;
import java.util.Map;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class GephiDataFormatConverter {
    private Color blueColor=new Color(0,150,255);
    ProjectController projectCtrl;
    Workspace workspace;
    GraphModel graphModel;
    
    public DirectedGraph toDirectedGraph(Map<String, ? extends Number> map, MyTableAttr tableAttr){
        
        projectCtrl=Lookup.getDefault().lookup(ProjectController.class);
        if(projectCtrl.getCurrentProject()==null){
            projectCtrl.newProject();
        }else{
            workspace=projectCtrl.newWorkspace(projectCtrl.getCurrentProject());
            projectCtrl.openWorkspace(workspace);
        }
        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        
        
        String[] tableAttrName=tableAttr.getColumnName();//列属性名称
        initAttributeColunms(graphModel, tableAttrName);
        for(String s:map.keySet()){
            String[] users=s.split("\t");
            Number weight=map.get(s);
            
            //添加节点，如果节点已经在图中了便不再添加
            if(directedGraph.getNode(users[0])==null){
                Node n1=graphModel.factory().newNode(users[0]);
                n1.setLabel(users[0]);
                directedGraph.addNode(n1);
                
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[0]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n1.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }if(directedGraph.getNode(users[1])==null){
                Node n2=graphModel.factory().newNode(users[1]);
                n2.setLabel(users[1]);
                directedGraph.addNode(n2);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[1]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n2.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }
            
            //添加边
            Node n1=directedGraph.getNode(users[0]);
            Node n2=directedGraph.getNode(users[1]);
            Edge edge=graphModel.factory().newEdge(n1, n2, 0, weight.floatValue(), true);
            if(!directedGraph.contains(edge)){
                directedGraph.addEdge(edge);
            }
        }
        GraphManipulateUtils.setNodeAttributes(directedGraph);
        
        //清理掉myTableAttr
        tableAttr.clear();
        
        return graphModel.getDirectedGraph();
    }
    
    public DirectedGraph mapStringToDirectedGraph(Map<String,String> map, MyTableAttr tableAttr){
        projectCtrl=Lookup.getDefault().lookup(ProjectController.class);
         if(projectCtrl.getCurrentProject()==null){
            projectCtrl.newProject();
        }else{
            workspace=projectCtrl.newWorkspace(projectCtrl.getCurrentProject());
            projectCtrl.openWorkspace(workspace);
         }
        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        
        String[] tableAttrName=tableAttr.getColumnName();//列属性名称
        initAttributeColunms(graphModel, tableAttrName);
        for(String s:map.keySet()){
            String[] users=s.split("\t");
            String weight=map.get(s);
            
            //添加节点，已经添加过的节点不再添加
            if(directedGraph.getNode(users[0])==null){
                Node n1=graphModel.factory().newNode();
                n1.setLabel(users[0]);
                directedGraph.addNode(n1);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[0]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n1.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }
            if(directedGraph.getNode(users[1])==null){
                Node n2=graphModel.factory().newNode();
                n2.setLabel(users[1]);
                directedGraph.addNode(n2);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[1]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n2.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }
            
            //添加边
            Node n1=directedGraph.getNode(users[0]);
            Node n2=directedGraph.getNode(users[1]);
            Edge edge=graphModel.factory().newEdge(n1, n2, 0, Float.parseFloat(weight), true);
            if(!directedGraph.contains(edge)){
                directedGraph.addEdge(edge);
            }
        }
        GraphManipulateUtils.setNodeAttributes(directedGraph);
        
        //清理MyTbaleAttr
        tableAttr.clear();
        return graphModel.getDirectedGraph();
    }
    
    public UndirectedGraph mapStringToUndirectedGraph(Map<String,String> map, MyTableAttr tableAttr){
        projectCtrl=Lookup.getDefault().lookup(ProjectController.class);
         if(projectCtrl.getCurrentProject()==null){
            projectCtrl.newProject();
        }else{
            workspace=projectCtrl.newWorkspace(projectCtrl.getCurrentProject());
            projectCtrl.openWorkspace(workspace);
        }
        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        
        String[] tableAttrName=tableAttr.getColumnName();//列属性名称
        initAttributeColunms(graphModel, tableAttrName);
        for(String s:map.keySet()){
            String[] users=s.split("\t");
            String weight=map.get(s);
            
            //添加节点，如果节点已经添加过则不再添加
            if(undirectedGraph.getNode(users[0])==null){
                Node n1=graphModel.factory().newNode(users[0]);
                n1.setLabel(users[0]);
                undirectedGraph.addNode(n1);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[0]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n1.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }
            if(undirectedGraph.getNode(users[1])==null){
                Node n2=graphModel.factory().newNode(users[1]);
                n2.setLabel(users[1]);
                undirectedGraph.addNode(n2);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[1]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n2.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }
            
            //添加边
            Node n1=undirectedGraph.getNode(users[0]);
            Node n2=undirectedGraph.getNode(users[1]);
            Edge edge=graphModel.factory().newEdge(n1, n2, 0, Float.parseFloat(weight), false);
            if(!undirectedGraph.contains(edge)){
                undirectedGraph.addEdge(edge);
            }
        }
        GraphManipulateUtils.setNodeAttributes(undirectedGraph);
        
        //清理MyTableAttr
        tableAttr.clear();
        return undirectedGraph;
    }
    
    public UndirectedGraph mapNumberToUndirectedGraph(Map<String, ? extends Number> map, MyTableAttr tableAttr){
        projectCtrl=Lookup.getDefault().lookup(ProjectController.class);
        if(projectCtrl.getCurrentProject()==null){
            projectCtrl.newProject();
        }else{
            workspace=projectCtrl.newWorkspace(projectCtrl.getCurrentProject());
            projectCtrl.openWorkspace(workspace);
        }
        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        
       
        
        String[] tableAttrName=tableAttr.getColumnName();//列属性名称
        initAttributeColunms(graphModel, tableAttrName);
        for(String s:map.keySet()){
            String[] users=s.split("\t");
            Number weight=map.get(s);
            
            //添加节点，如果节点已经添加过则不再添加
            if(undirectedGraph.getNode(users[0])==null){
                Node n1=graphModel.factory().newNode(users[0]);
                undirectedGraph.addNode(n1);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[0]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        String name=tableAttrName[i];
                        String attr=nodeAttrs[i];
                        n1.setAttribute(name, attr);
                    }
                }
            }
            if(undirectedGraph.getNode(users[1])==null){
                Node n2=graphModel.factory().newNode(users[1]);
                undirectedGraph.addNode(n2);
                //将tableAttr中的属性添加进graph中
                String[] nodeAttrs=tableAttr.getAtttributes(users[1]);
                for(int i=0; i<nodeAttrs.length; i++){
                    if(nodeAttrs[i]!=null){
                        n2.setAttribute(tableAttrName[i], nodeAttrs[i]);
                    }
                }
            }
            
            //添加边
            Node n1=undirectedGraph.getNode(users[0]);
            Node n2=undirectedGraph.getNode(users[1]);
            Edge edge=graphModel.factory().newEdge(n1, n2, 0, weight.floatValue(), false);
            if(!undirectedGraph.contains(edge)){
                undirectedGraph.addEdge(edge);
            }
        }
        GraphManipulateUtils.setNodeAttributes(undirectedGraph);
        
        //清理MyTableAttr
        tableAttr.clear();
        return undirectedGraph;
    }
    
    private void initAttributeColunms(GraphModel graphModel,String[] columnsAttributes){
        Table nodeTable=graphModel.getNodeTable();
        for(int i=0; i<columnsAttributes.length; i++){
            if(!nodeTable.hasColumn(columnsAttributes[i])){
                nodeTable.addColumn(columnsAttributes[i], String.class);
            }
        }
    }
    
    
    
}
