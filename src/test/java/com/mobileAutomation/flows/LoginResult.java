package com.mobileAutomation.flows;

import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.SecretPage;

public sealed interface LoginResult permits LoginResult.Success, LoginResult.Invalid {

    record Success(SecretPage page) implements LoginResult {}

    record Invalid(InvalidLoginDialog dialog) implements LoginResult {}
}