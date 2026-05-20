package com.joj.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/5 15:03
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageConfig {

    private String language;

    private String sourceFileName;

    private String dockerImage;

    private String[] compileCmd;

    private String[] runCmd;

    private Integer compileTimeoutMillis;

    private Integer runTimeoutMillis;


    private static final String RUNNER_IMAGE = "joj-runner:latest";

    public static LanguageConfig getConfig(String language, Integer timeLimit) {
        switch (language) {
            case "C++11":
                return new LanguageConfig(
                        language,
                        "main.cpp",
                        RUNNER_IMAGE,
                        new String[]{
                                "g++", "-DONLINE_JUDGE", "-O2", "-w",
                                "-fmax-errors=1", "-std=c++11",
                                "main.cpp", "-lm", "-o", "main"
                        },
                        new String[]{"./main"},
                        10_000,
                        timeLimit
                );

            case "C++20":
                return new LanguageConfig(
                        language,
                        "main.cpp",
                        RUNNER_IMAGE,
                        new String[]{
                                "g++", "-DONLINE_JUDGE", "-O2", "-w",
                                "-fmax-errors=1", "-std=c++2a",
                                "main.cpp", "-lm", "-o", "main"
                        },
                        new String[]{"./main"},
                        10_000,
                        timeLimit
                );

            case "Java":
                return new LanguageConfig(
                        language,
                        "Main.java",
                        RUNNER_IMAGE,
                        new String[]{
                                "sh", "-c",
                                "javac -encoding utf-8 Main.java && jar -cvf Main.jar *.class"
                        },
                        new String[]{
                                "java", "-Dfile.encoding=UTF-8", "-cp", "Main.jar", "Main"
                        },
                        10_000,
                        timeLimit
                );

            case "Python3":
                return new LanguageConfig(
                        language,
                        "main.py",
                        RUNNER_IMAGE,
                        new String[]{"python3", "-m", "py_compile", "main.py"},
                        new String[]{"python3", "main.py"},
                        5_000,
                        timeLimit
                );

            case "Pypy3":
                return new LanguageConfig(
                        language,
                        "main.py",
                        RUNNER_IMAGE,
                        new String[]{"pypy3", "-m", "py_compile", "main.py"},
                        new String[]{"pypy3", "main.py"},
                        5_000,
                        timeLimit
                );

            case "Golang":
                return new LanguageConfig(
                        language,
                        "main.go",
                        RUNNER_IMAGE,
                        new String[]{"go", "build", "-o", "main", "main.go"},
                        new String[]{"./main"},
                        20_000,
                        timeLimit
                );

            default:
                return null;
        }
    }
}
