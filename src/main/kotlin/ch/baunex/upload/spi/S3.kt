package ch.baunex.upload.spi

import jakarta.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Qualifier
@Retention(RUNTIME)
@Target(CLASS, FUNCTION, FIELD, VALUE_PARAMETER)
annotation class S3