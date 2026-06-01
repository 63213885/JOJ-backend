package com.joj.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.joj.api.MediaFeignClient;
import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.common.core.model.entity.User;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.course.mapper.CourseLessonMapper;
import com.joj.course.mapper.CourseMapper;
import com.joj.course.model.dto.CourseLessonCreateRequest;
import com.joj.course.model.dto.CourseLessonUpdateRequest;
import com.joj.course.model.dto.CourseLessonVO;
import com.joj.course.model.dto.VideoPlayUrlVO;
import com.joj.common.core.model.entity.Course;
import com.joj.common.core.model.entity.CourseLesson;
import com.joj.course.service.CourseLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:48
 */

@Service
@RequiredArgsConstructor
public class CourseLessonServiceImpl implements CourseLessonService {

    private final CourseLessonMapper courseLessonMapper;

    private final CourseMapper courseMapper;

//    private final MediaFeignClient mediaFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCourseLesson(CourseLessonCreateRequest courseLessonCreateRequest) {
        if (courseLessonCreateRequest == null) {
            throw new RuntimeException("课时不能为空");
        }
        if (courseLessonCreateRequest.getCourseId() == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        if (courseLessonCreateRequest.getTitle() == null || courseLessonCreateRequest.getTitle().trim().isEmpty()) {
            throw new RuntimeException("课时标题不能为空");
        }

        Course course = courseMapper.selectCourseById(courseLessonCreateRequest.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        CourseLesson courseLesson = BeanUtil.copyProperties(courseLessonCreateRequest, CourseLesson.class);
        if (courseLesson.getDuration() == null) {
            courseLesson.setDuration(0);
        }
        if (courseLesson.getSort() == null) {
            courseLesson.setSort(0);
        }
        courseLesson.setStatus(0);

        int insert = courseLessonMapper.insertCourseLesson(courseLesson);
        if (insert <= 0) {
            throw new RuntimeException("创建课时失败");
        }

        return courseLesson.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCourseLesson(Long id) {
        if (id == null) {
            throw new RuntimeException("课时ID不能为空");
        }

        return courseLessonMapper.deleteCourseLessonById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCourseLesson(CourseLessonUpdateRequest courseLessonUpdateRequest) {
        if (courseLessonUpdateRequest == null || courseLessonUpdateRequest.getId() == null) {
            throw new RuntimeException("课时ID不能为空");
        }
        CourseLesson courseLesson = BeanUtil.copyProperties(courseLessonUpdateRequest, CourseLesson.class);
        return courseLessonMapper.updateCourseLessonById(courseLesson) > 0;
    }

    @Override
    public CourseLesson getCourseLessonById(Long id) {
        if (id == null) {
            throw new RuntimeException("课时ID不能为空");
        }
        return courseLessonMapper.selectCourseLessonById(id);
    }

    @Override
    public CourseLessonVO getCourseLessonVOById(Long id) {
        CourseLesson courseLessonById = getCourseLessonById(id);
        return BeanUtil.copyProperties(courseLessonById, CourseLessonVO.class);
    }

    @Override
    public List<CourseLessonVO> listCourseLessonVOsByCourseId(Long courseId) {
        if (courseId == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        User user = UserContext.get();
        List<CourseLesson> courseLessons = courseLessonMapper.listCourseLessonsByCourseId(
                courseId,
                user != null && UserRoleEnum.fromValue(user.getRole()) == UserRoleEnum.ADMIN ? null : 1
        );
        return BeanUtil.copyToList(courseLessons, CourseLessonVO.class);
    }



//    @Override
//    public VideoPlayUrlVO getVideoPlayUrl(Long lessonId) {
//        CourseLesson lesson = courseLessonMapper.selectCourseLessonById(lessonId);
//        if (lesson == null || lesson.getIsDeleted() == 1) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时不存在");
//        }
//
//        if (UserRoleEnum.fromValue(UserContext.get().getRole()) == UserRoleEnum.USER && lesson.getStatus() == 0) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "课时未开放");
//        }
//
//        Long videoFileId = lesson.getVideoFileId();
//        if (videoFileId == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时未绑定视频");
//        }
//
//        // 这里做权限判断
//        // 免费课：判断课程是否公开即可
//        // 付费课：判断用户是否购买课程
//        // checkCanWatchLesson(lesson);
//
//        MediaFile mediaFile = mediaFeignClient.selectMediaFileById(videoFileId);
//        if (mediaFile == null || mediaFile.getIsDeleted() == 1) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "视频文件不存在");
//        }
//
//        if (!"video/mp4".equals(mediaFile.getContentType())) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前视频格式不支持播放");
//        }
//
//        int expireSeconds = 5 * 60;
//
//        String playUrl = mediaFeignClient.getPresignedGetUrl(
//                mediaFile.getBucketName(),
//                mediaFile.getObjectName(),
//                expireSeconds
//        );
//
//        VideoPlayUrlVO vo = new VideoPlayUrlVO();
//        vo.setPlayUrl(playUrl);
//        vo.setExpireSeconds(expireSeconds);
//        return vo;
//    }

}
