/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector.oldPerspective;

/**
 *  这个接口用来抽取该模块中正在显示的表格中的某些数据
 * @author hp-6380
 */
public interface LabPerDataAccessor {
    
    public boolean hasColumn(String columnName);
    
    public String[] getColumnNames();
    
    public String[] getARowData(String columnName,String rowData);
    
}
