package com.joj.problem.submission.service;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/19 19:38
 */

public interface RejudgeBatchService {

    void rejudgeBatch(List<Long> submissionIds);

}
