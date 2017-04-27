/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.datalab.plugin.manipulators.edges;

import edu.seu.myviz.msgDiffDemo.MsgDiff;
import edu.seu.myviz.msgDiffDemo.DiffContainer;
import javax.swing.Icon;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class MsgDiffStart extends BasicEdgesManipulator {

    MsgDiff msgDiff;
    
    @Override
    public void setup(Edge[] edges, Edge clickedEdge) {
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        msgDiff=DiffContainer.getMsgDiff(graphModel);
    }

    @Override
    public void execute() {
        msgDiff.start();
    }

    @Override
    public String getName() {
        return "开始消息传播路径动态演示";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean canExecute() {
        GraphController gc=Lookup.getDefault().lookup(GraphController.class);
        if(gc!=null&&gc.getGraphModel()!=null&&gc.getGraphModel().getGraph().getEdgeCount()>0){
            String graphDes=(String) gc.getGraphModel().getGraph().getAttribute("GraphDes");
            
            return graphDes.contains("消息传播")&&msgDiff.isRunning()==false;
        }else{
            return false;
        }
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("edu/seu/datalab/plugin/images/run.png", false);
    }
    
}
