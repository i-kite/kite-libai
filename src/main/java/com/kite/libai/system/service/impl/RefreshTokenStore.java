package com.kite.libai.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kite.libai.common.constant.SecurityConstants;
import com.kite.libai.system.domain.SysRefreshToken;
import com.kite.libai.system.mapper.SysRefreshTokenMapper;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 刷新令牌白名单存储。
 *
 * <p>单独抽出一个 Bean 而不是直接写在 {@link SysAuthServiceImpl} 里,是为了拿到
 * {@code REQUIRES_NEW} 的事务语义。检测到令牌重复使用时,既要撤销该用户全部刷新令牌,
 * 又要向调用方抛出 401;如果撤销和抛异常发生在同一个事务里,异常会让事务回滚,
 * 把撤销一起撤掉,泄露检测形同虚设。
 *
 * <p>{@code Propagation.REQUIRES_NEW} 会挂起外层事务、另起一个独立事务提交撤销结果,
 * 因此外层无论是否回滚都不影响撤销。注意这依赖 Spring AOP 代理,所以撤销方法必须
 * 定义在本类中由外部 Bean 调用,同类内自调用不会生效。
 *
 * @author kite
 */
@Service
public class RefreshTokenStore {

    private final SysRefreshTokenMapper sysRefreshTokenMapper;

    @Autowired
    public RefreshTokenStore(SysRefreshTokenMapper sysRefreshTokenMapper) {
        this.sysRefreshTokenMapper = sysRefreshTokenMapper;
    }

    /**
     * 按 jti 查询记录,不存在时返回 null。
     */
    public SysRefreshToken findByTokenId(String tokenId) {
        return sysRefreshTokenMapper.selectOne(
                Wrappers.<SysRefreshToken>lambdaQuery().eq(SysRefreshToken::getTokenId, tokenId));
    }

    /**
     * 记录一枚新签发的刷新令牌。
     */
    public void save(String tokenId, Long userId, LocalDateTime expiresAt) {
        SysRefreshToken record = new SysRefreshToken();
        record.setTokenId(tokenId);
        record.setUserId(userId);
        record.setExpiresAt(expiresAt);
        record.setRevoked(SecurityConstants.TOKEN_ACTIVE);
        record.setCreateTime(LocalDateTime.now());
        sysRefreshTokenMapper.insert(record);
    }

    /**
     * 撤销单条记录,随外层事务提交。
     */
    public void revokeById(Long id) {
        SysRefreshToken revoke = new SysRefreshToken();
        revoke.setId(id);
        revoke.setRevoked(SecurityConstants.TOKEN_REVOKED);
        sysRefreshTokenMapper.updateById(revoke);
    }

    /**
     * 撤销指定用户全部未撤销的刷新令牌,随外层事务提交。用于正常登出。
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeAllByUserId(Long userId) {
        doRevokeAll(userId);
    }

    /**
     * 在独立事务中撤销指定用户全部刷新令牌,即使外层事务回滚也保留撤销结果。
     * 用于令牌重复使用(疑似泄露)的处置。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void revokeAllByUserIdInNewTransaction(Long userId) {
        doRevokeAll(userId);
    }

    /**
     * 清理该用户已过期的记录,避免表无限增长,省去额外的定时任务。
     */
    public void deleteExpired(Long userId) {
        sysRefreshTokenMapper.delete(Wrappers.<SysRefreshToken>lambdaQuery()
                .eq(SysRefreshToken::getUserId, userId)
                .lt(SysRefreshToken::getExpiresAt, LocalDateTime.now()));
    }

    private void doRevokeAll(Long userId) {
        SysRefreshToken revoke = new SysRefreshToken();
        revoke.setRevoked(SecurityConstants.TOKEN_REVOKED);
        sysRefreshTokenMapper.update(revoke, Wrappers.<SysRefreshToken>lambdaUpdate()
                .eq(SysRefreshToken::getUserId, userId)
                .eq(SysRefreshToken::getRevoked, SecurityConstants.TOKEN_ACTIVE));
    }
}
