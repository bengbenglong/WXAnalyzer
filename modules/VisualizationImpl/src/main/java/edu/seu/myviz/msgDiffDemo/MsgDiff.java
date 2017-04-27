/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.msgDiffDemo;

import java.awt.Color;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.SwingWorker;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class MsgDiff {
    private volatile boolean running=false;
    private volatile boolean cancel=false;
    
    public boolean isRunning(){
        return running;
    }
    
    public void start(){
        GraphController graphController=Lookup.getDefault().lookup(GraphController.class);
        Iterator<Edge> ite=graphController.getGraphModel().getGraph().getEdges().iterator();
        final TreeMap<String,Edge> map=new TreeMap<>();
        while(ite.hasNext()){
            Edge edge=ite.next();
            String msgTime=(String) edge.getAttribute("发送时间");
            map.put(msgTime, edge);
            edge.setColor(Color.WHITE);
        }
        
        //开始动画演示消息传播路径
        new SwingWorker<Void,Void>(){
            ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
            ProgressTicket ticket ;
            @Override
            protected Void doInBackground() throws Exception {
                
                ticket=progressProvider.createTicket("消息传播行为演示中...", new Cancellable(){
                    @Override
                    public boolean cancel() {
                        for(String s:map.keySet()){
                            map.get(s).setColor(Color.DARK_GRAY);
                        }
                        cancel=true;
                        return true;
                    }
                });
                ticket.start();
                running=true;
                cancel=false;
                
                for(String s:map.keySet()){
                    Thread.sleep(800);
                    if(cancel==false){
                        map.get(s).setColor(Color.DARK_GRAY);
                    }else{
                        break;
                    }
                }
                running=false;
                ticket.finish();
                return null;
            }
            
            @Override
            protected void done(){
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        for(String s:map.keySet()){
                            map.get(s).setColor(Color.DARK_GRAY);
                        }
                    }
                }).start();
                if(ticket!=null){
                    ticket.finish();
                }
            }
        }.execute();
    }
    
    public void stop(){
        cancel=true;
    }
    
    
}
