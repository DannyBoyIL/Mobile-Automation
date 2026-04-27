package com.mobileAutomation.flows;

import com.mobileAutomation.pages.alerts.UrlNotAllowedAlert;
import com.mobileAutomation.pages.WebViewContent;

/**
 * Typed outcome of a navigation attempt from the WebView Demo screen.
 *
 * <p>Two variants — mirroring the sealed-interface idiom used by {@link LoginResult}:
 * <ul>
 *   <li>{@link Loaded} — the app accepted the URL and the webview is now rendering
 *       the remote page. The carried {@link WebViewContent} is scoped to the
 *       {@code WEBVIEW_*} context and its methods must be invoked inside a
 *       {@code ContextManager.runInWebView} block.</li>
 *   <li>{@link Blocked} — the app rejected the URL via native validation and
 *       raised the "Sorry, you are not allowed to visit that url" alert.</li>
 * </ul>
 *
 * <p>The Clear-button path is deliberately not a result variant — it doesn't fit
 * the "navigate and observe" mental model, and is exposed as a direct page
 * action on {@link com.mobileAutomation.pages.WebViewPage} instead.
 */
public sealed interface WebViewResult permits WebViewResult.Loaded, WebViewResult.Blocked {

    record Loaded(WebViewContent content) implements WebViewResult {}

    record Blocked(UrlNotAllowedAlert alert) implements WebViewResult {}
}
