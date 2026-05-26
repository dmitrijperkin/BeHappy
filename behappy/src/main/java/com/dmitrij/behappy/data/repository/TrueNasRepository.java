package com.dmitrij.behappy.data.repository;

import android.content.Context;
import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.network.TrueNasApi;
import com.dmitrij.behappy.data.network.TrueNasClientFactory;
import com.dmitrij.behappy.model.AlertInfo;
import com.dmitrij.behappy.model.DatasetInfo;
import com.dmitrij.behappy.model.DiskInfo;
import com.dmitrij.behappy.model.IxAppInfo;
import com.dmitrij.behappy.model.PoolInfo;
import com.dmitrij.behappy.model.ServiceInfo;
import com.dmitrij.behappy.model.SmbShare;
import com.dmitrij.behappy.model.SystemInfo;
import com.dmitrij.behappy.model.UsageInfo;
import com.dmitrij.behappy.model.VmInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrueNasRepository {
    private static TrueNasRepository instance;

    public static synchronized TrueNasRepository getInstance() {
        if (instance == null) {
            instance = new TrueNasRepository();
        }
        return instance;
    }

    public interface SystemInfoCallback {
        void onSuccess(SystemInfo info);
        void onError(String message);
    }
    public interface PoolsCallback {
        void onSuccess(List<PoolInfo> info);
        void onError(String message);
    }
    public interface ServicesCallback {
        void onSuccess(List<ServiceInfo> info);
        void onError(String message);
    }
    public interface AlertsCallback {
        void onSuccess(List<AlertInfo> info);
        void onError(String message);
    }
    public interface VmsCallback {
        void onSuccess(List<VmInfo> info);
        void onError(String message);
    }
    public interface AppsCallback {
        void onSuccess(List<IxAppInfo> info);
        void onError(String message);
    }
    public interface SmbCallback {
        void onSuccess(List<SmbShare> info);
        void onError(String message);
    }
    public interface DisksCallback {
        void onSuccess(List<DiskInfo> info);
        void onError(String message);
    }
    public interface ActionCallback {
        void onDone(int messageResId);
        void onError(String message);
    }
    public interface UsageCallback {
        void onSuccess(UsageInfo info);
        void onError(String message);
    }

    public void fetchSystemInfo(Context context, String host, String key, boolean selfSigned, SystemInfoCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.getSystemInfo().enqueue(wrap(context, cb::onSuccess, cb::onError)));
    }

    public void fetchSmbShares(Context context, String host, String key, boolean selfSigned, SmbCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.getSmbShares().enqueue(wrap(context, cb::onSuccess, cb::onError)));
    }

    public void fetchDisksWithTemps(Context context, String host, String key, boolean selfSigned, DisksCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            api.getDisks().enqueue(new Callback<List<DiskInfo>>() {
                @Override
                public void onResponse(Call<List<DiskInfo>> call, Response<List<DiskInfo>> diskResponse) {
                    if (diskResponse.isSuccessful() && diskResponse.body() != null) {
                        List<DiskInfo> disks = diskResponse.body();
                        
                        Map<String, List<String>> body = new HashMap<>();
                        body.put("names", new ArrayList<>()); 

                        api.getDiskTemperatures(body).enqueue(new Callback<Map<String, Object>>() {
                            @Override
                            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> tempResponse) {
                                if (tempResponse.isSuccessful() && tempResponse.body() != null) {
                                    Map<String, Object> temps = tempResponse.body();
                                    for (DiskInfo disk : disks) {
                                        Object tempObj = temps.get(disk.getName());
                                        if (tempObj instanceof Number) {
                                            disk.setTemperature(((Number) tempObj).intValue());
                                        }
                                    }
                                }
                                cb.onSuccess(disks);
                            }

                            @Override
                            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                cb.onSuccess(disks);
                            }
                        });
                    } else {
                        cb.onError(context.getString(R.string.err_disks_failed, String.valueOf(diskResponse.code())));
                    }
                }

                @Override
                public void onFailure(Call<List<DiskInfo>> call, Throwable t) {
                    cb.onError(mapFailure(context, t));
                }
            });
        });
    }

    public void toggleSmbShare(Context context, String host, String key, boolean selfSigned, int id, boolean enabled, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("enabled", enabled);
            api.updateSmbShare(id, body).enqueue(wrapAction(context, cb, enabled ? R.string.msg_smb_enabled : R.string.msg_smb_disabled));
        });
    }

    public void fetchPools(Context context, String host, String key, boolean selfSigned, PoolsCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.getPools().enqueue(wrap(context, cb::onSuccess, cb::onError)));
    }

    public void fetchPoolsWithDatasets(Context context, String host, String key, boolean selfSigned, PoolsCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            api.getPools().enqueue(new Callback<List<PoolInfo>>() {
                @Override
                public void onResponse(Call<List<PoolInfo>> call, Response<List<PoolInfo>> poolResponse) {
                    if (poolResponse.isSuccessful() && poolResponse.body() != null) {
                        List<PoolInfo> pools = poolResponse.body();
                        api.getDatasets().enqueue(new Callback<List<DatasetInfo>>() {
                            @Override
                            public void onResponse(Call<List<DatasetInfo>> call, Response<List<DatasetInfo>> dsResponse) {
                                if (dsResponse.isSuccessful() && dsResponse.body() != null) {
                                    List<DatasetInfo> allDatasets = dsResponse.body();
                                    for (PoolInfo pool : pools) {
                                        List<DatasetInfo> poolDatasets = new ArrayList<>();
                                        for (DatasetInfo ds : allDatasets) {
                                            if (pool.getName().equals(ds.getPool())) {
                                                poolDatasets.add(ds);
                                            }
                                        }
                                        pool.setDatasets(poolDatasets);
                                    }
                                }
                                cb.onSuccess(pools);
                            }

                            @Override
                            public void onFailure(Call<List<DatasetInfo>> call, Throwable t) {
                                cb.onSuccess(pools);
                            }
                        });
                    } else {
                        cb.onError(context.getString(R.string.err_pools_failed, String.valueOf(poolResponse.code())));
                    }
                }

                @Override
                public void onFailure(Call<List<PoolInfo>> call, Throwable t) {
                    cb.onError(mapFailure(context, t));
                }
            });
        });
    }

    public void fetchServices(Context context, String host, String key, boolean selfSigned, ServicesCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.getServices().enqueue(wrap(context, cb::onSuccess, cb::onError)));
    }

    public void fetchAlerts(Context context, String host, String key, boolean selfSigned, AlertsCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.getAlerts().enqueue(wrap(context, cb::onSuccess, cb::onError)));
    }

    public void fetchVms(Context context, String host, String key, boolean selfSigned, VmsCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.getVms().enqueue(wrap(context, cb::onSuccess, cb::onError)));
    }

    public void fetchApps(Context context, String host, String key, boolean selfSigned, AppsCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            api.getApps().enqueue(new Callback<List<IxAppInfo>>() {
                @Override
                public void onResponse(Call<List<IxAppInfo>> call, Response<List<IxAppInfo>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        cb.onSuccess(response.body());
                    } else {
                        fetchChartReleases(context, api, cb);
                    }
                }

                @Override
                public void onFailure(Call<List<IxAppInfo>> call, Throwable t) {
                    fetchChartReleases(context, api, cb);
                }
            });
        });
    }

    private void fetchChartReleases(Context context, TrueNasApi api, AppsCallback cb) {
        api.getChartReleases().enqueue(wrap(context, cb::onSuccess, cb::onError));
    }

    public void startService(Context context, String host, String key, boolean selfSigned, ServiceInfo service, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("service", service.getService());
            api.startService(body).enqueue(wrapAction(context, cb, R.string.msg_service_start));
        });
    }

    public void stopService(Context context, String host, String key, boolean selfSigned, ServiceInfo service, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("service", service.getService());
            api.stopService(body).enqueue(wrapAction(context, cb, R.string.msg_service_stop));
        });
    }

    public void startVm(Context context, String host, String key, boolean selfSigned, int id, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.startVm(id).enqueue(wrapAction(context, cb, R.string.msg_vm_start)));
    }

    public void stopVm(Context context, String host, String key, boolean selfSigned, int id, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.stopVm(id).enqueue(wrapAction(context, cb, R.string.msg_vm_stop)));
    }

    public void startApp(Context context, String host, String key, boolean selfSigned, String appName, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.startApp(appName).enqueue(wrapAction(context, cb, R.string.msg_app_start)));
    }

    public void stopApp(Context context, String host, String key, boolean selfSigned, String appName, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> api.stopApp(appName).enqueue(wrapAction(context, cb, R.string.msg_app_stop)));
    }

    public void upgradeApp(Context context, String host, String key, boolean selfSigned, String appName, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("method", "chart.release.upgrade");
            body.put("params", List.of(Map.of("release_name", appName)));
            
            api.coreCall(body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.isSuccessful()) cb.onDone(R.string.msg_app_update_started);
                    else cb.onError(context.getString(R.string.err_action_failed, String.valueOf(response.code())));
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    cb.onError(mapFailure(context, t));
                }
            });
        });
    }

    public void scrubPool(Context context, String host, String key, boolean selfSigned, Integer id, ActionCallback cb) {
        if (id == null) {
            cb.onError(context.getString(R.string.err_pool_id_missing));
            return;
        }
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("action", "START");
            api.scrubPool(id, body).enqueue(wrapAction(context, cb, R.string.msg_scrub_started));
        });
    }

    public void createSnapshot(Context context, String host, String key, boolean selfSigned, String dataset, String name, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("dataset", dataset);
            body.put("name", name);
            api.createSnapshot(body).enqueue(wrapAction(context, cb, R.string.msg_snapshot_created));
        });
    }

    public void fetchNetworkUsage(Context context, String host, String key, boolean selfSigned, SuccessConsumer<List<Map<String, Object>>> cb, ErrorConsumer ecb) {
        withApi(context, host, key, selfSigned, ecb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("graphs", List.of(Map.of("name", "interface", "identifier", "all")));
            api.getReportingData(body).enqueue(wrap(context, cb::onSuccess, ecb::onError));
        });
    }

    public void fetchDiskDetails(Context context, String host, String key, boolean selfSigned, String diskId, SuccessConsumer<Map<String, Object>> cb, ErrorConsumer ecb) {
        withApi(context, host, key, selfSigned, ecb::onError, api -> api.getDiskDetails(diskId).enqueue(wrap(context, cb::onSuccess, ecb::onError)));
    }

    public void rebootSystem(Context context, String host, String key, boolean selfSigned, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("reason", "Requested from TrueNAS Manager Android app");
            api.rebootSystem(body).enqueue(wrapAction(context, cb, R.string.msg_reboot_requested));
        });
    }

    public void shutdownSystem(Context context, String host, String key, boolean selfSigned, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("reason", "Requested from TrueNAS Manager Android app");
            api.shutdownSystem(body).enqueue(wrapAction(context, cb, R.string.msg_shutdown_requested));
        });
    }

    public void checkUpdate(Context context, String host, String key, boolean selfSigned, SuccessConsumer<Map<String, Object>> cb, ErrorConsumer ecb) {
        withApi(context, host, key, selfSigned, ecb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("method", "update.check_available");
            body.put("params", new ArrayList<>());
            api.coreCall(body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            if (response.body() instanceof Map) {
                                cb.onSuccess((Map<String, Object>) response.body());
                            } else {
                                ecb.onError("Unexpected response format from core.call");
                            }
                        } catch (Exception ex) {
                            ecb.onError("Error parsing response: " + ex.getMessage());
                        }
                    } else {
                        ecb.onError(context.getString(R.string.err_request_failed, String.valueOf(response.code())));
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    ecb.onError(mapFailure(context, t));
                }
            });
        });
    }

    public void updateSystem(Context context, String host, String key, boolean selfSigned, ActionCallback cb) {
        withApi(context, host, key, selfSigned, cb::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("method", "update.update");
            Map<String, Object> params = new HashMap<>();
            params.put("reboot", true);
            body.put("params", List.of(params));
            
            api.coreCall(body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.isSuccessful()) cb.onDone(R.string.msg_update_started);
                    else cb.onError(context.getString(R.string.err_action_failed, String.valueOf(response.code())));
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    cb.onError(mapFailure(context, t));
                }
            });
        });
    }

    public void fetchUsage(Context context, String host, String key, boolean selfSigned, UsageCallback cb) {
        fetchSystemInfo(context, host, key, selfSigned, new SystemInfoCallback() {
            @Override
            public void onSuccess(SystemInfo info) {
                UsageInfo usage = new UsageInfo(Math.random() * 100, Math.random() * 100);
                usage.setCpuModel(info.getCpuModel());
                usage.setTotalMemory(info.getTotalMemory());
                cb.onSuccess(usage);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    private interface ApiAction { void run(TrueNasApi api); }
    public interface ErrorConsumer { void onError(String msg); }
    public interface SuccessConsumer<T> { void onSuccess(T data); }

    private void withApi(Context context, String host, String key, boolean selfSigned, ErrorConsumer onError, ApiAction action) {
        if (host == null || host.isBlank()) { onError.onError(context.getString(R.string.err_host_empty)); return; }
        if (key == null || key.isBlank()) { onError.onError(context.getString(R.string.err_api_key_empty)); return; }
        action.run(TrueNasClientFactory.create(host, key, selfSigned));
    }

    private <T> Callback<T> wrap(Context context, SuccessConsumer<T> s, ErrorConsumer e) {
        return new Callback<>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) s.onSuccess(response.body());
                else e.onError(context.getString(R.string.err_request_failed, String.valueOf(response.code())));
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                e.onError(mapFailure(context, t));
            }
        };
    }

    private Callback<okhttp3.ResponseBody> wrapAction(Context context, ActionCallback cb, int successMsgResId) {
        return new Callback<>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    cb.onDone(successMsgResId);
                } else {
                    cb.onError(context.getString(R.string.err_action_failed, String.valueOf(response.code())));
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                cb.onError(mapFailure(context, t));
            }
        };
    }

    private String mapFailure(Context context, Throwable t) {
        String msg = t.getMessage() == null ? "unknown" : t.getMessage();
        if (msg.contains("Trust anchor") || msg.contains("CERT"))
            return context.getString(R.string.err_ssl_cert);
        return context.getString(R.string.err_network, msg);
    }
}
