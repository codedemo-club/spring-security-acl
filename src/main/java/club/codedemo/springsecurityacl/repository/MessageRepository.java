package club.codedemo.springsecurityacl.repository;

import club.codedemo.springsecurityacl.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
