/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.spi.LayoutBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class LayoutImpl {
    
    private volatile boolean isCancelled=false;
    
    public void cancel(boolean flag){
        this.isCancelled=flag;
    }
    
    
    public void forceAtlasLayout(GraphModel graphModel){
        LayoutController layoutCtrl=Lookup.getDefault().lookup(LayoutController.class);
        LayoutModel layoutModel=layoutCtrl.getModel();
        org.gephi.layout.spi.Layout layout=null;
        List<LayoutBuilder> builders = new ArrayList<>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        
        for(int i=0; i<builders.size(); i++){
            if(builders.get(i).getName().equals("ForceAtlas"))
                layout=layoutModel.getLayout(builders.get(i));
        }
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layoutCtrl.setLayout(layout);
        
        layout.initAlgo();
        for (int i = 0; i < 30000 && layout.canAlgo() && isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
    
    public  void forceAtlasLayout2(GraphModel graphModel){
        LayoutController layoutCtrl=Lookup.getDefault().lookup(LayoutController.class);
        LayoutModel layoutModel=layoutCtrl.getModel();
        org.gephi.layout.spi.Layout layout=null;
        List<LayoutBuilder> builders = new ArrayList<>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        
        for(int i=0; i<builders.size(); i++){
            if(builders.get(i).getName().equals("ForceAtlas 2"))
                layout=layoutModel.getLayout(builders.get(i));
        }
        
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        
        layout.initAlgo();
        for (int i = 0; i < 30000 && layout.canAlgo() && isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
    
    public void autoLayout(GraphModel graphModel){
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", 500., 0f);//500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();
    }
    
    public void yifuLayout(GraphModel graphModel){
        LayoutController layoutCtrl=Lookup.getDefault().lookup(LayoutController.class);
        LayoutModel layoutModel=layoutCtrl.getModel();
        org.gephi.layout.spi.Layout layout=null;
        List<LayoutBuilder> builders = new ArrayList<>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        
        for(int i=0; i<builders.size(); i++){
            if(builders.get(i).getName().equals("Yifan Hu"))
                layout=layoutModel.getLayout(builders.get(i));
        }
        
//        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        
        layout.initAlgo();
        for (int i = 0; i < 30000 && layout.canAlgo() && isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
    
    public void yifuLayout2(GraphModel graphModel){
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(500f);
        
        layout.initAlgo();
        for (int i = 0; i < 30000 && layout.canAlgo() &&isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
}
