package com.joj.judge.listener;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/5 20:36
 */

public interface CodeSandboxExecuteListener {

    Boolean onCompiling();

    Boolean onRunning();

    void onCompileError();

    void onRuntimeError(Integer timeUsed, Integer memoryUsed);

    void onMemoryLimitExceeded(Integer timeUsed, Integer memoryUsed);

    void onTimeLimitExceeded(Integer timeUsed, Integer memoryUsed);

    void onOutputLimitExceeded(Integer timeUsed, Integer memoryUsed);

    void onSystemError();

}
