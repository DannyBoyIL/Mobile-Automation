package com.mobileAutomation.flows;

import com.mobileAutomation.pages.alerts.InvalidLoginAlert;
import com.mobileAutomation.pages.SecretPage;

public sealed interface LoginResult permits LoginResult.Success, LoginResult.Invalid {

    record Success(SecretPage page) implements LoginResult {}

    record Invalid(InvalidLoginAlert alert) implements LoginResult {}
}