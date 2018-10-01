package com.baracode.eihsan.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ApiClient {
    private static Retrofit retrofit;
    private static Retrofit xenopayRetrofit;

    static Retrofit getApiClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ConstantData.API_SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    static Retrofit getXenopayApiClient() {
        if (xenopayRetrofit == null) {
            xenopayRetrofit = new Retrofit.Builder()
                    .baseUrl(ConstantData.XENOPAY_API_SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return xenopayRetrofit;
    }
}
