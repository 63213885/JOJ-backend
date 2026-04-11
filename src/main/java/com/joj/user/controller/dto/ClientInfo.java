package com.joj.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 23:43
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {

    String ip;
    String userAgent;

}
