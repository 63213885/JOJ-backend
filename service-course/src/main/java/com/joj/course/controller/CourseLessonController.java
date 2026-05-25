package com.joj.course.controller;

import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.course.model.dto.CourseLessonCreateRequest;
import com.joj.course.model.dto.CourseLessonUpdateRequest;
import com.joj.course.model.dto.CourseLessonVO;
import com.joj.course.model.dto.VideoPlayUrlVO;
import com.joj.course.service.CourseLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:50
 */

@RestController
@RequestMapping("/course/lesson")
@RequiredArgsConstructor
public class CourseLessonController {

    private final CourseLessonService courseLessonService;

    /**
     * 管理员新增课时
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping()
    public Result<Long> addLesson(@RequestBody CourseLessonCreateRequest request) {
        return Result.success(courseLessonService.addCourseLesson(request));
    }

    /**
     * 管理员删除课时
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteLesson(@PathVariable Long id) {
        return Result.success(courseLessonService.deleteCourseLesson(id));
    }

    /**
     * 管理员修改课时
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PutMapping()
    public Result<Boolean> updateLesson(@RequestBody CourseLessonUpdateRequest request) {
        return Result.success(courseLessonService.updateCourseLesson(request));
    }

    /**
     * 查看课时详情
     */
    @GetMapping("/{id}")
    public Result<CourseLessonVO> getLesson(@PathVariable Long id) {
        return Result.success(courseLessonService.getCourseLessonVOById(id));
    }

    /**
     * 查看某课程下的课时
     */
    @GetMapping("/list")
    public Result<List<CourseLessonVO>> listLessons(@RequestParam Long courseId) {
        return Result.success(courseLessonService.listCourseLessonVOsByCourseId(courseId));
    }



    /**
     * 看视频的临时url
     */
    @AuthCheck
    @GetMapping("/{lessonId}/video/play-url")
    public Result<VideoPlayUrlVO> getVideoPlayUrl(@PathVariable Long lessonId) {
        return Result.success(courseLessonService.getVideoPlayUrl(lessonId));
    }

}
