package com.fms.file;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class FileProcessManager implements FileProcessManagerImpl {
    private final String applicationFileName = "application.properties";
    private final String directory;
    private final String fileSize;
    private final String fileUnit;
    private final long MAX_FILE_SZ;

    public FileProcessManager() {
        // 프로퍼티 로드
        Properties properties = loadProperties(applicationFileName);
        this.directory = properties.getProperty("common.file.directory.path");
        this.fileSize = properties.getProperty("common.file.max.size");
        this.fileUnit = properties.getProperty("common.file.max.size.unit");
        this.MAX_FILE_SZ = fileSizeDefinition(fileSize, fileUnit);
    }

    private Properties loadProperties(String propertyFileName) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertyFileName)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + propertyFileName);
                return properties; // 빈 프로퍼티 반환
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        return properties;
    }

    public Long fileSizeDefinition(String size, String unit) {
        long result = 0L;
        try {
            if (unit.equalsIgnoreCase("GB")) {
                result = Long.parseLong(size) * 1024 * 1024 * 1024;
            } else if (unit.equalsIgnoreCase("MB")) {
                result = Long.parseLong(size) * 1024 * 1024;
            } else if (unit.equalsIgnoreCase("KB")) {
                result = Long.parseLong(size) * 1024;
            } else {
                System.err.println("Invalid unit: " + unit);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid size: " + size);
            System.err.println(e.getMessage());
        }
        return result;
    }

    @Override
    public boolean isValidDirectory(String directory) {
        Path path = Paths.get(directory);
        return Files.exists(path) && Files.isDirectory(path);
    }

    // @Override
    // public List<Map<String, Object>> searchfile(Map<String, Object> param) throws
    // Exception {
    // return null;
    // }
    //
    // @Override
    // public void createFile(String folder_name, String file_nm, String
    // file_content ,String ex){
    // String fileName = file_nm;
    // String fileContent = file_content;
    // String extensions = ex;
    // String folderName = folder_name;
    //
    // if(fileContent.getBytes().length > MAX_FILE_SZ){
    // System.err.println("File size exceeds the limit");
    // return;
    // }
    //
    // String directotyPath = directory;
    // if(!folderName.isEmpty()){
    // directotyPath = directory +"/"+folderName;
    // }
    //
    // if(!isValidDirectory(directotyPath)){
    // try {
    // Files.createDirectories(Paths.get(directotyPath));
    // System.out.println("Invalid directory path");
    // System.out.println("Directory created successfully");
    // } catch (IOException ioException) {
    // System.err.println("Failed to create directory");
    // System.err.println(ioException.getMessage());
    // return;
    // }
    // }
    // String filePath = "";
    // if(directotyPath.endsWith("/")){
    // filePath = directotyPath + fileName + extensions;
    // }else{
    // filePath = directotyPath +"/"+ fileName + extensions;
    // }
    //
    // try (FileOutputStream outputStream = new
    // FileOutputStream(filePath.toString())) {
    // outputStream.write(fileContent.getBytes());
    // System.out.println(filePath + ": File successfully created.");
    // } catch (IOException e) {
    // System.err.println("Error occurred while creating file: " + e.getMessage());
    // }
    //
    // }

    @Override
    public List<Map<String, Object>> uploadFile(String folder_name, MultipartFile[] files) throws IOException {
        String directoryPath = null;
        List<Map<String, Object>> result = new ArrayList<>();

        if (files != null && files.length > 0) {
            if (!folder_name.isEmpty()) {
                if (directory.endsWith("/")) {
                    directoryPath = directory + folder_name;
                } else {
                    directoryPath = directory + "/" + folder_name;
                }
            } else {
                directoryPath = directory;
            }

            // NOTE: 경로 조작(Path Traversal) 방어 - folder_name에 "../" 등이 포함되어
            //       베이스 디렉터리 밖으로 벗어나는 경우를 차단한다. (정규화된 경로 기준 포함 검사)
            File baseDir = new File(directory);
            File targetDir = new File(directoryPath);
            String baseCanonical = baseDir.getCanonicalPath();
            String targetCanonical = targetDir.getCanonicalPath();
            if (!targetCanonical.equals(baseCanonical)
                    && !targetCanonical.startsWith(baseCanonical + File.separator)) {
                throw new IOException("Invalid folder_name: path traversal detected");
            }
            directoryPath = targetCanonical;

            if (!isValidDirectory(directoryPath)) {
                try {
                    Files.createDirectories(Paths.get(directoryPath));
                    System.out.println("Invalid directory path");
                    System.out.println("Directory created successfully");
                } catch (IOException ioException) {
                    System.err.println(ioException.getMessage());
                    throw new IOException("Failed to create directory");
                }
            }
            int index = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    System.out.println("file.getOriginalFilename():" + file.getOriginalFilename());
                    System.out.println("file.getSize():" + file.getSize());

                    // NOTE: 최대 파일 크기 검증 복원. MAX_FILE_SZ가 0(프로퍼티 미설정)이면 검증을 건너뛴다.
                    if (MAX_FILE_SZ > 0 && file.getSize() > MAX_FILE_SZ) {
                        throw new IOException("File size exceeds the limit (" + MAX_FILE_SZ + " bytes): "
                                + file.getOriginalFilename());
                    }

                    UUID fileUUID = UUID.randomUUID();
                    Map<String, Object> map = new HashMap<>();
                    map.put("file_path", directoryPath);
                    map.put("file_id", String.valueOf(fileUUID));
                    result.add(map);

                    String originalFileName = file.getOriginalFilename();
                    List<String> list = Arrays.asList(Objects.requireNonNull(originalFileName).split("\\."));
                    String lastItem = list.stream().reduce((first, second) -> second).orElse(null);

                    map.put("file_nm", originalFileName);
                    map.put("file_sz", String.valueOf(file.getSize()));
                    map.put("file_ext", lastItem);

                    File uploadFile = new File(directoryPath + File.separator + fileUUID + "." + lastItem);
                    file.transferTo(uploadFile);
                    System.out.println("fileUUID::" + fileUUID);
                    System.out.println(uploadFile);

                }
                index++;
            }
            System.out.println("All files uploaded successfully");
        } else {
            System.out.println("No files uploaded");
        }
        return result;
    }

    public int fileDetailInsert(String fileUUID, String file_nm, String file_sz, String file_path) {
        try {

        } catch (Exception e) {

        }
        return 0;
    }

    @Override
    public void downloadFile(Map<String, Object> param, HttpServletResponse response) throws IOException {
        String filePath = (String) param.get("file_path");
        String fileId = (String) param.get("file_id");
        String extension = (String) param.get("file_ext");
        String originalFileName = (String) param.get("file_nm");

        String physicalFileName = fileId + (extension != null && !extension.isEmpty() ? "." + extension : "");
        Path path = Paths.get(filePath, physicalFileName);
        File file = path.toFile();

        if (file.exists()) {
            String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replaceAll("\\+",
                    "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength((int) file.length());

            try (InputStream inputStream = new FileInputStream(file)) {
                StreamUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                System.err.println("Download Error: " + e.getMessage());
                throw e;
            }
        } else {
            System.err.println("File not found: " + path.toString());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    //
    // @Override
    // public List<File> downloadZipFileList(List<Map<String,Object>> params) throws
    // IOException {
    // List<File> fileList = new LinkedList<>();
    // for(Map<String,Object> param : params){
    // String filePath = (String) param.get("file_path");
    // String fileName = (String) param.get("file_nm");
    // String fileOriginalName = (String) param.get("file_original_name");
    // Path sourcePath = Paths.get(filePath, fileName);
    // Path targetPath = Paths.get(filePath, fileOriginalName);
    //
    // try {
    // if (Files.exists(sourcePath)) {
    // Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    // fileList.add(targetPath.toFile());
    // System.out.println("Add FileList Success");
    //
    // } else {
    // throw new IOException("File not Exist");
    // }
    // } catch (IOException e) {
    // throw new IOException(e.getMessage());
    // }
    // }
    // return fileList;
    // }
    //
    // @Override
    // public void downloadZipFile(List<File> param, HttpServletResponse response ,
    // String zip_file_nm) throws Exception {
    // String zipFileName = zip_file_nm + ".zip";
    // String encodedFileName = URLEncoder.encode(zipFileName ,
    // StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    //
    // response.setHeader("Content-Disposition", "attachment; filename=" +
    // encodedFileName );
    // response.setContentType("application/zip");
    // response.setStatus(HttpServletResponse.SC_OK);
    //
    // try (ZipOutputStream zipOut = new
    // ZipOutputStream(response.getOutputStream())) {
    // for (File file : param) {
    // zipOut.putNextEntry(new ZipEntry(file.getName()));
    // try (FileInputStream fis = new FileInputStream(file)) {
    // StreamUtils.copy(fis, zipOut);
    // }
    // zipOut.closeEntry();
    // }
    // deleteFiles(param);
    // }
    // }
    // public void deleteFiles(List<File> fileList) throws IOException {
    // for (File file : fileList) {
    // Files.deleteIfExists(file.toPath());
    // }
    // }

    //

    // public String loadProperty(String propertyFileName, String propertyName) {
    // try{
    // Properties properties = new Properties();
    // properties.load(new FileInputStream(propertyFileName));
    // return properties.getProperty(propertyName);
    // }catch(IOException e){
    // System.err.println(e.getMessage());
    // return null;
    // }
    // }

}