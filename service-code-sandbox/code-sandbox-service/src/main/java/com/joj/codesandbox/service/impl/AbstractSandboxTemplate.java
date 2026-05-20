package com.joj.codesandbox.service.impl;


import com.joj.codesandbox.model.SandboxContext;
import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;
import com.joj.codesandbox.service.CodeSandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 19:00
 */

/**
 * 沙箱模板类
 *
 * 负责通用流程：
 * 1. compile：生成 sandboxId、准备环境、执行编译、保存上下文
 * 2. run：根据 sandboxId 找上下文，然后运行
 * 3. deleteFile：根据 sandboxId 删除容器和临时文件
 *
 * 具体 Docker 操作交给子类实现。
 */
@Slf4j
public abstract class AbstractSandboxTemplate implements CodeSandbox {

    protected final Map<String, SandboxContext> contextMap = new ConcurrentHashMap<>();

    /**
     * 准备沙箱环境：保存代码、设置语言配置、创建容器
     */
    protected abstract void prepare(SandboxContext context, CompileRequest compileRequest);

    /**
     * 执行编译命令
     */
    protected abstract CompileResponse doCompile(SandboxContext context);


    @Override
    public final CompileResponse compile(CompileRequest compileRequest) {
        String sandboxId = UUID.randomUUID().toString();

        SandboxContext context = SandboxContext.builder()
                .sandboxId(sandboxId)
                .build();

        try {
            prepare(context, compileRequest);

            CompileResponse compileMessage = doCompile(context);

            if (compileMessage.getSuccess()) {
                contextMap.put(sandboxId, context);
            } else {
                safeDelete(context);
            }

            return compileMessage;

        } catch (Throwable e) {
            // 捕获 prepare 或 doCompile 阶段的异常
            log.error("编译异常，sandboxId={}", sandboxId, e);
            safeDelete(context);
            return CompileResponse.builder()
                    .sandboxId(sandboxId)
                    .success(false)
                    .message(StringUtils.hasText(e.getMessage()) ? e.getMessage() : "编译异常")
                    .build();
        }
    }


    /**
     * 执行运行命令
     */
    protected abstract RunResponse doRun(SandboxContext context, RunRequest runRequest);

    @Override
    public final RunResponse run(RunRequest runRequest) {
        // 根据 sandboxId 获取编译阶段保存的上下文
        SandboxContext context = contextMap.get(runRequest.getSandboxId());

        // 找不到上下文，说明未编译成功、已删除，或者 sandboxId 错误
        if (context == null) {
            return RunResponse.builder()
                    .success(false)
                    .message("沙箱不存在或已删除")
                    .build();
        }

        try {
            // 子类执行真正的运行逻辑
            return doRun(context, runRequest);

        } catch (Throwable e) {
            // 捕获运行阶段异常
            log.error("运行异常，sandboxId={}", runRequest.getSandboxId(), e);
            return RunResponse.builder()
                    .success(false)
                    .message(StringUtils.hasText(e.getMessage()) ? e.getMessage() : "运行异常")
                    .build();
        }
    }

    /**
     * 删除容器和临时文件
     */
    protected abstract void doDelete(SandboxContext context);

    @Override
    public final void deleteFile(String sandboxId) {
        SandboxContext context = contextMap.remove(sandboxId);
        if (context == null) {
            return;
        }
        safeDelete(context);
    }

    private void safeDelete(SandboxContext context) {
        try {
            doDelete(context);
        } catch (Throwable e) {
            log.error("删除沙箱资源异常，sandboxId={}",
                    context == null ? null : context.getSandboxId(), e);
        }
    }

}