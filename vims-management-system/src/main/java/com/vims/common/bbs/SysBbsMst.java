package com.vims.common.bbs;

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
public class SysBbsMst extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "bbs_mst_id" });

    @Id
    /***
     * <pre>
     *  bbs_mst_id : 게시판 고유 코드
     * </pre>
     */
    private String bbs_mst_id;

    /***
     * <pre>
     *  bbs_nm : 게시판 명칭
     * </pre>
     */
    private String bbs_nm;

    /***
     * <pre>
     *  bbs_type : 게시판 유형
     * </pre>
     */
    private String bbs_type;

    /***
     * <pre>
     *  sys_crt_usr_id : 시스템 작성자ID
     * </pre>
     */
    private String sys_crt_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 시스템 등록 일시
     * </pre>
     */
    private Date sys_crt_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 시스템 수정자ID
     * </pre>
     */
    private String sys_upd_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 시스템 수정 일시
     * </pre>
     */
    private Date sys_upd_dt;

    /***
     * <pre>
     *  bbs_mst_id : 게시판 고유 코드
     * </pre>
     */
    @Transient
    private String _bbs_mst_id;

    /***
     * <pre>
     *  bbs_nm : 게시판 명칭
     * </pre>
     */
    @Transient
    private String _bbs_nm;

    /***
     * <pre>
     *  bbs_type : 게시판 유형
     * </pre>
     */
    @Transient
    private String _bbs_type;

    /***
     * <pre>
     *  sys_crt_usr_id : 시스템 작성자ID
     * </pre>
     */
    @Transient
    private String _sys_crt_usr_id;

    /***
     * <pre>
     *  sys_crt_dt : 시스템 등록 일시
     * </pre>
     */
    @Transient
    private Date _sys_crt_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 시스템 수정자ID
     * </pre>
     */
    @Transient
    private String _sys_upd_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 시스템 수정 일시
     * </pre>
     */
    @Transient
    private Date _sys_upd_dt;

}
