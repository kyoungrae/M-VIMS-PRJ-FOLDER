/**
 * @title : 현재 화면 정보 저장 (브라우저 새로고침 복원용)
 */
FormUtility.prototype.saveCurrentPage = function (reqUrl, prefixUrl) {
    if (!reqUrl || reqUrl === "/index/index" || reqUrl === "/") {
        return;
    }
    const pageInfo = { url: reqUrl };
    if (prefixUrl) {
        pageInfo.prgmUrl = prefixUrl;
    }
    sessionStorage.setItem("currentPage", JSON.stringify(pageInfo));
}

/**
 * @title : 새로고침 시 마지막 화면 복원
 */
FormUtility.prototype.restoreCurrentPageContent = function () {
    let currentPageItem = null;
    try {
        currentPageItem = JSON.parse(sessionStorage.getItem("currentPage") || "null");
    } catch (e) {
        currentPageItem = null;
    }

    if (!currentPageItem || !this.checkEmptyValue(currentPageItem.url)) {
        currentPageItem = new Session().getItem("recentPage");
    }

    if (!currentPageItem || !this.checkEmptyValue(currentPageItem.url)) {
        return;
    }

    const url = currentPageItem.url;
    if (url === "/index/index" || url === "/") {
        return;
    }

    let prgmUrl = currentPageItem.prgmUrl;
    if (!this.checkEmptyValue(prgmUrl)) {
        let $menu = $("[data-page-name='" + url + "'].sideNavPageLoad");
        if (url === "/bbs/view") {
            const data = new Session().getItem("DATA");
            if (data && data.bbs_id) {
                $menu = $("[data-page-name='/bbs/view'][data-menu-code='" + data.bbs_id + "']");
            }
        }
        if ($menu.length > 0) {
            prgmUrl = $menu.data("prgm-url");
        } else if (url === "/user/myinfo") {
            prgmUrl = "cms";
        } else {
            return;
        }
    }

    const data = new Session().getItem("DATA");
    const payload = data !== null && data !== undefined ? data : "";
    this.apiLoadContent(prgmUrl, url, payload);
}

/**
 * @title : 메뉴 활성화
 * @reqUrl : 요청 URL [String]
 * @text : 현재 페이지에 맞는 메뉴를 활성화하고 세션에 저장
 */
FormUtility.prototype.activatedMenu = function (reqUrl) {
    let sessionRecentPage = sessionStorage.getItem("recentPage");
    if (!sessionRecentPage) return;
    let sessionUrl = JSON.parse(sessionRecentPage).url;

    if (sessionUrl !== "/index/index" && sessionUrl !== "/" && sessionUrl !== "/common/myinfo" && (!reqUrl.includes("Register") || !reqUrl.includes("Modify"))) {
        if (sessionUrl.startsWith('/safety/safetyInspection')) sessionUrl = sessionUrl.replaceAll('New', '').replaceAll('Continuous', '');
        let refineRecentPage = sessionUrl.replaceAll("Detail", "List").replaceAll("Register", "List").replaceAll("Modify", "List").replaceAll("_2", "");
        let refineRecentPageInfo = {
            url: refineRecentPage
        };
        sessionStorage.setItem("recentPage", JSON.stringify(refineRecentPageInfo));
    }

    if (reqUrl.slice(-6) !== "Detail" && reqUrl.slice(-6) !== "Modify" && reqUrl !== "/index/index" && reqUrl !== "/common/myinfo") {
        let activatedMenuInfo = {
            url: reqUrl
        }
        sessionStorage.setItem("activatedMenu", JSON.stringify(activatedMenuInfo));

        // [중요] 전체 리셋 제거: 기존에 활성화된 페이지 아이템만 클래스 제거
        $("#side_nav_menu .active.page").removeClass("active page");

        if (formUtil.checkEmptyValue(reqUrl) && reqUrl !== "/index/index" && reqUrl !== "/common/myinfo") {
            let $activatedMenu;

            // 게시판 동적 경로 체크
            if (reqUrl === "/bbs/view") {
                let data = JSON.parse(sessionStorage.getItem("DATA"));
                let bbsId = data ? data.bbs_id : null;
                if (bbsId) {
                    $activatedMenu = $("[data-page-name='/bbs/view'][data-menu-code='" + bbsId + "']");
                }
            }

            if (!$activatedMenu || $activatedMenu.length === 0) {
                // 게시판처럼 URL이 겹치는 경우 광범위한 매칭 방지
                if (reqUrl !== "/bbs/view") {
                    $activatedMenu = $("[data-page-name='" + reqUrl.toString() + "']");
                }
            }

            if ($activatedMenu && $activatedMenu.length > 0) {
                // 1. 정확한 경로 추적
                let $targetLiPath = $activatedMenu.parents("li");

                // 2. 다른 브랜치(타겟 경로가 아닌 것)들을 부드럽게 정리
                $("#side_nav_menu > li").each(function () {
                    let $topLi = $(this);
                    if (!$targetLiPath.is($topLi)) {
                        $topLi.find("a.gi-side-nav-title.active").removeClass("active").addClass("collapsed");
                        $topLi.find("ul.gi-side-nav-menu-level:visible").stop(true).slideUp(250);
                    }
                });

                // 3. 페이지 하이라이트 정리 및 타겟 활성화
                $("#side_nav_menu .active.page").not($activatedMenu).removeClass("active page");
                $activatedMenu.addClass("active page");

                // 4. 조상 경로를 따라가며 필요한 요소만 정확히 펼치기
                $activatedMenu.parents("li").each(function () {
                    let $parentLi = $(this);
                    let $parentTitle = $parentLi.children("a.gi-side-nav-title");
                    let $targetSubmenu = $parentLi.children("ul.gi-side-nav-menu-level");

                    if ($parentTitle.length > 0) {
                        $parentTitle.removeClass("collapsed").addClass("active");
                    }

                    if ($targetSubmenu.length > 0 && !$targetSubmenu.is(":visible")) {
                        $targetSubmenu.stop(true).slideDown(250);
                    } else if ($targetSubmenu.length > 0) {
                        $targetSubmenu.show();
                    }
                });
            }
        }
    }
}

/**
 * @title : 컨텐츠 로드
 * @reqUrl : 요청 URL [String]
 * @DATA : 전달할 데이터 [Object]
 * @text : 페이지 컨텐츠를 로드하고 세션에 데이터 저장
 */
FormUtility.prototype.loadContent = function (reqUrl, DATA) {
    let cont = JSON.stringify(DATA);
    let url = `/common/redirectPage/redirect?url=${encodeURIComponent(reqUrl + ".html")}`;
    axios.get(url).then(response => {
        console.log(response)
        formUtil.resetFormUtilityValue();
        let pageSources = response.data;

        $("#gi-road-content").empty().html(pageSources);

        if (!formUtil.checkEmptyValue(sessionStorage.getItem("DATA"))) {
            sessionStorage.removeItem("DATA");
            sessionStorage.setItem("DATA", cont);
        } else {
            sessionStorage.removeItem("DATA");
            sessionStorage.setItem("DATA", cont);
        }

        if (typeof changedHomeType !== "undefined" && formUtil.checkEmptyValue(changedHomeType)) {
            $("#gi-road-content article:first").each(function () {
                this.style.setProperty("width", "100%", "important");
                if (!$(this).hasClass("gi-col-99") && !$(this).hasClass("gi-col-100")) {
                    this.style.setProperty("height", "98%", "important");
                }
            });

            $("#gi-road-content article#gi-search-popup").addClass("gi-row-84-important");
        }

        formUtil.activatedMenu(reqUrl);
        formUtil.saveCurrentPage(reqUrl, null);
    })
        .catch(error => {
            console.log(error)
            formUtil.toast('Failed to load content:', 'error');
        });
}
/**
 * @title : API Gateway를 통한 컨텐츠 로드
 * @text : API Gateway를 통한 loadContent
 * @param prefixUrl : API 프리픽스 URL [String]
 * @param reqUrl : 요청 URL [String]
 * @param DATA : 전달할 데이터 [Object]
 */
FormUtility.prototype.apiLoadContent = function (prefixUrl, reqUrl, DATA) {
    let cont = JSON.stringify(DATA);
    let url = "";

    // 동적 컨트롤러 호출 여부 판단 (여기서는 /bbs/view 등 특정 경로)
    // 기존의 모든 관리 페이지(List, Detail, Register 등)는 정적 템플릿 로딩 방식을 유지해야 함
    let isDynamicRoute = reqUrl === "/bbs/view";

    if (isDynamicRoute && typeof DATA === 'object' && DATA !== null) {
        url = prefixUrl + reqUrl;
        axios.post(url, DATA).then(response => {
            formUtil.resetFormUtilityValue();
            let pageSources = response.data;
            sessionStorage.setItem("DATA", cont);
            $("#gi-road-content").empty().html(pageSources);
            formUtil.activatedMenu(reqUrl);
            formUtil.saveCurrentPage(reqUrl, prefixUrl);
        }).catch(error => {
            formUtil.toast('Failed to load content:', 'error');
        });
    } else {
        // 기존의 정적 페이지(.html) 로딩 방식 유지
        url = prefixUrl + `/page/load` + `?url=${encodeURIComponent(reqUrl + ".html")}`;
        axios.get(url).then(response => {
            formUtil.resetFormUtilityValue();
            let pageSources = response.data;
            sessionStorage.setItem("DATA", cont);
            $("#gi-road-content").empty().html(pageSources);
            formUtil.activatedMenu(reqUrl);
            formUtil.saveCurrentPage(reqUrl, prefixUrl);
        }).catch(error => {
            formUtil.toast('Failed to load content:', 'error');
        });
    }
}
/**
 * @title : HTML 파일 로드
 * @cont :[url:url,data:data]
 * @text : html코드를 사입 하기 위한 함수 함수앞에 awaite 추가  ex) awaite formUtil.loadToHtml(cont)
 * @writer : 이경태
 */
FormUtility.prototype.loadToHtml = async function (cont) {
    return new Promise(resolve => {
        let url = `/common/redirectPage/redirect?url=${encodeURIComponent(cont.url + ".html")}`;
        let data = JSON.stringify(cont.data);

        axios.get(url).then(response => {
            if (formUtil.checkEmptyValue(response)) {
                return resolve(response.data);
            }
        }).catch(error => {
            formUtil.toast('Failed to load content:', 'error');
        });
    })
}
/**
 * @title : 페이지 이동 애니메이션
 * @text : 페이지 이동시 애니메이션 설정
 * @writer : 이경태
 */
FormUtility.prototype.pageReDirectAnimation = function () {
    if ($("#gi-road-content").data("animation")) {
        $(".gi-article-content").addClass("animate-content-start");
    } else {
        $(".gi-article-content").addClass("animate-content-end");
        $("#gi-road-content").data("animation", true);
    }
}

/**
 * @title : 페이지 제목 설정
 * @text : 페이지 이동시 애니메이션 설정
 * @writer : 이경태
 */
FormUtility.prototype.setTitle = function () {
    if (formUtil.checkEmptyValue(sessionStorage.getItem("DATA"))) {
        let data = JSON.parse(sessionStorage.getItem("DATA"));
        $(".gi-page-title").html(data.title)
    }
}
/**
 * @title : 툴팁 처리
 * @writer : 문상혁
 */
FormUtility.prototype.handleToolTip = function () {
    $(".gi-tooltip-info-icon").hover(
        function () {
            $(this).siblings(".gi-tooltip-info-text").removeClass("gi-hidden");
        },
        function () {
            $(this).siblings(".gi-tooltip-info-text").addClass("gi-hidden");
        },
    );
}
