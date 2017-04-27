/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.layout;

import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingWorker;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class MyLayoutController {
    private static Map<GraphModel,LayoutImpl3> map=new HashMap<>();
    private static Map<LayoutImpl3,ProgressTicket> ticketMap=new HashMap<>();
    private ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
    private ProgressTicket ticket;
    private final LayoutImpl3 layout;
    
    public static LayoutImpl3 getLayout(GraphModel graphModel){
        if(map.get(graphModel)==null){
            LayoutImpl3 layout=new LayoutImpl3(graphModel);
            map.put(graphModel, layout);
            return layout;
        }else{
            return map.get(graphModel);
        }
    }
    
    public MyLayoutController(LayoutImpl3 layout){
        this.layout=layout;
        if(ticket==null){
           if(ticketMap.get(layout)==null){
               ticket=progressProvider.createTicket("", null);
               ticketMap.put(layout, ticket);
           }else{
               ticket=ticketMap.get(layout);
           }
       }
    }
    
    public void startLayout(){
        Graph graph=Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        final String graphDes=(String)graph.getAttribute("GraphDes");
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                ticket=progressProvider.createTicket(graphDes+"节点布局中...", new Cancellable(){
                    @Override
                    public boolean cancel() {
                        layout.cancel(true);
                        return true;
                    }
                });
                ticket.start();
                layout.forceAtlasLayout();
                ticket.finish();
                return null;
            }

            @Override
            protected void done(){
                ticket.finish();
            }
        }.execute();
    }
    
    public void stopLayout(){
        layout.cancel(true);
        ticket.finish();
    }
    
}
