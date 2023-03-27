package au.id.wale.monty.util

import okhttp3.HttpUrl
import java.io.StringWriter
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun toPTVUrl(devId: String, devKey: String, urlBuilder: HttpUrl.Builder): HttpUrl {
    val HMAC_SHA1_ALGORITHM = "HmacSHA1"
    //val uriWithDeveloperID = "/${path}${if (path.contains("?")) "&" else "?"}devid=${devId}"
    val uriWithDeveloperID = urlBuilder
        .addQueryParameter("devid", devId)
        .build()
        .toString()
        .replace("https://timetableapi.ptv.vic.gov.au", "")

    val key: SecretKey = SecretKeySpec(devKey.toByteArray(), HMAC_SHA1_ALGORITHM)
    val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
    mac.init(key)
    val signatureBytes = mac.doFinal(uriWithDeveloperID.toByteArray())
    val writer = StringWriter()

    for (signatureByte in signatureBytes) {
        writer.append(String.format("%02x", signatureByte))
    }

    return urlBuilder
        .addQueryParameter("signature", writer.toString())
        .build()
}