package com.kim9212.ex80retrofitimageupload;


import retrofit2.Call;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitService {



    @Multipart
    @POST("/Retrofit/fileUpload.php")
    Call<String> uploadFile(@Part MultipartBody.Part filePart);
}
