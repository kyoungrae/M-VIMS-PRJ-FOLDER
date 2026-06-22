package com.system.common.base;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public abstract class AbstractCommonController<T> implements CommonController<T> {
    @Override
    public List<T> find(@RequestBody T request) throws Exception {
        return findImpl(request);
    }

    @Override
    public int remove(@RequestBody T request) throws Exception {
        return removeImpl(request);
    }

    @Override
    public int update(@RequestBody T request) throws Exception {
        return updateImpl(request);
    }

    @Override
    public int register(@RequestBody T request) throws Exception {
        return registerImpl(request);
    }

    protected abstract List<T> findImpl(T request) throws Exception;

    protected abstract int removeImpl(T request) throws Exception;

    protected abstract int updateImpl(T request) throws Exception;

    protected abstract int registerImpl(T request) throws Exception;

    protected abstract int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception;

}
