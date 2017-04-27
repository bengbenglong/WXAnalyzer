/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector2;

import java.awt.Color;
import java.util.Iterator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

/**
 *
 * @author hp-6380
 */
public class CommonMethods {
    
    public Column initAttributeColumns(GraphModel graphModel,String attr,Class type) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(attr);
        if (eigenCol == null) {
            eigenCol=nodeTable.addColumn(attr, type);
        }
        
        return eigenCol;
    }
    
    public Column initEdgeAttrColumns(GraphModel graphModel, String attr, Class type){
        Table edgeTable=graphModel.getEdgeTable();
        Column eigenCol=edgeTable.getColumn(attr);
        if(eigenCol==null){
            eigenCol=edgeTable.addColumn(attr, type);
        }
        return eigenCol;
    }
    
    //给每个节点设置默认的节点位置，大小，颜色
    public static void setDefaultSizeAndPosition(GraphModel graphModel){
        Node node;
        Iterator<Node> nodeIte=graphModel.getGraph().getNodes().iterator();
        
        while(nodeIte.hasNext()){
            node=nodeIte.next();
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
