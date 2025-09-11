package io.github.wliamp.pro.vrf

class OtpProvider(
    val firebase: IOtp?
) {
    fun of(otp: Otp): IOtp? =
        when (otp) {
            Otp.FIREBASE -> firebase
        }
}
