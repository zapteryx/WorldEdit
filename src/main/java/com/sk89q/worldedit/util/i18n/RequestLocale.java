/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.i18n;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the {@link LocaleManager] and {@link Locale} for the current thread.
 */
public class RequestLocale {

    private static final Logger log = Logger.getLogger(RequestLocale.class.getCanonicalName());
    private static final ThreadLocal<RequestLocale> threadLocal = new ThreadLocal<RequestLocale>() {
        @Override
        protected RequestLocale initialValue() {
            return new RequestLocale();
        }
    };

    private @Nullable LocaleManager manager;
    private @Nullable Locale locale;

    /**
     * Constructed by the {@link ThreadLocal}.
     */
    private RequestLocale() {
    }

    /**
     * Get the current locale.
     *
     * @return the current locale or null
     */
    @Nullable
    public Locale getLocale() {
        return locale;
    }

    /**
     * Get the current resource bundle.
     *
     * @return the current resource bundle, or null if not available
     */
    @Nullable
    public ResourceBundle getBundle() {
        return manager != null ? manager.getBundleForLocale(locale) : null;
    }

    /**
     * Set the locale manager and the locale for the current thread.
     *
     * @param manager the locale manager
     * @param locale the locale
     */
    public static void setLocale(LocaleManager manager, Locale locale) {
        checkNotNull(manager);
        checkNotNull(locale);
        RequestLocale requestLocale = threadLocal.get();
        requestLocale.manager = manager;
        requestLocale.locale = locale;
    }

    /**
     * Translate a string.
     *
     * <p>If the string is not available, then ${key} will be returned.</p>
     *
     * @param key the key
     * @return the translated string
     */
    public static String _(String key) {
        RequestLocale requestLocale = threadLocal.get();
        ResourceBundle bundle = requestLocale.getBundle();
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                log.log(Level.WARNING, "Failed to find message", e);
            }
        }

        return "${" + key + "}";
    }

    /**
     * Format a translated string.
     *
     * <p>If the string is not available, then ${key}:args will be returned.</p>
     *
     * @param key the key
     * @param args arguments
     * @return a translated string
     */
    public static String _(String key, Object... args) {
        RequestLocale requestLocale = threadLocal.get();
        ResourceBundle bundle = requestLocale.getBundle();
        if (bundle != null) {
            try {
                MessageFormat formatter = new MessageFormat(_(key));
                formatter.setLocale(requestLocale.getLocale());
                return formatter.format(args);
            } catch (MissingResourceException e) {
                log.log(Level.WARNING, "Failed to find message", e);
            }
        }

        return "${" + key + "}:" + args;
    }

}
