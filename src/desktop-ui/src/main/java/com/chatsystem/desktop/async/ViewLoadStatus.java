package com.chatsystem.desktop.async;

/** Canonical five UI loading/data states required across desktop views (Version 2 plan §2.3). */
public enum ViewLoadStatus {
    LOADING,
    CONTENT,
    EMPTY,
    RETRYABLE_ERROR,
    DISCONNECTED
}
