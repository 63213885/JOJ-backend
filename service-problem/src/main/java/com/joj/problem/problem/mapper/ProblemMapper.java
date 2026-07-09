package com.joj.problem.problem.mapper;

import com.joj.common.core.model.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/23 22:44
 */

@Mapper
public interface ProblemMapper {

    // 增
    int insert(Problem problem);

    // 删
    int deleteById(@Param("id") Long id);

    // 改
    int updateById(Problem problem);

    int incrementSubmitCount(@Param("id") Long id);

    int incrementAcceptedCount(@Param("id") Long id);

    // 查
    Problem findById(@Param("id") Long id);

    List<Problem> selectProblemPageByStatus(@Param("limit") Integer limit, @Param("offset") Integer offset, @Param("isAdmin") Boolean isAdmin);

    Integer total(@Param("isAdmin") Boolean isAdmin);

}
