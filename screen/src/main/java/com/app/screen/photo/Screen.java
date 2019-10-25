package com.app.screen.photo;

import com.app.screen.constent.Constent;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

@Service
public class Screen {

    private Dimension dimension = null;
    private Robot robot = null;
    private FFmpegFrameRecorder recorder;
    private OpenCVFrameConverter.ToIplImage converter;

    private File createImage(String name) throws Exception{
        Rectangle rectangle = new Rectangle(dimension);
        Robot robot = new Robot();

        BufferedImage image = robot.createScreenCapture(rectangle);
        File screenFile = new File("image");
        if (!screenFile.exists()) {
            screenFile.mkdir();
        }
        File f = new File(screenFile, name + ".JPEG");
        // 写入文件
        ImageIO.write(image, "JPEG", f);

        return f;
    }

    private byte[] getImageBytes()throws Exception{
        Rectangle rectangle = new Rectangle(dimension);

        BufferedImage image = robot.createScreenCapture(rectangle);
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", ops);

        return ops.toByteArray();
    }

    private void init(String fileName) throws Exception{
        dimension = Toolkit.getDefaultToolkit().getScreenSize();
        robot = new Robot();
        recorder = new FFmpegFrameRecorder(fileName + Constent.FILE_SUFFIX, (int)dimension.getWidth(), (int)dimension.getHeight() ,2);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
        // recorder.setFormat("mov,mp4,m4a,3gp,3g2,mj2,h264,ogg,MPEG4");
        recorder.setFormat("mp4");
        recorder.setSampleRate(44100);
        recorder.setFrameRate(24);

        recorder.setVideoQuality(0);
        // 2000 kb/s, 720P视频的合理比特率范围
        recorder.setVideoBitrate(1000000);
        recorder.setVideoOption("preset", "slow");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // yuv420p
        recorder.setAudioOption("crf", "0");
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

        recorder.start();

        converter = new OpenCVFrameConverter.ToIplImage();
    }

    private void start() throws Exception{
        for(int i = 0 ; i < 100 ; i ++){
            File file = createImage("simple");
            opencv_core.IplImage iplImage = cvLoadImage(file.getPath());
            recorder.record(converter.convert(iplImage));
            System.out.println("写入一帧");
            cvReleaseImage(iplImage);
            Thread.sleep(200);
            file.delete();
        }
        recorder.stop();
        recorder.release();
        recorder.close();

    }

    public static void main(String args[]){
        Screen screenPhoto = new Screen();
        try {
            screenPhoto.init("a");
            screenPhoto.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
