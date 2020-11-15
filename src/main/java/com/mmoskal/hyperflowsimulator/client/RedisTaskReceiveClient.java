package com.mmoskal.hyperflowsimulator.client;

import com.mmoskal.hyperflowsimulator.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

@Component
public class RedisTaskReceiveClient {

    private final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

    private final SimulationService simulationService;

    @Autowired
    public RedisTaskReceiveClient(SimulationService simulationService) {
        this.simulationService = simulationService;
        subscribe();
    }

    private void subscribe() {
        new Thread(() -> {
            Jedis subscribeRedisClient = jedisPool.getResource();
            subscribeRedisClient.configSet("notify-keyspace-events", "El");
            JedisPubSub jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    Jedis rpopRedisClient = jedisPool.getResource();
                    simulationService.addTask(rpopRedisClient.rpop(message));
                    rpopRedisClient.close();
                }
            };
            subscribeRedisClient.subscribe(jedisPubSub, "__keyevent@0__:lpush");
        }).start();
    }
}
