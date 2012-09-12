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

import java.util.Set;

import android.accounts.Account;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * UI Controller for non x-large devices.  Supports a single-pane layout.
 *
 * One one-pane, only at most one fragment can be installed at a time.
 *
 * Note: Always use {@link #commitFragmentTransaction} to operate fragment transactions,
 * so that we can easily switch between synchronous and asynchronous transactions.
 *
 * Major TODOs
 * - TODO Implement callbacks
 */
class UIControllerOnePane extends UIControllerBase {
    private static final String BUNDLE_KEY_PREVIOUS_FRAGMENT
            = "UIControllerOnePane.PREVIOUS_FRAGMENT";

    // Our custom poor-man's back stack which has only one entry at maximum.
    private Fragment mPreviousFragment;

    public UIControllerOnePane(ItemListActivity activity) {
        super(activity);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPreviousFragment != null) {
            mFragmentManager.putFragment(outState,
                    BUNDLE_KEY_PREVIOUS_FRAGMENT, mPreviousFragment);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPreviousFragment = mFragmentManager.getFragment(savedInstanceState,
                BUNDLE_KEY_PREVIOUS_FRAGMENT);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_item_onepane;
    }

    @Override
    public boolean onBackPressed(boolean isSystemBackKey) {
        // Custom back stack
        if (shouldPopFromBackStack(isSystemBackKey)) {
            popFromBackStack();
            return true;
        }

        // No entry in the back stack.
        if (isItemDetailInstalled()) {
            return true;
        } else if (isItemListInstalled()) {
            return true;
        } else if (isSubItemListInstalled()) {
            return true;
        }
        return false;
    }

    /**
     * @return currently installed {@link Fragment} (1-pane has only one at most), or null if none
     *         exists.
     */
    private Fragment getInstalledFragment() {
        if (isItemListInstalled()) {
            return getItemListFragment();
        } else if (isSubItemListInstalled()) {
            return getSubItemListFragment();
        } else if (isItemDetailInstalled()) {
            return getItemDetailFragment();
        }
        return null;
    }

    /**
     * Push the installed fragment into our custom back stack (or optionally
     * {@link FragmentTransaction#remove} it) and {@link FragmentTransaction#add} {@code fragment}.
     *
     * @param fragment {@link Fragment} to be added.
     *
     *  TODO Delay-call the whole method and use the synchronous transaction.
     */
    private void showFragment(Fragment fragment) {
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        final Fragment installed = getInstalledFragment();
        if ((installed instanceof ItemDetailFragment)
                && (fragment instanceof ItemDetailFragment)) {
            // Newer/older navigation, auto-advance, etc.
            // In this case we want to keep the backstack untouched, so that after back navigation
            // we can restore the message list, including scroll position and batch selection.
        } else {
            if (mPreviousFragment != null) {
                removeFragment(ft, mPreviousFragment);
                mPreviousFragment = null;
            }
            // Remove the current fragment or push it into the backstack.
            if (installed != null) {
                if (installed instanceof ItemDetailFragment) {
                    // Message view should never be pushed to the backstack.
                    ft.remove(installed);
                } else {
                    // Other fragments should be pushed.
                    mPreviousFragment = installed;
                    ft.detach(mPreviousFragment);
                }
            }
        }
        
        ft.replace(R.id.fragment_placeholder, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        commitFragmentTransaction(ft);
    }

    /**
     * @param isSystemBackKey <code>true</code> if the system back key was pressed.
     *        <code>false</code> if it's caused by the "home" icon click on the action bar.
     * @return true if we should pop from our custom back stack.
     */
    private boolean shouldPopFromBackStack(boolean isSystemBackKey) {
        if (mPreviousFragment == null) {
            return false; // Nothing in the back stack
        }
        if (mPreviousFragment instanceof ItemDetailFragment) {
            throw new IllegalStateException("ItemDetail should never be in backstack");
        }
        final Fragment installed = getInstalledFragment();
        if (installed == null) {
            // If no fragment is installed right now, do nothing.
            return false;
        }

        // Disallow the MailboxList--> non-inbox MessageList transition as the Mailbox list
        // is always considered "higher" than a non-inbox MessageList
        if ((mPreviousFragment instanceof SubItemListFragment)
                && (installed  instanceof ItemListFragment)) {
            return false;
        }
        return true;
    }

    /**
     * Pop from our custom back stack.
     *
     * TODO Delay-call the whole method and use the synchronous transaction.
     */
    private void popFromBackStack() {
        if (mPreviousFragment == null) {
            return;
        }
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        final Fragment installed = getInstalledFragment();
        removeFragment(ft, installed);

        // Restore listContext.
        if (mPreviousFragment instanceof ItemListFragment) {
            //setListContext(null);
        } else if (mPreviousFragment instanceof SubItemListFragment) {
            //setListContext(((SubItemListFragment) mPreviousFragment).getListContext());
        } else {
            throw new IllegalStateException("Item detal should never be in backstack");
        }

        ft.attach(mPreviousFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        mPreviousFragment = null;
        commitFragmentTransaction(ft);
        return;
    }

    @Override protected void navigateToMessage(long messageId) {
    }

	public void onItemSelected(String id) {
		// TODO Auto-generated method stub		
	}

	@Override
	protected void openInternal(MyContext listContext, long messageId) {
		// TODO Auto-generated method stub
		
	}
}
