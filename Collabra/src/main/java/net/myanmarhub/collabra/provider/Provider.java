package net.myanmarhub.collabra.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.appspot.myanhub_collabra.collabra.model.Conversation;
import com.appspot.myanhub_collabra.collabra.model.User;

import net.myanmarhub.collabra.dao.ConversationDAO;
import net.myanmarhub.collabra.dao.UserDAO;

/**
 * Tin Htoo Aung (Myanmar Hub) on 10/11/13.
 */
public class Provider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int USER_ITEM = 1;
    private static final int USER_DIR = 2;
    private static final int CONVERSATION_DIR = 3;
    private static final int CONVERSATION_ITEM = 4;

    static {
        URI_MATCHER.addURI(CollabraKind.AUTHORITY, CollabraKind.Conversation.CONVERSATION, CONVERSATION_DIR);
        URI_MATCHER.addURI(CollabraKind.AUTHORITY, CollabraKind.Conversation.CONVERSATION + "/#", CONVERSATION_ITEM);

        URI_MATCHER.addURI(CollabraKind.AUTHORITY, CollabraKind.User.USER, USER_DIR);
        URI_MATCHER.addURI(CollabraKind.AUTHORITY, CollabraKind.User.USER + "/#", USER_ITEM);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;
        UserDAO userDAO;
        ConversationDAO conversationDAO;
        switch (URI_MATCHER.match(uri)) {
            case USER_DIR:
                userDAO = new UserDAO(getContext());
                cursor = userDAO.getAll(null, null);
                break;
            case USER_ITEM:
                userDAO = new UserDAO(getContext());
                cursor = userDAO.getById(Long.valueOf(uri.getLastPathSegment()));
                break;
            case CONVERSATION_DIR:
                conversationDAO = new ConversationDAO(getContext());
                cursor = conversationDAO.getAll(null, null);
                break;
            case CONVERSATION_ITEM:
                conversationDAO = new ConversationDAO(getContext());
                cursor = conversationDAO.getById(Long.valueOf(uri.getLastPathSegment()));
                break;
            default:
                throw new IllegalArgumentException("Unkown URI: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case USER_ITEM:
                return CollabraKind.User.CONTENT_ITEM_TYPE;
            case USER_DIR:
                return CollabraKind.User.CONTENT_TYPE;
            case CONVERSATION_DIR:
                return CollabraKind.Conversation.CONTENT_TYPE;
            case CONVERSATION_ITEM:
                return CollabraKind.Conversation.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unkown Uri type: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = URI_MATCHER.match(uri);
        Long id;
        Uri returnUri;

        switch (uriType) {
            case USER_DIR:
                UserDAO userDAO = new UserDAO(getContext());
                id = userDAO.insert(userDAO.fromContentValues(contentValues));
                returnUri = Uri.parse(CollabraKind.User.USER + "/" + id);
                break;
            case CONVERSATION_DIR:
                ConversationDAO conversationDAO = new ConversationDAO(getContext());
                id = conversationDAO.insert(conversationDAO.fromContentValues(contentValues));
                returnUri = Uri.parse(CollabraKind.Conversation.CONVERSATION + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        int uriType = URI_MATCHER.match(uri);
        int row;
        switch (uriType) {
            case USER_ITEM:
                UserDAO userDAO = new UserDAO(getContext());
                User user = userDAO.fromContentValues(contentValues);
                row = userDAO.update(user, user.getId());
                break;
            case CONVERSATION_ITEM:
                ConversationDAO conversationDAO = new ConversationDAO(getContext());
                Conversation conversation = conversationDAO.fromContentValues(contentValues);
                row = conversationDAO.update(conversation, conversation.getId());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Integer.valueOf(String.valueOf(row));
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        int uriType = URI_MATCHER.match(uri);
        Integer row;
        UserDAO userDAO = null;
        ConversationDAO conversationDAO = null;
        switch (uriType) {
            case USER_ITEM:
                row = userDAO.delete(Long.parseLong(uri.getLastPathSegment()));
                break;
            case USER_DIR:
                row = userDAO.deleteAll();
                break;
            case CONVERSATION_ITEM:
                row = conversationDAO.delete(Long.parseLong(uri.getLastPathSegment()));
                break;
            case CONVERSATION_DIR:
                row = conversationDAO.deleteAll();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return row;
    }
}
