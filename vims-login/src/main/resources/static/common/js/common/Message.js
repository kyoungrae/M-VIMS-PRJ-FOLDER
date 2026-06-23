/**
 * @title : 기본 CRUD MESSAGE가 기준이 되며 페이지별 특정 문구를 작성하여 사용합니다.
 * @see : COMPLETE : 문장 종결이 완료
 * FAIL : 행위에 대한 실패를 알릴때
 * CONFIRM : 문장의 종결 표현이 판정 의문문.
 * CHECK : 특정 값이 존재 하지 않아 요구 할때
 * INFO : 정보성 메세지
 * */
// 기본 CRUD MESSAGE
const Message = {};
Message.Label = {};
Message.Label.Array = {};


Message.Label.Array["CHECK.ID"] = "아이디를 입력해 주세요";
Message.Label.Array["CHECK.PWD"] = "비밀번호를 입력해 주세요";

Message.Label.Array["COMPLETE.INSERT"] = "등록완료";
Message.Label.Array["COMPLETE.READ"] = "조회완료";
Message.Label.Array["COMPLETE.UPDATE"] = "수정완료";
Message.Label.Array["COMPLETE.DELETE"] = "삭제완료";
Message.Label.Array["COMPLETE.PERMIT"] = "승인완료";
Message.Label.Array["COMPLETE.RETURN"] = "반납완료";
Message.Label.Array["COMPLETE.RETURN_CANCEL"] = "반납 취소 완료";
Message.Label.Array["COMPLETE.CANCEL"] = "취소완료";
Message.Label.Array["COMPLETE.SAVE"] = "저장완료";
Message.Label.Array["COMPLETE.RESEND"] = "재발송완료";

Message.Label.Array["CONFIRM.INSERT"] = "등록하시겠습니까?";
Message.Label.Array["CONFIRM.PERMIT"] = "승인하시겠습니까?";
Message.Label.Array["CONFIRM.DELETE"] = "삭제하시겠습니까?";
Message.Label.Array["CONFIRM.UPDATE"] = "수정하시겠습니까?";
Message.Label.Array["CONFIRM.RESET"] = "초기화하시겠습니까?";
Message.Label.Array["CONFIRM.CANCEL"] = "취소하시겠습니까?";
Message.Label.Array["CONFIRM.RESEND"] = "재발송하시겠습니까?";
Message.Label.Array["CONFIRM.RESET"] = "입력된 모든 내용이 초기화됩니다. 계속하시겠습니까?";
Message.Label.Array["CONFIRM.RESET_PWD"] = "비밀번호를 초기화하시겠습니까?";

Message.Label.Array["FAIL.INSERT"] = "등록실패";
Message.Label.Array["FAIL.SELECT"] = "조회실패";
Message.Label.Array["FAIL.SELECT_NO_DATA"] = "데이터가 존재 하지 않습니다";
Message.Label.Array["FAIL.UPDATE"] = "수정실패";
Message.Label.Array["FAIL.DOWNLOAD"] = "다운로드실패";
Message.Label.Array["FAIL.DELETE"] = "삭제실패";
Message.Label.Array["FAIL.PERMIT"] = "승인실패";
Message.Label.Array["FAIL.RETURN"] = "반납실패";
Message.Label.Array["FAIL.RETURN_CANCEL"] = "반납 취소 실패";
Message.Label.Array["FAIL.LOGIN"] = "로그인실패";
Message.Label.Array["FAIL.IDPWD"] = "아이디 또는 비밀번호가 일치하지 않습니다";
Message.Label.Array["FAIL.REQUIRED"] = "필수 입력값을 입력해 주세요";
Message.Label.Array["FAIL.FUNCTION.POPUP"] = "팝업 호출 메소드가 없습니다";
Message.Label.Array["FAIL.NOT.EXIST.DISABLED"] = "data-disabled 태그가 설정되어 있지 않습니다";
Message.Label.Array["FAIL.SAME.PWD"] = "새 비밀번호가 기존 비밀번호와 같습니다";
Message.Label.Array["FAIL.NOT.MATCHED.PWD"] = "기존 비밀번호가 일치 하지 않습니다";
Message.Label.Array["FAIL.COMPARE.PWD"] = "비밀번호가 일치하지 않습니다.";
Message.Label.Array["FAIL.UPLOAD"] = "업로드 실패";
Message.Label.Array["FAIL.UPLOAD.SIZE_EXCEEDED"] = "파일 업로드 용량을 초과하였습니다";
Message.Label.Array["FAIL.ACCESS_DENIED"] = "접근 권한이 없습니다.";


Message.Label.Array["CHECK.FORMTYPE"] = "형식에 맞게 입력해 주세요";
Message.Label.Array["CHECK.DATA_ATTRIBUTE"] = "태그 속성을 다시 확인해 주세요";
Message.Label.Array["CHECK.MATCH.PWD"] = "기존 비밀번호를 입력해주세요";
Message.Label.Array["CHECK.INPUT.PWD"] = "비밀번호(을)를 입력해주세요";
Message.Label.Array["CHECK.INPUT.PWD_CHECK"] = "비밀번호 확인(을)를 입력해주세요";

Message.Label.Array["BEFORE_PWD"] = "기존비밀번호";

Message.Label.Array["ALERT.NO.CHANGED.FILE"] = "변경 된 내용이 없습니다";
Message.Label.Array["CONFIRM.CLOSE_UNSAVED_CHANGES"] = "저장되지 않은 변경 사항이 있습니다. 창을 닫으시겠습니까?";

Message.Label.Array["BACK_BTN"] = "뒤로가기";
Message.Label.Array["CLOSE_BTN"] = "닫기";
Message.Label.Array["CANCEL_BTN"] = "취소";
Message.Label.Array["DELETE_BTN"] = "삭제";
Message.Label.Array["DETAIL_BTN"] = "상세";
Message.Label.Array["UPDATE_BTN"] = "수정";
Message.Label.Array["MORE_BTN"] = "더보기";
Message.Label.Array["SAVE_BTN"] = "저장";
Message.Label.Array["SELECT_BTN"] = "선택";
Message.Label.Array["SEARCH_BTN"] = "조회";
Message.Label.Array["SEARCH_KEYWORD"] = "검색어";
Message.Label.Array["REGISTER_BTN"] = "등록";
Message.Label.Array["RESET_BTN"] = "초기화";
Message.Label.Array["UPLOAD_BTN"] = "업로드";
Message.Label.Array["SIDE_BTN"] = "사이드";
Message.Label.Array["RESET_PWD_BTN"] = "비밀번호 초기화";
Message.Label.Array["REDIRECT_BTN"] = "이동";
Message.Label.Array["WRITE_BTN"] = "글쓰기";
Message.Label.Array["CONFIRM_BTN"] = "확인";


Message.Label.Array["TOTAL"] = "전체";
Message.Label.Array["CASE"] = "건";
Message.Label.Array["ALL"] = "전체";
Message.Label.Array["GALLERY"] = "갤러리";
Message.Label.Array["NOTICE"] = "공지";
Message.Label.Array["SEARCH_PLACEHOLDER"] = "검색어를 입력해 주세요";
Message.Label.Array["NO_TITLE"] = "제목 없음";
Message.Label.Array["UNKNOWN"] = "알 수 없음";
Message.Label.Array["NO"] = "No";

Message.Label.Array["START_DATE"] = "시작일";
Message.Label.Array["END_DATE"] = "종료일";


Message.Label.Array["FAIL.DELETE.REASON.EXIST_CHILD_DATA"] = "하위 데이터가 존재하기 때문에 삭제 실패 하였습니다";
Message.Label.Array["MAXIMUM.UPLOAD.SIZE.100MB"] = "파일 업로드 용량은 100 MB를 초과할 수 없습니다";
Message.Label.Array["ALERT.NO.FILE"] = "선택 된 파일이 없습니다";
Message.Label.Array["FILE_DOWN_BTN"] = "첨부파일 다운로드";
Message.Label.Array["FILE_ATTACH"] = "파일 첨부";
Message.Label.Array["FILE_ATTACH_INFO"] = "첨부된 파일 목록을 아래에서 확인할 수 있습니다.";
Message.Label.Array["FILE_ATTACH_AREA_NOT_EXIST_FILE"] = "첨부된 파일이 없습니다.";
Message.Label.Array["FILE_REGISTER_BTN"] = "파일추가";
Message.Label.Array["FILE_NM"] = "파일명";
Message.Label.Array["FILE_SZ"] = "파일크기";
Message.Label.Array["FILE_EXT"] = "확장자";
Message.Label.Array["FILE_DESCRIPTION"] = "설명";

Message.Label.Array["USE_YN"] = "사용여부";
Message.Label.Array["NOT_USED"] = "미사용";
Message.Label.Array["USED"] = "사용";

Message.Label.Array["LOGIN"] = "로그인";
Message.Label.Array["EMAIL"] = "이메일";
Message.Label.Array["PWD"] = "비밀번호";
Message.Label.Array["REMEMBER_ME"] = "이메일 저장";

// LOGIN PAGE
Message.Label.Array["LOGIN.KEEP_LOGGED_IN"] = "로그인 유지";
Message.Label.Array["LOGIN.FORGOT_PWD"] = "비밀번호 찾기";
Message.Label.Array["LOGIN.RESET_PWD_TITLE"] = "비밀번호 초기화";
Message.Label.Array["LOGIN.SEND_VERIFICATION_CODE"] = "인증번호 전송";
Message.Label.Array["LOGIN.VERIFICATION_CODE"] = "인증번호";
Message.Label.Array["LOGIN.CONFIRM_VERIFICATION_CODE"] = "인증번호 확인";
Message.Label.Array["LOGIN.NEW_PWD"] = "새 비밀번호";
Message.Label.Array["LOGIN.PWD_POLICY_GUIDE"] = "4~12자리 영문 대소문자,숫자,특수문자 조합";
Message.Label.Array["LOGIN.NEW_PWD_CONFIRM"] = "새 비밀번호 확인";
Message.Label.Array["LOGIN.PWD_CONFIRM"] = "비밀번호 확인";
Message.Label.Array["LOGIN.INITIALIZE_PWD_BTN"] = "비밀번호 초기화";
Message.Label.Array["LOGIN.APPLY_BTN"] = "적용";
Message.Label.Array["LOGIN.CLOSE_BTN"] = "닫기";
Message.Label.Array["LOGIN.MAIL_TITLE_VERIFICATION"] = "[giens] 인증코드 발송";
Message.Label.Array["LOGIN.FAIL_SEND_VERIFICATION_CODE"] = "인증번호 전송 실패";
Message.Label.Array["LOGIN.EXP_VERIFICATION_CODE"] = "인증번호가 만료되었습니다.";
Message.Label.Array["LOGIN.MAIL_CONTENT_HEADER"] = "[giens] 이메일 인증코드";
Message.Label.Array["LOGIN.MAIL_CONTENT_BODY1"] = "비밀번호 초기화를 위한 인증 코드를 발송하였습니다.";
Message.Label.Array["LOGIN.MAIL_CONTENT_BODY2"] = "아래의 인증 코드를 인증번호에 입력해주시기 바랍니다.";
Message.Label.Array["LOGIN.COMPLETE_REGISTER"] = "등록완료";
Message.Label.Array["LOGIN.INPUT_VERIFICATION_CODE"] = "인증번호를 입력해주세요.";
Message.Label.Array["LOGIN.NEW_PWD"] = "새 비밀번호";
Message.Label.Array["LOGIN.NEW_PWD_CONFIRM"] = "새 비밀번호 확인";
Message.Label.Array["LOGIN.RESET_PWD_PAGE_GUIDE"] = "비밀번호 재설정을 진행해 주세요.";
Message.Label.Array["LOGIN.RESET_PWD_INVALID_ACCESS"] = "잘못된 접근입니다. 비밀번호 찾기를 다시 진행해 주세요.";
Message.Label.Array["CHECK.INPUT.EMAIL"] = "이메일(을)를 입력해주세요";


