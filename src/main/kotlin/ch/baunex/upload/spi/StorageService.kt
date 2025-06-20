package ch.baunex.upload.spi

import java.io.InputStream

/**
 * Abstracts where uploaded files go.
 */
interface StorageService {
    /**
     * Persist the given stream under a generated key and return
     * a URL (or key) that can later be used to fetch or delete it.
     */
    fun save(fileStream: InputStream, originalFilename: String): String

    /**
     * Remove the file at the given URL/key.
     */
    fun delete(fileUrl: String)
}
