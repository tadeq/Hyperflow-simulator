package com.mmoskal.hyperflowsimulator.client;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class RedisTaskResolveClient {

    private final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

    public void resolveTask(String wfId, String taskId) {
        new Thread(() -> {
            Jedis jedis = jedisPool.getResource();
            jedis.sadd("wf:" + wfId + ":tasksPendingCompletionHandling", taskId);
            System.out.println("SEND RESOLVE: " + wfId + ", " + taskId);
            jedis.close();
        }).start();
    }
}
