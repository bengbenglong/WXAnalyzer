/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector.oldPerspective;

import javax.swing.JTable;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=LabPerDataAccessor.class)
public class LabPerDataAccessorImpl implements LabPerDataAccessor{

    private OldPerspective myLabPer;
    private boolean initFlag=false;
    
    @Override
    public boolean hasColumn(String columnName) {
        if(initFlag==false){
            initMyLabPer();
            initFlag=true;
        }
        JTable table=myLabPer.getTable();
        boolean hasColumnFlag=true;
        try{
            table.getColumnModel().getColumnIndex(columnName);
        }catch(IllegalArgumentException e){
            hasColumnFlag=false;
        }
        return hasColumnFlag;
    }

    @Override
    public String[] getARowData(String columnName, String rowName) {
        if(initFlag==false){
            initMyLabPer();
            initFlag=true;
        }
        
        
        JTable table=myLabPer.getTable();
        int columnIndex=table.getColumnModel().getColumnIndex(columnName);
        int rowIndex=-1;
        for(int i=0; i<table.getRowCount(); i++){
            if(table.getValueAt(i, columnIndex).equals(rowName)){
                rowIndex=i;
                break;
            }
        }
        
        //遍历该行数据的每一列容
        int columnNum=table.getColumnCount();
        String[] rowDatas=new String[columnNum];
        for(int i=0; i<columnNum; i++){
            rowDatas[i]=(String)table.getValueAt(rowIndex, i);
        }
        return rowDatas;
    }
    
    @Override
    public String[] getColumnNames() {
        if(initFlag==false){
            initMyLabPer();
            initFlag=true;
        }
        JTable table=myLabPer.getTable();
        String[] columnName=new String[table.getColumnCount()];
        for(int i=0; i<table.getColumnCount(); i++){
            columnName[i]=table.getColumnName(i);
        }
        return columnName;
    }
    
    private void initMyLabPer(){
        myLabPer=OldPerspective.getInstance();
    }

    
    
}
