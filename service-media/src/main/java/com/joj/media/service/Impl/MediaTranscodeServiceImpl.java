package com.joj.media.service.Impl;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.common.core.model.enums.MediaEncryptTypeEnum;
import com.joj.media.service.MediaFileService;
import com.joj.media.service.MediaTranscodeService;
import com.joj.media.service.MinioObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:28
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaTranscodeServiceImpl implements MediaTranscodeService {

    /**
     * 项目根目录下的临时转码目录。
     */
    private static final String TRANSCODE_ROOT_DIR = "tmpTranscode";

    private static final String INPUT_FILE_NAME = "input.mp4";

    private static final String OUTPUT_DIR_NAME = "output";

    private static final String KEY_FILE_NAME = "key.key";

    private static final String KEY_INFO_FILE_NAME = "key_info.txt";

    private static final String HLS_INDEX_FILE_NAME = "index.m3u8";

    /**
     * 先写到 m3u8 里的占位符。
     * 后面 CourseLessonVideoService 返回 m3u8 时，再替换成真正的 key 接口。
     */
    private static final String KEY_URI_PLACEHOLDER = "__KEY_URI__";

    /**
     * 转码超时时间。
     */
    private static final long TRANSCODE_TIMEOUT_HOURS = 1L;

    private final MediaFileService mediaFileService;

    private final MinioObjectService minioObjectService;


    private void checkVideoFile(MediaFile mediaFile) {
        if (mediaFile.getBucketName() == null || mediaFile.getObjectName() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "媒体文件存储信息异常");
        }

        String contentType = mediaFile.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前文件不是视频文件");
        }
    }

    private void createDir(File dir) {
        if (dir.exists()) {
            return;
        }

        boolean success = dir.mkdirs();
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建转码目录失败");
        }
    }

    private byte[] generateAes128Key() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        char[] hexArray = "0123456789abcdef".toCharArray();

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            hexChars[i * 2] = hexArray[value >>> 4];
            hexChars[i * 2 + 1] = hexArray[value & 0x0f];
        }

        return new String(hexChars);
    }

    private String generateIvHex() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return bytesToHex(iv);
    }

    private void writeKeyInfoFile(File keyInfoFile, File keyFile, String iv) {
        try {
            String keyInfo = KEY_URI_PLACEHOLDER + "\n" + keyFile.getAbsolutePath() + "\n" + iv + "\n";

            Files.write(keyInfoFile.toPath(), keyInfo.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "写入 HLS 密钥配置失败");
        }
    }

    private String runCommand(List<String> command) {
        Process process = null;

        try {
            log.info("执行 FFmpeg 命令：{}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();

            StringBuilder outputBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(TRANSCODE_TIMEOUT_HOURS, TimeUnit.HOURS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "FFmpeg 转码超时");
            }

            int exitCode = process.exitValue();
            String output = outputBuilder.toString();

            if (exitCode != 0) {
                log.error("FFmpeg 执行失败，exitCode = {}, output = {}", exitCode, output);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "FFmpeg 执行失败");
            }

            return output;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            if (process != null) {
                process.destroyForcibly();
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行 FFmpeg 异常：" + e.getMessage());
        }
    }

    private void executeFfmpeg(File inputFile, File keyInfoFile, File outputDir, File indexFile) {
        List<String> command = Arrays.asList(
                "ffmpeg",
                "-y",
                "-i", inputFile.getAbsolutePath(),

                // 视频编码
                "-c:v", "libx264",

                // 音频编码
                "-c:a", "aac",

                // 转码速度和质量
                "-preset", "veryfast",
                "-crf", "23",

                // HLS 配置
                "-hls_time", "10",
                "-hls_playlist_type", "vod",
                "-hls_key_info_file", keyInfoFile.getAbsolutePath(),
                "-hls_segment_filename", new File(outputDir, "%06d.ts").getAbsolutePath(),

                // 输出 m3u8
                indexFile.getAbsolutePath()
        );

        String output = runCommand(command);

        if (!indexFile.exists()) {
            log.error("FFmpeg 未生成 index.m3u8，output = {}", output);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "FFmpeg 转码失败，未生成 m3u8");
        }
    }

    private String removeExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index < 0) {
            return filename;
        }

        return filename.substring(0, index);
    }

    private String buildHlsPrefix(String originObjectName) {
        String filename = originObjectName;

        int slashIndex = filename.lastIndexOf("/");
        if (slashIndex >= 0) {
            filename = filename.substring(slashIndex + 1);
        }

        String baseName = removeExtension(filename);

        return "course/hls/" + baseName + "/";
    }

    private String getHlsContentType(String filename) {
        if (filename.endsWith(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        }

        if (filename.endsWith(".ts")) {
            return "video/mp2t";
        }

        return "application/octet-stream";
    }

    private void uploadHlsFiles(String bucketName, String hlsPrefix, File outputDir) {
        File[] files = outputDir.listFiles();
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS 输出文件为空");
        }

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            String filename = file.getName();
            String objectName = hlsPrefix + filename;
            String contentType = getHlsContentType(filename);

            minioObjectService.uploadLocalFile(
                    bucketName,
                    objectName,
                    file.getAbsolutePath(),
                    contentType
            );
        }
    }

    private void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFile(child);
                }
            }
        }

        boolean deleted = file.delete();
        if (!deleted) {
            log.warn("删除临时文件失败，path = {}", file.getAbsolutePath());
        }
    }

    @Override
    public void transcodeToHls(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID不能为空");
        }

        MediaFile mediaFile = mediaFileService.getById(fileId);
        if (mediaFile == null || Objects.equals(mediaFile.getIsDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "媒体文件不存在");
        }

        checkVideoFile(mediaFile);

        String userDir = System.getProperty("user.dir");

        File workDir = new File(userDir + File.separator + TRANSCODE_ROOT_DIR + File.separator + fileId);

        File outputDir = new File(workDir, OUTPUT_DIR_NAME);

        File inputFile = new File(workDir, INPUT_FILE_NAME);
        File keyFile = new File(workDir, KEY_FILE_NAME);
        File keyInfoFile = new File(workDir, KEY_INFO_FILE_NAME);
        File indexFile = new File(outputDir, HLS_INDEX_FILE_NAME);

        try {
            log.info("开始视频转码");
            createDir(outputDir);
            log.info("转码工作目录准备 完成");


            log.info("开始从 MinIO 下载原始 MP4");
            // 1. 从 MinIO 下载原始 MP4
            minioObjectService.downloadObject(
                    mediaFile.getBucketName(),
                    mediaFile.getObjectName(),
                    inputFile.getAbsolutePath()
            );
            log.info("从 MinIO 下载原始 MP4 完成");


            log.info("开始生成 AES-128 真实 key");
            // 2. 生成 AES-128 真实 key
            byte[] realKey = generateAes128Key();
            Files.write(keyFile.toPath(), realKey);
            log.info("生成 AES-128 真实 key 完成");


            log.info("开始生成 IV");
            // 3. 生成 IV
            String iv = generateIvHex();
            log.info("生成 IV 完成");


            log.info("开始写 FFmpeg key_info_file");
            // 4. 写 FFmpeg key_info_file
            writeKeyInfoFile(keyInfoFile, keyFile, iv);
            log.info("写 FFmpeg key_info_file 完成");


            log.info("开始执行 FFmpeg 转码");
            // 5. 执行 FFmpeg 转码
            executeFfmpeg(inputFile, keyInfoFile, outputDir, indexFile);
            log.info("执行 FFmpeg 转码 完成");


            log.info("开始计算 HLS 存储目录");
            // 6. 计算 HLS 存储目录
            String hlsPrefix = buildHlsPrefix(mediaFile.getObjectName());
            log.info("计算 HLS 存储目录 完成");


            log.info("开始上传 index.m3u8 和 ts 分片到 MinIO");
            // 7. 上传 index.m3u8 和 ts 分片到 MinIO
            uploadHlsFiles(mediaFile.getBucketName(), hlsPrefix, outputDir);
            log.info("上传 index.m3u8 和 ts 分片到 MinIO 完成");


            // 8. 更新数据库为已转码
            mediaFileService.markTranscodeSuccess(
                    fileId,
                    hlsPrefix,
                    MediaEncryptTypeEnum.HLS_AES_128_KEY_OBFUSCATED.getValue(),
                    realKey,
                    iv
            );
            log.info("数据库已更新");

            log.info("视频转码成功，fileId = {}, hlsPrefix = {}", fileId, hlsPrefix);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("视频转码异常，fileId = {}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频转码失败：" + e.getMessage());
        } finally {
            deleteFile(workDir);
            log.info("临时文件已删除");
        }
    }

}
