package com.joj.judge.codeSandBox.service;

import com.joj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.joj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.joj.judge.listener.CodeSandboxExecuteListener;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 13:54
 */

public interface CodeSandBox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, CodeSandboxExecuteListener listener);

}
