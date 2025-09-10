package io.github.wliamp.pro.vrf

class OauthProvider(
    val facebook: IOauth?,
    val google: IOauth?,
    val zalo: IOauth?
) {
    fun of(oauth: Oauth): IOauth? =
        when (oauth) {
            Oauth.FACEBOOK -> facebook
            Oauth.GOOGLE -> google
            Oauth.ZALO -> zalo
        }
}
