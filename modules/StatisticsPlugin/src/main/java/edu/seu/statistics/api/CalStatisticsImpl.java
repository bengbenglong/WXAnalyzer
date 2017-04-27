/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.statistics.api;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.gephi.graph.api.Graph;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=CalStatistics.class)
public class CalStatisticsImpl implements CalStatistics{

    @Override
    public void execute() {
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        Workspace w=pc.getCurrentWorkspace();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel(w);
        Graph graph=graphModel.getGraph();
        
        
        new EigenvectorCentrality().execute(graphModel.getGraph());
        new Degree().execute(graphModel.getGraph());
        new GraphDistance().execute(graphModel.getGraph());
    }
    
}
