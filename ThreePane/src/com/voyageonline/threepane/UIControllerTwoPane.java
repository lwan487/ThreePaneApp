/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;

/**
 * UI Controller for x-large devices.  Supports a multi-pane layout.
 *
 * Note: Always use {@link #commitFragmentTransaction} to operate fragment transactions,
 * so that we can easily switch between synchronous and asynchronous transactions.
 */
class UIControllerTwoPane extends UIControllerBase implements ThreePaneLayout.Callback {
    
    // Other UI elements
    protected ThreePaneLayout mThreePane;

    public UIControllerTwoPane(ItemListActivity activity) {
        super(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_item_twopane;
    }

    // ThreePaneLayoutCallback
    public void onVisiblePanesChanged(int previousVisiblePanes) {
        // If the right pane is gone, remove the message view.
        final int visiblePanes = mThreePane.getVisiblePanes();

        if (((visiblePanes & ThreePaneLayout.PANE_RIGHT) == 0) &&
                ((previousVisiblePanes & ThreePaneLayout.PANE_RIGHT) != 0)) {
            // Message view just got hidden
            unselectMessage();
        }
        
    }

    /**
     * Must be called just after the activity sets up the content view.
     */
    @Override
    public void onActivityViewReady() {
        super.onActivityViewReady();

        // Set up content
        mThreePane = (ThreePaneLayout) mActivity.findViewById(R.id.three_pane);
        mThreePane.setCallback(this);

    }

    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /** {@inheritDoc} */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void installSubItemListFragment(SubItemListFragment fragment) {
        super.installSubItemListFragment(fragment);

        if (isItemListInstalled()) {
            //getItemListFragment().setHighlightedMailbox(fragment.getMailboxId());
        }
        //getSubItemListFragment().setLayout(mThreePane);
    }

    @Override
    protected void installItemDetailFragment(ItemDetailFragment fragment) {
        super.installItemDetailFragment(fragment);

        if (isSubItemListInstalled()) {
            //getSubItemListFragment().setSelectedMessage(fragment.getMessageId());
        }
    }

    @Override
    public void openInternal(final MyContext listContext, final long messageId) {
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        updateItemList(ft, true);
        updateSubItemList(ft, true);

        if (messageId != 0) {
            updateMessageView(ft, messageId);
            mThreePane.showRightPane();
        } else {
            mThreePane.showLeftPane();
        }
        commitFragmentTransaction(ft);
    }

    /**
     * Loads the given account and optionally selects the given mailbox and message. If the
     * specified account is already selected, no actions will be performed unless
     * <code>forceReload</code> is <code>true</code>.
     *
     * @param ft {@link FragmentTransaction} to use.
     * @param clearDependentPane if true, the message list and the message view will be cleared
     */
    private void updateItemList(FragmentTransaction ft, boolean clearDependentPane) {
        if (clearDependentPane) {
            removeSubItemListFragment(ft);
            removeItemDetailFragment(ft);
        }
        ft.add(mThreePane.getLeftPaneId(),
                new ItemListFragment());
    }

    /**
     * Go back to a mailbox list view. If a message view is currently active, it will
     * be hidden.
     */
    private void goBackToMailbox() {
        if (isItemDetailInstalled()) {
            mThreePane.showLeftPane(); // Show mailbox list
        }
    }

    /**
     * Show the message list fragment for the given mailbox.
     *
     * @param ft {@link FragmentTransaction} to use.
     */
    private void updateSubItemList(FragmentTransaction ft, boolean clearDependentPane) {
        //if (mListContext.getMailboxId() != getMessageListMailboxId()) {
        //    removeMessageListFragment(ft);
        //    ft.add(mThreePane.getMiddlePaneId(), MessageListFragment.newInstance(mListContext));
        //}
        if (clearDependentPane) {
            removeItemDetailFragment(ft);
        }
    }

    /**
     * Shortcut to call {@link #updateSubItemList(FragmentTransaction, boolean)} and
     * commit.
     */
    private void updateMessageList(boolean clearDependentPane) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        updateSubItemList(ft, clearDependentPane);
        commitFragmentTransaction(ft);
    }

    /**
     * Show a message on the message view.
     *
     * @param ft {@link FragmentTransaction} to use.
     * @param messageId ID of the mailbox to load. Must never be {@link Message#NO_MESSAGE}.
     */
    private void updateMessageView(FragmentTransaction ft, long messageId) {
        if (messageId == 0) {
            throw new IllegalArgumentException();
        }

        //if (messageId == getMessageId()) {
        //    return; // nothing to do.
        //}

        removeItemDetailFragment(ft);

        ft.add(mThreePane.getRightPaneId(), new ItemDetailFragment());
    }

    /**
     * Shortcut to call {@link #updateMessageView(FragmentTransaction, long)} and commit.
     */
    @Override protected void navigateToMessage(long messageId) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        updateMessageView(ft, messageId);
        commitFragmentTransaction(ft);
    }

    /**
     * Remove the message view if shown.
     */
    private void unselectMessage() {
        commitFragmentTransaction(removeItemDetailFragment(mFragmentManager.beginTransaction()));
        if (isSubItemListInstalled()) {
            //getSubItemListFragment().setSelectedMessage(Message.NO_MESSAGE);
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean onBackPressed(boolean isSystemBackKey) {
        if (!mThreePane.isPaneCollapsible()) {
            if (mThreePane.showLeftPane()) {
                return true;
            }
        }
        return false;
    }

	public void onItemSelected(String id) {
		// TODO Auto-generated method stub
		
	}
}
