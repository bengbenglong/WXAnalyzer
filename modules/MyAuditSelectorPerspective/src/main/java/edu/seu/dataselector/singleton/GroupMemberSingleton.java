/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector.singleton;

import edu.seu.dataselector.drivers.SelectorDriver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 *
 * @author hp-6380
 */
public class GroupMemberSingleton {
    private static GroupMemberSingleton instance;
    
    private Map<String,Set<String>> groupNet=new HashMap<>();
    private Map<String,Set<String>> userNet=new HashMap<>();
    
    private GroupMemberSingleton(){
        initNet();
    }
    
    public synchronized static GroupMemberSingleton getInstance(){
        if(instance==null){
            instance=new GroupMemberSingleton();
        }
        return instance;
    }
    
    private void initNet(){
        try{
            SelectorDriver driver=new SelectorDriver();
            ResultSet rs=driver.getGroupNet();
            while(rs.next()){
                String groupId=rs.getString("GROUPID");
                String userId=rs.getString("USERID");
                
                //添加group的成员信息
                Set<String> targetUserSet=groupNet.get(groupId);
                if(targetUserSet==null){
                    Set<String> newTargetUserSet=new HashSet<>();
                    newTargetUserSet.add(userId);
                    groupNet.put(groupId, newTargetUserSet);
                }else if(targetUserSet.contains(userId)==false){
                    targetUserSet.add(userId);
                    groupNet.put(groupId, targetUserSet);
                }
                
                //添加userId所有已经加入的群组信息，方便做二阶查找
                Set<String> targetGroupSet=userNet.get(userId);
                if(targetGroupSet==null){
                    Set<String> newTargetGroupSet=new HashSet<>();
                    newTargetGroupSet.add(groupId);
                    userNet.put(userId, newTargetGroupSet);
                }else if(targetGroupSet.contains(groupId)==false){
                    targetGroupSet.add(groupId);
                    userNet.put(userId, targetGroupSet);
                }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "从数据库中取数据过程出错");
        }
    } 
    
    public Map<String,Set<String>> getGroupNet(){
        return groupNet;
    }
    
    public Map<String,Set<String>> getUserNet(){
        return userNet;
    }
}
