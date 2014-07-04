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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The purpose of a {@code LocaleManager} is to manage the loading of
 * {@code ResourceBundles} and maintain the "active locale" for the current
 * thread.
 * <p>
 * This class allows an "active locale" to be set for the current thread,
 * which can be used to load a {@link ResourceBundle}. In addition, an instance
 * of this class stores a {@link Control} that can determine how these bundles
 * are loaded.
 */
public class LocaleManager {

    private static final ThreadLocal<ActiveLocale> threadLocal = new ThreadLocal<ActiveLocale>();

    private final Control control;

    /**
     * Create a new instance of the locale manager.
     *
     * @param control the control
     */
    public LocaleManager(Control control) {
        checkNotNull(control);
        this.control = control;
    }

    /**
     * Get the bundle given the bundle name, a locale, and a class loader.
     *
     * @param bundleName the name of the bundle to load
     * @param locale the locale
     * @param classLoader the class loader
     * @return a resource bundle, or null if one could not be found
     */
    @Nullable
    public ResourceBundle getBundle(String bundleName, Locale locale, ClassLoader classLoader) {
        return ResourceBundle.getBundle(bundleName, locale, classLoader, control);
    }

    /**
     * Set the current thread to use the given locale (as well as this
     * {@code LocaleManager}.
     *
     * @param locale the locale
     */
    public void setLocaleForThread(Locale locale) {
        checkNotNull(locale);
        threadLocal.set(new ActiveLocale(this, locale));
    }

    /**
     * Get the bundle given the bundle name and a class loader, using the
     * the locale manager set on this thread (if available) to determine
     * the locale.
     *
     * @param bundleName the bundle name
     * @param classLoader the class loader
     * @return the bundle or null
     */
    @Nullable
    public static ResourceBundle getBundleForThread(String bundleName, ClassLoader classLoader) {
        ActiveLocale active = threadLocal.get();
        if (active != null) {
            return active.manager.getBundle(bundleName, active.locale, classLoader);
        } else {
            return null;
        }
    }

    private static class ActiveLocale {
        private final LocaleManager manager;
        private final Locale locale;

        private ActiveLocale(LocaleManager manager, Locale locale) {
            this.manager = manager;
            this.locale = locale;
        }
    }

}
