package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.callback.ErrorHandlingOkCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import okhttp3.Request;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseHandoutFragment extends BaseFragment implements RefreshListener {
    protected final Logger logger = new Logger(getClass().getName());

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @InjectView(R.id.weblink)
    private TextView weblink;

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_HANDOUTS, courseData.getCourse().getId(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_handout, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        weblink.setMovementMethod(LinkMovementMethod.getInstance());
        loadData();
    }

    private void loadData() {
        okHttpClientProvider.getWithOfflineCache().newCall(new Request.Builder()
                .url(courseData.getCourse().getCourse_handouts())
                .get()
                .build())
                .enqueue(new ErrorHandlingOkCallback<HandoutModel>(getActivity(),
                        HandoutModel.class, errorNotification, snackbarErrorNotification, this) {
                    @Override
                    protected void onResponse(@NonNull final HandoutModel result) {
                        if (getActivity() == null) {
                            return;
                        }

                        if (!TextUtils.isEmpty(result.handouts_html)) {
                            populateHandouts(result);
                        } else {
                            errorNotification.showError(R.string.no_handouts_to_display,
                                    FontAwesomeIcons.fa_exclamation_circle, 0, null);
                        }
                    }

                    @Override
                    protected void onFailure(@NonNull final Throwable error) {
                        super.onFailure(error);

                        if (getActivity() == null) {
                            return;
                        }
                    }

                    @Override
                    protected void onFinish() {
                        if (!EventBus.getDefault().isRegistered(CourseHandoutFragment.this)) {
                            EventBus.getDefault().registerSticky(CourseHandoutFragment.this);
                        }
                    }
                });
    }

    private void populateHandouts(HandoutModel handout) {
        String address = environment.getConfig().getApiHostURL() + handout.handouts_html.substring(handout.handouts_html.indexOf("\"")+1, handout.handouts_html.lastIndexOf("\""));
        weblink.setText(Html.fromHtml("<p><a href=" + address + ">Quick User Guide</a></p>"));
        StringBuilder buff = WebViewUtil.getIntialWebviewBuffer(getActivity(), logger);
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (!NetworkUtil.isConnected(getContext())) {
            if (!errorNotification.isShowing()) {
                snackbarErrorNotification.showOfflineError(this);
            }
        }
    }

    @Override
    public void onRefresh() {
        errorNotification.hideError();
        loadData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onRevisit() {
        if (NetworkUtil.isConnected(getActivity())) {
            snackbarErrorNotification.hideError();
        }
    }
}
