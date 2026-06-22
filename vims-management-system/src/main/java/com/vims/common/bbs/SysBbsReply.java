package com.vims.common.bbs;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysBbsReply extends Common {
    @Transient
    @Builder.Default
    private String keys = Arrays.toString(new String[] { "cmnt_id" });

    @Id
    private String cmnt_id; // 댓글 ID (UUID)
    private String pst_id; // 게시물 ID (SYS_BBS_PST FK)
    private String prnt_cmnt_id; // 부모 댓글 ID (대댓글용)
    private String cn; // 댓글 내용
    private String wrtr_nm; // 작성자 이름

    private String sys_crt_usr_id;
    private Date sys_crt_dt;
    private String sys_upd_usr_id;
    private Date sys_upd_dt;

    // 검색용 필드
    @Transient
    private String _cn;
    @Transient
    private String _wrtr_nm;
}
