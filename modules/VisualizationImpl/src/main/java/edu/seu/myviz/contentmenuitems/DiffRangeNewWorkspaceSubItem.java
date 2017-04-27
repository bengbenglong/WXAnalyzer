/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.networkbuild.api.BuildDiffPaths;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
//@ServiceProvider(service= GraphContextMenuItem.class )
public class DiffRangeNewWorkspaceSubItem extends BasicItem{

    @Override
    public void execute() {
        if(nodes.length>0){
            final Node firstNode=nodes[0];
            new SwingWorker<Void,Void>(){
                @Override
                protected Void doInBackground() throws Exception {
                    BuildDiffPaths buildDiffPaths=Lookup.getDefault().lookup(BuildDiffPaths.class);
                    buildDiffPaths.buildOnNewWorkspace((String)firstNode.getId());
                    //然后再定位到图中心
                    ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
                    Node n=graph.getNode(firstNode.getId());
                    if(n!=null){
                        String name=(String) n.getAttribute("GROUPNAME");
                        if(name!=null){
                            pc.renameWorkspace(pc.getCurrentWorkspace(), "节点“"+name+"”的消息传播范围");
                        }
                    }else{
                        pc.renameWorkspace(pc.getCurrentWorkspace(), "节点“"+firstNode.getId()+"”的消息传播范围");
                    }
                    VizController.getInstance().getGraphIO().centerOnGraph();
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public String getName() {
        return "计算传播范围（在新面板中）";
    }

    @Override
    public boolean canExecute() {
        return nodes.length>0;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
    
    
}
