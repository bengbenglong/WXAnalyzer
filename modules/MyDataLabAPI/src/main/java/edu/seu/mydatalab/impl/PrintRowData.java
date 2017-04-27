/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.mydatalab.impl;

import java.util.List;

/**
 *
 * @author hp-6380
 */
public class PrintRowData {
    
    private List<String> selectedRowData;
    
    public PrintRowData(List<String> selectedRowData){
        this.selectedRowData=selectedRowData;
    }
    
    public void printSelectedRowData(){
        for(int i=0;i<selectedRowData.size();i++){
            System.out.println(selectedRowData.get(i));
        }
    }
    
}
