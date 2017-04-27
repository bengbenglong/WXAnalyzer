/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.mydatalab.api;

import java.util.List;

/**
 *
 * @author hp-6380
 */
public interface MyRowDataManipulater {
    
    List<String> getSelectedRowData();
    
    void setSelectedRowData(List<String> selectedRowData);
    
    int getSelectedRowDataNum();
    
    void printSelectedRowData();
}
