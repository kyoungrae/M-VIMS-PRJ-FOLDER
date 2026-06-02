/**
 * @title : 그리드 생성
 * @text : 그리드 관련 유틸리티 (giGrid 등)
 */
FormUtility.prototype.giGrid = function (layout, paging, page, gridId) {
    let gridSortManager = formUtil.gridSortManager;
    //localStorage에서 정렬값을 가져와 setting
    gridSortManager.loadSortState();
    if (!formUtil.checkEmptyValue(paging)) paging = 1;
    if (!formUtil.checkEmptyValue(page)) page = 1;

    let title = layout.title;
    let grid_list_header = "";
    let headerItem = [];
    let prePageAnimationCont = $("#gi-grid-list-body").data("pageNumber");
    let currentPageAnimationCont = page
    let pagingAnimationClass = "";

    if (!formUtil.checkEmptyValue(gridId)) gridId = "gi-Grid";
    let gridData = [];
    let rowWarningFn = null;

    if (formUtil.checkEmptyValue(prePageAnimationCont)) {
        //애니메이션 효과 적용
        if (prePageAnimationCont > currentPageAnimationCont) {
            pagingAnimationClass = "tilt-in-left-1";
        } else if (prePageAnimationCont < currentPageAnimationCont) {
            pagingAnimationClass = "tilt-in-right-1";
        } else if (prePageAnimationCont === currentPageAnimationCont) {
            pagingAnimationClass = "fade-in";
        }
    }
    function typeTransferDateToString(v) {
        if (v === null || v === undefined || v === "") return "";
        const r = new Date(v);
        if (isNaN(r.getTime())) return "";

        const yyyy = String(r.getFullYear());
        const MM = String(r.getMonth() + 1).padStart(2, "0");
        const dd = String(r.getDate()).padStart(2, "0");
        const hh = String(r.getHours()).padStart(2, "0");
        const mm = String(r.getMinutes()).padStart(2, "0");
        const ss = String(r.getSeconds()).padStart(2, "0");

        return yyyy + '-' + MM + '-' + dd + ' ' + hh + ':' + mm + ":" + ss;
    }

    layout.list.map((item) => {
        let hidden = "";
        let sort = "";
        //그리드 데이터 각 row 생성하기 위해 데이터 담기
        headerItem.push({
            ID: item.ID,
            WIDTH: item.WIDTH,
            TEXT_ALIGN: item.TEXT_ALIGN,
            FONT_SIZE: item.FONT_SIZE,
            TYPE: item.TYPE,
            HEADER: item.HEADER,
            SYS_CD_GRP_ID: item.SYS_CD_GRP_ID,
            TARGET: item.TARGET,
            HIDDEN: item.HIDDEN,
            VISIBLE_OPTION_BTN: item.VISIBLE_OPTION_BTN
        });
        // //정렬 대상이라면 정렬순서 추가
        // if (gridSortManager.sortColumn !== null && gridSortManager.sortColumn !== undefined && gridSortManager.sortColumn.trim() !== '') {
        //     if (gridSortManager.sortColumn === item.ID) {
        //         sort = 'gi-grid-sort-'+gridSortManager.sortColumn;
        //     }
        // }

        //컬럼 히든처리
        if (formUtil.checkEmptyValue(item.HIDDEN) && item.HIDDEN) {
            hidden = "gi-hidden ";
        } else {
            hidden = "gi-show-li ";
        }

        let sortArray = gridSortManager.getSort();

        if (sortArray.order !== null) {
            if (item.ID === sortArray.column) {
                sort = 'gi-grid-sort-' + sortArray.order;
            }
        }
        // grid_list_header += '<li class="gi-row-' + item.WIDTH + ' gi-flex gi-flex-center gi-overflow-scroll gi-col-30px '+hidden+'">' +
        switch (item.TYPE) {
            case "checkbox":
                grid_list_header += '<li data-column="' + item.ID + '_checkbox_all" class="gi-flex-justify-content-center resizableBox gi-min-row-50px gi-row-' + item.WIDTH + ' gi-overflow-scroll gi-col-30px ' + hidden + '' + sort + '">' +
                    '<input type="checkbox" id="' + gridId + '_checkbox_all" class="gi-padding-left-right-10px"/>' +
                    '</li>';
                break;
            default:
                grid_list_header += '<li data-column="' + item.ID + '" class="resizableBox gi-min-row-50px gi-row-' + item.WIDTH + ' gi-overflow-scroll gi-col-30px ' + hidden + '' + sort + '">' +
                    '<span class="gi-width-100 gi-padding-left-right-10px gi-flex gi-flex-justify-content-center">' + item.HEADER + '</span>' +
                    '<div class="gridColumResizer"></div>' +
                    '</li>';
                break;
        }

    })

    let totalPageCount = Math.ceil(paging);
    let maxPagesToShow = 10;

    let startPage = Math.floor((page - 1) / maxPagesToShow) * maxPagesToShow + 1;
    let endPage = Math.min(totalPageCount, startPage + maxPagesToShow - 1);

    let pagingArea = '';
    let giGridPagingBtn = gridId + "_gi-grid-paging-btn";
    if (page > 1) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn" data-field="1"><i class="fa-solid fa-angles-left"></i></span>';
    }
    if (startPage > 1) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn gi-grid-paging-prev-btn" data-field="' + (startPage - maxPagesToShow) + '"><i class="fa-solid fa-chevron-left"></i></span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn" data-field="' + i + '">' + i + '</span>';
    }

    if (endPage < totalPageCount) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn gi-grid-paging-next-btn" data-field="' + totalPageCount + '"><i class="fa-solid fa-chevron-right"></i></span>';
    }

    //페이징 row 개수 설정
    let options = "";
    let giGridRowSelectorId = "gi-grid-row-selector_" + gridId;
    for (let i = 1; i < 11; i++) {
        let selectedOption = "";
        if (parseInt($("#" + giGridRowSelectorId + " option:selected").val()) === 10 * i) {
            selectedOption = "selected";
        }
        options += '<option value="' + 10 * i + '" ' + selectedOption + '>' + 10 * i + ' row</option>>'
    }

    let grid =
        '            <figure class="gi-figure-content gi-overflow-scroll gi-col-100 gi-row-100 gi-flex gi-flex-justify-content-center gi-flex gi-flex-direction-column">' +
        '                <div class="gi-article-content gi-min-col-90 gi-col-100 gi-row-100">' +
        // '                    <header class="gi-row-100 gi-col-5 gi-margin-bottom-1"><h4>' + title + '</h4></header>' +
        '                    <div class="gi-row-100 gi-flex gi-flex-justify-content-space-between gi-margin-bottom-1 ">' +
        '                        <select class="gi-grid-row-selector gi-row-65px" id="' + giGridRowSelectorId + '">' +
        options +
        '                        </select>' +
        '                       <div class="gi-flex gi-flex-justify-content-end gi-gap-3px">' +
        '                             <button id="excel-upload-btn_' + gridId + '" class="gi-excel-upload-btn" type="button">' +
        '                               <i class="fa-solid fa-file-excel"></i>' +
        '                               <span>excel upload</span>' +
        '                             </button>' +
        '                             <button id="excel-download-btn_' + gridId + '" class="gi-excel-download-btn" type="button">' +
        '                               <i class="fa-solid fa-file-excel"></i>' +
        '                               <span>excel download</span>' +
        '                             </button>' +
        '                       </div>' +
        '                    </div>' +
        '                    <div id="gi-grid-list-body" data-page-number="' + page + '" class="gi-row-100 gi-overflow-scroll gi-flex gi-flex-direction-column">' +
        '                        <ul class="gi-grid-list-header gi-row-100 gi-col-30px gi-ul gi-flex">' +
        grid_list_header +
        '                        </ul>' +
        '                    </div>' +
        '                </div>' +
        '                <div class="gi-grid-paging-content gi-col-5 gi-row-100">' +
        pagingArea +
        '                </div>' +
        '            </figure>';


    $("#" + gridId).html(grid);

    let items = $("#" + gridId).find(".gi-show-li");
    items.map((index, item) => {
        if (index !== items.length - 1) {
            item.style.borderRight = '1px solid #bbbbbb6e';
        }
    });

    // 초기 활성화 페이징 번호 설정
    $(`.${giGridPagingBtn}[data-field="${page}"]`).addClass("active");

    //그리드 생성 후 데이터 바인딩
    return {
        //그리드 데이터 설정
        DataSet: async function (data) {
            gridData = data;

            // RowWarning 설정이 있는 경우 실행
            if (rowWarningFn && data) {
                data.forEach(item => {
                    let msg = rowWarningFn(item);
                    if (msg) {
                        item.ROW_WARN_MSG = msg;
                    }
                });
            }

            let flag = formUtil.checkEmptyValue(data);
            let grid_list = "";
            let sysCodeGroupIdArray = [];
            //NOTE: rows BTN 노출 이벤트 로직 설정
            let visibleOptionArray = [];
            headerItem.map(item => {
                if (formUtil.checkEmptyValue(item.VISIBLE_OPTION_BTN)) {
                    item.VISIBLE_OPTION_BTN["BTN_ID"] = item.ID;
                    visibleOptionArray.push(item.VISIBLE_OPTION_BTN);
                }
            })

            if (flag) {
                for (let i = 0; i < data.length; i++) {
                    let originalDataForVisibleOption = [];
                    let warnClass = "";
                    let warnMsgAttr = "";

                    if (formUtil.checkEmptyValue(data[i].ROW_WARN_MSG)) {
                        warnClass = "gi-grid-list-warn";
                        warnMsgAttr = ' data-warn-msg="' + data[i].ROW_WARN_MSG + '"';
                    }

                    grid_list += '<ul class="gi-grid-list gi-row-100 gi-ul gi-flex ' + pagingAnimationClass + ' ' + warnClass + '" ' + warnMsgAttr + ' data-row-num="' + i + '">';

                    for (let j = 0; j < headerItem.length; j++) {
                        let item = headerItem[j];
                        let tag = "";
                        let sysCodeName = "";
                        let sysCodeValue = "";
                        let hidden = "";
                        if (formUtil.checkEmptyValue(item.SYS_CD_GRP_ID)) {
                            sysCodeName = await checkSameCode(sysCodeGroupIdArray, item.SYS_CD_GRP_ID, data[i]);
                            sysCodeValue = data[i][item.ID];
                        } else {
                            sysCodeName = data[i][item.ID];
                        }
                        if (formUtil.checkEmptyValue(item.HIDDEN)) {
                            if (item.HIDDEN) {
                                hidden = "hidden";
                            } else {
                                hidden = "";
                            }
                        }

                        //NOTE: rows BTN 노출 이벤트 필터링
                        visibleOptionArray.map(visibleOptionKeys => {
                            if (formUtil.checkEmptyValue(visibleOptionKeys.length)) {
                                visibleOptionKeys.map(ArrItem => {
                                    if (Object.keys(ArrItem)[0] === item.ID) {
                                        if (ArrItem[item.ID] === sysCodeName + "") {
                                            originalDataForVisibleOption[visibleOptionKeys.BTN_ID] = "true";
                                        }
                                    }
                                })
                            } else {
                                if (Object.keys(visibleOptionKeys)[0] === item.ID) {
                                    if (visibleOptionKeys[item.ID] === sysCodeName + "") {
                                        originalDataForVisibleOption[visibleOptionKeys.BTN_ID] = "true";
                                    }
                                }
                            }
                        })

                        if (!formUtil.checkEmptyValue(sysCodeName)) sysCodeName = "";
                        var hasVisibleOption = headerItem.some(h => h.ID === item.ID && formUtil.checkEmptyValue(h.VISIBLE_OPTION_BTN));
                        switch (item.TYPE) {
                            case "text":
                                sysCodeValue
                                    ?
                                    tag = '<span class="resizer gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" data-grid-value="' + sysCodeValue + '">' + sysCodeName + '</span>'
                                    :
                                    tag = '<span class="resizer gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '">' + sysCodeName + '</span>';
                                break;
                            case "button":
                                // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                if (hasVisibleOption) {
                                    // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                    if (originalDataForVisibleOption[item.ID] !== "true") {
                                        tag = '<button type="button" id="' + item.ID + "_" + i + '" class="gi-grid-btn gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + item.HEADER + '</button>';
                                    }
                                } else {
                                    tag = '<button type="button" id="' + item.ID + "_" + i + '" class="gi-grid-btn gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + item.HEADER + '</button>';
                                }
                                break;
                            case "map":
                                tag = '<span id="' + item.ID + "_" + i + '" class="gi-map-btn gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '</span>';
                                break;
                            case "password":
                                tag = '<span class="resizer gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" data-grid-value="' + sysCodeValue + '">••••••••</span>';
                                break;
                            case "checkbox":
                                tag = '<input type="checkbox" id="' + gridId + '_checkbox_' + i + '" class="gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" value="' + data[i][item.ID] + '" />';
                                break;
                            case "date":
                                tag = '<span class="resizer gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" data-grid-value="' + typeTransferDateToString(data[i][item.ID]) + '">' + typeTransferDateToString(data[i][item.ID]) + '</span>';
                                break;
                            case "img":
                                switch (item.ID) {
                                    case "detail_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/detail.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/detail.png"></button>';
                                        };
                                        break;
                                    case "update_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/pen.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/pen.png"></button>';
                                        };
                                        break;
                                    case "delete_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/trash.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/trash.png"></button>';
                                        };
                                        break;
                                    case "select_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/select.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/select.png"></button>';
                                        };
                                        break;
                                    case "redirect_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/redirect.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/redirect.png"></button>';
                                        };
                                        break;

                                }
                                break;
                        }

                        grid_list += '<li class="resizableBox gi-min-row-50px gi-row-' + item.WIDTH + ' gi-col-16px gi-flex gi-overflow-scroll gi-flex-justify-content-' + item.TEXT_ALIGN + ' gi-text-align-' + item.TEXT_ALIGN + ' ' + hidden + '" data-grid-row="' + j + '" data-field="' + item.ID + '">' + tag + '</li>';
                    }
                    grid_list += '</ul>';
                }
            } else {
                grid_list = '<div class="gi-row-100 gi-col-100 gi-flex gi-flex-align-items-center gi-flex-justify-content-center bounce-in-top"><div class="grid-no-data"><img src="../common/img/doc.png"><span class="gi-text-align-center">No Data</span></div></div>';
                $("#" + gridId + " .gi-grid-paging-content").html('');
            }

            $("#" + gridId + " .gi-grid-list-header").after(grid_list);

            let rows = $("#" + gridId + " .gi-grid-list");
            rows.map((i, row) => {
                let rowInLi = $(row).find("li");

                $(rows).not("[data-row-num='0']").addClass("border-top-dotted-gray");
                $(rowInLi).not(":last").addClass("gi-grid-li-border-dotted");
                unUsedMenuUISettings(row);
            })

            function unUsedMenuUISettings(e) {
                let flag = $(e).find("li[data-field='use_yn']").not(".hidden").find("span[data-grid-value]").length === 0;
                let a = ""; //NOTE: 그리드 내부에 SYS_CD_GRP_ID 함수로 인해 값이 동적으로 변화 하는걸 대비(공통코드 적용시 text, 미적용시 interger)
                let b = "";
                let c = "";

                if (flag) {
                    a = $(e).find("li[data-field='use_yn']").not(".hidden").find("span").text();
                    b = "0";
                    c = "1";
                } else {
                    a = $(e).find("li[data-field='use_yn']").not(".hidden").find("span").data("gridValue");
                    b = 0;
                    c = 1;
                }

                if (a === b) {
                    $(e).addClass("unused-menu");
                } else {
                    if (a === b && a === c) {
                        $(e.$row).removeClass("unused-menu");
                    }
                }
            }
        },
        //그리드 row 개수 변경 및 페이징 버튼 이벤트 설정
        pagingSet: function (fn) {
            let range = "";

            $("#" + giGridRowSelectorId).change(function () {
                range = parseInt($("#" + giGridRowSelectorId + " option:selected").val());
                $("#" + giGridRowSelectorId).val(range);
                fn(1, range);
            })
            $("." + giGridPagingBtn).click(function () {
                // 기존에 활성화된 페이징 넘버에서 active 클래스를 제거
                $("." + giGridPagingBtn).removeClass("active");

                // 현재 클릭된 페이징 넘버에 active 클래스 추가
                $(this).addClass("active");

                let pagingNum = $(this).data("field");
                range = parseInt($("#" + giGridRowSelectorId + " option:selected").val());
                fn(pagingNum, range);
            })
        },
        // 로우 경고 설정 (재사용 가능한 공통 컴포넌트 기능)
        // 1. 함수 방식: RowWarning(function(item) { return msg; })
        // 2. 선언적 방식: RowWarning(columnId, conditionValue, message)
        RowWarning: function (arg1, arg2, arg3) {
            if (typeof arg1 === 'function') {
                rowWarningFn = arg1;
            } else if (arg1 && arg3 !== undefined) {
                rowWarningFn = function (item) {
                    let val = item[arg1];
                    let isMatch = false;
                    if (typeof arg2 === 'function') {
                        isMatch = arg2(val);
                    } else {
                        isMatch = (val == arg2);
                    }
                    return isMatch ? arg3 : null;
                };
            }
            return this;
        },
        //그리드 내부의 상세 버튼 클릭 이벤트 설정(버튼클릭시 호출될 함수, 그리드 헤더 부분에 설정한 버튼 ID)
        detailBtnClick: function (fn, btnName) {
            let flag = formUtil.checkEmptyValue(fn);
            if (flag) {

                //최초 한번은 이벤트 등록
                $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                    detailBtnClickEventHandler(e);
                });
                // grid 안에 상세버튼 클릭 이벤트
                const observer = new MutationObserver((mutations) => {
                    mutations.forEach((mutation) => {
                        if (mutation.addedNodes.length > 0) {
                            let $giGridList = $("#" + gridId).find(".gi-grid-list");
                            if ($giGridList.length > 0) {
                                observer.disconnect();
                            }
                            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                                detailBtnClickEventHandler(e);
                            });
                        }
                    });
                });
                const gridTarget = $("#" + gridId)[0];
                if (gridTarget) {
                    observer.observe(gridTarget, { childList: true, subtree: true });
                }
                function detailBtnClickEventHandler(e) {
                    let rowId = $(e.target).data("rowNum");
                    let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                    let dataList = {};

                    dataItems.map((i, item) => {
                        let columnName = $(item).data("field");
                        let columnValue = "";

                        $(item).children().each(function () {
                            if ($(this).is("span")) {
                                formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                            } else if ($(this).is("button")) {
                                columnName = "target";
                                columnValue = $(this).data("btn-target");
                            }
                        });

                        if (columnValue === '') columnValue = null;

                        dataList[columnName] = columnValue;
                    })
                    // console.log(dataList);
                    fn(dataList);
                }
            } else {
                formUtil.showMessage("detailBtnClick : please set function call name");
            }
        },
        //수정 버튼 설정
        updateBtnClick: function (fn, btnName) {
            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                updateBtnClickEventHandler(e);
            });
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                            updateBtnClickEventHandler(e);
                        });
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function updateBtnClickEventHandler(e) {
                let rowId = $(e.target).data("rowNum");
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};

                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });

                    if (columnValue === '') columnValue = null;

                    dataList[columnName] = columnValue;
                })

                // console.log(dataList);
                formUtil.popup("updatePopup_" + btnName, Message.Label.Array["CONFIRM.UPDATE"], fn, dataList);
                // fn(dataList);
            }
        },
        //삭제 버튼 설정
        deleteBtnClick: function (fn, btnName) {
            //최초 한번 이벤트 바인딩
            //최초 한번 이벤트 바인딩
            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                deleteBtnClickEventHandler(e);
            });
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                            deleteBtnClickEventHandler(e);
                        });
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function deleteBtnClickEventHandler(e) {
                let rowId = $(e.target).data("rowNum");
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};

                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });

                    if (columnValue === '') columnValue = null;

                    dataList[columnName] = columnValue;
                })

                // console.log(dataList);
                formUtil.popup("deletePopup_" + btnName, Message.Label.Array["CONFIRM.DELETE"], fn, dataList);
                // fn(dataList);
            }
        },
        //수정 버튼 설정
        resendBtnClick: function (fn, btnName) {
            $("#" + gridId).find("." + btnName).off("click.resendBtnClickEventHandler").on("click.resendBtnClickEventHandler", function (e) {
                resendBtnClickEventHandler(e);
            });
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        $("#" + gridId).find("." + btnName).off("click.resendBtnClickEventHandler").on("click.resendBtnClickEventHandler", function (e) {
                            resendBtnClickEventHandler(e);
                        });
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function resendBtnClickEventHandler(e) {
                let rowId = $(e.target).data("rowNum");
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};
                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });

                    if (columnValue === '') columnValue = null;

                    dataList[columnName] = columnValue;
                })
                // console.log(dataList);
                formUtil.popup("updatePopup_" + btnName, Message.Label.Array["CONFIRM.RESEND"], fn, dataList);
                // fn(dataList);
            }
        },
        mapBtnClick: function (tagId, keywordColumnName, btnName) {
            let map = new kakaoMap();
            //최초 한번 이벤트 바인딩
            //최초 한번 이벤트 바인딩
            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                mapBtnClickEventHandler(e);
            });
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                            mapBtnClickEventHandler(e);
                        });
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function mapBtnClickEventHandler(e) {
                let mapCloseBtn = '<div class="map_close-btn"></div>'
                let $tagId = $("#" + tagId);
                let targetUl = $(e.currentTarget).parent().parent();
                let targetLi = $(targetUl).children("li");
                let keyword = "";

                $("[data-side-grid-open]").map((i, item) => {
                    $(item).attr("data-side-grid-open", "false");
                    $(item).empty();
                })

                $($tagId).attr("data-side-grid-open", "true");

                targetLi.map((i, item) => {
                    if ($(item).data("field") === keywordColumnName) {
                        keyword = $(item)[0].innerText;
                    }
                });

                map.createMap(tagId, keyword);
                $tagId.append(mapCloseBtn);

                $(".map_close-btn").click(function (e) {
                    $($tagId).attr("data-side-grid-open", "false");
                    $("#" + tagId).empty();
                })
            }
        },
        sideOpenBtnClick: function (tagId, btnName, fn) {
            let $tagId = $("#" + tagId);
            let sideGridOpenCloseBtn = '<div class="side_grid_close-btn"></div>'

            // 최초 이벤트 바인딩
            bindSideOpenEvent();

            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        bindSideOpenEvent();
                    }
                });
            });

            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }

            function bindSideOpenEvent() {
                $("." + btnName).off("click.sideOpenBtnClickEventHandler").on("click.sideOpenBtnClickEventHandler", function (e) {
                    sideOpenBtnClickEventHandler(e);
                });
            }

            async function sideOpenBtnClickEventHandler(e) {
                // 다른 열려있는 사이드 패널 닫기
                $("[data-side-grid-open]").not($tagId).attr("data-side-grid-open", "false");

                // 데이터 추출
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};

                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();
                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });
                    if (columnValue === '') columnValue = null;
                    dataList[columnName] = columnValue;
                });

                // 레이아웃 조정
                $("#" + gridId).removeClass("gi-col-100").addClass("gi-flex-1");
                $tagId.addClass("gi-flex-1");
                $($tagId).attr("data-side-grid-open", "true");

                // 상시 버튼 감시 및 유지 로직 (MutationObserver)
                const buttonObserver = new MutationObserver(() => {
                    if ($tagId.attr("data-side-grid-open") === "true") {
                        if ($tagId.find(".side_grid_close-btn").length === 0) {
                            $tagId.prepend(sideGridOpenCloseBtn);
                            sideGridCloseBtnEvent();
                        }
                    }
                });
                buttonObserver.observe($tagId[0], { childList: true });

                // 1. 컨텐츠 초기화 및 콜백 실행
                $tagId.empty();
                if (typeof fn === "function") {
                    await fn(dataList);
                }

                sideGridCloseBtnEvent();
            }

            function sideGridCloseBtnEvent() {
                $tagId.find(".side_grid_close-btn").off("click.sideGridCloseBtnClickEventHandler").on("click.sideGridCloseBtnClickEventHandler", function (e) {
                    $($tagId).attr("data-side-grid-open", "false");
                    $("#" + gridId).removeClass("gi-flex-1").addClass("gi-col-100");
                    $tagId.removeClass("gi-flex-1");
                    $tagId.empty();
                });
            }
        },
        //정렬용 컬럼 클릭 이벤트
        sortDataSet: function (fn, notSortList) {
            notSortList = notSortList || [];
            // 이벤트 위임: 부모 요소에 클릭 이벤트 등록
            $("#" + gridId + ' ul.gi-grid-list-header').off('click').on('click', 'li', function (e) {
                let column = $(this).data('column');

                // 버튼 컬럼이나 제외 컬럼은 처리하지 않음
                if (column.endsWith('_btn') || column.endsWith('_checkbox_all') || notSortList.includes(column)) {
                    return;
                }

                // 정렬 상태 변경
                if (gridSortManager.sortColumn === column && gridSortManager.sortOrder === 'asc') {
                    gridSortManager.setSort(column, 'desc');
                } else if (gridSortManager.sortColumn === column && gridSortManager.sortOrder === 'desc') {
                    gridSortManager.setSort(null, null); // 정렬 해제
                } else {
                    gridSortManager.setSort(column, 'asc');
                }

                // 현재 설정된 옵션
                let pagingOption = $('#' + giGridRowSelectorId + ' option:selected').val();
                let currentPage = $('.active').data('field');

                // 정렬용 콜백 함수 실행
                fn(currentPage, pagingOption, gridSortManager.sortColumn, gridSortManager.sortOrder);
            });
        },
        rowClick: function (fn) {
            // 최초 로딩 시 이벤트를 설정
            setRowClickEvent(fn);

            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect(); // 추가된 노드가 있을 때만 observer를 종료
                        }
                        // 이벤트 처리
                        setRowClickEvent(fn);  // 추가된 그리드에도 rowClick 이벤트 설정
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }

            // rowClick 이벤트를 설정하는 함수
            function setRowClickEvent(fn) {
                let gridSelector = "#" + gridId;
                $(gridSelector).find(".gi-grid-list").addClass("gi-cursor-pointer");
                $(gridSelector).find(".gi-grid-list")
                    .mouseenter(function () {
                        $(this).addClass("gi-grid-list-hover");
                    })
                    .mouseleave(function () {
                        $(this).removeClass("gi-grid-list-hover");
                    })
                    .click(function () {
                        if ($(this).hasClass("gi-grid-list-select")) {
                            $(this).removeClass("gi-grid-list-select");
                        } else {
                            $(gridSelector).find(".gi-grid-list").removeClass("gi-grid-list-select");
                            $(this).addClass("gi-grid-list-select");
                        }
                    });


                $(gridSelector).find("ul[data-row-num]").off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                    if (!$(e.target).is("button")) {
                        rowClickEventHandler(e, fn);
                    }
                });
            }

            // rowClick 이벤트 핸들러
            // rowClick 이벤트 핸들러
            function rowClickEventHandler(e, fn) {
                let columnArray = $(e.currentTarget).children("li");
                let resultList = [];
                columnArray.map((i, item) => {
                    const columnName = $(item).data("field");
                    const columnValue = $(item).children("span").text();
                    const hasDataGridValue = $(item).children("span").data("gridValue");

                    if (formUtil.checkEmptyValue(hasDataGridValue)) {
                        resultList[columnName + "_value"] = hasDataGridValue;
                    }
                    resultList[columnName] = columnValue;
                });
                fn(resultList, e);
            }
        },
        //NOTE : doubleClick 이벤트 설정
        rowDoubleClick: function (fn) {
            // 최초 로딩 시 이벤트를 설정
            setRowDoubleClickEvent(fn);

            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect(); // 추가된 노드가 있을 때만 observer를 종료
                        }
                        // 이벤트 처리
                        setRowDoubleClickEvent(fn);  // 추가된 그리드에도 rowClick 이벤트 설정
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }

            // rowClick 이벤트를 설정하는 함수
            function setRowDoubleClickEvent(fn) {
                let gridSelector = "#" + gridId;
                $(gridSelector).find(".gi-grid-list").addClass("gi-cursor-pointer");
                $(gridSelector).find(".gi-grid-list")
                    .mouseenter(function () {
                        $(this).addClass("gi-grid-list-hover");
                        $(this).addClass("no-drag");
                    })
                    .mouseleave(function () {
                        $(this).removeClass("gi-grid-list-hover");
                    });
                $(gridSelector).find("ul[data-row-num]").off("dblclick.rowDoubleClickEventHandler").on("dblclick.rowDoubleClickEventHandler", function (e) {
                    if (!$(e.target).is("button") && e.target.type !== "checkbox") {
                        rowDoubleClickEventHandler(e, fn);
                    }
                });
            }

            // rowClick 이벤트 핸들러
            // rowClick 이벤트 핸들러
            function rowDoubleClickEventHandler(e, fn) {
                let columnArray = $(e.currentTarget).children("li");
                let resultList = [];

                columnArray.map((i, item) => {
                    const columnName = $(item).data("field");
                    const columnValue = $(item).children("span").text();
                    const hasDataGridValue = $(item).children("span").data("gridValue");

                    if (formUtil.checkEmptyValue(hasDataGridValue)) {
                        resultList[columnName + "_value"] = hasDataGridValue;
                    }
                    resultList[columnName] = columnValue;
                });
                fn(resultList);
            }
        },
        rowCheckboxClick: function (fn) {
            setRowCheckBoxClickEvent(fn);
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect(); // 추가된 노드가 있을 때만 observer를 종료
                        }
                        // 이벤트 처리
                        setRowCheckBoxClickEvent(fn);  // 추가된 그리드에도 CheckBoxClick 이벤트 설정
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }

            function setRowCheckBoxClickEvent(fn) {
                let gridSelector = "#" + gridId;
                $(gridSelector).find("input[type='checkbox']").addClass("gi-cursor-pointer");
                $(gridSelector).find("input[type='checkbox']").off("click.rowCheckBoxClickEventHandler").on("click.rowCheckBoxClickEventHandler", function (e) {
                    if (!$(e.target).is("button")) {
                        rowCheckBoxClickEventHandler(e, fn);
                    }
                });
            }
            function rowCheckBoxClickEventHandler(e, fn) {
                let $checkBoxArray = $("#" + gridId + " input[type='checkbox']");
                let resultList = [];
                let isCheckBoxAll = e.currentTarget.id.includes("_checkbox_all");
                let isChecked = $(e.currentTarget).is(":checked");

                // 전체 선택/해제 로직 최적화
                if (isCheckBoxAll) {
                    $checkBoxArray.prop("checked", isChecked);
                }

                // 체크된 항목 정보 수집
                $checkBoxArray.each((i, item) => {
                    if ($(item).is(":checked") && !item.id.includes("_checkbox_all")) {
                        let tempArray = {};
                        $(item).closest(".gi-grid-list").children("li").each((_, liItem) => {
                            const columnName = $(liItem).data("field");
                            const columnValue = $(liItem).children("span").text();
                            tempArray[columnName] = columnValue;
                        });
                        resultList.push(tempArray);
                    }
                });

                // "_checkbox_all" 체크 상태 동기화
                $checkBoxArray.each((_, item) => {
                    if (item.id.includes("_checkbox_all")) {
                        $(item).prop("checked", resultList.length === $checkBoxArray.length - 1);
                    }
                });

                fn(resultList);
            }
        },
        rowMultiSelectClick: function (fn) {
            setMultiRowClickEvent(fn);
            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect(); // 추가된 노드가 있을 때만 observer를 종료
                        }
                        setMultiRowClickEvent(fn)// 이벤트 처리
                    }
                });
            });

            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function setMultiRowClickEvent(fn) {
                $("#" + gridId).find(".gi-grid-list").addClass("gi-cursor-pointer");
                $("#" + gridId).find(".gi-grid-list").mouseenter(function () {
                    $(this).addClass("gi-grid-list-hover");
                }).mouseleave(function () {
                    $(this).removeClass("gi-grid-list-hover");
                });

                // 클릭 시 이벤트 설정
                $("#" + gridId).find("ul[data-row-num]").off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                    if (!$(e.target).is("button")) {
                        rowMultiClickEventHandler(e, fn);
                    }
                });
                function rowMultiClickEventHandler(e, fn) {
                    let target = e.currentTarget;
                    let isSelected = target.classList.contains("gi-grid-list-multi_select");
                    let resultList = [];

                    if (isSelected) {
                        $(target).removeClass("gi-grid-list-multi_select");
                    } else {
                        $(target).addClass("gi-grid-list-multi_select");
                    }

                    let isSelectedVolume = $("#" + gridId + " .gi-grid-list-multi_select");
                    isSelectedVolume.each((i, item) => {
                        let columnArray = $(item).children("li");
                        let tempList = [];
                        columnArray.map((i, item) => {
                            const columnName = $(item).data("field");
                            const columnValue = $(item).children("span").text();
                            tempList[columnName] = columnValue;
                        });
                        resultList.push(tempList);
                    })
                    fn(resultList);
                }
            }
        },
        gridColumResize: function (gridId) {
            formUtil.gridResize(gridId);
        },
        excelDownload: function (fileName) {
            if (typeof XLSX === 'undefined') {
                formUtil.toast("Excel library (SheetJS) is not loaded.", "error");
                return;
            }
            if (!gridData || gridData.length === 0) {
                formUtil.toast("No data to download.", "error");
                return;
            }

            // 헤더 정보 구성 (HIDDEN 제외, 버튼/체크박스 제외)
            let headers = headerItem.filter(item => {
                let isHidden = item.HIDDEN === true;
                return !isHidden && item.TYPE !== 'button' && item.TYPE !== 'checkbox';
            });

            let headerNames = headers.map(item => item.HEADER);

            // 데이터 변환
            let excelData = gridData.map(row => {
                let newRow = {};
                headers.forEach(header => {
                    newRow[header.HEADER] = row[header.ID];
                });
                return newRow;
            });

            // 워크시트 생성
            const worksheet = XLSX.utils.json_to_sheet(excelData, { header: headerNames });
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Data");

            // 파일 다운로드
            let name = fileName || (title || "grid_data");
            XLSX.writeFile(workbook, name + ".xlsx");
        },
        excelDownloadEvent: function (fileName) {
            let $btn = $("#excel-download-btn_" + gridId);
            $btn.css("display", "flex");
            $btn.off("click").on("click", function () {
                this.excelDownload(fileName);
            }.bind(this));
        },
        excelUpload: function (files, url) {
            if (!files || files.length === 0) return;

            let formData = new FormData();
            formData.append("file", files[0].file);
            axios.post(url, formData, {
                headers: {
                    "Content-Type": "multipart/form-data"
                }, withCredentials: true
            }).then(response => {

                if (response.status === 200) {
                    formUtil.toast(Message.Label.Array["COMPLETE.INSERT"], "success");
                }
            }).catch(error => {
                formUtil.toast(Message.Label.Array["FAIL.UPLOAD"], "error");
            });
        },
        excelUploadEvent: function (url) {
            let $btn = $("#excel-upload-btn_" + gridId);
            $btn.css("display", "flex");

            $btn.off("click").on("click", async function () {

                try {
                    let fileInstance = new file();

                    const files = await fileInstance.customCreateFileUpload({
                        multiple: false,
                        accept: '.xlsx,.xls'
                    });
                    this.excelUpload(files, url);
                } catch (error) {
                    if (error.message !== "User cancelled file upload") {
                        formUtil.toast("파일 선택 중 오류가 발생했습니다.", "error");
                    } else {
                        formUtil.toast("User cancelled file selection.", "warning");
                    }
                }
            }.bind(this));
        }

    }
}

//NOTE: 불필요한 SYS_CODE 조회 차단 로직
async function checkSameCode(sysCodeGroupIdArray, sysDeptGroupCodeId, cont) {
    let param = { grp_id: sysDeptGroupCodeId };
    let sysCodeArray = [];
    //NOTE: 공통코드 Array에 그리드 SYS_CD_GRP_ID 값과 일치하는 값이 있는지 여부
    let isExist = sysCodeGroupIdArray.some(item => {
        return Object.keys(item)[0] === sysDeptGroupCodeId;
    });

    //NOTE : 그리드 DataSet 호출 시 그리드 내의 SYS_CD_GRP_ID로 SYS_CODE 조회 후 sysCodeGroupIdArray 배열에 추가
    if (!isExist) {
        let sysCodeList = await findSysCode(param);
        sysCodeList.map(item => {
            sysCodeArray.push({ [item.cd_id]: item.cd_nm });
        })
        sysCodeGroupIdArray.push({ [sysDeptGroupCodeId]: sysCodeArray });
    }
    //NOTE: sysCodeGroupIdArray 배열의 SYS_CODE 키:값 으로 데이터 바인딩
    for (let key in cont) {
        let returnVALUE = "";
        let lowerKey = sysDeptGroupCodeId.toLowerCase();
        //NOTE : 그리드 row의 키가 SYS_CD_GRP_ID와 일치 하는지 여부 파악
        if (key.toLowerCase() === lowerKey) {
            //NOTE: sysCodeGroupIdArray 배열안의 키:값과 일치 하면 CD_NM 리턴
            sysCodeGroupIdArray.find(item => {
                if (formUtil.checkEmptyValue(item[sysDeptGroupCodeId])) {
                    item[sysDeptGroupCodeId].find(valueItem => {
                        if (Object.keys(valueItem)[0] === cont[key]) {
                            returnVALUE = valueItem[cont[key]];
                        }
                    })
                }
            })
            return returnVALUE;
        }
    }
    // console.log("푸쉬 푸쉬::", JSON.parse(JSON.stringify(sysCodeGroupIdArray)));
}
FormUtility.prototype.giGridHierarchy = function (layout, paging, page, gridId) {
    let gridSortManager = formUtil.gridSortManager;
    //localStorage에서 정렬값을 가져와 setting
    gridSortManager.loadSortState();
    if (!formUtil.checkEmptyValue(paging)) paging = 1;
    if (!formUtil.checkEmptyValue(page)) page = 1;

    let title = layout.title;
    let grid_list_header = "";
    let headerItem = [];
    let prePageAnimationCont = $("#gi-grid-list-body").data("pageNumber");
    let currentPageAnimationCont = page
    let pagingAnimationClass = "";
    let application_level_hierarchyOptionColumn = "";
    let application_parent_hierarchyOptionColumn = "";
    let application_sub_hierarchyOptionColumn = "";

    function expandAllHierarchyRows() {
        if (!formUtil.checkEmptyValue(application_level_hierarchyOptionColumn)) {
            return;
        }
        let $body = $("#" + gridId + " #gi-grid-list-body");
        $body.find("ul.gi-grid-list").removeClass("gi-hidden");
        $body.find(".gi-tree-toggle")
            .removeClass("collapsed fa-caret-right")
            .addClass("expanded fa-caret-down");
    }

    function collapseAllHierarchyRows() {
        if (!formUtil.checkEmptyValue(application_level_hierarchyOptionColumn)) {
            return;
        }
        let $body = $("#" + gridId + " #gi-grid-list-body");
        $body.find("ul.gi-grid-list").each(function () {
            let $row = $(this);
            let level = $row.find(`li[data-field="${application_level_hierarchyOptionColumn}"] span`)
                .first().text().trim();
            if (level !== "0") {
                $row.addClass("gi-hidden");
            }
        });
        $body.find(".gi-tree-toggle")
            .removeClass("expanded fa-caret-down")
            .addClass("collapsed fa-caret-right");
    }

    function bindHierarchyTreeControls() {
        let $grid = $("#" + gridId);
        $grid.find(".gi-hierarchy-tree-controls").removeClass("gi-hidden");
        $grid.find(".gi-hierarchy-expand-all-btn").off("click.hierarchyTree").on("click.hierarchyTree", function (e) {
            e.preventDefault();
            e.stopPropagation();
            expandAllHierarchyRows();
        });
        $grid.find(".gi-hierarchy-collapse-all-btn").off("click.hierarchyTree").on("click.hierarchyTree", function (e) {
            e.preventDefault();
            e.stopPropagation();
            collapseAllHierarchyRows();
        });
    }

    if (!formUtil.checkEmptyValue(gridId)) gridId = "gi-Grid";
    let gridData = [];
    let rowWarningFn = null;

    if (formUtil.checkEmptyValue(prePageAnimationCont)) {
        //애니메이션 효과 적용
        if (prePageAnimationCont > currentPageAnimationCont) {
            pagingAnimationClass = "tilt-in-left-1";
        } else if (prePageAnimationCont < currentPageAnimationCont) {
            pagingAnimationClass = "tilt-in-right-1";
        } else if (prePageAnimationCont === currentPageAnimationCont) {
            pagingAnimationClass = "fade-in";
        }
    }


    layout.list.map((item) => {
        let hidden = "";
        let sort = "";
        //그리드 데이터 각 row 생성하기 위해 데이터 담기
        headerItem.push({
            ID: item.ID,
            WIDTH: item.WIDTH,
            TEXT_ALIGN: item.TEXT_ALIGN,
            FONT_SIZE: item.FONT_SIZE,
            TYPE: item.TYPE,
            HEADER: item.HEADER,
            SYS_CD_GRP_ID: item.SYS_CD_GRP_ID,
            TARGET: item.TARGET,
            HIDDEN: item.HIDDEN,
            VISIBLE_OPTION_BTN: item.VISIBLE_OPTION_BTN
        });
        // //정렬 대상이라면 정렬순서 추가
        // if (gridSortManager.sortColumn !== null && gridSortManager.sortColumn !== undefined && gridSortManager.sortColumn.trim() !== '') {
        //     if (gridSortManager.sortColumn === item.ID) {
        //         sort = 'gi-grid-sort-'+gridSortManager.sortColumn;
        //     }
        // }

        //컬럼 히든처리
        if (formUtil.checkEmptyValue(item.HIDDEN) && item.HIDDEN) {
            hidden = "gi-hidden ";
        } else {
            hidden = "gi-show-li ";
        }
        let sortArray = gridSortManager.getSort();
        if (sortArray.order !== null) {
            if (item.ID === sortArray.column) {
                sort = 'gi-grid-sort-' + sortArray.order;
            }
        }
        // grid_list_header += '<li class="gi-row-' + item.WIDTH + ' gi-flex gi-flex-center gi-overflow-scroll gi-col-30px '+hidden+'">' +
        grid_list_header +=
            '<li data-column="' + item.ID + '" class="resizableBox gi-min-row-50px gi-row-' + item.WIDTH + ' gi-overflow-scroll gi-col-30px ' + hidden + '' + sort + '">' +
            '<span class="gi-width-100 gi-padding-left-right-10px gi-flex gi-flex-justify-content-center">' + item.HEADER + '</span>' +
            '<div class="gridColumResizer"></div>' +
            '</li>';
    })
    let totalPageCount = Math.ceil(paging);
    let maxPagesToShow = 10;

    let startPage = Math.floor((page - 1) / maxPagesToShow) * maxPagesToShow + 1;
    let endPage = Math.min(totalPageCount, startPage + maxPagesToShow - 1);

    let pagingArea = '';
    let giGridPagingBtn = gridId + "_gi-grid-paging-btn";
    if (page > 1) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn" data-field="1"><i class="fa-solid fa-angles-left"></i></span>';
    }
    if (startPage > 1) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn gi-grid-paging-prev-btn" data-field="' + (startPage - maxPagesToShow) + '"><i class="fa-solid fa-chevron-left"></i></span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn" data-field="' + i + '">' + i + '</span>';
    }

    if (endPage < totalPageCount) {
        pagingArea += '<span class="' + giGridPagingBtn + ' gi-grid-paging-btn gi-grid-paging-next-btn" data-field="' + totalPageCount + '"><i class="fa-solid fa-chevron-right"></i></span>';
    }

    //페이징 row 개수 설정
    let options = "";
    let giGridRowSelectorId = "gi-grid-row-selector_" + gridId;
    for (let i = 1; i < 11; i++) {
        let selectedOption = "";
        if (parseInt($("#" + giGridRowSelectorId + " option:selected").val()) === 10 * i) {
            selectedOption = "selected";
        }
        options += '<option value="' + 10 * i + '" ' + selectedOption + '>' + 10 * i + ' row</option>>'
    }

    let grid =
        '            <figure class="gi-figure-content gi-overflow-scroll gi-col-100 gi-row-100 gi-flex gi-flex-justify-content-center gi-flex gi-flex-direction-column">' +
        '                <div class="gi-article-content gi-min-col-90 gi-col-100 gi-row-100">' +
        '                    <div class="gi-row-100 gi-flex gi-flex-justify-content-space-between gi-margin-bottom-1 ">' +
        '                        <div class="gi-flex gi-gap-3px gi-flex-align-items-center">' +
        '                             <div class="gi-hierarchy-tree-controls gi-hidden gi-flex gi-gap-3px">' +
        '                               <button type="button" class="gi-hierarchy-tree-btn gi-hierarchy-expand-all-btn" title="전체 펼치기">' +
        '                                 <i class="fa-solid fa-folder-open"></i>' +
        '                               </button>' +
        '                               <button type="button" class="gi-hierarchy-tree-btn gi-hierarchy-collapse-all-btn" title="전체 접기">' +
        '                                 <i class="fa-solid fa-folder"></i>' +
        '                               </button>' +
        '                             </div>' +
        '                             <select class="gi-grid-row-selector gi-row-65px" id="' + giGridRowSelectorId + '">' +
        options +
        '                             </select>' +
        '                        </div>' +
        '                       <div class="gi-flex gi-flex-justify-content-end gi-gap-3px">' +
        '                             <button id="excel-upload-btn_' + gridId + '" class="gi-excel-upload-btn" type="button">' +
        '                               <i class="fa-solid fa-file-excel"></i>' +
        '                               <span>excel upload</span>' +
        '                             </button>' +
        '                             <button id="excel-download-btn_' + gridId + '" class="gi-excel-download-btn" type="button">' +
        '                               <i class="fa-solid fa-file-excel"></i>' +
        '                               <span>excel download</span>' +
        '                             </button>' +
        '                       </div>' +
        '                    </div>' +
        '                    <div id="gi-grid-list-body" data-page-number="' + page + '" class="gi-row-100 gi-overflow-scroll gi-flex gi-flex-direction-column gi-margin-top-10px">' +
        '                        <ul class="gi-grid-list-header gi-row-100 gi-col-30px gi-ul gi-flex">' +
        grid_list_header +
        '                        </ul>' +
        '                    </div>' +
        '                </div>' +
        '                <div class="gi-grid-paging-content gi-col-5 gi-row-100">' +
        // pagingArea +
        '                </div>' +
        '            </figure>';


    $("#" + gridId).html(grid);

    const items = document.querySelectorAll('.gi-grid-list-header > .gi-show-li');
    items.forEach((item, index) => {
        if (index !== items.length - 1) {
            item.style.borderRight = '1px solid #bbbbbb6e';
        }
    });

    // 초기 활성화 페이징 번호 설정
    $(`.${giGridPagingBtn}[data-field="${page}"]`).addClass("active");

    //그리드 생성 후 데이터 바인딩
    return {
        //계층구조 기준 컬럼 설정
        DataSet: async function (data) {
            gridData = data;
            let flag = formUtil.checkEmptyValue(data);
            let isHierarchy = formUtil.checkEmptyValue(application_level_hierarchyOptionColumn)
                && formUtil.checkEmptyValue(application_parent_hierarchyOptionColumn)
                && formUtil.checkEmptyValue(application_sub_hierarchyOptionColumn);
            let grid_list = "";
            let sysCodeGroupIdArray = [];

            // RowWarning 설정이 있는 경우 실행
            if (rowWarningFn && data) {
                data.forEach(item => {
                    let msg = rowWarningFn(item);
                    if (msg) {
                        item.ROW_WARN_MSG = msg;
                    }
                });
            }

            //NOTE: rows BTN 노출 이벤트 로직 설정
            let visibleOptionArray = [];
            let originalDataForVisibleOption = [];
            function constSetVisibleOption() {
                headerItem.map(item => {
                    if (formUtil.checkEmptyValue(item.VISIBLE_OPTION_BTN)) {
                        item.VISIBLE_OPTION_BTN["BTN_ID"] = item.ID;
                        visibleOptionArray.push(item.VISIBLE_OPTION_BTN);
                    }
                })
            }

            if (flag) {
                //NOTE: rows BTN 노출 이벤트 함수 호출
                constSetVisibleOption();
                for (let i = 0; i < data.length; i++) {
                    let warnClass = "";
                    let warnMsgAttr = "";

                    if (formUtil.checkEmptyValue(data[i].ROW_WARN_MSG)) {
                        warnClass = "gi-grid-list-warn";
                        warnMsgAttr = ' data-warn-msg="' + data[i].ROW_WARN_MSG + '"';
                    }

                    grid_list += '<ul class="gi-grid-list gi-row-100 gi-ul gi-flex ' + pagingAnimationClass + ' ' + warnClass + '" ' + warnMsgAttr + ' data-row-num="' + i + '">';
                    originalDataForVisibleOption = [];
                    for (let j = 0; j < headerItem.length; j++) {
                        let item = headerItem[j];
                        let tag = "";
                        let sysCodeName = "";
                        let sysCodeValue = "";
                        let hidden = true;

                        if (formUtil.checkEmptyValue(item.SYS_CD_GRP_ID)) {
                            sysCodeName = await checkSameCode(sysCodeGroupIdArray, item.SYS_CD_GRP_ID, data[i]);
                            sysCodeValue = data[i][item.ID];
                        } else {
                            sysCodeName = data[i][item.ID];
                        }
                        if (formUtil.checkEmptyValue(item.HIDDEN)) {
                            if (item.HIDDEN) {
                                hidden = "hidden";
                            } else {
                                hidden = "";
                            }
                        }

                        //NOTE: rows BTN 노출 이벤트 함수 호출
                        visibleOptionArray.map(visibleOptionKeys => {
                            //NOTE: VISIBLE_OPTION_BTN:[{"menu_lvl":"0"},{"menu_lvl":"1"}] 조건이 배열일때 적용 합수
                            if (formUtil.checkEmptyValue(visibleOptionKeys.length)) {
                                visibleOptionKeys.map(ArrItem => {
                                    if (Object.keys(ArrItem)[0] === item.ID) {
                                        if (ArrItem[item.ID] === sysCodeName + "") {
                                            originalDataForVisibleOption[visibleOptionKeys.BTN_ID] = "true";
                                        }
                                    }
                                })
                            } else {
                                //NOTE: VISIBLE_OPTION_BTN:{"menu_lvl":"0"} 조건이 하나 일때
                                if (Object.keys(visibleOptionKeys)[0] === item.ID) {
                                    if (visibleOptionKeys[item.ID] === sysCodeName + "") {
                                        originalDataForVisibleOption[visibleOptionKeys.BTN_ID] = "true";
                                    }
                                }
                            }
                        })
                        if (!formUtil.checkEmptyValue(sysCodeName)) sysCodeName = "";
                        var hasVisibleOption = headerItem.some(h => h.ID === item.ID && formUtil.checkEmptyValue(h.VISIBLE_OPTION_BTN));
                        switch (item.TYPE) {
                            case "text":
                                sysCodeValue
                                    ?
                                    tag = '<span class="resizer gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" data-grid-value="' + sysCodeValue + '">' + sysCodeName + '</span>'
                                    :
                                    tag = '<span class="resizer gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '">' + sysCodeName + '</span>';
                                break;
                            // case "radio":
                            //     tag = '<input type="radio" class="gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" data-field="'+data[i][item.ID]+'"/>';
                            //     break;
                            case "button":
                                // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                if (hasVisibleOption) {
                                    // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                    if (originalDataForVisibleOption[item.ID] !== "true") {
                                        tag = '<button type="button" id="' + item.ID + "_" + i + '" class="gi-grid-btn gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + item.HEADER + '</button>';
                                    }
                                } else {
                                    tag = '<button type="button" id="' + item.ID + "_" + i + '" class="gi-grid-btn gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + item.HEADER + '</button>';
                                }
                                break;
                            case "toggle":
                                if (!hasVisibleOption || originalDataForVisibleOption[item.ID] !== "true") {
                                    const rawValue = data[i][item.ID];
                                    const isOn = rawValue === "1" || rawValue === 1;
                                    const checkedAttr = isOn ? " checked" : "";
                                    tag = '<label class="gi-toggle-switch" data-focus-label="false" style="height:auto !important; width:40px !important;">'
                                        + '<input type="checkbox" class="gi-grid-toggle" id="' + item.ID + '_' + i + '"'
                                        + ' data-field="' + item.ID + '" data-grid-value="' + rawValue + '" data-row-num="' + i + '"' + checkedAttr + '>'
                                        + '<span class="gi-toggle-slider-grid-cell"></span></label>';
                                }
                                
                                break;
                            // case "checkbox":
                            //     tag = '<input type="checkbox" class="gi-row-100 gi-padding-left-right-10px gi-font-size-' + item.FONT_SIZE + '" value="' + data[i][item.ID] + '" />';
                            //     break;
                            case "img":
                                switch (item.ID) {
                                    case "detail_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/detail.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/detail.png"></button>';
                                        };
                                        break;
                                    case "update_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/pen.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/pen.png"></button>';
                                        };
                                        break;
                                    case "delete_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/trash.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/trash.png"></button>';
                                        };
                                        break;
                                    case "select_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/select.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/select.png"></button>';
                                        };
                                        break;
                                    case "redirect_btn":
                                        // VISIBLE_OPTION_BTN 조건이 있는 경우 체크
                                        if (hasVisibleOption) {
                                            // 조건이 맞지 않을 때만 버튼 표시 (기존 로직)
                                            if (originalDataForVisibleOption[item.ID] !== "true") {
                                                tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/redirect.png"></button>';
                                            }
                                        } else {
                                            tag = '<button type="button" id="' + item.ID + "_" + i + '" class="grid-button-img gi-row-50 gi-font-size-' + item.FONT_SIZE + ' ' + item.ID + '" data-row-num="' + i + '" data-btn-target="' + item.TARGET + '">' + '<img class="grid-img" src="../common/img/redirect.png"></button>';
                                        };
                                        break;


                                }
                        }
                        grid_list += '<li class="resizableBox gi-min-row-50px gi-row-' + item.WIDTH + ' gi-col-16px gi-flex gi-overflow-scroll gi-flex-justify-content-' + item.TEXT_ALIGN + ' gi-text-align-' + item.TEXT_ALIGN + ' ' + hidden + '" data-grid-row="' + j + '" data-field="' + item.ID + '">' + tag + '</li>';
                    }
                    grid_list += '</ul>';
                }
            } else {
                grid_list = '<div class="gi-row-100 gi-col-100 gi-flex gi-flex-align-items-center gi-flex-justify-content-center bounce-in-top"><div class="grid-no-data"><img src="../common/img/doc.png"><span class="gi-text-align-center">No Data</span></div></div>';
                $("#" + gridId + " .gi-grid-paging-content").html('');
            }

            $("#" + gridId + " .gi-grid-list-header").after(grid_list);

            if (isHierarchy) {
                // 1. 필요한 데이터 추출 rows에 보관
                let rows = [];
                $("#" + gridId + " .gi-grid-list").each(function () {
                    let $row = $(this);
                    // 각 행 내부에서 필요한 값을 추출 (trim()으로 공백 제거)
                    let level = $row.find(`li[data-field="${application_level_hierarchyOptionColumn}"] span`)
                        .first().text().trim();
                    let parentVal = $row.find(`li[data-field="${application_parent_hierarchyOptionColumn}"] span`)
                        .first().text().trim();
                    let subVal = $row.find(`li[data-field="${application_sub_hierarchyOptionColumn}"] span`)
                        .first().text().trim();
                    rows.push({ $row, level, parentVal, subVal });
                });

                // [추가] 자식 여부 확인
                rows.forEach(r => {
                    r.hasChild = rows.some(child => child.subVal === r.parentVal);
                });

                // 2. 레벨별 그룹화
                let depth0 = rows.filter(r => r.level === "0");
                let depth1 = rows.filter(r => r.level === "1");
                let depth2 = rows.filter(r => r.level === "2");

                // HIDDEN이 아닌 첫번째 li에 계층 클래스 추가
                rows.forEach((r, i) => {
                    //NOTE: row의 li 요소중 hidden과 마지막 li 요소를 제외한 li에 border dotted 추가
                    r.$row.find("li").not('.hidden').not(":last").addClass("gi-grid-li-border-dotted");

                    let $firstLi = r.$row.find("li").not('.hidden').first();

                    // [추가] 토글 아이콘 추가 (자식이 있는 경우에만)
                    if (r.hasChild) {
                        if ($firstLi.find('.gi-tree-toggle').length === 0) {
                            $firstLi.prepend('<i class="fa-solid fa-caret-down gi-tree-toggle expanded"></i>');
                        }
                    }

                    if (r.level === "0") {
                        $firstLi.addClass("gi-grid-hierarchy-depth0");
                        //NOTE: 첫번째 row를 제외한 row에 border-top-dotted-gray 추가
                        if (r !== depth0[0]) {
                            r.$row.addClass("border-top-dotted-gray");
                        }
                        unUsedMenuUISettings(r);

                    } else if (r.level === "1") {
                        $firstLi.addClass("gi-grid-hierarchy-depth1");
                        unUsedMenuUISettings(r);
                    } else if (r.level === "2") {
                        $firstLi.addClass("gi-grid-hierarchy-depth2");
                        unUsedMenuUISettings(r);
                    }

                    //NOTE: 사용여부에 따른 ui 변경
                    function unUsedMenuUISettings(e) {
                        //NOTE: 미사용시 메뉴 비활성화
                        let $useYnCell = e.$row.find("li[data-field='use_yn']").not(".hidden");
                        let $valueEl = $useYnCell.find("[data-grid-value]").first();
                        let flag = $valueEl.length === 0;
                        let a = "";
                        let b = "";
                        let c = "";

                        if (flag) {
                            a = $useYnCell.find("span").text();
                            b = "0";
                            c = "1";
                        } else {
                            a = $valueEl.data("gridValue");
                            b = 0;
                            c = 1;
                        }

                        if (a === b) {
                            $(e.$row).addClass("unused-menu");
                            let parentValue = $(e)[0].parentVal;
                            let dept2CodeName = "";
                            rows.forEach(item => {
                                if (item.subVal === parentValue) {
                                    $(item.$row).addClass("unused-menu")
                                    dept2CodeName = $(item.$row).find("li[data-field='menu_cd']").find("span").text();
                                }
                                if (item.subVal === dept2CodeName) {
                                    $(item.$row).addClass("unused-menu")
                                }
                            });
                        } else {
                            //NOTE: 사용중인 최상위 메뉴의 하위메뉴 비활성화
                            if (a === b && a === c) {
                                $(e.$row).removeClass("unused-menu");
                            }
                        }
                    }
                });

                // 3. 데이터 배치
                let finalOrder = [];
                depth0.forEach((r0, idx0) => {
                    finalOrder.push(r0);
                    // depth1에서 부모로 찾기
                    let matchingDepth1 = depth1.filter(r1 => r1.subVal === r0.parentVal);
                    matchingDepth1.forEach((r1, idx1) => {
                        let isParentLast = (idx1 === matchingDepth1.length - 1);
                        if (isParentLast) {
                            r1.$row.find(".gi-grid-hierarchy-depth1").addClass("gi-grid-hierarchy-last");
                        }
                        finalOrder.push(r1);
                        // depth2에서 부모로 찾기
                        let matchingDepth2 = depth2.filter(r2 => r2.subVal === r1.parentVal);
                        matchingDepth2.forEach((r2, idx2) => {
                            if (isParentLast) {
                                r2.$row.find(".gi-grid-hierarchy-depth2").addClass("gi-grid-hierarchy-parent-last");
                            }
                            if (idx2 === matchingDepth2.length - 1) {
                                r2.$row.find(".gi-grid-hierarchy-depth2").addClass("gi-grid-hierarchy-last");
                            }
                            finalOrder.push(r2);
                        });
                    });
                });

                // 4. 삽입
                let $body = $("#" + gridId + " #gi-grid-list-body");

                $body.find("ul.gi-grid-list").detach(); //remove대신 사용

                finalOrder.forEach(r => {
                    $body.append(r.$row);
                });

                // [추가] 토글 이벤트 핸들러
                $body.off("click", ".gi-tree-toggle").on("click", ".gi-tree-toggle", function (e) {
                    e.stopPropagation();
                    e.preventDefault();

                    let $btn = $(this);
                    let $row = $btn.closest("ul.gi-grid-list");
                    let isExpanded = $btn.hasClass("expanded");

                    // 현재 Row의 PK (parentVal)
                    let myPK = $row.find(`li[data-field="${application_parent_hierarchyOptionColumn}"] span`).first().text().trim();

                    if (isExpanded) {
                        $btn.removeClass("expanded fa-caret-down").addClass("collapsed fa-caret-right");
                        toggleChildren(myPK, false);
                    } else {
                        $btn.removeClass("collapsed fa-caret-right").addClass("expanded fa-caret-down");
                        toggleChildren(myPK, true);
                    }
                });

                function toggleChildren(parentPK, show) {
                    $body.find("ul.gi-grid-list").each(function () {
                        let $childRow = $(this);
                        // Child의 FK (subVal)
                        let childFK = $childRow.find(`li[data-field="${application_sub_hierarchyOptionColumn}"] span`).first().text().trim();

                        if (childFK === parentPK) {
                            if (show) {
                                $childRow.removeClass("gi-hidden");
                                // 자식이 펼쳐져 있다면 그 후손도 보여줌
                                let $childBtn = $childRow.find(".gi-tree-toggle");
                                if ($childBtn.hasClass("expanded")) {
                                    let childPK = $childRow.find(`li[data-field="${application_parent_hierarchyOptionColumn}"] span`).first().text().trim();
                                    toggleChildren(childPK, true);
                                }
                            } else {
                                $childRow.addClass("gi-hidden");
                                // 후손도 숨김
                                let childPK = $childRow.find(`li[data-field="${application_parent_hierarchyOptionColumn}"] span`).first().text().trim();
                                toggleChildren(childPK, false);
                            }
                        }
                    });
                }
            }
        },
        //그리드 데이터 설정
        HierarchyOption: function (hierarchyOptionItem) {
            application_level_hierarchyOptionColumn = hierarchyOptionItem.level_column;
            application_parent_hierarchyOptionColumn = hierarchyOptionItem.parent_depth_column;
            application_sub_hierarchyOptionColumn = hierarchyOptionItem.child_depth_column;
            bindHierarchyTreeControls();
        },
        expandAllHierarchy: function () {
            expandAllHierarchyRows();
        },
        collapseAllHierarchy: function () {
            collapseAllHierarchyRows();
        },
        //그리드 row 개수 변경 및 페이징 버튼 이벤트 설정
        pagingSet: function (fn) {
            let range = "";
            $("#" + giGridRowSelectorId).change(function () {
                range = parseInt($("#" + giGridRowSelectorId + " option:selected").val());
                $("#" + giGridRowSelectorId).val(range);
                fn(1, range);
            })
            $("." + giGridPagingBtn).click(function () {
                // 기존에 활성화된 페이징 넘버에서 active 클래스를 제거
                $("." + giGridPagingBtn).removeClass("active");

                // 현재 클릭된 페이징 넘버에 active 클래스 추가
                $(this).addClass("active");

                let pagingNum = $(this).data("field");
                range = parseInt($("#" + giGridRowSelectorId + " option:selected").val());
                fn(pagingNum, range);
            })
        },
        RowWarning: function (arg1, arg2, arg3) {
            if (typeof arg1 === 'function') {
                rowWarningFn = arg1;
            } else if (arg1 && arg3 !== undefined) {
                rowWarningFn = function (item) {
                    let val = item[arg1];
                    let isMatch = false;
                    if (typeof arg2 === 'function') {
                        isMatch = arg2(val);
                    } else {
                        isMatch = (val == arg2);
                    }
                    return isMatch ? arg3 : null;
                };
            }
            return this;
        },
        //그리드 내부의 상세 버튼 클릭 이벤트 설정(버튼클릭시 호출될 함수, 그리드 헤더 부분에 설정한 버튼 ID)
        detailBtnClick: function (fn, btnName) {
            let flag = formUtil.checkEmptyValue(fn);
            if (flag) {

                //최초 한번은 이벤트 등록
                $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                    detailBtnClickEventHandler(e);
                });
                // grid 안에 상세버튼 클릭 이벤트
                const observer = new MutationObserver((mutations) => {
                    mutations.forEach((mutation) => {
                        if (mutation.addedNodes.length > 0) {
                            let $giGridList = $("#" + gridId).find(".gi-grid-list");
                            if ($giGridList.length > 0) {
                                observer.disconnect();
                            }
                            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                                detailBtnClickEventHandler(e);
                            });
                        }
                    });
                });
                const gridTarget = $("#" + gridId)[0];
                if (gridTarget) {
                    observer.observe(gridTarget, { childList: true, subtree: true });
                }
                function detailBtnClickEventHandler(e) {
                    let rowId = $(e.target).data("rowNum");
                    let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                    let dataList = {};

                    dataItems.map((i, item) => {
                        let columnName = $(item).data("field");
                        let columnValue = "";

                        $(item).children().each(function () {
                            if ($(this).is("span")) {
                                formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                            } else if ($(this).is("button")) {
                                columnName = "target";
                                columnValue = $(this).data("btn-target");
                            }
                        });

                        if (columnValue === '') columnValue = null;

                        dataList[columnName] = columnValue;
                    })
                    // console.log(dataList);
                    fn(dataList);
                }
            } else {
                formUtil.showMessage("detailBtnClick : please set function call name");
            }
        },
        //수정 버튼 설정
        updateBtnClick: function (fn, btnName) {
            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                updateBtnClickEventHandler(e);
            });
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                            updateBtnClickEventHandler(e);
                        });
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function updateBtnClickEventHandler(e) {
                let rowId = $(e.target).data("rowNum");
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};

                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });

                    if (columnValue === '') columnValue = null;

                    dataList[columnName] = columnValue;
                })

                // console.log(dataList);
                formUtil.popup("updatePopup_" + btnName, Message.Label.Array["CONFIRM.UPDATE"], fn, dataList);
                // fn(dataList);
            }
        },
        //삭제 버튼 설정
        deleteBtnClick: function (fn, btnName) {
            //최초 한번 이벤트 바인딩
            //최초 한번 이벤트 바인딩
            $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                deleteBtnClickEventHandler(e);
            });
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $("#" + gridId).find(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        $("#" + gridId).find("." + btnName).off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                            deleteBtnClickEventHandler(e);
                        });
                    }
                });
            });
            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function deleteBtnClickEventHandler(e) {
                let rowId = $(e.target).data("rowNum");
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};

                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();

                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });

                    if (columnValue === '') columnValue = null;

                    dataList[columnName] = columnValue;
                })

                // console.log(dataList);
                formUtil.popup("deletePopup_" + btnName, Message.Label.Array["CONFIRM.DELETE"], fn, dataList);
                // fn(dataList);
            }
        },
        //정렬용 컬럼 클릭 이벤트
        sortDataSet: function (fn, notSortList) {
            notSortList = notSortList || [];

            // 중복실행이 너무 많아서 수정 -> 부모 요소에 클릭 이벤트 등록
            $("#" + gridId + ' ul.gi-grid-list-header').off('click').on('click', 'li', function () {
                let column = $(this).data('column');

                // 버튼 컬럼이나 제외 컬럼은 처리하지 않음
                if (column.endsWith('_btn') || notSortList.includes(column)) {
                    return;
                }

                // 정렬 상태 변경
                if (gridSortManager.sortColumn === column && gridSortManager.sortOrder === 'asc') {
                    gridSortManager.setSort(column, 'desc');
                } else if (gridSortManager.sortColumn === column && gridSortManager.sortOrder === 'desc') {
                    gridSortManager.setSort(null, null); // 정렬 해제
                } else {
                    gridSortManager.setSort(column, 'asc');
                }

                // 현재 설정된 옵션
                let pagingOption = $('#' + giGridRowSelectorId + ' option:selected').val();
                let currentPage = $('.active').data('field');

                // 정렬용 콜백 함수 실행
                fn(currentPage, pagingOption, gridSortManager.sortColumn, gridSortManager.sortOrder);
            });
        },
        rowClick: function (fn) {
            // 최초 로딩 시 이벤트를 설정
            setRowClickEvent(fn);
            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                const hasAddedNodes = mutations.some(mutation => mutation.addedNodes.length > 0);
                if (hasAddedNodes) {
                    let $giGridList = $(".gi-grid-list");
                    if ($giGridList.length > 0) {
                        observer.disconnect(); // 필요시 사용
                    }
                    setRowClickEvent(fn);
                }
            });

            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }

            // rowClick 이벤트를 설정하는 함수
            function setRowClickEvent(fn) {
                let gridSelector = "#" + gridId;
                $(gridSelector).find(".gi-grid-list").addClass("gi-cursor-pointer");
                $(gridSelector).find(".gi-grid-list, .unused-menu")
                    .mouseenter(function () {
                        $(this).addClass("gi-grid-list-hover");
                    })
                    .mouseleave(function () {
                        $(this).removeClass("gi-grid-list-hover");
                    })
                    .click(function () {
                        if ($(this).hasClass("gi-grid-list-select")) {
                            $(this).removeClass("gi-grid-list-select");
                        } else {
                            $("#" + gridId).find(".gi-grid-list").removeClass("gi-grid-list-select");
                            $(this).addClass("gi-grid-list-select");
                        }
                    })
                    ;

                // 클릭 시 이벤트 설정
                $("#" + gridId).find("ul[data-row-num]").off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                    if (!$(e.target).is("button")) {
                        rowClickEventHandler(e, fn);
                    }
                });
            }

            // rowClick 이벤트 핸들러
            function rowClickEventHandler(e, fn) {
                let columnArray = $(e.currentTarget).children("li");
                let resultList = [];

                columnArray.map((i, item) => {
                    const columnName = $(item).data("field");
                    const columnValue = $(item).children("span").text();
                    const hasDataGridValue = $(item).children("span").data("gridValue");

                    if (formUtil.checkEmptyValue(hasDataGridValue)) {
                        resultList[columnName + "_value"] = hasDataGridValue;
                    }

                    resultList[columnName] = columnValue;
                    resultList["EVENT"] = e;
                });

                fn(resultList, e);
            }
        },
        rowMultiSelectClick: function (fn) {
            setMultiRowClickEvent(fn);
            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect(); // 추가된 노드가 있을 때만 observer를 종료
                        }
                        setMultiRowClickEvent(fn)// 이벤트 처리
                    }
                });
            });

            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }
            function setMultiRowClickEvent(fn) {
                $("#" + gridId).find(".gi-grid-list").addClass("gi-cursor-pointer");
                $("#" + gridId).find(".gi-grid-list").mouseenter(function () {
                    $(this).addClass("gi-grid-list-hover");
                }).mouseleave(function () {
                    $(this).removeClass("gi-grid-list-hover");
                });

                // 클릭 시 이벤트 설정
                $("#" + gridId).find("ul[data-row-num]").off("click.rowClickEventHandler").on("click.rowClickEventHandler", function (e) {
                    if (!$(e.target).is("button")) {
                        rowMultiClickEventHandler(e, fn);
                    }
                });
                function rowMultiClickEventHandler(e, fn) {
                    let target = e.currentTarget;
                    let isSelected = target.classList.contains("gi-grid-list-multi_select");
                    let resultList = [];

                    if (isSelected) {
                        $(target).removeClass("gi-grid-list-multi_select");
                    } else {
                        $(target).addClass("gi-grid-list-multi_select");
                    }

                    let isSelectedVolume = $("#" + gridId + " .gi-grid-list-multi_select");
                    isSelectedVolume.each((i, item) => {
                        let columnArray = $(item).children("li");
                        let tempList = [];
                        columnArray.map((i, item) => {
                            const columnName = $(item).data("field");
                            const columnValue = $(item).children("span").text();
                            tempList[columnName] = columnValue;
                        });
                        resultList.push(tempList);
                    })
                    fn(resultList);
                }
            }
        },
        sideOpenBtnClick: function (tagId, btnName, fn) {
            let $tagId = $("#" + tagId);
            let sideGridOpenCloseBtn = '<div class="side_grid_close-btn"></div>'

            // 최초 이벤트 바인딩
            bindSideOpenEvent();

            // MutationObserver로 동적 추가된 요소에 대해서도 이벤트 설정
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length > 0) {
                        let $giGridList = $(".gi-grid-list");
                        if ($giGridList.length > 0) {
                            observer.disconnect();
                        }
                        bindSideOpenEvent();
                    }
                });
            });

            const gridTarget = $("#" + gridId)[0];
            if (gridTarget) {
                observer.observe(gridTarget, { childList: true, subtree: true });
            }

            function bindSideOpenEvent() {
                $("." + btnName).off("click.sideOpenBtnClickEventHandler").on("click.sideOpenBtnClickEventHandler", function (e) {
                    sideOpenBtnClickEventHandler(e);
                });
            }

            function sideOpenBtnClickEventHandler(e) {
                // 다른 열려있는 사이드 패널 닫기
                $("[data-side-grid-open]").not($tagId).attr("data-side-grid-open", "false");

                // 데이터 추출 (detailBtnClick과 동일한 로직)
                let dataItems = $(e.currentTarget).parents(".gi-grid-list").children("li");
                let dataList = {};

                dataItems.map((i, item) => {
                    let columnName = $(item).data("field");
                    let columnValue = "";

                    $(item).children().each(function () {
                        if ($(this).is("span")) {
                            formUtil.checkEmptyValue($(this).data("gridValue")) ? columnValue = $(this).data("gridValue") + "" : columnValue = $(this).text();
                        } else if ($(this).is("button")) {
                            columnName = "target";
                            columnValue = $(this).data("btn-target");
                        }
                    });
                    if (columnValue === '') columnValue = null;
                    dataList[columnName] = columnValue;
                });

                // 사이드 패널 활성화
                $($tagId).attr("data-side-grid-open", "true");
                $tagId.empty();

                // 레이아웃 조정 (반반씩)
                $("#" + gridId).removeClass("gi-col-100").addClass("gi-flex-1");
                $tagId.addClass("gi-flex-1");

                // 콜백 함수 실행 (데이터 전달)
                if (typeof fn === "function") {
                    fn(dataList);
                }

                // 닫기 버튼 처리 (콜백 후 실행하여 사용자 오버라이드 방지)
                if ($tagId.find(".side_grid_close-btn").length === 0) {
                    $tagId.prepend(sideGridOpenCloseBtn);
                }

                sideGridCloseBtnEvent();
            }

            function sideGridCloseBtnEvent() {
                $(".side_grid_close-btn").off("click.sideGridCloseBtnClickEventHandler").on("click.sideGridCloseBtnClickEventHandler", function (e) {
                    $($tagId).attr("data-side-grid-open", "false");

                    // 레이아웃 복구 (애니메이션 대기)
                    setTimeout(() => {
                        $("#" + gridId).removeClass("gi-flex-1").addClass("gi-col-100");
                        $tagId.removeClass("gi-flex-1");
                    }, 300);
                });
            }
        },
        Hierarchy2DepthMultiSelectClick: function (fn) {
            //cursor & hover
            $(".gi-grid-list").addClass("gi-cursor-pointer");

            // clickEvent
            $("ul[data-row-num]").off("click.hierarchy2DepthMultiSelectClickEventHandler").on("click.hierarchy2DepthMultiSelectClickEventHandler", function (e) {
                let $ul = $(e.currentTarget);
                let firstLi = $ul.children("li").not('.hidden').first(); //depth 클래스가 있는 위치
                let resultList = [];

                // 'gi-grid-hierarchy-depth0'(상위)인지 확인
                if (firstLi.hasClass("gi-grid-hierarchy-depth0")) {
                    let isSelected = $ul.hasClass("gi-grid-list-root-select");

                    $ul.toggleClass("gi-grid-list-root-select");

                    // 다음 상위 요소가 나오기 전까지 모든 하위 요소 처리
                    $ul.nextAll("ul[data-row-num]").each(function () {
                        let $nextUl = $(this);
                        let $firstLi = $nextUl.children("li").first();

                        if ($firstLi.hasClass("gi-grid-hierarchy-depth0")) {
                            return false;
                        }
                        $nextUl.toggleClass("gi-grid-list-multi_select", !isSelected);
                    });
                } else { // 하위요소라면 본인만 처리
                    $ul.toggleClass("gi-grid-list-multi_select");
                }

                // 선택된 모든 요소의 데이터를 가져와서 배열로 변환
                let isSelectedVolume = $(".gi-grid-list-multi_select");
                isSelectedVolume.each((i, item) => {
                    let columnArray = $(item).children("li");
                    let tempList = {};
                    columnArray.each((i, li) => {
                        const columnName = $(li).data("field");
                        const columnValue = $(li).children("span").text();
                        tempList[columnName] = columnValue;
                    });
                    resultList.push(tempList);
                });

                fn(resultList);
            });
        },
        gridColumResize: function (gridId) {
            formUtil.gridResize(gridId);
        },
        excelDownload: function (fileName) {
            if (typeof XLSX === 'undefined') {
                formUtil.toast("Excel library (SheetJS) is not loaded.", "error");
                return;
            }
            if (!gridData || gridData.length === 0) {
                formUtil.toast("No data to download.", "error");
                return;
            }

            // 헤더 정보 구성 (HIDDEN 제외, 버튼/체크박스 제외)
            let headers = headerItem.filter(item => {
                let isHidden = item.HIDDEN === true;
                return !isHidden && item.TYPE !== 'button' && item.TYPE !== 'checkbox';
            });

            let headerNames = headers.map(item => item.HEADER);

            // 데이터 변환
            let excelData = gridData.map(row => {
                let newRow = {};
                headers.forEach(header => {
                    newRow[header.HEADER] = row[header.ID];
                });
                return newRow;
            });

            // 워크시트 생성
            const worksheet = XLSX.utils.json_to_sheet(excelData, { header: headerNames });
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Data");

            // 파일 다운로드
            let name = fileName || (title || "grid_data");
            XLSX.writeFile(workbook, name + ".xlsx");
        },
        excelDownloadEvent: function (fileName) {
            let $btn = $("#excel-download-btn_" + gridId);
            $btn.css("display", "flex");
            $btn.off("click").on("click", function () {
                this.excelDownload(fileName);
            }.bind(this));
        }
    }
}
FormUtility.prototype.gridResize = function (gridId) {
    const $grid = $("#" + gridId);

    // 리사이저 클릭 시 헤더의 정렬(Sort) 이벤트가 발생하는 것을 방지
    $grid.find(".gridColumResizer").off("click.gridResize").on("click.gridResize", function (e) {
        e.stopPropagation();
    });

    $grid.find(".gridColumResizer").off("mousedown.gridResize").on("mousedown.gridResize", function (e) {
        const $resizer = $(this);
        const $headerLi = $resizer.closest("li");
        const columnKey = $headerLi.data("column");

        if (!columnKey) return;

        const startX = e.clientX;
        const startWidth = $headerLi.outerWidth();

        // 선택된 컬럼의 모든 헤더와 바디 셀 찾기
        const $allCells = $grid.find(`li[data-column='${columnKey}'], li[data-field='${columnKey}']`);

        // 드래그 중 텍스트 선택 방지 및 커서 유지
        $("body").css({
            "user-select": "none",
            "cursor": "col-resize"
        });

        $(window).on("mousemove.gridResize", function (moveEvent) {
            const currentX = moveEvent.clientX;
            const diffX = currentX - startX;
            const newWidth = Math.max(startWidth + diffX, 30); // 최소 너비 30px

            $allCells.css({
                "width": newWidth + "px",
                "min-width": newWidth + "px",
                "max-width": newWidth + "px",
                "flex": "none"
            });
        });

        $(window).on("mouseup.gridResize", function () {
            $(window).off(".gridResize");
            $("body").css({
                "user-select": "",
                "cursor": ""
            });
        });

        e.preventDefault();
        e.stopPropagation();
    });
}
