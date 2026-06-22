package com.vims.common.code;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class SysCode extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "cd_id", "grp_id" });

    @Id

    /***
     * <pre>
     *  cd_id : 코드ID
     * </pre>
     */
    private String cd_id;

    /***
     * <pre>
     *  grp_id : 그룹ID
     * </pre>
     */
    private String grp_id;

    /***
     * <pre>
     *  cd_nm : 그룹명
     * </pre>
     */
    private String cd_nm;

    /***
     * <pre>
     *  cd_no : 노출순서
     * </pre>
     */
    private String cd_no;

    /***
     * <pre>
     *  use_yn : 사용여부
     * </pre>
     */
    private String use_yn;

    /***
     * <pre>
     *  cd_id : 코드ID
     * </pre>
     */
    @Transient
    private String _cd_id;

    /***
     * <pre>
     *  grp_id : 그룹ID
     * </pre>
     */
    @Transient
    private String _grp_id;

    /***
     * <pre>
     *  cd_nm : 그룹명
     * </pre>
     */
    @Transient
    private String _cd_nm;

    /***
     * <pre>
     *  cd_no : 노출순서
     * </pre>
     */
    @Transient
    private String _cd_no;

    /***
     * <pre>
     *  use_yn : 사용여부
     * </pre>
     */
    @Transient
    private String _use_yn;

    @Transient
    private Date sys_crt_dt;
    @Transient
    private String sys_crt_usr_id;
    @Transient
    private Date sys_upd_dt;
    @Transient
    private String sys_upd_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 작성일자
     * </pre>
     */
    @Transient
    private java.sql.Date _sys_crt_dt;

    /***
     * <pre>
     *  sys_crt_usr_id : 작성자ID
     * </pre>
     */
    @Transient
    private String _sys_crt_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 수정일자
     * </pre>
     */
    @Transient
    private java.sql.Date _sys_upd_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 수정자ID
     * </pre>
     */
    @Transient
    private String _sys_upd_usr_id;
}
