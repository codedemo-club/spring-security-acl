package club.codedemo.springsecurityacl;

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

/**
 * 2.3 相关配置
 * @author panjie
 */
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

    /**BasePermission
     * 用于验证Spring Security权限注解
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

    /**
     * 判断某用户/角色是否有对某个资源有某项访问权限的策略
     * 这里使用默认策略，表示：
     * 根据acl_entry表中的记录做判断
     * 该表中有个ace_order字段，在进行权限判断时，会按该字段进行排序。
     * 然后进行遍历。
     * 如果找到了granting为1的记录，则不再遍历而返回true(有权限）
     * 否则会继续遍历下一条，直接遍历到granting为1记录或是遍历完毕为止。
     * 如果没有遍历到granting为1的记录，则将返回首条granting为0的记录中的原因（audit_failure)做为无访问权限的原因返回
     *
     * 在构造函数中传入的new ConsoleAuditLogger()作用是：在控制台上直接打印权限判断的结果。
     * 此时将校验通过或未通过时，将在控制台看到相应的校验结果。
     *
     * 想了解更多详细信息，可参考：https://docs.spring.io/spring-security/site/docs/4.2.15.RELEASE/apidocs/org/springframework/security/acls/domain/DefaultPermissionGrantingStrategy.html
     *
     * @return
     */
    private PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    /**
     * 设置谁可以管理ACL控制策略，即设置ACL的管理员。
     * AclAuthorizationStrategyImpl将管理权限细分为3种。
     * 当传入1个参数时，3种管理权限将统一赋值为该参数。
     * 除此此外，还可以传入3个参数分别对3种管理权限进行配置。
     *
     * 更多详情可参考：https://docs.spring.io/spring-security/site/docs/4.2.15.RELEASE/apidocs/org/springframework/security/acls/domain/AclAuthorizationStrategyImpl.html
     *
     * @return
     */
    private AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /**
     * Acl会被频繁访问，所以设置缓存相当有必要
     *
     * @return
     */
    private SpringCacheBasedAclCache aclCache() {
        return new SpringCacheBasedAclCache(
                this.cacheManager.getCache("acl"),
                this.permissionGrantingStrategy(),
                this.aclAuthorizationStrategy()
        );
    }

    /**
     * LookupStrategy主要提供两个功能：
     * 1. lookupPrimaryKeys 查找资源的主健
     * 2. lookupObjectIdentities 根据资源主键、资源对应的Class，近而查找资源对应的acl_object_identity中的主键
     * 该acl_object_identity主键将被PermissionGrantingStrategy调用，用于在acl_entry查找对应权限策略
     *
     * @return
     */
    private LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(
                dataSource,
                this.aclCache(),
                this.aclAuthorizationStrategy(),
                new ConsoleAuditLogger()
        );
    }

    /**
     * 基于数据源的ACL权限控制服务
     * dataSource 数据源
     * lookupStrategy 查找实体ID，实体对应的CLASS，应该此两项在acl_object_identity中的记录
     * aclCache 根据acl_object_identity记录、当前登录用户/角色，查找acl_entry表，最终获取相应的权限
     * @return
     */
    private JdbcMutableAclService aclService() {
        return new JdbcMutableAclService(
                dataSource, this.lookupStrategy(), this.aclCache()
        );
    }
}
