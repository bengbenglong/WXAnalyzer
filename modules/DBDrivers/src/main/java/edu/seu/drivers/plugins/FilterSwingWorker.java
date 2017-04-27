/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.drivers.plugins;

import javax.swing.JTable;
import javax.swing.SwingWorker;

/**
 *
 * @author hp-6380
 */
public class FilterSwingWorker extends SwingWorker<Void,Void>{

    private JTable table;
    private FilterTypeEnum filterType;
    private MyProcessBar myProcessBar;
    
    public FilterSwingWorker(JTable table,FilterTypeEnum filterType, MyProcessBar myProcessBar){
        this.table=table;
        this.filterType=filterType;
        this.myProcessBar=myProcessBar;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        MsgMonitorTableFilter friendsTableFilter=new MsgMonitorTableFilter(table,filterType);
        friendsTableFilter.execute();
        return null;
    }
    
    @Override
    protected void done(){
        String result="过滤后结果数量为："+table.getRowCount();
        myProcessBar.setResultNumLabel(result);
    }
    
}
