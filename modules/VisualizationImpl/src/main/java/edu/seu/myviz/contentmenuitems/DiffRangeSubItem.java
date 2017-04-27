/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.networkbuild.api.BuildDiffPaths;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.SwingWorker;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class DiffRangeSubItem extends BasicItem{

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes=nodes;
    }
    
    @Override
    public void execute() {
        resetColor();
        if(nodes.length>0){
            final Node firstNode=nodes[0];
            new SwingWorker<Void,Void>(){
                @Override
                protected Void doInBackground() throws Exception {
                    BuildDiffPaths buildDiffPaths=Lookup.getDefault().lookup(BuildDiffPaths.class);
                    buildDiffPaths.buildOnOriginal((String) firstNode.getId());
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public String getName() {
        return "计算传播范围";
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
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
    private void resetColor(){
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()!=null){
            GraphController gc = Lookup.getDefault().lookup(GraphController.class);
            GraphModel gm = gc.getGraphModel();
            Graph graph = gm.getGraphVisible();
            for (Node n : graph.getNodes()) {
                String color=(String)n.getAttribute("defaultColor");
                switch(color){
                    case("红色"):
                        n.setColor(Color.RED);
                        break;
                    case("蓝色"):
                        n.setColor(new Color(0,150,255));
                        break;
                    case("紫色"):
                        n.setColor(new Color(204,0,153));
                        break;
                    case("橙色"):
                        n.setColor(new Color(255,100,0));
                        break;
                    case("淡黄"):
                        n.setColor(new Color(255,204,51));
                        break;

                    default:n.setColor(Color.GRAY);
                }

            }
        }
    }
    
}
