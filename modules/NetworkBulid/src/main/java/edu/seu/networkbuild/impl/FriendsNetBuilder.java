/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.impl;

import edu.seu.networkbuild.api.BuildAttribute;
import edu.seu.networkbuild.util.NetBuildMethods;
import edu.seu.networkbuild.api.BuildNet;
import edu.seu.networkbuild.api.MyTableAttr;
import edu.seu.networkbuild.util.GephiDataFormatConverter;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Graph;


/**
 *
 * @author hp-6380
 */
public class FriendsNetBuilder implements BuildNet{

    Map<String,Integer> map;

    private final String idColumnName="FRIENDID";
    private final String[] needfulColumnNames=new String[]{"FRIENDID","USERID"};
    private List<String[]> data;
    private String buildType;
    private MyTableAttr tableAttr;
    
    @Override
    public String getIdColumnName(){
        return idColumnName;
    }
    
    @Override
    public void init(List<String[]> data, String buildType, MyTableAttr tableAttr) {
       this.data=data;
       this.buildType=buildType;
       this.tableAttr=tableAttr;
    }

    @Override
    public void setData(List<String[]> data) {
        this.data=data;
    }

    @Override
    public String[] getNeedfulColumnNames() {
        return needfulColumnNames;
    }

    @Override
    public void build() {
        switch(buildType){
            case BuildAttribute.FRIENDS_PLAIN:
                map=buildNet();
                break;
//            case JOINED_GROUP_NET:
//                map=buildNet();
//                break;
            case BuildAttribute.FRIENDS_RELATION:
                map=buildRelationNet();
                break;
            default:
                throw new AssertionError("buildType选择错误");
        }
    }
    
    @Override
    public Graph getDirectedGraph() {
        return null;
    }

    @Override
    public void setBuildType(String type) {
        this.buildType=type;
    }

    @Override
    public String getBuildTypes() {
        return buildType;
    }

    
    /**
     * 该方法可以构建PLAIN_NET和JOINED_GROUP_NET;
     * @return 
     */
    private Map<String,Integer> buildNet(){
        Map<String,Integer> plainNet=NetBuildMethods.buildPlainNet(data);
        //生成gephi形式网络
        GephiDataFormatConverter gephiConverter=new GephiDataFormatConverter();
        Graph graph=gephiConverter.toDirectedGraph(plainNet, tableAttr);
        //设置该网络的属性
        graph.setAttribute(BuildAttribute.BUILD_TYPE, BuildAttribute.FRIENDS_PLAIN);
        return plainNet;
    }
    
    /**
     * 该方法可以构建RELATION_NET
     * @return 
     */
    private Map<String,Integer> buildRelationNet(){
        Map<String,List<String>> targetNet=NetBuildMethods.buildTargetsNet(data);
        Map<String,Integer> relationNet=NetBuildMethods.binToSinNet(targetNet);
        //生成gephi形式网络
        GephiDataFormatConverter gephiConverter=new GephiDataFormatConverter();
        Graph graph=gephiConverter.mapNumberToUndirectedGraph(relationNet, tableAttr);
        //设置该网络的属性
        graph.setAttribute(BuildAttribute.BUILD_TYPE, BuildAttribute.FRIENDS_RELATION);
        return relationNet;
    }
    
    
}
