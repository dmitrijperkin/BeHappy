package com.dmitrij.behappy.data.network;

import com.dmitrij.behappy.model.AlertInfo;
import com.dmitrij.behappy.model.DatasetInfo;
import com.dmitrij.behappy.model.DiskInfo;
import com.dmitrij.behappy.model.IxAppInfo;
import com.dmitrij.behappy.model.PoolInfo;
import com.dmitrij.behappy.model.ServiceInfo;
import com.dmitrij.behappy.model.SmbShare;
import com.dmitrij.behappy.model.SystemInfo;
import com.dmitrij.behappy.model.VmInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TrueNasApi {
    @GET("api/v2.0/system/info") Call<SystemInfo> getSystemInfo();
    @GET("api/v2.0/pool") Call<List<PoolInfo>> getPools();
    @GET("api/v2.0/pool/dataset") Call<List<DatasetInfo>> getDatasets();
    @GET("api/v2.0/pool/snapshottask") Call<List<Map<String, Object>>> getSnapshotTasks();
    @POST("api/v2.0/reporting/get_data") Call<List<Map<String, Object>>> getReportingData(@Body Object body);
    @GET("api/v2.0/service") Call<List<ServiceInfo>> getServices();
    @GET("api/v2.0/alert/list") Call<List<AlertInfo>> getAlerts();
    @GET("api/v2.0/vm") Call<List<VmInfo>> getVms();
    @GET("api/v2.0/app") Call<List<IxAppInfo>> getApps();
    @GET("api/v2.0/chart/release") Call<List<IxAppInfo>> getChartReleases();
    @GET("api/v2.0/sharing/smb") Call<List<SmbShare>> getSmbShares();
    @GET("api/v2.0/disk") Call<List<DiskInfo>> getDisks();
    @GET("api/v2.0/disk/id/{id}") Call<Map<String, Object>> getDiskDetails(@Path("id") String id);
    @GET("api/v2.0/interface/") Call<List<com.dmitrij.behappy.model.NetworkInterface>> getNetworkInterfaces();

    @POST("api/v2.0/service/start") Call<okhttp3.ResponseBody> startService(@Body Map<String, Object> body);
    @POST("api/v2.0/service/stop") Call<okhttp3.ResponseBody> stopService(@Body Map<String, Object> body);

    @POST("api/v2.0/pool/id/{id}/scrub") Call<okhttp3.ResponseBody> scrubPool(@Path("id") Integer id, @Body Map<String, Object> body);
    @POST("api/v2.0/zfs/snapshot") Call<okhttp3.ResponseBody> createSnapshot(@Body Map<String, Object> body);
    @POST("api/v2.0/shell/copy_paste_token") Call<Map<String, String>> getShellToken();

    @POST("api/v2.0/vm/id/{id}/start") Call<okhttp3.ResponseBody> startVm(@Path("id") int id);
    @POST("api/v2.0/vm/id/{id}/stop") Call<okhttp3.ResponseBody> stopVm(@Path("id") int id);
    @POST("api/v2.0/app/start") Call<okhttp3.ResponseBody> startApp(@Body String appName);
    @POST("api/v2.0/app/stop") Call<okhttp3.ResponseBody> stopApp(@Body String appName);
    @POST("api/v2.0/system/reboot") Call<okhttp3.ResponseBody> rebootSystem(@Body Map<String, Object> body);
    @POST("api/v2.0/system/shutdown") Call<okhttp3.ResponseBody> shutdownSystem(@Body Map<String, Object> body);

    @PUT("api/v2.0/sharing/smb/id/{id}") Call<okhttp3.ResponseBody> updateSmbShare(@Path("id") int id, @Body Map<String, Object> body);

    @PUT("api/v2.0/network/interface/id/{id}") Call<okhttp3.ResponseBody> updateNetworkInterface(@Path("id") String id, @Body Map<String, Object> body);
    @PUT("api/v2.0/interface/id/{id}") Call<okhttp3.ResponseBody> updateInterface(@Path("id") String id, @Body Map<String, Object> body);

    @POST("api/v2.0/update/update") Call<okhttp3.ResponseBody> updateSystem(@Body Map<String, Object> body);

    @POST("api/v2.0/core/call") Call<Object> coreCall(@Body Map<String, Object> body);
    @GET("api/v2.0/audit/") Call<List<com.dmitrij.behappy.model.AuditEntry>> getAudits();

    @POST("api/v2.0/disk/temperatures") Call<Map<String, Object>> getDiskTemperatures(@Body Map<String, List<String>> body);
}
