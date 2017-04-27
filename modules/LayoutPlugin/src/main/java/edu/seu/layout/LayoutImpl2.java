/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class LayoutImpl2 {
    private static LayoutImpl2 instance=null;
    private static Map<GraphModel,LayoutImpl2> map=new HashMap<>();
    
    private volatile boolean isCancelled=false;
    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
    ProgressTicket ticket = progressProvider.createTicket("", null);
    LayoutImpl2 currentLayout;
    
    public void cancel(boolean flag){
        this.isCancelled=flag;
    }
    
    private LayoutImpl2(){
        
    }
    
    public synchronized static LayoutImpl2 getInstance(){
        if(instance==null){
            instance=new LayoutImpl2();
        }
        return instance;
    }
    
    public LayoutImpl2 getLayout(GraphModel graphModel){
        currentLayout=map.get(graphModel);
        //
        if(currentLayout==null){
            currentLayout=new LayoutImpl2();
            map.put(graphModel, currentLayout);
            return currentLayout;
        }else{
            return currentLayout;
        }
    }
    
    public void start(GraphModel graphModel){
        ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
            @Override
            public boolean cancel() {
                currentLayout.cancel(true);
                return true;
            }
        });
        ticket.start();
        currentLayout.forceAtlasLayout(graphModel);
        ticket.finish();
    }
    
    public void stop(){
        ticket.finish();
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
