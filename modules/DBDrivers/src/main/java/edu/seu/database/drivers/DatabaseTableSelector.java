/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.database.drivers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author hp-6380
 */
public class DatabaseTableSelector {
    
    private DatabaseConnection dbConn=null;
    private ResultSet resultSet=null;
    private String dbTable=null;
    private String timeIntever=null;
    
    
    public DatabaseTableSelector(String dbTable, String timeIntever){
        this.dbTable=dbTable;
        this.timeIntever=timeIntever;
    }
    
    //根据dbTable字段来选择合适的方法加载数据库数据，根据where字段来选择符合条件的数据
    public ResultSet getData(){
        //只选择dbTable的情况
        if(timeIntever.equals("----年--月--日,----年--月--日")){
            if(dbTable.equals("TB_WX_ACCOUNT")){
                    return getDBAccount();
            }
            else if(dbTable.equals("TB_WX_FRIENDS")){
                return getDBFriends();
            }
            else if(dbTable.equals("TB_WX_GROUP")){
                return getDBGroup();
            }
            else if(dbTable.equals("TB_WX_GROUPMEMBER")){
                return getDBGroupMember();
            }
            else if(dbTable.equals("TB_WX_GROUPMESSAGE")){
                return getDBGroupMessage();
            }
            else if(dbTable.equals("TB_WX_MSGMONITOR")){
                return getDBMsgMonitor();
            }
            else{
                System.err.print("取数据库数据出错");
                return null;
            }
        }else{
            if(dbTable.equals("TB_WX_GROUPMESSAGE")){
                return getDBGroupMessage(timeIntever);
            }
            else if(dbTable.equals("TB_WX_MSGMONITOR")){
                return getDBMsgMonitor(timeIntever);
            }
            else{
                System.err.print("取数据库数据出错");
                return null;
            }
        }
        
    }
    
    
    private ResultSet getDBAccount(){
        String sql="select * from TB_WX_ACCOUNT";
//        String sql="select ACCOUNTNAME,USERID,NICKNAME,IMID,MOBILEPHONE,SEX,EMAIL,PROVINCE,CITY from TB_WX_ACCOUNT";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    private ResultSet getDBFriends(){
        String sql="select * from TB_WX_FRIENDS";
//        String sql="select ACCOUNTNAME,USERID,FRIENDID,NICKNAME,IMID,MOBILEPHONE,SEX,EMAIL,PROVINCE,CITY from TB_WX_FRIENDS";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    private ResultSet getDBGroup(){
        String sql="select * from TB_WX_GROUP";
//        String sql="select GROUPID,GROUPNAME,CREATOR,CREATETIME,MEMBERNUM from TB_WX_GROUP";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    private ResultSet getDBGroupMember(){
        String sql="select * from TB_WX_GROUPMEMBER";
//        String sql="select GROUPID,ACCOUNTNAME,USERID,NICKNAME,IMID,MOBILEPHONE,SEX,EMAIL,PROVINCE,CITY from TB_WX_GROUPMEMBER";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    private ResultSet getDBGroupMessage(){
        String sql="select * from TB_WX_GROUPMESSAGE";
//        String sql="select CMDID,MSGID,GROUPID,USERID,ACCOUNTNAME,MSGTIME,CONTENT from TB_WX_GROUPMESSAGE";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    private ResultSet getDBGroupMessage(String where){
        String[] time=where.split(",");
        String condition=null;
        //分别设置只选择结束时间、只选择开始时间、开始时间和结束时间相同、开始和结束时间不同的sql语句
        if(time[0].equals("----年--月--日")){
            condition=" where MSGTIME < '"+time[1]+"'";
        }
        else if(time[1].equals("----年--月--日")){
            condition=" where MSGTIME > '"+time[0]+"'";
        }
        else if(time[0].equals(time[1])){
            String startTime=time[0]+" 00:00:00";
            String endTime=time[0]+" 23:59:59";
            condition=" where MSGTIME >= '"+startTime+"' AND MSGTIME <= '"+endTime+"'";
        }
        else{
            condition=" where MSGTIME > '"+time[0]+"' AND MSGTIME < '"+time[1]+"'";
        }
        String sql="select * from TB_WX_GROUPMESSAGE"+condition;
//        String sql="select CMDID,MSGID,GROUPID,USERID,ACCOUNTNAME,MSGTIME,CONTENT from TB_WX_GROUPMESSAGE"+condition;
        System.out.println(sql);
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        try {
            System.out.println("result: "+rs.getRow());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return rs;
    }
    
    
    private ResultSet getDBMsgMonitor(){
        String sql="select * from TB_WX_MSGMONITOR";
//        String sql="select ACCOUNTNAME,USERID,NICKNAME,RECERUSERNAME,MSGTIME,CONTENT,MSGDEVICETYPE from TB_WX_MSGMONITOR";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    private ResultSet getDBMsgMonitor(String where){
        String[] time=where.split(",");
         String condition=null;
        //分别设置只选择结束时间、只选择开始时间、开始和结束时间都选择的sql语句
        if(time[0].equals("----年--月--日")){
            condition=" where MSGTIME < '"+time[1]+"'";
        }
        else if(time[1].equals("----年--月--日")){
            condition=" where MSGTIME > '"+time[0]+"'";
        }
        else if(time[0].equals(time[1])){
            String startTime=time[0]+" 00:00:00";
            String endTime=time[0]+" 23:59:59";
            condition=" where MSGTIME >= '"+startTime+"' AND MSGTIME <= '"+endTime+"'";
        }
        else{
            condition=" where MSGTIME > '"+time[0]+"' AND MSGTIME < '"+time[1]+"'";
        }
        String sql="select * from TB_WX_MSGMONITOR"+condition;
//        String sql="select ACCOUNTNAME,USERID,NICKNAME,RECERUSERNAME,MSGTIME,CONTENT,MSGDEVICETYPE from TB_WX_MSGMONITOR"+condition;
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    public void close(){
        dbConn.close();
    }
   
}
