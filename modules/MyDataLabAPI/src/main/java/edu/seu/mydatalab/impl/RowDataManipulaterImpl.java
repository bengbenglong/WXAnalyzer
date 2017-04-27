/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.mydatalab.impl;

import edu.seu.mydatalab.api.MyRowDataManipulater;
import java.util.List;

/**
 *
 * @author hp-6380
 */
public class RowDataManipulaterImpl implements MyRowDataManipulater{

    private List<String> selectedRowData;
    
    public RowDataManipulaterImpl(List<String> selectedRowData){
        this.selectedRowData=selectedRowData;
    }
    @Override
    public List<String> getSelectedRowData() {
        return selectedRowData;
    }

    @Override
    public int getSelectedRowDataNum() {
        return selectedRowData.size();
    }

    @Override
    public void printSelectedRowData() {
        if(selectedRowData!=null){
            for(int i=0; i<selectedRowData.size(); i++){
                System.out.println(selectedRowData.get(i));
            }
        }
    }

    @Override
    public void setSelectedRowData(List<String> selectedRowData) {
        this.selectedRowData = selectedRowData;
    }
    
}
