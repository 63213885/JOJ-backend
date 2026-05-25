package com.joj.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.joj.api.MediaFeignClient;
import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.User;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.course.mapper.CourseMapper;
import com.joj.course.model.dto.CourseCreateRequest;
import com.joj.course.model.dto.CourseUpdateRequest;
import com.joj.course.model.dto.CourseVO;
import com.joj.common.core.model.entity.Course;
import com.joj.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:47
 */

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final MediaFeignClient mediaFeignClient;

    @Value("${minio.public-endpoint}")
    private String publicEndpoint;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCourse(CourseCreateRequest courseCreateRequest) {
        if (courseCreateRequest == null) {
            throw new RuntimeException("课程不能为空");
        }
        if (courseCreateRequest.getTitle() == null || courseCreateRequest.getTitle().trim().isEmpty()) {
            throw new RuntimeException("课程标题不能为空");
        }

        if (courseCreateRequest.getPrice() == null) {
            courseCreateRequest.setPrice(BigDecimal.ZERO);
        }
        if (courseCreateRequest.getOriginalPrice() == null) {
            courseCreateRequest.setOriginalPrice(BigDecimal.ZERO);
        }
        if (courseCreateRequest.getSort() == null) {
            courseCreateRequest.setSort(0);
        }
        if (courseCreateRequest.getStatus() == null) {
            courseCreateRequest.setStatus(0);
        }

        Course course = BeanUtil.copyProperties(courseCreateRequest, Course.class);
        course.setCreatorId(UserContext.get().getId());
        int insert = courseMapper.insertCourse(course);
        if (insert <= 0) {
            throw new RuntimeException("创建课程失败");
        }

        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCourse(Long id) {
        if (id == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        return courseMapper.deleteCourseById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCourse(CourseUpdateRequest courseUpdateRequest) {
        if (courseUpdateRequest == null || courseUpdateRequest.getId() == null) {
            throw new RuntimeException("课程ID不能为空");
        }

        Course course = BeanUtil.copyProperties(courseUpdateRequest, Course.class);
        return courseMapper.updateCourseById(course) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadCover(Long courseId, MultipartFile file) {
        String url = mediaFeignClient.uploadCover(file);
        if (StrUtil.isBlank(url)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败。");
        }
        int ok = courseMapper.updateCourseById(
                Course.builder()
                        .id(courseId)
                        .coverUrl(url)
                        .build()
        );
        if (ok <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新头像失败。");
        }
        return publicEndpoint + "/" + url;
    }


    @Override
    public Course getCourseById(Long id) {
        if (id == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        return courseMapper.selectCourseById(id);
    }

    @Override
    public CourseVO getCourseVOById(Long id) {
        if (id == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        Course course = getCourseById(id);
        User user = UserContext.get();
        if (course.getStatus() == 0 && (user == null || UserRoleEnum.fromValue(user.getRole()) == UserRoleEnum.USER)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程未上架");
        }
        CourseVO courseVO = BeanUtil.copyProperties(course, CourseVO.class);
        courseVO.setCoverUrl(publicEndpoint + "/" + course.getCoverUrl());
        return courseVO;
    }

    @Override
    public List<CourseVO> listCourseVOs() {
        User user = UserContext.get();
        List<Course> courses = courseMapper.listCourses(
                user == null || UserRoleEnum.fromValue(user.getRole()) == UserRoleEnum.USER ? 1 : null
        );
        List<CourseVO> courseVOs = BeanUtil.copyToList(courses, CourseVO.class);
        for (CourseVO courseVO : courseVOs) {
            courseVO.setCoverUrl(publicEndpoint + "/" + courseVO.getCoverUrl());
        }
        return courseVOs;
    }
}
