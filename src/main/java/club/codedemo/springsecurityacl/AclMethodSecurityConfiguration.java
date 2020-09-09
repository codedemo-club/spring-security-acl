package club.codedemo.springsecurityacl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;

@Configuration
@EnableCaching
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AclMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    final
    DataSource dataSource;

    final
    CacheManager cacheManager;

    public AclMethodSecurityConfiguration(DataSource dataSource, CacheManager cacheManager) {
        this.dataSource = dataSource;
        this.cacheManager = cacheManager;
    }

    /**
     * 在处理方法上的安全表达式注解时
     * 使用此方法返回的方法验证表达式
     *
     * @return
     */
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler
                = new DefaultMethodSecurityExpressionHandler();
        AclPermissionEvaluator permissionEvaluator
                = new AclPermissionEvaluator(this.aclService());
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    /**
     * 当前用户是否拥有某些对象的必需权限
     *
     * @return
     */
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public SpringCacheBasedAclCache aclCache() {
        return new SpringCacheBasedAclCache(
                this.cacheManager.getCache("acl"),
                this.permissionGrantingStrategy(),
                this.aclAuthorizationStrategy()
        );
    }

    public LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(
                dataSource,
                this.aclCache(),
                this.aclAuthorizationStrategy(),
                new ConsoleAuditLogger()
        );
    }

    public JdbcMutableAclService aclService() {
        return new JdbcMutableAclService(
                dataSource, this.lookupStrategy(), this.aclCache()
        );
    }

}
