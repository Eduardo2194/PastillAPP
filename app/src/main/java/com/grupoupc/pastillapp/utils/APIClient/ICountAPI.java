package com.grupoupc.pastillapp.utils.APIClient;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ICountAPI {
    @GET
    Call<String> getPath(@Url String url);

}
