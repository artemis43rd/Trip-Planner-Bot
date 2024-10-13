package com.telegrambot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.component.*;

import java.util.Collection;

@Configuration
@PropertySource("classpath:bot.properties")
@ComponentScan(basePackages = "com.telegrambot")
public class TelegramConfig {

    @Autowired
    Environment env;

    @Autowired
    StartCommand start;

    @Autowired
    HelpCommand help;

    @Autowired
    ScoreCommand show_score;

    @Autowired
    AddTripCommand create_trip;

    @Autowired
    ShowTripListCommand show_trips;

    @Autowired
    DeleteTripCommand delete_trip;

    @Autowired
    ShowTripDetCommand show_trip_detail;

    @Autowired
    AddPointCommand delete_point;

    @Autowired
    DeletePointCommand create_point;

    @Autowired
    TelegramBot bot;

    @Bean
    public Collection<IBotCommand> getRegistry(){
        return bot.getRegisteredCommands();
    }

    @Bean
    public BotSession sessionStart(TelegramBotsLongPollingApplication botsApplication, TelegramBot bot) throws TelegramApiException {
        bot.register(start);
        bot.register(help);
        bot.register(show_score);
        bot.register(create_trip);
        bot.register(show_trips);
        bot.register(delete_trip);
        bot.register(show_trip_detail);
        bot.register(delete_point);
        bot.register(create_point);
        return botsApplication.registerBot(env.getProperty("token"), bot);
    }

    @Bean
    public TelegramBotsLongPollingApplication application() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(env.getProperty("token"));
    }
}
