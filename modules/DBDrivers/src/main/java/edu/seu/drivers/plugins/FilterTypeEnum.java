/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.drivers.plugins;

/**
 *
 * @author hp-6380
 */
public enum FilterTypeEnum {
    //过滤friends表格中的公共号和内容为空的行的信息
    TABLE_FRIENDS,
    //过滤friends表中的公共号和目标成员所加入的群信息
    TABLE_FRIENDS_GROUP,
    //过滤friends表中的公共号和目标成员的好友信息
    TABLE_FRIENDS_FRI,
    
    //过滤msgmonitor中的weixin，fmessage，qqmail，tmessage等信息
    TABLE_MSG_MONITOR,
    //过滤msgmonitor中列名RECERUSERNAME中内容包含@chatroom内容的行数据
    TABLE_MSG_MONITOR_GROUP,
    //过滤msgmonitor中列名为RECERUSERNAME中内容不为@chatroom内容的行数据
    TABLE_MSG_MONITOR_FIR,
    
    
    //构建网络选择的构建网络方式
    FRIENDS_RELATION, FRIENDS_PLAIN_FRI, FRIENDS_PLAIN_GROUP,
    GROUP_MEMBER_PLAIN, GROUP_MEMBER_RELATION,
    GROUP_MSG_DIFFUSION,
    MSG_MONITOR_DIFFUISON, MSG_MONITOR_PLAIN_FRI, MSG_MONITOR_PLAIN_GROUP
}
