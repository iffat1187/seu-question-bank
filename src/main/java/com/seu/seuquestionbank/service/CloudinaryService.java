package com.seu.seuquestionbank.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads a file to Cloudinary and returns the secure URL.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        Map params = ObjectUtils.asMap(
                "folder", "seu_question_bank/question_papers",
                "resource_type", "auto"
        );
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl == null) {
            secureUrl = uploadResult.get("url");
        }
        return secureUrl != null ? secureUrl.toString() : null;
    }

    /**
     * Deletes an image from Cloudinary given its secure URL.
     * Extracts public_id from URL and calls destroy.
     * Failures are logged but not thrown.
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId == null) {
                log.warn("Could not extract publicId from URL: {}", imageUrl);
                return;
            }
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary delete for {} -> {}", publicId, result.get("result"));
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary image {}: {}", imageUrl, e.getMessage());
        }
    }

    /**
     * Extracts public_id from Cloudinary URL.
     * Example: https://res.cloudinary.com/w8m5elro/image/upload/v1234567890/seu_question_bank/question_papers/abc123.jpg
     * -> seu_question_bank/question_papers/abc123
     */
    private String extractPublicId(String url) {
        try {
            // Find /upload/ segment
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return null;
            String after = url.substring(uploadIdx + "/upload/".length());
            // Remove version prefix v1234567890/
            if (after.matches("^v\\d+/.*")) {
                after = after.substring(after.indexOf('/') + 1);
            }
            // Remove extension
            int dotIdx = after.lastIndexOf('.');
            if (dotIdx != -1) {
                after = after.substring(0, dotIdx);
            }
            // Remove query params
            int qIdx = after.indexOf('?');
            if (qIdx != -1) after = after.substring(0, qIdx);
            return after;
        } catch (Exception e) {
            log.warn("Error extracting publicId from {}", url, e);
            return null;
        }
    }
}
