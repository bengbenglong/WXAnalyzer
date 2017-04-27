package edu.seu.networkbuild.impl;

import edu.seu.layout.LayoutImpl;
import edu.seu.networkbuild.api.BuildDiffPaths;
import edu.seu.networkbuild.diffusion.DiffusionNet;
import edu.seu.networkbuild.util.GraphManipulateUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=BuildDiffPaths.class)
public class DiffusionPathBuilder implements BuildDiffPaths{
    private Color blueColor=new Color(0,150,255);
    
    private DiffusionNet diffGraph;
    
    private ProjectController pc;
    private Workspace workspace;
    private GraphController graphCtrl;
    private GraphModel graphModel;
    private Graph graph;
    
    //网络中消息传播最快和最慢时间，用来确定每个节点大小
    //节点大小与消息传播时间成反比，源节点最大，其他节点都按时间比例比源节点小
    private float minTime=Integer.MAX_VALUE;
    private float maxTime=Integer.MIN_VALUE;
    
    public DiffusionPathBuilder(){
        diffGraph=DiffusionNet.getInstance();
    }
    
    @Override
    public void buildOnOriginal(String startVertex) {
        this.diffRange(startVertex);
    }

    @Override
    public void buildOnNewWorkspace(String startVertex) {
        this.diffRangeOnNewWorkspace(startVertex);
    }

    @Override
    public void calMinSpanningTree(String startVertex) {
        this.getMinSpaningTree(startVertex);
    }
    
    
    private void getMinSpaningTree(String startVertex){
        DiffusionNet gg=DiffusionNet.getInstance();
        Map<String,String> reachedMap=gg.runDiffusionPath(startVertex, 500);
        Set<String> reachedVertexSet=reachedMap.keySet();
        Map<String,Map<String,Integer>> map=gg.getImportEdgeMap();

        pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }
        workspace=pc.getCurrentWorkspace();
        graphModel=workspace.getLookup().lookup(GraphModel.class);
        graph=graphModel.getGraph();
//        pc.renameWorkspace(workspace, "节点"+startVertex+"的传播路径最小生成树");
//        pc.openWorkspace(workspace);
        

        Set<Node> nodeSet=new HashSet<>();
        nodeSet.add(graph.getNode(startVertex));//将源节点加入
        for(String s:map.keySet()){
            if(reachedVertexSet.contains(s)){
                nodeSet.add(graph.getNode(s));
            }
        }

        this.findDiffPath(startVertex, graphModel, reachedMap, nodeSet);
    }
    
    private void diffRange(String startVertex){
        DiffusionNet gg=DiffusionNet.getInstance();
        Map<String,String> reachedMap=gg.runDiffusionPath(startVertex, 500);
        Set<String> reachedVertexSet=reachedMap.keySet();
        Map<String,Map<String,Integer>> importEdgeMap=gg.getImportEdgeMap();

        //
        pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }
        workspace=pc.getCurrentWorkspace();
        graphModel=workspace.getLookup().lookup(GraphModel.class);
        graph=graphModel.getGraph();
        
        
        Column column=initAttributeColunms(graphModel,"消息传播到达时间",Double.class);
        //清除之前计算的消息传播到达时间值
        Iterator<Node> ite=graph.getNodes().iterator();
        while(ite.hasNext()){
            ite.next().setAttribute(column, (double)0.0);
        }
        //开始计算消息传播的到达时间
        for(String s:importEdgeMap.keySet()){
            if(reachedVertexSet.contains(s)){
                this.setColour(s, new Color(255,204,51),"淡黄");
                String attrValue=reachedMap.get(s);
                graph.getNode(s).setAttribute(column,(double)Double.parseDouble(attrValue));
            }
        }
        this.setColour(startVertex, new Color(255,100,0),"橙色");
    }
    
    private GraphModel diffRangeOnNewWorkspace(String startVertex){
        DiffusionNet gg=DiffusionNet.getInstance();
        Map<String,String> reachedMap=gg.runDiffusionPath(startVertex, 500);
        Set<String> reachedVertexSet=reachedMap.keySet();
        Map<String,Map<String,Integer>> map=gg.getImportEdgeMap();

        pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()==null){
            pc.newProject();
        }
        workspace=pc.getCurrentWorkspace();
        graphModel=workspace.getLookup().lookup(GraphModel.class);
        graph=graphModel.getGraph();

        Set<Node> nodeSet=new HashSet<>();
        nodeSet.add(graph.getNode(startVertex));//将源节点加入
        for(String s:map.keySet()){
            if(reachedVertexSet.contains(s)){
                nodeSet.add(graph.getNode(s));
            }
        }

        //新建一个workspace并且将计算的到的节点以及这些节点的参数深度复制到新建的workspace中
        Workspace newWorkspace=pc.newWorkspace(pc.getCurrentProject());
        GraphModel newGraphModel=newWorkspace.getLookup().lookup(GraphModel.class);
        newGraphModel.setConfiguration(graphModel.getConfiguration());
        newGraphModel.setTimeFormat(graphModel.getTimeFormat());
        newGraphModel.setTimeZone(graphModel.getTimeZone());
        pc.openWorkspace(newWorkspace);
        //开始复制
        try{
            newGraphModel.bridge().copyNodes(nodeSet.toArray(new Node[0]));
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "该节点无法生成最短路径");
        }
        //在新的图中设置nodeAttr
        Column column=initAttributeColunms(newGraphModel,"消息传播到达时间",Double.class);
        initAttributeColunms(newGraphModel,"defaultColor",String.class);
        newGraphModel.getGraph().setAttribute("GraphDes", "源节点“"+startVertex+"”的消息传播范围网络");
        //清除之前计算的消息传播到达时间值
        Iterator<Node> ite=newGraphModel.getGraph().getNodes().iterator();
        while(ite.hasNext()){
            Node n=ite.next();
            n.setColor(new Color(255,204,51));
            n.setAttribute("defaultColor", "淡黄");
            String weight=reachedMap.get(n.getId());
            n.setAttribute(column, Double.parseDouble(weight));
            
            //计算最大最小时间
            float time=Float.parseFloat(reachedMap.get(n.getId()));
            if(time>0.0&&time<minTime){
                minTime=time;
            }
            if(time>0.0&&time>maxTime){
                maxTime=time;
            }
        }
        newGraphModel.getGraph().getNode(startVertex).setColor(new Color(255,100,0));
        newGraphModel.getGraph().getNode(startVertex).setAttribute("defaultColor", "橙色");
        
        //根据传播时间来重新设置节点大小
        this.setNodeSize(newGraphModel.getGraph(), newGraphModel.getGraph().getNode(startVertex));
        
        return newGraphModel;
    }
    
    private Column initAttributeColunms(GraphModel graphModel,String attr,Class type) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(attr);
        if (eigenCol == null) {
            eigenCol=nodeTable.addColumn(attr, type);
        }
        
        return eigenCol;
    }
    
    private void setColour(String node,Color color,String colorName){
        
        if(graph.getNode(node)==null){
            throw new NullPointerException();
        }else{
            graph.getNode(node).setColor(color);
        }
    }
    
    private void convertMapToGraph(String startVertex, String targetVertex, double weight){
        
        if(graph.getNode(startVertex)==null){
            Node n=graphModel.factory().newNode(startVertex);
            n.setLabel(startVertex);
            graph.addNode(n);
        }
        if(graph.getNode(targetVertex)==null){
            Node n=graphModel.factory().newNode(targetVertex);
            n.setLabel(targetVertex);
            graph.addNode(n);
        }
        Node startN=graph.getNode(startVertex);
        Node targetN=graph.getNode(targetVertex);
        Edge edge=graphModel.factory().newEdge(startN, targetN, 0, weight, true);
        if(!graph.contains(edge)){
            graph.addEdge(edge);
        }
        GraphManipulateUtils.setNodeAttributes(graph);
    }
    
    //用来寻找消息扩散路径的。思想如下：
    //先找到目标节点的一阶好友，将所有一阶好友加入到扩散路径中，剩余的节点放入otherSet中，然后去otherSet中的某个节点，
    //找到已经存在与传播路径中，并且和该节点有之间有边的连接的节点列表，将其中消息传播时间最短的已存在节点作为该节点的源节点
    private void findDiffPath(String startVertex,GraphModel originalGraphModel,Map<String,String> reachedMap,Set<Node> nodesSet ){
        try{
            Set<Node> addedNodes=new HashSet<>();
            List<Node> otherNodes=new ArrayList<>();
            Set<Edge> addedEdges=new HashSet<>();

            DirectedGraph dgraph=originalGraphModel.getDirectedGraph();
            Node startNode=dgraph.getNode(startVertex);
            Iterator<Node> ite=dgraph.getSuccessors(startNode).iterator();
            otherNodes.addAll(nodesSet);
            otherNodes.remove(startNode);
            while(ite.hasNext()){
                Node node=ite.next();
                if(nodesSet.contains(node)){
                    addedNodes.add(node);
                    Edge edge=dgraph.getEdge(startNode,node);
                    addedEdges.add(edge);
                    otherNodes.remove(node);
                }
            }

            while(otherNodes.isEmpty()==false){
                Node currNode=otherNodes.remove(otherNodes.size()-1);
                double currNodeTime=Double.parseDouble(reachedMap.get(currNode.getId()));
                Node[] preNodes=dgraph.getPredecessors(currNode).toArray();

                double diffTime=0;
                Node preNode=null;
                for(Node n:preNodes){
                    //如果该节点的前驱节点是已经添加了的节点
                    if(addedNodes.contains(n)){
                        double preNodeTime=Double.parseDouble(reachedMap.get(n.getId()));
                        if(preNodeTime<currNodeTime && currNodeTime-preNodeTime>diffTime){
                            diffTime=currNodeTime-preNodeTime;
                            preNode=n;
                        }
                    }
                }
                if(preNode!=null){
                    addedNodes.add(currNode);
                    Edge edge=dgraph.getEdge(preNode, currNode);
                    addedEdges.add(edge);
                }
            }
            
            //====接下来的工作是为了安全的建立一个新的workspace以及graph（通过复制的会有问题，api实现有问题）======
            //新建workspace,并将之前的节点的某些属性复制到新建的节点上
            Workspace newWorkspace=pc.newWorkspace(pc.getCurrentProject());
            final GraphModel newGraphModel=newWorkspace.getLookup().lookup(GraphModel.class);
            Graph newGraph=newGraphModel.getGraph();
            pc.openWorkspace(newWorkspace);
            

            Column column=this.initAttributeColunms(newGraphModel, "消息传播到达时间",Double.class);
            initAttributeColunms(newGraphModel,"defaultColor",String.class);
                       
            for(Edge e:addedEdges){
                
                if(newGraph.getNode(e.getSource().getId())==null){
                    Node srcNode=newGraphModel.factory().newNode(e.getSource().getId());
                    //添加原来节点的属性
                    srcNode.setColor(new Color(255,204,51));
                    srcNode.setAttribute("defaultColor", "浅黄");
//                    srcNode.setSize(e.getSource().size());
                    srcNode.setPosition(e.getSource().x(), e.getSource().y());
                    srcNode.setLayoutData(e.getSource().getLayoutData());
                    
                    String[] attrKeys=e.getSource().getAttributeKeys().toArray(new String[0]);
                    Set<String> userfulKey=new HashSet<>();
                    userfulKey.add("离心率");
                    userfulKey.add("度");
                    userfulKey.add("label");
                    userfulKey.add("群id");
                    userfulKey.add("入度");
                    userfulKey.add("介数");
                    userfulKey.add("接近度");
                    userfulKey.add("特征向量");
                    userfulKey.add("出度");
                    userfulKey.add("id");
                    for(int i=0;i<attrKeys.length;i++){
                        if(!userfulKey.contains(attrKeys[i])){
                            Column newcolumn=initAttributeColunms(newGraphModel,attrKeys[i], String.class);
                            srcNode.setAttribute(newcolumn, e.getSource().getAttribute(attrKeys[i]));
                        }
                    }
                    //将最新计算得到的消息传播时间参数替换之前计算得到的
                    srcNode.setAttribute(column, Double.parseDouble(reachedMap.get(e.getSource().getId())));
                    //计算最大最小时间
                    float time=Float.parseFloat(reachedMap.get(e.getSource().getId()));
                    if(time>0.0&&time<minTime){
                        minTime=time;
                    }
                    if(time>0.0&&time>maxTime){
                        maxTime=time;
                    }
                    
                    newGraph.addNode(srcNode);
                }
                if(newGraph.getNode(e.getTarget().getId())==null){
                    Node tarNode=newGraphModel.factory().newNode(e.getTarget().getId());
                    //添加原来节点的属性
                    tarNode.setColor(new Color(255,204,51));
                    tarNode.setAttribute("defaultColor", "浅黄");
//                    tarNode.setSize(e.getTarget().size());
                    tarNode.setPosition(e.getTarget().x(), e.getTarget().y());
                    tarNode.setLayoutData(e.getTarget().getLayoutData());
                    
                    String[] attrKeys=e.getTarget().getAttributeKeys().toArray(new String[0]);
                    Set<String> userfulKey=new HashSet<>();
                    userfulKey.add("离心率");
                    userfulKey.add("度");
                    userfulKey.add("label");
                    userfulKey.add("群id");
                    userfulKey.add("入度");
                    userfulKey.add("介数");
                    userfulKey.add("接近度");
                    userfulKey.add("特征向量");
                    userfulKey.add("出度");
                    userfulKey.add("id");
                    for(int i=0;i<attrKeys.length;i++){
                        if(!userfulKey.contains(attrKeys[i])){
                            Column newcolumn=initAttributeColunms(newGraphModel,attrKeys[i],String.class);
                            tarNode.setAttribute(newcolumn, e.getTarget().getAttribute(attrKeys[i]));
                        }
                    }
                    //将最近计算得到的消息传播时间参数替换之前计算得到的
                    tarNode.setAttribute(column, Double.parseDouble(reachedMap.get(e.getTarget().getId())));
                    //计算最大最小时间
                    float time=Float.parseFloat(reachedMap.get(e.getTarget().getId()));
                    if(time>0.0&&time<minTime){
                        minTime=time;
                    }
                    if(time>0.0&&time>maxTime){
                        maxTime=time;
                    }
                    
                    newGraph.addNode(tarNode);
                }
                Edge newEdge=newGraphModel.factory().newEdge( newGraph.getNode( e.getSource().getId() ), newGraph.getNode( e.getTarget().getId() ) );
                newGraph.addEdge(newEdge);
            }
            
            //将节点重新设置颜色，前面虽然设置了颜色，但是好像不起作用，不知道什么原因。
            Iterator<Node> nodeIte=newGraph.getNodes().iterator();
            while(nodeIte.hasNext()){
                Node n=nodeIte.next();
                n.setColor(new Color(255,204,51));
                n.setAttribute("defaultColor", "浅黄");
            }
            newGraph.getNode(startVertex).setColor(new Color(255,100,0));
            newGraph.getNode(startVertex).setAttribute("defaultColor", "橙色");
            
            newGraph.setAttribute("GraphDes", "消息传播概率路径的最短路径网络   源节点：“"+startVertex+"”");
            newGraph.setAttribute("FirstNodeDes", "消息源");
            newGraph.setAttribute("FirstNodeColor", "橙色");
            newGraph.setAttribute("SecondNodeDes", "扩散节点");
            newGraph.setAttribute("SecondNodeColor", "浅黄");
            
            
            //根据到达时间来分配节点大小
            this.setNodeSize(newGraph, newGraph.getNode(startVertex));
            
//            //进行布局
//            new Thread(new Runnable(){
//                @Override
//                public void run() {
//                    try{
//                        executeLayout(newGraphModel);
//                    }catch(Exception e){
//                        e.printStackTrace();
//                        JOptionPane.showMessageDialog(null, "布局过程出现错误");
//                    }
//                }
//            }).start();
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "所选节点无法构造最短路径！");
        }
    }
    
    private void executeLayout(GraphModel graphModel){
        
        LayoutImpl layout=new LayoutImpl();
        layout.forceAtlasLayout(graphModel);
    }
    
    
    //根据传播时间快慢来设置节点大小
    private void setNodeSize(Graph graph,Node startNode){
        Iterator<Node> ite=graph.getNodes().iterator();
        float len=maxTime-minTime;
        startNode.setSize(100f);
        while(ite.hasNext()){
            Node n=ite.next();
            if(n!=startNode){
                float time=Float.parseFloat(String.valueOf(n.getAttribute("消息传播到达时间")));
                float size=(maxTime-time)/len*100;
                if(size<=20){
                    size=20;
                }
                n.setSize(size);
            }
        }
        
    }

}
