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
public class SysBbs extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "bbs_id" });

    @Id
    /***
     * <pre>
     *  bbs_mst_id : 게시판 마스터 ID
     * </pre>
     */
    private String bbs_mst_id;

    /***
     * <pre>
     *  bbs_id : 게시판 ID
     * </pre>
     */
    private String bbs_id;
    private String bbs_type; // 게시판 타입 (GALLERY, NOTICE, etc.)

    /***
     * <pre>
     *  bbs_nm : 게시판 이름
     * </pre>
     */
    private String bbs_nm;

    /***
     * <pre>
     *  bbs_mngr : 게시판 관리자
     * </pre>
     */
    private String bbs_mngr;

    /***
     * <pre>
     *  file_atch_yn : 파일첨부 가능 여부
     * </pre>
     */
    private String file_atch_yn;

    /***
     * <pre>
     *  cmnt_yn : 댓글 가능 여부
     * </pre>
     */
    private String cmnt_yn;

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
     *  up_menu_cd : 상위 메뉴 코드
     * </pre>
     */
    private String up_menu_cd;

    /***
     * <pre>
     *  access_group_count : 권한 그룹 개수
     * </pre>
     */
    @Transient
    private int access_group_count;

    /***
     * <pre>
     *  bbs_mst_id : 게시판 마스터 ID
     * </pre>
     */
    @Transient
    private String _bbs_mst_id;

    /***
     * <pre>
     *  bbs_id : 게시판 ID
     * </pre>
     */
    @Transient
    private String _bbs_id;

    /***
     * <pre>
     *  bbs_nm : 게시판 이름
     * </pre>
     */
    @Transient
    private String _bbs_nm;

    /***
     * <pre>
     *  bbs_mngr : 게시판 관리자
     * </pre>
     */
    @Transient
    private String _bbs_mngr;

    /***
     * <pre>
     *  file_atch_yn : 파일첨부 가능 여부
     * </pre>
     */
    @Transient
    private String _file_atch_yn;

    /***
     * <pre>
     *  cmnt_yn : 댓글 가능 여부
     * </pre>
     */
    @Transient
    private String _cmnt_yn;

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
