package com.joj.codesandbox.service.impl;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/5 13:52
 */

import cn.hutool.core.io.FileUtil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;
import com.joj.codesandbox.model.LanguageConfig;
import com.joj.codesandbox.model.SandboxContext;
import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;
import com.joj.codesandbox.sdk.enmus.RunResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Docker 通用代码沙箱
 *
 * 负责：
 * 1. 保存代码到本地目录
 * 2. 创建 Docker 容器
 * 3. 在 Docker 容器中编译
 * 4. 在 Docker 容器中运行
 * 5. 删除容器和临时文件
 */
@Slf4j
@Component
public class DockerCodeSandbox extends AbstractSandboxTemplate {

    @Resource
    private DockerClient dockerClient;

//     宿主机保存代码的根目录
    private static final String CODE_ROOT_DIR = "tmpCode";

//     容器内部工作目录
    private static final String CONTAINER_WORK_DIR = "/w";

    private static final Long MAX_MEMORY_LIMIT = 512L * 1024 * 1024;

    private static final String REAL_KEY = "REAL";

    private static final String USER_KEY = "USER";

    private static final String SYS_KEY = "SYS";

    private static final String MEMORY_KEY = "MEMORY";

//    保存用户代码到宿主机
    private void saveCodeToFile(SandboxContext context, String code) {
        LanguageConfig languageConfig = context.getLanguageConfig();

        String userDir = System.getProperty("user.dir");
        // 拼接代码目录，例如：项目路径/tmpCode/{sandboxId}
        String codeDir = userDir + File.separator + CODE_ROOT_DIR + File.separator + context.getSandboxId();
        // 拼接代码文件路径，例如：项目路径/tmpCode/{sandboxId}/Main.java
        String codePath = codeDir + File.separator + languageConfig.getSourceFileName();
        // 写入代码文件
        File codeFile = FileUtil.writeString(code, codePath, StandardCharsets.UTF_8);

        // 保存代码文件对象
        context.setCodeFile(codeFile);
        // 保存代码目录路径，后续 Docker 挂载和删除都要用
        context.setCodeDir(codeDir);
    }

//    创建并启动 Docker 容器
    private void createAndStartContainer(SandboxContext context) {
        LanguageConfig languageConfig = context.getLanguageConfig();

        // 配置容器运行限制和挂载
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withMemory(MAX_MEMORY_LIMIT) // 设置内存限制
                .withMemorySwap(MAX_MEMORY_LIMIT) // 禁止使用 swap，避免突破内存限制
                .withCpuCount(1L) // 限制 CPU 数量
                .withNetworkMode("none") // 禁用网络
                .withReadonlyRootfs(false) // 根文件系统暂时不设只读，否则部分语言编译可能失败
                .withBinds(new Bind(context.getCodeDir(), new Volume(CONTAINER_WORK_DIR))) // 把宿主机代码目录挂载到容器 /w
                .withCapDrop(Capability.ALL); // 去掉 Linux capabilities，降低容器权限

        // 创建容器
        CreateContainerResponse response = dockerClient.createContainerCmd(languageConfig.getDockerImage())
                .withHostConfig(hostConfig) // 设置 HostConfig
                .withWorkingDir(CONTAINER_WORK_DIR) // 设置容器工作目录
                // 关键：让容器保持运行，否则容器启动后会马上退出，后续 docker exec 会报 container is not running
                .withCmd("/bin/sh", "-c", "while true; do sleep 3600; done")
                .withAttachStdin(true) // 允许 stdin
                .withAttachStdout(true) // 允许 stdout
                .withAttachStderr(true) // 允许 stderr
                .withTty(false) // 不使用 tty，便于区分 stdout/stderr
                .exec();

        // 启动容器
        dockerClient.startContainerCmd(response.getId()).exec();
        // 保存容器 ID
        context.setContainerId(response.getId());
    }

    //     准备沙箱环境
    @Override
    protected void prepare(SandboxContext context, CompileRequest compileRequest) {
        LanguageConfig languageConfig = LanguageConfig.getConfig(
                compileRequest.getLanguage(),
                compileRequest.getTimeLimit()
        );
        context.setLanguageConfig(languageConfig);

        saveCodeToFile(context, compileRequest.getCode());
        // 创建并启动 Docker 容器
        createAndStartContainer(context);
    }

//    执行编译
    @Override
    protected CompileResponse doCompile(SandboxContext context) {
        LanguageConfig languageConfig = context.getLanguageConfig();

        // 如果该语言不需要编译，则直接返回成功
        if (languageConfig.getCompileCmd() == null || languageConfig.getCompileCmd().length == 0) {
            return CompileResponse.builder()
                    .sandboxId(context.getSandboxId())
                    .success(true)
                    .message("不需要编译")
                    .build();
        }

        ExecResultCallback callback = new ExecResultCallback();
        try {
            // 创建 docker exec 编译命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(context.getContainerId())
                    .withCmd(languageConfig.getCompileCmd())
                    .withAttachStdin(false)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            // 启动编译命令
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(callback);

            // 等待编译完成
            boolean finished = callback.awaitCompletion(
                    languageConfig.getCompileTimeoutMillis(),
                    TimeUnit.MILLISECONDS
            );

            // 编译超时
            if (!finished) {
                return CompileResponse.builder()
                        .success(false)
                        .message("编译超时")
                        .build();
            }

            // 获取编译退出码
            Long exitCode = dockerClient.inspectExecCmd(execCreateCmdResponse.getId())
                    .exec()
                    .getExitCodeLong();

            if (exitCode != null && exitCode == 0) {
                return CompileResponse.builder()
                        .sandboxId(context.getSandboxId())
                        .success(true)
                        .message("编译成功")
                        .build();
            }
            return CompileResponse.builder()
                    .success(false)
                    .message("编译失败，错误信息：" + callback.getStderr())
                    .build();

        } catch (Exception e) {
            return CompileResponse.builder()
                    .success(false)
                    .message("编译异常，" + e.getMessage())
                    .build();
        } finally {
            try {
                callback.close();
            } catch (Exception e) {
                log.error("关闭 Docker 编译回调异常", e);
            }
        }
    }

    private Integer getTimeOrMemory(SandboxContext context, String timeFileName, String key) {
        // 读取 /usr/bin/time 输出文件
        File timeFile = new File(context.getCodeDir(), timeFileName);
        if (!timeFile.exists()) {
            return 0;
        }
        try {
            String content = FileUtil.readString(timeFile, StandardCharsets.UTF_8);
            if (!StringUtils.hasText(content)) {
                return 0;
            }

            String[] parts = content.trim().split("\\s+");

            for (String part : parts) {
                if (!part.startsWith(key + "=")) {
                    continue;
                }

                String value = part.substring((key + "=").length());

                if (REAL_KEY.equals(key) || USER_KEY.equals(key) || SYS_KEY.equals(key)) {
                    double seconds = Double.parseDouble(value);
                    return (int) Math.ceil(seconds * 1000);
                }

                if (MEMORY_KEY.equals(key)) {
                    return Integer.parseInt(value);
                }
            }
            return 0;
        } catch (Exception e) {
            log.error("读取 /usr/bin/time 结果失败，sandboxId={}, timeFileName={}, key={}", context.getSandboxId(), timeFileName, key, e);
            return 0;
        }
    }

    private RunResponse execRunCmdWithTime(SandboxContext context, String[] runCmd, String input, long timeoutMillis) {
        String timeFileName = ".oj_time_" + UUID.randomUUID().toString().replace("-", "");
        String timeFilePath = CONTAINER_WORK_DIR + "/" + timeFileName;

        List<String> cmd = new ArrayList<>();
        cmd.add("/usr/bin/time");
        cmd.add("-f");
        cmd.add("REAL=%e USER=%U SYS=%S MEMORY=%M");
        cmd.add("-o");
        cmd.add(timeFilePath);

        cmd.addAll(Arrays.asList(runCmd));

        ExecResultCallback callback = new ExecResultCallback();

        try {
            // 创建 docker exec 运行命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(context.getContainerId())
                    .withCmd(cmd.toArray(new String[0]))
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            // 空输入处理
            if (input == null) {
                input = "";
            }

            // 确保输入以换行结尾，模拟控制台输入
            if (!input.endsWith("\n")) {
                input = input + "\n";
            }

            // 启动运行命令，并写入 stdin
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .withStdIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))
                    .exec(callback);

            // 等待运行完成
            boolean finished = callback.awaitCompletion(timeoutMillis + 5000, TimeUnit.MILLISECONDS);

            Integer realTime = getTimeOrMemory(context, timeFileName, REAL_KEY);
            Integer timeUsed = getTimeOrMemory(context, timeFileName, USER_KEY) + getTimeOrMemory(context, timeFileName, SYS_KEY);
            Integer memoryUsed = getTimeOrMemory(context, timeFileName, MEMORY_KEY);

            log.info(REAL_KEY + " = " + realTime);
            log.info("timeUsed = " + timeUsed);
            log.info("memoryUsed = " + memoryUsed);

            // 运行超时
            if (!finished) {
                // 超时后杀掉容器，避免用户程序继续跑
                try {
                    dockerClient.killContainerCmd(context.getContainerId()).exec();
                } catch (Exception ignored) {
                }

                // 有可能是输出超限
                if (callback.getOutputLimitExceeded()) {
                    return RunResponse.builder()
                            .success(false)
                            .message(RunResultEnum.OUTPUT_LIMIT_EXCEEDED.getValue())
                            .timeUsed(timeUsed)
                            .memoryUsed(memoryUsed)
                            .build();
                }
                return RunResponse.builder()
                        .success(false)
                        .message(RunResultEnum.TIME_LIMIT_EXCEEDED.getValue())
                        .timeUsed(timeUsed)
                        .memoryUsed(memoryUsed)
                        .build();
            }

            // 获取运行退出码
            Long exitCode = dockerClient.inspectExecCmd(execCreateCmdResponse.getId())
                    .exec()
                    .getExitCodeLong();

            if (exitCode != null && exitCode == 137) {
                return RunResponse.builder()
                        .success(false)
                        .message(RunResultEnum.MEMORY_LIMIT_EXCEEDED.getValue())
                        .timeUsed(timeUsed)
                        .memoryUsed(memoryUsed)
                        .build();
            }
            if (exitCode == null || exitCode != 0) {
                log.info("运行时错误 stderr: {}", callback.getStderr());
                return RunResponse.builder()
                        .success(false)
                        .message(RunResultEnum.RUNTIME_ERROR.getValue())
                        .timeUsed(timeUsed)
                        .memoryUsed(memoryUsed)
                        .build();
            }

            return RunResponse.builder()
                    .success(true)
                    .message(RunResultEnum.RUN_SUCCESS.getValue())
                    .outputList(Collections.singletonList(callback.getStdout()))
                    .timeUsed(timeUsed)
                    .memoryUsed(memoryUsed)
                    .build();
        } catch (Exception e) {
            return RunResponse.builder()
                    .success(false)
                    .message(RunResultEnum.SYSTEM_ERROR.getValue())
                    .timeUsed(getTimeOrMemory(context, timeFileName, USER_KEY) + getTimeOrMemory(context, timeFileName, SYS_KEY))
                    .memoryUsed(getTimeOrMemory(context, timeFileName, MEMORY_KEY))
                    .build();
        } finally {
            try {
                callback.close();
                FileUtil.del(new File(context.getCodeDir(), timeFileName));
            } catch (Exception e) {
                log.error("关闭 Docker 运行回调异常", e);
            }
        }
    }

    /**
     * 执行运行
     */
    @Override
    protected RunResponse doRun(SandboxContext context, RunRequest runRequest) {
        List<String> inputList = runRequest.getInputList();

        // 如果没有输入，则默认传空输入
        if (inputList == null || inputList.isEmpty()) {
            inputList = Collections.singletonList("");
        }

        List<String> outputList = new ArrayList<>();
        int maxTime = 0;
        int maxMemory = 0;

        LanguageConfig languageConfig = context.getLanguageConfig();

        // 逐个输入用例运行
        for (String input : inputList) {
            // 在容器中执行运行命令，并把 input 写入 stdin
            RunResponse runResponse = execRunCmdWithTime(
                    context,
                    languageConfig.getRunCmd(),
                    input,
                    languageConfig.getRunTimeoutMillis()
            );

            if (!runResponse.getSuccess()) {
                return runResponse;
            }

            // 统计最大时间
            if (runResponse.getTimeUsed() != null) {
                maxTime = Math.max(maxTime, runResponse.getTimeUsed());
            }
            // 统计最大内存
            if (runResponse.getMemoryUsed() != null) {
                maxMemory = Math.max(maxMemory, runResponse.getMemoryUsed());
            }
            // 保存本次运行的标准输出
            outputList.add(runResponse.getOutputList().get(0));
        }
        return RunResponse.builder()
                .success(true)
                .message(RunResultEnum.RUN_SUCCESS.getValue())
                .outputList(outputList)
                .timeUsed(maxTime)
                .memoryUsed(maxMemory)
                .build();
    }

    /**
     * 删除沙箱资源
     */
    @Override
    protected void doDelete(SandboxContext context) {
        // 上下文为空，直接返回
        if (context == null) {
            return;
        }
        // 获取容器 ID
        String containerId = context.getContainerId();

        // 删除 Docker 容器
        if (dockerClient != null && StringUtils.hasText(containerId)) {
            try {
                dockerClient.removeContainerCmd(containerId)
                        .withForce(true)
                        .exec();
            } catch (Exception e) {
                log.error("删除 Docker 容器失败，sandboxId={}, containerId={}", context.getSandboxId(), containerId, e);
            }
        }

        // 删除宿主机代码目录
        if (StringUtils.hasText(context.getCodeDir())) {
            try {
                FileUtil.del(context.getCodeDir());
            } catch (Exception e) {
                log.error("删除代码目录失败，sandboxId={}, codeDir={}", context.getSandboxId(), context.getCodeDir(), e);
            }
        }
    }

    /**
     * Docker exec 输出回调
     */
    private static class ExecResultCallback extends ResultCallback.Adapter<Frame> {

        private final StringBuilder stdout = new StringBuilder();

        private final StringBuilder stderr = new StringBuilder();

        // 输出限制，按字符数算；比如 1MB 左右
        private static final int MAX_OUTPUT_LENGTH = 16 * 1024 * 1024;

        private boolean outputLimitExceeded = false;

        /**
         * 追加输出，同时控制最大长度
         */
        private void appendWithLimit(StringBuilder builder, String text) {
            if (outputLimitExceeded || builder.length() + text.length() > MAX_OUTPUT_LENGTH) {
                outputLimitExceeded = true;
                return;
            }
            builder.append(text);
        }

        @Override
        public void onNext(Frame frame) {
            // 把字节转换为字符串
            String text = new String(frame.getPayload(), StandardCharsets.UTF_8);

            // stderr
            if (StreamType.STDERR.equals(frame.getStreamType())) {
                appendWithLimit(stderr, text);
            } else {
                // stdout
                appendWithLimit(stdout, text);
            }
        }

        public String getStdout() {
            return stdout.toString();
        }

        public String getStderr() {
            return stderr.toString();
        }

        public boolean getOutputLimitExceeded() {
            return outputLimitExceeded;
        }

    }

}