package com.fms.file;

import com.fms.common.SysFile;
import com.fms.common.SysFileMapper;
import com.fms.common.SysFileDetail;
import com.fms.common.SysFileDetailMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileManagerService extends FileProcessManager {
    private final SysFileMapper sysFileMapper;
    private final SysFileDetailMapper sysFileDetailMapper;

    // private final String applicationFileName = "application.properties";
    // public List<Map<String,Object>> fileSearch(Map<String,Object> param) throws
    // Exception{
    // return sysFileMapper.SYS_FILE_SELECT(param);
    // }
    // public void fileCreate(Map<String , Object> map) throws Exception{
    // String folderName = (String) map.get("folder_name");
    // String fileName = (String) map.get("file_nm");
    // String fileContent = (String) map.get("file_content");
    // String fileExtension = (String) map.get("file_ext");
    //
    // createFile(folderName, fileName, fileContent, fileExtension);
    // }
    public List<Map<String, Object>> fileUpload(String folder_name, String file_uuid, MultipartFile[] files)
            throws Exception {
        // NOTE : 인가 정보 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // NOTE : unique ID 생성 (전달받은 uuid가 있으면 사용, 없으면 생성)
        String uid = (file_uuid != null && !file_uuid.isEmpty()) ? file_uuid : String.valueOf(UUID.randomUUID());
        String userId = (authentication != null) ? authentication.getName() : "anonymousUser";

        try {
            List<Map<String, Object>> result = uploadFile(folder_name, files);
            for (Map<String, Object> list : result) {
                list.put("file_uuid", uid);
                list.put("sys_crt_usr_id", userId);
            }
            if (!result.isEmpty()) {
                // 기존 UUID인 경우 SYS_FILE 테이블에 이미 존재할 수 있으므로 존재 여부 체크 후 인서트
                List<SysFile> existingFile = null;
                try {
                    existingFile = sysFileMapper.SYS_FILE_SELECT(SysFile.builder().file_uuid(uid).build());
                } catch (Exception e) {
                    System.err.println("Error selecting existing file: " + e.getMessage());
                    e.printStackTrace();
                }

                if (existingFile == null || existingFile.isEmpty()) {
                    var sysFile = SysFile.builder()
                            .file_uuid(uid)
                            .temp_yn(0) // NOTE : 파일 임시 저장, 사용자가 작성 취소 하거나, 화면을 나갈 경우 삭제하기 위한 flag
                            .sys_crt_usr_id(userId)
                            .sys_crt_dt(new java.util.Date())
                            .build();
                    sysFileMapper.SYS_FILE_INSERT(sysFile);
                }
                return result;
            } else {
                throw new Exception("Uploaded file list is empty");
            }
        } catch (Exception e) {
            System.err.println("File upload exception: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("File upload failed: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    public void fileDownload(String file_id, HttpServletResponse response) throws Exception {
        SysFileDetail searchParam = new SysFileDetail();
        searchParam.setFile_id(file_id);
        List<SysFileDetail> details = sysFileDetailMapper.SELECT(searchParam);

        if (details != null && !details.isEmpty()) {
            SysFileDetail detail = details.get(0);
            Map<String, Object> param = new HashMap<>();
            param.put("file_path", detail.getFile_path());
            param.put("file_id", detail.getFile_id());
            param.put("file_nm", detail.getFile_nm());
            param.put("file_ext", detail.getFile_ext());

            downloadFile(param, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void fileDownloadByUuid(String file_uuid, HttpServletResponse response) throws Exception {
        SysFileDetail searchParam = new SysFileDetail();
        searchParam.setFile_uuid(file_uuid);
        List<SysFileDetail> details = sysFileDetailMapper.SELECT(searchParam);

        if (details != null && !details.isEmpty()) {
            // Get the first file if multiple exist for the UUID
            SysFileDetail detail = details.get(0);
            Map<String, Object> param = new HashMap<>();
            param.put("file_path", detail.getFile_path());
            param.put("file_id", detail.getFile_id());
            param.put("file_nm", detail.getFile_nm());
            param.put("file_ext", detail.getFile_ext());

            downloadFile(param, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void filePreviewByUuid(String file_uuid, HttpServletResponse response) throws Exception {
        SysFileDetail searchParam = new SysFileDetail();
        searchParam.setFile_uuid(file_uuid);
        List<SysFileDetail> details = sysFileDetailMapper.SELECT(searchParam);

        if (details != null && !details.isEmpty()) {
            SysFileDetail detail = details.get(0);
            Map<String, Object> param = new HashMap<>();
            param.put("file_path", detail.getFile_path());
            param.put("file_id", detail.getFile_id());
            param.put("file_nm", detail.getFile_nm());
            param.put("file_ext", detail.getFile_ext());

            previewFile(param, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    // public void zipFileDownload(List<Map<String,Object>> params,
    // HttpServletResponse response) throws Exception{
    // String zipFileName = (String) params.get(0).get("file_zip_file_nm");
    // downloadZipFile(downloadZipFileList(params),response,zipFileName);
    // }
    // public String getImage(Map<String,Object> request) throws
    // MalformedURLException {
    // String baseUrl = (String) request.get("BASE_URL");
    // String base_path = (String) request.get("file_path");
    // String fileId = (String) request.get("file_id");
    // // 파일 존재 여부 확인
    // Path filePath = Paths.get(base_path, fileId);
    // if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
    // return ""; // 파일이 없으면 빈 문자열 반환
    // }
    //
    // return baseUrl +"?fileId="+fileId+"&basePath="+base_path;
    // }
}