package com.system.accslog;

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
public class SysAccsLog extends Common {
    @Transient
    @Builder.Default
    private String keys = Arrays
            .toString(new String[] { "id", "user_id", "sys_login_dt", "dev_type", "os_nm" });

    @Id
    /***
     * <pre>
     *  id : 접속 고유 ID
     * </pre>
     */
    private String id;

    /***
     * <pre>
     *  user_id : 접속 사용자 ID
     * </pre>
     */
    private String user_id;

    /***
     * <pre>
     *  email : 접속 사용자 Email
     * </pre>
     */
    private String email;

    /***
     * <pre>
     *  sys_login_dt : 로그인 일시
     * </pre>
     */
    private Date sys_login_dt;

    /***
     * <pre>
     *  sys_logout_dt : 로그아웃 일시
     * </pre>
     */
    private Date sys_logout_dt;

    /***
     * <pre>
     *  ip_addr : 접속 IP
     * </pre>
     */
    private String ip_addr;

    /***
     * <pre>
     *  dev_type : 기기 유형
     * </pre>
     */
    private String dev_type;

    /***
     * <pre>
     *  os_nm : 운영체제 (Windows, Android 등)
     * </pre>
     */
    private String os_nm;

    /***
     * <pre>
     *  brwsr_nm : 브라우저 (Chrome, Safari 등)
     * </pre>
     */
    private String brwsr_nm;

    /***
     * <pre>
     *  id : 접속 고유 ID
     * </pre>
     */
    @Transient
    private String _id;

    /***
     * <pre>
     *  user_id : 접속 사용자 ID
     * </pre>
     */
    @Transient
    private String _user_id;

    /***
     * <pre>
     *  email : 접속 사용자 Email
     * </pre>
     */
    @Transient
    private String _email;

    /***
     * <pre>
     *  sys_login_dt : 로그인 일시
     * </pre>
     */
    @Transient
    private Date _sys_login_dt;

    /***
     * <pre>
     *  sys_logout_dt : 로그아웃 일시
     * </pre>
     */
    @Transient
    private Date _sys_logout_dt;

    /***
     * <pre>
     *  ip_addr : 접속 IP
     * </pre>
     */
    @Transient
    private String _ip_addr;

    /***
     * <pre>
     *  dev_type : 기기 유형
     * </pre>
     */
    @Transient
    private String _dev_type;

    /***
     * <pre>
     *  os_nm : 운영체제 (Windows, Android 등)
     * </pre>
     */
    @Transient
    private String _os_nm;

    /***
     * <pre>
     *  brwsr_nm : 브라우저 (Chrome, Safari 등)
     * </pre>
     */
    @Transient
    private String _brwsr_nm;

}