/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hp-6380
 */
public class NetBuildMethods {
    
    /**
     * 创建网络，该方法将每行数据中的第一个元素直接转换成第二个元素.
     * Map<String,Integer>数据中，key保存的是sourceNode+"\t"+targetNode格式数据，表示源节点指向目标节点，value保存的是边的权重，权重是根据这条边出现次数决定的
     * @param list
     * @return 
     */
    public static Map<String,Integer> buildPlainNet(List<String[]> list){
        Map<String, Integer> map=new HashMap<>();
        
        for(int i=0; i<list.size(); i++){
            String sourceNode=list.get(i)[0];
            String targetNode=list.get(i)[1];
            String source2target=sourceNode+"\t"+targetNode;
            Integer value=map.get(source2target);
            map.put(source2target, value==null?1:value+1);
        }
        return map;
    }
    
    /**
     * 创建TB_WX_FRIENDS表中目标节点和其好友关系网的构建网络方法.
     * @param list
     * @return 
     */
    public static Map<String, List<String>> buildTargetsNet(List<String[]> list){
        Map<String, List<String>> map=new HashMap<>();
        
        for(int i=0; i<list.size(); i++){
            String sourceNode=list.get(i)[0];
            String targetNode=list.get(i)[1];
            
            List<String> temp=map.get(sourceNode);
            if(temp==null){
                List<String> targetNodeList=new ArrayList<>();
                targetNodeList.add(targetNode);
                map.put(sourceNode, targetNodeList);
            }else{
                List<String> targetNodeList=new ArrayList<>();
                targetNodeList.add(targetNode);
                targetNodeList.addAll(temp);
                map.put(sourceNode, targetNodeList);
            }
            
        }
        return map;
    }
    
   
    /**
     * 将二分网络投影成单顶点网络，所用思想：例如计算GroupMember中群与群之间的关系，统计两个群中所包含的共同好友的个数，赋予权重.
     * 数据结构是将Map<String,List<String>> 中map的key字段两两统计，统计每对key字段中的list所包含的相同成员数目
     * @param map key（String）：friendId或者groupId；value（List<String>）好友列表或者群成员列表
     * @return
     */
    public static Map<String,Integer> binToSinNet(Map<String,List<String>> map){
        List<String> keyList=new ArrayList<>();
        Set<String> set=map.keySet();
        keyList.addAll(set);

        Map<String,Integer> binToSinMap=new HashMap<>();

        for(int i=0;i<keyList.size();i++)
            for(int j=i+1;j<keyList.size();j++){
                List valueList1=map.get(keyList.get(i));
                List valueList2=map.get(keyList.get(j));

                int count=0;
                for(int k=0;k<valueList1.size();k++){
                    if(valueList2.contains(valueList1.get(k))){
                        count++;
                    }
                }

                if(count!=0){
                    String gro2gro=keyList.get(i)+"\t"+keyList.get(j);
                    binToSinMap.put(gro2gro, count);   
                }
            }
        return binToSinMap;
    }
    
    
}
