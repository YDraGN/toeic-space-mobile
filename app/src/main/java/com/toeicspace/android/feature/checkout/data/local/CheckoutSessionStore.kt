package com.toeicspace.android.feature.checkout.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Qualifier

/** The checkout reference sent with `POST /orders`, and the coupon it was first sent with. */
data class StoredReference(
    val reference: String,
    val couponCode: String?,
)

/**
 * Remembers an unfinished checkout per user and course. Only ids live here (a random GUID and the
 * order id): never the bank account, amount or transfer memo, which are read from the order again.
 */
interface CheckoutSessionStore {
    suspend fun reference(
        userId: String,
        courseId: String,
    ): StoredReference?

    suspend fun saveReference(
        userId: String,
        courseId: String,
        reference: StoredReference,
    )

    suspend fun orderId(
        userId: String,
        courseId: String,
    ): String?

    suspend fun saveOrderId(
        userId: String,
        courseId: String,
        orderId: String,
    )

    suspend fun clear(
        userId: String,
        courseId: String,
    )
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CheckoutDataStore

/** Backed by its own Preferences file, which like every DataStore file is left out of backups. */
internal class DataStoreCheckoutSessionStore
    @Inject
    constructor(
        @CheckoutDataStore private val dataStore: DataStore<Preferences>,
    ) : CheckoutSessionStore {
        override suspend fun reference(
            userId: String,
            courseId: String,
        ): StoredReference? {
            val raw = dataStore.data.first()[referenceKey(userId, courseId)] ?: return null
            val reference = raw.substringBefore(SEPARATOR)
            val coupon = raw.substringAfter(SEPARATOR, missingDelimiterValue = "")
            return StoredReference(reference = reference, couponCode = coupon.ifEmpty { null })
        }

        override suspend fun saveReference(
            userId: String,
            courseId: String,
            reference: StoredReference,
        ) {
            dataStore.edit {
                it[referenceKey(userId, courseId)] =
                    reference.reference + SEPARATOR + reference.couponCode.orEmpty()
            }
        }

        override suspend fun orderId(
            userId: String,
            courseId: String,
        ): String? = dataStore.data.first()[orderKey(userId, courseId)]

        override suspend fun saveOrderId(
            userId: String,
            courseId: String,
            orderId: String,
        ) {
            dataStore.edit { it[orderKey(userId, courseId)] = orderId }
        }

        override suspend fun clear(
            userId: String,
            courseId: String,
        ) {
            dataStore.edit {
                it.remove(referenceKey(userId, courseId))
                it.remove(orderKey(userId, courseId))
            }
        }

        private fun referenceKey(
            userId: String,
            courseId: String,
        ) = stringPreferencesKey("ref_${userId}_$courseId")

        private fun orderKey(
            userId: String,
            courseId: String,
        ) = stringPreferencesKey("order_${userId}_$courseId")

        private companion object {
            const val SEPARATOR = "\n"
        }
    }
