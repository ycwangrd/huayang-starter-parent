package com.huayang.product.common.utils.file;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 媒体类型工具类
 *
 * @author huayang
 */
public class MimeTypeUtils {
    public static final String IMAGE_PNG = "image/png";

    public static final String IMAGE_JPG = "image/jpg";

    public static final String IMAGE_JPEG = "image/jpeg";

    public static final String IMAGE_BMP = "image/bmp";

    public static final String IMAGE_GIF = "image/gif";

    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png"};

    public static final String[] FLASH_EXTENSION = {"swf", "flv"};

    public static final String[] MEDIA_EXTENSION = {"swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb"};

    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb"};
    
    public static final String[] TXT_EXTENSION = {"txt", "html", "c", "cpp", "java", "css", "py", "htm", "mht"};

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            // pdf
            "pdf"};

    public static String getExtension(String prefix) {
        switch (prefix) {
            case IMAGE_PNG:
                return "png";
            case IMAGE_JPG:
                return "jpg";
            case IMAGE_JPEG:
                return "jpeg";
            case IMAGE_BMP:
                return "bmp";
            case IMAGE_GIF:
                return "gif";
            default:
                return "";
        }
    }

    /**
     * 可预览类型
     * @param fileName
     * @return
     */
    public static boolean checkPreviewFileType(String fileName) {
        Set<String> canPreview = new HashSet<>();
        canPreview.addAll(Arrays.asList(TXT_EXTENSION));
        canPreview.addAll(Arrays.asList(MEDIA_EXTENSION));
        canPreview.addAll(Arrays.asList(IMAGE_EXTENSION));
        String ext = FilenameUtils.getExtension(fileName).toLowerCase();
        return canPreview.contains(ext);
    }
}
