package com.joj.common.web.bloomfilter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/6/25 00:31
 */

@Component
@RequiredArgsConstructor
public class BloomFilterHelper {

    private final BloomFilterManager bloomFilterManager;

    public void addAfterCommit(BloomFilterTypeEnum typeEnum, Long value) {
        if (value == null || value <= 0) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            bloomFilterManager.add(typeEnum, value);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bloomFilterManager.add(typeEnum, value);
            }
        });
    }
}
