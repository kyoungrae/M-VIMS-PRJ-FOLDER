package com.fms.file;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fms/fileManager")
public class FileManagerController {
    private final FileManagerService fileManagerService;
    // private final String BASE_URL = "http://localhost:8080/fileManager/images/";
    // private final String BASE_PATH = "C:/Temp/vims/file/userImgFolder/";

    // @PostMapping("/create")
    // public void createFile(@RequestBody Map<String ,Object> param) throws
    // Exception {
    // fileManagerService.fileCreate(param);
    // }
    @PostMapping("/upload")
    // public List<Map<String ,Object>> uploadFile(@RequestParam("folder_name")
    // String folder_name, @RequestParam(value = "uuid", required = false) String
    // uuid, @RequestParam("files") MultipartFile[] files) throws Exception {
    public List<Map<String, Object>> uploadFile(@RequestParam("folder_name") String folder_name,
            @RequestParam(value = "file_uuid", required = false) String file_uuid,
            @RequestParam("files") MultipartFile[] files) throws Exception {
        List<Map<String, Object>> map = fileManagerService.fileUpload(folder_name, file_uuid, files);
        return map;
    }

    // @PostMapping("/search")
    // public List<Map<String,Object>> searchFile(@RequestBody Map<String,Object>
    // param) throws Exception{
    // return fileManagerService.fileSearch(param);
    // }
    //
    @GetMapping("/download")
    public void downloadFile(@RequestParam("fileId") String fileId, HttpServletResponse response) throws Exception {
        fileManagerService.fileDownload(fileId, response);
    }

    @GetMapping("/downloadByUuid")
    public void downloadByUuid(@RequestParam("fileUuid") String fileUuid, HttpServletResponse response)
            throws Exception {
        fileManagerService.fileDownloadByUuid(fileUuid, response);
    }

    @GetMapping("/previewByUuid")
    public void previewByUuid(@RequestParam("fileUuid") String fileUuid, HttpServletResponse response)
            throws Exception {
        fileManagerService.filePreviewByUuid(fileUuid, response);
    }
    // @PostMapping("/downloadZipFile")
    // public void downloadZipFile(@RequestBody List<Map<String,Object>> param ,
    // HttpServletResponse response) throws Exception{
    // fileManagerService.zipFileDownload(param,response);
    // }
    // @PostMapping("/searchImg")
    // public String getImage(@RequestBody Map<String,Object> request) throws
    // Exception {
    // String baseUrl =
    // ApplicationResource.get("application.properties").get("imgPath").toString();
    // request.put("BASE_URL",baseUrl);
    // return fileManagerService.getImage(request);
    // }
    // @GetMapping("/images")
    // public Resource serveImage(@RequestParam String fileId, String basePath)
    // throws MalformedURLException {
    // Path filePath = Paths.get(basePath, fileId);
    // return new UrlResource(filePath.toUri());
    // }
}