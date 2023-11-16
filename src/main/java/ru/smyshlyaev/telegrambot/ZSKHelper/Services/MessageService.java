package ru.smyshlyaev.telegrambot.ZSKHelper.Services;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.Message;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.People;
import ru.smyshlyaev.telegrambot.ZSKHelper.Repositories.MessageRepository;
import ru.smyshlyaev.telegrambot.ZSKHelper.Repositories.PeopleRepository;
import ru.smyshlyaev.telegrambot.ZSKHelper.Utils.MessageSenderException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service

public class MessageService {
    private final MessageRepository messageRepository;
    private final PeopleRepository peopleRepository;
    private static final Logger LOGGER=Logger.getLogger(People.class);


@Autowired
    public MessageService(MessageRepository messageRepository, PeopleRepository peopleRepository) {
    this.messageRepository = messageRepository;
    this.peopleRepository = peopleRepository;

}
    @Transactional
    public List<Message> findAllMessage(long userId) {
        List<Message> messageList=new ArrayList<>();
        messageList=messageRepository.findMessageBySenderMessage(userId);
        return messageList;
    }

    @Transactional
    public Optional<Optional<Message>> findOneMessage(long id) throws MessageSenderException {

        Optional<Optional<Message>> findMessage= Optional.of(Optional.of(new Message()));
        Optional<Optional<Message>> message= Optional.ofNullable(messageRepository.findById(id));

        if(message.isPresent()) {
            findMessage=message;
        }
        else if(message.isEmpty()) {
            throw new MessageSenderException("Сообщение не отправлено! Для успешной отпраки сообщения необходимо зарегистрироваться");
        }
        Hibernate.initialize(findMessage.get());
        return findMessage;
    }


    @Transactional
    public void updateMessage(String response, long id) {
    Optional<Message> updateMessage=messageRepository.findById(id);
        updateMessage.get().setId((int) id);
        updateMessage.get().setStatus(response);
        saveMessage(String.valueOf(updateMessage),id);


    }
    @Transactional
    public void saveMessage(String message, long userId) {
        Message newMessage= new Message();

            People senderMessage = peopleRepository.findPeopleByTelegramId(userId);

            newMessage.setSenderMessage(senderMessage);
            LOGGER.info(senderMessage.getUserName()+ " " + senderMessage.getTelegramId());
            newMessage.setSenderName(senderMessage.getFullName());
            newMessage.setMessage(message);
            newMessage.setCreatedAt(LocalDateTime.now());
            newMessage.setStatus("На рассмотрении");

            messageRepository.save(newMessage);

        }
    @Transactional
        public void deleteAllMessage(){
        messageRepository.deleteAll();
        }


    }


