package com.vims.common.user;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.Arrays;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysUser extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "id" });

    @Id
    /***
     * <pre>
     *  id : 시퀀스 아이디
     * </pre>
     */
    private Long id;

    /***
     * <pre>
     *  eml : 이메일
     * </pre>
     */
    private String eml;

    /***
     * <pre>
     *  password : 비밀번호
     * </pre>
     */
    private String pswd;

    /***
     * <pre>
     *  role : 역할
     * </pre>
     */
    private String role;

    /***
     * <pre>
     *  user_id : 유저아이디
     * </pre>
     */
    private String user_id;

    /***
     * <pre>
     *  inst_cd : 소속코드
     * </pre>
     */
    private String inst_cd;

    /***
     * <pre>
     *  sys_crt_usr_id : 작성자ID
     * </pre>
     */
    private String sys_crt_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 작성일자
     * </pre>
     */
    private Date sys_crt_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 수정자ID
     * </pre>
     */
    private String sys_upd_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 수정일자
     * </pre>
     */
    private Date sys_upd_dt;

    /***
     * <pre>
     *  fnm : 사용자이름
     * </pre>
     */
    private String fnm;

    /***
     * <pre>
     *  telno : 전화번호
     * </pre>
     */
    private String telno;

    /***
     * <pre>
     *  address : 주소
     * </pre>
     */
    private String addr;

    /***
     * <pre>
     *  addr_dtl : 주소상세
     * </pre>
     */
    private String addr_dtl;

    /***
     * <pre>
     *  post_cd : 우편번호
     * </pre>
     */
    private String post_cd;

    /***
     * <pre>
     *  uuid : 파일ID
     * </pre>
     */
    private String uuid;
    /***
     * <pre>
     *  before_pwd : 기존비밀번호
     * </pre>
     */
    private String before_pwd;
    /***
     * <pre>
     *  before_pwd : 비밀번호 확인
     * </pre>
     */
    @Transient
    private String password_confirm;

    /***
     * <pre>
     *  inst_nm : 소속명
     * </pre>
     */
    @Transient
    private String inst_nm;

    /***
     * <pre>
     *  id : 시퀀스 아이디
     * </pre>
     */
    @Transient
    private String _id;

    /***
     * <pre>
     *  eml : 이메일
     * </pre>
     */
    @Transient
    private String _eml;

    /***
     * <pre>
     *  password : 비밀번호
     * </pre>
     */
    @Transient
    private String _pswd;

    /***
     * <pre>
     *  role : 역할
     * </pre>
     */
    @Transient
    private String _role;

    /***
     * <pre>
     *  user_id : 유저아이디
     * </pre>
     */
    @Transient
    private String _user_id;

    /***
     * <pre>
     *  inst_cd : 소속코드
     * </pre>
     */
    @Transient
    private String _inst_cd;

    /***
     * <pre>
     *  sys_crt_usr_id : 작성자ID
     * </pre>
     */
    @Transient
    private String _sys_crt_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 작성일자
     * </pre>
     */
    @Transient
    private String _sys_crt_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 수정자ID
     * </pre>
     */
    @Transient
    private String _sys_upd_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 수정일자
     * </pre>
     */
    @Transient
    private String _sys_upd_dt;

    /***
     * <pre>
     *  fnm : 사용자이름
     * </pre>
     */
    @Transient
    private String _fnm;

    /***
     * <pre>
     *  telno : 전화번호
     * </pre>
     */
    @Transient
    private String _telno;

    /***
     * <pre>
     *  address : 주소
     * </pre>
     */
    @Transient
    private String _addr;

    /***
     * <pre>
     *  addr_dtl : 주소상세
     * </pre>
     */
    @Transient
    private String _addr_dtl;

    /***
     * <pre>
     *  post_cd : 우편번호
     * </pre>
     */
    @Transient
    private String _post_cd;

    /***
     * <pre>
     *  uuid : 파일ID
     * </pre>
     */
    @Transient
    private String _uuid;

}
