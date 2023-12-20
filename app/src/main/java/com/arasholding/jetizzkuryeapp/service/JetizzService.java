package com.arasholding.jetizzkuryeapp.service;

import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.ApiResponseModelObj;
import com.arasholding.jetizzkuryeapp.apimodels.AtfDetayModel;
import com.arasholding.jetizzkuryeapp.apimodels.DeliveryRequest;
import com.arasholding.jetizzkuryeapp.apimodels.GetTalimatRequestModel;
import com.arasholding.jetizzkuryeapp.apimodels.HareketRequestModel;
import com.arasholding.jetizzkuryeapp.apimodels.LoginRequest;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.MusteriIadeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.OnIadeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.ReturnRequest;
import com.arasholding.jetizzkuryeapp.apimodels.SmsResponseModel;
import com.arasholding.jetizzkuryeapp.apimodels.SpinnerModel;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface JetizzService {
    @FormUrlEncoded
    @POST("Auth")
    Call<LoginResponse> login(@Field("username") String username, @Field("password") String password, @Field("grant_type") String grant_type, @Field("scope") String scope);

    @GET("api/Operasyon/MerkezdenGelen")
    Call<ApiResponseModel> MerkezdenGelen(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/SubeYukleme")
    Call<ApiResponseModel> SubeYukleme(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @POST("api/Operasyon/SubeYuklemeMobil")
    Call<ApiResponseModelObj<String[]>> SubeYuklemeMobil(@Header("Authorization")String token, @Body HareketRequestModel request);

    @POST("api/Operasyon/CikisTMIndirmeMobil")
    Call<ApiResponseModel> CikisTMIndirmeMobil(@Header("Authorization")String token, @Body HareketRequestModel request);

    @GET("api/Operasyon/SubeIndirme")
    Call<ApiResponseModel> SubeIndirme(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/HatYukleme")
    Call<ApiResponseModel> HatYukleme(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/HatIndirme")
    Call<ApiResponseModel> HatIndirme(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/DagitimSubeSevk")
    Call<ApiResponseModel> DagitimSubeSevk(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @POST("api/Operasyon/DagitimSubeSevkRequest")
    Call<ApiResponseModel> DagitimSubeSevkRequest(@Header("Authorization")String token, @Body HareketRequestModel request);

    @GET("api/Operasyon/TMSevk")
    Call<ApiResponseModel> TMSevk(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/TMIndirme")
    Call<ApiResponseModel> TMIndirme(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/Zimmet")
    Call<ApiResponseModel> ZimmetAl(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("kuryeId") String kuryeId,@Query("sefer") String sefer,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/DesiGuncelleme")
    Call<ApiResponseModel> DesiGuncelle(@Header("Authorization")String token, @Query("barkod") String barkod,@Query("desi") String desi,@Query("kaynak") String kaynak,@Query("isKoliNo") String isKoliNo);

    @GET("api/Operasyon/GetZimmetListesi")
    Call<List<ZimmetListModel>> GetZimmetListesi(@Header("Authorization")String token,@Query("kuryeId") String kuryeId);

    @GET("api/Operasyon/GetGelenGonderiListesi")
    Call<List<ZimmetListModel>> GetGelenGonderiListesi(@Header("Authorization")String token,@Query("kuryeId") String kuryeId);

    @GET("api/Atf/AtfDetayGetir")
    Call<AtfDetayModel> AtfDetayGetir(@Header("Authorization")String token, @Query("atfId") String atfId);

    @GET("api/Atf/GetTeslimTipleri")
    Call<List<SpinnerModel>> GetTeslimTipleri(@Header("Authorization")String token);

    @GET("api/Operasyon/GetSubeList")
    Call<List<SpinnerModel>> GetSubeler(@Header("Authorization")String token);

    @GET("api/Atf/GetIadeTipleri")
    Call<List<SpinnerModel>> GetIadeTipleri(@Header("Authorization")String token);

    @GET("api/Atf/GetOnIadeTipleri")
    Call<List<SpinnerModel>> GetOnIadeTipleri(@Header("Authorization")String token);

    @POST("api/Atf/TeslimGirisi")
    Call<ApiResponseModel> TeslimGirisi(@Header("Authorization")String token, @Body DeliveryRequest request);

    @POST("api/Atf/IadeGirisi")
    Call<ApiResponseModel> IadeGirisi(@Header("Authorization")String token, @Body ReturnRequest request);

    @POST("api/Atf/OnIadeGirisi")
    Call<ApiResponseModel> OnIadeGirisi(@Header("Authorization")String token, @Body OnIadeRequest request);

    @GET("api/User/PhoneDeviceTokenUpdate")
    Call<ApiResponseModel> PhoneDeviceTokenUpdate(@Header("Authorization")String token, @Query("deviceToken") String deviceToken);

    @GET("api/Operasyon/GetGonderi")
    Call<List<ZimmetListModel>> GetGonderi(@Header("Authorization")String token,@Query("barkod") String barkod);

    @GET("api/Operasyon/GunlukTeslimatSayisi")
    Call<Integer> GunlukTeslimatSayisi(@Header("Authorization")String token);

    @GET("api/Operasyon/GetUygulamaSurumu")
    Call<String> GetUygulamaSurumu(@Header("Authorization")String token);

    @GET("api/Musteri/GetMusteriList")
    Call<List<SpinnerModel>> GetMusteriList(@Header("Authorization")String token);

    @GET("api/Atf/GetTrendyolOrder")
    Call<ApiResponseModel> GetTrendyolOrder(@Header("Authorization")String token,@Query("orderNumber") String orderNumber,@Query("musteriId") int musteriId);

    @POST("api/Atf/HubTopluTeslimGirisi")
    Call<ApiResponseModel> HubTopluTeslimGirisi(@Header("Authorization")String token, @Body DeliveryRequest request);

    @POST ("api/Sefer/MobilDeneme")
    Call<ApiResponseModel> MobilDeneme(@Header("Authorization")String token, @Body MobilDenemeRequest request);

    @POST ("api/Sefer/MobilDenemeImageUpload")
    Call<ApiResponseModel> MobilDenemeImageUpload(@Header("Authorization")String token, @Body MobilDenemeRequest request);

    @GET("api/Sefer/MobilDenemeGetList")
    Call<List<MobilDenemeRequest>> MobilDenemeGetList(@Header("Authorization") String token);

    @GET("api/Atf/N11Alimi")
    Call<ApiResponseModel> N11Alimi(@Header("Authorization")String token, @Query("barkod") String barkod);

    @POST ("api/Operasyon/MusteriIadeGirisi")
    Call<ApiResponseModel> MusteriIadeGirisi(@Header("Authorization")String token, @Body MusteriIadeRequest model);

    @POST("api/Atf/TalimatGetir")
    Call<ApiResponseModel> TalimatGetir(@Header("Authorization")String token, @Body GetTalimatRequestModel model);

    @GET("api/User/SendSms")
    Call<ApiResponseModelObj<String>> SendSms(@Header("Authorization")String token);

    @GET("api/Operasyon/GetNotBirakilanListesi")
    Call<List<ZimmetListModel>> GetNotBirakilanListesi(@Header("Authorization")String token,@Query("kuryeId") String kuryeId);

}
