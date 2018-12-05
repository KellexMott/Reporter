package com.techart.reporter.constants;

/**
 * Created by brad on 2017/02/05.
 * Stores firebase node keys and other com.techart.reporter.constants to prevent spelling mistakes in different part of
 * the apps
 */

public class Constants {
    public static final String POST_AUTHOR = "author";
    public static final String AUTHOR_URL = "authorUrl";
    public static final String USER = "user";
    public static final String TIME_CREATED = "timeCreated";
    public static final String NUM_LIKES = "numLikes";
    public static final String NUM_COMMENTS = "numComments";
    public static final String NUM_VIEWS = "numViews";
    public static final String VIEWS_KEY = "Views";
    public static final String USERS = "Users";

    public static final String NEW_POST_SUBSCRIPTION = "newPost";

    public static final String CREATE_STORY = "Create Story";

    public static final String POST_KEY = "postKey";
    public static final String STORY_TITLE = "title";
    public static final String POST_CONTENT = "postContent";


    //Spiritual
    public static final String MESSAGE = "message";
    public static final String PROVINCE = "province";


    public static final String SUBSCRIPTIONS_KEY  = "Subscriptions";

    //Comments
    public static final String COMMENTS_KEY = "Comments";
    public static final String REPLIES_KEY = "Replies";
    public static final String COMMENT_TEXT = "commentText";
    public static final String COMMENT_KEY = "commentKey";
    public static final String POST_TYPE = "postType";
    public static final String REPLIES = "replies";
    public static final String POST_TITLE = "postTitle";



    public static final String STORY_KEY = "Stories";
    public static final String TITLE = "title";
    public static final String STORY_CATEGORY = "category";
    public static final String STORY_STATUS = "status";
    public static final String STORY_DESCRIPTION = "description";
    public static final String STORY_CHAPTERCOUNT = "chapters";


    // Notification Channel com.techart.reporter.constants

    // Name of Notification Channel for verbose notifications of background work
    public static final CharSequence VERBOSE_NOTIFICATION_CHANNEL_NAME =
            "Verbose WorkManager Notifications";
    public static String VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
            "Shows notifications whenever work starts";
    public static final CharSequence NOTIFICATION_TITLE = "Progress report";
    public static final String CHANNEL_ID = "VERBOSE_NOTIFICATION" ;
    public static final int NOTIFICATION_ID = 1;

    // Other keys
    public static final String OUTPUT_PATH = "blur_filter_outputs";
    public static final String KEY_IMAGE_URI = "KEY_IMAGE_URI";
    static final String TAG_OUTPUT = "OUTPUT";
}
