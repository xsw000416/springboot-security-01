package com.xsw.service;

import com.sun.org.apache.regexp.internal.RE;
import com.xsw.Dao.SysUserRoleMapper;
import com.xsw.pojo.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserRoleService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    public List<SysUserRole> listByUserId(Integer userId){
        return userRoleMapper.listByUserID(userId);
    }
}
