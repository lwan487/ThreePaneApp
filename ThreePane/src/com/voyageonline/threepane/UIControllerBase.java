/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.voyageonline.threepane;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Base class for the UI controller.
 */
abstract class UIControllerBase implements 
        ItemListFragment.Callbacks, SubItemListFragment.Callbacks  {
    
	/** The owner activity */
    final ItemListActivity mActivity;
    final FragmentManager mFragmentManager;

    /**
     * Fragments that are installed.
     *
     * A fragment is installed in {@link Fragment#onActivityCreated} and uninstalled in
     * {@link Fragment#onDestroyView}, using {@link FragmentInstallable} callbacks.
     *
     * This means fragments in the back stack are *not* installed.
     *
     * We set callbacks to fragments only when they are installed.
     *
     * @see FragmentInstallable
     */
    private ItemListFragment mItemListFragment;
    private SubItemListFragment mSubItemListFragment;
    private ItemDetailFragment mItemDetailFragment;
    protected MyContext mListContext;
    
    /**
     * To avoid double-deleting a fragment (which will cause a runtime exception),
     * we put a fragment in this list when we {@link FragmentTransaction#remove(Fragment)} it,
     * and remove from the list when we actually uninstall it.
     */
    private final List<Fragment> mRemovedFragments = new LinkedList<Fragment>();

    public UIControllerBase(ItemListActivity activity) {
        mActivity = activity;
        mFragmentManager = activity.getSupportFragmentManager();
    }

    /** @return the layout ID for the activity. */
    public abstract int getLayoutId();

    /**
     * Must be called just after the activity sets up the content view.  Used to initialize views.
     *
     * (Due to the complexity regarding class/activity initialization order, we can't do this in
     * the constructor.)
     */
    public void onActivityViewReady() {

    }

    /**
     * Called at the end of {@link ItemListActivity#onCreate}.
     */
    public void onActivityCreated() {
    }

    /**
     * Handles the {@link android.app.Activity#onStart} callback.
     */
    public void onActivityStart() {
        
    }

    /**
     * Handles the {@link android.app.Activity#onResume} callback.
     */
    public void onActivityResume() {
    }

    /**
     * Handles the {@link android.app.Activity#onPause} callback.
     */
    public void onActivityPause() {
        
    }

    /**
     * Handles the {@link android.app.Activity#onStop} callback.
     */
    public void onActivityStop() {

    }

    /**
     * Handles the {@link android.app.Activity#onDestroy} callback.
     */
    public void onActivityDestroy() {
    }

    /**
     * Handles the {@link android.app.Activity#onSaveInstanceState} callback.
     */
    public void onSaveInstanceState(Bundle outState) {

    }

    /**
     * Handles the {@link android.app.Activity#onRestoreInstanceState} callback.
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        
    }

    /**
     * Install a fragment.  Must be caleld from the host activity's
     * {@link FragmentInstallable#onInstallFragment}.
     */
    public final void onInstallFragment(Fragment fragment) {
        if (fragment instanceof ItemListFragment) {
            installItemListFragment((ItemListFragment) fragment);
        } else if (fragment instanceof SubItemListFragment) {
            installSubItemListFragment((SubItemListFragment) fragment);
        } else if (fragment instanceof ItemDetailFragment) {
            installItemDetailFragment((ItemDetailFragment) fragment);
        } else {
            throw new IllegalArgumentException("Tried to install unknown fragment");
        }
    }

    /** Install fragment */
    protected void installItemListFragment(ItemListFragment fragment) {
        mItemListFragment = fragment;
        mItemListFragment.setCallback(this);
    }

    /** Install fragment */
    protected void installSubItemListFragment(SubItemListFragment fragment) {
        mSubItemListFragment = fragment;
        mSubItemListFragment.setCallback(this);
    }

    /** Install fragment */
    protected void installItemDetailFragment(ItemDetailFragment fragment) {
        mItemDetailFragment = fragment;
    }

    /**
     * Uninstall a fragment.  Must be caleld from the host activity's
     * {@link FragmentInstallable#onUninstallFragment}.
     */
    public final void onUninstallFragment(Fragment fragment) {
        mRemovedFragments.remove(fragment);
        if (fragment == mItemListFragment) {
            uninstallItemListFragment();
        } else if (fragment == mSubItemListFragment) {
            uninstallSubItemListFragment();
        } else if (fragment == mItemDetailFragment) {
            uninstallItemDetailFragment();
        } else {
            throw new IllegalArgumentException("Tried to uninstall unknown fragment");
        }
    }

    protected void uninstallItemListFragment() {
        mItemListFragment.setCallback(null);
        mItemListFragment = null;
    }

    protected void uninstallSubItemListFragment() {
        mSubItemListFragment.setCallback(null);
        mSubItemListFragment = null;
    }

    protected void uninstallItemDetailFragment() {
        mItemDetailFragment = null;
    }

    /**
     * If a {@link Fragment} is not already in {@link #mRemovedFragments},
     * {@link FragmentTransaction#remove} it and add to the list.
     *
     * Do nothing if {@code fragment} is null.
     */
    protected final void removeFragment(FragmentTransaction ft, Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (!mRemovedFragments.contains(fragment)) {
            // Remove try/catch when b/4981556 is fixed (framework bug)
            try {
                ft.remove(fragment);
            } catch (IllegalStateException ex) {
                
            }
            addFragmentToRemovalList(fragment);
        }
    }

    /**
     * Remove a {@link Fragment} from {@link #mRemovedFragments}.  No-op if {@code fragment} is
     * null.
     *
     * {@link #removeMailboxListFragment}, {@link #removeMessageListFragment} and
     * {@link #removeMessageViewFragment} all call this, so subclasses don't have to do this when
     * using them.
     *
     * However, unfortunately, subclasses have to call this manually when popping from the
     * back stack to avoid double-delete.
     */
    protected void addFragmentToRemovalList(Fragment fragment) {
        if (fragment != null) {
            mRemovedFragments.add(fragment);
        }
    }

    /**
     * Remove the fragment if it's installed.
     */
    protected FragmentTransaction removeMailboxListFragment(FragmentTransaction ft) {
        removeFragment(ft, mItemListFragment);
        return ft;
    }

    /**
     * Remove the fragment if it's installed.
     */
    protected FragmentTransaction removeSubItemListFragment(FragmentTransaction ft) {
        removeFragment(ft, mSubItemListFragment);
        return ft;
    }

    /**
     * Remove the fragment if it's installed.
     */
    protected FragmentTransaction removeItemDetailFragment(FragmentTransaction ft) {
        removeFragment(ft, mItemDetailFragment);
        return ft;
    }

    /** @return true if a {@link MailboxListFragment} is installed. */
    protected final boolean isItemListInstalled() {
        return mItemListFragment != null;
    }

    /** @return true if a {@link MessageListFragment} is installed. */
    protected final boolean isSubItemListInstalled() {
        return mSubItemListFragment != null;
    }

    protected final boolean isItemDetailInstalled() {
        return mItemDetailFragment != null;
    }

    protected final ItemListFragment getItemListFragment() {
        return mItemListFragment;
    }

    protected final SubItemListFragment getSubItemListFragment() {
        return mSubItemListFragment;
    }

    protected final ItemDetailFragment getItemDetailFragment() {
        return mItemDetailFragment;
    }

    /**
     * Commit a {@link FragmentTransaction}.
     */
    protected void commitFragmentTransaction(FragmentTransaction ft) {
        if (!ft.isEmpty()) {
            // NB: there should be no cases in which a transaction is committed after
            // onSaveInstanceState. Unfortunately, the "state loss" check also happens when in
            // LoaderCallbacks.onLoadFinished, and we wish to perform transactions there. The check
            // by the framework is conservative and prevents cases where there are transactions
            // affecting Loader lifecycles - but we have no such cases.
            // TODO: use asynchronous callbacks from loaders to avoid this implicit dependency
            ft.commitAllowingStateLoss();
            mFragmentManager.executePendingTransactions();
        }
    }


    /**
     * Performs the back action.
     *
     * @param isSystemBackKey <code>true</code> if the system back key was pressed.
     * <code>false</code> if it's caused by the "home" icon click on the action bar.
     */
    public abstract boolean onBackPressed(boolean isSystemBackKey);

    /**
     * Called when the user taps newer/older.  Subclass must implement it to open the specified
     * message.
     *
     * It's a bit different from just showing the message view fragment; on one-pane we show the
     * message view fragment but don't want to change back state.
     */
    protected abstract void navigateToMessage(long messageId);

    @Override
    public String toString() {
        return getClass().getSimpleName(); // Shown on logcat
    }
    
    /**
     * Sets the internal value of the list context for the message list.
     */
    protected void setListContext(MyContext listContext) {
        mListContext = listContext;
    }
    
    /**
     * Opens a given list
     * @param listContext the list context for the message list to open
     * @param messageId if specified and not {@link Message#NO_MESSAGE}, will open the message
     *     in the message list.
     */
    public final void open(final MyContext listContext, final long messageId) {
        setListContext(listContext);
        openInternal(listContext, messageId);
    }
    
    protected abstract void openInternal(
            final MyContext listContext, final long messageId);

}
