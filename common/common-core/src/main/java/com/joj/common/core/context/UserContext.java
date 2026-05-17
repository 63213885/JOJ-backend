package com.joj.common.core.context;


import com.joj.common.core.model.entity.User;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/25 17:01
 */

public class UserContext {

    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(User user) {
        USER_HOLDER.set(user);
    }

    public static User get() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

}
