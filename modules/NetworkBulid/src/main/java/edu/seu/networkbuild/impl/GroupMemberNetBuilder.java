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
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=BuildNet.class)
public class GroupMemberNetBuilder implements BuildNet {

    Map<String,Integer> map=null;
    
    private final String idColumnName="GROUPID";
    private final String[] needfulColumnNames=new String[]{"GROUPID","USERID"};
    private List<String[]> data;
    private String buildType;
    private MyTableAttr tableAttr;
    
    
    @Override
    public String getIdColumnName(){
        return idColumnName;
    }
    
    @Override
    public void init(List<String[]> data,String buildType, MyTableAttr tableAttr){
        this.data=data;
        this.buildType=buildType;
        this.tableAttr=tableAttr;
    }
    
    @Override
    public void setData(List<String[]> data) {
        this.data=data;
    }

    @Override
    public void build() {
        GephiDataFormatConverter gephiConverter=new GephiDataFormatConverter();
        
        switch(buildType){
            case BuildAttribute.GROUP_MEMBER_PLAIN:
                map=buildPlainNet();
                Graph graph1=gephiConverter.toDirectedGraph(map, tableAttr);
                graph1.setAttribute(BuildAttribute.BUILD_TYPE, BuildAttribute.GROUP_MEMBER_PLAIN);
                break;
            case BuildAttribute.GROUP_MEMBER_RELATION:
                 map=buildRelationNet();
                 Graph graph2=gephiConverter.mapNumberToUndirectedGraph(map, tableAttr);
                 graph2.setAttribute(BuildAttribute.BUILD_TYPE, BuildAttribute.GROUP_MEMBER_RELATION);
                break;
            default:
                throw new AssertionError("buildType选择错误");
        }
    }
    
    @Override
    public String[] getNeedfulColumnNames(){
        return needfulColumnNames;
    }

    @Override
    public Graph getDirectedGraph() {
//        GephiDataFormatConverter gephiConverter=new GephiDataFormatConverter();
//        switch(buildType){
//            case RELATION_NET:
//                return gephiConverter.mapNumberToUndirectedGraph(map);
//            case PLAIN_NET:
//                return gephiConverter.toDirectedGraph(map);
//            default:
//                throw new AssertionError(buildType.name());
//        }
        return null;
    }

    @Override
    public void setBuildType(String type) {
        this.buildType=type;
    }

    @Override
    public String getBuildTypes() {
       return this.buildType;
    }

    
    private Map<String,Integer> buildPlainNet(){
        Map<String,Integer> plainNet=NetBuildMethods.buildPlainNet(data);
        return plainNet;
    }
    
    private Map<String,Integer> buildRelationNet(){
        Map<String,List<String>> map=NetBuildMethods.buildTargetsNet(data);
        Map<String,Integer> relationNet=NetBuildMethods.binToSinNet(map);
        return relationNet;
    }



}
    