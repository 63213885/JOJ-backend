package com.joj.codesandbox.init;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.joj.codesandbox.model.ExecuteCodeRequest;
import com.joj.codesandbox.model.ExecuteCodeResponse;
import com.joj.codesandbox.model.LanguageConfig;
import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;
import com.joj.codesandbox.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/6 14:30
 */

@Slf4j
@Component
public class SandboxInitRunner implements ApplicationRunner {

    @Resource
    private DockerClient dockerClient;

    private final String baseUrl = "http://localhost:5050/api/codeSandbox";

    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "jzz";

    private static final List<String> LANGUAGE_LIST = Arrays.asList(
            "C++11",
            "C++20",
            "Java",
            "Python3",
            "Pypy3",
            "Golang"
    );

    private CompileResponse compile(CompileRequest request) {
        return HttpUtils.post(
                baseUrl + "/compile",
                request,
                HttpUtils.headers(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET),
                CompileResponse.class
        );
    }

    private RunResponse run(RunRequest request) {
        return HttpUtils.post(
                baseUrl + "/run",
                request,
                HttpUtils.headers(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET),
                RunResponse.class
        );
    }

    private Void deleteFile(String sandboxId) {
        return HttpUtils.delete(
                baseUrl + "/" + sandboxId,
                HttpUtils.headers(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET),
                Void.class
        );
    }

    private ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        Integer timeLimit = executeCodeRequest.getTimeLimit();
        Integer memoryLimit = executeCodeRequest.getMemoryLimit();

        String codeSandboxId = null;
        try {
            CompileResponse compileResponse = compile(
                    CompileRequest.builder()
                            .code(code)
                            .language(language)
                            .timeLimit(timeLimit)
                            .build()
            );
            if (compileResponse == null) {
                log.info("compile response is null");
                return null;
            }
            if (!compileResponse.getSuccess()) {
                log.info("compile message: {}", compileResponse.getMessage());
                return null;
            }
            codeSandboxId = compileResponse.getSandboxId();
            log.info("compile ok codeSandboxId: {}", codeSandboxId);

            RunResponse runResponse = run(
                    RunRequest.builder()
                            .sandboxId(codeSandboxId)
                            .inputList(inputList)
                            .build()
            );
            if (runResponse == null) {
                log.info("runResponse is null");
                return null;
            }
            if (!runResponse.getSuccess()) {
                log.info("run message: {}", runResponse.getMessage());
                return null;
            }
            return ExecuteCodeResponse.builder()
                    .outputList(runResponse.getOutputList())
                    .status(runResponse.getMessage())
                    .timeUsed(runResponse.getTimeUsed())
                    .memoryUsed(runResponse.getMemoryUsed())
                    .build();

        } catch (Exception e) {
            log.info("message: {}", e.getMessage());
            return null;
        } finally {
            if (codeSandboxId != null) {
                deleteFile(codeSandboxId);
            } else {
                log.info("codeSandboxId is null");
            }
        }
    }

//    private void pullImageIfAbsent(String image) {
//        try {
//            dockerClient.inspectImageCmd(image).exec();
//            log.info("Docker 镜像已存在：{}", image);
//            return;
//        } catch (Exception ignored) {
//            log.info("Docker 镜像不存在，开始拉取：{}", image);
//        }
//
//        try {
//            dockerClient.pullImageCmd(image)
//                    .exec(new PullImageResultCallback() {
//                        @Override
//                        public void onNext(PullResponseItem item) {
//                            log.info("拉取镜像 {}: id={}, status={}, progress={}",
//                                    image,
//                                    item.getId(),
//                                    item.getStatus(),
//                                    item.getProgress()
//                            );
//                            super.onNext(item);
//                        }
//                    })
//                    .awaitCompletion();
//
//            log.info("Docker 镜像拉取完成：{}", image);
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Docker 镜像拉取被中断：" + image, e);
//        } catch (Exception e) {
//            throw new RuntimeException("Docker 镜像拉取失败：" + image, e);
//        }
//    }


    private boolean imageExists(String imageName) {
        try {
            dockerClient.inspectImageCmd(imageName).exec();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void buildRunnerImage(String imageName) {
        File dockerfileDir = new File(System.getProperty("user.dir"));

        if (!new File(dockerfileDir, "Dockerfile").exists()) {
            throw new RuntimeException("Dockerfile 不存在：" + dockerfileDir.getAbsolutePath());
        }

        Set<String> tags = Collections.singleton(imageName);

        try {
            dockerClient.buildImageCmd(dockerfileDir)
                    .withTags(tags)
                    .exec(new BuildImageResultCallback() {
                        @Override
                        public void onNext(com.github.dockerjava.api.model.BuildResponseItem item) {
                            if (item.getStream() != null) {
                                log.info(item.getStream().trim());
                            }
                            if (item.getError() != null) {
                                log.error(item.getError());
                            }
                            super.onNext(item);
                        }
                    })
                    .awaitImageId();

        } catch (Exception e) {
            throw new RuntimeException("构建沙箱镜像失败：" + imageName, e);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化代码沙箱");

        for (String language : LANGUAGE_LIST) {
            log.info("language: {}", language);
            LanguageConfig languageConfig = LanguageConfig.getConfig(language, 1000);
            if (languageConfig == null) {
                log.info("language config is null");
            }

            if (imageExists(languageConfig.getDockerImage())) {
                log.info("image: {} 已存在", languageConfig.getDockerImage());
            } else {
                log.info("image: {} 不存在，开始拉取", languageConfig.getDockerImage());
                buildRunnerImage(languageConfig.getDockerImage());
            }

            ExecuteCodeResponse executeCodeResponse = executeCode(
                    ExecuteCodeRequest.builder()
                            .code(getCode(language))
                            .language(language)
                            .inputList(Arrays.asList("3 4\n", "100 99\n", "-1 1\n"))
                            .timeLimit(1000)
                            .memoryLimit(256)
                            .build()
            );

            if (executeCodeResponse == null) {
                log.info("execute code is null");
                continue;
            }
            List<String> outputList = executeCodeResponse.getOutputList();
            String status = executeCodeResponse.getStatus();
            Integer timeUsed = executeCodeResponse.getTimeUsed();
            Integer memoryUsed = executeCodeResponse.getMemoryUsed();

            log.info("outputList: {}, status: {}, timeUsed: {}, memoryUsed: {}", outputList, status, timeUsed, memoryUsed);

            if (timeUsed > 1000) {
                log.info("TLE");
                continue;
            }
            if (memoryUsed > 128 * 1024 * 1024) {
                log.info("MLE");
                continue;
            }
            if (outputList == null || outputList.isEmpty() || outputList.size() != 3) {
                log.info("WA");
                continue;
            }
            if (!outputList.get(0).equals("7\n") || !outputList.get(1).equals("199\n") || !outputList.get(2).equals("0\n")) {
                log.info("WA");
                continue;
            }
            log.info("AC");
        }
    }

    private String getCode(String language) {
        if ("C++11".equalsIgnoreCase(language) || "C++20".equalsIgnoreCase(language)) {
            return "#include <bits/stdc++.h>\n"
                    + "using namespace std;\n"
                    + "int main() {\n"
                    + "    long long a, b;\n"
                    + "    cin >> a >> b;\n"
                    + "    cout << a + b << endl;\n"
                    + "    return 0;\n"
                    + "}\n";
        }

        if ("Java".equalsIgnoreCase(language)) {
            return "import java.util.*;\n"
                    + "public class Main {\n"
                    + "    public static void main(String[] args) {\n"
                    + "        Scanner scanner = new Scanner(System.in);\n"
                    + "        long a = scanner.nextLong();\n"
                    + "        long b = scanner.nextLong();\n"
                    + "        System.out.println(a + b);\n"
                    + "    }\n"
                    + "}\n";
        }

        if ("Python3".equalsIgnoreCase(language) || "Pypy3".equalsIgnoreCase(language)) {
            return "a, b = map(int, input().split())\n"
                    + "print(a + b)\n";
        }

        if ("Go".equalsIgnoreCase(language) || "Golang".equalsIgnoreCase(language)) {
            return "package main\n"
                    + "\n"
                    + "import \"fmt\"\n"
                    + "\n"
                    + "func main() {\n"
                    + "    var a, b int64\n"
                    + "    fmt.Scan(&a, &b)\n"
                    + "    fmt.Println(a + b)\n"
                    + "}\n";
        }

        throw new RuntimeException("不支持的语言：" + language);
    }

}
