package cn.itcast.config;

import cn.itcast.service.impl.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
/**
 * springboot整合security里面的重要配置类相当于之前的security的配置文件
 */
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //表单登录代表不需要验证可直接访问
        //允许所有用户访问"/"和"/home"
        //允许所有用户访问"/"和"/home"
        http
                .formLogin().loginPage("/login").loginProcessingUrl("/login/form").failureUrl("/login-error").permitAll()  //表单登录，permitAll()表示这个不需要验证 登录页面，登录失败页面
                .and()
                .authorizeRequests()
                .antMatchers("/pages/system/**").hasRole("ADMIN")
                .antMatchers("/pages/base/**").hasRole("USER")
                .anyRequest()
                .authenticated()//需要登录
                .and()
                .logout()
                .logoutUrl("/logout/form")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)//清除session
                .permitAll()
                .and()
                .csrf().disable();

//        http.authorizeRequests()
//                .antMatchers("/login", "/home").permitAll()
//                //其他地址的访问均需验证权限
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                //指定登录页是"/login"
//                .loginPage("/login")
//                .loginProcessingUrl("/login/form")
//                .defaultSuccessUrl("/index")//登录成功后默认跳转到"/index"
//                .failureUrl("/login-error")
//                .permitAll()
//                .and()
//                .logout()
//                .logoutSuccessUrl("/logout")//退出登录后的默认url是"/logout"
//                .permitAll();

    }

    //    /**
//     * 忽略静态资源不被拦截
//     *
//     * @param web
//     * @throws Exception
//     */

    @Autowired
    private AuthenticationProvider provider;  //注入我们自己的AuthenticationProvider

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/css/**", "/js/**", "/images/**", "/pages/**", "/home.html", "/login.html", "/logout");

    }

    //
//    /**
//     * spring容器的管理
//     *
//     * @return
//     * @throws Exception
//     */
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        AuthenticationManager manager = super.authenticationManagerBean();
//        return manager;
//    }
//
    /**
     * 加密方式
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//
//    /**
//     * 自定义UserDetailsService实现用户的认证和授权
//     *
//     * @param auth
//     * @throws Exception
//     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(myUserDetailsService()).passwordEncoder(new BCryptPasswordEncoder());
        auth.authenticationProvider(provider);
       // inMemoryAuthentication 从内存中获取
//        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder()).withUser("admin").password(new BCryptPasswordEncoder().encode("123")).roles("ADMIN")
//        .and().withUser("user1").password(new BCryptPasswordEncoder().encode("456")).roles("USER")
//        .and().withUser("user2").password(new BCryptPasswordEncoder().encode("666")).roles("ROOT");
    }



    /**
     * 自定义UserDetailsService，授权
     *
     * @return
     */
    @Bean
    public MyUserDetailsService myUserDetailsService() {
        return new MyUserDetailsService();

    }
}
