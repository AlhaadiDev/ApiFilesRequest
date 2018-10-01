package com.baracode.eihsan.data;

import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

interface ApiService {
    /* Auth */

    @POST("auth/get-token")
    Call<JsonObject> postAccessToken(@Body HashMap<String, String> inputParams);

    /* Cause */

    @GET("causes")
    Call<JsonObject> getAllCauses(@Header("Authorization") String authorization, @Query("page") String page, @Query("sort") String sort);

    @GET("causes/search")
    Call<JsonObject> getAllCausesWithSearch(@Header("Authorization") String authorization, @Query("name") String name, @Query("page") String page, @Query("sort") String sort);

    @GET("causes/{cause_id}")
    Call<JsonObject> getOneCause(@Header("Authorization") String authorization, @Path("cause_id") String causeId);

    /* Donation */

    @POST("donations")
    Call<JsonObject> postCreateDonation(@Header("Authorization") String authorization, @Body HashMap<String, String> inputParams);

    @POST("donations/{donation_id}")
    Call<JsonObject> postUpdateDonation(@Header("Authorization") String authorization, @Header("X-HTTP-Method-Override") String methodOverride, @Path("donation_id") String donationId, @Body HashMap<String, String> inputParams);

    @GET("donations/search")
    Call<JsonObject> getAllLiveFeeds(@Header("Authorization") String authorization, @Query("sort") String sort, @Query("status") String status);

    @GET("donations/beneficiary-summary")
    Call<JsonObject> getBeneficiarySummary(@Header("Authorization") String authorization, @QueryMap(encoded = true) HashMap<String, String> queries);

    @GET("donations/summary")
    Call<JsonObject> getSummary(@Header("Authorization") String authorization, @Query("beneficiary_type") String beneficiaryType, @Query("report_type") String reportType);

    @GET("donations/beneficiary-statistic")
    Call<JsonObject> getBeneficiaryStatistic(@Header("Authorization") String authorization, @QueryMap(encoded = true) HashMap<String, String> queries);

    /* Headline */

    @GET("headlines")
    Call<JsonObject> getAllHeadlines(@Header("Authorization") String authorization, @Query("page") String page);

    @GET("headlines/{headline_id}")
    Call<JsonObject> getOneHeadline(@Header("Authorization") String authorization, @Path("headline_id") String headlineIc);

    @GET("headlines/search")
    Call<JsonObject> getOneOrgAllHeadlines(@Header("Authorization") String authorization, @Query("organization_id") String orgId);

    /* Organization */

    @GET("organizations")
    Call<JsonObject> getAllOrg(@Header("Authorization") String authorization, @Query("page") String page, @Query("sort") String sort);

    @GET("organizations/search")
    Call<JsonObject> getAllOrgWithSearch(@Header("Authorization") String authorization, @Query("name") String name, @Query("page") String page, @Query("sort") String sort);

    @GET("organizations/{organization_id}")
    Call<JsonObject> getOneOrg(@Header("Authorization") String authorization, @Path("organization_id") String orgId);

    @GET("organizations/{organization_id}/causes")
    Call<JsonObject> getOneOrgAllCauses(@Header("Authorization") String authorization, @Path("organization_id") String orgId);

    @GET("organizations/search")
    Call<JsonObject> getOneOrgAllChildOrg(@Header("Authorization") String authorization, @Query("parent_id") String parentId);

    /* Reward */
    @GET("reward-options")
    Call<JsonObject> getReward (@Header("Authorization") String authorization, @Query("sort") String sort);

}
