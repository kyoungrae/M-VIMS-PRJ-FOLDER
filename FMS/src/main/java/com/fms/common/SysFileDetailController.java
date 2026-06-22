package com.fms.common;

import com.system.common.base.AbstractCommonController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fms/common/file/sysFileDetail")
@RequiredArgsConstructor
public class SysFileDetailController extends AbstractCommonController<SysFileDetail> {

    private final SysFileDetailService sysFileDetailService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysFileDetail reqeust) throws Exception {
        return sysFileDetailService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    public List<SysFileDetail> findAll(@RequestBody SysFileDetail request) throws Exception {
        return sysFileDetailService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    public List<SysFileDetail> findImpl(@RequestBody SysFileDetail request) throws Exception {
        return sysFileDetailService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    public int removeImpl(@RequestBody SysFileDetail request) {
        return sysFileDetailService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    public int updateImpl(@RequestBody SysFileDetail request) {
        return sysFileDetailService.updateImpl(request);
    }

    @Override
    protected int registerImpl(SysFileDetail request) {
        return 0;
    }

    @PostMapping("/register")
    public int registerImpl(@RequestBody List<SysFileDetail> fileList) throws Exception {
        return sysFileDetailService.registerImpl(fileList);
    }

    @PostMapping("/removeByFileIdAndUuid")
    public int removeByFileIdAndUuid(@RequestBody SysFileDetail request) throws Exception {
        return sysFileDetailService.removeByFileIdAndUuid(request);
    }

    @PostMapping("/removeByFileUuid")
    public int removeByFileUuid(@RequestBody SysFileDetail request) throws Exception {
        return sysFileDetailService.removeByFileUuid(request);
    }

    @PostMapping("/updateList")
    public int updateList(@RequestBody List<SysFileDetail> fileList) throws Exception {
        return sysFileDetailService.updateList(fileList);
    }

    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile arg0) throws Exception {
        return 0;
    }

}
