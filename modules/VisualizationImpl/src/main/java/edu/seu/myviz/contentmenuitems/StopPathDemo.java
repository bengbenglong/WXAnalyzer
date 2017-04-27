/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.myviz.msgDiffDemo.DiffContainer;
import edu.seu.myviz.msgDiffDemo.MsgDiff;
import edu.seu.myviz.msgDiffDemo.SpanningPathDiff;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=GraphContextMenuItem.class)
public class StopPathDemo extends BasicItem{

    SpanningPathDiff pathDiff;
    MsgDiff msgDiff;
    
    @Override
    public void setup(Graph graph, Node[] nodes) {
       GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
       if(graphModel!=null){
            String graphDes=(String) graphModel.getGraph().getAttribute("GraphDes");
            if(graphDes.contains("群组间消息传播网")){
                msgDiff=DiffContainer.getMsgDiff(graphModel);
            }else if(graphDes.contains("最短路径")){
                pathDiff=DiffContainer.getPathDiff(graphModel);
            }
       }
    }
    
    @Override
    public void execute() {
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        String graphDes=(String) graphModel.getGraph().getAttribute("GraphDes");
        if(graphDes.contains("群组间消息传播网")){
            msgDiff.stop();
        }else if(graphDes.contains("最短路径")){
            pathDiff.stop();
        }
    }

    @Override
    public String getName() {
        return "取消消息传播路径动态演示";
    }

    @Override
    public boolean canExecute() {
        GraphController gc=Lookup.getDefault().lookup(GraphController.class);
        if(gc!=null&&gc.getGraphModel()!=null&&gc.getGraphModel().getGraph().getEdgeCount()>0){
            String graphDes=(String) gc.getGraphModel().getGraph().getAttribute("GraphDes");
//            JOptionPane.showMessageDialog(null, "graphDes:"+graphDes+"\tpathDiff:"+pathDiff==null);
//            return (graphDes.contains("群组间消息传播网")||graphDes.contains("最小生成树"))&&((pathDiff!=null&&pathDiff.isRunning())||(msgDiff!=null||msgDiff.isRunning()));
            return graphDes.contains("群组间消息传播网")&&msgDiff!=null&&msgDiff.isRunning();
        }else{
            return false;
        }
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 200;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("edu/seu/viz/image/stop.png", false);
    }
    
}
