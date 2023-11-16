package ru.smyshlyaev.telegrambot.ZSKHelper.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findMessageBySenderMessage(long userId);
}
