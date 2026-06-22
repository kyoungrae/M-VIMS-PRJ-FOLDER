package com.fms.file;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileProcessManagerImpl {
    boolean isValidDirectory(String str) throws Exception;

    // List<Map<String,Object>> searchfile(Map<String,Object> param) throws
    // Exception;
    // void createFile(String f,String t , String v, String s) throws Exception;
    List<Map<String, Object>> uploadFile(String folder_name, MultipartFile[] files) throws Exception;

    void downloadFile(Map<String, Object> param, HttpServletResponse response) throws IOException;
    // List<File> downloadZipFileList(List<Map<String,Object>> param) throws
    // Exception;
    // void deleteFiles(List<File> fileList) throws Exception;
    // void downloadZipFile(List<File> param, HttpServletResponse response,String
    // zip_file_nm) throws Exception;
    // String loadProperty(String propertyFileName,String propertyName) throws
    // IOException;
    // Long fileSizeDefinition(String size , String unit );
}
