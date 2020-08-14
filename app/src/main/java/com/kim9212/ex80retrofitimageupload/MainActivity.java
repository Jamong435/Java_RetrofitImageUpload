package com.kim9212.ex80retrofitimageupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    ImageView iv;
    //업로드할 이미지의 절대경로 참조변수
    String imgpath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = findViewById(R.id.iv);

        //외부저장소 접근에 대한 동적퍼미션
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "앱을 정상적으로 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    public void clickBtn(View view) {
        // 사진앱을 실행해서 사진을 선택할 수 있도록. [외부저장소 퍼미션 필요]
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK) {
            //선택된 이미지의 uri 를 가지고 돌아온 Intent객체에게 달라고
            Uri uri = data.getData();
            if (uri != null) {
                Glide.with(this).load(uri).into(iv);

                //선택된 이미지를 서버로 전송 [Retrofit library 사용]
                //단, 서버에 전송하려면 파일의 uri가 아니라
                //파일의 실제경로(절대주소)가 필요함.

                //new AlertDialog.Builder(this).setMessage(uri.toString()).create().show();

                //uri-->절대경로로바꿔야한다.
                imgpath=getRealPathFromUri(uri);
                //잘되었는지확인
                new AlertDialog.Builder(this).setMessage(imgpath).show();


            }
        }



    }//onact
    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    public void upload(View view) {
        Retrofit retrofit=RetrofitHelper.newRetrofit();
        RetrofitService retrofitService=retrofit.create(RetrofitService.class);
        //서버에 보낼파일의 multipartbody.part
        File file= new File(imgpath);
        final RequestBody requestBody=RequestBody.create(MediaType.parse("image/*"),file);
        //포장작업이라 생각하면된다 File이라는 객체를 만들어서 그안에 image를 구하는방법이다 다른 video나 text도가능하다
        //['식별자key','파일명','요청객체']를 모두가지고있는 객체
        MultipartBody.Part filepart= MultipartBody.Part.createFormData("img",file.getName(),requestBody);

        Call<String> call=retrofitService.uploadFile(filepart);
        //한방에 모아서 보낸다 이름 파일명 요청객체까지
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String s= response.body();
                    new AlertDialog.Builder(MainActivity.this).setMessage(s).show();

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }
}//main
