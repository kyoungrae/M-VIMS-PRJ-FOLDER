/**
 *  ++ giens Product ++
 */
package com.vims.common.user;

import com.system.auth.authuser.AuthUser;
import com.system.auth.service.SequenceService;
import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import com.system.common.util.passwordvalidation.PasswordPolicy;
import com.system.common.util.passwordvalidation.PasswordValidationUtil;
import com.system.common.util.validation.ValidationService;
import com.vims.common.siteconfig.SysSiteConfig;
import com.vims.common.siteconfig.SysSiteConfigService;
import com.vims.common.usergroup.SysUserGroup;
import com.vims.common.usergroup.SysUserGroupService;
import com.vims.fmsClient.ExcelDataResponse;
import com.vims.fmsClient.FmsExcelClient;

import com.system.auth.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserService extends AbstractCommonService<SysUser> {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final SysSiteConfigService sysSiteConfigService;
    private final TokenService tokenService;
    private final SequenceService sequenceService;
    private final SysUserGroupService sysUserGroupService;
    private final FmsExcelClient fmsExcelClient; // FMS 서비스 통신용 Feign Client

    @Value("${fms.internal.api-key}")
    private String fmsInternalApiKey; // 내부 API 키 (application.yml에서 주입)

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysUser> selectPage(SysUser request) throws Exception {
        try {
            return sysUserMapper.SELECT_PAGE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysUser request) throws Exception {
        try {
            return sysUserMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }
    }

    @Override
    protected List<SysUser> findImpl(SysUser request) throws Exception {
        try {
            return sysUserMapper.SELECT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.NOT.FOUND.USER"));
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    protected int removeImpl(SysUser request) {
        try {
            int tokenDeleteResult = tokenService.deleteExpiredTokens(request.getId());
            if (tokenDeleteResult < 0) {
                throw new CustomException(getMessage("EXCEPTION.REMOVE.TOKEN"));
            }

            var sysUserGroup = SysUserGroup.builder().id(request.getId()).build();
            int userGroupDeleteResult = sysUserGroupService.removeImpl(sysUserGroup);
            if (userGroupDeleteResult < 0) {
                throw new CustomException(getMessage("EXCEPTION.REMOVE.USER_GROUP"));
            }
            return sysUserMapper.DELETE(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(getMessage("EXCEPTION.REMOVE"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected int removeToken(AuthUser request) throws Exception {
        int rtn = 0;
        try {
            rtn = sysUserMapper.DELETE_TOKEN(request);
        } catch (Exception e) {
            throw new Exception(e + ": Fail to Remove Token");
        }
        return rtn;
    }

    @Override
    protected int updateImpl(SysUser request) throws Exception {
        ValidationService validationService = new ValidationService();
        boolean isPasswordProvided = validationService.checkEmptyValue(request.getPswd());
        try {
            if (isPasswordProvided) {
                // 1. 비밀번호 확인 체크
                if (!request.getPswd().equals(request.getPassword_confirm())) {
                    throw new CustomException(getMessage("EXCEPTION.PWD.CONFIRM_NOT_MATCH"));
                }

                // 2. 기존 비밀번호와 동일한지 체크 (raw 패스워드와 비교)
                SysUser existingUser = sysUserMapper.SELECT(SysUser.builder().id(request.getId()).build()).get(0);
                if (passwordEncoder.matches(request.getPswd(), existingUser.getPswd())) {
                    throw new CustomException(getMessage("EXCEPTION.PWD.SAME_AS_OLD"));
                }

                // 3. 비밀번호 정책 확인
                validationPasswordPolicy(request.getPswd());

                // 4. 비밀번호 암호화
                request.setPswd(passwordEncoder.encode(request.getPswd()));
            } else {
                // 비밀번호가 입력되지 않은 경우 기존 비밀번호를 유지
                request.setPswd(null);
            }
            return sysUserMapper.UPDATE(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e + ": Fail to Update User");
        }
    }

    protected int updatePasswordImpl(SysUser request) throws Exception {
        try {
            String pw = "1234";
            var pwParam = SysUser.builder().id(request.getId()).pswd(passwordEncoder.encode(pw)).build();
            return sysUserMapper.UPDATE(pwParam);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    protected int registerImpl(SysUser request) throws Exception {
        // 비밀번호 확인
        if (!request.getPswd().equals(request.getPassword_confirm())) {
            throw new CustomException(getMessage("EXCEPTION.PWD.CONFIRM_NOT_MATCH"));
        }
        // 비밀번호 정책 확인
        validationPasswordPolicy(request.getPswd());

        // 비밀번호 암호화
        request.setPswd(passwordEncoder.encode(request.getPswd()));

        try {
            if (request.getId() == null) {
                request.setId(sequenceService.selectTokenSequence());
            }
            int result = sysUserMapper.INSERT(request);

            // Register Token

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e + "");

        }
    }

    public int changePassword(SysUser request) throws Exception {
        var sysUser = SysUser.builder().eml(request.getEml()).build();
        List<SysUser> users = sysUserMapper.SELECT(sysUser);
        if (users == null || users.isEmpty() || users.size() != 1) {
            throw new CustomException(getMessage("EXCEPTION.NOT.FOUND.USER"));
        }

        if (!matchToPassword(request)) {
            throw new CustomException(getMessage("EXCEPTION.PWD.NOT_MATCH"));
        }
        validationPasswordPolicy(request.getPswd());

        var user = SysUser.builder()
                .id(users.get(0).getId())
                .pswd(passwordEncoder.encode(request.getPswd()))
                .build();
        return sysUserMapper.UPDATE(user);
    }

    /**
     * DB에서 비밀번호 정책을 조회하여 PasswordPolicy 객체로 변환
     * 캐싱을 통해 매번 DB 조회를 방지하여 성능 개선
     */
    @Cacheable(value = "passwordPolicy", unless = "#result == null")
    private PasswordPolicy getPasswordPolicyFromConfig() throws Exception {
        var sysSiteConfig = SysSiteConfig.builder()
                .cfg_grp_id("PWD_POLICY")
                .use_yn("1")
                .build();
        List<SysSiteConfig> configList = sysSiteConfigService.findImpl(sysSiteConfig);

        // 정책 키-값을 맵으로 변환 후, 정책 파싱은 Core-lib PasswordValidationUtil.buildPolicy 로 위임
        // (정책 파싱 로직을 한 곳에서 관리하여 유지보수성 향상)
        Map<String, String> configMap = new HashMap<>();
        if (configList != null) {
            for (SysSiteConfig config : configList) {
                if (config.getCfg_key() != null) {
                    configMap.put(config.getCfg_key(), config.getCfg_val());
                }
            }
        }
        return new PasswordValidationUtil().buildPolicy(configMap);
    }

    /**
     * 비밀번호 정책 검증 (Core 라이브러리의 PasswordValidationUtil 사용)
     * MessageSource를 통한 국제화 지원 및 모든 에러 메시지 표시
     */
    public void validationPasswordPolicy(String newPassword) {
        try {
            PasswordPolicy policy = getPasswordPolicyFromConfig();
            PasswordValidationUtil validator = new PasswordValidationUtil();
            List<String> errors = validator.validatePassword(newPassword, policy, messageSource);

            if (!errors.isEmpty()) {
                String allErrors = String.join(" / ", errors);
                throw new CustomException(allErrors);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(getMessage("EXCEPTION.PWD.POLICY.LOAD_FAILED"));
        }
    }

    public boolean matchToPassword(SysUser request) {
        var sysUser = SysUser.builder()
                .eml(request.getEml())
                .build();
        List<SysUser> userList = sysUserMapper.SELECT(sysUser);
        String before_pwd_encoded = userList.get(0).getPswd();
        return passwordEncoder.matches(request.getBefore_pwd(), before_pwd_encoded);
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        try {
            // FMS 서비스의 엑셀 업로드 API 호출
            ExcelDataResponse excelData = fmsExcelClient.uploadExcel(file, fmsInternalApiKey);
            // System.out.println("excelData::::" + excelData);
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
