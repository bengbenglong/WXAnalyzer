/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.mydatafilter.tablefilter.impl;

import edu.seu.mydatafilter.tablefilter.api.TableDataFilter;
import edu.seu.mydatafilter.tablefilter.api.FilterTypeEnum;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=TableDataFilter.class)
public class TableDataFilterImpl implements TableDataFilter{

    private JTable table;
    private String tableName;
    private FilterTypeEnum filterType;
    
     @Override
    public void init(JTable table, String tableName, FilterTypeEnum filterType) {
        this.table=table;
        this.tableName=tableName;
        this.filterType=filterType;
    }
    
    @Override
    public void execute() {
        if(tableName.equals("TB_WX_FRIENDS")){
            FriendsTableFilter friendsTableFilter=new FriendsTableFilter(table,filterType);
            friendsTableFilter.execute();
        }
    }


    @Override
    public void setFilterTable(JTable jTable) {
        this.table=jTable;
    }

    @Override
    public JTable getfilterTable() {
        return table;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName=tableName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setFilterType(FilterTypeEnum filterType) {
        this.filterType=filterType;
    }

    @Override
    public FilterTypeEnum getFilterType() {
        return filterType;
    }

   

    
    
}
