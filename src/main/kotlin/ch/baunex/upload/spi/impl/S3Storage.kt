package ch.baunex.upload.spi.impl

import ch.baunex.upload.spi.S3
import ch.baunex.upload.spi.StorageService
import io.quarkus.arc.properties.IfBuildProperty
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@ApplicationScoped
@S3
@IfBuildProperty(name = "storage.type", stringValue = "s3")
class S3Storage @Inject constructor(
    private val s3Client: S3Client,
    @ConfigProperty(name = "storage.bucket")
    private val bucket: String
) : StorageService {

    override fun save(stream: InputStream, originalFilename: String): String {
        val key = "notes/${UUID.randomUUID()}-${sanitize(originalFilename)}"
        // upload
        val put = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(detectMime(originalFilename))
            .build()
        s3Client.putObject(put, RequestBody.fromInputStream(stream, stream.available().toLong()))
        // build a GET URL; for real presigned URLs, swap this out for S3Presigner
        return "https://${bucket}.s3.amazonaws.com/${urlEncode(key)}"
    }

    override fun delete(fileUrl: String) {
        val key = extractKeyFromUrl(fileUrl)
        val req = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        s3Client.deleteObject(req)
    }

    private fun detectMime(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "gif"         -> "image/gif"
            else          -> "application/octet-stream"
        }

    /** escape spaces etc so path() works in a browser */
    private fun urlEncode(key: String): String =
        URLEncoder.encode(key, StandardCharsets.UTF_8)

    /** strip off the “https://bucket.s3.amazonaws.com/” prefix */
    private fun extractKeyFromUrl(url: String): String {
        val marker = "/${bucket}.s3.amazonaws.com/"
        return url.substringAfter(marker)
    }

    /** remove any path characters that might confuse S3 keys */
    private fun sanitize(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}
