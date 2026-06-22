/**
 *  ++ giens Product ++
 */
package com.vims.common.bbs;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.vims.fmsClient.ExcelDataResponse;
import com.vims.fmsClient.FmsExcelClient;
import org.springframework.beans.factory.annotation.Value;


import java.util.List;

@Service
@RequiredArgsConstructor
public class SysBbsMstService extends AbstractCommonService<SysBbsMst> {
    private final SysBbsMstMapper sysBbsMstMapper;
    private final MessageSource messageSource;
    private final FmsExcelClient fmsExcelClient; // FMS 서비스 통신용 Feign Client

    @Value("${fms.internal.api-key}")
    private String fmsInternalApiKey; // 내부 API 키 (application.yml에서 주입)

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    @Override
    protected List<SysBbsMst> selectPage(SysBbsMst request) throws Exception {
        try{
            return sysBbsMstMapper.SELECT_PAGE(request);
        }catch (Exception e){
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysBbsMst request) throws Exception {
        try{
          return sysBbsMstMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        }catch (Exception e){
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }
    @Override
    protected List<SysBbsMst> findImpl(SysBbsMst request) throws Exception {
        try{
            return sysBbsMstMapper.SELECT(request);
        }catch (Exception e){
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }

    }

    @Override
    protected int removeImpl(SysBbsMst request) {
        try{
            return sysBbsMstMapper.DELETE(request);
        }catch (Exception e){
            throw new CustomException(getMessage("EXCEPTION.REMOVE"));
        }
    }

    @Override
    protected int updateImpl(SysBbsMst request) {
        try{
            return sysBbsMstMapper.UPDATE(request);
        }catch (Exception e){
            throw new CustomException(getMessage("EXCEPTION.UPDATE"));
        }
    }

    @Override
    protected int registerImpl(SysBbsMst request){
        try{
            return sysBbsMstMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        }

    }
    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        try {
            // FMS 서비스의 엑셀 업로드 API 호출
            ExcelDataResponse excelData = fmsExcelClient.uploadExcel(file, fmsInternalApiKey);
            System.out.println("excelData::::" + excelData);
            // 엑셀 데이터 검증
            if (excelData == null || excelData.getDataRows() == null || excelData.getDataRows().isEmpty()) {
                throw new CustomException(getMessage("EXCEPTION.FMS.NO_DATA"));
            }
            return 0;

        } catch (IllegalArgumentException e) {
            throw new CustomException(getMessage("EXCEPTION.FMS.INVALID_FILE_FORMAT"));
        } catch (SecurityException e) {
            throw new CustomException(getMessage("EXCEPTION.FMS.ACCESS_DENIED"));
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.FMS.UPLOAD_ERROR"));
        }
    }
}
