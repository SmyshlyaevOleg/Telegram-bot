package ru.smyshlyaev.telegrambot.ZSKHelper.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.People;

@Repository
public interface PeopleRepository extends JpaRepository<People,Long> {

     People findPeopleByTelegramId(long userId);
}

