package com.xsw.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration  //标识该类为配置类
@EnableWebSecurity //开启Security服务
@EnableGlobalMethodSecurity(prePostEnabled = true)  //开启全局Security注解
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomUserDeatilsService userDeatilsService;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception{
        //替换掉默认的userDetailService     加密    new BCryptPasswordEncoder()
        auth.userDetailsService(userDeatilsService).passwordEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                //密码解密 用户的用户名密码和权限都存在userDetailsService中
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                //密码校验
                return s.equals(charSequence.toString());
            }
        });
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //如果有允许匿名的url 填在下面
               // .antMatchers().permitAll()
                //     anonymous() 允许匿名用户访问
                //     permitAll() 无条件允许访问
                .anyRequest().authenticated()   //配置所有的路劲必须经过认证
                .and()
                //设置登录页
                .formLogin().loginPage("/login")
                //设置登录成功页面
                .defaultSuccessUrl("/").permitAll()
                //自定义登录用户名和密码参数  默认为username和password
                //.usernameParameter("username")
                //.passwordParameter("password")
                .and()
                .logout().permitAll();//设置登出的页面


        //关闭CSRF跨域
        http.csrf().disable();


    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //设置拦截忽略文件夹  可以对静态资源放行
        web.ignoring().antMatchers("/css/**","/js/**");

    }
}
