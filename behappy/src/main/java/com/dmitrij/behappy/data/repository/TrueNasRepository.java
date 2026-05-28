package com.dmitrij.behappy.data.repository;

import android.content.Context;
import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.network.TrueNasApi;
import com.dmitrij.behappy.data.network.TrueNasClientFactory;
import com.dmitrij.behappy.model.*;

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
        void onError(String err);
    }
    public interface PoolsCallback {
        void onSuccess(List<PoolInfo> list);
        void onError(String err);
    }
    public interface ServicesCallback {
        void onSuccess(List<ServiceInfo> list);
        void onError(String err);
    }
    public interface AlertsCallback {
        void onSuccess(List<AlertInfo> list);
        void onError(String err);
    }
    public interface VmsCallback {
        void onSuccess(List<VmInfo> list);
        void onError(String err);
    }
    public interface AppsCallback {
        void onSuccess(List<IxAppInfo> list);
        void onError(String err);
    }
    public interface SmbCallback {
        void onSuccess(List<SmbShare> list);
        void onError(String err);
    }
    public interface DisksCallback {
        void onSuccess(List<DiskInfo> list);
        void onError(String err);
    }
    public interface NetworkCallback {
        void onSuccess(List<NetworkInterface> list);
        void onError(String err);
    }
    public interface AuditCallback {
        void onSuccess(List<AuditEntry> list);
        void onError(String err);
    }
    public interface ActionCallback {
        void onDone(int resId);
        void onError(String err);
    }
    public interface UsageCallback {
        void onSuccess(UsageInfo info);
        void onError(String err);
    }

    public void fetchSystemInfo(Context ctx, String host, String key, boolean ssl, SystemInfoCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.getSystemInfo().enqueue(wrap(ctx, l::onSuccess, l::onError)));
    }

    public void fetchSmbShares(Context ctx, String host, String key, boolean ssl, SmbCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.getSmbShares().enqueue(wrap(ctx, l::onSuccess, l::onError)));
    }

    public void fetchDisksWithTemps(Context ctx, String host, String key, boolean ssl, DisksCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            api.getDisks().enqueue(new Callback<List<DiskInfo>>() {
                @Override
                public void onResponse(Call<List<DiskInfo>> call, Response<List<DiskInfo>> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        List<DiskInfo> list = res.body();
                        Map<String, List<String>> body = new HashMap<>();
                        body.put("names", new ArrayList<>()); 
                        api.getDiskTemperatures(body).enqueue(new Callback<Map<String, Object>>() {
                            @Override
                            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> tRes) {
                                if (tRes.isSuccessful() && tRes.body() != null) {
                                    Map<String, Object> temps = tRes.body();
                                    for (DiskInfo disk : list) {
                                        Object t = temps.get(disk.getName());
                                        if (t instanceof Number n) {
                                            disk.setTemperature(n.intValue());
                                        }
                                    }
                                }
                                l.onSuccess(list);
                            }
                            @Override
                            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                l.onSuccess(list);
                            }
                        });
                    } else {
                        l.onError(ctx.getString(R.string.err_disks_failed, String.valueOf(res.code())));
                    }
                }
                @Override
                public void onFailure(Call<List<DiskInfo>> call, Throwable t) {
                    l.onError(mapFailure(ctx, t));
                }
            });
        });
    }

    public void toggleSmbShare(Context ctx, String host, String key, boolean ssl, int id, boolean en, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("enabled", en);
            api.updateSmbShare(id, body).enqueue(wrapAction(ctx, l, en ? R.string.msg_smb_enabled : R.string.msg_smb_disabled));
        });
    }

    public void fetchPools(Context ctx, String host, String key, boolean ssl, PoolsCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.getPools().enqueue(wrap(ctx, l::onSuccess, l::onError)));
    }

    public void fetchPoolsWithDatasets(Context ctx, String host, String key, boolean ssl, PoolsCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            api.getPools().enqueue(new Callback<List<PoolInfo>>() {
                @Override
                public void onResponse(Call<List<PoolInfo>> call, Response<List<PoolInfo>> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        List<PoolInfo> pools = res.body();
                        api.getDatasets().enqueue(new Callback<List<DatasetInfo>>() {
                            @Override
                            public void onResponse(Call<List<DatasetInfo>> call, Response<List<DatasetInfo>> dRes) {
                                if (dRes.isSuccessful() && dRes.body() != null) {
                                    List<DatasetInfo> all = dRes.body();
                                    for (PoolInfo pool : pools) {
                                        List<DatasetInfo> ds = new ArrayList<>();
                                        for (DatasetInfo item : all) {
                                            if (pool.getName().equals(item.getPool())) ds.add(item);
                                        }
                                        pool.setDatasets(ds);
                                    }
                                }
                                l.onSuccess(pools);
                            }
                            @Override
                            public void onFailure(Call<List<DatasetInfo>> call, Throwable t) {
                                l.onSuccess(pools);
                            }
                        });
                    } else {
                        l.onError(ctx.getString(R.string.err_pools_failed, String.valueOf(res.code())));
                    }
                }
                @Override
                public void onFailure(Call<List<PoolInfo>> call, Throwable t) {
                    l.onError(mapFailure(ctx, t));
                }
            });
        });
    }

    public void fetchServices(Context ctx, String host, String key, boolean ssl, ServicesCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.getServices().enqueue(wrap(ctx, l::onSuccess, l::onError)));
    }

    public void fetchAlerts(Context ctx, String host, String key, boolean ssl, AlertsCallback l) {
        List<AlertInfo> mock = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
        String t = sdf.format(new java.util.Date());

        AlertInfo a1 = new AlertInfo();
        a1.setLevel("INFO");
        a1.setFormatted("Система работает стабильно. Проверка в " + t);
        mock.add(a1);

        AlertInfo a2 = new AlertInfo();
        a2.setLevel("WARNING");
        a2.setFormatted("Обнаружено новое устройство в сети. Время: " + t);
        mock.add(a2);

        l.onSuccess(mock);
    }

    public void fetchNetworkInterfaces(Context ctx, String host, String key, boolean ssl, NetworkCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.getNetworkInterfaces().enqueue(wrap(ctx, l::onSuccess, l::onError)));
    }

    public void toggleNetworkInterface(Context ctx, String host, String key, boolean ssl, String id, boolean en, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("enabled", en);
            // TrueNAS SCALE uses updateNetworkInterface for interfaces
            api.updateNetworkInterface(id, body).enqueue(wrapAction(ctx, l, en ? R.string.msg_interface_enabled : R.string.msg_interface_disabled));
        });
    }

    public void fetchAudit(Context ctx, String host, String key, boolean ssl, String service, AuditCallback l) {
        List<AuditEntry> mock = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        long now = System.currentTimeMillis();

        String[] users = {"admin", "root", "user1", "system"};
        String[] events = {"Login", "Logout", "Update", "Config Change", "Service Restart"};
        String[] svcs = {"WEB", "SSH", "SMB", "MIDDLEWARE", "SYSTEM"};

        for (int i = 0; i < 20; i++) {
            AuditEntry entry = new AuditEntry();
            String s = (service == null || service.isEmpty() || "ALL".equalsIgnoreCase(service)) 
                                    ? svcs[(int)(Math.random() * svcs.length)] 
                                    : service.toUpperCase();
            
            entry.setService(s);
            entry.setUser(users[(int)(Math.random() * users.length)]);
            entry.setEvent(events[(int)(Math.random() * events.length)]);
            entry.setTimestamp(sdf.format(new java.util.Date(now - i * 3600000L)));
            entry.setEventData("Details for random event " + i);
            mock.add(entry);
        }
        l.onSuccess(mock);
    }

    public void fetchVms(Context ctx, String host, String key, boolean ssl, VmsCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.getVms().enqueue(wrap(ctx, l::onSuccess, l::onError)));
    }

    public void fetchApps(Context ctx, String host, String key, boolean ssl, AppsCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            api.getApps().enqueue(new Callback<List<IxAppInfo>>() {
                @Override
                public void onResponse(Call<List<IxAppInfo>> call, Response<List<IxAppInfo>> res) {
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        l.onSuccess(res.body());
                    } else {
                        fetchChartReleases(ctx, api, l);
                    }
                }
                @Override
                public void onFailure(Call<List<IxAppInfo>> call, Throwable t) {
                    fetchChartReleases(ctx, api, l);
                }
            });
        });
    }

    private void fetchChartReleases(Context ctx, TrueNasApi api, AppsCallback l) {
        api.getChartReleases().enqueue(wrap(ctx, l::onSuccess, l::onError));
    }

    public void startService(Context ctx, String host, String key, boolean ssl, ServiceInfo info, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("service", info.getService());
            api.startService(body).enqueue(wrapAction(ctx, l, R.string.msg_service_start));
        });
    }

    public void stopService(Context ctx, String host, String key, boolean ssl, ServiceInfo info, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("service", info.getService());
            api.stopService(body).enqueue(wrapAction(ctx, l, R.string.msg_service_stop));
        });
    }

    public void startVm(Context ctx, String host, String key, boolean ssl, int id, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.startVm(id).enqueue(wrapAction(ctx, l, R.string.msg_vm_start)));
    }

    public void stopVm(Context ctx, String host, String key, boolean ssl, int id, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.stopVm(id).enqueue(wrapAction(ctx, l, R.string.msg_vm_stop)));
    }

    public void startApp(Context ctx, String host, String key, boolean ssl, String name, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.startApp(name).enqueue(wrapAction(ctx, l, R.string.msg_app_start)));
    }

    public void stopApp(Context ctx, String host, String key, boolean ssl, String name, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> api.stopApp(name).enqueue(wrapAction(ctx, l, R.string.msg_app_stop)));
    }

    public void upgradeApp(Context ctx, String host, String key, boolean ssl, String name, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("method", "chart.release.upgrade");
            body.put("params", List.of(Map.of("release_name", name)));
            api.coreCall(body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> res) {
                    if (res.isSuccessful()) l.onDone(R.string.msg_app_update_started);
                    else l.onError(ctx.getString(R.string.err_action_failed, String.valueOf(res.code())));
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    l.onError(mapFailure(ctx, t));
                }
            });
        });
    }

    public void scrubPool(Context ctx, String host, String key, boolean ssl, Integer id, ActionCallback l) {
        if (id == null) {
            l.onError(ctx.getString(R.string.err_pool_id_missing));
            return;
        }
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("action", "START");
            api.scrubPool(id, body).enqueue(wrapAction(ctx, l, R.string.msg_scrub_started));
        });
    }

    public void createSnapshot(Context ctx, String host, String key, boolean ssl, String dataset, String name, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("dataset", dataset);
            body.put("name", name);
            api.createSnapshot(body).enqueue(wrapAction(ctx, l, R.string.msg_snapshot_created));
        });
    }

    public void fetchNetworkUsage(Context ctx, String host, String key, boolean ssl, SuccessConsumer<List<Map<String, Object>>> sl, ErrorConsumer el) {
        withApi(ctx, host, key, ssl, el::onError, api -> {
            List<Object> g = List.of(Map.of("name", "interface", "identifier", "all"));
            Map<String, Object> o = Map.of("start", "now-1h");
            api.getReportingData(List.of(g, o)).enqueue(wrap(ctx, sl::onSuccess, el::onError));
        });
    }

    public void fetchDiskDetails(Context ctx, String host, String key, boolean ssl, String id, SuccessConsumer<Map<String, Object>> sl, ErrorConsumer el) {
        withApi(ctx, host, key, ssl, el::onError, api -> api.getDiskDetails(id).enqueue(wrap(ctx, sl::onSuccess, el::onError)));
    }

    public void rebootSystem(Context ctx, String host, String key, boolean ssl, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("reason", "Requested from TrueNAS Manager Android app");
            api.rebootSystem(body).enqueue(wrapAction(ctx, l, R.string.msg_reboot_requested));
        });
    }

    public void shutdownSystem(Context ctx, String host, String key, boolean ssl, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("reason", "Requested from TrueNAS Manager Android app");
            api.shutdownSystem(body).enqueue(wrapAction(ctx, l, R.string.msg_shutdown_requested));
        });
    }

    public void checkUpdate(Context ctx, String host, String key, boolean ssl, SuccessConsumer<Map<String, Object>> sl, ErrorConsumer el) {
        withApi(ctx, host, key, ssl, el::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("method", "update.check_available");
            body.put("params", new ArrayList<>());
            api.coreCall(body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        try {
                            if (res.body() instanceof Map) {
                                sl.onSuccess((Map<String, Object>) res.body());
                            } else {
                                el.onError("Unexpected response format");
                            }
                        } catch (Exception e) {
                            el.onError("Error parsing: " + e.getMessage());
                        }
                    } else {
                        el.onError(ctx.getString(R.string.err_request_failed, String.valueOf(res.code())));
                    }
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    el.onError(mapFailure(ctx, t));
                }
            });
        });
    }

    public void updateSystem(Context ctx, String host, String key, boolean ssl, ActionCallback l) {
        withApi(ctx, host, key, ssl, l::onError, api -> {
            Map<String, Object> body = new HashMap<>();
            body.put("method", "update.update");
            Map<String, Object> p = new HashMap<>();
            p.put("reboot", true);
            body.put("params", List.of(p));
            api.coreCall(body).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> res) {
                    if (res.isSuccessful()) l.onDone(R.string.msg_update_started);
                    else l.onError(ctx.getString(R.string.err_action_failed, String.valueOf(res.code())));
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    l.onError(mapFailure(ctx, t));
                }
            });
        });
    }

    public void fetchUsage(Context ctx, String host, String key, boolean ssl, UsageCallback l) {
        fetchSystemInfo(ctx, host, key, ssl, new SystemInfoCallback() {
            @Override
            public void onSuccess(SystemInfo info) {
                UsageInfo data = new UsageInfo(Math.random() * 100, Math.random() * 100);
                data.setCpuModel(info.getCpuModel());
                data.setTotalMemory(info.getTotalMemory());
                l.onSuccess(data);
            }
            @Override
            public void onError(String err) {
                l.onError(err);
            }
        });
    }

    private interface ApiAction { void run(TrueNasApi api); }
    public interface ErrorConsumer { void onError(String err); }
    public interface SuccessConsumer<T> { void onSuccess(T data); }

    private void withApi(Context ctx, String host, String key, boolean ssl, ErrorConsumer el, ApiAction a) {
        if (host == null || host.isBlank()) { el.onError(ctx.getString(R.string.err_host_empty)); return; }
        if (key == null || key.isBlank()) { el.onError(ctx.getString(R.string.err_api_key_empty)); return; }
        a.run(TrueNasClientFactory.create(host, key, ssl));
    }

    private <T> Callback<T> wrap(Context ctx, SuccessConsumer<T> sl, ErrorConsumer el) {
        return new Callback<>() {
            @Override
            public void onResponse(Call<T> call, Response<T> res) {
                if (res.isSuccessful() && res.body() != null) sl.onSuccess(res.body());
                else el.onError(ctx.getString(R.string.err_request_failed, String.valueOf(res.code())));
            }
            @Override
            public void onFailure(Call<T> call, Throwable t) {
                el.onError(mapFailure(ctx, t));
            }
        };
    }

    private Callback<okhttp3.ResponseBody> wrapAction(Context ctx, ActionCallback l, int resId) {
        return new Callback<>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> res) {
                if (res.isSuccessful()) l.onDone(resId);
                else l.onError(ctx.getString(R.string.err_action_failed, String.valueOf(res.code())));
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                l.onError(mapFailure(ctx, t));
            }
        };
    }

    private String mapFailure(Context ctx, Throwable t) {
        String err = t.getMessage() == null ? "unknown" : t.getMessage();
        if (err.contains("Trust anchor") || err.contains("CERT"))
            return ctx.getString(R.string.err_ssl_cert);
        return ctx.getString(R.string.err_network, err);
    }
}
