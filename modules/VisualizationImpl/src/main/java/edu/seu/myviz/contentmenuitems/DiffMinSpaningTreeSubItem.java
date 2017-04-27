/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.layout.LayoutImpl;
import edu.seu.layout.LayoutImpl3;
import edu.seu.layout.MyLayoutController;
import edu.seu.networkbuild.api.BuildDiffPaths;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
//@ServiceProvider(service=BasicItem.class)
public class DiffMinSpaningTreeSubItem extends BasicItem{

    @Override
    public void execute() {
        if(nodes.length>0){
            final String nodeName=(String)nodes[0].getId();
            new SwingWorker<Void,Void>(){
                ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                ProgressTicket ticket;
                
                @Override
                protected Void doInBackground() throws Exception {
                    try{
                        BuildDiffPaths buildDiffPaths=Lookup.getDefault().lookup(BuildDiffPaths.class);
                        buildDiffPaths.calMinSpanningTree(nodeName);
                        
                        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
                        Node n=graph.getNode(nodeName);
                        if(n!=null){
                            String name=(String) n.getAttribute("GROUPNAME");
                            if(name!=null){
                                pc.renameWorkspace(pc.getCurrentWorkspace(), "节点“"+name+"”的消息传播路径最短路径");
                            }
                        }else{
                            pc.renameWorkspace(pc.getCurrentWorkspace(), "节点“"+nodeName+"”的消息传播路径最短路径");
                        }
                        //定位到图中心
                        VizController.getInstance().getGraphIO().centerOnGraph();
//                        //对该网络进行节点布局
//                        final LayoutImpl layout=new LayoutImpl();
//                        ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
//                            @Override
//                            public boolean cancel() {
//                                layout.cancel(true);
//                                return true;
//                            }
//                        });
//                        ticket.start();
//                        //建好之后布局
//                        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
//                        layout.forceAtlasLayout(graphModel);
//                        ticket.finish();
                        //进行节点布局
                        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                        final LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
                        ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
                            @Override
                            public boolean cancel() {
                                layout.cancel(true);
                                return true;
                            }
                        });
                        ticket.start();
                        MyLayoutController layoutContainer=new MyLayoutController(layout);
                        if(graphModel==null||layout==null){

                        }else if(layout.isRunning()){
                            layoutContainer.stopLayout();
                        }else{
                            layoutContainer.startLayout();
                        }
                        ticket.finish();
                    }catch(Exception e){
                        e.printStackTrace();
                        if(ticket!=null){
                            ticket.finish();
                        }
                        JOptionPane.showMessageDialog(null, "出错了");
                    }
                    return null;
                }
                
                @Override
                protected void done(){
                    if(ticket!=null){
                        ticket.finish();
                    }
                }
            }.execute();
        }
    }

    @Override
    public String getName() {
        return "消息传播最短路径";
    }

    @Override
    public boolean canExecute() {
        return nodes.length>0;
    }

    @Override
    public int getType() {
        return 100;
    }
    

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
}
