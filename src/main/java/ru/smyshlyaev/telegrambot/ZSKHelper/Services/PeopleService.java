package ru.smyshlyaev.telegrambot.ZSKHelper.Services;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.Message;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.People;
import ru.smyshlyaev.telegrambot.ZSKHelper.Repositories.PeopleRepository;
import ru.smyshlyaev.telegrambot.ZSKHelper.Utils.MessageSenderException;
import ru.smyshlyaev.telegrambot.ZSKHelper.Utils.RegistrationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PeopleService {
    private final PeopleRepository peopleRepository;

    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Transactional
    public void saveUser(long telegramId,String fullName,String userName) throws RegistrationException {

        Optional<People> person= Optional.ofNullable(peopleRepository.findPeopleByTelegramId(telegramId));

        if(person.isEmpty()) {
            People user = new People(telegramId,fullName,userName);
            peopleRepository.save(user);
        }
        else if(person.isPresent()) {
           throw new RegistrationException("Пользователь уже существует в базе данных");
        }

    }
    @Transactional
    public Optional<People> findOneUser(long telegramId) throws MessageSenderException {
        Optional<People> finderPeople= Optional.of(new People());
        Optional<People> person= Optional.ofNullable(peopleRepository.findPeopleByTelegramId(telegramId));

        if(person.isPresent()) {
            finderPeople=person;
        }
        else if(person.isEmpty()) {
            throw new MessageSenderException("Сообщение не отправлено! Для успешной отпраки сообщения необходимо зарегистрироваться");
        }
        Hibernate.initialize(finderPeople.get());
        return finderPeople;
    }
@Transactional
    public List<Message> findMessageTelegramId(long telegramId) {
        Optional<People> person= Optional.ofNullable(peopleRepository.findPeopleByTelegramId(telegramId));

        if (person.isPresent()) {
            Hibernate.initialize(person.get().getMessages());
            return person.get().getMessages();
        }

        else {
            return Collections.emptyList();
        }
    }


}

