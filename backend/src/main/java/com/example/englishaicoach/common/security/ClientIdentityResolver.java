package com.example.englishaicoach.common.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;

/** Chỉ nhận chuỗi forwarding khi peer trực tiếp là proxy đã được cấu hình tin cậy. */
public final class ClientIdentityResolver {
    private final Set<String> trustedProxies;

    public ClientIdentityResolver(List<String> trustedProxies) {
        Set<String> normalized = new HashSet<>();
        for (String address : trustedProxies) {
            String parsed = literalAddress(address);
            if (parsed == null) {
                throw new IllegalArgumentException("Trusted proxy phải là địa chỉ IP tường minh");
            }
            normalized.add(parsed);
        }
        this.trustedProxies = Set.copyOf(normalized);
    }

    public String resolve(HttpServletRequest request) {
        String peer = literalAddress(request.getRemoteAddr());
        if (peer == null || !trustedProxies.contains(peer)) {
            return request.getRemoteAddr();
        }
        var headers = request.getHeaders("X-Forwarded-For");
        if (!headers.hasMoreElements()) {
            return peer;
        }
        String chain = headers.nextElement();
        if (headers.hasMoreElements() || chain.length() > 1024) {
            return peer;
        }
        String[] hops = chain.split(",", -1);
        if (hops.length > 20) {
            return peer;
        }
        for (int index = hops.length - 1; index >= 0; index--) {
            String address = literalAddress(hops[index].trim());
            if (address == null) {
                return peer;
            }
            if (!trustedProxies.contains(address)) {
                return address;
            }
        }
        return peer;
    }

    private static String literalAddress(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.contains(".")) {
            String[] octets = value.split("\\.", -1);
            if (octets.length != 4) {
                return null;
            }
            for (String octet : octets) {
                if (!octet.matches("[0-9]{1,3}") || Integer.parseInt(octet) > 255) {
                    return null;
                }
            }
        } else if (!value.contains(":") || !value.matches("[0-9a-fA-F:]+")) {
            return null;
        }
        try {
            return InetAddress.getByName(value).getHostAddress();
        } catch (UnknownHostException exception) {
            return null;
        }
    }
}
