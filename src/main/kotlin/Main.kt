import cn.llonvne.Dotenv
import cn.llonvne.loadEnvProperty
import io.github.cdimascio.dotenv.dotenv

enum class IPAddress {
    IPV4, IPV6
}


@Dotenv(
    prefix = "test",
    prefixNameSpilt = "."
)
data class EnvProperty(

    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val version: String,
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val ipAddress: IPAddress,
)


fun main() {
    val p = Dotenv.loadEnvProperty()
    println(p)
}