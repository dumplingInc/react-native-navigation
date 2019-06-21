package com.reactnativenavigation.viewcontrollers;

import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.reactnativenavigation.parse.Options;
import com.reactnativenavigation.presentation.Presenter;
import com.reactnativenavigation.utils.StatusBarUtils;
import com.reactnativenavigation.viewcontrollers.navigator.Navigator;
import com.reactnativenavigation.views.Component;

public abstract class ChildController<T extends ViewGroup> extends ViewController<T>  {
    private final Presenter presenter;
    private final ChildControllersRegistry childRegistry;

    public ChildControllersRegistry getChildRegistry() {
        return childRegistry;
    }

    public ChildController(Activity activity, ChildControllersRegistry childRegistry, String id, Presenter presenter, Options initialOptions) {
        super(activity, id, new NoOpYellowBoxDelegate(), initialOptions);
        this.presenter = presenter;
        this.childRegistry = childRegistry;
    }

    @Override
    public T getView() {
        if (view == null) {
            super.getView();
            view.setFitsSystemWindows(true);
            if (this instanceof Navigator) return view;
//            if (this instanceof ParentController) return view;
            ViewCompat.setOnApplyWindowInsetsListener(view, this::onApplyWindowInsets);
        }
        return view;
    }

    @Override
    @CallSuper
    public void setDefaultOptions(Options defaultOptions) {
        presenter.setDefaultOptions(defaultOptions);
    }

    @Override
    public void onViewAppeared() {
        super.onViewAppeared();
        childRegistry.onViewAppeared(this);
    }

    @Override
    public void onViewDisappear() {
        super.onViewDisappear();
        childRegistry.onViewDisappear(this);
    }

    public void onViewBroughtToFront() {
        presenter.onViewBroughtToFront(resolveCurrentOptions());
    }

    @Override
    public void applyOptions(Options options) {
        super.applyOptions(options);
        Options resolvedOptions = resolveCurrentOptions();
        presenter.applyOptions(this, resolvedOptions);
    }

    @Override
    public void mergeOptions(Options options) {
        if (options == Options.EMPTY) return;
        if (isViewShown()) presenter.mergeOptions(getView(), options);
        super.mergeOptions(options);
    }

    @Override
    public void destroy() {
        if (!isDestroyed() && getView() instanceof Component) {
            performOnParentController(parent -> parent.onChildDestroyed(this));
        }
        super.destroy();
        childRegistry.onChildDestroyed(this);
    }

    protected boolean isRoot() {
        return getParentController() == null &&
                !(this instanceof Navigator) &&
                getView().getParent() != null;
    }

    private WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
        StatusBarUtils.saveStatusBarHeight(insets.getSystemWindowInsetTop());
//        if (!isViewShown()) return insets;
        Log.d("ChildController", "applyWindowInsets " + findController(view).getId() + " [" +insets.getSystemWindowInsetBottom() + "]");
//        return insets;
        return applyWindowInsets(findController(view), insets);
    }

    protected WindowInsetsCompat applyWindowInsets(ViewController view, WindowInsetsCompat insets) {
        return insets.replaceSystemWindowInsets(
                insets.getSystemWindowInsetLeft(),
                0,
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        );
    }
}
