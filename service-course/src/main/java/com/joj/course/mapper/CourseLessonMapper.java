package com.joj.course.mapper;

import com.joj.common.core.model.entity.CourseLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:46
 */

@Mapper
public interface CourseLessonMapper {

    int insertCourseLesson(CourseLesson courseLesson);

    int updateCourseLessonById(CourseLesson courseLesson);

    int deleteCourseLessonById(@Param("id") Long id);

    CourseLesson selectCourseLessonById(@Param("id") Long id);

    List<CourseLesson> listCourseLessonsByCourseId(@Param("courseId") Long courseId,
                                                   @Param("status") Integer status);
}
