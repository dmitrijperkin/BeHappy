package com.dmitrij.behappy.data.network;

import com.dmitrij.behappy.data.StatsManager;
import com.dmitrij.behappy.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class DemoTrueNasApi implements TrueNasApi {

    private static DemoTrueNasApi instance;
    private final Gson gson = new Gson();

    private List<ServiceInfo> services;
    private List<VmInfo> vms;
    private List<IxAppInfo> apps;
    private List<SmbShare> shares;

    private DemoTrueNasApi() {
        initData();
        prepopulateStats();
    }

    public static synchronized DemoTrueNasApi getInstance() {
        if (instance == null) {
            instance = new DemoTrueNasApi();
        }
        return instance;
    }

    private void initData() {
        services = gson.fromJson("[" +
                "{\"service\":\"ssh\",\"state\":\"RUNNING\",\"id\":1}," +
                "{\"service\":\"smb\",\"state\":\"RUNNING\",\"id\":2}," +
                "{\"service\":\"nfs\",\"state\":\"STOPPED\",\"id\":3}," +
                "{\"service\":\"s3\",\"state\":\"STOPPED\",\"id\":4}," +
                "{\"service\":\"webdav\",\"state\":\"STOPPED\",\"id\":5}" +
                "]", new TypeToken<List<ServiceInfo>>() {}.getType());

        vms = gson.fromJson("[" +
                "{\"name\":\"Ubuntu-Server\",\"status\":{\"state\":\"RUNNING\"},\"id\":1}," +
                "{\"name\":\"Home-Assistant\",\"status\":{\"state\":\"RUNNING\"},\"id\":2}," +
                "{\"name\":\"Windows-11-Test\",\"status\":{\"state\":\"STOPPED\"},\"id\":3}" +
                "]", new TypeToken<List<VmInfo>>() {}.getType());

        apps = gson.fromJson("[" +
                "{\"name\":\"Nextcloud\",\"state\":\"RUNNING\",\"version\":\"27.1.3\",\"upgrade_available\":false}," +
                "{\"name\":\"Plex\",\"state\":\"RUNNING\",\"version\":\"1.32.5\",\"upgrade_available\":true}," +
                "{\"name\":\"Pi-hole\",\"state\":\"RUNNING\",\"version\":\"5.17.1\",\"upgrade_available\":false}" +
                "]", new TypeToken<List<IxAppInfo>>() {}.getType());

        shares = gson.fromJson("[" +
                "{\"id\":1,\"path\":\"/mnt/tank/media\",\"name\":\"Media\",\"enabled\":true,\"comment\":\"Public media share\"}," +
                "{\"id\":2,\"path\":\"/mnt/tank/backups\",\"name\":\"Backups\",\"enabled\":true,\"comment\":\"Time Machine backups\"}," +
                "{\"id\":3,\"path\":\"/mnt/tank/private\",\"name\":\"Private\",\"enabled\":false,\"comment\":\"Restricted access\"}" +
                "]", new TypeToken<List<SmbShare>>() {}.getType());
    }

    private void prepopulateStats() {
        StatsManager stats = StatsManager.getInstance();
        if (stats.getCpuHistory().isEmpty()) {
            for (int i = 0; i < 60; i++) {
                stats.addCpuSample((float) (10 + Math.random() * 20));
                stats.addRamSample((float) (40 + Math.random() * 5));
            }
        }
    }

    @Override
    public Call<SystemInfo> getSystemInfo() {
        return new DemoCall<>(gson.fromJson("{" +
                "\"version\":\"TrueNAS-SCALE-23.10.1\"," +
                "\"uptime\":\"30 days, 4:12:05\"," +
                "\"model\":\"AMD Ryzen 9 9900X3D\"," +
                "\"physmem\":137438953472" +
                "}", SystemInfo.class));
    }

    @Override
    public Call<List<PoolInfo>> getPools() {
        return new DemoCall<>(gson.fromJson("[" +
                "{\"id\":1,\"name\":\"tank\",\"healthy\":true,\"status\":\"ONLINE\",\"size\":32000000000000,\"free\":12000000000000,\"allocated\":20000000000000}," +
                "{\"id\":2,\"name\":\"boot-pool\",\"healthy\":true,\"status\":\"ONLINE\",\"size\":500000000000,\"free\":450000000000,\"allocated\":50000000000}" +
                "]", new TypeToken<List<PoolInfo>>() {}.getType()));
    }

    @Override
    public Call<List<DatasetInfo>> getDatasets() {
        return new DemoCall<>(new ArrayList<>());
    }

    @Override
    public Call<List<ServiceInfo>> getServices() {
        return new DemoCall<>(new ArrayList<>(services));
    }

    @Override
    public Call<List<AlertInfo>> getAlerts() {
        return new DemoCall<>(gson.fromJson("[" +
                "{\"level\":\"INFO\",\"formatted\":\"System has been running stable for over 30 days. All services are normal.\"}," +
                "{\"level\":\"WARNING\",\"formatted\":\"Update available for Plex application.\"}" +
                "]", new TypeToken<List<AlertInfo>>() {}.getType()));
    }

    @Override
    public Call<List<VmInfo>> getVms() {
        return new DemoCall<>(new ArrayList<>(vms));
    }

    @Override
    public Call<List<IxAppInfo>> getApps() {
        return new DemoCall<>(new ArrayList<>(apps));
    }

    @Override
    public Call<List<IxAppInfo>> getChartReleases() {
        return new DemoCall<>(new ArrayList<>(apps));
    }

    @Override
    public Call<List<SmbShare>> getSmbShares() {
        return new DemoCall<>(new ArrayList<>(shares));
    }

    @Override
    public Call<List<DiskInfo>> getDisks() {
        return new DemoCall<>(gson.fromJson("[" +
                "{\"name\":\"sda\",\"serial\":\"S676NX0R102030\",\"model\":\"Samsung SSD 990 PRO 2TB\",\"size\":2000398934016,\"type\":\"SSD\"}," +
                "{\"name\":\"sdb\",\"serial\":\"Z720A1B2\",\"model\":\"Seagate IronWolf 10TB\",\"size\":10000831348736,\"type\":\"HDD\"}," +
                "{\"name\":\"sdc\",\"serial\":\"Z720A1B3\",\"model\":\"Seagate IronWolf 10TB\",\"size\":10000831348736,\"type\":\"HDD\"}," +
                "{\"name\":\"sdd\",\"serial\":\"Z720A1B4\",\"model\":\"Seagate IronWolf 10TB\",\"size\":10000831348736,\"type\":\"HDD\"}" +
                "]", new TypeToken<List<DiskInfo>>() {}.getType()));
    }

    @Override
    public Call<Map<String, Object>> getDiskDetails(String id) {
        return new DemoCall<>(new HashMap<>());
    }

    @Override
    public Call<List<Map<String, Object>>> getSnapshotTasks() {
        return new DemoCall<>(new ArrayList<>());
    }

    @Override
    public Call<List<Map<String, Object>>> getReportingData(Map<String, Object> body) {
        return new DemoCall<>(new ArrayList<>());
    }

    @Override
    public Call<ResponseBody> startService(Map<String, Object> body) {
        String name = (String) body.get("service");
        updateState(services, name, "RUNNING");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> stopService(Map<String, Object> body) {
        String name = (String) body.get("service");
        updateState(services, name, "STOPPED");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> startVm(int id) {
        updateVmState(id, "RUNNING");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> stopVm(int id) {
        updateVmState(id, "STOPPED");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> startApp(String appName) {
        updateAppState(appName, "RUNNING");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> stopApp(String appName) {
        updateAppState(appName, "STOPPED");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> rebootSystem(Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> shutdownSystem(Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> scrubPool(Integer id, Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> createSnapshot(Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<Map<String, String>> getShellToken() {
        Map<String, String> token = new HashMap<>();
        token.put("token", "demo-token-12345");
        return new DemoCall<>(token);
    }

    @Override
    public Call<ResponseBody> updateSmbShare(int id, Map<String, Object> body) {
        if (body.containsKey("enabled")) {
            boolean enabled = (boolean) body.get("enabled");
            for (SmbShare share : shares) {
                if (share.getId() == id) {
                    share.setEnabled(enabled);
                    break;
                }
            }
        }
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> updateSystem(Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<Object> coreCall(Map<String, Object> body) {
        String method = (String) body.get("method");
        if ("update.check_available".equals(method)) {
            Map<String, Object> res = new HashMap<>();
            res.put("status", "AVAILABLE");
            res.put("version", "TrueNAS-SCALE-23.10.2");
            return new DemoCall<>(res);
        } else if ("chart.release.upgrade".equals(method)) {
            return new DemoCall<>(null);
        }
        return new DemoCall<>(null);
    }

    @Override
    public Call<Map<String, Object>> getDiskTemperatures(Map<String, List<String>> body) {
        Map<String, Object> temps = new HashMap<>();
        temps.put("sda", 35);
        temps.put("sdb", 42);
        temps.put("sdc", 41);
        temps.put("sdd", 43);
        return new DemoCall<>(temps);
    }

    private void updateState(List<ServiceInfo> list, String name, String state) {
        for (ServiceInfo s : list) {
            if (s.getService().equals(name)) {
                try {
                    java.lang.reflect.Field f = ServiceInfo.class.getDeclaredField("state");
                    f.setAccessible(true);
                    f.set(s, state);
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void updateVmState(int id, String state) {
        for (VmInfo v : vms) {
            if (v.getId() == id) {
                try {
                    java.lang.reflect.Field statusField = VmInfo.class.getDeclaredField("status");
                    statusField.setAccessible(true);
                    Object status = statusField.get(v);
                    java.lang.reflect.Field stateField = status.getClass().getDeclaredField("state");
                    stateField.setAccessible(true);
                    stateField.set(status, state);
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void updateAppState(String name, String state) {
        for (IxAppInfo a : apps) {
            if (a.getName().equals(name)) {
                try {
                    java.lang.reflect.Field f = IxAppInfo.class.getDeclaredField("state");
                    f.setAccessible(true);
                    f.set(a, gson.toJsonTree(state));
                } catch (Exception ignored) {}
                break;
            }
        }
    }
}
