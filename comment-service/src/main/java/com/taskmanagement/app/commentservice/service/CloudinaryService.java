package com.taskmanagement.app.commentservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map<String, String> uploadFile(MultipartFile file, String folder) throws IOException {

        String contentType = file.getContentType();
        String resourceType;

        if (contentType != null && contentType.startsWith("image/")) {
            resourceType = "image";
        } else if (contentType != null && contentType.startsWith("video/")) {
            resourceType = "video";
        } else {
            resourceType = "raw";  // PDFs, docs, etc
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",        folder,
                        "resource_type", resourceType
                )
        );

        String fileUrl = (String) uploadResult.get("secure_url");

        // Build Google Docs viewer URL for PDFs
        String viewerUrl = null;
        if ("application/pdf".equals(contentType)) {
            viewerUrl = "https://docs.google.com/viewer?url="
                    + fileUrl + "&embedded=true";
        }

        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", fileUrl);
        result.put("viewerUrl", viewerUrl);
        return result;
    }

    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}