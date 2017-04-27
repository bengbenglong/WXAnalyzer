/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.diffusion;

import edu.seu.networkbuild.drivers.DiffusionDataFetcher;
import edu.seu.networkbuild.util.NetBuildMethods;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *该类计算传播概率图（传播者的传播行为止于再一次遇到该传播者之前，不会跨越整个事件周期）
 * @author hp-6380
 */
public class ProbabilityNet {
    private List<String[]> groupMemList;
    private List<String[]> groupMsgList;
    
    //概率权重网络，key为群组—群组，value存储了两个群组的传播者及传播权重
    private Map<String,List<String>> probabilityWeightMap=new HashMap<>();
    //概率图网络（去除了广播行为，权重未归一化）
    private Map<String,Double> probabilityGraph=new HashMap<>();
    //边的“时间-频率”分布网络
    private Map<String,List<String>> edgeTimeFrequency=new HashMap<>();
    //变得“时间-频率”分布网络（过滤了广播行为）
    private Map<String,List<String>> edgeTimeFrequencyFiltered=new HashMap<>();
    
    
    public void execute() throws Exception{
        this.fetchData();
        Map<String,List<String>> probabilityWeightMap=this.preProcess();
        this.wipeOutBroadcast(probabilityWeightMap);
        //规格化概率图权重
        this.normalizationProbability(probabilityGraph);
    }
    
    
    private Map<String,List<String>> preProcess() throws ParseException, Exception{
        //========处理groupmemeber======
        //将groupmember中的数据构建成网络
        Map<String,List<String>> groupMemNet=NetBuildMethods.buildTargetsNet(groupMemList);
        Map<String,List<String>> groupMemRelationNet=this.binToSinNet2(groupMemNet);
        Set<String> groupSet=new HashSet<>();
        //提取出groupMember网络中国的群组，单独保存
        for(String s:groupMemRelationNet.keySet()){
            String[] users=s.split("\t");
            groupSet.add(users[0]);
            groupSet.add(users[1]);
        }
        
        /**
         * ====处理GroupMessage====
         * 将数据按照事件分类，同时过滤掉在GroupMember中不存在的群号码。
         * Map<String，List<String[]>> zhuanfaRatioMap 是用来分类存放事件的映射，key保存事件，value保存该事件下出现的所有消息
         * value的数据结构是List<String[]>，其中，String[]:groupId,userId,time,cmdId;
         */
        Map<String,List<String[]>> zhuanfaRatioMap=new HashMap<>();//key保存每个事件，value保存每个事件中的信息
        //将groupMsgList转换成特定格式，格式为String[]:
        for(int i=0; i<groupMsgList.size(); i++){
            String[] rowData=groupMsgList.get(i);
            if(groupSet.contains(rowData[0])){
                List<String[]> value=zhuanfaRatioMap.get(rowData[3]);
                if(value==null){
                    List<String[]> newValue=new ArrayList<>();
                    newValue.add(rowData);
                    zhuanfaRatioMap.put(rowData[3], newValue);
                }else{
                    value.add(rowData);
                    zhuanfaRatioMap.put(rowData[3], value);
                }
            }
        }
        
        //每个事件调用一次findPath方法
        int count=0;
        for(String s : zhuanfaRatioMap.keySet()){
            this.findPath(groupMemNet, zhuanfaRatioMap.get(s));
            this.calEdgeTimeDis(groupMemNet, zhuanfaRatioMap.get(s));
        }
        
        return probabilityWeightMap;
    }
    
    
    
    /**
     * 寻找传播路径，不计算广播行为权重.权重计算用的是math.pow(timeSpan, -0.42),可以考虑使用其他公式
     * @param groupNetworkMap 群组之间的关系网络
     * @param groupMsgList  群消息网络（已经将不存在于GroupMember中的群组过滤掉了），数据结构格式为List<String[]>:groupId,userID,time.cmdId
     */
    private void findPath( Map<String,List<String>> groupNetworkMap, List<String[]> groupMsgList) throws ParseException{
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df=new DecimalFormat("0.000");
 
 
        for(int i=0;i<groupMsgList.size();i++)
            for(int j=i+1;j<groupMsgList.size();j++){
                //此if-else语句是用来修正之前的方法的
                //之前的方法是从计算该节点到事件末尾之间的全部消息传播关系，这个是只计算同一个人两次发言之间的消息传播关系
                if(groupMsgList.get(i).equals(groupMsgList.get(j))){
                    break;
                }
                else
                {
                    String[] rowData1=groupMsgList.get(i);
                    String[] rowData2=groupMsgList.get(j);
                    String group_group=rowData1[0]+"\t"+rowData2[0];
                    String group_group2=rowData2[0]+"\t"+rowData1[0];
                    String difusionUser=rowData2[1];	//是第二个群组的传播者

                    //注意，生成的传播网络是有向图
                    List<String> userList1=groupNetworkMap.get(group_group);//userList是两个群组间的共同好友列表
                    List<String> userList2=groupNetworkMap.get(group_group2);
                    if(userList1!=null){
                        if(userList1.contains(difusionUser)){
                            long previousTime=dateFormat.parse(rowData1[2]).getTime();	//源节点发出时间
                            long currentTime=dateFormat.parse(rowData2[2]).getTime();	//传播者发出时间
                            long timeSpan=(currentTime-previousTime)/60000+1;	//+1是为了防止timeSpan为0的情况；
                            double weight=Math.pow(timeSpan, -0.42);
                            String difusior_difusior_weight=rowData1[1]+"-"+rowData2[1]+"-"+df.format(weight);
   
         
                            List<String> value=probabilityWeightMap.get(group_group);

                            if(value==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(difusior_difusior_weight);
                                probabilityWeightMap.put(group_group, newValue );
                            }
                            else{
                                value.add(difusior_difusior_weight);
                                probabilityWeightMap.put(group_group, value);
                            }
                        }
                    }
                    else if(userList2!=null){
                        if(userList2.contains(difusionUser)){
                            long previousTime=dateFormat.parse(rowData1[2]).getTime();	//源节点发出时间
                            long currentTime=dateFormat.parse(rowData2[2]).getTime();	//传播者发出时间
                            long timeSpan=(currentTime-previousTime)/60000+1;	//+1是为了防止timeSpan为0的情况；
                            double weight=Math.pow(timeSpan, -0.42);
                            String difusior_difusior=rowData1[1]+"-"+rowData2[1]+"-"+df.format(weight);

                            List<String> value=probabilityWeightMap.get(group_group);

                            if(value==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(difusior_difusior);
                                probabilityWeightMap.put(group_group, newValue );
                            }
                            else{
                                value.add(difusior_difusior);
                                probabilityWeightMap.put(group_group, value);
                            }
                        }
                    }
                }
            }
    }
    
    /**
     * 这个方法是用来计算边时间概率分布图的
     * @param groupNetworkMap
     * @param groupMsgList
     * @throws Exception
     */
    private void calEdgeTimeDis(Map<String,List<String>> groupNetworkMap, List<String[]> groupMsgList) throws Exception{
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 
        for(int i=0;i<groupMsgList.size();i++)
            for(int j=i+1;j<groupMsgList.size();j++){
                if(groupMsgList.get(i).equals(groupMsgList.get(j))){
                    break;
                }
                else{
                    String[] rowData1=groupMsgList.get(i);
                    String[] rowData2=groupMsgList.get(j);
                    String group_group=rowData1[0]+"\t"+rowData2[0];
                    String group_group2=rowData2[0]+"\t"+rowData1[0];
                    String difusionUser=rowData2[1];	//是第二个群组的传播者

                    //注意，生成的传播网络是有向图
                    List<String> userList1=groupNetworkMap.get(group_group);//userList是两个群组间的共同好友列表
                    List<String> userList2=groupNetworkMap.get(group_group2);
                    if(userList1!=null){
                        if(userList1.contains(difusionUser)){
                            long previousTime=dateFormat.parse(rowData1[2]).getTime();	//源节点发出时间
                            long currentTime=dateFormat.parse(rowData2[2]).getTime();	//传播者发出时间
                            long timeSpan=(currentTime-previousTime)/60000+1;	//+1是为了防止timeSpan为0的情况；

                            String diffusior_diffusior_timeSpan=rowData1[1]+"-"+rowData2[1]+"-"+timeSpan;

                            List<String> value=edgeTimeFrequency.get(group_group);

                            if(value==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(diffusior_diffusior_timeSpan);
                                edgeTimeFrequency.put(group_group, newValue );
                            }
                            else{
                                value.add(diffusior_diffusior_timeSpan);
                                edgeTimeFrequency.put(group_group, value);
                            }
                        }
                    }
                    else if(userList2!=null){
                        if(userList2.contains(difusionUser)){
                            long previousTime=dateFormat.parse(rowData1[2]).getTime();	//源节点发出时间
                            long currentTime=dateFormat.parse(rowData2[2]).getTime();	//传播者发出时间
                            long timeSpan=(currentTime-previousTime)/60000+1;	//+1是为了防止timeSpan为0的情况；

                            String diffusior_diffusior_timeSpan=rowData1[1]+"-"+rowData2[1]+"-"+timeSpan;

                            List<String> value=edgeTimeFrequency.get(group_group);

                            if(value==null){
                                List<String> newValue=new ArrayList<>();
                                newValue.add(diffusior_diffusior_timeSpan);
                                edgeTimeFrequency.put(group_group, newValue );
                            }
                            else{
                                value.add(diffusior_diffusior_timeSpan);
                                edgeTimeFrequency.put(group_group, value);
                            }
                        }
                    }
                }
            }
    }
    
    private void fetchData() throws SQLException{
        DiffusionDataFetcher dataFetcher=new DiffusionDataFetcher();
        ResultSet memRS=dataFetcher.getFilteredGroupMember();
        
        int columnCount=memRS.getMetaData().getColumnCount();
        while(memRS.next()){
            String[] rowData=new String[columnCount];
             //注意：遍历resultSet的下标得从1开始。
            for (int i = 1; i <= columnCount; i++) {
                rowData[i - 1] = memRS.getString(i);
            }
            groupMemList.add(rowData);
        }
        dataFetcher.close();
        
        DiffusionDataFetcher dataFetcher2=new DiffusionDataFetcher();
        ResultSet msgRS=dataFetcher2.getFilteredGroupMsg();
        
        columnCount=msgRS.getMetaData().getColumnCount();
        while(msgRS.next()){
            String[] rowData=new String[columnCount];
            for(int i=1; i<=columnCount; i++){
                rowData[i-1]=msgRS.getString(i);
            }
            groupMsgList.add(rowData);
        }
        dataFetcher.close();
    }
    
    /**
    * 将二分网络投影成单定点网络（修改版）.
    * 实际所用的思想是：例如计算GroupMember中群与群之间的关系，统计两个群众所包含的共同好友的个数，赋予权重.\
    * 数据结构是将Map<String,List<String>> 中map的key字段两两统计，统计每对key字段中的list所包含的相同成员数目\
    * 返回的是Map<String,List<String>>\
    * @param map 表中的key字段装的是群组ID，表中的value字段装的是该群众中的成员
    * @return 表中的key装的是“群组ID——群组ID”，value字段装的是共同的成员名称
    */
    private Map<String,List<String>> binToSinNet2(Map<String,List<String>> map){
        List<String> keyList=new ArrayList<>();
        Set<String> set=map.keySet();
        keyList.addAll(set);//将map中的群组存储到list中，方便遍历

        Map<String,List<String>> binToSinMap=new HashMap<>();

        for(int i=0;i<keyList.size();i++)
            for(int j=i+1;j<keyList.size();j++){
                List<String> valueList1=map.get(keyList.get(i));
                List<String> valueList2=map.get(keyList.get(j));

                List<String> edgeList=new ArrayList<>();//存储边的列表
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
    
    /**
     * 将概率权重网络转换成概率图网络(概率权重网络),转换过程中去除概率为0的边
     * @param probabilityWeightMap
     */
    private void wipeOutBroadcast(Map<String,List<String>> probabilityWeightMap){
        DecimalFormat df=new DecimalFormat("0.000");
        //首先，计算“群组——群组”的权重（去除广播）
        for(String s:probabilityWeightMap.keySet()){
            List<String> list=probabilityWeightMap.get(s);
   
            double weight=0.0;
            for(int i=0;i<list.size();i++){
                String[] users=list.get(i).split("-");
                if(!users[0].equals(users[1]))
                weight+=Double.valueOf(users[2]);
            }
            if(weight>0.0){
                probabilityGraph.put(s, Double.valueOf(df.format(weight)));
            }
        }
 
        for(String s:edgeTimeFrequency.keySet()){
            List<String> list=edgeTimeFrequency.get(s);
            List<String> newList=new ArrayList<>();

            for(int i=0;i<list.size();i++){
                String[] users=list.get(i).split("-");
                if(!users[0].equals(users[1]))
                newList.add(list.get(i));
            }
            if(newList.size()>0){
                edgeTimeFrequencyFiltered.put(s, newList);
            }
        }
    }
    
    /**
     * 将概率图网络权重归一化
     * @param probabilityGraph
     */
    private void normalizationProbability(Map<String,Double> probabilityGraph){
        DecimalFormat df=new DecimalFormat("0.000");
 
        //首先找到最大权重
        double maxValue=0.0;
        for(String s:probabilityGraph.keySet())
            if(probabilityGraph.get(s)>maxValue){
                maxValue=probabilityGraph.get(s);
            }

        //用最大权重来归一化每条边
        double weight;
        for(String s:probabilityGraph.keySet()){
            weight=probabilityGraph.get(s);
            probabilityGraph.put(s, Double.valueOf(df.format(weight/maxValue)));
        }
    }
    
    public Map<String,Double> getProbabilityGraph(){
        return probabilityGraph;
    }
    
    public Map<String,List<String>> getProbabilityWeightMap(){
        return probabilityWeightMap;
    }
    
    public Map<String,List<String>> getEdgeTimeFrequencyFiltered(){
        return edgeTimeFrequencyFiltered;
    }
}
