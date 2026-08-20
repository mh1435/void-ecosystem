package com.voidecosystem.app

import android.app.Application

/**
 * Composition root for cross-module singletons (DI graph, background
 * services shared by the routine scheduler / automation modules, etc).
 * Kept dependency-free for now — wire a DI framework here as the
 * ecosystem grows past stub modules.
 */
class VoidApplication : Application()
