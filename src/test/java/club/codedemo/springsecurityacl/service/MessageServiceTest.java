package club.codedemo.springsecurityacl.service;

import club.codedemo.springsecurityacl.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageServiceTest {
    @Autowired
    MessageService messageService;

    @Test
    @WithMockUser(username = "zhangsan")
    void findAll() {
        List<Message> messages = this.messageService.findAll();
        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals(1, messages.get(0).getId());
    }

    @Test
    void findById() {
    }

    @Test
    void save() {
    }
}