/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.api;

import java.util.List;
import org.gephi.graph.api.Graph;

/**
 *
 * @author hp-6380
 */
public interface BuildNet {
    
    void init(List<String[]> data, String buildType, MyTableAttr tableAttr);
    
    void setData(List<String[]> data);
    
    void build();
    
    void setBuildType(String type);
    
    String getBuildTypes();
    
    String[] getNeedfulColumnNames();
    
    String getIdColumnName();
    
    Graph getDirectedGraph();
}
