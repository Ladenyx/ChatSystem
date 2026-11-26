package com.chatsystem.common.domain;

/** E2EE device trust establishment method matching {@code e2ee_devices.trust_method} and DEC-05. */
public enum E2eeTrustMethod {
    INITIAL,
    TRUSTED_DEVICE,
    RECOVERY
}
