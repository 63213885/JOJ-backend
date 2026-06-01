package com.joj.common.core.model.vo;

import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:52
 */

@Data
public class VideoPlayInfoVO {

    /**
     * 播放类型：MP4 / HLS
     */
    private String playType;

    /**
     * 播放地址。
     * MP4：MinIO 临时 URL
     * HLS：后端 m3u8 接口地址
     */
    private String url;

}
