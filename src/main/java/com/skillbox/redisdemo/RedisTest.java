package com.skillbox.redisdemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RedisTest {

    // Запуск докер-контейнера:
    // docker run --rm --name skill-redis -p 127.0.0.1:6379:6379/tcp -d redis

    // Для теста будем считать неактивными пользователей, которые не заходили 2 секунды
    private static final int DELETE_SECONDS_AGO = 20;

    // Допустим пользователи делают 500 запросов к сайту в секунду
    private static final int RPS = 500;

    // И всего на сайт заходило 1000 различных пользователей
    private static final int USERS = 20;

    // Также мы добавим задержку между посещениями
    private static final int SLEEP = 10; // 1 миллисекунда

    private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss");

    private static void log(int userId) {
        String log = String.format("[%s] Пользователь %d онлайн ", DF.format(new Date()), userId);
        System.out.println(log);
    }

    public static void main(String[] args) throws InterruptedException {

        RedisStorage redis = new RedisStorage();
        redis.init();
        // Эмулируем 10 секунд работы сайта
        int userCounter = 1;
        while (userCounter <= USERS) {
            redis.logPageVisit(userCounter);
            Thread.sleep(SLEEP);
            log(userCounter++);
        }
/*        new Thread(redis::listUsers).start();

        new Thread(() -> {
            Random randomUser = new Random();
            redis.topUserDonate(String.valueOf(randomUser.nextInt(9) + 1));
        }).start();*/

        new Thread(() -> {
            for(;;) {
                for (String user : redis.getOnlineUsers()) {
                    redis.listUsers(user);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(() -> {
            Random r1 = new Random();
            for(;;) {
                if (r1.nextInt(100) > 60)
                    redis.topUserDonate(String.valueOf(r1.nextInt(19) + 1));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


//        redis.shutdown();
    }
}