package com.vims.common.bbs;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cms/bbs")
@RequiredArgsConstructor
public class BbsPageController {

    private final SysBbsService sysBbsService;
    private final SysBbsMstService sysBbsMstService;
    private final com.system.common.util.pageredirect.PageRedirectService pageRedirectService;

    @PostMapping("/view")
    public String viewBoard(@RequestBody SysBbs request) throws Exception {
        String bbsId = request.getBbs_id();
        // 1. 게시판 정보 조회
        SysBbs sysBbs = new SysBbs();
        sysBbs.setBbs_id(bbsId);
        List<SysBbs> bbsList = sysBbsService.findImpl(sysBbs);

        if (bbsList == null || bbsList.isEmpty()) {
            return "<div>게시판 정보를 찾을 수 없습니다.</div>";
        }
        SysBbs currentBbs = bbsList.get(0);

        // 2. 게시판 타입 조회 (Master 정보)
        SysBbsMst sysBbsMst = new SysBbsMst();
        sysBbsMst.setBbs_mst_id(currentBbs.getBbs_mst_id());
        List<SysBbsMst> mstList = sysBbsMstService.findImpl(sysBbsMst);

        String layoutType = "basic"; // 기본값
        if (mstList != null && !mstList.isEmpty()) {
            String type = mstList.get(0).getBbs_type();
            if (type != null) {
                layoutType = type.toLowerCase();
            }
        }

        // 3. 레이아웃 리턴 (시스템 공통 pageLoad 방식 사용)
        return pageRedirectService.pageLoad("/bbs/bbsLayout_" + layoutType + ".html");
    }
}
