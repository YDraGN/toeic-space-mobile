package com.toeicspace.android.feature.checkout.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Payment service order endpoints, behind the gateway's `/payment` prefix. All need a user token. */
interface CheckoutApiService {
    @GET("payment/api/v1/orders/preview")
    suspend fun previewPrice(
        @Query("courseId") courseId: String,
        @Query("couponCode") couponCode: String?,
    ): PricePreviewDto

    @POST("payment/api/v1/orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequestDto,
    ): OrderResultDto

    @GET("payment/api/v1/orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String,
    ): OrderResultDto

    /** Answers 200 with an empty body. */
    @POST("payment/api/v1/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: String,
    )
}
