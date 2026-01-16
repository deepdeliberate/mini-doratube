package com.naman.youtube.mini_doratube.worker;

import com.naman.youtube.mini_doratube.model.Video;
import com.naman.youtube.mini_doratube.model.VideoStatus;
import com.naman.youtube.mini_doratube.repository.VideoRepository;
import io.minio.MinioClient;
import io.minio.DownloadObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoProcessingWorker {

    private final RedisTemplate<String, String> redisTemplate;
    private final VideoRepository videoRepository;
    private final MinioClient minioClient;

    private static final String QUEUE_KEY = "video:processing:queue";

    @Scheduled(fixedDelay = 3000)
    public void processNextVideo() {
        String videoIdStr = redisTemplate.opsForList().rightPop(QUEUE_KEY);
        if(videoIdStr == null) return;

        UUID videoId = UUID.fromString(videoIdStr);
        try{
            transcode(videoId);
            markReady(videoId);
        } catch (Exception e){
            e.printStackTrace();
            markFailed(videoId);
        }
    }

    private void transcode(UUID videoId) throws Exception{
        // Download OG Video
        File input = new File("/tmp/" + videoId + ".mp4");

        if(input.exists()){
            input.delete();
        }
        File hls360 = new File("/tmp/hls" + videoId + "/360p");
        File hls720 = new File("/tmp/hls" + videoId + "/720p");
        File hlsDir = new File("/tmp/hls/" + videoId);

        hls360.mkdirs();
        hls720.mkdirs();
        hlsDir.mkdirs();

        // 1. Download Original
        minioClient.downloadObject(
                DownloadObjectArgs.builder()
                        .bucket("videos")
                        .object(videoId + ".mp4")
                        .filename(input.getAbsolutePath())
                        .build()
        );

        // 2. Run FFmpeg 720
        runFFmpeg(input, hls720, 720);

        // 3. Run FFmpeg 360p
        runFFmpeg(input, hls360, 360);

        uploadDirectory("videos", "hls/" + videoId + "/360p", hls360);
        uploadDirectory("videos", "hls/" + videoId + "/720", hls720);

        generateMasterPlaylist(videoId, hlsDir);
    }

    private void runFFmpeg(File input, File outputDir, int height) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", input.getAbsolutePath(),
                "-vf", "scale=-2:" + height,
                "-c:v", "h264",
                "-crf", height == 720 ? "20" : "23",
                "-g", "48",
                "-keyint_min", "48",
                "-sc_threshold", "0",
                "-c:a", "aac",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", outputDir.getAbsolutePath() + "/segment_%03d.ts",
                outputDir.getAbsolutePath() + "/index.m3u8"
        );

        pb.inheritIO();
        Process p = pb.start();
        p.waitFor();
    }

    private void generateMasterPlaylist(UUID videoID, File hlsDir) throws Exception{
        File masterFile = new File(hlsDir, "master.m3u8");

        String content = "#EXTM3U\n" +
                "#EXT-X-VERSION:3\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=800000, RESOLUTION=640x360\n"+
                "360/index.m3u8\n"+
                "#EXT-X-STREAM-INF:BANDWIDTH=2500000, RESOLUTION=1280x720\n"+
                "720/index.m3u8\n";

        java.nio.file.Files.write(masterFile.toPath(), content.getBytes());

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("videos")
                        .object("hls/"+videoID+"/master.m3u8")
                        .filename(masterFile.getAbsolutePath())
                        .build()
        );



    }

    private void uploadDirectory(String bucket, String prefix, File dir) throws Exception {
        for(File file: dir.listFiles()){
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(prefix + "/" + file.getName())
                            .filename(file.getAbsolutePath())
                            .build()
            );
        }
    }

    private void markReady(UUID videoId){
        Video video = videoRepository.findById(videoId).orElseThrow();
        video.setStatus(VideoStatus.READY);
        videoRepository.save(video);
    }

    private void markFailed(UUID videoId){
        Video video = videoRepository.findById(videoId).orElseThrow();
        video.setStatus(VideoStatus.FAILED);
        videoRepository.save(video);
    }
}
