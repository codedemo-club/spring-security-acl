package club.codedemo.springsecurityacl.service;

import club.codedemo.springsecurityacl.entity.Message;
import club.codedemo.springsecurityacl.repository.MessageRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    final
    MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Message> findAll() {
        List<Message> messages = this.messageRepository.findAll();
        return messages;
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    Message findById(Long id) {
        return this.messageRepository.findById(id).orElse(null);
    }

    @PreAuthorize("hasPermission(#message, 'WRITE')")
    Message save(@Param("message") Message message) {
        return this.messageRepository.save(message);
    }
}
