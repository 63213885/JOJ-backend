package com.joj.course.service;

import com.joj.course.model.dto.CourseLessonCreateRequest;
import com.joj.course.model.dto.CourseLessonUpdateRequest;
import com.joj.course.model.dto.CourseLessonVO;
import com.joj.course.model.dto.VideoPlayUrlVO;
import com.joj.common.core.model.entity.CourseLesson;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:47
 */

public interface CourseLessonService {

    Long addCourseLesson(CourseLessonCreateRequest courseLessonCreateRequest);

    Boolean deleteCourseLesson(Long id);

    Boolean updateCourseLesson(CourseLessonUpdateRequest courseLessonUpdateRequest);

    CourseLesson getCourseLessonById(Long id);

    CourseLessonVO getCourseLessonVOById(Long id);

    List<CourseLessonVO> listCourseLessonVOsByCourseId(Long courseId);


//    VideoPlayUrlVO getVideoPlayUrl(Long lessonId);

}
