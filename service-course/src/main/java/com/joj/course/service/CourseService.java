package com.joj.course.service;

import com.joj.course.model.dto.CourseCreateRequest;
import com.joj.course.model.dto.CourseUpdateRequest;
import com.joj.course.model.dto.CourseVO;
import com.joj.common.core.model.entity.Course;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:47
 */

public interface CourseService {

    Long addCourse(CourseCreateRequest courseCreateRequest);

    Boolean deleteCourse(Long id);

    Boolean updateCourse(CourseUpdateRequest courseUpdateRequest);

    String uploadCover(Long courseId, MultipartFile file);

    Course getCourseById(Long id);

    CourseVO getCourseVOById(Long id);

    List<CourseVO> listCourseVOs();
}
