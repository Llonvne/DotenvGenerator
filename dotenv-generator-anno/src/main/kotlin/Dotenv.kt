package cn.llonvne

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Dotenv(
    /**
     * 指定生成的读取函数的名称，默认策略是 load<你的类名>
     */
    val loaderFunctionName: String = "load%T",
    val prefix: String = "",
    /**
     * 仅当 prefix 不为空字符串时应用
     */
    val prefixNameSpilt: String = "_"
) {
    annotation class Field(
        /**
         * 命名策略将暗示DotenvGenerator从ENV读取时使用的键的名称，具体见[FieldNamePolicy].
         * 请注意这里指定的键不一定与最终读取的时的完全一致，他可能受[Dotenv]前缀或者后缀影响.
         */
        val namePolicy: FieldNamePolicy = FieldNamePolicy.UPPERCASE_WITH_UNDERSCORE,
        /**
         * 为键指定特殊的名称，使用该参数时请调整 [namePolicy] 为 [FieldNamePolicy.SPECIFIC]，否则该项设置失效.
         */
        val specific: String = ""
    )

    enum class FieldNamePolicy {
        UPPERCASE_WITH_UNDERSCORE,
        FIELD_NAME,
        SPECIFIC,
    }

    companion object
}

