package top.zynorl.demo.gatewaydemo.util;

import org.apache.commons.lang.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @Author niuzy
 * @Date 2024/01/01
 **/
public class UUIDUtil {
    public UUIDUtil() {
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getRandomSlimUUID() {
        return getSlimUUID(UUID.randomUUID().toString());
    }

    public static String getSlimUUID(String uuid) {
        String[] parts = uuid.split("-");
        return StringUtils.join(parts);
    }

    public static String getFatUUID(String id) {
        return id.substring(0, 8) + '-' + id.substring(8, 12) + '-' + id.substring(12, 16) + '-' + id.substring(16, 20) + '-' + id.substring(20, 32);
    }

    public static boolean isUUID(String uuid) {
        if (uuid.length() == 36) {
            uuid = getSlimUUID(uuid);
        }

        if (uuid.length() == 32) {
            Pattern pattern = Pattern.compile("\\p{XDigit}+");
            return pattern.matcher(uuid).matches();
        } else {
            return false;
        }
    }
}
