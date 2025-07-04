#ifndef ANDROID_COMPAT_H
#define ANDROID_COMPAT_H

#ifdef __ANDROID__

// Android compatibility stubs for missing POSIX functions
#include <sys/mman.h>

// Define missing POSIX_MADV constants
#ifndef POSIX_MADV_WILLNEED
#define POSIX_MADV_WILLNEED 1
#endif

#ifndef POSIX_MADV_RANDOM
#define POSIX_MADV_RANDOM 1
#endif

// Stub implementation of posix_madvise for Android
static inline int posix_madvise(void *addr, size_t len, int advice) {
    (void)addr;
    (void)len;
    (void)advice;
    // Return success - no-op on Android
    return 0;
}

#endif // __ANDROID__

#endif // ANDROID_COMPAT_H