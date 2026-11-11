package com.elementars.eclient.friend;

import io.netty.util.internal.ConcurrentSet;

public class Friends
{
    private static ConcurrentSet<Friend> friends;

    public static void addFriend(final String name) {
        Friends.friends.add(new Friend(name));
    }

    public static void delFriend(final String name) {
        Friends.friends.remove(getFriendByName(name));
    }

    public static Friend getFriendByName(final String name) {
        for (final Friend f : Friends.friends) {
            if (f.username.equalsIgnoreCase(name)) {
                return f;
            }
        }
        return null;
    }

    public static ConcurrentSet<Friend> getFriends() {
        return Friends.friends;
    }

    public static boolean isFriend(final String name) {
        return Friends.friends.stream().anyMatch(friend -> friend.username.equalsIgnoreCase(name));
    }

    static {
        Friends.friends = new ConcurrentSet<Friend>();
    }
}
