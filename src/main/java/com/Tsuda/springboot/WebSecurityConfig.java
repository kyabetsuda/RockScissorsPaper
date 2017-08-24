package com.Tsuda.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled=true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserInfoService userInfoService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
          // アクセス権限の設定
          // 制限なしのurl
          .antMatchers("/css/**", "/fonts/**", "/js/**", "/", "/register").permitAll()
          // '/admin/'で始まるURLには、'ADMIN'ロールのみアクセス可
          .antMatchers("/admin/**").hasRole("ADMIN")
          // 他は制限なし
          .anyRequest().authenticated()
        .and()
          //csrfを無効化
          .csrf()
          .disable()
          // ログイン処理の設定
          .formLogin()
            // ログイン処理のURL
            .loginPage("/login")
            //ログイン成功時の遷移先url
            .defaultSuccessUrl("/", false)
            // usernameのパラメタ名
            .usernameParameter("name")
            // passwordのパラメタ名
            .passwordParameter("password")
            .permitAll()
        .and()
          // ログアウト処理の設定
          .logout()
            // ログアウト処理のURL
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            // ログアウト成功時の遷移先URL
            .logoutSuccessUrl("/log_out")
            // ログアウト時に削除するクッキー名
            .deleteCookies("JSESSIONID")
            // ログアウト時のセッション破棄を有効化
            .invalidateHttpSession(true)
            .permitAll();
    http
    	  //多重ログイン禁止
         .sessionManagement()
           .maximumSessions(1)
           .maxSessionsPreventsLogin(true)
           .sessionRegistry(sessionRegistry());
        ;
  }

  @Bean
  public SessionRegistry sessionRegistry() {
      SessionRegistry sessionRegistry = new SessionRegistryImpl();
      return sessionRegistry;
  }

  @Bean
  public static ServletListenerRegistrationBean httpSessionEventPublisher() {
      return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userInfoService);
 }
}
