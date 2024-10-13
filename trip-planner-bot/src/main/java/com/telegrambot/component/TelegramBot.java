package com.telegrambot.component;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.telegrambot.entity.Point;
import com.telegrambot.entity.Trip;
import com.telegrambot.service.PointService;
import com.telegrambot.service.TripService;
import com.telegrambot.service.GeocodingService; // Импортируем новый сервис

@PropertySource("classpath:bot.properties")
@Component
public class TelegramBot extends CommandLongPollingTelegramBot {

    @Value("${api-key}")
    private String apiKey;

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final ConcurrentHashMap<Integer, Long> usersChats;

    private final ScheduledExecutorService scheduler;

    private static final int BACKGROUND_TASK_PERIOD = 12;

    private final TripService tripService;

    private final PointService pointService;

    public TelegramBot(TelegramClient client, @Value("${bot.name}") String botName, TripService tripService, PointService pointService) {
        super(client, true, () -> botName);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::backgroundTasks, 0, BACKGROUND_TASK_PERIOD, TimeUnit.HOURS);
        usersChats = new ConcurrentHashMap<>();
        this.tripService = tripService;
        this.pointService = pointService;
    }

    private void backgroundTasks() {
        logger.info("Update background tasks.");
        checkForDeadlines();
        checkTripState();
    }

    private void checkForDeadlines() {
        List<Trip> trips = tripService.getTripsWithProgress("planned");
        trips.addAll(tripService.getTripsWithProgress("in-progress"));

        for (Trip trip : trips) {
            List<Point> points = tripService.getPointsByTrip(trip.getNameTrip(), trip.getUser().getTelegramId());

            for (Point point : points) {
                long hoursUntilPoint = Duration.between(LocalDateTime.now(), point.getPointDate().toLocalDateTime()).toHours();

                if (hoursUntilPoint <= 24 && hoursUntilPoint > 0) {
                    Long chatId = usersChats.get(trip.getUser().getTelegramId().intValue());

                    if (chatId != null) {
                        String message = "Reminder: Less than " + hoursUntilPoint +" hours left until the point \"" 
                                        + point.getNamePoint() + "\" in your trip \"" 
                                        + trip.getNameTrip() + "\".";
                        sendResponse(chatId, message);
                    }
                }
            }
        }
    }

    private void checkTripState() {
        List<Trip> trips = tripService.getTripsWithProgress("planned");

        for (Trip trip : trips) {
            List<Point> points = tripService.getPointsByTrip(trip.getNameTrip(), trip.getUser().getTelegramId());
            boolean hasFutureDate = false;

            if (points.size() == 0) { break; }
            for (Point point : points) {
                if (point.getPointDate().toLocalDateTime().isAfter(LocalDateTime.now())) {
                    hasFutureDate = true;
                    break;
                }
            }

            if (!hasFutureDate) {
                trip.setProgress("in-progress");
                tripService.saveTrip(trip);
            }
        }

        trips = tripService.getTripsWithProgress("in-progress");

        for (Trip trip : trips) {
            List<Point> points = tripService.getPointsByTrip(trip.getNameTrip(), trip.getUser().getTelegramId());
            boolean allPointsPast = true;

            if (points.size() == 0) { break; }
            for (Point point : points) {
                if (point.getPointDate().toLocalDateTime().isAfter(LocalDateTime.now())) {
                    allPointsPast = false;
                    break;
                }
            }

            if (allPointsPast) {
                trip.setProgress("finished");
                tripService.saveTrip(trip);
            }
        }
    }

    @Override
    public boolean filter(Message message) {
        logger.info("Got message: " + message.getText() + " from User: " + message.getFrom().getUserName() + " (" + message.getFrom().getId() + ")");
        usersChats.put(message.getFrom().getId().intValue(), message.getChatId());
        return super.filter(message);
    }

    @Override
    public void processInvalidCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Сообщение о некорректной команде
            sendResponse(chatId, "Invalid command: " + messageText +
            " Try /help to see the possible commands");
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();

            if (update.getMessage().hasLocation()) {
                // Обработка геопозиции
                double latitude = update.getMessage().getLocation().getLatitude();
                double longitude = update.getMessage().getLocation().getLongitude();

                // Обработка координат
                if (handleCoordinates(chatId, latitude, longitude)) { return; }
            }

            if (update.getMessage().hasText()) {
                sendResponse(chatId, "I'm just a bot that will help you record the details of your trip, I can't reply to messages that aren't commands.\n\n" +
                    "If you want to know what I can do, write /help.");
            }
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long telegramId = update.getCallbackQuery().getFrom().getId();

            switch (callbackData) {
                case "showTrips:1":
                    sendTrips(chatId, "planned");
                    break;
                case "showTrips:2":
                    sendTrips(chatId, "in-progress");
                    break;
                case "showTrips:3":
                    sendTrips(chatId, "finished");
                    break;
                case "showTrips:4":
                    sendTrips(chatId, "all");
                    break;
                case "addPoint:confirm":
                    sendResponse(chatId, "The point was successfully added");
                    break;
                default:
                    if (callbackData.startsWith("addP:c")) {
                        String[] details = callbackData.substring(6).split(";");
                        pointService.deletePoint(details[0], details[1],  details[2], telegramId);
                        sendResponse(chatId, "Adding a point has been canceled");
                        break;
                    }
                    sendResponse(chatId, "Unknown action: " + callbackData);
                    break;
            }
            return;
        }
    }

    private boolean handleCoordinates(Long chatId, double latitude, double longitude) {
        try {
            // Создаём экземпляр GeocodingService
            GeocodingService geocodingService = new GeocodingService(apiKey);
            String cityName = geocodingService.getCityName(latitude, longitude);

            // Проверяем trip со статусом in-progress
            checkTripWithCoordinates(chatId, cityName);
            return true; // Успешно обработано

        } catch (Exception e) {
            sendResponse(chatId, "Error processing location data. Please try again.");
            return true;
        }
    }

    private void sendTrips(Long chatId, String progress) {
        List<Trip> trips = tripService.getTripsByProgress(progress, chatId);
        if (trips.isEmpty()) {
            sendResponse(chatId, "No trips found with progress: " + progress);
        } else {
            StringBuilder response;
            if (progress == "all") {
                response = new StringBuilder("All trips:\n");
            } else {
                response = new StringBuilder("Trips with progress \'" + progress + "\':\n");
            }

            for (Trip trip : trips) {
                response.append("- ").append(trip.getNameTrip()).append("\n");
            }
            sendResponse(chatId, response.toString());
        }
    }

    // Вспомогательный метод для отправки сообщения
    private void sendResponse(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message: " + e.getMessage(), e);
        }
    }

    private void checkTripWithCoordinates(Long chatId, String pointName) {
        if (pointName == "") {
            String message = "The locality where you are located has not been found . Try /set_visited_state";
            sendResponse(chatId, message);
            return;
        }

        List<Trip> trips = tripService.getTripsWithProgress("in-progress");

        for (Trip trip : trips) {
            List<Point> points = tripService.getPointsByTrip(trip.getNameTrip(), trip.getUser().getTelegramId());

            for (Point point : points) {
                if (point.getNamePoint().equals(pointName)) {
                    if (point.getVisited() == true) {
                        String message = pointName + " is already marked as visited";
                        sendResponse(chatId, message);
                        return;
                    }
                    point.setVisited(true);
                    pointService.savePoint(point);
                    String message = pointName + " marked as visited";
                    sendResponse(chatId, message);
                    return;
                }
            }
        }
        String message = "Can't find point " + pointName + " in your \"in-progress\" trips. Try /set_visited_state";
        sendResponse(chatId, message);
    }
}
