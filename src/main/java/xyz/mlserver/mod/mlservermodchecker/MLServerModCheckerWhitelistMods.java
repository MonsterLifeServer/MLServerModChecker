package xyz.mlserver.mod.mlservermodchecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLServerModCheckerWhitelistMods {

    public static final String json_file = "https://wiki.mlserver.xyz/api/whitelist_mods/whitelist_mods.json";
    public static final String saveDir = "./config/mlservermodchecker/";
    public static final String whitelistFileName = "whitelist_mods.json";
    public static final String noWhitelistFileName = "export_no_whitelist_mods.json";

    public static List<String> getWhitelistMods() {
        // mod idのフォルダにJsonファイルを生成する。
        List<String> whitelist_mods = new ArrayList<>();
        try {
            ModList gson = getModList();
            if (gson == null) return null;
            for (Object mod : gson.getData()) whitelist_mods.add(mod.toString());
            return whitelist_mods;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ModContainer> noWhitelistMods;

    public static List<ModContainer> getNoWhitelistMods() {
        if (noWhitelistMods == null) noWhitelistMods = new ArrayList<>();
        return noWhitelistMods;
    }

    public static void fetchModList(Loader loader) {
        // Minecraftオブジェクトの取得
        List<ModContainer> modMap = loader.getModList();
        List<String> mods = getWhitelistMods();

        if (mods == null) {
            System.out.println("ホワイトリストの取得に失敗しました。");
            return;
        }

        noWhitelistMods = new ArrayList<>();

        // ユーザーが導入したModのリストを出力
        System.out.println("ユーザーが導入したMod:");
        for (ModContainer modContainer : modMap) {
            if (mods.contains(modContainer.getModId())) continue;
            noWhitelistMods.add(modContainer);
        }
    }

    private static ModList getModList() throws IOException {
        downloadFile(json_file, saveDir, whitelistFileName);

        File file = new File(saveDir + whitelistFileName);
        if (file.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.toURI().toURL().openStream()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            System.out.println(stringBuilder);
            Gson gson = new Gson();
            return gson.fromJson(stringBuilder.toString(), ModList.class);
        } else {
            return null;
        }
    }

    public static void downloadFile(String fileURL, String saveDir, String fileName) {
        try {
            // SSL証明書の検証を無効化する
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // URL接続を作成してファイルをダウンロード
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // 接続確認
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = saveDir + File.separator + fileName;
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                System.out.println("File downloaded successfully.");
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ModList {
        private final List<String> data;

        public ModList(List<String> data) {
            this.data = data;
        }

        public List<String> getData() {
            return data;
        }
    }

    public static void exportNoWhitelistMods(String json_text) {
        String file_path = saveDir + noWhitelistFileName;
        // ファイルが存在したら削除
        File file = new File(file_path);
        if (file.exists()) {
            file.delete();
        }
        // jsonに変換してファイルに書き込み
        try (Writer writer = new FileWriter(file_path)) {
            writer.write(json_text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NoModList {
        private final List<Map<String, String>> data;

        public NoModList(List<Map<String, String>> data) {
            this.data = data;
        }

        public List<Map<String, String>> getData() {
            return data;
        }
    }
}
