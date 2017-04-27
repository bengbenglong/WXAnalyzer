/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author hp-6380
 */
public class MyTableAttr {
    
    private Map<String,String[]> map=new HashMap<>();
    private String[] columnName;
    private String idColumnName;
    ConcurrentHashMap c=new ConcurrentHashMap();
    
    public void clear(){
        map.clear();
    }
    
    public void setIdColumnname(String id){
        idColumnName=id;
    }
    
    public String getIdColumnName(){
        return idColumnName;
    }
    
    public MyTableAttr(String[] columnName){
        this.columnName=columnName;
    }
    
    public String[] getColumnName(){
        return columnName;
    }

    public void addAttributes(String id, String[] columnName){
        map.put(id, columnName);
    }
    
    public String[] getAtttributes(String id){
        return map.get(id);
    }
    
}
