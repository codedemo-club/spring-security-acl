-- 新增3条测试消息
INSERT INTO message(id, content) VALUES
(1, '第一条给张三的消息'),
(2, '第二条给李四消息'),
(3, '第三条给王五的消息');

-- 建立两个用户zhangsan, lisi，一个角色ROLE_ADMIN
INSERT INTO acl_sid (id, principal, sid) VALUES
  (1, 1, 'zhangsan'),
  (2, 1, 'lisi'),
  (3, 0, 'ROLE_ADMIN');

-- 建立实体类映射
INSERT INTO acl_class (id, class) VALUES
  (1, 'club.codedemo.springsecurityacl.entity.Message');

-- 创建ACL基表，用于关联实体类中的id。实际使用中的权限策略将关联此基表。
INSERT INTO acl_object_identity
(id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES
-- id为1的message的拥有者为1号zhangsan(注意：拥有者是谁并不影响本文中的权限判断)
(1, 1, 1, NULL, 1, 0),
-- id为2的message的拥有者为2号lisi(注意：拥有者是谁并不影响本文中的权限判断)
(2, 1, 2, NULL, 2, 0),
-- id为3的message的拥有者为3号ROLE_EDITOR(注意：拥有者是谁并不影响本文中的权限判断)
(3, 1, 3, NULL, 3, 0);

-- BasePermission 权限策略依赖于此表。
INSERT INTO acl_entry
(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
VALUES
-- 1号zhangsan用户对消息1拥有read读权限
(1, 1, 1, 1, 1, 1, 1, 1),

-- 1号zhangsan用户对消息1拥有write写权限
(2, 1, 2, 1, 2, 1, 1, 1),

-- 3号ROLE_EDITOR角色对消息1拥有read读权限
(3, 1, 3, 3, 1, 1, 1, 1),

-- 2号lisi用户对消息2拥有read读权限
(4, 2, 1, 2, 1, 1, 1, 1),

-- 3号ROLE_EDITOR角色对消息2拥有read读权限
(5, 2, 2, 3, 1, 1, 1, 1),

-- 3号ROLE_EDITOR角色对消息3拥有read+write读写权限
(6, 3, 1, 3, 1, 1, 1, 1),
(7, 3, 2, 3, 2, 1, 1, 1);