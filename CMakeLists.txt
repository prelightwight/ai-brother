cmake_minimum_required(VERSION 3.10.2)

project("aibrother")

# Set compilation flags for Android
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -O3 -DNDEBUG -fno-exceptions -fno-rtti")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -DNDEBUG")

# Disable unnecessary llama.cpp features for mobile
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_STATIC ON CACHE BOOL "" FORCE)
set(LLAMA_NATIVE OFF CACHE BOOL "" FORCE)
set(LLAMA_AVX OFF CACHE BOOL "" FORCE)
set(LLAMA_AVX2 OFF CACHE BOOL "" FORCE)
set(LLAMA_FMA OFF CACHE BOOL "" FORCE)
set(LLAMA_F16C OFF CACHE BOOL "" FORCE)

# Add llama.cpp subdirectory
add_subdirectory(external/llama.cpp)

add_library(
        llama
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        llama
        llama-static
        ${log-lib}
)

target_include_directories(
        llama PRIVATE
        external/llama.cpp/include
        external/llama.cpp/src
        external/llama.cpp/common
)
