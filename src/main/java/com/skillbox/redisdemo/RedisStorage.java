package com.skillbox.redisdemo;

import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;


public class RedisStorage implements Runnable {

    // Объект для работы с Redis
    private RedissonClient redisson;

    // Объект для работы с ключами
    private RKeys rKeys;

    // Объект для работы с Sorted Set'ом
    private RScoredSortedSet<String> onlineUsers;

    private final static String KEY = "ONLINE_USERS";


    // Пример вывода всех ключей
    public void listKeys() {
        Iterable<String> keys = rKeys.getKeys();
        for (String key : keys) {
            System.out.println(("KEY: " + key + ", type:" + rKeys.getType(key)));
        }
    }

    void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.47:6379");
        try {
            redisson = Redisson.create(config);
        } catch (RedisConnectionException Exc) {
            System.out.println(("Не удалось подключиться к Redis"));
            System.out.println((Exc.getMessage()));
        }
        rKeys = redisson.getKeys();
        rKeys.delete(KEY);
        onlineUsers = redisson.getScoredSortedSet(KEY);
    }

    void shutdown() {
        redisson.shutdown();
    }

    private double getTs() {
        return new Date().getTime() / 1000;
    }

    // Фиксирует посещение пользователем страницы
    void logPageVisit(int user_id) {
        double timestamp = getTs();
        //ZADD ONLINE_USERS
        onlineUsers.add(timestamp, String.valueOf(user_id));
    }

    // Удаляет
    void deleteOldEntries(int secondsAgo) {
        //ZREVRANGEBYSCORE ONLINE_USERS 0 <time_5_seconds_ago>
        onlineUsers.removeRangeByScore(0, true, getTs() - secondsAgo, true);


    }

    int calculateUsersNumber() {
        //ZCOUNT ONLINE_USERS
        return onlineUsers.count(Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY, true);
    }

    public void getUserScores() {
        //ZSCORE ONLINE_USERS
        Iterator<String> users = onlineUsers.iterator();

    }

    public synchronized void listUsers(String user) {
            System.out.println("- На главной странице показываем пользователя " + user);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    public synchronized void topUserDonate(String user) {
        Random random = new Random();
        System.out.println("\t-> Пользователь " + user + " оплатил vip услугу");
        double userScore = onlineUsers.getScore(user);
        double lastUserScore = onlineUsers.lastScore();


        if (random.nextInt(100) > 10) {
            onlineUsers.addScore(user, userScore+lastUserScore);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public RScoredSortedSet<String> getOnlineUsers() {
        return onlineUsers;
    }

    @Override
    public void run() {

    }
}