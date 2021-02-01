package templates;

import ru.megafon.pom.ConfigSettings;
import ru.megafon.util.Statistic;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope("prototype")
public class HTTPSClientComponent {
    private static Statistic stat = new Statistic(HTTPSClient.class);
    private URL url;
    private KeyStore ks;
    private TrustManagerFactory tmf;
    private KeyManagerFactory keyManagerFactory;
    private SSLContext sslContext;
    private HttpsURLConnection con;
    private InputStream inputStream;
    public static final String POST = "POST";

    @Autowired
    APIPOMSettings settings;


    @PostConstruct
    private void prepareConnection() {
        loadKeyStore();
        initKeyManagerFactory();
        initTrustManagerFactory();
        initSSLContext();
    }

    private void loadKeyStore() {
        try {
            char[] passphrase = settings.getPassword().toCharArray();
            ks = KeyStore.getInstance("JKS");
            FileInputStream fileInputStream = new FileInputStream(ConfigSettings.path + "keystore.jks");
            ks.load(new BufferedInputStream(fileInputStream), passphrase);
            stat.appInfo("loadKeyStore", "Сертификат APIPOM keystore.jks успешно загружен!");
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            stat.appError("loadKeyStore", "Ошибка при загрузке APIPOM сертификата keystore.jks" + e.getMessage());
        }
    }

    private void initKeyManagerFactory() {
        try {
            char[] passphrase = settings.getPassword().toCharArray();
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, passphrase);
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            stat.appError("initKeyManagerFactory", "Ошибка при инициализации KeyManagerFactory - " + e.getMessage());
        }
    }

    private void initTrustManagerFactory() {
        try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            stat.appError("initTrustManagerFactory", "Ошибка при инициализации TrustManagerFactory - " + e.getMessage());
        }
    }

    private void initSSLContext() {
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            stat.appError("initSSLContext", "Ошибка при инициализации SSLContext - " + e.getMessage());
        }
    }

    private void initConnection(String https_url, String requestMethod) {
        createConnection(https_url);
        setHeadersToConnection();
        setRequestMethodToConnection(requestMethod);
    }

    private void createConnection(String https_url) {
        try {
            url = new URL(https_url);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            con = (HttpsURLConnection) url.openConnection();
            stat.appInfo("createConnection", "Соединение с  " + url + " установлено успешно!");
            con.setDoOutput(true);
        } catch (IOException e) {
            stat.appError("createConnection", "Ошибка при создании соединения - " + e.getMessage());
        }
    }

    private void setHeadersToConnection() {
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Cache-Control", "no-cache");
    }

    private void setRequestMethodToConnection(String requestMethod) {
        try {
            con.setRequestMethod(requestMethod);
        } catch (ProtocolException e) {
            stat.appError("setRequestMethodToConnection", "Ошибка при установке метода запроса " + requestMethod + " - " + e.getMessage());
        }
    }

    private void getResponse(String requestMethod, String body) {
        int responseCode;
        if (requestMethod.equals(POST)) {
            try (
                    OutputStream out = con.getOutputStream()) {
                sendPostRequest(out, Objects.isNull(body) ? "" : body);
                stat.appInfo("getResponse", "Отправка POST запроса с телом - " + body);
            } catch (IOException e) {
                stat.appError("getResponse", "Ошибка при отправке запроса: " + body + " - " + e.getMessage());
            }
        }
        try {
            responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
                stat.appInfo("getResponse", "Ответ сервера получен! Статус - " + responseCode);
            } else {
                inputStream = con.getErrorStream();
                stat.appInfo("getResponse", "Удаленный сервер ответил с ошибкой! Статус - " + responseCode + " - " + con.getResponseMessage());
            }
        } catch (IOException e) {
            stat.appError("getResponse", "Ошибка при получении ответа от сервера! - " + e.getMessage());
        }
    }

    private String readResponse() {
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader reader;
            String line;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            inputStream.close();
            stat.appInfo("readResponse", "Ответ удаленного сервера - " + response.toString());

        } catch (IOException e) {
            stat.appError("readResponse", "Ошибка при считывании ответа от сервера! - " + e.getMessage());
        } finally {
            con.disconnect();
        }
        return response.toString();
    }

    public String doRequest(String https_url, String body, String requestMethod) {
        initConnection(https_url, requestMethod);
        getResponse(requestMethod, body);
        return readResponse();
    }

    private void sendPostRequest(OutputStream out, String body) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write(body);
            writer.flush();
        }
    }
}
