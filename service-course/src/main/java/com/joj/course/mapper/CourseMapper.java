package com.joj.course.mapper;

import com.joj.common.core.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:45
 */

@Mapper
public interface CourseMapper {

    int insertCourse(Course course);

    int deleteCourseById(@Param("id") Long id);

    int updateCourseById(Course course);

    Course selectCourseById(@Param("id") Long id);

    List<Course> listCourses(@Param("status") Integer status);
}
