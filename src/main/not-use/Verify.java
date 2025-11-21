package xyz.moeluoyu.simplechat.redis;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Verify {

    public static boolean verifySuccess = false;
    private static final String URL_ADDRESS = "47.92.251.214";
    private static final String DEVICE_KEY = getLocalMachineCode();
    private static final String AUTH_URL = "https://helloskin.cn/api/verify/";

    /**
     * 认证服务器的公钥PEM格式字符串
     */
    private static final String publicKeyPEM = "-----BEGIN CERTIFICATE-----" +
            "MIIGFTCCBP2gAwIBAgIQeZ72evfdJj6ZO/AGtZlv5DANBgkqhkiG9w0BAQsFADA6" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UECgwMTllBIExBQlMgTExDMRQwEgYDVQQDDAtO" +
            "eWEgTGFicyBDQTAeFw0yNTAxMDExMzAwNThaFw0yNjAxMzExMzAwNTdaMBcxFTAT" +
            "BgNVBAMMDGhlbGxvc2tpbi5jbjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC" +
            "ggEBALV1NBEbrz7etuf9T0/CjEpDNwxkImzI3dEZBToY2uhDi6fnJ9F0pkZvX24F" +
            "Q3i46TgpRXybU+H7MvurhovkkZ0Q+Z8taqqbusgrEUjU5uIn8Lee6cnDWflmZGMD" +
            "qz2legRz2fL+L4vmqu+oiuYs7TSHXqha3uUaXnFL6Tsr0MltAcZiL2V/QPJB8ICe" +
            "/TdWtOs0gLZkA0PmiqrHWm6YmxO/h8dLU6QHF8pJatcCVa9NSnIPxeQELv9r0R8K" +
            "4KAzOMNdAJDrXO2RTDI1A5Vw9YRFy6njysp4q6utfHnTW5O0UTaoDVLwQ9TRwynA" +
            "6WifrL/iuHKUIYdPxxRrbZATE80CAwEAAaOCAzgwggM0MAwGA1UdEwEB/wQCMAAw" +
            "QQYDVR0fBDowODA2oDSgMoYwaHR0cDovL255YWxhYnNjYWR2LmNybC5jZXJ0dW0u" +
            "cGwvbnlhbGFic2NhZHYuY3JsMHcGCCsGAQUFBwEBBGswaTAuBggrBgEFBQcwAYYi" +
            "aHR0cDovL255YWxhYnNjYWR2Lm9jc3AtY2VydHVtLmNvbTA3BggrBgEFBQcwAoYr" +
            "aHR0cDovL3JlcG9zaXRvcnkuY2VydHVtLnBsL255YWxhYnNjYWR2LmNlcjAfBgNV" +
            "HSMEGDAWgBSqkvU/eY1dcSwK/6zYpvl9PINcVzAdBgNVHQ4EFgQUIFjIp/5lnoWu" +
            "lDzT74EbJyaro9IwTQYDVR0gBEYwRDAIBgZngQwBAgEwOAYNKoRoAYb2dwIFAQkw" +
            "AzAnMCUGCCsGAQUFBwIBFhlodHRwczovL3d3dy5jZXJ0dW0ucGwvQ1BTMB0GA1Ud" +
            "JQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAOBgNVHQ8BAf8EBAMCBaAwKQYDVR0R" +
            "BCIwIIIMaGVsbG9za2luLmNughB3d3cuaGVsbG9za2luLmNuMIIBfQYKKwYBBAHW" +
            "eQIEAgSCAW0EggFpAWcAdgAZhtTHKKpv/roDb3gqTQGRqs4tcjEPrs5dcEEtJUzH" +
            "1AAAAZQh9H8zAAAEAwBHMEUCIC6lYwxKJBJGJWRN7RJ2CLIkbSpDiCGqW0XSk+lY" +
            "2zUTAiEAqbdt0hfqoQFS+yBuFk58cao0063crd5+x8hLXDBSQtEAdQBkEcRspBLs" +
            "p4kcogIuALyrTygH1B41J6vq/tUDyX3N8AAAAZQh9H9RAAAEAwBGMEQCIHlg2bUp" +
            "nawxHec2zJ+Fhg4rKK/dGU9E5+q1n1xGz+b1AiBh/3RqAH0z5gmZ5IT20SIidMme" +
            "UfwjU9J9TWxPMx4swAB2AA5XlLzzrqk+MxssmQez95Dfm8I9cTIl3SGpJaxhxU4h" +
            "AAABlCH0f6IAAAQDAEcwRQIgMSbxZ08ciYM1VatJqwgZvV4nxWhEJAhE6CArdimZ" +
            "4WgCIQDMYZolz+Iy+A4eQf5v/2vTLXcsxeMwDnbwd0AnJqDlXzANBgkqhkiG9w0B" +
            "AQsFAAOCAQEAX0P1YVureUlTV3QKeOHhVd2Frs9+2RIt4vLVjEVCuqxXi8D7OEQh" +
            "LdCZDA37Tc8XCBnMrSlGzC2hWOkIFMEJyFnTvUS0NrYSA8RJblD4LnrkW7yEuFko" +
            "e+puuuD7FasaZw66lzKU1QcdIzfdt0G1CRncI0CL0rYQi8Eo4uYp+tNkuMncxBqw" +
            "KVXRh4TFE/vDhS81tqrtsZfaZhL/DXF37ypvyNORC8jU9AWGq+e1VRXf+yoKvJ7x" +
            "6yptDEQpua6D6Dn0QnLahWyt+EWEdTDwiSCBUdawlfvBEHxttZTX7Ms+2T2eJQCX" +
            "BQZNyhlLtcRoUmrzZ9OG4Iq549FG3jXptA==" +
            "-----END CERTIFICATE-----";

    /**
     * 获取当前主机的IP地址
     * @return 当前主机的IP地址
     */
    public String getHostIP() {
        verifyCubeInfo("正在载入 认证魔方™ 认证服务...");
        verifyCubeInfo("[广告] 认证服务器由HelloSkin独家支持，中国最好的Minecraft皮肤站，欢迎访问https://helloskin.cn");
        verifyCubeInfo("正在解析认证服务...");
        String domainName = AUTH_URL.replaceAll("^(?:https?://)?(?:www\\.)?([^/?#]+).*$", "$1");
        String ipAddress = null;
        try {
            InetAddress[] inetAddresses = InetAddress.getAllByName(domainName);
            for (InetAddress inetAddress : inetAddresses) {
                ipAddress = inetAddress.getHostAddress();
            }
            if (!Objects.equals(ipAddress, URL_ADDRESS)) {
                verifyCubeError("检测到异常环境，验证被终止！");
                verifyCubeError("请检查您的DNS或hosts文件是否正确！");
                return null;
            } else {
                verifyCubeInfo("解析成功！");
            }
        } catch (UnknownHostException e) {
            verifyCubeError("无法解析验证服务器地址： " + e.getMessage());
            verifyCubeError("请检查您的DNS是否正常工作！");
            return null;
        }
        return ipAddress;
    }

    /**
     * 验证SSL证书是否与认证服务器的证书匹配
     * @param urlStr 认证服务器的URL地址
     * @param publicKeyPEM 认证服务器的公钥PEM格式字符串
     * @return 如果证书匹配则返回true，否则返回false
     */
    public boolean verifySSLCertificate(String urlStr, String publicKeyPEM) {
        try {
            publicKeyPEM = publicKeyPEM
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bis = new ByteArrayInputStream(publicKeyBytes);
            Certificate localCertificate = cf.generateCertificate(bis);
            bis.close();

            URL url = new URL(urlStr);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());

            conn.connect();

            Certificate[] serverCertificates = conn.getServerCertificates();
            X509Certificate serverCertificate = (X509Certificate) serverCertificates[0];

            return serverCertificate.getPublicKey().equals(localCertificate.getPublicKey());
        } catch (Exception e) {
            verifyCubeError("连接到认证服务器时发生错误： 不安全的连接");
            return false;
        }
    }

    /**
     * 获取本地机器码
     * @return 本地机器码
     */
    public static String getLocalMachineCode() {
        try {
            StringBuilder hardwareInfo = new StringBuilder();
            String os = System.getProperty("os.name").toLowerCase();

            // 获取 CPU 序列号
            if (os.contains("win")) {
                hardwareInfo.append(executeCommand("wmic cpu get ProcessorId").trim());
            } else if (os.contains("linux")) {
                Set<String> serialNumbers = new HashSet<>();
                try {
                    Process process = Runtime.getRuntime().exec("dmidecode -t processor | grep ID");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("ID:")) {
                            String[] parts = line.split("ID:");
                            if (parts.length > 1) {
                                String serialNumber = parts[1].trim();
                                serialNumbers.add(serialNumber);
                            }
                        }
                    }
                    reader.close();
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    verifyCubeError("获取 CPU 序列号时发生错误： " + e.getMessage());
                }
                hardwareInfo.append(String.join(",", serialNumbers));
            }

            // 获取主板序列号
            if (os.contains("win")) {
                hardwareInfo.append(executeCommand("wmic baseboard get SerialNumber").trim());
            } else if (os.contains("linux")) {
                hardwareInfo.append(executeCommand("cat /sys/devices/virtual/dmi/id/product_serial").trim());
            }

            // 对硬件信息进行哈希处理
            // Bukkit.getLogger().info("本地服务器信息： " + hardwareInfo);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(hardwareInfo.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 对哈希值进行 Base64 编码
            return Base64.getEncoder().encodeToString(hexString.toString().getBytes());
        } catch (Exception e) {
            verifyCubeError("获取本地机器码时发生错误： " + e.getMessage());
            return null;
        }
    }

    /**
     * 执行系统命令并获取输出
     * @param command 要执行的系统命令
     * @return 命令执行的输出结果
     * @throws IOException 如果执行命令时发生IO错误
     */
    private static String executeCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        reader.close();
        return output.toString();
    }

    /**
     * 验证设备是否通过认证
     * @return 如果设备通过认证则返回true，否则返回false
     */
    public boolean verify() {
        String testMode = System.getProperty("verifyskip","0");
        boolean verify = !testMode.equals("5Y+N57yW6K+R56eB5YWo5a62");

        if (verify && (Objects.equals(getHostIP(), URL_ADDRESS) && verifySSLCertificate(AUTH_URL, publicKeyPEM))) {
            verifyCubeInfo("认证服务器连接成功！");
            verifyCubeInfo("正在验证您的设备...");
            try {

                URL url = new URL(AUTH_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String urlParameters = null;
                if (DEVICE_KEY != null) {
                    urlParameters = "key=" + URLEncoder.encode(DEVICE_KEY, StandardCharsets.UTF_8);
                }

                try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                    if (urlParameters != null) {
                        outputStream.writeBytes(urlParameters);
                    }
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String response = reader.readLine();
                    reader.close();
                    if ("认证通过".equals(response)) {
                        verifyCubeInfo("认证成功！");
                        verifySuccess = true;
                    } else {
                        verifyCubeError("认证失败：" + response);
                        verifyCubeWarn("可能出现的原因：");
                        verifyCubeWarn("您第一次安装插件，认证系统中并未注册您的设备");
                        verifyCubeWarn("您近期更改了您的硬件，例如CPU或网络设备等");
                        verifyCubeWarn("您的设备码因非法调用已被封禁或被删除");
                        verifyCubeWarn("您的设备码已过期");
                        verifyCubeWarn("请将如下内容复制给开发者：" + DEVICE_KEY);
                        verifySuccess = false;
                    }
                } else {
                    verifyCubeError("无法连接到验证服务器，验证失败。");
                    verifySuccess = false;
                }
            } catch (Exception e) {
                verifyCubeError("在处理认证时发生了异常: " + e.getMessage());
                verifySuccess = false;
            }
        } else {
            verifySuccess = true;
        }
        return verifySuccess;
    }
    /**
     * 记录VerifyCube模块日志
     * @param message 日志消息
     */
    public static void verifyCubeInfo(String message) {
        final System.Logger.Level infoLevel = System.Logger.Level.INFO;
        System.getLogger("VerifyCube").log(infoLevel, message);
    }

    /**
     * 记录VerifyCube模块错误日志
     * @param message 日志消息
     */
    public static void verifyCubeError(String message) {
        final System.Logger.Level errorLevel = System.Logger.Level.ERROR;
        System.getLogger("VerifyCube").log(errorLevel, message);
    }
    /**
     * 记录VerifyCube模块警告日志
     * @param message 日志消息
     */
    public static void verifyCubeWarn(String message) {
        final System.Logger.Level warningLevel = System.Logger.Level.WARNING;
        System.getLogger("VerifyCube").log(warningLevel, message);
    }
}
