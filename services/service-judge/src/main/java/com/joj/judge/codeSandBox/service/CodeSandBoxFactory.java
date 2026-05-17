package com.joj.judge.codeSandBox.service;

import com.joj.judge.codeSandBox.service.impl.ExampleCodeSandBox;
import com.joj.judge.codeSandBox.service.impl.RemoteCodeSandBox;
import com.joj.judge.codeSandBox.service.impl.ThirdPartyCodeSandBox;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 14:00
 */

public class CodeSandBoxFactory {

    public static CodeSandBox newInstance(String type) {
        if (type.equals("example")) {
            return new ExampleCodeSandBox();
        } else if (type.equals("remote")) {
            return new RemoteCodeSandBox();
        } else {
            return new ThirdPartyCodeSandBox();
        }
    }

}
