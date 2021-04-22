package com.xsw.security;

import com.xsw.pojo.SysRole;
import com.xsw.pojo.SysUser;
import com.xsw.pojo.SysUserRole;
import com.xsw.service.SysRoleService;
import com.xsw.service.SysUserRoleService;
import com.xsw.service.SysUserService;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service("userDetailsService")
public class CustomUserDeatilsService implements UserDetailsService {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private SysUserRoleService userRoleService;

    /**
     *登录页面提交的时候默认调用loadUserByUsername方法  并且传入用户名
     * @param username 用户名
     * @return   返回用户的权限
     * @throws UsernameNotFoundException
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        //从数据库中取出用户信息
        SysUser user =userService.selectByName(username);

        //判断用户是否存在
        if (user==null){
            throw new UsernameNotFoundException("用户名不存在");
        }

        //添加权限
        List<SysUserRole> userRoles = userRoleService.listByUserId(user.getId());
        for (SysUserRole userRole:userRoles){
            SysRole role=roleService.selectById(userRole.getRoleId());
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        //返回UserDetailsService    返回用户的权限
       return new User( user.getName(),user.getPassword(),authorities);
    }
}
