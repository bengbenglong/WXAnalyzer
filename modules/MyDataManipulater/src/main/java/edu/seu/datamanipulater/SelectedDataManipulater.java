/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.datamanipulater;

import java.util.List;

/**
 *
 * @author hp-6380
 */
public interface SelectedDataManipulater {
    
    void setSelectedData(List<String> selectedData);
    
    List<String> getSelectedData();
    
    void addARowData(String rowData);
    
    void printSelectedData();
}
