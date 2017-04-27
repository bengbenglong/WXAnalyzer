/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.layout.LayoutImpl3;
import edu.seu.layout.MyLayoutController;
import javax.swing.Icon;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
//@ServiceProvider(service=GraphContextMenuItem.class)
public class StartLayoutMenu extends BasicItem{

    @Override
    public void execute() {
    GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
        final MyLayoutController layoutContainer=new MyLayoutController(layout);
            layoutContainer.startLayout();
        }
        
    @Override
    public String getName() {
            return "开始节点布局";
        }

    @Override
    public boolean canExecute() {
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        if(graphModel==null||graphModel.getGraph().getNodeCount()==0){
            return false;
        }
        LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
        return !layout.isRunning();//注意前面有个！符号
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("edu/seu/viz/image/layoutRun.png", false);
    }
    
}
