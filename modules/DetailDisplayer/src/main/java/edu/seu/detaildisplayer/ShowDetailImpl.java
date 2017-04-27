/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.detaildisplayer;

import edu.seu.detaildisplayer.api.ShowDetail;
import edu.seu.lizhenglong.mylabperspective.api.LabPerDataAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=ShowDetail.class)
public class ShowDetailImpl implements ShowDetail{

    private DisplayerPanel displayer;
    private Map<String,String> attrName;//在显示的时候，将属性名称从英文转换成中文
    public ShowDetailImpl(){
        displayer=DisplayerPanel.getInstance();
        AttributeName_zh_cn attributeName_zh_cn=new AttributeName_zh_cn();
        attrName=attributeName_zh_cn.getAttributeNameZh();
        
                
    }
    
    @Override
    public void showDetail(Node n) {
        List<String> attrList=new ArrayList<>();
        Set<String> attrKeys=n.getAttributeKeys();
        for(String key:attrKeys){
            if(n.getAttribute(key)!=null && !key.equals("cmdid")){//不显示cmdid属性和空值属性
                if(attrName.get(key)!=null ){
                    attrList.add(attrName.get(key)+"\t"+n.getAttribute(key));
                }else{
                    attrList.add(key+"\t"+n.getAttribute(key));
                }
            }
        }
        
        //在这里调用展示
        displayer.showNewObject(attrList.toArray(new String[]{}));
        
        //==========这里显示边的中的属性================
        
    }
    
    
    
    //显示节点信息
    private String[] showNodeDetails(String columnName, String aRowName){
        LabPerDataAccessor dataAcce=Lookup.getDefault().lookup(LabPerDataAccessor.class);
        
        String[] columnNames=dataAcce.getColumnNames();
        String[] rowDatas=new String[columnNames.length];
        
        if(!dataAcce.hasColumn(columnName)){
            return null;
        }
        rowDatas=dataAcce.getARowData(columnName, aRowName);
        
        //将两个内容合并并去除空的列内容
        List<String> list=new ArrayList<>();
        for(int i=0; i<rowDatas.length; i++){
            if(rowDatas[i]!=null){
                String row=columnNames[i]+":\t"+rowDatas[i];
                list.add(row);
            }
        }
        return list.toArray(new String[list.size()]);
    }
    
}
