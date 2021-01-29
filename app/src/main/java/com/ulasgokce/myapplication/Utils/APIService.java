package com.ulasgokce.myapplication.Utils;

import com.ulasgokce.myapplication.Models.MyResponse;
import com.ulasgokce.myapplication.Models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAOoJhmPE:APA91bG0ICzZXZa_WZmP-6FJYaguIfs3vq9tW4dWaqFhgv65j9OIxB6DZa5vgRZLISmKFWIf8mlWXVY82FOrGNkV4AdPG4cZ0lucViFPy5H_282knxVKsI0dSOC1_BNzL17Rh-MgmLV-"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
