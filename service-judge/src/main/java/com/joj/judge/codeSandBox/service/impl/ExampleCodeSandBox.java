package com.joj.judge.codeSandBox.service.impl;

import com.joj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.joj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.joj.judge.codeSandBox.service.CodeSandBox;
import com.joj.judge.listener.CodeSandboxExecuteListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 14:08
 */

//@Slf4j
//public class ExampleCodeSandBox implements CodeSandBox {
//
//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, CodeSandboxExecuteListener listener) {
//        System.out.println("示例代码沙箱（仅用于跑通流程）");
//        List<String> inputList = executeCodeRequest.getInputList();
//        String code = executeCodeRequest.getCode();
//        String language = executeCodeRequest.getLanguage();
//
//        log.info("代码沙箱请求参数：{}", executeCodeRequest);
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        executeCodeResponse.setOutputList(inputList);
//        executeCodeResponse.setStatus("success");
//        executeCodeResponse.setTimeUsed(899);
//        executeCodeResponse.setMemoryUsed(10243);
//        return executeCodeResponse;
//    }
//
//}
