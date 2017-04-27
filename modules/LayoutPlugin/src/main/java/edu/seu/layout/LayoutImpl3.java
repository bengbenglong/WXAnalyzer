/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;
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
public class LayoutImpl3 {
    
    private volatile boolean isRunning=false;
    private volatile boolean isCancelled=false;
    private GraphModel graphModel;
    
    public void cancel(boolean flag){
        this.isCancelled=flag;
    }
    
    public LayoutImpl3(GraphModel graphModel){
        this.graphModel=graphModel;
    }
    
    public boolean isRunning(){
        return isRunning;
    }
    
    
    public void forceAtlasLayout(){
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
        isRunning=true;
        for (int i = 0; i < 30000 && layout.canAlgo() && isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
        isRunning=false;
        isCancelled=false;//恢复这个标志位，为下一次布局做准备
    }
    
    public  void forceAtlasLayout2(){
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
        isRunning=true;
        for (int i = 0; i < 30000 && layout.canAlgo() && isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
        isRunning=false;
    }
    
    public void autoLayout(){
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", 500., 0f);//500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        isRunning=true;
        autoLayout.execute();
        isRunning=false;
    }
    
    public void yifuLayout(){
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
        isRunning=true;
        for (int i = 0; i < 30000 && layout.canAlgo() && isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
        isRunning=false;
    }
    
    public void yifuLayout2(){
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(500f);
        
        layout.initAlgo();
        isRunning=true;
        for (int i = 0; i < 30000 && layout.canAlgo() &&isCancelled==false; i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
        isRunning=false;
    }
}
