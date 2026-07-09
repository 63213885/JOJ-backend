package com.joj.problem.bloomfilter;

import com.joj.common.web.bloomfilter.BloomFilterManager;
import com.joj.common.web.bloomfilter.BloomFilterTypeEnum;
import com.joj.problem.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/6/25 00:36
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemBloomLoader implements ApplicationRunner {

    private final BloomFilterManager bloomFilterManager;

    private final ProblemService problemService;

    @Override
    public void run(ApplicationArguments args) {
        bloomFilterManager.init(BloomFilterTypeEnum.PROBLEM_ID);

        long lastId = 0L;
        int limit = 1000;
        long total = 0L;

        while (true) {
            List<Long> ids = problemService.listIds(lastId, limit);
            if (ids == null || ids.isEmpty()) {
                break;
            }

            bloomFilterManager.addAll(BloomFilterTypeEnum.PROBLEM_ID, ids);

            total += ids.size();
            lastId = ids.get(ids.size() - 1);
        }

        log.info("题目ID布隆过滤器加载完成，total = {}", total);
    }
}
