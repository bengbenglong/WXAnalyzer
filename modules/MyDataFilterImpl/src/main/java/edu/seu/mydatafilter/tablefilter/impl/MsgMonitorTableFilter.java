/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.mydatafilter.tablefilter.impl;

import edu.seu.mydatafilter.tablefilter.api.FilterTypeEnum;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author hp-6380
 */
public class MsgMonitorTableFilter {
    private JTable table;
    private FilterTypeEnum filterType;
    
    public MsgMonitorTableFilter(JTable table, FilterTypeEnum filterType){
        this.table=table;
        this.filterType=filterType;
    }
    
    public void execute(){
        String firstPattern=null;
        String secondPattern=null;
        switch(filterType){
            case TABLE_MSG_MONITOR:
                firstPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                secondPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                filter(firstPattern, secondPattern);
                break;
            case TABLE_MSG_MONITOR_GROUP:
                firstPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                secondPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$|.*@chatroom$";
                filter(firstPattern, secondPattern);
                break;
            case TABLE_MSG_MONITOR_FIR:
                firstPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                secondPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                String thirdPattern=".*@chatroom";
                filter(firstPattern, secondPattern);
                filter2(thirdPattern);
                break;
            default:
                throw new AssertionError(filterType.name());   
        }
                
        
    }
    
    private void filter(String firstPattern, String secondPattern){
        DefaultTableModel tableModel=(DefaultTableModel)table.getModel();
        
        int rowNum=tableModel.getRowCount();
        int firstColumnIndex=-1;
        int secondColumnIndex=-1;
        for(int i=0; i<tableModel.getColumnCount(); i++){
            if(tableModel.getColumnName(i).equals("ACCOUNTNAME")){
                firstColumnIndex=i;
            }
            if(tableModel.getColumnName(i).equals("RECERUSERNAME")){
                secondColumnIndex=i;
            }
        }
        
        Pattern p1=Pattern.compile(firstPattern);
        Pattern p2=Pattern.compile(secondPattern);
        for(int i=0; i<rowNum; ){
            String firstContent=(String) tableModel.getValueAt(i, firstColumnIndex);
            String secondContent=(String) tableModel.getValueAt(i, secondColumnIndex);
            //判断列ACCOUNTNAME和列RECERUSERNAME字段内容是否为空，为空直接删除此行数据，不为空在继续判断是否匹配
            if(firstContent==null||secondContent==null){
                tableModel.removeRow(i);
                rowNum--;
            }else{
                //若列ACCOUNTNAME或列RECERUSERNAME中有任何一个子弹满足，便删除此列
                if(p1.matcher(firstContent).lookingAt()||p2.matcher(secondContent).lookingAt()){
                    tableModel.removeRow(i);
                    rowNum--;
                }else{
                    i++;
                }
            }
        }
    }
    
    private void filter2(String thirdPattern){
        DefaultTableModel tableModel=(DefaultTableModel)table.getModel();
        
        int rowNum=tableModel.getRowCount();
        int columnIndex=-1;
        for(int i=0; i<tableModel.getColumnCount(); i++){
            if(tableModel.getColumnName(i).equals("RECERUSERNAME")){
                columnIndex=i;
                break;
            }
        }

        Pattern p=Pattern.compile(thirdPattern);
        for(int i=0; i<rowNum; ){
            String content=(String) tableModel.getValueAt(i, columnIndex);
            //删除不是以“@chatroom”结尾的行
            if(p.matcher(content).lookingAt()==false){
                tableModel.removeRow(i);
                rowNum--;
            }else{
                i++;
            }
        }
    }
    
    
}