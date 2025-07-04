cmake_minimum_required(VERSION 3.10.2)

project("aibrother")

# Set compilation flags for Android
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -O2")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2")

# Create the native library
add_library(
        aibrother
        SHARED
        src/main/cpp/native-lib.cpp
)

# Find the log library
find_library(
        log-lib
        log
)

# Link the native library with log library
target_link_libraries(
        aibrother
        ${log-lib}
)
