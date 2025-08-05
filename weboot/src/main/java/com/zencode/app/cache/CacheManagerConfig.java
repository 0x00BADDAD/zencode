import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;





@Configuration
@EnableCaching
public class CacheManagerConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeine1 = Caffeine.newBuilder()
            .expireAfterWrite(1800, TimeUnit.SECONDS)
            .maximumSize(500);

        Caffeine<Object, Object> caffeine2 = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.DAYS)
            .maximumSize(500);

        CaffeineCache accessTokenCache = new CaffeineCache("accessTokenCache", caffeine1.build());
        CaffeineCache refreshTokenCache = new CaffeineCache("refreshTokenCache", caffeine2.build());
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(accessTokenCache, refreshTokenCache));
        return manager;
    }
}

