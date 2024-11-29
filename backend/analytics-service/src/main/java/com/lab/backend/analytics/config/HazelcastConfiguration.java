package com.lab.backend.analytics.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {
    @Bean
    public Config hazelcastConfig() {
        return new Config()
                .setInstanceName("hazelcast-instance")
                .addMapConfig(new MapConfig()
                        .setName("weeklyStatsCache")
                        .setTimeToLiveSeconds(3600));
    }

    @Bean
    public CacheManager cacheManager() {
        return new HazelcastCacheManager(com.hazelcast.core.Hazelcast.newHazelcastInstance(hazelcastConfig()));
    }
}
