package com.vims.common.office;

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
public class SysOffice extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "inst_cd" });

    @Id
    /***
     * <pre>
     *  up_inst_cd : 최상위 코드
     * </pre>
     */
    private String up_inst_cd;

    /***
     * <pre>
     *  inst_nm : 기관명
     * </pre>
     */
    private String inst_nm;

    /***
     * <pre>
     *  inst_cd : 기관코드
     * </pre>
     */
    private String inst_cd;

    /***
     * <pre>
     *  inst_se_cd : 기관유형
     * </pre>
     */
    private String inst_se_cd;

    /***
     * <pre>
     *  inst_type_cd : 등록관청 구분부호
     * </pre>
     */
    private String inst_type_cd;

    /***
     * <pre>
     *  sys_crt_usr_id : 생성자ID
     * </pre>
     */
    private String sys_crt_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 생성일자
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
     *  up_inst_cd : 최상위 코드
     * </pre>
     */
    @Transient
    private String _up_inst_cd;

    /***
     * <pre>
     *  inst_nm : 기관명
     * </pre>
     */
    @Transient
    private String _inst_nm;

    /***
     * <pre>
     *  inst_cd : 기관코드
     * </pre>
     */
    @Transient
    private String _inst_cd;

    /***
     * <pre>
     *  inst_se_cd : 기관유형
     * </pre>
     */
    @Transient
    private String _inst_se_cd;

    /***
     * <pre>
     *  inst_type_cd : 등록관청 구분부호
     * </pre>
     */
    @Transient
    private String _inst_type_cd;

    /***
     * <pre>
     *  sys_crt_usr_id : 생성자ID
     * </pre>
     */
    @Transient
    private String _sys_crt_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 생성일자
     * </pre>
     */
    @Transient
    private Date _sys_crt_dt;

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
    private Date _sys_upd_dt;

}
