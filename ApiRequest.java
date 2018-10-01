package com.baracode.eihsan.data;

import android.content.Context;
import android.os.Build;

import com.baracode.eihsan.MainApplication;
import com.baracode.eihsan.model.Organization;
import com.baracode.eihsan.model.Reward;
import com.baracode.eihsan.util.DeviceUuidFactory;
import com.baracode.eihsan.model.Cause;
import com.baracode.eihsan.model.Accomplishments;
import com.baracode.eihsan.model.LiveFeed;
import com.baracode.eihsan.model.SummaryStatistics;
import com.baracode.eihsan.util.StaticFunction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiRequest {
    private static ApiRequest apiRequest;
    private ApiService apiService;
    private ApiPaymentService xenopayApiService;
    private Gson gson;

    private ApiRequest() {
        apiService = ApiClient.getApiClient().create(ApiService.class);
        xenopayApiService = ApiClient.getXenopayApiClient().create(ApiPaymentService.class);
        gson = new Gson();
    }

    public static ApiRequest getInstance() {
        if (apiRequest == null) {
            apiRequest = new ApiRequest();
        }

        return apiRequest;
    }

    /* Auth */

    public void postAccessToken(Context context, final GetAccessTokenCallback apiCallback) {
        DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(context);
        String uuid = deviceUuidFactory.getDeviceUuid();

        HashMap<String, String> inputParams = new HashMap<>();
        inputParams.put("device_id", uuid);
        inputParams.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
        inputParams.put("device_email", "");
        inputParams.put("device_mac", "");

        Call<JsonObject> call = apiService.postAccessToken(inputParams);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("postAccessToken: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    apiCallback.postAccessTokenSuccess(jsonData.get("access_token").getAsString());
                } else {
                    apiCallback.postAccessTokenFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.postAccessTokenFail("Data error");
            }
        });
    }

    /* Cause */

    public void getAllCauses(String searchQuery, String sorting, final int currentPage, final GetAllCausesCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getAllCauses(auth, Integer.toString(currentPage), sorting);

        if (!searchQuery.equals("")) {
            call = apiService.getAllCausesWithSearch(auth, searchQuery, Integer.toString(currentPage), sorting);
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getAllCauses: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();
                Headers headers = response.headers();

                if (isSuccess) {
                    int totalItemCount = Integer.parseInt(headers.get("x-pagination-total-count"));
                    int totalPageCount = Integer.parseInt(headers.get("x-pagination-page-count"));

                    if (currentPage <= totalPageCount && totalItemCount > 0) {
                        JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                        Type listType = new TypeToken<ArrayList<Cause>>() {
                        }.getType();
                        ArrayList<Cause> causeList = jsonArrayConverter(jsonDataArray, -1, Cause.class, listType);

                        if (currentPage < totalPageCount) {
                            apiCallback.getAllCausesSuccess(causeList, true);
                        } else {
                            apiCallback.getAllCausesSuccess(causeList, false);
                        }
                    } else {
                        ArrayList<Cause> causeList = new ArrayList<>();
                        apiCallback.getAllCausesSuccess(causeList, false);
                    }
                } else {
                    apiCallback.getAllCausesFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getAllCausesFail("Data error");
            }
        });
    }

    public void getOneCause(String causeId, final GetCausedDetailsCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getOneCause(auth, causeId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getOneCause: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    Cause cause = gson.fromJson(jsonData, Cause.class);
                    apiCallback.getCausedDetailsSuccess(cause);
                } else {
                    apiCallback.getCausedDetailsFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getCausedDetailsFail("Data error");
            }
        });
    }

    /* Donation */

    public void postCreateDonation(HashMap<String, String> inputParams, final PostCreateDonation apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.postCreateDonation(auth, inputParams);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("postCreateDonation: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    apiCallback.postCreateDonationSuccess(jsonData.get("id").getAsString());
                } else {
                    apiCallback.postCreateDonationFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.postCreateDonationFail("Data error");
            }
        });
    }

    public void postUpdateDonation(String donationId, HashMap<String, String> inputParams, final PostUpdateDonationCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.postUpdateDonation(auth, "PUT", donationId, inputParams);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("postUpdateDonation: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    apiCallback.postUpdateDonationIsSuccess();
                } else {
                    apiCallback.postUpdateDonationIsFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.postUpdateDonationIsFail("Data error");
            }
        });
    }

    public void getAllLiveFeeds(final GetAllLiveFeedsCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getAllLiveFeeds(auth, "-created_at", "1");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getAllLiveFeeds: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    Type listType = new TypeToken<ArrayList<LiveFeed>>() {
                    }.getType();
                    ArrayList<LiveFeed> liveFeed = jsonArrayConverter(jsonDataArray, -1, LiveFeed.class, listType);

                    apiCallback.getAllLiveFeedsSuccess(liveFeed);
                } else {
                    apiCallback.getAllLiveFeedsFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getAllLiveFeedsFail("Data error");
            }
        });

    }

    public void getBeneficiarySummary(HashMap<String, String> inputParams, final GetBeneficiarySummaryCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getBeneficiarySummary(auth, inputParams);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getBeneficiarySummary: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    Type listType = new TypeToken<ArrayList<SummaryStatistics>>() {
                    }.getType();
                    ArrayList<SummaryStatistics> summaryStatisticsArrayList = jsonArrayConverter(jsonDataArray, -1, SummaryStatistics.class, listType);

                    apiCallback.getBeneficiarySummarySuccess(summaryStatisticsArrayList);
                } else {
                    apiCallback.getBeneficiarySummaryFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getBeneficiarySummaryFail("Data error");
            }
        });
    }

    public void getSummary(String beneficiaryType, String reportType, final GetSummaryCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getSummary(auth, beneficiaryType, reportType);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getSummary: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    JsonObject jsonDataObj = jsonDataArray.get(0).getAsJsonObject();

                    HashMap<String, String> outputParams = new HashMap<>();
                    outputParams.put("count", getJsonDataString(jsonDataObj, "count", "0"));
                    outputParams.put("amount", getJsonDataString(jsonDataObj, "amount", "0.00"));

                    apiCallback.getSummarySuccess(outputParams);
                } else {
                    apiCallback.getSummaryFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getSummaryFail("Data error");
            }
        });
    }

    public void getBeneficiaryStatistic(HashMap<String, String> inputParams, final GetBeneficiaryStatistic apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getBeneficiaryStatistic(auth, inputParams);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getBeneficiaryStatistic: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    JsonArray jsonStatisticArray = jsonData.get("statistic").getAsJsonArray();
                    HashMap<String, String> resultParams = new HashMap<>();
                    ArrayList<SummaryStatistics> tempList = new ArrayList<>();

                    for (int i = 0; i < jsonStatisticArray.size(); i++) {
                        JsonObject temp = jsonStatisticArray.get(i).getAsJsonObject();
                        SummaryStatistics tempSummary = new SummaryStatistics();
                        tempSummary.setDate(getJsonDataString(temp, "date", ""));
                        tempSummary.setCount(getJsonDataString(temp, "count", "0"));
                        tempSummary.setAmount(getJsonDataString(temp, "amount", "0.00"));
                        tempList.add(tempSummary);
                    }

                    resultParams.put("total_count", getJsonDataString(jsonData, "totalcount", "0"));
                    resultParams.put("total_amount", getJsonDataString(jsonData, "totalamount", "0.00"));
                    resultParams.put("target", getJsonDataString(jsonData, "target", "0"));
                    apiCallback.getBeneficiaryStatisticSuccess(resultParams, tempList);
                } else {
                    apiCallback.getBeneficiaryStatisticFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getBeneficiaryStatisticFail("Data error");
            }
        });
    }

    /* Headline */

    public void getAllHeadlines(final int currentPage, final GetAllHeadlinesCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getAllHeadlines(auth, Integer.toString(currentPage));

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getAllHeadlines: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();
                Headers headers = response.headers();

                if (isSuccess) {
                    int totalItemCount = Integer.parseInt(headers.get("x-pagination-total-count"));
                    int totalPageCount = Integer.parseInt(headers.get("x-pagination-page-count"));

                    if (currentPage <= totalPageCount && totalItemCount > 0) {
                        JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                        Type listType = new TypeToken<ArrayList<Accomplishments>>() {
                        }.getType();
                        ArrayList<Accomplishments> accomplishmentsList = jsonArrayConverter(jsonDataArray, -1, Accomplishments.class, listType);

                        if (currentPage < totalPageCount) {
                            apiCallback.getAllHeadlinesSuccess(accomplishmentsList, true);
                        } else {
                            apiCallback.getAllHeadlinesSuccess(accomplishmentsList, false);
                        }
                    } else {
                        ArrayList<Accomplishments> accomplishmentsList = new ArrayList<>();
                        apiCallback.getAllHeadlinesSuccess(accomplishmentsList, false);
                    }
                } else {
                    apiCallback.getAllHeadlinesFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getAllHeadlinesFail("Data error");
            }
        });
    }

    public void getOneHeadline(String headlineId, final GetOneHeadlineCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getOneHeadline(auth, headlineId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getOneHeadline: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    Accomplishments accomplishments = gson.fromJson(jsonData, Accomplishments.class);
                    apiCallback.getOneHeadlineSuccess(accomplishments);
                } else {
                    apiCallback.getOneHeadlineFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getOneHeadlineFail("Data error");
            }
        });
    }

    public void getOneOrgAllHeadlines(String orgId, final GetOneOrgAllHeadlinesCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getOneOrgAllHeadlines(auth, orgId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getOneOrgAllHeadlines: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    Type listType = new TypeToken<ArrayList<Accomplishments>>() {
                    }.getType();
                    ArrayList<Accomplishments> accomplishmentsList = jsonArrayConverter(jsonDataArray, -1, Accomplishments.class, listType);
                    apiCallback.getOneOrgAllHeadlinesSuccess(accomplishmentsList);
                } else {
                    apiCallback.getOneOrgAllHeadlinesFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getOneOrgAllHeadlinesFail("Data error");
            }
        });
    }

    /* Organization */

    public void getAllOrg(String searchQuery, String sorting, final int currentPage, final GetAllOrgCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getAllOrg(auth, Integer.toString(currentPage), sorting);

        if (!searchQuery.equals("")) {
            call = apiService.getAllOrgWithSearch(auth, searchQuery, Integer.toString(currentPage), sorting);
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getAllOrg: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();
                Headers headers = response.headers();

                if (isSuccess) {
                    int totalItemCount = Integer.parseInt(headers.get("x-pagination-total-count"));
                    int totalPageCount = Integer.parseInt(headers.get("x-pagination-page-count"));

                    if (currentPage <= totalPageCount && totalItemCount > 0) {
                        JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                        Type listType = new TypeToken<ArrayList<Organization>>() {
                        }.getType();
                        ArrayList<Organization> orgList = jsonArrayConverter(jsonDataArray, -1, Organization.class, listType);

                        if (currentPage < totalPageCount) {
                            apiCallback.getAllOrgSuccess(orgList, true);
                        } else {
                            apiCallback.getAllOrgSuccess(orgList, false);
                        }
                    } else {
                        ArrayList<Organization> orgList = new ArrayList<>();
                        apiCallback.getAllOrgSuccess(orgList, false);
                    }
                } else {
                    apiCallback.getAllOrgFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getAllOrgFail("Data error");
            }
        });
    }

    public void getOneOrg(String orgId, final GetOneOrgCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getOneOrg(auth, orgId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getOneOrg: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    Organization org = gson.fromJson(jsonData, Organization.class);
                    apiCallback.getOneOrgSuccess(org);
                } else {
                    apiCallback.getOneOrgFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getOneOrgFail("Data error");
            }
        });
    }

    public void getOneOrgAllCauses(String orgId, final GetOneOrgAllCausesCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getOneOrgAllCauses(auth, orgId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getOneOrgAllCauses: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    Type listType = new TypeToken<ArrayList<Cause>>() {
                    }.getType();
                    ArrayList<Cause> causedList = jsonArrayConverter(jsonDataArray, -1, Cause.class, listType);
                    apiCallback.getOneOrgAllCausesSuccess(causedList);
                } else {
                    apiCallback.getOneOrgAllCausesFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getOneOrgAllCausesFail("Data error");
            }
        });
    }

    public void getOneOrgAllChildOrg(String orgId, final GetOneOrgAllChildOrg apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getOneOrgAllChildOrg(auth, orgId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getOneOrgAllChildOrg: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    Type listType = new TypeToken<ArrayList<Organization>>() {
                    }.getType();
                    ArrayList<Organization> orgList = jsonArrayConverter(jsonDataArray, -1, Organization.class, listType);
                    apiCallback.getOneOrgAllChildOrgSuccess(orgList);
                } else {
                    apiCallback.getOneOrgAllChildFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                apiCallback.getOneOrgAllChildFail("Data error");
            }
        });
    }

    /* Payment */

    public void postCreateBill(HashMap<String, String> inputParams, final PostCreateBillCallback apiCallback) {
        String auth = "Bearer " + ConstantData.XENOPAY_USER_ACCESS_TOKEN;

        HashMap<String, Object> inputP = new HashMap<>();
        inputP.put("Bill", inputParams);

        Call<JsonObject> call = xenopayApiService.postCreateBill(auth, inputP);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("postCreateBill: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();
                    JsonObject jsonContent = jsonData.get("content").getAsJsonObject();

                    HashMap<String, String> resultParams = new HashMap<>();
                    resultParams.put("id", getJsonDataString(jsonContent, "id", ""));
                    resultParams.put("ref_no", getJsonDataString(jsonContent, "ref_no", ""));
                    resultParams.put("currency_code", getJsonDataString(jsonContent, "currency_code", ""));
                    resultParams.put("amount", getJsonDataString(jsonContent, "amount", ""));
                    resultParams.put("email", getJsonDataString(jsonContent, "email", ""));
                    resultParams.put("username", getJsonDataString(jsonContent, "username", ""));
                    resultParams.put("contact", getJsonDataString(jsonContent, "contact", ""));
                    resultParams.put("url", getJsonDataString(jsonContent, "url", ""));
                    resultParams.put("description", getJsonDataString(jsonContent, "description", ""));

                    apiCallback.postCreateBillSuccess(resultParams);
                } else {
                    apiCallback.postCreateBillFail("Data error occurred.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (StaticFunction.isConnected(MainApplication.getInstance())) {
                    apiCallback.postCreateBillFail("Data error occurred.");
                } else {
                    apiCallback.postCreateBillFail("There is no internet connection.");
                }
            }
        });
    }

    public void getBill(String billId, final GetBillCallback apiCallback) {
        String auth = "Bearer " + ConstantData.XENOPAY_USER_ACCESS_TOKEN;
        Call<JsonObject> call = xenopayApiService.getBill(auth, billId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getBill: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonObject jsonData = response.body().get("data").getAsJsonObject();

                    HashMap<String, String> resultParams = new HashMap<>();
                    resultParams.put("id", getJsonDataString(jsonData, "id", ""));
                    resultParams.put("ref_no", getJsonDataString(jsonData, "ref_no", ""));
                    resultParams.put("currency_code", getJsonDataString(jsonData, "currency_code", ""));
                    resultParams.put("amount", getJsonDataString(jsonData, "amount", ""));
                    resultParams.put("description", getJsonDataString(jsonData, "description", ""));
                    resultParams.put("payment_id", getJsonDataString(jsonData, "payment_id", ""));
                    resultParams.put("username", getJsonDataString(jsonData, "username", ""));
                    resultParams.put("email", getJsonDataString(jsonData, "email", ""));
                    resultParams.put("contact", getJsonDataString(jsonData, "contact", ""));
                    resultParams.put("remark", getJsonDataString(jsonData, "remark", ""));
                    resultParams.put("status", getJsonDataString(jsonData, "status", ""));
                    resultParams.put("status_description", getJsonDataString(jsonData, "status_description", ""));
                    resultParams.put("created_at", getJsonDataString(jsonData, "created_at", ""));
                    resultParams.put("updated_at", getJsonDataString(jsonData, "updated_at", ""));

                    apiCallback.getBillSuccess(resultParams);
                } else {
                    apiCallback.getBillFail("Data error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (StaticFunction.isConnected(MainApplication.getInstance())) {
                    apiCallback.getBillFail("Data error");
                } else {
                    apiCallback.getBillFail("There is no internet connection.");
                }
            }
        });
    }

    /* Reward */

    public void getReward(final GetRewardCallback apiCallback) {
        String auth = "Bearer " + LocalData.getInstance().loadStringData(LocalData.SP_KEY_ACCESS_TOKEN);
        Call<JsonObject> call = apiService.getReward(auth, "sort_order");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                StaticFunction.showLogD("getReward: " + response.body().toString());
                Boolean isSuccess = response.body().get("success").getAsBoolean();

                if (isSuccess) {
                    JsonArray jsonDataArray = response.body().get("data").getAsJsonArray();
                    Type listType = new TypeToken<ArrayList<Reward>>() {
                    }.getType();
                    ArrayList<Reward> rewardsList = jsonArrayConverter(jsonDataArray, -1, Reward.class, listType);
                    apiCallback.getRewardSuccess(rewardsList);
                } else {
                    apiCallback.getRewardFail("Data Error");
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                apiCallback.getRewardFail("Data Error");
            }
        });
    }

    //json array convert to json String Array
    private <T> ArrayList<T> jsonArrayConverter(JsonArray response, int index, Class<T> className, Type listType) {
        ArrayList<T> dataList;

        if (response.size() == 0) {
            return new ArrayList<>();
        }
        if (index == -1) {
            dataList = gson.fromJson(response.toString(), listType);
        } else if (null != response.get(index) && response.get(index).isJsonArray()) {
            dataList = gson.fromJson(response.get(index).toString(), listType);
        } else if (null != response.get(index) && response.get(index).isJsonObject()) {
            dataList = new ArrayList<>();
            dataList.add(gson.fromJson(response.get(index).toString(), className));
        } else {
            dataList = new ArrayList<>();
        }

        return dataList;
    }


    /* Json functions convert dataType */

    public static int getJsonDataInt(JsonObject jsonObject, String key, int defaultValue) {
        try {
            return jsonObject.get(key).getAsInt();
        } catch (Throwable rr) {
            return defaultValue;
        }
    }

    public static String getJsonDataString(JsonObject jsonObject, String key, String defaultValue) {
        try {
            return jsonObject.get(key).getAsString();
        } catch (Throwable rr) {
            return defaultValue;
        }
    }

    public static Boolean getJsonDataBoolean(JsonObject jsonObject, String key, boolean defaultValue) {
        try {
            return jsonObject.get(key).getAsBoolean();
        } catch (Throwable rr) {
            return defaultValue;
        }
    }

    public static JsonObject getJsonObject(JsonObject jsonObject, String key) {
        try {
            return jsonObject.get(key).getAsJsonObject();
        } catch (Throwable rr) {
            return null;
        }
    }

    public static JsonArray getJsonArray(JsonObject jsonObject, String key) {
        try {
            return jsonObject.get(key).getAsJsonArray();
        } catch (Throwable rr) {
            return new JsonArray();
        }
    }

    /* API callback */

    public interface GetAccessTokenCallback {
        void postAccessTokenSuccess(String result);

        void postAccessTokenFail(String message);
    }

    public interface GetAllCausesCallback {
        void getAllCausesSuccess(ArrayList<Cause> result, boolean hasMoreItems);

        void getAllCausesFail(String message);
    }

    public interface GetCausedDetailsCallback {
        void getCausedDetailsSuccess(Cause result);

        void getCausedDetailsFail(String message);
    }

    public interface PostCreateDonation {
        void postCreateDonationSuccess(String result);

        void postCreateDonationFail(String message);
    }

    public interface PostUpdateDonationCallback {
        void postUpdateDonationIsSuccess();

        void postUpdateDonationIsFail(String message);
    }

    public interface GetAllLiveFeedsCallback {
        void getAllLiveFeedsSuccess(ArrayList<LiveFeed> result);

        void getAllLiveFeedsFail(String message);
    }

    public interface GetBeneficiarySummaryCallback {
        void getBeneficiarySummarySuccess(ArrayList<SummaryStatistics> result);

        void getBeneficiarySummaryFail(String message);
    }

    public interface GetSummaryCallback {
        void getSummarySuccess(HashMap<String, String> result);

        void getSummaryFail(String message);
    }

    public interface GetBeneficiaryStatistic {
        void getBeneficiaryStatisticSuccess(HashMap<String, String> resultParams, ArrayList<SummaryStatistics> resultList);

        void getBeneficiaryStatisticFail(String message);
    }

    public interface GetAllHeadlinesCallback {
        void getAllHeadlinesSuccess(ArrayList<Accomplishments> result, boolean hasMoreItems);

        void getAllHeadlinesFail(String message);
    }

    public interface GetOneHeadlineCallback {
        void getOneHeadlineSuccess(Accomplishments result);

        void getOneHeadlineFail(String message);
    }

    public interface GetOneOrgAllHeadlinesCallback {
        void getOneOrgAllHeadlinesSuccess(ArrayList<Accomplishments> result);

        void getOneOrgAllHeadlinesFail(String message);
    }

    public interface GetOneOrgCallback {
        void getOneOrgSuccess(Organization result);

        void getOneOrgFail(String message);
    }

    public interface GetAllOrgCallback {
        void getAllOrgSuccess(ArrayList<Organization> result, boolean hasMoreItems);

        void getAllOrgFail(String message);
    }

    public interface GetOneOrgAllCausesCallback {
        void getOneOrgAllCausesSuccess(ArrayList<Cause> result);

        void getOneOrgAllCausesFail(String message);
    }

    public interface GetOneOrgAllChildOrg {
        void getOneOrgAllChildOrgSuccess(ArrayList<Organization> result);

        void getOneOrgAllChildFail(String message);
    }

    public interface PostCreateBillCallback {
        void postCreateBillSuccess(HashMap<String, String> result);

        void postCreateBillFail(String message);
    }

    public interface GetBillCallback {
        void getBillSuccess(HashMap<String, String> result);

        void getBillFail(String message);
    }

    public interface GetRewardCallback {
        void getRewardSuccess(ArrayList<Reward> result);

        void getRewardFail(String message);
    }
}
