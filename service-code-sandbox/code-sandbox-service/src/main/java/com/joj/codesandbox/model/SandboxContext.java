package com.joj.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/5 15:07
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxContext {

    private String sandboxId;

    private LanguageConfig languageConfig;

    private File codeFile;

    private String codeDir;

//    private DockerClient dockerClient;

    private String containerId;

}
