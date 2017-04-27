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
public class FriendsTableFilter {
    
    private JTable table;
    private FilterTypeEnum filterType;
    
    public FriendsTableFilter(JTable table, FilterTypeEnum filterType){
        this.table=table;
        this.filterType=filterType;
    }
    
    public void execute(){
        String filterPattern=null;
        switch(filterType){
            case TABLE_FRIENDS:
                //过滤公共号和内容为空的行
                filterPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                filterMode(filterPattern);
                break;
            case TABLE_FRIENDS_GROUP:
                //过滤公共号和内容为空的行，同时过滤表格中ACCOUNTNAME列中以@chatroom结尾的行数据
                filterPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$|.*@chatroom$";
                filterMode(filterPattern);
                break;
            case TABLE_FRIENDS_FRI:
                //这部分先过滤公共号和内容为空的行，在用filterMode2方法将ACCOUNTNAME列中以@chatroom结尾的数据保留，其他数据行删除
                filterPattern="^gh_|^weixin$|^medianote$|^fmessage$|^floatbottle$|^qqmail$|^qmessage$|^tmessage$|^weibo$|^filehelper$";
                filterMode(filterPattern);
                String filterPattern2=".*@chatroom$";
                filterMode2(filterPattern2);
                break;
            default:
                throw new AssertionError(filterType.name());
        }
            
    }
    
    //过滤目标成员所加入的群组信息（同时也过滤掉公共号和内容为空的行
    private void filterMode(String filterPattern){
        DefaultTableModel tableModel=(DefaultTableModel)table.getModel();
        
        int rowNum=tableModel.getRowCount();
        int columnIndex=-1;
        for(int i=0; i<tableModel.getColumnCount(); i++){
            if(tableModel.getColumnName(i).equals("ACCOUNTNAME")){
                columnIndex=i;
                break;
            }
        }

        Pattern p=Pattern.compile(filterPattern);
        for(int i=0; i<rowNum; ){
            String content=(String) tableModel.getValueAt(i, columnIndex);
            //当content存在内容是判断,若content==null则直接删除这一行
            if(content!=null){
                if(p.matcher(content).lookingAt()){
                    tableModel.removeRow(i);
                    rowNum--;
                }else{
                    i++;
                }
            }else{
                tableModel.removeRow(i);
                rowNum--;
            }
        }
    }
    
    //该方法是用来选择待保留的数据
    private void filterMode2(String filterPattern){
        DefaultTableModel tableModel=(DefaultTableModel)table.getModel();
        
        int rowNum=tableModel.getRowCount();
        int columnIndex=-1;
        for(int i=0; i<tableModel.getColumnCount(); i++){
            if(tableModel.getColumnName(i).equals("ACCOUNTNAME")){
                columnIndex=i;
                break;
            }
        }

        Pattern p=Pattern.compile(filterPattern);
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
            
