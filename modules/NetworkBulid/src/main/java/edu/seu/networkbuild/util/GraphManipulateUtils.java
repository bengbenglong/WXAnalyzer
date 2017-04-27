/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class GraphManipulateUtils {
     //设置每个节点的大小，位置等属性
    public static void setNodeAttributes(Graph graph){
        Node node;
        Iterator<Node> ite=graph.getNodes().iterator();
        
        while(ite.hasNext()){
            node=ite.next();
            
            //设置节点大小
            int neighborCount=graph.getDegree(node);
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
    
    public static void setNodeSize(Graph graph){
        Node node;
        Iterator<Node> ite=graph.getNodes().iterator();
        
        while(ite.hasNext()){
            node=ite.next();
            int neighborCount=graph.getDegree(node);
            node.setSize(50f+neighborCount*2);
        }
    }
    
    public static void forceAtlasLayout(GraphModel graphModel){
        LayoutController layoutCtrl=Lookup.getDefault().lookup(LayoutController.class);
        LayoutModel layoutModel=layoutCtrl.getModel();
        Layout layout=null;
        List<LayoutBuilder> builders = new ArrayList<LayoutBuilder>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        
        for(int i=0; i<builders.size(); i++){
            if(builders.get(i).getName().equals("ForceAtlas 2"))
                layout=layoutModel.getLayout(builders.get(i));
        }
        
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        
        layout.initAlgo();
        for (int i = 0; i < 30000 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
    
    public static void yifuLayout(GraphModel graphModel){
        LayoutController layoutCtrl=Lookup.getDefault().lookup(LayoutController.class);
        LayoutModel layoutModel=layoutCtrl.getModel();
        Layout layout=null;
        List<LayoutBuilder> builders = new ArrayList<>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        
        for(int i=0; i<builders.size(); i++){
            if(builders.get(i).getName().equals("Yifan Hu"))
                layout=layoutModel.getLayout(builders.get(i));
        }
        
//        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        
        layout.initAlgo();
        for (int i = 0; i < 30000 && layout.canAlgo() ; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
    
    
    
}
