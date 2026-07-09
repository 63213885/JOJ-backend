package com.joj.user.bloomfilter;

import com.joj.common.core.model.enums.SortOrderEnum;
import com.joj.common.web.bloomfilter.BloomFilterManager;
import com.joj.common.web.bloomfilter.BloomFilterTypeEnum;
import com.joj.user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/6/25 00:34
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBloomLoader implements ApplicationRunner {

    private final BloomFilterManager bloomFilterManager;

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        bloomFilterManager.init(BloomFilterTypeEnum.USER_ID);

        long lastId = 0L;
        long limit = 1000L;
        long total = 0L;

        while (true) {
            List<Long> ids = userService.listIds(lastId, limit, null, null);
            if (ids == null || ids.isEmpty()) {
                break;
            }

            bloomFilterManager.addAll(BloomFilterTypeEnum.USER_ID, ids);

            total += ids.size();
            lastId = ids.get(ids.size() - 1);
        }

        log.info("用户ID布隆过滤器加载完成，total = {}", total);
    }
}
