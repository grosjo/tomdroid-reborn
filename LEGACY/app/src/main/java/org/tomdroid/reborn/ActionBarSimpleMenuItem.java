package org.tomdroid.reborn;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * A <em>really</em> dumb implementation of the {@link android.view.MenuItem} interface, that's only
 * useful for our actionbar-compat purposes. See
 * <code>com.android.internal.view.menu.MenuItemImpl</code> in AOSP for a more complete
 * implementation.
 */
public class ActionBarSimpleMenuItem implements MenuItem {

    private ActionBarSimpleMenu mMenu;

    private final int mId;
    private final int mOrder;
    private CharSequence mTitle;
    private CharSequence mTitleCondensed;
    private Drawable mIconDrawable;
    private int mIconResId = 0;
    private boolean mEnabled = true;

    public ActionBarSimpleMenuItem(ActionBarSimpleMenu menu, int id, int order, CharSequence title) {
        mMenu = menu;
        mId = id;
        mOrder = order;
        mTitle = title;
    }

    public int getItemId() {
        return mId;
    }

    public int getOrder() {
        return mOrder;
    }

    public MenuItem setTitle(CharSequence title) {
        mTitle = title;
        return this;
    }

    public MenuItem setTitle(int titleRes) {
        return setTitle(mMenu.getContext().getString(titleRes));
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public MenuItem setTitleCondensed(CharSequence title) {
        mTitleCondensed = title;
        return this;
    }

    public CharSequence getTitleCondensed() {
        return mTitleCondensed != null ? mTitleCondensed : mTitle;
    }

    public MenuItem setIcon(Drawable icon) {
        mIconResId = 0;
        mIconDrawable = icon;
        return this;
    }

    public MenuItem setIcon(int iconResId) {
        mIconDrawable = null;
        mIconResId = iconResId;
        return this;
    }

    public Drawable getIcon() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        }

        if (mIconResId != 0) {
            return mMenu.getResources().getDrawable(mIconResId);
        }

        return null;
    }

    public MenuItem setEnabled(boolean enabled) {
        mEnabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    // No-op operations. We use no-ops to allow inflation from menu XML.

    public int getGroupId() {
        // Noop
        return 0;
    }

    public View getActionView() {
        // Noop
        return null;
    }

    @Override
    public MenuItem setActionProvider(ActionProvider actionProvider) {
        return null;
    }

    @Override
    public ActionProvider getActionProvider() {
        return null;
    }

    public boolean expandActionView() {
        // Noop
        return false;
    }

    public boolean collapseActionView() {
        // Noop
        return false;
    }

    public boolean isActionViewExpanded() {
        // Noop
        return false;
    }

    @Override
    public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
        return null;
    }

    public MenuItem setIntent(Intent intent) {
        // Noop
        return this;
    }

    public Intent getIntent() {
        // Noop
        return null;
    }

    public MenuItem setShortcut(char c, char c1) {
        // Noop
        return this;
    }

    public MenuItem setNumericShortcut(char c) {
        // Noop
        return this;
    }

    public char getNumericShortcut() {
        // Noop
        return 0;
    }

    public MenuItem setAlphabeticShortcut(char c) {
        // Noop
        return this;
    }

    public char getAlphabeticShortcut() {
        // Noop
        return 0;
    }

    public MenuItem setCheckable(boolean b) {
        // Noop
        return this;
    }

    public boolean isCheckable() {
        // Noop
        return false;
    }

    public MenuItem setChecked(boolean b) {
        // Noop
        return this;
    }

    public boolean isChecked() {
        // Noop
        return false;
    }

    public MenuItem setVisible(boolean b) {
        // Noop
        return this;
    }

    public boolean isVisible() {
        // Noop
        return true;
    }

    public boolean hasSubMenu() {
        // Noop
        return false;
    }

    public SubMenu getSubMenu() {
        // Noop
        return null;
    }

    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        // Noop
        return this;
    }

    public ContextMenu.ContextMenuInfo getMenuInfo() {
        // Noop
        return null;
    }

    public void setShowAsAction(int i) {
        // Noop
    }

    public MenuItem setShowAsActionFlags(int i) {
        // Noop
        return null;
    }

    public MenuItem setActionView(View view) {
        // Noop
        return this;
    }

    public MenuItem setActionView(int i) {
        // Noop
        return this;
    }
}
