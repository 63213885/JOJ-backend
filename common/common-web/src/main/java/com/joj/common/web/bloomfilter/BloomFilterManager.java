package com.joj.common.web.bloomfilter;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/6/25 00:26
 */

@Component
@RequiredArgsConstructor
public class BloomFilterManager {

    private final RedissonClient redissonClient;

    public RBloomFilter<Long> getBloomFilter(BloomFilterTypeEnum typeEnum) {
        return redissonClient.getBloomFilter(typeEnum.getKey());
    }

    public void init(BloomFilterTypeEnum typeEnum) {
        getBloomFilter(typeEnum).tryInit(
                typeEnum.getExpectedInsertions(),
                typeEnum.getFalseProbability()
        );
    }

    public boolean mightContain(BloomFilterTypeEnum typeEnum, Long value) {
        if (value == null || value <= 0) {
            return false;
        }

        return getBloomFilter(typeEnum).contains(value);
    }

    public void add(BloomFilterTypeEnum typeEnum, Long value) {
        if (value == null || value <= 0) {
            return;
        }

        getBloomFilter(typeEnum).add(value);
    }

    public void addAll(BloomFilterTypeEnum typeEnum, Collection<Long> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        RBloomFilter<Long> bloomFilter = getBloomFilter(typeEnum);
        for (Long value : values) {
            if (value != null && value > 0) {
                bloomFilter.add(value);
            }
        }
    }
}
