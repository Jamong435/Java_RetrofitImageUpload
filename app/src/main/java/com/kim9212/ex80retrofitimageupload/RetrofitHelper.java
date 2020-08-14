package com.kim9212.ex80retrofitimageupload;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitHelper  {

    public static Retrofit newRetrofit(){

        Retrofit.Builder builder= new Retrofit.Builder();
        builder.baseUrl("http://toki666.dothome/co.kr");
        builder.addConverterFactory(ScalarsConverterFactory.create());
        return  builder.build();
    }
}
