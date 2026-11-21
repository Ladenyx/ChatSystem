package com.chatsystem.desktop.navigation;

/** Immutable data supplied for a view activation. Page history keeps the input for each entry. */
public interface ViewInput {
    ViewInput NONE = new ViewInput() {};
}
