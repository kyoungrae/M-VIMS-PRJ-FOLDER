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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysBbsService extends AbstractCommonService<SysBbs> {
    private final SysBbsMapper sysBbsMapper;
    private final MessageSource messageSource;
    private final FmsExcelClient fmsExcelClient; // FMS 서비스 통신용 Feign Client
    private final com.vims.common.menu.SysMenuService sysMenuService;
    private final com.vims.common.accessgroupmenu.SysAccsGroupMenuMapper sysAccsGroupMenuMapper;
    private final SysBbsBoardMapper sysBbsBoardMapper;

    @Value("${fms.internal.api-key}")
    private String fmsInternalApiKey; // 내부 API 키 (application.yml에서 주입)

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysBbs> selectPage(SysBbs request) throws Exception {
        try {
            return sysBbsMapper.SELECT_PAGE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysBbs request) throws Exception {
        try {
            return sysBbsMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected List<SysBbs> findImpl(SysBbs request) throws Exception {
        try {
            return sysBbsMapper.SELECT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }

    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    protected int removeImpl(SysBbs request) {
        try {
            // 1. 해당 게시판(메뉴 코드 겸용)에 연결된 권한 그룹 데이터가 있는지 확인
            com.vims.common.accessgroupmenu.SysAccsGroupMenu accessParam = com.vims.common.accessgroupmenu.SysAccsGroupMenu
                    .builder()
                    .menu_cd(request.getBbs_id())
                    .build();
            List<com.vims.common.accessgroupmenu.SysAccsGroupMenu> acList = sysAccsGroupMenuMapper.SELECT(accessParam);

            if (!acList.isEmpty()) {
                // 권한 데이터가 존재하면 삭제 중단 및 예외 발생
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.ACCESS_RIGHTS_GROUP_DATA"));
            }

            // 2. 해당 게시판에 게시물 데이터가 존재하는지 확인
            SysBbsBoard boardParam = SysBbsBoard.builder()
                    .bbs_id(request.getBbs_id())
                    .build();
            List<SysBbsBoard> boardList = sysBbsBoardMapper.SELECT(boardParam);

            if (!boardList.isEmpty()) {
                // 게시물 데이터가 존재하면 삭제 중단 및 예외 발생
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.BOARD_DATA"));
            }

            // 2. 게시판 삭제 시 관련 메뉴도 삭제 시도 (종속 관계)
            com.vims.common.menu.SysMenu menu = com.vims.common.menu.SysMenu.builder()
                    .menu_cd(request.getBbs_id())
                    .build();
            try {
                sysMenuService.remove(menu);
            } catch (Exception e) {
                System.err.println("Menu removal failed for board: " + e.getMessage());
            }

            // 3. SYS_BBS 테이블에서 데이터 삭제
            return sysBbsMapper.DELETE(request);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.REMOVE"));
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    protected int updateImpl(SysBbs request) {
        try {
            int result = sysBbsMapper.UPDATE(request);
            if (result > 0) {
                // 게시판 수정 시 관련 메뉴도 업데이트 시도
                com.vims.common.menu.SysMenu menu = com.vims.common.menu.SysMenu.builder()
                        .menu_cd(request.getBbs_id())
                        .menu_nm_kr(request.getBbs_nm())
                        .top_menu_cd(request.getUp_menu_cd())
                        .url("/bbs/view?bbsId=" + request.getBbs_id())
                        .build();
                try {
                    sysMenuService.update(menu);
                } catch (Exception e) {
                    System.err.println("Menu update failed for board: " + e.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.UPDATE"));
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    protected int registerImpl(SysBbs request) {
        try {
            // 1. 게시판 ID 생성 및 게시판 정보 사전 등록
            UUID uuid = UUID.randomUUID();
            request.setBbs_id(uuid.toString());

            // 2. SYS_BBS 테이블에 먼저 저장
            int result = sysBbsMapper.INSERT(request);

            // 3. 게시판 등록 성공 시에만 메뉴 테이블(SYS_MENU)에 등록
            if (result > 0 && request.getUp_menu_cd() != null && !request.getUp_menu_cd().isEmpty()) {
                com.vims.common.menu.SysMenu menu = com.vims.common.menu.SysMenu.builder()
                        .menu_cd(request.getBbs_id()) // 게시판 ID를 메뉴 코드로 사용
                        .menu_nm_kr(request.getBbs_nm())
                        .top_menu_cd(request.getUp_menu_cd())
                        .url("/bbs/view")
                        .prgm_url("cms") // Gateway용 prefix 추가
                        .use_yn("1") // 기본 사용
                        .menu_lvl("2") // 게시판은 2레벨
                        .menu_no("1")
                        .build();
                try {
                    sysMenuService.register(menu);
                } catch (Exception e) {
                    // 메뉴 등록 실패 시 게시판 등록도 취소되도록 예외 던짐 (Transactional)
                    throw new CustomException("게시판 메뉴 등록 중 오류가 발생했습니다: " + e.getMessage());
                }
            }
            return result;
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        } catch (Exception e) {
            throw new CustomException("게시판 저장 중 오류가 발생했습니다: " + e.getMessage());
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
