/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
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
public class CalClusterMenu extends BasicItem{

    @Override
    public void execute() {
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() {
                try{
                    ClustererBuilder clustererBuilder=Lookup.getDefault().lookup(ClustererBuilder.class);
                    Clusterer clusterer=clustererBuilder.getClusterer();
                    GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                    ProgressTicket ticket = null;
                    if (progressProvider != null) {
                        ticket = progressProvider.createTicket("划分社团", null);
                    }
//                    Progress.start(ticket);
                    clusterer.myExecute(graphModel, ticket);
//                    clusterer.execute(graphModel);
//                    Progress.finish(ticket);
                }catch(Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "划分社团时出错");
                }
                return null;
            }
        }.execute();
    }

    @Override
    public String getName() {
        
        return "划分社团";
    }

    @Override
    public boolean canExecute() {
        
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        GraphController gc=Lookup.getDefault().lookup(GraphController.class);
        if(pc!=null&&gc!=null){
            return gc.getGraphModel()!=null;
        }else{
            return false;
        }
    }

    @Override
    public int getType() {
        return 300;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("edu/seu/viz/image/cluster.png", false);
    }
    
}
