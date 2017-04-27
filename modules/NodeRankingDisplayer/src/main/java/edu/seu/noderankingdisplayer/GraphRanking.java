/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.noderankingdisplayer;

import javax.swing.JOptionPane;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.Degree;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class GraphRanking {
    
   public void getGraphRanking(){
       ProjectController projectCtrl=Lookup.getDefault().lookup(ProjectController.class);
       if(projectCtrl.getCurrentProject()==null){
           JOptionPane.showMessageDialog(null, "没有项目打开");
       }
       GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
       
       AppearanceController appCtrl=Lookup.getDefault().lookup(AppearanceController.class);
       AppearanceModel appModel=appCtrl.getModel();
       
       Graph graph=graphModel.getGraph();
       
       //ranking by degree
       Degree degree=new Degree();
       degree.execute(graph);
       
   }
    
    
    
}
