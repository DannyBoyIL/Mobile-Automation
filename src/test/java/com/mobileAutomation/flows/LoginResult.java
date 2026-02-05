package com.mobileAutomation.flows;

import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.SecretPage;

public sealed interface LoginResult {

    final class Success implements LoginResult {
        private final SecretPage page;

        public Success(SecretPage page) {
            this.page = page;
        }

        public SecretPage page() {
            return page;
        }
    }

    final class Invalid implements LoginResult {
        public InvalidLoginDialog dialog() {
            return new InvalidLoginDialog();
        }
    }
}