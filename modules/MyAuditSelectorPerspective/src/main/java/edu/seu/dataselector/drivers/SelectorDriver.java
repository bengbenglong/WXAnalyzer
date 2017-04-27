/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector.drivers;

import edu.seu.database.drivers.DatabaseConnection;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author hp-6380
 */
public class SelectorDriver {
    private DatabaseConnection dbConn;
    
    
    //=============================2017/03/08新增方法=========================
    public ResultSet getGroupListWithTime(String startTime, String endTime){
        String sql="select * from TB_WX_GROUP where CREATETIME > '"+startTime+"' AND CREATETIME < '"+endTime+"'";
        return this.execute(sql);
    }
    
    public ResultSet getFriendListWithTime(String startTime,String endTime){
        String sql="select ACCOUNT,NUMBERTYPE,APPLYTIME,AUDITTIME,CMDID from TB_AUDIT where AUDITTYPE='2' and ( AUDITRESULT='1' or AUDITRESULT='-2') and AUDITTIME > '"+startTime+"' and AUDITTIME < '"+endTime+"'";
        return this.execute(sql);
    }
    
    public ResultSet getKeywodListWithTime(String startTime, String endTime){
        String sql="select APPLYKEYWORD,APPLYSTARTTIME,APPLYENDTIME,CMDID from TB_AUDIT where numbertype='4' and APPLYSTARTTIME > '"+startTime+"' and APPLYENDTIME < '"+endTime+"'";
        return this.execute(sql);
    }
    //=========================================================================
    
    public ResultSet getKeywordList(){
        String sql="select APPLYKEYWORD,APPLYSTARTTIME,APPLYENDTIME,CMDID from TB_AUDIT where numbertype='4'";
        return this.execute(sql);
    }
    
    public ResultSet getGroupMessage(String[] cmdid){
        String sql=null;
        if(cmdid.length==1){
            sql="select GROUPID,USERID,ACCOUNTNAME,MSGTIME,CONTENT from TB_WX_GROUPMESSAGE where CMDID='"+cmdid[0]+"'";
        }else{
            String cond="'"+cmdid[0]+"'";
            for(int i=1;i<cmdid.length;i++){
                cond+=",'"+cmdid[i]+"'";
            }
            sql="select GROUPID,USERID,ACCOUNTNAME,MSGTIME,CONTENT from TB_WX_GROUPMESSAGE where CMDID in ( "+cond+" )";
            System.out.println(sql);
        }
        return this.execute(sql);
    }
    
    public ResultSet getFriendsList(){
        String sql="select ACCOUNT,NUMBERTYPE,APPLYTIME,AUDITTIME,CMDID from TB_AUDIT where AUDITTYPE='2' and ( AUDITRESULT='1' or AUDITRESULT='-2') ";
        return this.execute(sql);
    }
    
    public ResultSet getFriendsNet(){
        String sql="select USERID,FRIENDID from TB_WX_FRIENDS"
                +" where ACCOUNTNAME not like '%@chatroom' "
                +" and ACCOUNTNAME not like 'gh_%' "
                +" and ACCOUNTNAME not in ('weixin','medianote','fmessage','floatbottle', 'qqmail', 'qmessage', 'tmessage','weibo','filehelper','qqsync')";
        return this.execute(sql);
    }
    
    public ResultSet getGroupNet(){
        String sql="select GROUPID,USERID from TB_WX_GROUPMEMBER";
        return this.execute(sql);
    }
    
    public ResultSet getAllFriendsInfo(String[] friendsIds){
        String sql=null;
        if(friendsIds.length==1){
            sql="select * from TB_WX_FRIENDS where FRIENDID ='"+friendsIds[0]+"'";
        }else {
            String cond="'"+friendsIds[0]+"'";
            for(int i=1;i<friendsIds.length;i++){
                cond+=",'"+friendsIds[i]+"'";
            }
            sql="select * from TB_WX_FRIENDS where FRIENDID in ("+cond+")";
        }
        return this.execute(sql);
    }
    
    public ResultSet getAllFriendsInfo2(String[] friendsIds){
        String sql=null;
        if(friendsIds.length==1){
            sql="select * from TB_WX_FRIENDS where FRIENDID ='"+friendsIds[0]+"'"
                    +" and ACCOUNTNAME not like '%@chatroom' "
                    +" and ACCOUNTNAME not like 'gh_%' "
                    +" and ACCOUNTNAME not in ('weixin','medianote','fmessage','floatbottle', 'qqmail', 'qmessage', 'tmessage','weibo','filehelper','qqsync')";
        }else {
            String cond="'"+friendsIds[0]+"'";
            for(int i=1;i<friendsIds.length;i++){
                cond+=",'"+friendsIds[i]+"'";
            }
            sql="select * from TB_WX_FRIENDS where FRIENDID in ("+cond+")"
                    +" and ACCOUNTNAME not like '%@chatroom' "
                    +" and ACCOUNTNAME not like 'gh_%' "
                    +" and ACCOUNTNAME not in ('weixin','medianote','fmessage','floatbottle', 'qqmail', 'qmessage', 'tmessage','weibo','filehelper','qqsync')";
        }
        return this.execute(sql);
    }
    
    
    public ResultSet getAllAccountInfo(String[] friendIDs){
        String sql=null;
        if(friendIDs.length==1){
            sql="select * from TB_WX_ACCOUNT where USERID = '"+friendIDs[0]+"' or ACCOUNTNAME = '"+friendIDs[0]+"' or IMID = '"+friendIDs[0]+"' or MOBILEPHONE = '"+ friendIDs[0]+"'";
        }else{
            String cond="'"+friendIDs[0]+"'";
            for(int i=1;i<friendIDs.length;i++){
                cond+=",'"+friendIDs[i]+"'";
            }
            sql="select * from TB_WX_ACCOUNT where USERID in ("+cond+") or ACCOUNTNAME in ("+cond+") or IMID in ("+cond+") or MOBILEPHONE in ("+cond+")";
        }
        return this.execute(sql);
    }
    
    public ResultSet getAllGroupInfo(String[] groupIds){
        String sql=null;
        if(groupIds.length==1){
            sql="select * from TB_WX_GROUP where GROUPID = '"+groupIds[0]+"'";
        }else{
            String cond="'"+groupIds[0]+"'";
            for(int i=1;i<groupIds.length;i++){
                cond+=",'"+groupIds[i]+"'";
            }
            sql="select * from TB_WX_GROUP where GROUPID in ("+cond+")";
        }
        return this.execute(sql);
    }
    
    
    
    public ResultSet getSingleGroupMemberInfo(String groupId){
        String sql="select * from TB_WX_GROUPMEMBER where GROUPID ='"+groupId+"'";
        return this.execute(sql);
    }
    
    public ResultSet getSingleFriendsInfo(String friendId){
        String sql="select * from TB_WX_FRIENDS where FRIENDID ='"+friendId+"'"
                +" and ACCOUNTNAME not like '%@chatroom' "
                +" and ACCOUNTNAME not like 'gh_%' "
                +" and ACCOUNTNAME not in ('weixin','medianote','fmessage','floatbottle', 'qqmail', 'qmessage', 'tmessage','weibo','filehelper','qqsync')";
       return this.execute(sql);
    }
    
    public ResultSet getAllGroupMemberInfo(String[] groupIds){
        String sql=null;
        if(groupIds.length==1){
            sql="select * from TB_WX_GROUPMEMBER where GROUPID ='"+groupIds[0]+"'";
        }else{
            String cond="'"+groupIds[0]+"'";
            for(int i=1;i<groupIds.length;i++){
                cond+=",'"+groupIds[i]+"'";
            }
            sql="select * from TB_WX_GROUPMEMBER where GROUPID in ("+cond+")";
        }
        return this.execute(sql);
    }
    
    public ResultSet getGroupList(){
        String sql="select * from TB_WX_GROUP";
        return this.execute(sql);
    }
    
    
    
    
    public void close(){
        try{
            if(dbConn!=null){
                dbConn.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接未正确关闭");
        }
    }
    
    private ResultSet execute(String sql){
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
}
