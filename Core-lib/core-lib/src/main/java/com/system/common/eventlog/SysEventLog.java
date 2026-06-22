package com.system.common.eventlog;

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
public class SysEventLog extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "id" });

    @Id
    /***
     * <pre>
     *  id : 로그 고유 ID
     * </pre>
     */
    private String id;

    /***
     * <pre>
     *  user_id : 행위 수행자 ID
     * </pre>
     */
    private String user_id;

    /***
     * <pre>
     *  email : 행위 수행자 email
     * </pre>
     */
    private String email;

    /***
     * <pre>
     *  role : 수행 당시 권한 (ADMIN, MANAGER, USER)
     * </pre>
     */
    private String role;

    /***
     * <pre>
     *  act_type : 작업 유형
     * </pre>
     */
    private String act_type;

    /***
     * <pre>
     *  tgt_tbl : 대상 테이블명
     * </pre>
     */
    private String tgt_tbl;

    /***
     * <pre>
     *  tgt_id : 대상 데이터의 PK
     * </pre>
     */
    private String tgt_id;

    /***
     * <pre>
     *  bfr_data : 변경 전 데이터
     * </pre>
     */
    private String bfr_data;

    /***
     * <pre>
     *  aft_data : 변경 후 데이터
     * </pre>
     */
    private String aft_data;

    /***
     * <pre>
     *  ip_addr : IPv4 또는 IPv6 주소
     * </pre>
     */
    private String ip_addr;

    /***
     * <pre>
     *  sys_crt_dt : 로그 발생 일시
     * </pre>
     */
    private Date sys_crt_dt;

    /***
     * <pre>
     *  id : 로그 고유 ID
     * </pre>
     */
    @Transient
    private String _id;

    /***
     * <pre>
     *  user_id : 행위 수행자 ID
     * </pre>
     */
    @Transient
    private String _user_id;

    /***
     * <pre>
     *  email : 행위 수행자 email
     * </pre>
     */
    @Transient
    private String _email;

    /***
     * <pre>
     *  role : 수행 당시 권한 (ADMIN, MANAGER, USER)
     * </pre>
     */
    @Transient
    private String _role;

    /***
     * <pre>
     *  act_type : 작업 유형
     * </pre>
     */
    @Transient
    private String _act_type;

    /***
     * <pre>
     *  tgt_tbl : 대상 테이블명
     * </pre>
     */
    @Transient
    private String _tgt_tbl;

    /***
     * <pre>
     *  tgt_id : 대상 데이터의 PK
     * </pre>
     */
    @Transient
    private String _tgt_id;

    /***
     * <pre>
     *  bfr_data : 변경 전 데이터
     * </pre>
     */
    @Transient
    private String _bfr_data;

    /***
     * <pre>
     *  aft_data : 변경 후 데이터
     * </pre>
     */
    @Transient
    private String _aft_data;

    /***
     * <pre>
     *  ip_addr : IPv4 또는 IPv6 주소
     * </pre>
     */
    @Transient
    private String _ip_addr;

    /***
     * <pre>
     *  sys_crt_dt : 로그 발생 일시
     * </pre>
     */
    @Transient
    private String _sys_crt_dt;

}
