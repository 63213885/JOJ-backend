package com.joj.course.controller;

import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.course.model.dto.CourseCreateRequest;
import com.joj.course.model.dto.CourseUpdateRequest;
import com.joj.course.model.dto.CourseVO;
import com.joj.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:49
 */

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 管理员新增课程
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping()
    public Result<Long> addCourse(@RequestBody CourseCreateRequest request) {
        return Result.success(courseService.addCourse(request));
    }

    /**
     * 管理员删除课程
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCourse(@PathVariable Long id) {
        return Result.success(courseService.deleteCourse(id));
    }

    /**
     * 管理员修改课程
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PutMapping()
    public Result<Boolean> updateCourse(@RequestBody CourseUpdateRequest request) {
        return Result.success(courseService.updateCourse(request));
    }

    /**
     * 上传课程封面
     *
     * @param file
     * @return
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/{id}/upload/cover")
    public Result<String> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return Result.success(courseService.uploadCover(id, file));
    }


    /**
     * 查看课程
     */
    @GetMapping("/{id}")
    public Result<CourseVO> getCourse(@PathVariable Long id) {
        return Result.success(courseService.getCourseVOById(id));
    }

    /**
     * 查询所有课程
     */
    @GetMapping("/list")
    public Result<List<CourseVO>> listOnlineCourses() {
        return Result.success(courseService.listCourseVOs());
    }

}
