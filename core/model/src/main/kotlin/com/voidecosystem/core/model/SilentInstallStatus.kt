package com.voidecosystem.core.model

/**
 * Where the optional Shizuku-backed silent-install path currently stands.
 * Produced by :app (which owns the actual Shizuku binder calls), rendered
 * by :feature:dashboard — same split as [AppInstallState].
 */
enum class SilentInstallStatus {
    /** Shizuku isn't installed, isn't running, or hasn't been paired yet. */
    UNAVAILABLE,

    /** Shizuku is running but this app hasn't been granted its permission yet. */
    NEEDS_PERMISSION,

    /** Ready — installs/updates can skip the system installer prompt entirely. */
    READY,
}
