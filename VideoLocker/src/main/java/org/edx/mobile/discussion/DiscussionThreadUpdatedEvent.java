package org.proversity.edx.mobile.discussion;

import android.support.annotation.NonNull;

import org.proversity.edx.mobile.discussion.DiscussionThread;

public class DiscussionThreadUpdatedEvent {

    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadUpdatedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
