cmake_minimum_required(VERSION 3.10.2)

project("aibrother")

# Set compilation flags for Android
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -O2 -DGGML_USE_CPU")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2 -DGGML_USE_CPU")

# Configure llama.cpp for Android
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_STATIC ON CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_LTO OFF CACHE BOOL "" FORCE)

# Android-specific fixes for compatibility
if(ANDROID)
    # Include our Android compatibility header
    include_directories(src/main/cpp)
    add_definitions(-DANDROID_COMPAT)
endif()

# Add llama.cpp subdirectory
add_subdirectory(external/llama.cpp EXCLUDE_FROM_ALL)

# Create the native library
add_library(
        aibrother
        SHARED
        src/main/cpp/native-lib.cpp
)

# Find required libraries
find_library(
        log-lib
        log
)

# Include llama.cpp headers
target_include_directories(aibrother PRIVATE 
    external/llama.cpp/include
    external/llama.cpp/ggml/include
    external/llama.cpp/common
)

# Link the native library with llama.cpp and system libraries
target_link_libraries(
        aibrother
        llama
        ggml
        ${log-lib}
)
