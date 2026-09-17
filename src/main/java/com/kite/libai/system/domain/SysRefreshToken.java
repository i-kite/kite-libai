package com.kite.libai.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷新令牌实体,对应表 sys_refresh_token。
 *
 * <p>属于认证基础设施表,不参与业务审计,也不使用逻辑删除,
 * 因此不继承 BaseEntity(否则会被自动填充 create_by 等不存在的列)。
 *
 * @author kite
 */
@TableName("sys_refresh_token")
public class SysRefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** JWT 的 jti */
    @TableField("token_id")
    private String tokenId;

    @TableField("user_id")
    private Long userId;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /** 是否已撤销:0-否 1-是 */
    @TableField("revoked")
    private Integer revoked;

    @TableField("create_time")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getRevoked() {
        return revoked;
    }

    public void setRevoked(Integer revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
