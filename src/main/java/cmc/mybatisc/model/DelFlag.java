package cmc.mybatisc.model;

/**
 * 删除标志
 */
public enum DelFlag {
    /**
     * 逻辑删除
     */
    DEL_FLAG("del_flag", false, "2", "0"),
    /**
     * 被删除
     */
    IS_DELETED("is_deleted", false, "0", "1"),
    /**
     * 时间戳删除
     */
    IS_DELETE("is_delete", true, null, 0L),
    /**
     * 时间删除
     */
    TIME_DEL_FLAG("delete_at", true, null, null),
    /**
     * 空对象
     */
    EMPTY(null, false, null, null);

    /**
     * 字段名
     */
    public final String fieldName;

    /**
     * 是否是删除时间
     */
    public final boolean isDeleteTime;

    /**
     * 删除值
     */
    public final String deleteValue;

    /**
     * 未删除值
     */
    public final Object notDeleteValue;

    DelFlag(String fieldName, boolean isDeleteTime, String deleteValue, Object notDeleteValue) {
        this.fieldName = fieldName;
        this.isDeleteTime = isDeleteTime;
        this.deleteValue = deleteValue;
        this.notDeleteValue = notDeleteValue;
    }
}
