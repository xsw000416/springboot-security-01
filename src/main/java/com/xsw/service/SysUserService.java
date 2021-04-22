package com.xsw.service;

import com.xsw.Dao.SysUserMapper;
import com.xsw.pojo.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserService {
    @Autowired
    private SysUserMapper userMapper;

    public SysUser selectById(Integer id){
        return userMapper.selectById(id);
    }

    public SysUser selectByName(String name){
        return userMapper.selectByName(name);
    }
}
