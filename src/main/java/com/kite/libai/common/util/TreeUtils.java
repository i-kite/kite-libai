package com.kite.libai.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用树形结构构建工具。
 *
 * <p>部门树与菜单树的组装逻辑完全一致,抽到这里避免两处重复实现。
 * 采用一次遍历建索引、再一次遍历挂父子的方式,复杂度 O(n),避免递归查库。
 *
 * @author kite
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 将平铺列表组装为树。
     *
     * <p>父节点不在列表中的节点会被视为根节点一并返回,防止因数据权限过滤
     * 掉了上级而导致子节点整体丢失。
     *
     * @param nodes       平铺节点列表
     * @param idGetter    取节点 ID
     * @param pidGetter   取父节点 ID
     * @param childSetter 设置子节点列表
     * @param <T>         节点类型
     * @param <I>         ID 类型
     * @return 根节点列表,顺序与入参一致
     */
    public static <T, I> List<T> build(Collection<T> nodes,
                                       Function<T, I> idGetter,
                                       Function<T, I> pidGetter,
                                       BiConsumer<T, List<T>> childSetter) {
        List<T> roots = new ArrayList<>();
        if (nodes == null || nodes.isEmpty()) {
            return roots;
        }

        Map<I, T> nodeMap = new HashMap<>(nodes.size());
        Map<I, List<T>> childrenMap = new HashMap<>(nodes.size());
        Set<I> ids = new HashSet<>(nodes.size());
        for (T node : nodes) {
            I id = idGetter.apply(node);
            nodeMap.put(id, node);
            ids.add(id);
        }

        for (T node : nodes) {
            I pid = pidGetter.apply(node);
            if (pid != null && ids.contains(pid)) {
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(node);
            } else {
                roots.add(node);
            }
        }

        for (Map.Entry<I, List<T>> entry : childrenMap.entrySet()) {
            T parent = nodeMap.get(entry.getKey());
            if (parent != null) {
                childSetter.accept(parent, entry.getValue());
            }
        }
        return roots;
    }
}
