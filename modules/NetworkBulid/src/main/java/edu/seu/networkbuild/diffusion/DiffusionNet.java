/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.diffusion;

import edu.seu.networkbuild.drivers.DiffusionDataFetcher;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import edu.seu.networkbuild.util.NetBuildMethods;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class DiffusionNet {
    //单例
    private static DiffusionNet instance;
    
    private List<String[]> groupMemList=new ArrayList<>();
    private List<String[]> groupMsgList=new ArrayList<>();

    private Map<String,List<String>> groupMemMap=new HashMap<>();
    private Map<String,List<String>> groupMemNetworkMap=new HashMap<>();
    private Set<String> groupSet=new HashSet<>();
    
    //计算消息传播网络时，显示进度条
    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
    ProgressTicket ticket = null;
    
    //===============================================================================
    //用来保存拓扑结构的映射，key保存的是源节点，value保存的是该源节点的邻居节点
    private  Map<String,List<String>> topologyGraphMap=new HashMap<>();
    private  Map<String,List<String>> topologyGraphFiltered=new HashMap<>();//这个便是概率图网络
    //保存“时间—频度”网络key:源节点-目标节点，value保存的是频度，每出现一种频度便在list中添加一次
    private  Map<String,List<String>> timeFrequencyMap=new HashMap<>();
    //保存遍历过程中的节点到达时间
    private  Map<String,String> reachedMap=new HashMap<>();
    //保存一个节点改动了哪些节点，这个表用于在修改到达时间时，根据此表找到被修改到达时间的源节点改动过的节点
    private  Map<String,List<String>> changedMap=new HashMap<>();
    //该映射保存每个节点的入边以及该入边出现的次数
    private Map<String,Map<String,Integer>> importEdgeMap=new HashMap<>();
    //===============================================================================
    
    private DiffusionNet(){
        this.init();
    }
    
    public Map<String,List<String>> getTopologyGraphMap(){
        return topologyGraphFiltered;
    }
    
    public Map<String,List<String>> getTimeFrequencyMap(){
        return timeFrequencyMap;
    }
    
    public Map<String,Map<String,Integer>> getImportEdgeMap(){
        return importEdgeMap;
    }
    
    //双重检查锁定，单例模式
    public static DiffusionNet getInstance(){
        if(instance==null){
            synchronized(DiffusionNet.class){
                if(instance==null){
                    instance=new DiffusionNet();
                }
            }
        }
        return instance;
    }
    
//    public Graph generateDiffusionGraph(){
//        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
//        if(pc.getCurrentProject()==null){
//            pc.newProject();
//        }
//        
//        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
//        Graph directedGraph=graphModel.getDirectedGraph();
//        directedGraph.setAttribute(BuildAttribute.BUILD_TYPE, BuildAttribute.DIFF_GROUP_TYPE);
//        
//        for(String s:topologyGraphFiltered.keySet()){
//            
//            if(directedGraph.getNode(s)==null){
//                Node n=graphModel.factory().newNode(s);
//                n.setLabel(s);
//                directedGraph.addNode(n);
//            }
//            List<String> neighbors=topologyGraphFiltered.get(s);
//            for(int i=0; i<neighbors.size(); i++){
//                String target=neighbors.get(i);
//                //先添加节点，然后添加边
//                if(directedGraph.getNode(target)==null){
//                    Node n=graphModel.factory().newNode(target);
//                    n.setLabel(target);
//                    directedGraph.addNode(n);
//                }
//                //添加边
//                Node sourceNode=directedGraph.getNode(s);
//                Node targetNode=directedGraph.getNode(target);
//                Edge edge=graphModel.factory().newEdge(sourceNode, targetNode);
//                if(!directedGraph.contains(edge)){
//                    directedGraph.addEdge(edge);
//                }
//            }
//        }
//        GraphManipulateUtils.setNodeAttributes(directedGraph);
//        GraphManipulateUtils.yifuLayout(graphModel);
//        return directedGraph;
//        
//    }

    /**
     * 使扩散运行指定次数，并且返回扩散到的节点以及平均到达时间
     * @param startVertex
     * @return
     */
    public Map<String,String> runDiffusionPath(String startVertex,int loopNum){
        Map<String,List<String>> testReachedMap=new HashMap<>();

        if(loopNum<=0){
            return null;
        }

        //运行指定趟数
        for(int i=0;i<loopNum;i++){
            reachedMap.put(startVertex, "0");
            this.oneTimeDiffusion(timeFrequencyMap, topologyGraphFiltered, startVertex);

            //将一遍遍历结果保存下来
            for(String s:reachedMap.keySet()){
                List<String> list=testReachedMap.get(s);
                if(list==null){
                    List<String> newList=new ArrayList<>();
                    newList.add(reachedMap.get(s));
                    testReachedMap.put(s, newList);
                }
                else{
                    list.add(reachedMap.get(s));
                    testReachedMap.put(s, list);
                }
            }
            reachedMap.clear();
            changedMap.clear();
        }
        //返回改变的内容
        Map<String,String> scopeMap=this.calDiffusionScope(testReachedMap,loopNum);
        return scopeMap;
    }


    /**
     * 用广度优先搜索的思想来模拟消息扩散的过程，某些访问过的节点有可能会被再次访问。同时也保存了节点的入边以及入边的出现次数。
     * @param timeFrequencyMap
     * @param topologyMap
     * @param sourceVertex
     */
    private void oneTimeDiffusion(Map<String,List<String>> timeFrequencyMap, Map<String,List<String>> topologyMap,String sourceVertex){
        Queue<String> queue=new LinkedList<>();
        queue.add(sourceVertex);

        while(!queue.isEmpty()){
            String startVertex=queue.poll();
            Set<String> updatedVertex=this.getNeighbourAndTime2(startVertex);
            queue.addAll(updatedVertex);
            //保存入边以及入边的出现次数
            if(updatedVertex!=null){
                for(String s:updatedVertex){
                    Map<String,Integer> value=importEdgeMap.get(s);
                    if(value==null){
                        Map<String,Integer> newValue=new HashMap<>();
                        newValue.put(startVertex, 1);

                        importEdgeMap.put(s, newValue);
                    }
                    else{
                        Integer a=value.get(startVertex);
                        value.put(startVertex, a==null?1:a+1);

                        importEdgeMap.put(s, value);
                    }
                }
            }
        }
    }

    /**
     *统计随着时间增长的节点的扩散范围
     * @param sumReachedMap
     */
    private Map<String,String> calDiffusionScope(Map<String,List<String>> sumReachedMap,int loopNum){
        Map<String,String> map=new HashMap<>();
        //该段代码用来计算loopNum次的平均值，只输出出现次数大于90%的节点的平均值
        for(String s:sumReachedMap.keySet()){
            List<String> list=sumReachedMap.get(s);

            int sum=0;
            for(int i=0;i<list.size();i++)
                sum+=Integer.valueOf(list.get(i));

            Double average=(double) sum/list.size();

            //过滤掉出现概率小于0.90的节点
            if((double)list.size()/loopNum>=0.90)
                map.put(s, String.valueOf(average.intValue()));
        }
        return map;
    }

    private Set<String> getNeighbourAndTime(String startVertex, Map<String,List<String>> topologyMap, Map<String,List<String>> timeFrequencyMap, Map<String,String> preReachedMap){
        Random random=new Random();
        Set<String> updatedVertex=new HashSet<>();
        //startVertex节点时间
        int startTime=Integer.parseInt(preReachedMap.get(startVertex));

        List<String> neighbour=topologyMap.get(startVertex);
        
        //如果有邻居节点
        if(neighbour!=null){
            //依次处理每个邻居节点
            for(int i=0;i<neighbour.size();i++){
                String targetVertex=neighbour.get(i);
                String group_group=startVertex+"\t"+targetVertex;
                List<String> edgeTimes=timeFrequencyMap.get(group_group);

                //随机选择一个时间
                int rnd=random.nextInt(edgeTimes.size());
                String selectedTime=edgeTimes.get(rnd);

                String isReachedVertex=preReachedMap.get(targetVertex);

                if(!selectedTime.equals("no")){
                    if(isReachedVertex!=null){
                        //修改到达时间，用短时间取代长时间
                        if(!isReachedVertex.equals("no")){
                            int currentTime=Integer.parseInt(selectedTime);
                            int preTime=Integer.parseInt(isReachedVertex);

                            if(currentTime+startTime<preTime){
                                preReachedMap.put(targetVertex, String.valueOf(currentTime+startTime));
                                //这个地方要不要把tragetVertex加入？룿
                                updatedVertex.add(targetVertex);
                            }
                        }
                        //这个else好像是多余的
                        else{
                            int currentTime=Integer.parseInt(selectedTime);
                            preReachedMap.put(targetVertex, String.valueOf(currentTime+startTime));
                            updatedVertex.add(targetVertex);
                        }
                    }
                    else
                    {
                        int currentTime=Integer.parseInt(selectedTime);
                        preReachedMap.put(targetVertex, String.valueOf(currentTime+startTime));
                        updatedVertex.add(targetVertex);
                    }
                }
            }
        }
        return updatedVertex;
    }

    /**
     * 修正过的用来计算开始节点StartVertex的邻居节点的时间的，这个方法中，包含了处理已经被赋予到达时间的邻居节点方法
     * @param startVertex 开始节点
     * @return 返回的是startVertex中随机被选中可以传播信息的邻居节点的集合
     */
    private Set<String> getNeighbourAndTime2(String startVertex){
        Random random=new Random();
        Set<String> updatedVertex=new HashSet<>();
        //startVertex节点时间
        int startTime=Integer.parseInt(reachedMap.get(startVertex));
        List<String> neighbour=topologyGraphFiltered.get(startVertex);

        //如果有邻居节点
        if(neighbour!=null){
            //依次处理每个邻居节点
            for(int i=0;i<neighbour.size();i++){
                String targetVertex=neighbour.get(i);
                String group_group=startVertex+"\t"+targetVertex;
                List<String> edgeTimes=timeFrequencyMap.get(group_group);

                //随机选择一个时间
                int rnd=random.nextInt(edgeTimes.size());
                String selectedTime=edgeTimes.get(rnd);

                String isReachedVertex=reachedMap.get(targetVertex);

                if(!selectedTime.equals("no")){
                    if(isReachedVertex!=null){
                        //修改到达时间，用短时间取代长时间
                        int currentTime=Integer.parseInt(selectedTime);
                        int preTime=Integer.parseInt(isReachedVertex);

                        if(currentTime+startTime<preTime){
                            reachedMap.put(targetVertex, String.valueOf(currentTime+startTime));

                            //修改被此源节点改动过的直接邻居的到达时间
                            int reductionTime=preTime-(currentTime+startTime);
                            this.alterNeighbourTime(targetVertex, reductionTime, changedMap, reachedMap);
                        }
                    }
                    else{
                        int currentTime=Integer.parseInt(selectedTime);
                        reachedMap.put(targetVertex, String.valueOf(currentTime+startTime));
                        updatedVertex.add(targetVertex);

                        //保存从源节点出发，保存改动了到达时间的目标节点
                        List<String> changedList=changedMap.get(startVertex);
                        if(changedList==null){
                            List<String> list=new ArrayList<>();
                            list.add(targetVertex);
                            changedMap.put(startVertex, list);
                        }
                        else{
                            changedList.add(targetVertex);
                            changedMap.put(startVertex, changedList);
                        }
                    }
                }
            }
        }
        return updatedVertex;
    }

    /**
     * 用来更改节点的邻居节点的到达时间。（只更改有消息传播行为的邻居时间）
     * @param sourceVertex
     * @param reductionTime
     * @param changedMap
     * @param reachedMap
     */
    private void alterNeighbourTime(String sourceVertex,int reductionTime,Map<String,List<String>> changedMap, Map<String,String> reachedMap){
        List<String> changedVertex=changedMap.get(sourceVertex);

        if(changedVertex!=null){
            for(int i=0;i<changedVertex.size();i++){
                int reachedTime=Integer.parseInt(reachedMap.get(changedVertex.get(i)));
                if(reachedTime>reductionTime){
                    reachedTime=reachedTime-reductionTime;
                    reachedMap.put(changedVertex.get(i), String.valueOf(reachedTime));
                }
            }
        }
    }

   

    /**
     * 寻找传播路径（改进版，只计算同一个人两次发言之间的传播关系，不跨越整个长度），不计算广播行为的权重。注意，该方法一次处理一个时间中的消息传播序列
     * @param groupNetworkMap 是群组间联系网络
     * @param groupMsgList 是群组消息网络（已经将不存在于GroupMmber中的群组过滤掉），数据结构格式为：List<String[]>??groupID??userID??time??cmdID
     */
    private void findPath2(Map<String,List<String>> groupNetworkMap, List<String[]> groupMsgList){
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for(int i=0;i<groupMsgList.size();i++){
            //flag用来确保：该节点有消息传播行为时，再添加No频率，若无消息传播行为，则不添加No频率
            boolean flag=false;
            Map<String,List<String>> shortMsgMap=new HashMap<>();
            //保存一次消息片段内传播者的群组Id
            Set<String> currentGroup=new HashSet<>();

            for(int j=i+1;j<groupMsgList.size();j++){
                //此if-else是用来修正之前的方法的
                //之前的方法是计算该节点到时间末尾之间的全部消息传播关系，这个只是计算同一个人两次发言之间的消息传播关系
                if(groupMsgList.get(i)[0].equals(groupMsgList.get(j)[0])){
                    break;
                }
                else{

                    String[] rowData1=groupMsgList.get(i);
                    String[] rowData2=groupMsgList.get(j);
                    String group_group=rowData1[0]+"\t"+rowData2[0];
                    String group_group2=rowData2[0]+"\t"+rowData1[0];
                    String difusionUser=rowData2[1];	//是第二个群组的传播者


                    //注意，生成的传播网络是有向图
                    List<String> userList1=groupNetworkMap.get(group_group);//userList是两个群组之间的共同好友列表��
                    List<String> userList2=groupNetworkMap.get(group_group2);
                    if(userList1!=null){
                        if(userList1.contains(difusionUser)){
                            //将目标群组加入
                            currentGroup.add(rowData2[0]);
                            flag=true;

                            long timeSpan=-1;
                            try{
                                long previousTime=dateFormat.parse(rowData1[2]).getTime();	//源节点发出时间
                                long currentTime=dateFormat.parse(rowData2[2]).getTime();	//传播者发出时间
                                timeSpan=(currentTime-previousTime)/60000+1;	//+1是为了防止timeSpan为0的情况
                            }catch(ParseException e){
                                e.printStackTrace();
                                System.exit(1);
                            }
                            
                            List<String> value=timeFrequencyMap.get(group_group);


                            if(value==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(String.valueOf(timeSpan));
                                timeFrequencyMap.put(group_group, newValue );

                                //
                                shortMsgMap.put(group_group, newValue);
                            }
                            else{
                                value.add(String.valueOf(timeSpan));
                                timeFrequencyMap.put(group_group, value);

                                //
                                shortMsgMap.put(group_group, value);
                            }
                        }
                    }
                    else if(userList2!=null){
                        if(userList2.contains(difusionUser)){
                            currentGroup.add(rowData2[0]);
                            flag=true;

                            long timeSpan=-1;
                            try{
                                long previousTime=dateFormat.parse(rowData1[2]).getTime();	//源节点发出时间
                                long currentTime=dateFormat.parse(rowData2[2]).getTime();	//传播者发出时间
                                timeSpan=(currentTime-previousTime)/60000+1;	//+1是为了防止timeSpan为0的情况
                            }catch(ParseException e){
                                e.printStackTrace();
                                System.exit(1);
                            }
                            

                            List<String> value=timeFrequencyMap.get(group_group);

                            if(value==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(String.valueOf(timeSpan));
                                timeFrequencyMap.put(group_group, newValue );

                                //
                                shortMsgMap.put(group_group, newValue);
                            }
                            else{
                                value.add(String.valueOf(timeSpan));
                                timeFrequencyMap.put(group_group, value);

                                //
                                shortMsgMap.put(group_group, value);
                            }
                        }
                    }
                }
            }

            Map<String,List<String>> nullMap=new HashMap<>();

            //处理完带时间的群组，在处理该节点中不带时间数据的群组
            String sourceGroup=groupMsgList.get(i)[0];
            List<String> neighbourGroup=topologyGraphFiltered.get(sourceGroup);
            if(neighbourGroup!=null&&flag==true){
                for(int k=0;k<neighbourGroup.size();k++){
                    if(!currentGroup.contains(neighbourGroup.get(k))){
                        String timeSpan="no";
                        String group_group=sourceGroup+"\t"+neighbourGroup.get(k);
                        List<String> value=timeFrequencyMap.get(group_group);

                        if(value==null){
                            List<String> newValue=new ArrayList<>();
                            newValue.add(timeSpan);
                            timeFrequencyMap.put(group_group, newValue );

                            //
                            nullMap.put(group_group, newValue);
                        }
                        else{
                            value.add(timeSpan);
                            timeFrequencyMap.put(group_group, value);

                            //
                            nullMap.put(group_group, value);
                        }
                    }
                }
            }
        }
    }


    /**
     * 预处理数据部分\
     * 将GroupMember表中构建成网络\
     * 将GroupMsgMonitor表格中的信息构建成拓扑网络，拓扑结构表示的是网络中群与群之间所有可能的消息传播方向\
     * @throws Exception 
     */
    private void init(){
        try{
            if (progressProvider != null) {
                ticket = progressProvider.createTicket("提取数据中...", null);
            }
            ticket.start( 5);

            //先取数据
            ticket.progress( 1);
            fetchData();

            /*
             * 处理Member
             */
            //将list形式的groupmember转换成map<String,List<String>>形式
    //        groupMemMap=CreateNetwork.createFriendNetwork(groupMemList);//这是先前的

            ticket.setDisplayName("预处理数据...");
            ticket.progress(2);
            groupMemMap=NetBuildMethods.buildTargetsNet(groupMemList);

            //将groupmemmber构建成网络，网络的节点是群组ID，边记录的是共同的群组成员
            groupMemNetworkMap=this.binToSinNet(groupMemMap);

            //提取数group网络中的群组，单独保存
            for(String s:groupMemNetworkMap.keySet()){
                String[] user=s.split("\t");
                groupSet.add(user[0]);
                groupSet.add(user[1]);
            }

            /**
             * 处理message，将数据按照事件分类，同时提出不在GroupMember中的成员。
             * Map<String,List<String[]>> oneEventMap用来分类存放事件的映射，key保存事件cmdID，value保存该事件下出现的所有消息
             * value的数据结构是List<String[]>,其中，String[]:groupId,userId,time,cmdId;
             * 
             */
            Map<String,List<String[]>> eventsMap=new HashMap<>();//key保存每个事件，value保存每个事件中的信息

            //将groupMsgList转换成特定格式
            //格式为String[]，groupID，userID，time，cmdID
            for(int i=0;i<groupMsgList.size();i++){
                Object[] rowData=(Object[])groupMsgList.get(i);
                String[] rowData2=new String[4];
                rowData2[0]=(String) rowData[0];
                rowData2[1]=(String) rowData[1];
                rowData2[2]=(String) rowData[2];
                rowData2[3]=(String) rowData[3];

                //剔除不在GroupMember中的群组���
                if(groupSet.contains(rowData2[0])){
                    List<String[]> value=eventsMap.get(rowData2[3]);
                    if(value==null){
                        List<String[]> newValue=new ArrayList<>();

                        newValue.add(rowData2);
                        eventsMap.put(rowData2[3], newValue);
                    }
                    else{
                        value.add(rowData2);
                        eventsMap.put(rowData2[3], value);
                    }
                }
            }


            /*
             * 这段代码用来分别调用findPath（）方法的，每个事件调用一次这个方法
             */
            ticket.setDisplayName("开始构建网络...");
            ticket.progress(3);
            int count=0;
            for(String s:eventsMap.keySet()){
                this.generateTopology(groupMemNetworkMap, eventsMap.get(s));
            }

            //过滤topologyGraph中重复的节点
            for(String s:topologyGraphMap.keySet()){
                Set<String> set=new HashSet<>(topologyGraphMap.get(s));
                topologyGraphFiltered.put(s, new ArrayList<>(set));
            }

            count=0;
            for(String s:eventsMap.keySet()){
                this.findPath2(groupMemNetworkMap, eventsMap.get(s));
            }
            ticket.finish();
            
        }catch(Exception e){
            
        }finally{
            ticket.finish();
        }
    }

    private void generateTopology(Map<String,List<String>> groupNetworkMap, List<String[]> groupMsgList){

        for(int i=0;i<groupMsgList.size();i++)
            for(int j=i+1;j<groupMsgList.size();j++){
                //此if-else用来修正之前的方法的
                //之前的方法是从计算该节点到事件末尾之间的全部消息传播关系，这个是只计算同一个人两次发言之间的消息传播关系
                if(groupMsgList.get(i)[0].equals(groupMsgList.get(j)[0])){
                    break;
                }
                else{
                    String[] rowData1=groupMsgList.get(i);
                    String[] rowData2=groupMsgList.get(j);
                    String group_group=rowData1[0]+"\t"+rowData2[0];
                    String group_group2=rowData2[0]+"\t"+rowData1[0];
                    String difusionUser=rowData2[1];	//是第二个群组的传播者

                    //注意，生成的传播网络是有向图
                    List<String> userList1=groupNetworkMap.get(group_group);//userList是两个群组间的共同好友列表��
                    List<String> userList2=groupNetworkMap.get(group_group2);
                    if(userList1!=null){
                        if(userList1.contains(difusionUser)){
                            List<String> targetGroup=topologyGraphMap.get(rowData1[0]);
                            if(targetGroup==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(rowData2[0]);
                                topologyGraphMap.put(rowData1[0], newValue);
                            }
                            else{
                                targetGroup.add(rowData2[0]);
                                topologyGraphMap.put(rowData1[0], targetGroup);
                            }
                        }
                    }
                    else if(userList2!=null){
                        if(userList2.contains(difusionUser)){
                            List<String> targetGroup=topologyGraphMap.get(rowData1[0]);
                            if(targetGroup==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(rowData2[0]);
                                topologyGraphMap.put(rowData1[0], newValue);
                            }
                            else{
                                targetGroup.add(rowData2[0]);
                                topologyGraphMap.put(rowData1[0], targetGroup);
                            }
                        }
                    }
                }
            }
    }

    private void fetchData(){
        DiffusionDataFetcher ddf=new DiffusionDataFetcher();
        ResultSet rs=ddf.getFilteredGroupMember();
        try {
            while(rs.next()){
                String[] s=new String[2];
                s[0]=rs.getString(1);
                s[1]=rs.getString(2);
                groupMemList.add(s);
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            
            ddf.close();
            System.exit(0);
        }

        DiffusionDataFetcher ddf2=new DiffusionDataFetcher();
        ResultSet rs2=ddf2.getFilteredGroupMsg();
        try{
            while(rs2.next()){
                String[] s=new String[4];
                for(int i=0; i<s.length; i++){
                    s[i]=rs2.getString(i+1);
                }
                groupMsgList.add(s);
            }
        }catch(SQLException e){
            Exceptions.printStackTrace(e);
            ddf.close();
            System.exit(0);
        }

    }

    /**
     * 将二分网络投影成单顶点网络.\
     * 实际所有思想是：例如计算GroupMember中群与群之间的关系，统计两个群中所包含的共同的好友个数，赋予权重.\
     * 数据结构是将Map<String,List<String>>中map的key字段两两统计，统计每对key字段中的list所包含的相同成员数目\
     * 返回的是Map<String,List<String>>\
     * @param map 表中的key字段装的是群组Id，表中的value字段装的是该群中的成员
     * @return 表中的key装的是“群组ID-群组ID”，value字段装的是共同的成员名称。
     */
    private Map<String,List<String>> binToSinNet(Map<String,List<String>> map){
        List<String> keyList=new ArrayList<>();
        Set<String> set=map.keySet();
        keyList.addAll(set);//将Map中的群组存储到list中，方便遍历

        Map<String,List<String>> binToSinMap=new HashMap<>();

        for(int i=0;i<keyList.size();i++)
            for(int j=i+1;j<keyList.size();j++){
                List<String> valueList1=map.get(keyList.get(i));
                List<String> valueList2=map.get(keyList.get(j));

                List<String> edgeList=new ArrayList<>();//存储边的列表���
                for(int k=0;k<valueList1.size();k++){
                    if(valueList2.contains(valueList1.get(k))){
                        edgeList.add(valueList1.get(k));
                    }
                }
                if(edgeList.size()!=0){
                    String gro2gro=keyList.get(i)+"\t"+keyList.get(j);
                    binToSinMap.put(gro2gro, edgeList);
                }
            }
        return binToSinMap;
    }
}

