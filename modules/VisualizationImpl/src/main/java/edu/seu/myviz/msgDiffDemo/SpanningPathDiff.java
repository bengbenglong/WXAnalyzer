/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.msgDiffDemo;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class SpanningPathDiff {
    private volatile boolean running=false;
    private volatile boolean cancel=false;
    
    public boolean isRunning(){
        return running;
    }
    
    public void start(){
        GraphController gc=Lookup.getDefault().lookup(GraphController.class);
        final Graph graph=gc.getGraphModel().getGraph();
        Iterator<Node> ite=graph.getNodes().iterator();
        final TreeMap<Double,Node> map=new TreeMap<>();
        while(ite.hasNext()){
            Node n=ite.next();
            double reachedTime=(Double) n.getAttribute("消息传播到达时间");
            map.put(reachedTime, n);
        }
        
        Iterator<Edge> edgeIte=graph.getEdges().iterator();
        while(edgeIte.hasNext()){
            edgeIte.next().setColor(Color.WHITE);
        }
        
        
        //开始动画演示消息传播路径
        new SwingWorker<Void,Void>(){
         
            ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
            ProgressTicket ticket ;
            
            
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    
                    ticket=progressProvider.createTicket("消息传播行为演示中...", new Cancellable(){
                        @Override
                        public boolean cancel() {
                            Iterator<Edge> ite=graph.getEdges().iterator();
                            while(ite.hasNext()){
                                ite.next().setColor(Color.DARK_GRAY);
                            }
                            return true;
                        }
                    });
                    ticket.start();
                    running=true;
                    cancel=false;


                    for(double d:map.keySet()){
                        Node n=map.get(d);
                        if(cancel==false&&graph.getNeighbors(n)!=null){
                            Iterator<Node> neighbors=graph.getNeighbors(n).iterator();
                            List<Edge> list=new ArrayList<>();
                            while(neighbors!=null&&neighbors.hasNext()){
                                list.add(graph.getEdge(n,neighbors.next()));
                                
                            }
                            for(int i=0;i<list.size();i++){
                                if(cancel==false){
                                    Thread.sleep(800);
                                    if(list.get(i)!=null){
                                        list.get(i).setColor(Color.DARK_GRAY);
                                    }
                                }else{
                                    break;
                                }
                            }
                        }
                    }
                    running=false;
                    ticket.finish();
                }catch(Exception ex){
                }
                
                return null;
            }
            
            @Override
            protected void done(){
                
                try{
                    if(ticket!=null){
                        ticket.finish();
                    }
                //    
                    
                new Thread(new Runnable(){
                        @Override
                        public void run() {
                           Iterator<Edge> edgeIte=graph.getEdges().iterator();
                            while(edgeIte.hasNext()){
                                edgeIte.next().setColor(Color.WHITE);
                            }
                        }
                    }).start();
                }catch(Exception ex){
                }
                
            }
        }.execute();
        
        
    }
    
    public void stop(){
        cancel=true;
    }
}
