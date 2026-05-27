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
        populateData();
        initStats();
    }

    public static synchronized DemoTrueNasApi getInstance() {
        if (instance == null) {
            instance = new DemoTrueNasApi();
        }
        return instance;
    }

    private void populateData() {
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

    private void initStats() {
        StatsManager stats = StatsManager.getInstance();
        if (stats.getProcessorUsageList().isEmpty()) {
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
    public Call<List<NetworkInterface>> getNetworkInterfaces() {
        return queryNetworkInterfaces(new ArrayList<>());
    }

    public Call<List<NetworkInterface>> queryNetworkInterfaces(List<Object> body) {
        return new DemoCall<>(gson.fromJson("[" +
                "{\"name\":\"enp0s31f6\",\"type\":\"LINK_AGGREGATION\",\"link_state\":\"LINK_STATE_UP\",\"aliases\":[{\"address\":\"192.168.1.100\",\"netmask\":24}]}," +
                "{\"name\":\"lo\",\"type\":\"LOOPBACK\",\"link_state\":\"LINK_STATE_UP\",\"aliases\":[{\"address\":\"127.0.0.1\",\"netmask\":8}]}" +
                "]", new TypeToken<List<NetworkInterface>>() {}.getType()));
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
    public Call<List<Map<String, Object>>> getReportingData(Object body) {
        return new DemoCall<>(new ArrayList<>());
    }

    @Override
    public Call<ResponseBody> startService(Map<String, Object> body) {
        String name = (String) body.get("service");
        updateServiceState(services, name, "RUNNING");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> stopService(Map<String, Object> body) {
        String name = (String) body.get("service");
        updateServiceState(services, name, "STOPPED");
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
    public Call<ResponseBody> startApp(String name) {
        updateAppState(name, "RUNNING");
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> stopApp(String name) {
        updateAppState(name, "STOPPED");
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
    public Call<ResponseBody> updateNetworkInterface(String id, Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> updateInterface(String id, Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<ResponseBody> updateSystem(Map<String, Object> body) {
        return new DemoCall<>(null);
    }

    @Override
    public Call<List<AuditEntry>> getAudits() {
        return queryAudit(null);
    }

    public Call<List<AuditEntry>> queryAudit(Map<String, Object> body) {
        String filter = null;
        if (body != null && body.containsKey("services")) {
            List<?> services = (List<?>) body.get("services");
            if (services != null && services.size() == 1) {
                filter = String.valueOf(services.get(0));
            }
        }
        
        if (filter == null && body != null && body.containsKey("query-filters")) {
            List<?> filters = (List<?>) body.get("query-filters");
            if (filters != null) {
                for (Object item : filters) {
                    if (item instanceof List) {
                        List<?> list = (List<?>) item;
                        if (list.size() >= 3 && "service".equals(list.get(0)) && "=".equals(list.get(1))) {
                            filter = String.valueOf(list.get(2));
                        }
                    }
                }
            }
        }

        List<AuditEntry> all = gson.fromJson("[" +
                "{\"service\":\"MIDDLEWARE\",\"user\":\"truenas_admin\",\"timestamp\":\"2026-05-27 19:56:03\",\"event\":\"Authentication\",\"event_data\":\"Credentials: Password login\"}," +
                "{\"service\":\"MIDDLEWARE\",\"user\":\"truenas_admin\",\"timestamp\":\"2026-05-27 19:56:03\",\"event\":\"Call Method\",\"event_data\":\"Generate authentication token for session\"}," +
                "{\"service\":\"SMB\",\"user\":\"truenas_admin\",\"timestamp\":\"2026-05-27 19:40:00\",\"event\":\"Connect\",\"event_data\":\"IP: 192.168.1.50\"}," +
                "{\"service\":\"SUDO\",\"user\":\"root\",\"timestamp\":\"2026-05-27 19:30:15\",\"event\":\"Command\",\"event_data\":\"apt update\"}," +
                "{\"service\":\"SYSTEM\",\"user\":\"system\",\"timestamp\":\"2026-05-27 19:00:00\",\"event\":\"Startup\",\"event_data\":\"System boot successful\"}" +
                "]", new TypeToken<List<AuditEntry>>() {}.getType());

        if (filter == null || filter.isEmpty() || "ALL".equalsIgnoreCase(filter)) {
            return new DemoCall<>(all);
        }

        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry entry : all) {
            if (filter.equalsIgnoreCase(entry.getService())) {
                result.add(entry);
            }
        }
        return new DemoCall<>(result);
    }

    @Override
    public Call<Object> coreCall(Map<String, Object> body) {
        String method = (String) body.get("method");
        if ("update.check_available".equals(method)) {
            Map<String, Object> res = new HashMap<>();
            res.put("status", "AVAILABLE");
            res.put("version", "TrueNAS-SCALE-23.10.2");
            return new DemoCall<>(res);
        } else if ("interface.query".equals(method)) {
            return new DemoCall<>(gson.fromJson("[" +
                "{\"name\":\"enp0s31f6\",\"type\":\"LINK_AGGREGATION\",\"link_state\":\"LINK_STATE_UP\",\"aliases\":[{\"address\":\"192.168.1.100\",\"netmask\":24}]}," +
                "{\"name\":\"lo\",\"type\":\"LOOPBACK\",\"link_state\":\"LINK_STATE_UP\",\"aliases\":[{\"address\":\"127.0.0.1\",\"netmask\":8}]}" +
                "]", new TypeToken<List<NetworkInterface>>() {}.getType()));
        } else if ("audit.query".equals(method)) {
            List<AuditEntry> all = gson.fromJson("[" +
                "{\"service\":\"MIDDLEWARE\",\"user\":\"truenas_admin\",\"timestamp\":\"2026-05-27 19:56:03\",\"event\":\"Authentication\",\"event_data\":\"Credentials: Password login\"}," +
                "{\"service\":\"MIDDLEWARE\",\"user\":\"truenas_admin\",\"timestamp\":\"2026-05-27 19:56:03\",\"event\":\"Call Method\",\"event_data\":\"Generate authentication token for session\"}," +
                "{\"service\":\"SMB\",\"user\":\"truenas_admin\",\"timestamp\":\"2026-05-27 19:40:00\",\"event\":\"Connect\",\"event_data\":\"IP: 192.168.1.50\"}," +
                "{\"service\":\"SUDO\",\"user\":\"root\",\"timestamp\":\"2026-05-27 19:30:15\",\"event\":\"Command\",\"event_data\":\"apt update\"}," +
                "{\"service\":\"SYSTEM\",\"user\":\"system\",\"timestamp\":\"2026-05-27 19:00:00\",\"event\":\"Startup\",\"event_data\":\"System boot successful\"}" +
                "]", new TypeToken<List<AuditEntry>>() {}.getType());
            return new DemoCall<>(all);
        } else if ("chart.release.upgrade".equals(method)) {
            return new DemoCall<>(null);
        } else if ("system.get_error_log".equals(method)) {
            return new DemoCall<>("[2023-11-20 10:15:33] INFO: System startup complete\n" +
                    "[2023-11-20 12:45:01] WARNING: High memory usage detected in Plex\n" +
                    "[2023-11-21 03:00:10] INFO: Scheduled snapshot created for tank/media\n" +
                    "[2023-11-22 09:22:45] INFO: SMB service restarted successfully\n" +
                    "[2023-11-23 15:10:12] ERROR: Failed to connect to update server (timeout)\n" +
                    "[2023-11-24 11:00:00] INFO: Periodic scrub started on boot-pool");
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

    private void updateServiceState(List<ServiceInfo> list, String name, String state) {
        for (ServiceInfo item : list) {
            if (item.getService().equals(name)) {
                try {
                    java.lang.reflect.Field field = ServiceInfo.class.getDeclaredField("state");
                    field.setAccessible(true);
                    field.set(item, state);
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void updateVmState(int id, String state) {
        for (VmInfo item : vms) {
            if (item.getId() == id) {
                try {
                    java.lang.reflect.Field field = VmInfo.class.getDeclaredField("status");
                    field.setAccessible(true);
                    Object status = field.get(item);
                    java.lang.reflect.Field sField = status.getClass().getDeclaredField("state");
                    sField.setAccessible(true);
                    sField.set(status, state);
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void updateAppState(String name, String state) {
        for (IxAppInfo item : apps) {
            if (item.getName().equals(name)) {
                try {
                    java.lang.reflect.Field field = IxAppInfo.class.getDeclaredField("state");
                    field.setAccessible(true);
                    field.set(item, gson.toJsonTree(state));
                } catch (Exception ignored) {}
                break;
            }
        }
    }
}
