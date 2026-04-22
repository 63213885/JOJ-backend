package com.joj.user.counter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 16:36
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {

    private Long userId;

    private Integer submitCount;
    private Integer acceptedCount;
    private Integer solvedCount;

    private Integer followerCount;
    private Integer followingCount;

    private Integer rating;

    private Integer courseCount;

    private Integer pkCount;
    private Integer pkWinCount;

    private Integer contestCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
