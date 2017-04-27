/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.datamanipulater.impl;

import edu.seu.datamanipulater.SelectedDataManipulater;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service = SelectedDataManipulater.class)
public class DefaultSelectedDataManipulater implements SelectedDataManipulater{

    private List<String> selectedData;
    
    public DefaultSelectedDataManipulater(){
        selectedData=new ArrayList<>();
    }
    
    public DefaultSelectedDataManipulater(List<String> selectedData){
        this.selectedData=selectedData;
    }
    
    @Override
    public void setSelectedData(List<String> selectedData) {
        this.selectedData=selectedData;
    }

    @Override
    public List<String> getSelectedData() {
        return selectedData;
    }

    @Override
    public void addARowData(String rowData) {
        selectedData.add(rowData);
    }

    @Override
    public void printSelectedData() {
        for(int i=0; i<selectedData.size(); i++){
            System.out.println(selectedData.get(i));
        }
    }

    
}
