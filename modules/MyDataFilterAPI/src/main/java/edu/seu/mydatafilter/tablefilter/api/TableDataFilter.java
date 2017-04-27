/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.mydatafilter.tablefilter.api;

import javax.swing.JTable;

/**
 *
 * @author hp-6380
 */
public interface TableDataFilter {

    
    void init(JTable table, String tableName, FilterTypeEnum filterType);
    
    void execute();
    
    void setTableName(String tableName);
    
    String getTableName();
    
    void setFilterType(FilterTypeEnum filterType);
    
    FilterTypeEnum getFilterType();
    
    void setFilterTable(JTable jTable);
    
    JTable getfilterTable();
}
