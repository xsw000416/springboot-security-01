# springboot-security-01

# SpringBoot-Security-入门程序

```
一、导入依赖
二、创建数据库
三、准备页面
四、配置application.properties
五、创建实体、Dao、Service和Controller
5.1 实体
5.2 Dao
5.3 Service
5.4 Controller
六、配置SpringSecurity
 6.1 UserDetailsService
 6.2 WebSecurityConfig
七、运行程序
```





### 一、导入依赖

```xml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.1</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
```



### 二、创建数据库

一般权限控制有三层   即 `用户`<==>`角色`<==>`权限`  用户与角色是多对多，角色和权限也是多对多，暂时先不考虑权限，只考虑 `用户`<==>`角色`



1. 创建用户表   sys_user:

   ```sql
   CREATE TABLE `sys_user` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `name` varchar(255) NOT NULL,
     `password` varchar(255) NOT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
   ```

2. 创建权限表 sys_role:

   ```sql
   CREATE TABLE `sys_role` (
     `id` int(11) NOT NULL,
     `name` varchar(255) NOT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
   ```

3. 创建用户-角色表  sys_user_role:

   ```sql
   CREATE TABLE `sys_user_role` (
     `user_id` int(11) NOT NULL,
     `role_id` int(11) NOT NULL,
     PRIMARY KEY (`user_id`,`role_id`),
     KEY `fk_role_id` (`role_id`),
     CONSTRAINT `fk_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
     CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
   ```

4. 初始化数据

   ```sql
   INSERT INTO `sys_role` VALUES ('1', 'ROLE_ADMIN');
   INSERT INTO `sys_role` VALUES ('2', 'ROLE_USER');
   
   INSERT INTO `sys_user` VALUES ('1', 'admin', '123');
   INSERT INTO `sys_user` VALUES ('2', 'jitwxs', '123');
   
   INSERT INTO `sys_user_role` VALUES ('1', '1');
   INSERT INTO `sys_user_role` VALUES ('2', '2');
   ```

   >这里的权限格式为ROLE_XXX 是Spring Security规定的  名字不能乱起

### 三、准备页面

将页面放在resources/template下

login.html    页面

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>登陆</title>
</head>
<body>
<h1>登陆</h1>
<form method="post" action="/login">
    <div>
        用户名：<input type="text" name="username">
    </div>
    <div>
        密码：<input type="password" name="password">
    </div>
    <div>
        <button type="submit">立即登陆</button>
    </div>
</form>
</body>
</html>
```

>用户的登录认证是由Spring security进行处理的  请求路径默认为 /login，用户名
>
>字段默认为 username  密码字段默认为 password



home.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1>登陆成功</h1>
<a href="/admin">检测ROLE_ADMIN角色</a>
<a href="/user">检测ROLE_USER角色</a>
<button onclick="window.location.href='/logout'">退出登录</button>
</body>
</html>
```



### 四、配置application.properties

在properties文件中配置连接数据库：

```properties
#根据自己的mysql版本  对应的 驱动 格式  填写    (com.mysql.jdbc.Driver)
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/testdb?useUnicode=true&characterEncoding=utf-8&useSSL=true
spring.datasource.username=root
spring.datasource.password=979416

#开启Mybatis下划线命名转驼峰命名
mybatis.configuration.map-underscore-to-camel-case=true
```



### 五、实体类、Dao、Service、和Controller

#### 5.1、实体类

##### SysUser：

```java
    static final long  serialVersionUID=1L;

    private Integer id;
    private String name;
    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

```

>**serialVersionUID**适用于java序列化机制。简单来说，JAVA序列化的机制是通过 判断类的serialVersionUID来验证的版本一致的。在进行反序列化时，JVM会把传来的字节流中的serialVersionUID于本地相应实体类的serialVersionUID进行比较。如果相同说明是一致的，可以进行反序列化，否则会出现反序列化版本一致的异常，即是InvalidCastException。
>
>**具体序列化的过程是这样的：**序列化操作时会把系统当前类的serialVersionUID写入到序列化文件中，当反序列化时系统会自动检测文件中的serialVersionUID，判断它是否与当前类中的serialVersionUID一致。如果一致说明序列化文件的版本与当前类的版本是一样的，可以反序列化成功，否则就失败；



##### SysRole:

```java
static final long  serialVersionUID=1L;

    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
```

##### SysUserRole:

```java
static final long  serialVersionUID=1L;

    private Integer userId;
    private Integer roleId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
```



#### 5.2、Dao

##### SysUserMapper:

```java
@Mapper
public interface SysUserMapper {
    @Select("select * from sys_user where id=#{id}")
    SysUser selectById(Integer id);

    @Select("select * from sys_user where name=#{name}")
    SysUser selectByName(String name);
}
```

##### SysRoleMapper

```java
@Mapper
public interface SysRoleMapper {
    @Select("select *from sys_role where id=#{id}")
    SysRole selectById(Integer id);
}

```

##### SysUserRoleMapper

```java
@Mapper
public interface SysUserRoleMapper {
    @Select("select * from sys_user_role where user_id=#{userId}")
    List<SysUserRole> listByUserID(Integer userId);
}
```



#### 5.3、Service

##### SysUserService

```java
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
```

##### SysRoleService

```java
@Service
public class SysRoleService {
    @Autowired
    private SysRoleMapper roleMapper;

    public SysRole selectById(Integer id){
        return roleMapper.selectById(id);
    }
}
```

##### SysUserRoleService

```java
@Service
public class SysUserRoleService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    public List<SysUserRole> listByUserId(Integer userId){
        return userRoleMapper.listByUserID(userId);
    }
}
```



#### 5.4 Controller

```java
@Controller
public class SysController {
    private Logger logger = (Logger) LoggerFactory.getLogger(SysController.class);

    @RequestMapping("/")
    public String showHome(){
        Authentication name= SecurityContextHolder.getContext().getAuthentication();
        logger.info("当前登录用户:"+name);

        return "home.html";
    }

    @RequestMapping("/login")
    public String showLogin(){
        return "login.html";
    }

    @RequestMapping("/admin")
    @ResponseBody
    @PreAuthorize("hasRole('Role_ADMIN')")
    public String printAdmin(){
        return "这里代表你有ROLE_ADMIN角色";
    }

    @RequestMapping("/user")
    @ResponseBody
    @PreAuthorize("hasRole('Role_USER')")
    public String printUser(){
        return "这里代表你有ROLE_USER角色";
    }
}
```

>- 代码所示  获取当前登录用户
>
>  SecurityContextHolder.getContext().getAuthentication()
>
>-  @PreAuthorize  用于判断用户是否有指定权限 没有就不能访问



### 六、配置SpringSecurity

#### 6.1 UserDetailsService

自定义UserDetailsService  ，将用户信息和权限注入进来

需要重写 `loadUserByUsername` 方法，参数是用户输入的用户名。返回值是`UserDetails`，这是一个接口，一般使用它的子类`org.springframework.security.core.userdetails.User`，它有三个参数，分别是用户名、密码和权限集。

>实际情况下 大多将DAO中的User类继承
>
>org.springframework.security.core.userdetails.User  返回

```java
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
```



#### 6.2 WebSecurityConfig

该类是 Spirng Security 的配置类  

首先 我们自定义的 userDetailsService 注入进来  再configuration 方法中使用 

auth.userDetailsService()方法替换掉默认的userDetailsService

>这里我们还指定了密码的加密方式(5.0版本强制要求设置)  因为我们数据库是铭文存储的 所以 明文返回即可

```java
@Configuration  //标识该类为配置类
@EnableWebSecurity //开启Security服务
@EnableGlobalMethodSecurity(prePostEnabled = true)  //开启全局Security注解
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomUserDeatilsService userDeatilsService;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception{
        //替换掉默认的userDetailService
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
```



### 七、运行程序  测试

