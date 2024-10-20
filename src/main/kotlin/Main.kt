import cn.llonvne.Dotenv
import cn.llonvne.loadIpAddress

enum class IPAddressType {
    IPV4, IPV6
}

@Dotenv(
    prefix = "username",
    prefixNameSpilt = "."
)
data class Username(
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val firstname: String,
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val secondName: String,
    @Dotenv.Field(recursive = true)
    val phoneNumber: PhoneNumber
)

@Dotenv
data class PhoneNumber(
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val isd: String,
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val number: String
)


@Dotenv(
    prefix = "test",
    prefixNameSpilt = "."
)
data class IpAddress(
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val address: String,
    @Dotenv.Field(Dotenv.FieldNamePolicy.FIELD_NAME)
    val ipAddress: IPAddressType,
    @Dotenv.Field(recursive = true)
    val username: Username,
    @Dotenv.Field(recursive = true)
    val user2: Username
)


fun main() {
    val p = Dotenv.loadIpAddress()
    println(p)
}